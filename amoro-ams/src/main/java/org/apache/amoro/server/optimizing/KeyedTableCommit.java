/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.optimizing;

import static org.apache.amoro.hive.op.UpdateHiveFiles.DELETE_UNTRACKED_HIVE_FILE;
import static org.apache.amoro.hive.op.UpdateHiveFiles.SYNC_DATA_TO_HIVE;

import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.exception.OptimizingCommitException;
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.iceberg.Constants;
import org.apache.amoro.op.OverwriteBaseFiles;
import org.apache.amoro.op.SnapshotSummary;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.ContentFiles;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyedTableCommit extends UnKeyedTableCommit implements TableOptimizingCommitter {

  private static final Logger LOG = LoggerFactory.getLogger(KeyedTableCommit.class);

  protected MixedTable table;

  protected Collection<RewriteStageTask> tasks;

  protected Long fromSnapshotId;

  protected StructLikeMap<Long> fromSequenceOfPartitions;

  protected StructLikeMap<Long> toSequenceOfPartitions;

  public KeyedTableCommit(
      MixedTable table,
      Collection<RewriteStageTask> tasks,
      Long fromSnapshotId,
      Map<String, Long> fromSequence,
      Map<String, Long> toSequence) {
    super(fromSnapshotId, table, tasks);
    this.table = table;
    this.tasks = tasks;
    this.fromSnapshotId = fromSnapshotId == null ? Constants.INVALID_SNAPSHOT_ID : fromSnapshotId;
    this.fromSequenceOfPartitions = convertPartitionSequence(table, fromSequence);
    this.toSequenceOfPartitions = convertPartitionSequence(table, toSequence);
  }

  private static StructLikeMap<Long> convertPartitionSequence(
      MixedTable table, Map<String, Long> partitionSequence) {
    PartitionSpec spec = table.spec();
    StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
    partitionSequence.forEach(
        (partition, sequence) -> {
          if (spec.isUnpartitioned()) {
            results.put(TablePropertyUtil.EMPTY_STRUCT, sequence);
          } else {
            StructLike partitionData = MixedDataFiles.data(spec, partition);
            results.put(partitionData, sequence);
          }
        });
    return results;
  }

  @Override
  public void commit() throws OptimizingCommitException {
    if (tasks.isEmpty()) {
      LOG.info("No tasks to commit for table {}", table.id());
      return;
    }
    long startTime = System.currentTimeMillis();
    LOG.info(
        "Starting to commit table {} with {} tasks from snapshot {}.",
        table.id(),
        tasks.size(),
        fromSnapshotId);

    // Filter tasks with null output to avoid deleting input files of failed tasks
    List<RewriteStageTask> successTasks =
        tasks.stream().filter(task -> task.getOutput() != null).collect(Collectors.toList());

    if (successTasks.isEmpty()) {
      LOG.info("No tasks with output to commit for table {}", table.id());
      return;
    }

    // Rebuild toSequenceOfPartitions from only success tasks to avoid consuming
    // change files that belong to failed tasks
    rebuildToSequenceOfPartitions(successTasks);

    // In the scene of moving files to hive, the files will be renamed
    List<DataFile> hiveNewDataFiles = moveFile2HiveIfNeed();

    Set<DataFile> addedDataFiles = Sets.newHashSet();
    Set<DataFile> removedDataFiles = Sets.newHashSet();
    Set<DeleteFile> addedDeleteFiles = Sets.newHashSet();
    Set<DeleteFile> removedDeleteFiles = Sets.newHashSet();

    StructLikeMap<Long> partitionOptimizedSequence =
        MixedTableUtil.readOptimizedSequence(table.asKeyedTable());

    for (RewriteStageTask task : successTasks) {
      RewriteFilesInput input = task.getInput();
      if (input == null) {
        continue;
      }
      StructLike partition = partition(input);

      // Check if the partition version has expired
      if (fileInPartitionNeedSkip(
          partition, partitionOptimizedSequence, fromSequenceOfPartitions)) {
        toSequenceOfPartitions.remove(partition);
        continue;
      }

      // Only base data file need to remove
      if (input.rewrittenDataFiles() != null) {
        Arrays.stream(input.rewrittenDataFiles())
            .map(s -> (PrimaryKeyedFile) s)
            .filter(s -> s.type() == DataFileType.BASE_FILE)
            .forEach(removedDataFiles::add);
      }

      // Only position delete need to remove
      if (input.rewrittenDeleteFiles() != null) {
        Arrays.stream(input.rewrittenDeleteFiles())
            .filter(ContentFiles::isDeleteFile)
            .map(ContentFiles::asDeleteFile)
            .forEach(removedDeleteFiles::add);
      }

      RewriteFilesOutput output = task.getOutput();
      if (CollectionUtils.isNotEmpty(hiveNewDataFiles)) {
        addedDataFiles.addAll(hiveNewDataFiles);
      } else if (output.getDataFiles() != null) {
        Collections.addAll(addedDataFiles, output.getDataFiles());
      }

      if (output.getDeleteFiles() != null) {
        Collections.addAll(addedDeleteFiles, output.getDeleteFiles());
      }
    }

    try {
      executeCommit(addedDataFiles, removedDataFiles, addedDeleteFiles, removedDeleteFiles);
      LOG.info(
          "Successfully committed table {} in {} ms.",
          table.id(),
          System.currentTimeMillis() - startTime);
    } catch (Exception e) {
      // Only failures to clean files will trigger a retry
      LOG.warn("Failed to commit table {}.", table.id(), e);

      if (needMoveFile2Hive()) {
        correctHiveData(addedDataFiles, addedDeleteFiles);
      }
      throw new OptimizingCommitException("unexpected commit error ", e);
    }
  }

  private void executeCommit(
      Set<DataFile> addedDataFiles,
      Set<DataFile> removedDataFiles,
      Set<DeleteFile> addedDeleteFiles,
      Set<DeleteFile> removedDeleteFiles) {
    // overwrite files
    OverwriteBaseFiles overwriteBaseFiles = new OverwriteBaseFiles(table.asKeyedTable());
    overwriteBaseFiles.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name());
    overwriteBaseFiles.validateNoConflictingAppends(Expressions.alwaysFalse());
    overwriteBaseFiles.dynamic(false);
    toSequenceOfPartitions.forEach(overwriteBaseFiles::updateOptimizedSequence);
    addedDataFiles.forEach(overwriteBaseFiles::addFile);
    addedDeleteFiles.forEach(overwriteBaseFiles::addFile);
    removedDataFiles.forEach(overwriteBaseFiles::deleteFile);
    if (TableTypeUtil.isHive(table) && !needMoveFile2Hive()) {
      overwriteBaseFiles.set(DELETE_UNTRACKED_HIVE_FILE, "true");
      overwriteBaseFiles.set(SYNC_DATA_TO_HIVE, "true");
    }
    overwriteBaseFiles.skipEmptyCommit().commit();

    // remove delete files
    if (CollectionUtils.isNotEmpty(removedDeleteFiles)) {
      RewriteFiles rewriteFiles = table.asKeyedTable().baseTable().newRewrite();
      rewriteFiles.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name());
      rewriteFiles.rewriteFiles(
          Collections.emptySet(),
          removedDeleteFiles,
          Collections.emptySet(),
          Collections.emptySet());
      try {
        rewriteFiles.commit();
      } catch (ValidationException e) {
        LOG.warn("Iceberg RewriteFiles commit failed, but ignore", e);
      }
    }

    LOG.info(
        "{} optimize committed, delete {} files [{} posDelete files], "
            + "add {} new files [{} posDelete files]",
        table.id(),
        removedDataFiles.size(),
        removedDeleteFiles.size(),
        addedDataFiles.size(),
        addedDataFiles.size());
  }

  private boolean fileInPartitionNeedSkip(
      StructLike partitionData,
      StructLikeMap<Long> partitionOptimizedSequence,
      StructLikeMap<Long> fromSequenceOfPartitions) {
    Long optimizedSequence = partitionOptimizedSequence.getOrDefault(partitionData, -1L);
    Long fromSequence = fromSequenceOfPartitions.getOrDefault(partitionData, Long.MAX_VALUE);

    return optimizedSequence >= fromSequence;
  }

  private StructLike partition(RewriteFilesInput input) {
    return input.allFiles()[0].partition();
  }

  private void rebuildToSequenceOfPartitions(List<RewriteStageTask> successTasks) {
    toSequenceOfPartitions.clear();
    for (RewriteStageTask task : successTasks) {
      RewriteFilesInput input = task.getInput();
      if (input == null) {
        continue;
      }
      StructLike partition = partition(input);
      for (ContentFile<?> file : input.allFiles()) {
        if (ContentFiles.isDeleteFile(file)) {
          continue;
        }
        DataFileType type = ((PrimaryKeyedFile) ContentFiles.asDataFile(file)).type();
        if (type == DataFileType.INSERT_FILE || type == DataFileType.EQ_DELETE_FILE) {
          long seq = file.dataSequenceNumber();
          toSequenceOfPartitions.merge(partition, seq, Math::max);
        }
      }
    }
  }
}

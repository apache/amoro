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
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.op.OverwriteBaseFiles;
import org.apache.amoro.op.SnapshotSummary;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.exception.OptimizingCommitException;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.ContentFiles;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.util.StructLikeMap;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class KeyedTableCommit extends UnKeyedTableCommit {

  private static final Logger LOG = LoggerFactory.getLogger(KeyedTableCommit.class);

  protected MixedTable table;

  protected Collection<TaskRuntime> tasks;

  protected Long fromSnapshotId;

  protected StructLikeMap<Long> fromSequenceOfPartitions;

  protected StructLikeMap<Long> toSequenceOfPartitions;

  public KeyedTableCommit(
      MixedTable table,
      Collection<TaskRuntime> tasks,
      Long fromSnapshotId,
      StructLikeMap<Long> fromSequenceOfPartitions,
      StructLikeMap<Long> toSequenceOfPartitions) {
    super(fromSnapshotId, table, tasks);
    this.table = table;
    this.tasks = tasks;
    this.fromSnapshotId =
        fromSnapshotId == null ? AmoroServiceConstants.INVALID_SNAPSHOT_ID : fromSnapshotId;
    this.fromSequenceOfPartitions = fromSequenceOfPartitions;
    this.toSequenceOfPartitions = toSequenceOfPartitions;
  }

  @Override
  public void commit() throws OptimizingCommitException {
    if (tasks.isEmpty()) {
      LOG.info("{} found no tasks to commit", table.id());
    }
    LOG.info("{} found tasks to commit from snapshot {}", table.id(), fromSnapshotId);

    // In the scene of moving files to hive, the files will be renamed
    List<DataFile> hiveNewDataFiles = moveFile2HiveIfNeed();

    Set<DataFile> addedDataFiles = Sets.newHashSet();
    Set<DataFile> removedDataFiles = Sets.newHashSet();
    Set<DeleteFile> addedDeleteFiles = Sets.newHashSet();
    Set<DeleteFile> removedDeleteFiles = Sets.newHashSet();

    StructLikeMap<Long> partitionOptimizedSequence =
        MixedTableUtil.readOptimizedSequence(table.asKeyedTable());

    for (TaskRuntime taskRuntime : tasks) {
      RewriteFilesInput input = taskRuntime.getInput();
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

      RewriteFilesOutput output = taskRuntime.getOutput();
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
    } catch (Exception e) {
      // Only failures to clean files will trigger a retry
      LOG.warn("Optimize commit table {} failed, give up commit.", table.id(), e);

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
}

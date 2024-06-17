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

package org.apache.amoro.op;

import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.StatisticsFileUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.events.CreateSnapshotEvent;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.StructLikeMap;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/** Overwrite {@link BaseTable} and change max transaction id map */
public class OverwriteBaseFiles extends PartitionTransactionOperation {

  public static final String PROPERTIES_TRANSACTION_ID = "txId";

  private final List<DataFile> deleteFiles;
  private final List<DataFile> addFiles;
  private final List<DeleteFile> deleteDeleteFiles;
  private final List<DeleteFile> addDeleteFiles;
  private Expression deleteExpression = Expressions.alwaysFalse();
  private boolean deleteExpressionApplied = false;
  private final StructLikeMap<Long> partitionOptimizedSequence;

  private Long optimizedSequence;
  // dynamic indicate that the optimized sequence should be applied to the changed partitions
  private Boolean dynamic;
  private Expression conflictDetectionFilter = null;

  public OverwriteBaseFiles(KeyedTable table) {
    super(table);
    this.deleteFiles = Lists.newArrayList();
    this.addFiles = Lists.newArrayList();
    this.deleteDeleteFiles = Lists.newArrayList();
    this.addDeleteFiles = Lists.newArrayList();
    this.partitionOptimizedSequence = StructLikeMap.create(table.spec().partitionType());
  }

  public OverwriteBaseFiles overwriteByRowFilter(Expression expr) {
    if (expr != null) {
      deleteExpression = Expressions.or(deleteExpression, expr);
    }
    return this;
  }

  public OverwriteBaseFiles addFile(DataFile file) {
    addFiles.add(file);
    return this;
  }

  public OverwriteBaseFiles addFile(DeleteFile file) {
    addDeleteFiles.add(file);
    return this;
  }

  public OverwriteBaseFiles deleteFile(DataFile file) {
    deleteFiles.add(file);
    return this;
  }

  public OverwriteBaseFiles deleteFile(DeleteFile file) {
    deleteDeleteFiles.add(file);
    return this;
  }

  public OverwriteBaseFiles dynamic(boolean enable) {
    this.dynamic = enable;
    return this;
  }

  /**
   * Update optimized sequence for partition. The files of ChangeStore whose sequence is bigger than
   * optimized sequence should migrate to BaseStore later.
   *
   * @param partitionData - partition
   * @param sequence - optimized sequence
   * @return this for chain
   */
  public OverwriteBaseFiles updateOptimizedSequence(StructLike partitionData, long sequence) {
    Preconditions.checkArgument(
        this.dynamic == null || !this.dynamic,
        "updateOptimizedSequenceDynamically() and updateOptimizedSequence() can't be used simultaneously");
    this.partitionOptimizedSequence.put(partitionData, sequence);
    this.dynamic = false;
    return this;
  }

  /**
   * Update optimized sequence for changed partitions. The files of ChangeStore whose sequence is
   * bigger than optimized sequence should migrate to BaseStore later.
   *
   * @param sequence - optimized sequence
   * @return this for chain
   */
  public OverwriteBaseFiles updateOptimizedSequenceDynamically(long sequence) {
    Preconditions.checkArgument(
        this.dynamic == null || this.dynamic,
        "updateOptimizedSequenceDynamically() and updateOptimizedSequence() can't be used simultaneously");
    this.optimizedSequence = sequence;
    this.dynamic = true;
    return this;
  }

  public OverwriteBaseFiles validateNoConflictingAppends(Expression newConflictDetectionFilter) {
    Preconditions.checkArgument(
        newConflictDetectionFilter != null, "Conflict detection filter cannot be null");
    this.conflictDetectionFilter = newConflictDetectionFilter;
    return this;
  }

  @Override
  protected boolean isEmptyCommit() {
    applyDeleteExpression();
    return deleteFiles.isEmpty()
        && addFiles.isEmpty()
        && deleteDeleteFiles.isEmpty()
        && addDeleteFiles.isEmpty()
        && partitionOptimizedSequence.isEmpty();
  }

  @Override
  protected List<StatisticsFile> apply(Transaction transaction) {
    Preconditions.checkState(
        this.dynamic != null,
        "updateOptimizedSequence() or updateOptimizedSequenceDynamically() must be invoked");
    applyDeleteExpression();

    StructLikeMap<Long> sequenceForChangedPartitions = null;
    if (this.dynamic) {
      sequenceForChangedPartitions =
          StructLikeMap.create(transaction.table().spec().partitionType());
    }

    UnkeyedTable baseTable = keyedTable.baseTable();
    List<CreateSnapshotEvent> newSnapshots = Lists.newArrayList();

    // step1: overwrite data files
    if (!this.addFiles.isEmpty() || !this.deleteFiles.isEmpty()) {
      OverwriteFiles overwriteFiles = transaction.newOverwrite();
      overwriteFiles.set(MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST, "true");
      overwriteFiles.set(MixedTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME_EXIST, "true");

      if (conflictDetectionFilter != null && baseTable.currentSnapshot() != null) {
        overwriteFiles.conflictDetectionFilter(conflictDetectionFilter).validateNoConflictingData();
        overwriteFiles.validateFromSnapshot(baseTable.currentSnapshot().snapshotId());
      }
      if (this.dynamic) {
        for (DataFile d : this.addFiles) {
          sequenceForChangedPartitions.put(d.partition(), this.optimizedSequence);
        }
        for (DataFile d : this.deleteFiles) {
          sequenceForChangedPartitions.put(d.partition(), this.optimizedSequence);
        }
      }
      this.addFiles.forEach(overwriteFiles::addFile);
      this.deleteFiles.forEach(overwriteFiles::deleteFile);
      if (optimizedSequence != null && optimizedSequence > 0) {
        overwriteFiles.set(PROPERTIES_TRANSACTION_ID, optimizedSequence + "");
      }

      if (MapUtils.isNotEmpty(properties)) {
        properties.forEach(overwriteFiles::set);
      }
      overwriteFiles.commit();
      newSnapshots.add((CreateSnapshotEvent) overwriteFiles.updateEvent());
    }

    // step2: RowDelta/Rewrite pos-delete files
    if (CollectionUtils.isNotEmpty(addDeleteFiles)
        || CollectionUtils.isNotEmpty(deleteDeleteFiles)) {
      if (CollectionUtils.isEmpty(deleteDeleteFiles)) {
        RowDelta rowDelta = transaction.newRowDelta();
        rowDelta.set(MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST, "true");
        rowDelta.set(MixedTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME_EXIST, "true");
        if (baseTable.currentSnapshot() != null) {
          rowDelta.validateFromSnapshot(baseTable.currentSnapshot().snapshotId());
        }

        if (this.dynamic) {
          for (DeleteFile d : this.addDeleteFiles) {
            sequenceForChangedPartitions.put(d.partition(), this.optimizedSequence);
          }
        }

        addDeleteFiles.forEach(rowDelta::addDeletes);
        if (MapUtils.isNotEmpty(properties)) {
          properties.forEach(rowDelta::set);
        }
        rowDelta.commit();
        newSnapshots.add((CreateSnapshotEvent) rowDelta.updateEvent());
      } else {
        RewriteFiles rewriteFiles = transaction.newRewrite();
        rewriteFiles.set(MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST, "true");
        rewriteFiles.set(MixedTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME_EXIST, "true");
        if (baseTable.currentSnapshot() != null) {
          rewriteFiles.validateFromSnapshot(baseTable.currentSnapshot().snapshotId());
        }

        if (this.dynamic) {
          for (DeleteFile d : this.addDeleteFiles) {
            sequenceForChangedPartitions.put(d.partition(), this.optimizedSequence);
          }
          for (DeleteFile d : this.deleteDeleteFiles) {
            sequenceForChangedPartitions.put(d.partition(), this.optimizedSequence);
          }
        }
        rewriteFiles.rewriteFiles(
            Collections.emptySet(),
            new HashSet<>(deleteDeleteFiles),
            Collections.emptySet(),
            new HashSet<>(addDeleteFiles));
        if (MapUtils.isNotEmpty(properties)) {
          properties.forEach(rewriteFiles::set);
        }
        rewriteFiles.commit();
        newSnapshots.add((CreateSnapshotEvent) rewriteFiles.updateEvent());
      }
    }
    if (newSnapshots.isEmpty()) {
      return Collections.emptyList();
    }

    // step3: set optimized sequence id, optimized time
    long commitTime = System.currentTimeMillis();
    PartitionSpec spec = transaction.table().spec();
    StructLikeMap<Long> oldOptimizedSequence = MixedTableUtil.readOptimizedSequence(keyedTable);
    StructLikeMap<Long> oldOptimizedTime = MixedTableUtil.readBaseOptimizedTime(keyedTable);
    StructLikeMap<Long> optimizedSequence = StructLikeMap.create(spec.partitionType());
    StructLikeMap<Long> optimizedTime = StructLikeMap.create(spec.partitionType());
    if (oldOptimizedSequence != null) {
      optimizedSequence.putAll(oldOptimizedSequence);
    }
    if (oldOptimizedTime != null) {
      optimizedTime.putAll(oldOptimizedTime);
    }
    StructLikeMap<Long> toChangePartitionSequence;
    if (this.dynamic) {
      toChangePartitionSequence = sequenceForChangedPartitions;
    } else {
      toChangePartitionSequence = this.partitionOptimizedSequence;
    }
    toChangePartitionSequence.forEach(
        (partition, sequence) -> {
          optimizedSequence.put(partition, sequence);
          optimizedTime.put(partition, commitTime);
        });

    StatisticsFile statisticsFile = null;
    List<StatisticsFile> result = Lists.newArrayList();
    for (CreateSnapshotEvent newSnapshot : newSnapshots) {
      if (statisticsFile != null) {
        result.add(StatisticsFileUtil.copyToSnapshot(statisticsFile, newSnapshot.snapshotId()));
      } else {
        Table table = transaction.table();
        StatisticsFileUtil.PartitionDataSerializer<Long> dataSerializer =
            StatisticsFileUtil.createPartitionDataSerializer(table.spec(), Long.class);
        statisticsFile =
            StatisticsFileUtil.writerBuilder(table)
                .withSnapshotId(newSnapshot.snapshotId())
                .build()
                .add(MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE, optimizedSequence, dataSerializer)
                .add(MixedTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME, optimizedTime, dataSerializer)
                .complete();
        result.add(statisticsFile);
      }
    }
    return result;
  }

  private void applyDeleteExpression() {
    if (this.deleteExpressionApplied) {
      return;
    }
    if (this.deleteExpression == null) {
      return;
    }
    try (CloseableIterable<CombinedScanTask> combinedScanTasks =
        keyedTable.newScan().filter(deleteExpression).planTasks()) {
      combinedScanTasks.forEach(
          combinedTask ->
              combinedTask
                  .tasks()
                  .forEach(
                      t -> {
                        t.dataTasks().forEach(ft -> deleteFiles.add(ft.file()));
                        t.mixedEquityDeletes().forEach(ft -> deleteFiles.add(ft.file()));
                      }));
      this.deleteExpressionApplied = true;
    } catch (IOException e) {
      throw new IllegalStateException(
          "failed when apply delete expression when overwrite files", e);
    }
  }
}

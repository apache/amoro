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

package com.netease.arctic.scan;

import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.ListMultimap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Multimaps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.relocated.com.google.common.collect.Streams;
import org.apache.iceberg.util.StructLikeMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseChangeTableScan implements ChangeTableScan {

  private final ChangeTable table;
  private StructLikeMap<Long> partitionMaxTransactionId;
  private Expression expression;

  public BaseChangeTableScan(ChangeTable table) {
    this.table = table;
  }

  @Override
  public ChangeTableScan filter(Expression expr) {
    if (expression == null) {
      expression = expr;
    } else {
      expression = Expressions.and(expr, expression);
    }
    return this;
  }

  @Override
  public ChangeTableScan partitionMaxTransactionId(StructLikeMap<Long> partitionMaxTransactionId) {
    this.partitionMaxTransactionId = partitionMaxTransactionId;
    return this;
  }

  @Override
  public CloseableIterable<ArcticFileScanTask> planTasks() {
    Snapshot currentSnapshot = table.currentSnapshot();
    if (currentSnapshot == null) {
      return CloseableIterable.empty();
    }
    ListMultimap<Long, StructLike> partitionsGroupedBySequenceTxId =
        Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
    Set<StructLike> optimizedPartitions = Sets.newHashSet();
    if (partitionMaxTransactionId != null) {
      for (Map.Entry<StructLike, Long> entry : partitionMaxTransactionId.entrySet()) {
        StructLike partition = entry.getKey();
        Long txId = entry.getValue();
        if (txId == TableProperties.PARTITION_MAX_TRANSACTION_ID_DEFAULT) {
          continue;
        }
        partitionsGroupedBySequenceTxId.put(txId, partition);
        optimizedPartitions.add(partition);
      }
    }
    Set<Long> txIds = partitionsGroupedBySequenceTxId.keySet();
    if (txIds.isEmpty()) {
      return CloseableIterable.transform(planFiles(table, currentSnapshot.snapshotId()),
          BaseArcticFileScanTask::new);
    }
    List<CloseableIterable<FileScanTask>> result = new ArrayList<>();

    List<Long> sortedTxIds = txIds.stream().sorted().collect(Collectors.toList());
    Iterable<Snapshot> snapshots = table.snapshots();
    if (!table.spec().isUnpartitioned()) {
      // Step1: plan with first snapshot
      Long firstTxId = sortedTxIds.get(0);
      long firstSnapshotId = getSnapshotIdBySnapshotSequence(snapshots, firstTxId);

      result.add(planFilesIgnorePartitions(table, firstSnapshotId, optimizedPartitions));

      // Step2: incremental plan from first snapshot to last snapshot
      for (int i = 0; i < sortedTxIds.size() - 1; i++) {
        Long thisTxId = sortedTxIds.get(i);
        Long nextTxId = sortedTxIds.get(i + 1);
        long fromSnapshotId = getSnapshotIdBySnapshotSequence(snapshots, thisTxId);
        long toSnapshotId = getSnapshotIdBySnapshotSequence(snapshots, nextTxId);
        List<StructLike> thisPartitions = partitionsGroupedBySequenceTxId.get(thisTxId);
        thisPartitions.forEach(optimizedPartitions::remove);
        Set<StructLike> ignorePartitions = Sets.newHashSet(optimizedPartitions);
        result.add(incrementalPlanFilesIgnorePartitions(table, fromSnapshotId, toSnapshotId, ignorePartitions));
      }
    }

    // Step3: incremental plan from last snapshot to current snapshot
    Long lastTxId = sortedTxIds.get(sortedTxIds.size() - 1);
    long lastSnapshotId = getSnapshotIdBySnapshotSequence(snapshots, lastTxId);
    long currentSnapshotId = table.currentSnapshot().snapshotId();
    if (lastSnapshotId != currentSnapshotId) {
      result.add(incrementalPlanFilesIgnorePartitions(table, lastSnapshotId, currentSnapshotId,
          Collections.emptySet()));
    }
    return CloseableIterable.transform(CloseableIterable.concat(result), BaseArcticFileScanTask::new);
  }

  private long getSnapshotIdBySnapshotSequence(Iterable<Snapshot> snapshots, long sequenceNumber) {
    return Streams.stream(snapshots)
        .filter(s -> s.sequenceNumber() == sequenceNumber)
        .map(Snapshot::snapshotId)
        .findAny()
        .orElseThrow(() -> new IllegalStateException("can't find snapshot of snapshot sequence " + sequenceNumber));
  }

  private CloseableIterable<FileScanTask> incrementalPlanFiles(UnkeyedTable internalTable, long fromSnapshotId,
                                                               long toSnapshotId) {
    TableScan scan = internalTable.newScan();
    if (fromSnapshotId == toSnapshotId) {
      return CloseableIterable.empty();
    }
    if (fromSnapshotId != -1) {
      scan = scan.appendsBetween(fromSnapshotId, toSnapshotId);
    } else {
      scan.useSnapshot(toSnapshotId);
    }
    if (this.expression != null) {
      scan = scan.filter(this.expression);
    }
    return scan.planFiles();
  }

  private CloseableIterable<FileScanTask> planFiles(UnkeyedTable internalTable, long snapshotId) {
    TableScan scan = internalTable.newScan();
    scan.useSnapshot(snapshotId);
    if (this.expression != null) {
      scan = scan.filter(this.expression);
    }
    return scan.planFiles();
  }

  private CloseableIterable<FileScanTask> planFilesIgnorePartitions(UnkeyedTable internalTable, long snapshotId,
                                                                    Set<StructLike> ignorePartitions) {
    CloseableIterable<FileScanTask> fileScanTasks = planFiles(internalTable, snapshotId);
    return CloseableIterable.filter(fileScanTasks, f -> {
      StructLike partition = f.file().partition();
      return !ignorePartitions.contains(partition);
    });
  }

  private CloseableIterable<FileScanTask> incrementalPlanFilesIgnorePartitions(UnkeyedTable internalTable,
                                                                               long fromSnapshotId,
                                                                               long toSnapshotId,
                                                                               Set<StructLike> ignorePartitions) {
    CloseableIterable<FileScanTask> fileScanTasks = incrementalPlanFiles(internalTable, fromSnapshotId, toSnapshotId);
    return CloseableIterable.filter(fileScanTasks, f -> {
      StructLike partition = f.file().partition();
      return !ignorePartitions.contains(partition);
    });
  }

}

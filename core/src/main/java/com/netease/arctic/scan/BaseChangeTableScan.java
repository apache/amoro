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
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.TablePropertyUtil;
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
    ListMultimap<Long, StructLike> partitionsGroupedBySequenceTxId =
        Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
    if (partitionMaxTransactionId != null) {
      for (Map.Entry<StructLike, Long> entry : partitionMaxTransactionId.entrySet()) {
        StructLike partition = entry.getKey();
        Long txId = entry.getValue();
        partitionsGroupedBySequenceTxId.put(txId, partition);
      }
    }
    Set<Long> txIds = partitionsGroupedBySequenceTxId.keySet();

    Iterable<Snapshot> snapshots = table.snapshots();
    List<Long> sortedTxIds = txIds.stream().sorted().collect(Collectors.toList());
    Set<StructLike> validPartitions = Sets.newHashSet();
    List<CloseableIterable<FileScanTask>> result = new ArrayList<>();
    Long fromSnapshotId = null;
    for (Long txId : sortedTxIds) {
      Long snapshotId = getSnapshotIdBySnapshotSequence(snapshots, txId);
      if (snapshotId == null) {
        throw new IllegalStateException("can't find snapshot of sequence " + txId);
      }
      if (fromSnapshotId != null) {
        result.add(incrementalPlanFilesOfPartitions(table, fromSnapshotId, snapshotId,
            Sets.newHashSet(validPartitions)));
      }
      List<StructLike> partitions = partitionsGroupedBySequenceTxId.get(txId);
      validPartitions.addAll(partitions);
      fromSnapshotId = snapshotId;
    }
    long currentSnapshotId = table.currentSnapshot().snapshotId();
    if (fromSnapshotId != null) {
      result.add(incrementalPlanFiles(table, fromSnapshotId, currentSnapshotId));
    } else {
      result.add(incrementalPlanFiles(table, -1, currentSnapshotId));
    }
    return CloseableIterable.transform(CloseableIterable.concat(result), BaseArcticFileScanTask::new);
  }

  private Long getSnapshotIdBySnapshotSequence(Iterable<Snapshot> snapshots, long sequenceNumber) {
    return Streams.stream(snapshots)
        .filter(s -> s.sequenceNumber() == sequenceNumber)
        .map(Snapshot::snapshotId)
        .findAny()
        .orElse(-1L);
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

  private CloseableIterable<FileScanTask> incrementalPlanFilesOfPartitions(UnkeyedTable internalTable,
                                                                  long fromSnapshotId,
                                                                  long toSnapshotId,
                                                                  Set<StructLike> partitions) {
    final boolean unPartitioned =
        partitions.size() == 1 && partitions.iterator().next().equals(TablePropertyUtil.EMPTY_STRUCT);
    CloseableIterable<FileScanTask> fileScanTasks = incrementalPlanFiles(internalTable, fromSnapshotId, toSnapshotId);
    return CloseableIterable.filter(fileScanTasks, f -> {
      if (unPartitioned) {
        return true;
      }
      StructLike partition = f.file().partition();
      return partitions.contains(partition);
    });
  }
}

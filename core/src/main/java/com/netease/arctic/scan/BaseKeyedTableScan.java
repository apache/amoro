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

import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.table.BaseKeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Iterators;
import org.apache.iceberg.relocated.com.google.common.collect.ListMultimap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Multimaps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.relocated.com.google.common.collect.Streams;
import org.apache.iceberg.relocated.com.google.common.collect.UnmodifiableIterator;
import org.apache.iceberg.util.BinPacking;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base implementation of {@link KeyedTableScan}, including the merge-on-read plan logical
 */
public class BaseKeyedTableScan implements KeyedTableScan {
  private static final Logger LOG = LoggerFactory.getLogger(BaseKeyedTableScan.class);

  private final BaseKeyedTable table;
  List<NodeFileScanTask> splitTasks = new ArrayList<>();
  private final Map<StructLike, List<NodeFileScanTask>> fileScanTasks = new HashMap<>();
  private final int lookBack;
  private final long openFileCost;
  private final long splitSize;
  private Expression expression;

  public BaseKeyedTableScan(BaseKeyedTable table) {
    this.table = table;
    openFileCost = PropertyUtil.propertyAsLong(table.properties(),
        TableProperties.SPLIT_OPEN_FILE_COST, TableProperties.SPLIT_OPEN_FILE_COST_DEFAULT);
    splitSize = PropertyUtil.propertyAsLong(table.properties(),
        TableProperties.SPLIT_SIZE, TableProperties.SPLIT_SIZE_DEFAULT);
    lookBack = PropertyUtil.propertyAsInt(table.properties(),
        TableProperties.SPLIT_LOOKBACK, TableProperties.SPLIT_LOOKBACK_DEFAULT);
  }

  @Override
  public KeyedTableScan filter(Expression expr) {
    if (expression == null) {
      expression = expr;
    } else {
      expression = Expressions.and(expr, expression);
    }
    return this;
  }

  @Override
  public CloseableIterable<CombinedScanTask> planTasks() {
    // base file
    List<ArcticFileScanTask> changeFileList = new ArrayList<>();
    List<ArcticFileScanTask> baseFileList = new ArrayList<>();
    table.io().doAs(() -> {
      planFiles(table.baseTable()).forEach(
          fileScanTask -> baseFileList.add(new BaseArcticFileScanTask(fileScanTask))
      );
      return null;
    });

    if (table.primaryKeySpec().primaryKeyExisted()) {
      table.io().doAs(() -> {
        planChangeFiles(changeFileList);
        return null;
      });
    }
    LOG.info("mor statistics plan change file size {},base file size {}", changeFileList.size(), baseFileList.size());

    // 1. group files by partition
    Map<StructLike, Collection<ArcticFileScanTask>> partitionedFiles =
        groupFilesByPartition(changeFileList, baseFileList);
    LOG.info("planning table {} need plan partition size {}", table.id(), partitionedFiles.size());
    partitionedFiles.forEach(this::partitionPlan);
    LOG.info("planning table {} partitionPlan end", table.id());
    // 2.split node task (FileScanTask -> FileScanTask List)
    split();
    LOG.info("planning table {} split end", table.id());
    // 3.combine node task (FileScanTask List -> CombinedScanTask)
    return combineNode(CloseableIterable.withNoopClose(splitTasks),
        splitSize, lookBack, openFileCost);
  }

  private void planChangeFiles(List<ArcticFileScanTask> collector) {
    StructLikeMap<Long> partitionMaxTxId = TablePropertyUtil.getPartitionMaxTransactionId(table);
    ListMultimap<Long, StructLike> partitionsGroupedBySequenceTxId =
        Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
    for (Map.Entry<StructLike, Long> entry : partitionMaxTxId.entrySet()) {
      StructLike partition = entry.getKey();
      Long txId = entry.getValue();
      partitionsGroupedBySequenceTxId.put(txId, partition);
    }
    Set<Long> txIds = partitionsGroupedBySequenceTxId.keySet();

    Iterable<Snapshot> snapshots = table.changeTable().snapshots();
    List<Long> sortedTxIds = txIds.stream().sorted().collect(Collectors.toList());
    Set<StructLike> validPartitions = Sets.newHashSet();
    Long fromSnapshotId = null;
    for (Long txId : sortedTxIds) {
      Long snapshotId = getSnapshotIdBySnapshotSequence(snapshots, txId);
      if (snapshotId == null) {
        throw new IllegalStateException("can't find snapshot of sequence " + txId);
      }
      if (fromSnapshotId != null) {
        incrementalPlanFilesOfPartitions(table.changeTable(), fromSnapshotId, snapshotId, validPartitions)
            .forEach(f -> collector.add(new BaseArcticFileScanTask(f)));
      }
      List<StructLike> partitions = partitionsGroupedBySequenceTxId.get(txId);
      validPartitions.addAll(partitions);
      fromSnapshotId = snapshotId;
    }
    if (fromSnapshotId != null) {
      long currentSnapshotId = table.changeTable().currentSnapshot().snapshotId();
      incrementalPlanFiles(table.changeTable(), fromSnapshotId,
          currentSnapshotId).forEach(f -> collector.add(new BaseArcticFileScanTask(f)));
    }
  }

  private Long getSnapshotIdBySnapshotSequence(Iterable<Snapshot> snapshots, long sequenceNumber) {
    return Streams.stream(snapshots)
        .filter(s -> s.sequenceNumber() == sequenceNumber)
        .map(Snapshot::snapshotId)
        .findAny()
        .orElse(-1L);
  }

  private CloseableIterable<FileScanTask> planFiles(UnkeyedTable internalTable) {
    TableScan scan = internalTable.newScan();
    if (this.expression != null) {
      scan = scan.filter(this.expression);
    }
    return scan.planFiles();
  }

  private Iterable<FileScanTask> incrementalPlanFilesOfPartitions(UnkeyedTable internalTable,
                                                                  long fromSnapshotId,
                                                                  long toSnapshotId,
                                                                  Set<StructLike> partitions) {
    final boolean unPartitioned =
        partitions.size() == 1 && partitions.iterator().next().equals(TablePropertyUtil.EMPTY_STRUCT);
    CloseableIterable<FileScanTask> fileScanTasks = incrementalPlanFiles(internalTable, fromSnapshotId, toSnapshotId);
    return Iterables.filter(fileScanTasks, f -> {
      if (unPartitioned) {
        return true;
      }
      StructLike partition = f.file().partition();
      return partitions.contains(partition);
    });
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

  private void split() {
    fileScanTasks.forEach((structLike, fileScanTasks1) -> {
      for (NodeFileScanTask task : fileScanTasks1) {
        if (task.cost() <= splitSize) {
          splitTasks.add(task);
          continue;
        }
        if (task.dataTasks().size() < 2) {
          splitTasks.add(task);
          continue;
        }
        CloseableIterable<NodeFileScanTask> tasksIterable = splitNode(CloseableIterable.withNoopClose(task.dataTasks()),
            task.arcticEquityDeletes(), splitSize, lookBack, openFileCost);
        List<NodeFileScanTask> tasks =
            Lists.newArrayList(tasksIterable);
        splitTasks.addAll(tasks);
      }
    });
  }

  public CloseableIterable<NodeFileScanTask> splitNode(
      CloseableIterable<ArcticFileScanTask> splitFiles,
      List<ArcticFileScanTask> deleteFiles,
      long splitSize, int lookback, long openFileCost) {
    Function<ArcticFileScanTask, Long> weightFunc = task -> Math.max(task.file().fileSizeInBytes(), openFileCost);
    return CloseableIterable.transform(
        CloseableIterable.combine(
            new BinPacking.PackingIterable<>(splitFiles, splitSize, lookback, weightFunc, true),
            splitFiles),
        datafiles -> packingTask(datafiles, deleteFiles));
  }

  private NodeFileScanTask packingTask(List<ArcticFileScanTask> datafiles, List<ArcticFileScanTask> deleteFiles) {
    // TODO Optimization: Add files in batch
    return new NodeFileScanTask(Stream.concat(datafiles.stream(), deleteFiles.stream()).collect(Collectors.toList()));
  }

  public CloseableIterable<CombinedScanTask> combineNode(
      CloseableIterable<NodeFileScanTask> splitFiles,
      long splitSize, int lookback, long openFileCost) {
    Function<NodeFileScanTask, Long> weightFunc = file -> Math.max(file.cost(), openFileCost);
    return CloseableIterable.transform(
        CloseableIterable.combine(
            new BinPacking.PackingIterable<>(splitFiles, splitSize, lookback, weightFunc, true),
            splitFiles),
        BaseCombinedScanTask::new);
  }

  /**
   * Construct tree node task according to partition
   * 1. Put all files into the node they originally belonged to
   * 2. Find all data nodes, traverse, and find the delete that intersects them
   */
  private void partitionPlan(StructLike partition, Collection<ArcticFileScanTask> keyedTableTasks) {
    Map<DataTreeNode, NodeFileScanTask> nodeFileScanTaskMap = new HashMap<>();
    // planfiles() cannot guarantee the uniqueness of the file,
    // so Set<path> here is used to remove duplicate files
    Set<String> pathSets = new HashSet<>();
    keyedTableTasks.forEach(task -> {
      if (!pathSets.contains(task.file().path().toString())) {
        pathSets.add(task.file().path().toString());
        DataTreeNode treeNode = task.file().node();
        NodeFileScanTask nodeFileScanTask = nodeFileScanTaskMap.getOrDefault(treeNode, new NodeFileScanTask(treeNode));
        nodeFileScanTask.addFile(task);
        nodeFileScanTaskMap.put(treeNode, nodeFileScanTask);
      }
    });

    nodeFileScanTaskMap.forEach((treeNode, nodeFileScanTask) -> {
      if (!nodeFileScanTask.isDataNode()) {
        return;
      }

      nodeFileScanTaskMap.forEach((treeNode1, nodeFileScanTask1) -> {
        if (!treeNode1.equals(treeNode) && (treeNode1.isSonOf(treeNode) || treeNode.isSonOf(treeNode1))) {
          List<ArcticFileScanTask> deletes = nodeFileScanTask1.arcticEquityDeletes().stream()
              .filter(file -> file.file().node().equals(treeNode1))
              .collect(Collectors.toList());

          nodeFileScanTask.addTasks(deletes);
        }
      });
    });

    List<NodeFileScanTask> fileScanTaskList = new ArrayList<>();
    nodeFileScanTaskMap.forEach((treeNode, nodeFileScanTask) -> {
      if (!nodeFileScanTask.isDataNode()) {
        return;
      }
      fileScanTaskList.add(nodeFileScanTask);
    });

    fileScanTasks.put(partition, fileScanTaskList);
  }

  public Map<StructLike, Collection<ArcticFileScanTask>> groupFilesByPartition(List<ArcticFileScanTask> changeTasks,
      List<ArcticFileScanTask> baseTasks) {
    ListMultimap<StructLike, ArcticFileScanTask> filesGroupedByPartition
        = Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);

    changeTasks.forEach(task -> filesGroupedByPartition.put(task.file().partition(), task));
    baseTasks.forEach(task -> filesGroupedByPartition.put(task.file().partition(), task));
    return filesGroupedByPartition.asMap();
  }
}
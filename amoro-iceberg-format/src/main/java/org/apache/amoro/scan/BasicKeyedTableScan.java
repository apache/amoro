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

package org.apache.amoro.scan;

import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.data.DefaultKeyedFile;
import org.apache.amoro.scan.expressions.BasicPartitionEvaluator;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.BasicKeyedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.BinPacking;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

/** Basic implementation of {@link KeyedTableScan}, including the merge-on-read plan logical */
public class BasicKeyedTableScan implements KeyedTableScan {
  private static final Logger LOG = LoggerFactory.getLogger(BasicKeyedTableScan.class);

  private final BasicKeyedTable table;
  List<NodeFileScanTask> splitTasks = new ArrayList<>();
  private final StructLikeMap<List<NodeFileScanTask>> fileScanTasks;
  private final int lookBack;
  private final long openFileCost;
  private final long splitSize;
  private Double splitTaskByDeleteRatio;
  private Expression expression;

  public BasicKeyedTableScan(BasicKeyedTable table) {
    this.table = table;
    this.openFileCost =
        PropertyUtil.propertyAsLong(
            table.properties(),
            TableProperties.SPLIT_OPEN_FILE_COST,
            TableProperties.SPLIT_OPEN_FILE_COST_DEFAULT);
    this.splitSize =
        PropertyUtil.propertyAsLong(
            table.properties(), TableProperties.SPLIT_SIZE, TableProperties.SPLIT_SIZE_DEFAULT);
    this.lookBack =
        PropertyUtil.propertyAsInt(
            table.properties(),
            TableProperties.SPLIT_LOOKBACK,
            TableProperties.SPLIT_LOOKBACK_DEFAULT);
    this.fileScanTasks = StructLikeMap.create(table.spec().partitionType());
  }

  /**
   * Config this scan with filter by the {@link Expression}. For Change Table, only filters related
   * to partition will take effect.
   *
   * @param expr a filter expression
   * @return scan based on this with results filtered by the expression
   */
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
    CloseableIterable<MixedFileScanTask> baseFileList;
    baseFileList = planBaseFiles();

    // change file
    CloseableIterable<MixedFileScanTask> changeFileList;
    if (table.primaryKeySpec().primaryKeyExisted()) {
      changeFileList = planChangeFiles();
    } else {
      changeFileList = CloseableIterable.empty();
    }

    // 1. group files by partition
    StructLikeMap<Collection<MixedFileScanTask>> partitionedFiles =
        groupFilesByPartition(table.spec(), changeFileList, baseFileList);
    LOG.info("planning table {} need plan partition size {}", table.id(), partitionedFiles.size());
    partitionedFiles.forEach(this::partitionPlan);
    LOG.info("planning table {} partitionPlan end", table.id());
    // 2.split node task (FileScanTask -> FileScanTask List)
    split();
    LOG.info("planning table {} split end", table.id());
    // 3.combine node task (FileScanTask List -> CombinedScanTask)
    return combineNode(
        CloseableIterable.withNoopClose(splitTasks), splitSize, lookBack, openFileCost);
  }

  @Override
  public KeyedTableScan enableSplitTaskByDeleteRatio(double splitTaskByDeleteRatio) {
    this.splitTaskByDeleteRatio = splitTaskByDeleteRatio;
    return this;
  }

  private CloseableIterable<MixedFileScanTask> planBaseFiles() {
    TableScan scan = table.baseTable().newScan();
    if (this.expression != null) {
      scan = scan.filter(this.expression);
    }
    CloseableIterable<FileScanTask> fileScanTasks = scan.planFiles();
    return CloseableIterable.transform(
        fileScanTasks,
        fileScanTask ->
            new BasicMixedFileScanTask(
                DefaultKeyedFile.parseBase(fileScanTask.file()),
                fileScanTask.deletes(),
                fileScanTask.spec(),
                expression));
  }

  private CloseableIterable<MixedFileScanTask> planChangeFiles() {
    StructLikeMap<Long> partitionOptimizedSequence = MixedTableUtil.readOptimizedSequence(table);
    Expression partitionExpressions = Expressions.alwaysTrue();
    if (expression != null) {
      // Only push down filters related to partition
      partitionExpressions = new BasicPartitionEvaluator(table.spec()).project(expression);
    }

    ChangeTableIncrementalScan changeTableScan =
        table.changeTable().newScan().fromSequence(partitionOptimizedSequence);

    changeTableScan = changeTableScan.filter(partitionExpressions);

    return CloseableIterable.transform(changeTableScan.planFiles(), s -> (MixedFileScanTask) s);
  }

  private void split() {
    fileScanTasks.forEach(
        (structLike, fileScanTasks1) -> {
          for (NodeFileScanTask task : fileScanTasks1) {
            if (task.dataTasks().size() < 2) {
              splitTasks.add(task);
              continue;
            }

            if (splitTaskByDeleteRatio != null) {
              long deleteWeight =
                  task.mixedEquityDeletes().stream()
                      .mapToLong(s -> s.file().fileSizeInBytes())
                      .map(s -> s + openFileCost)
                      .sum();

              long dataWeight =
                  task.dataTasks().stream()
                      .mapToLong(s -> s.file().fileSizeInBytes())
                      .map(s -> s + openFileCost)
                      .sum();
              double deleteRatio = deleteWeight * 1.0 / dataWeight;

              if (deleteRatio < splitTaskByDeleteRatio) {
                long targetSize =
                    Math.min(
                        new Double(deleteWeight / splitTaskByDeleteRatio).longValue(), splitSize);
                split(task, targetSize);
                continue;
              }
            }

            if (task.cost() <= splitSize) {
              splitTasks.add(task);
              continue;
            }
            split(task, splitSize);
          }
        });
  }

  private void split(NodeFileScanTask task, long targetSize) {
    CloseableIterable<NodeFileScanTask> tasksIterable =
        splitNode(
            CloseableIterable.withNoopClose(task.dataTasks()),
            task.mixedEquityDeletes(),
            targetSize,
            lookBack,
            openFileCost);
    splitTasks.addAll(Lists.newArrayList(tasksIterable));
  }

  public CloseableIterable<NodeFileScanTask> splitNode(
      CloseableIterable<MixedFileScanTask> splitFiles,
      List<MixedFileScanTask> deleteFiles,
      long splitSize,
      int lookback,
      long openFileCost) {
    Function<MixedFileScanTask, Long> weightFunc =
        task -> Math.max(task.file().fileSizeInBytes(), openFileCost);
    return CloseableIterable.transform(
        CloseableIterable.combine(
            new BinPacking.PackingIterable<>(splitFiles, splitSize, lookback, weightFunc, true),
            splitFiles),
        datafiles -> packingTask(datafiles, deleteFiles));
  }

  private NodeFileScanTask packingTask(
      List<MixedFileScanTask> datafiles, List<MixedFileScanTask> deleteFiles) {
    // TODO Optimization: Add files in batch
    return new NodeFileScanTask(
        Stream.concat(datafiles.stream(), deleteFiles.stream()).collect(Collectors.toList()));
  }

  public CloseableIterable<CombinedScanTask> combineNode(
      CloseableIterable<NodeFileScanTask> splitFiles,
      long splitSize,
      int lookback,
      long openFileCost) {
    Function<NodeFileScanTask, Long> weightFunc = file -> Math.max(file.cost(), openFileCost);
    return CloseableIterable.transform(
        CloseableIterable.combine(
            new BinPacking.PackingIterable<>(splitFiles, splitSize, lookback, weightFunc, true),
            splitFiles),
        BaseCombinedScanTask::new);
  }

  /**
   * Construct tree node task according to partition 1. Put all files into the node they originally
   * belonged to 2. Find all data nodes, traverse, and find the delete that intersects them
   */
  private void partitionPlan(StructLike partition, Collection<MixedFileScanTask> keyedTableTasks) {
    Map<DataTreeNode, NodeFileScanTask> nodeFileScanTaskMap = new HashMap<>();
    // planfiles() cannot guarantee the uniqueness of the file,
    // so Set<path> here is used to remove duplicate files
    Set<String> pathSets = new HashSet<>();
    keyedTableTasks.forEach(
        task -> {
          if (!pathSets.contains(task.file().path().toString())) {
            pathSets.add(task.file().path().toString());
            DataTreeNode treeNode = task.file().node();
            NodeFileScanTask nodeFileScanTask =
                nodeFileScanTaskMap.getOrDefault(treeNode, new NodeFileScanTask(treeNode));
            nodeFileScanTask.addFile(task);
            nodeFileScanTaskMap.put(treeNode, nodeFileScanTask);
          }
        });

    nodeFileScanTaskMap.forEach(
        (treeNode, nodeFileScanTask) -> {
          if (!nodeFileScanTask.isDataNode()) {
            return;
          }

          nodeFileScanTaskMap.forEach(
              (treeNode1, nodeFileScanTask1) -> {
                if (!treeNode1.equals(treeNode)
                    && (treeNode1.isSonOf(treeNode) || treeNode.isSonOf(treeNode1))) {
                  List<MixedFileScanTask> deletes =
                      nodeFileScanTask1.mixedEquityDeletes().stream()
                          .filter(file -> file.file().node().equals(treeNode1))
                          .collect(Collectors.toList());

                  nodeFileScanTask.addTasks(deletes);
                }
              });
        });

    List<NodeFileScanTask> fileScanTaskList = new ArrayList<>();
    nodeFileScanTaskMap.forEach(
        (treeNode, nodeFileScanTask) -> {
          if (!nodeFileScanTask.isDataNode()) {
            return;
          }
          fileScanTaskList.add(nodeFileScanTask);
        });

    fileScanTasks.put(partition, fileScanTaskList);
  }

  public StructLikeMap<Collection<MixedFileScanTask>> groupFilesByPartition(
      PartitionSpec partitionSpec,
      CloseableIterable<MixedFileScanTask> changeTasks,
      CloseableIterable<MixedFileScanTask> baseTasks) {
    StructLikeMap<Collection<MixedFileScanTask>> filesGroupedByPartition =
        StructLikeMap.create(partitionSpec.partitionType());
    try {
      changeTasks.forEach(
          task ->
              filesGroupedByPartition
                  .computeIfAbsent(task.file().partition(), k -> Lists.newArrayList())
                  .add(task));
      baseTasks.forEach(
          task ->
              filesGroupedByPartition
                  .computeIfAbsent(task.file().partition(), k -> Lists.newArrayList())
                  .add(task));
      return filesGroupedByPartition;
    } finally {
      try {
        changeTasks.close();
        baseTasks.close();
      } catch (IOException e) {
        LOG.warn("Failed to close table scan of {} ", table.id(), e);
      }
    }
  }
}

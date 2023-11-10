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

package com.netease.arctic.spark.actions;

import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.scan.BaseCombinedScanTask;
import com.netease.arctic.scan.BasicArcticFileScanTask;
import com.netease.arctic.scan.CombinedScanTask;
import com.netease.arctic.scan.NodeFileScanTask;
import com.netease.arctic.spark.actions.optimizing.KeyedTableFileScanHelper;
import com.netease.arctic.spark.actions.optimizing.KeyedTableSnapshot;
import com.netease.arctic.spark.actions.optimizing.TableFileScanHelper;
import com.netease.arctic.spark.actions.optimizing.TableSnapshot;
import com.netease.arctic.spark.util.ArcticSparkUtils;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.ListMultimap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Multimaps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.BinPacking;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyedTableRewriteAction extends BaseRewriteAction {

  private final TableFileScanHelper scanHelper;
  private final int lookBack;
  private final long openFileCost;
  private final long splitSize;
  private CloseableIterable<TableFileScanHelper.FileScanResult> rewriteFiles;
  private final TableSnapshot tableSnapshot;
  private Set<String> needRewritePartition = Sets.newHashSet();
  private Set<String> allRewritePartition = Sets.newHashSet();
  private final PartitionSpec spec;

  public KeyedTableRewriteAction(SparkSession spark, ArcticTable table) {
    super(spark, table);
    spec = table.spec();
    tableSnapshot = ArcticSparkUtils.getSnapshot(table);
    this.scanHelper =
        new KeyedTableFileScanHelper(table.asKeyedTable(), ((KeyedTableSnapshot) tableSnapshot));
    openFileCost = PropertyUtil.propertyAsLong(table.properties(),
        TableProperties.SPLIT_OPEN_FILE_COST, TableProperties.SPLIT_OPEN_FILE_COST_DEFAULT);
    splitSize = PropertyUtil.propertyAsLong(table.properties(),
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE, TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
    lookBack = PropertyUtil.propertyAsInt(table.properties(),
        TableProperties.SPLIT_LOOKBACK, TableProperties.SPLIT_LOOKBACK_DEFAULT);
  }

  @Override
  void doRewrite(String groupId) {
    new MixFormatDataRewriter(getSpark(), getTable()).doRewrite(groupId);
  }

  @Override
  CloseableIterable<CombinedScanTask> planFiles() {
    ArcticTable table = getTable();
    CloseableIterable<TableFileScanHelper.FileScanResult> fileScanResults = scanHelper.scan();

    List<Predicate<DataFile>> filters = new ArrayList<>();
    if (TableTypeUtil.isHive(table)) {
      HiveRewriteFilter hiveRewriteFilter = new HiveRewriteFilter(table, fileScanResults);
      needRewritePartition = hiveRewriteFilter.needRewriteHivePartition();
      filters.add(hiveRewriteFilter);
    } else {
      RewriteFilter rewriteFilter = new RewriteFilter(table, fileScanResults);
      needRewritePartition = rewriteFilter.needRewritePartition();
      filters.add(rewriteFilter);
    }
    rewriteFiles =
        CloseableIterable.filter(fileScanResults, (result) -> filters.stream().allMatch(e -> e.test(result.file())));
    rewriteFiles.forEach(fileScanResult -> allRewritePartition.add(spec.partitionToPath(fileScanResult.file()
        .partition())));
    Map<StructLike, Collection<TableFileScanHelper.FileScanResult>> partitionFiles =
        groupFilesByPartition(rewriteFiles);
    Map<StructLike, List<NodeFileScanTask>> fileScanTasks = partitionFiles.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> partitionNodeTask(e.getValue())));

    return combineNode(CloseableIterable.withNoopClose(split(fileScanTasks)),
        splitSize, lookBack, openFileCost);
  }

  @Override
  RewriteCommitter committer() {
    if (TableTypeUtil.isHive(getTable())) {
      return new SupportHiveRewriteCommitter(getTable(), needRewritePartition, allRewritePartition, rewriteFiles,
          tableSnapshot.snapshotId());
    } else {
      return new MixFormatRewriteCommitter(getTable(), allRewritePartition, rewriteFiles,
          tableSnapshot.snapshotId());
    }
  }

  public Map<StructLike, Collection<TableFileScanHelper.FileScanResult>> groupFilesByPartition(
      CloseableIterable<TableFileScanHelper.FileScanResult> fileScanResults) {
    ListMultimap<StructLike, TableFileScanHelper.FileScanResult> filesGroupedByPartition
        = Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
    fileScanResults.forEach(task -> filesGroupedByPartition.put(task.file().partition(), task));
    return filesGroupedByPartition.asMap();
  }

  public ArcticFileScanTask buildArcticFileScanTask(
      TableFileScanHelper.FileScanResult fileScanResult) {
    PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) fileScanResult.file();
    return new BasicArcticFileScanTask(
        primaryKeyedFile,
        fileScanResult.deleteFiles()
            .stream()
            .filter(e -> e.content() == FileContent.POSITION_DELETES)
            .map(e -> (DeleteFile) e)
            .collect(
                Collectors.toList()),
        spec);
  }

  private List<NodeFileScanTask> partitionNodeTask(
      Collection<TableFileScanHelper.FileScanResult> partitionFiles) {
    Map<DataTreeNode, NodeFileScanTask> nodeFileScanTaskMap = new HashMap<>();
    // planfiles() cannot guarantee the uniqueness of the file,
    // so Set<path> here is used to remove duplicate files
    Set<String> pathSets = new HashSet<>();
    partitionFiles.forEach(partitionFile -> {
      PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) partitionFile.file();
      if (!pathSets.contains(partitionFile.file().path().toString())) {
        pathSets.add(partitionFile.file().path().toString());
        DataTreeNode treeNode = primaryKeyedFile.node();
        NodeFileScanTask nodeFileScanTask = nodeFileScanTaskMap.getOrDefault(treeNode, new NodeFileScanTask(treeNode));
        ArcticFileScanTask task = buildArcticFileScanTask(partitionFile);
        nodeFileScanTask.addFile(task);
        nodeFileScanTaskMap.put(treeNode, nodeFileScanTask);
      }
      partitionFile.deleteFiles()
          .stream()
          .filter(e -> e.content() != FileContent.POSITION_DELETES)
          .forEach(e -> {
            PrimaryKeyedFile eqDeleteFile = (PrimaryKeyedFile) e;
            if (!pathSets.contains(eqDeleteFile.path().toString())) {
              pathSets.add(eqDeleteFile.path().toString());
              DataTreeNode treeNode = eqDeleteFile.node();
              NodeFileScanTask nodeFileScanTask =
                  nodeFileScanTaskMap.getOrDefault(treeNode, new NodeFileScanTask(treeNode));
              ArcticFileScanTask task =
                  new BasicArcticFileScanTask(eqDeleteFile, Lists.newArrayList(), spec);
              nodeFileScanTask.addFile(task);
              nodeFileScanTaskMap.put(treeNode, nodeFileScanTask);
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

    return fileScanTaskList;
  }

  private List<NodeFileScanTask> split(Map<StructLike, List<NodeFileScanTask>> fileScanTasks) {
    List<NodeFileScanTask> splitTasks = new ArrayList<>();
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
    return splitTasks;
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
}

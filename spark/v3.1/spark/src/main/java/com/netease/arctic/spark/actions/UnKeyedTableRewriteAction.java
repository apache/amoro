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

import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.scan.BasicArcticFileScanTask;
import com.netease.arctic.spark.actions.optimizing.TableFileScanHelper;
import com.netease.arctic.spark.actions.optimizing.TableSnapshot;
import com.netease.arctic.spark.actions.optimizing.UnkeyedTableFileScanHelper;
import com.netease.arctic.spark.util.ArcticSparkUtils;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.BaseCombinedScanTask;
import org.apache.iceberg.CombinedScanTask;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.BinPacking;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.StructLikeMap;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UnKeyedTableRewriteAction extends BaseRewriteAction {
  private final TableFileScanHelper scanHelper;
  private final int lookBack;
  private final long openFileCost;
  private final long splitSize;
  private CloseableIterable<TableFileScanHelper.FileScanResult> rewriteFiles;
  private final TableSnapshot tableSnapshot;
  private Set<String> needRewritePartition = Sets.newHashSet();
  private final Set<String> allRewritePartition = Sets.newHashSet();
  private final PartitionSpec spec;

  public UnKeyedTableRewriteAction(SparkSession spark, ArcticTable table) {
    super(spark, table);
    spec = table.spec();
    tableSnapshot = ArcticSparkUtils.getSnapshot(table);
    this.scanHelper =
        new UnkeyedTableFileScanHelper(table.asUnkeyedTable(), tableSnapshot.snapshotId());
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
    StructLikeMap<List<FileScanTask>> groupByPartition = groupByPartition(CloseableIterable.transform(
        rewriteFiles,
        this::buildArcticFileScanTask));

    return CloseableIterable.concat(groupByPartition.values()
        .stream()
        .map(e -> split(CloseableIterable.withNoopClose(e))).collect(Collectors.toList()));
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

  private StructLikeMap<List<FileScanTask>> groupByPartition(Iterable<FileScanTask> tasks) {
    Types.StructType partitionType = spec.partitionType();
    StructLikeMap<List<FileScanTask>> filesByPartition = StructLikeMap.create(partitionType);
    StructLike emptyStruct = GenericRecord.create(partitionType);

    for (FileScanTask task : tasks) {
      // If a task uses an incompatible partition spec the data inside could contain values
      // which belong to multiple partitions in the current spec. Treating all such files as
      // un-partitioned and grouping them together helps to minimize new files made.
      StructLike taskPartition =
          task.file().specId() == spec.specId() ? task.file().partition() : emptyStruct;

      List<FileScanTask> files = filesByPartition.get(taskPartition);
      if (files == null) {
        files = Lists.newArrayList();
      }

      files.add(task);
      filesByPartition.put(taskPartition, files);
    }
    return filesByPartition;
  }

  private CloseableIterable<CombinedScanTask> split(CloseableIterable<FileScanTask> tasks) {
    Function<FileScanTask, Long> weightFunc = task -> Math.max(task.file().fileSizeInBytes(), openFileCost);
    return CloseableIterable.transform(
        CloseableIterable.combine(
            new BinPacking.PackingIterable<>(tasks, splitSize, lookBack, weightFunc, true),
            tasks),
        BaseCombinedScanTask::new);
  }
}

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

package org.apache.amoro.server.utils;

import org.apache.amoro.IcebergFileEntry;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.hive.optimizing.plan.MixedHiveOptimizingEvaluator;
import org.apache.amoro.hive.optimizing.plan.MixedHiveOptimizingPlanner;
import org.apache.amoro.iceberg.Constants;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.optimizing.plan.AbstractOptimizingPlanner;
import org.apache.amoro.optimizing.plan.IcebergOptimizerEvaluator;
import org.apache.amoro.optimizing.plan.IcebergOptimizingPlanner;
import org.apache.amoro.optimizing.plan.MixedIcebergOptimizingEvaluator;
import org.apache.amoro.optimizing.plan.MixedIcebergOptimizingPlanner;
import org.apache.amoro.scan.TableEntriesScan;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.base.Predicate;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.BasicTableSnapshot;
import org.apache.amoro.table.KeyedTableSnapshot;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableSnapshot;
import org.apache.amoro.utils.ExpressionUtil;
import org.apache.amoro.utils.TableFileUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataOperations;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.MetadataTableUtils;
import org.apache.iceberg.ReachableFileUtil;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Util class for iceberg table. */
public class IcebergTableUtil {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergTableUtil.class);

  public static long getSnapshotId(Table table, boolean refresh) {
    Snapshot currentSnapshot = getSnapshot(table, refresh);
    if (currentSnapshot == null) {
      return Constants.INVALID_SNAPSHOT_ID;
    } else {
      return currentSnapshot.snapshotId();
    }
  }

  public static TableSnapshot getSnapshot(MixedTable mixedTable, TableRuntime tableRuntime) {
    if (mixedTable.isUnkeyedTable()) {
      return new BasicTableSnapshot(tableRuntime.getCurrentSnapshotId());
    } else {
      return new KeyedTableSnapshot(
          tableRuntime.getCurrentSnapshotId(), tableRuntime.getCurrentChangeSnapshotId());
    }
  }

  public static Snapshot getSnapshot(Table table, boolean refresh) {
    if (refresh) {
      table.refresh();
    }
    return table.currentSnapshot();
  }

  public static Optional<Snapshot> findFirstMatchSnapshot(
      Table table, Predicate<Snapshot> predicate) {
    List<Snapshot> snapshots = Lists.newArrayList(table.snapshots());
    Collections.reverse(snapshots);
    return Optional.ofNullable(Iterables.tryFind(snapshots, predicate).orNull());
  }

  public static Optional<Snapshot> findLatestOptimizingSnapshot(Table table) {
    return IcebergTableUtil.findFirstMatchSnapshot(
        table,
        snapshot ->
            snapshot.summary().containsValue(CommitMetaProducer.OPTIMIZE.name())
                && DataOperations.REPLACE.equals(snapshot.operation()));
  }

  public static Set<String> getAllContentFilePath(Table internalTable) {
    Set<String> validFilesPath = new HashSet<>();

    TableEntriesScan entriesScan =
        TableEntriesScan.builder(internalTable)
            .includeFileContent(
                FileContent.DATA, FileContent.POSITION_DELETES, FileContent.EQUALITY_DELETES)
            .allEntries()
            .build();
    try (CloseableIterable<IcebergFileEntry> entries = entriesScan.entries()) {
      for (IcebergFileEntry entry : entries) {
        validFilesPath.add(TableFileUtil.getUriPath(entry.getFile().path().toString()));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    return validFilesPath;
  }

  public static Set<String> getAllStatisticsFilePath(Table table) {
    return ReachableFileUtil.statisticsFilesLocations(table).stream()
        .map(TableFileUtil::getUriPath)
        .collect(Collectors.toSet());
  }

  public static Set<DeleteFile> getDanglingDeleteFiles(Table internalTable) {
    if (internalTable.currentSnapshot() == null) {
      return Collections.emptySet();
    }
    long snapshotId = internalTable.currentSnapshot().snapshotId();
    Set<String> deleteFilesPath = new HashSet<>();
    TableScan tableScan = internalTable.newScan().useSnapshot(snapshotId);
    try (CloseableIterable<FileScanTask> fileScanTasks = tableScan.planFiles()) {
      for (FileScanTask fileScanTask : fileScanTasks) {
        for (DeleteFile delete : fileScanTask.deletes()) {
          deleteFilesPath.add(delete.path().toString());
        }
      }
    } catch (IOException e) {
      LOG.error("table scan plan files error", e);
      return Collections.emptySet();
    }

    Set<DeleteFile> danglingDeleteFiles = new HashSet<>();
    TableEntriesScan entriesScan =
        TableEntriesScan.builder(internalTable)
            .useSnapshot(snapshotId)
            .includeFileContent(FileContent.EQUALITY_DELETES, FileContent.POSITION_DELETES)
            .build();
    try (CloseableIterable<IcebergFileEntry> entries = entriesScan.entries()) {
      for (IcebergFileEntry entry : entries) {
        ContentFile<?> file = entry.getFile();
        String path = file.path().toString();
        if (!deleteFilesPath.contains(path)) {
          danglingDeleteFiles.add((DeleteFile) file);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Error when fetch iceberg entries", e);
    }

    return danglingDeleteFiles;
  }

  /**
   * Fetch all manifest files of an Iceberg Table
   *
   * @param table An iceberg table, or maybe base store or change store of mixed-iceberg format.
   * @return Path set of all valid manifest files.
   */
  public static Set<String> getAllManifestFiles(Table table) {
    Preconditions.checkArgument(
        table instanceof HasTableOperations, "the table must support table operation.");
    TableOperations ops = ((HasTableOperations) table).operations();

    Table allManifest =
        MetadataTableUtils.createMetadataTableInstance(
            ops,
            table.name(),
            table.name() + "#" + MetadataTableType.ALL_MANIFESTS.name(),
            MetadataTableType.ALL_MANIFESTS);

    Set<String> allManifestFiles = Sets.newConcurrentHashSet();
    TableScan scan = allManifest.newScan().select("path");

    CloseableIterable<FileScanTask> tasks = scan.planFiles();
    CloseableIterable<CloseableIterable<StructLike>> transform =
        CloseableIterable.transform(tasks, task -> task.asDataTask().rows());

    try (CloseableIterable<StructLike> rows = CloseableIterable.concat(transform)) {
      rows.forEach(r -> allManifestFiles.add(r.get(0, String.class)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return allManifestFiles;
  }

  public static AbstractOptimizingEvaluator createOptimizingEvaluator(
      TableRuntime tableRuntime,
      MixedTable table,
      TableSnapshot snapshot,
      int maxPendingPartitions) {
    ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
    OptimizingConfig config = tableRuntime.getOptimizingConfig();
    long lastMinor = tableRuntime.getLastMinorOptimizingTime();
    long lastFull = tableRuntime.getLastFullOptimizingTime();
    if (TableFormat.ICEBERG.equals(table.format())) {
      return new IcebergOptimizerEvaluator(
          identifier, config, table, snapshot, maxPendingPartitions, lastMinor, lastFull);
    } else if (TableFormat.MIXED_ICEBERG.equals(table.format())) {
      return new MixedIcebergOptimizingEvaluator(
          identifier, config, table, snapshot, maxPendingPartitions, lastMinor, lastFull);
    } else if (TableFormat.MIXED_HIVE.equals(table.format())) {
      return new MixedHiveOptimizingEvaluator(
          identifier, config, table, snapshot, maxPendingPartitions, lastMinor, lastFull);
    }
    throw new IllegalStateException("Un-supported table-format:" + table.format().toString());
  }

  public static AbstractOptimizingEvaluator createOptimizingEvaluator(
      TableRuntime tableRuntime, MixedTable table, int maxPendingPartitions) {
    TableSnapshot snapshot = IcebergTableUtil.getSnapshot(table, tableRuntime);
    return createOptimizingEvaluator(tableRuntime, table, snapshot, maxPendingPartitions);
  }

  public static AbstractOptimizingPlanner createOptimizingPlanner(
      TableRuntime tableRuntime,
      MixedTable table,
      double availableCore,
      long maxInputSizePerThread) {
    Expression partitionFilter =
        tableRuntime.getPendingInput() == null
            ? Expressions.alwaysTrue()
            : tableRuntime.getPendingInput().getPartitions().entrySet().stream()
                .map(
                    entry ->
                        ExpressionUtil.convertPartitionDataToDataFilter(
                            table, entry.getKey(), entry.getValue()))
                .reduce(Expressions::or)
                .orElse(Expressions.alwaysTrue());
    long planTime = System.currentTimeMillis();
    long processId = Math.max(tableRuntime.getNewestProcessId() + 1, planTime);
    ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
    OptimizingConfig config = tableRuntime.getOptimizingConfig();
    long lastMinor = tableRuntime.getLastMinorOptimizingTime();
    long lastFull = tableRuntime.getLastFullOptimizingTime();
    TableSnapshot snapshot = IcebergTableUtil.getSnapshot(table, tableRuntime);
    if (TableFormat.ICEBERG.equals(table.format())) {
      return new IcebergOptimizingPlanner(
          identifier,
          config,
          table,
          snapshot,
          partitionFilter,
          processId,
          availableCore,
          maxInputSizePerThread,
          lastMinor,
          lastFull);
    } else if (TableFormat.MIXED_ICEBERG.equals(table.format())) {
      return new MixedIcebergOptimizingPlanner(
          identifier,
          config,
          table,
          snapshot,
          partitionFilter,
          processId,
          availableCore,
          maxInputSizePerThread,
          lastMinor,
          lastFull);
    } else if (TableFormat.MIXED_HIVE.equals(table.format())) {
      return new MixedHiveOptimizingPlanner(
          identifier,
          config,
          table,
          snapshot,
          partitionFilter,
          processId,
          availableCore,
          maxInputSizePerThread,
          lastMinor,
          lastFull);
    }
    throw new IllegalStateException("Unsupported table format:" + table.format().toString());
  }
}

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

package com.netease.arctic.server.table.executor;

import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.data.FileNameRules;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.scan.TableEntriesScan;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.HiveLocationUtil;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.PuffinUtil;
import com.netease.arctic.utils.TableFileUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.iceberg.relocated.com.google.common.primitives.Longs.min;

/**
 * Service for expiring tables periodically.
 */
public class SnapshotsExpiringExecutor extends BaseTableExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(SnapshotsExpiringExecutor.class);

  public static final String FLINK_MAX_COMMITTED_CHECKPOINT_ID = "flink.max-committed-checkpoint-id";

  private static final int DATA_FILE_LIST_SPLIT = 3000;

  // 1 hour
  private static final long INTERVAL = 60 * 60 * 1000L;

  public SnapshotsExpiringExecutor(TableManager tableRuntimes, int poolSize) {
    super(tableRuntimes, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().isExpireSnapshotEnabled();
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  public void execute(TableRuntime tableRuntime) {
    try {
      TableConfiguration tableConfiguration = tableRuntime.getTableConfiguration();
      if (!tableConfiguration.isExpireSnapshotEnabled()) {
        return;
      }

      ArcticTable arcticTable = loadTable(tableRuntime);
      expireArcticTable(arcticTable, tableRuntime);
    } catch (Throwable t) {
      LOG.error("unexpected expire error of table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }

  public static void expireArcticTable(ArcticTable arcticTable, TableRuntime tableRuntime) {
    long startTime = System.currentTimeMillis();
    TableConfiguration tableConfiguration = tableRuntime.getTableConfiguration();
    LOG.info("{} start expire", tableRuntime.getTableIdentifier());

    long changeDataTTL = tableConfiguration.getChangeDataTTLMinutes() * 60 * 1000;
    long baseSnapshotsKeepTime = tableConfiguration.getSnapshotTTLMinutes() * 60 * 1000;

    Set<String> hiveLocations = new HashSet<>();
    if (TableTypeUtil.isHive(arcticTable)) {
      hiveLocations = HiveLocationUtil.getHiveLocation(arcticTable);
    }

    if (arcticTable.isKeyedTable()) {
      KeyedTable keyedArcticTable = arcticTable.asKeyedTable();
      Set<String> finalHiveLocations = hiveLocations;
      keyedArcticTable.io().doAs(() -> {
        UnkeyedTable baseTable = keyedArcticTable.baseTable();
        UnkeyedTable changeTable = keyedArcticTable.changeTable();

        // getRuntime valid files in the change store which shouldn't physically delete when expire the snapshot
        // in the base store
        Set<String> baseExcludePaths = IcebergTableUtil.getAllContentFilePath(changeTable);
        baseExcludePaths.addAll(finalHiveLocations);
        long latestBaseFlinkCommitTime = fetchLatestFlinkCommittedSnapshotTime(baseTable);
        long optimizingSnapshotTime = fetchOptimizingSnapshotTime(baseTable, tableRuntime);
        long baseOlderThan = startTime - baseSnapshotsKeepTime;
        LOG.info("{} base table expire with latestFlinkCommitTime={}, optimizingSnapshotTime={}, olderThan={}",
            arcticTable.id(), latestBaseFlinkCommitTime, optimizingSnapshotTime, baseOlderThan);
        expireSnapshots(
            baseTable,
            min(latestBaseFlinkCommitTime, optimizingSnapshotTime, baseOlderThan),
            baseExcludePaths);
        long baseCleanedTime = System.currentTimeMillis();
        LOG.info("{} base expire cost {} ms", arcticTable.id(), baseCleanedTime - startTime);
        // delete ttl files
        long changeTTLPoint = startTime - changeDataTTL;
        List<IcebergFileEntry> expiredDataFileEntries = getExpiredDataFileEntries(
            changeTable, System.currentTimeMillis() - changeDataTTL);
        deleteChangeFile(keyedArcticTable, expiredDataFileEntries);

        // getRuntime valid files in the base store which shouldn't physically delete when expire the snapshot
        // in the change store
        Set<String> changeExclude = IcebergTableUtil.getAllContentFilePath(baseTable);
        changeExclude.addAll(finalHiveLocations);

        long latestChangeFlinkCommitTime = fetchLatestFlinkCommittedSnapshotTime(changeTable);
        long changeOlderThan = changeTTLPoint;
        LOG.info("{} change table expire with latestFlinkCommitTime={}, olderThan={}", arcticTable.id(),
            latestChangeFlinkCommitTime, changeOlderThan);
        expireSnapshots(
            changeTable,
            Math.min(latestChangeFlinkCommitTime, changeOlderThan),
            changeExclude);
        return null;
      });
      LOG.info("{} expire cost total {} ms", arcticTable.id(), System.currentTimeMillis() - startTime);
    } else {
      UnkeyedTable unKeyedArcticTable = arcticTable.asUnkeyedTable();
      long latestFlinkCommitTime = fetchLatestFlinkCommittedSnapshotTime(unKeyedArcticTable);
      long optimizingSnapshotTime = fetchOptimizingSnapshotTime(unKeyedArcticTable, tableRuntime);
      long olderThan = startTime - baseSnapshotsKeepTime;
      LOG.info("{} unKeyedTable expire with latestFlinkCommitTime={}, optimizingSnapshotTime={}, olderThan={}",
          arcticTable.id(), latestFlinkCommitTime, optimizingSnapshotTime, olderThan);
      expireSnapshots(
          unKeyedArcticTable,
          min(latestFlinkCommitTime, optimizingSnapshotTime, olderThan),
          hiveLocations);
      long baseCleanedTime = System.currentTimeMillis();
      LOG.info("{} unKeyedTable expire cost {} ms", arcticTable.id(), baseCleanedTime - startTime);
    }
  }

  /**
   * When committing a snapshot, Flink will write a checkpoint id into the snapshot summary.
   * The latest snapshot with checkpoint id should not be expired or the flink job can't recover from state.
   *
   * @param table -
   * @return commit time of snapshot with the latest flink checkpointId in summary
   */
  public static long fetchLatestFlinkCommittedSnapshotTime(UnkeyedTable table) {
    long latestCommitTime = Long.MAX_VALUE;
    for (Snapshot snapshot : table.snapshots()) {
      if (snapshot.summary().containsKey(FLINK_MAX_COMMITTED_CHECKPOINT_ID)) {
        latestCommitTime = snapshot.timestampMillis();
      }
    }
    return latestCommitTime;
  }

  /**
   * When optimizing tasks are not committed, the snapshot with which it planned should not be expired, since
   * it will use the snapshot to check conflict when committing.
   *
   * @param table - table
   * @return commit time of snapshot for optimizing
   */
  public static long fetchOptimizingSnapshotTime(UnkeyedTable table, TableRuntime tableRuntime) {
    if (tableRuntime.getOptimizingStatus().isProcessing()) {
      long targetSnapshotId = tableRuntime.getOptimizingProcess().getTargetSnapshotId();
      for (Snapshot snapshot : table.snapshots()) {
        if (snapshot.snapshotId() == targetSnapshotId) {
          return snapshot.timestampMillis();
        }
      }
    }
    return Long.MAX_VALUE;
  }

  public static void expireSnapshots(
      UnkeyedTable arcticInternalTable,
      long olderThan,
      Set<String> exclude) {
    LOG.debug("start expire snapshots older than {}, the exclude is {}", olderThan, exclude);
    final AtomicInteger toDeleteFiles = new AtomicInteger(0);
    final AtomicInteger deleteFiles = new AtomicInteger(0);
    Set<String> parentDirectory = new HashSet<>();
    arcticInternalTable.expireSnapshots()
        .retainLast(1)
        .expireOlderThan(olderThan)
        .deleteWith(file -> {
          try {
            String filePath = TableFileUtil.getUriPath(file);
            if (!exclude.contains(filePath) && !exclude.contains(new Path(filePath).getParent().toString())) {
              arcticInternalTable.io().deleteFile(file);
            }
            parentDirectory.add(new Path(file).getParent().toString());
            deleteFiles.incrementAndGet();
          } catch (Throwable t) {
            LOG.warn("failed to delete file " + file, t);
          } finally {
            toDeleteFiles.incrementAndGet();
          }
        })
        .cleanExpiredFiles(true)
        .commit();
    if (arcticInternalTable.io().supportFileSystemOperations()) {
      parentDirectory.forEach(parent -> TableFileUtil.deleteEmptyDirectory(arcticInternalTable.io(), parent, exclude));
    }
    LOG.info("to delete {} files, success delete {} files", toDeleteFiles.get(), deleteFiles.get());
  }


  public static List<IcebergFileEntry> getExpiredDataFileEntries(UnkeyedTable changeTable, long ttlPoint) {
    TableEntriesScan entriesScan = TableEntriesScan.builder(changeTable)
        .includeFileContent(FileContent.DATA)
        .build();
    List<IcebergFileEntry> changeTTLFileEntries = new ArrayList<>();

    try (CloseableIterable<IcebergFileEntry> entries = entriesScan.entries()) {
      entries.forEach(entry -> {
        Snapshot snapshot = changeTable.snapshot(entry.getSnapshotId());
        if (snapshot == null || snapshot.timestampMillis() < ttlPoint) {
          changeTTLFileEntries.add(entry);
        }
      });
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to close manifest entry scan of " + changeTable.name(), e);
    }
    return changeTTLFileEntries;
  }

  public static void deleteChangeFile(KeyedTable keyedTable, List<IcebergFileEntry> expiredDataFileEntries) {
    if (CollectionUtils.isEmpty(expiredDataFileEntries)) {
      return;
    }

    StructLikeMap<Long> partitionMaxTransactionId = PuffinUtil.reader(keyedTable).readOptimizedSequence();
    if (MapUtils.isEmpty(partitionMaxTransactionId)) {
      LOG.info("table {} not contains max transaction id", keyedTable.id());
      return;
    }

    Map<String, List<IcebergFileEntry>> partitionDataFileMap = expiredDataFileEntries.stream()
        .collect(Collectors.groupingBy(entry ->
            keyedTable.spec().partitionToPath(entry.getFile().partition()), Collectors.toList()));

    List<DataFile> changeDeleteFiles = new ArrayList<>();
    if (keyedTable.spec().isUnpartitioned()) {
      List<IcebergFileEntry> partitionDataFiles =
          partitionDataFileMap.get(keyedTable.spec().partitionToPath(
              expiredDataFileEntries.get(0).getFile().partition()));

      Long optimizedSequence = partitionMaxTransactionId.get(TablePropertyUtil.EMPTY_STRUCT);
      if (optimizedSequence != null && CollectionUtils.isNotEmpty(partitionDataFiles)) {
        changeDeleteFiles.addAll(partitionDataFiles.stream()
            .filter(entry -> FileNameRules.parseChangeTransactionId(
                entry.getFile().path().toString(), entry.getSequenceNumber()) <= optimizedSequence)
            .map(entry -> (DataFile) entry.getFile())
            .collect(Collectors.toList()));
      }
    } else {
      partitionMaxTransactionId.forEach((key, value) -> {
        List<IcebergFileEntry> partitionDataFiles =
            partitionDataFileMap.get(keyedTable.spec().partitionToPath(key));

        if (CollectionUtils.isNotEmpty(partitionDataFiles)) {
          changeDeleteFiles.addAll(partitionDataFiles.stream()
              .filter(entry -> FileNameRules.parseChangeTransactionId(
                  entry.getFile().path().toString(), entry.getSequenceNumber()) <= value)
              .map(entry -> (DataFile) entry.getFile())
              .collect(Collectors.toList()));
        }
      });
    }
    tryClearChangeFiles(keyedTable, changeDeleteFiles);
  }

  public static void tryClearChangeFiles(KeyedTable keyedTable, List<DataFile> changeFiles) {
    if (CollectionUtils.isEmpty(changeFiles)) {
      return;
    }
    try {
      if (keyedTable.primaryKeySpec().primaryKeyExisted()) {
        for (int startIndex = 0; startIndex < changeFiles.size(); startIndex += DATA_FILE_LIST_SPLIT) {
          int end = Math.min(startIndex + DATA_FILE_LIST_SPLIT, changeFiles.size());
          List<DataFile> tableFiles = changeFiles.subList(startIndex, end);
          LOG.info("{} delete {} change files", keyedTable.id(), tableFiles.size());
          if (!tableFiles.isEmpty()) {
            DeleteFiles changeDelete = keyedTable.changeTable().newDelete();
            changeFiles.forEach(changeDelete::deleteFile);
            changeDelete.commit();
          }
          LOG.info("{} change committed, delete {} files, complete {}/{}", keyedTable.id(),
              tableFiles.size(), end, changeFiles.size());
        }
      }
    } catch (Throwable t) {
      LOG.error(keyedTable.id() + " failed to delete change files, ignore", t);
    }
  }
}

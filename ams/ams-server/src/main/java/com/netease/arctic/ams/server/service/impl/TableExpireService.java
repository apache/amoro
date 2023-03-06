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

package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.service.ITableExpireService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.utils.ChangeFilesUtil;
import com.netease.arctic.ams.server.utils.ContentFileUtil;
import com.netease.arctic.ams.server.utils.HiveLocationUtils;
import com.netease.arctic.ams.server.utils.ScheduledTasks;
import com.netease.arctic.ams.server.utils.ThreadPool;
import com.netease.arctic.ams.server.utils.UnKeyedTableUtil;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.FileUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TableExpireService implements ITableExpireService {
  private static final Logger LOG = LoggerFactory.getLogger(TableExpireService.class);
  private static final long EXPIRE_INTERVAL = 3600_000; // 1 hour
  /**
   * the same with org.apache.iceberg.flink.sink.IcebergFilesCommitter#MAX_COMMITTED_CHECKPOINT_ID
   */
  public static final String FLINK_MAX_COMMITTED_CHECKPOINT_ID = "flink.max-committed-checkpoint-id";

  private ScheduledTasks<TableIdentifier, TableExpireTask> cleanTasks;

  @Override
  public synchronized void checkTableExpireTasks() {
    LOG.info("Schedule Expired Cleaner");
    if (cleanTasks == null) {
      cleanTasks = new ScheduledTasks<>(ThreadPool.Type.EXPIRE);
    }
    List<TableMetadata> tables = ServiceContainer.getMetaService().listTables();
    Set<TableIdentifier> ids =
        tables.stream().map(TableMetadata::getTableIdentifier).collect(Collectors.toSet());
    cleanTasks.checkRunningTask(ids,
        () -> 0L,
        () -> EXPIRE_INTERVAL,
        TableExpireTask::new,
        false);
    LOG.info("Schedule Expired Cleaner finished with {} valid ids", ids.size());
  }

  public static class TableExpireTask implements ScheduledTasks.Task {
    private final TableIdentifier tableIdentifier;

    TableExpireTask(TableIdentifier tableIdentifier) {
      this.tableIdentifier = tableIdentifier;
    }

    @Override
    public void run() {
      try {
        ArcticCatalog catalog =
            CatalogLoader.load(ServiceContainer.getTableMetastoreHandler(), tableIdentifier.getCatalog());
        ArcticTable arcticTable = catalog.loadTable(tableIdentifier);
        boolean needClean = Boolean.parseBoolean(arcticTable.properties()
            .getOrDefault(TableProperties.ENABLE_TABLE_EXPIRE,
                TableProperties.ENABLE_TABLE_EXPIRE_DEFAULT));
        if (!needClean) {
          return;
        }
        expireArcticTable(arcticTable);
      } catch (Throwable t) {
        LOG.error("unexpected expire error of table {} ", tableIdentifier, t);
      }
    }
  }

  public static void expireArcticTable(ArcticTable arcticTable) {
    TableIdentifier tableIdentifier = arcticTable.id();
    long startTime = System.currentTimeMillis();
    LOG.info("{} start expire", tableIdentifier);

    long changeDataTTL = Long.parseLong(arcticTable.properties()
        .getOrDefault(TableProperties.CHANGE_DATA_TTL,
            TableProperties.CHANGE_DATA_TTL_DEFAULT)) * 60 * 1000;
    long baseSnapshotsKeepTime = Long.parseLong(arcticTable.properties()
        .getOrDefault(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES,
            TableProperties.BASE_SNAPSHOT_KEEP_MINUTES_DEFAULT)) * 60 * 1000;
    long changeSnapshotsKeepTime = Long.parseLong(arcticTable.properties()
        .getOrDefault(TableProperties.CHANGE_SNAPSHOT_KEEP_MINUTES,
            TableProperties.CHANGE_SNAPSHOT_KEEP_MINUTES_DEFAULT)) * 60 * 1000;

    Set<String> hiveLocations = new HashSet<>();
    if (TableTypeUtil.isHive(arcticTable)) {
      hiveLocations = HiveLocationUtils.getHiveLocation(arcticTable);
    }

    if (arcticTable.isKeyedTable()) {
      KeyedTable keyedArcticTable = arcticTable.asKeyedTable();
      Set<String> finalHiveLocations = hiveLocations;
      keyedArcticTable.io().doAs(() -> {
        UnkeyedTable baseTable = keyedArcticTable.baseTable();
        UnkeyedTable changeTable = keyedArcticTable.changeTable();

        // get valid files in the change store which shouldn't physically delete when expire the snapshot
        // in the base store
        Set<String> baseExcludePaths = UnKeyedTableUtil.getAllContentFilePath(changeTable);
        baseExcludePaths.addAll(finalHiveLocations);
        long latestBaseFlinkCommitTime = fetchLatestFlinkCommittedSnapshotTime(baseTable);
        expireSnapshots(baseTable, Math.min(latestBaseFlinkCommitTime, startTime - baseSnapshotsKeepTime),
            baseExcludePaths);
        long baseCleanedTime = System.currentTimeMillis();
        LOG.info("{} base expire cost {} ms", arcticTable.id(), baseCleanedTime - startTime);

        // delete ttl files
        List<DataFileInfo> changeDataFiles = ServiceContainer.getFileInfoCacheService()
            .getChangeTableTTLDataFiles(keyedArcticTable.id().buildTableIdentifier(),
                System.currentTimeMillis() - changeDataTTL);
        deleteChangeFile(keyedArcticTable, changeDataFiles);

        // get valid files in the base store which shouldn't physically delete when expire the snapshot
        // in the change store
        Set<String> changeExclude = UnKeyedTableUtil.getAllContentFilePath(baseTable);
        changeExclude.addAll(finalHiveLocations);
        long latestChangeFlinkCommitTime = fetchLatestFlinkCommittedSnapshotTime(changeTable);
        expireSnapshots(changeTable, Math.min(latestChangeFlinkCommitTime, startTime - changeSnapshotsKeepTime),
            changeExclude);
        return null;
      });
      LOG.info("{} expire cost total {} ms", arcticTable.id(),
          System.currentTimeMillis() - startTime);
    } else {
      UnkeyedTable unKeyedArcticTable = arcticTable.asUnkeyedTable();
      long latestFlinkCommitTime = fetchLatestFlinkCommittedSnapshotTime(unKeyedArcticTable);
      expireSnapshots(unKeyedArcticTable, Math.min(latestFlinkCommitTime, startTime - baseSnapshotsKeepTime),
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

  public static void deleteChangeFile(KeyedTable keyedTable, List<DataFileInfo> changeDataFiles) {
    if (CollectionUtils.isEmpty(changeDataFiles)) {
      return;
    }

    StructLikeMap<Long> baseMaxTransactionId = TablePropertyUtil.getPartitionMaxTransactionId(keyedTable);
    if (MapUtils.isEmpty(baseMaxTransactionId)) {
      LOG.info("table {} not contains max transaction id", keyedTable.id());
      return;
    }

    Map<String, List<DataFileInfo>> partitionDataFileMap = new HashMap<>();
    for (DataFileInfo changeDataFile : changeDataFiles) {
      List<DataFileInfo> dataFileInfos =
          partitionDataFileMap.computeIfAbsent(changeDataFile.getPartition(), e -> new ArrayList<>());
      dataFileInfos.add(changeDataFile);
    }

    List<DataFileInfo> deleteFiles = new ArrayList<>();
    if (keyedTable.baseTable().spec().isUnpartitioned()) {
      List<DataFileInfo> partitionDataFiles =
          partitionDataFileMap.get(changeDataFiles.get(0).getPartition());

      Long maxTransactionId = baseMaxTransactionId.get(TablePropertyUtil.EMPTY_STRUCT);
      if (CollectionUtils.isNotEmpty(partitionDataFiles)) {
        deleteFiles.addAll(partitionDataFiles.stream()
            .filter(dataFileInfo ->
                FileUtil.parseFileTidFromFileName(dataFileInfo.getPath()) <= maxTransactionId)
            .collect(Collectors.toList()));
      }
    } else {
      baseMaxTransactionId.forEach((key, value) -> {
        List<DataFileInfo> partitionDataFiles =
            partitionDataFileMap.get(keyedTable.baseTable().spec().partitionToPath(key));

        if (CollectionUtils.isNotEmpty(partitionDataFiles)) {
          deleteFiles.addAll(partitionDataFiles.stream()
              .filter(dataFileInfo ->
                  FileUtil.parseFileTidFromFileName(dataFileInfo.getPath()) <= value)
              .collect(Collectors.toList()));
        }
      });
    }

    String fileFormat = keyedTable.properties().getOrDefault(TableProperties.DEFAULT_FILE_FORMAT,
        TableProperties.DEFAULT_FILE_FORMAT_DEFAULT);
    List<PrimaryKeyedFile> changeDeleteFiles = deleteFiles.stream().map(dataFileInfo -> {
      PartitionSpec partitionSpec = keyedTable.changeTable().specs().get((int) dataFileInfo.getSpecId());

      if (partitionSpec == null) {
        LOG.error("{} can not find partitionSpec id: {}", dataFileInfo.getPath(), dataFileInfo.specId);
        return null;
      }
      ContentFile<?> contentFile = ContentFileUtil.buildContentFile(dataFileInfo, partitionSpec, fileFormat);
      return new DefaultKeyedFile((DataFile) contentFile);
    }).filter(Objects::nonNull).collect(Collectors.toList());

    ChangeFilesUtil.tryClearChangeFiles(keyedTable, changeDeleteFiles);
  }

  public static void expireSnapshots(UnkeyedTable arcticInternalTable,
                                     long olderThan,
                                     Set<String> exclude) {
    LOG.debug("start expire snapshots older than {}, the exclude is {}", olderThan, exclude);
    final AtomicInteger toDeleteFiles = new AtomicInteger(0);
    final AtomicInteger deleteFiles = new AtomicInteger(0);
    Set<String> parentDirectory = new HashSet<>();
    arcticInternalTable.expireSnapshots()
        .retainLast(1).expireOlderThan(olderThan)
        .deleteWith(file -> {
          try {
            String filePath = FileUtil.getUriPath(file);
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
        }).cleanExpiredFiles(true).commit();
    parentDirectory.forEach(parent -> FileUtil.deleteEmptyDirectory(arcticInternalTable.io(), parent, exclude));
    LOG.info("to delete {} files, success delete {} files", toDeleteFiles.get(), deleteFiles.get());
  }
}

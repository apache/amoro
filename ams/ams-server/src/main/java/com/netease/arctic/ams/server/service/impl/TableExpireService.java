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

import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.service.ITableExpireService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.utils.ChangeFilesUtil;
import com.netease.arctic.ams.server.utils.ContentFileUtil;
import com.netease.arctic.ams.server.utils.ScheduledTasks;
import com.netease.arctic.ams.server.utils.ThreadPool;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.FileUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TableExpireService implements ITableExpireService {
  private static final Logger LOG = LoggerFactory.getLogger(TableExpireService.class);
  private static final long EXPIRE_INTERVAL = 3600_000; // 1 hour

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
        tableId -> EXPIRE_INTERVAL,
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
      long startTime = System.currentTimeMillis();
      final String traceId = UUID.randomUUID().toString();
      try {
        LOG.info("[{}] {} start expire", traceId, tableIdentifier);
        ArcticCatalog catalog =
            CatalogLoader.load(ServiceContainer.getTableMetastoreHandler(), tableIdentifier.getCatalog());
        ArcticTable arcticTable = catalog.loadTable(tableIdentifier);
        boolean needClean = Boolean.parseBoolean(arcticTable.properties()
            .getOrDefault(TableProperties.ENABLE_TABLE_EXPIRE,
                TableProperties.ENABLE_TABLE_EXPIRE_DEFAULT));
        if (!needClean) {
          return;
        }
        long changeDataTTL = Long.parseLong(arcticTable.properties()
            .getOrDefault(TableProperties.CHANGE_DATA_TTL,
                TableProperties.CHANGE_DATA_TTL_DEFAULT)) * 60 * 1000;
        long baseSnapshotsKeepTime = Long.parseLong(arcticTable.properties()
            .getOrDefault(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES,
                TableProperties.BASE_SNAPSHOT_KEEP_MINUTES_DEFAULT)) * 60 * 1000;
        long changeSnapshotsKeepTime = Long.parseLong(arcticTable.properties()
            .getOrDefault(TableProperties.CHANGE_SNAPSHOT_KEEP_MINUTES,
                TableProperties.CHANGE_SNAPSHOT_KEEP_MINUTES_DEFAULT)) * 60 * 1000;

        if (arcticTable.isKeyedTable()) {
          KeyedTable keyedArcticTable = arcticTable.asKeyedTable();
          keyedArcticTable.io().doAs(() -> {
            BaseTable baseTable = keyedArcticTable.baseTable();
            if (baseTable == null) {
              LOG.warn("[{}] Base table is null: {} ", traceId, tableIdentifier);
              return null;
            }
            List<DataFileInfo> changeFilesInfo = ServiceContainer.getFileInfoCacheService()
                .getOptimizeDatafiles(tableIdentifier.buildTableIdentifier(), Constants.INNER_TABLE_CHANGE);
            Set<String> baseExclude = changeFilesInfo.stream().map(DataFileInfo::getPath).collect(Collectors.toSet());
            expireSnapshots(baseTable, startTime - baseSnapshotsKeepTime, baseExclude);
            long baseCleanedTime = System.currentTimeMillis();
            LOG.info("[{}] {} base expire cost {} ms", traceId, arcticTable.id(), baseCleanedTime - startTime);

            ChangeTable changeTable = keyedArcticTable.changeTable();
            if (changeTable == null) {
              LOG.warn("[{}] Change table is null: {}", traceId, tableIdentifier);
              return null;
            }
            // delete ttl files
            List<DataFileInfo> changeDataFiles = ServiceContainer.getFileInfoCacheService()
                .getChangeTableTTLDataFiles(keyedArcticTable.id().buildTableIdentifier(),
                    System.currentTimeMillis() - changeDataTTL);
            deleteChangeFile(keyedArcticTable, changeDataFiles);
            List<DataFileInfo> baseFilesInfo = ServiceContainer.getFileInfoCacheService()
                .getOptimizeDatafiles(tableIdentifier.buildTableIdentifier(), Constants.INNER_TABLE_BASE);
            Set<String> changeExclude = baseFilesInfo.stream().map(DataFileInfo::getPath).collect(Collectors.toSet());
            expireSnapshots(changeTable, startTime - changeSnapshotsKeepTime, changeExclude);
            return null;
          });
          LOG.info("[{}] {} expire cost total {} ms", traceId, arcticTable.id(),
              System.currentTimeMillis() - startTime);
        } else {
          UnkeyedTable unKeyedArcticTable = arcticTable.asUnkeyedTable();
          expireSnapshots(unKeyedArcticTable, startTime - baseSnapshotsKeepTime, new HashSet<>());
          long baseCleanedTime = System.currentTimeMillis();
          LOG.info("[{}] {} unKeyedTable expire cost {} ms", traceId, arcticTable.id(), baseCleanedTime - startTime);
        }
      } catch (Throwable t) {
        LOG.error("[" + traceId + "] unexpected expire error of table " + tableIdentifier, t);
      }
    }
  }

  public static void deleteChangeFile(KeyedTable keyedTable, List<DataFileInfo> changeDataFiles) {
    StructLikeMap<Long> baseMaxTransactionId = keyedTable.baseTable().partitionMaxTransactionId();
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
          partitionDataFileMap.get(null);

      Long maxTransactionId = baseMaxTransactionId.get(null);
      if (CollectionUtils.isNotEmpty(partitionDataFiles)) {
        deleteFiles.addAll(partitionDataFiles.stream()
            .filter(dataFileInfo ->
                DefaultKeyedFile.parseMetaFromFileName(dataFileInfo.getPath()).transactionId() <= maxTransactionId)
            .collect(Collectors.toList()));
      }
    } else {
      baseMaxTransactionId.forEach((key, value) -> {
        List<DataFileInfo> partitionDataFiles =
            partitionDataFileMap.get(keyedTable.baseTable().spec().partitionToPath(key));

        if (CollectionUtils.isNotEmpty(partitionDataFiles)) {
          deleteFiles.addAll(partitionDataFiles.stream()
              .filter(dataFileInfo ->
                  DefaultKeyedFile.parseMetaFromFileName(dataFileInfo.getPath()).transactionId() <= value)
              .collect(Collectors.toList()));
        }
      });
    }


    List<PrimaryKeyedFile> changeDeleteFiles = deleteFiles.stream().map(dataFileInfo -> {
      PartitionSpec partitionSpec = keyedTable.changeTable().specs().get((int) dataFileInfo.getSpecId());

      if (partitionSpec == null) {
        LOG.error("{} can not find partitionSpec id: {}", dataFileInfo.getPath(), dataFileInfo.specId);
        return null;
      }
      ContentFile<?> contentFile = ContentFileUtil.buildContentFile(dataFileInfo, partitionSpec);
      return new DefaultKeyedFile((DataFile) contentFile);
    }).filter(Objects::nonNull).collect(Collectors.toList());

    ChangeFilesUtil.tryClearChangeFiles(keyedTable, changeDeleteFiles);
  }

  public static void expireSnapshots(UnkeyedTable arcticInternalTable,
                                     long olderThan,
                                     Set<String> exclude) {
    final AtomicInteger toDeleteFiles = new AtomicInteger(0);
    final AtomicInteger deleteFiles = new AtomicInteger(0);
    Set<String> parentDirectory = new HashSet<>();
    arcticInternalTable.expireSnapshots()
        .retainLast(1).expireOlderThan(olderThan)
        .deleteWith(file -> {
          try {
            if (!exclude.contains(file) && !exclude.contains(new Path(file).getParent().toString())) {
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

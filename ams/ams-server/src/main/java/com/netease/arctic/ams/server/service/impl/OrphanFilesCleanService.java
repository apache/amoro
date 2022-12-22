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
import com.netease.arctic.ams.server.service.IOrphanFilesCleanService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.utils.CatalogUtil;
import com.netease.arctic.ams.server.utils.HiveLocationUtils;
import com.netease.arctic.ams.server.utils.ScheduledTasks;
import com.netease.arctic.ams.server.utils.ThreadPool;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.ReachableFileUtil;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrphanFilesCleanService implements IOrphanFilesCleanService {
  private static final Logger LOG = LoggerFactory.getLogger(OrphanFilesCleanService.class);

  public static final String METADATA_FOLDER_NAME = "metadata";
  public static final String DATA_FOLDER_NAME = "data";

  private static final long CHECK_INTERVAL = 7 * 24 * 60 * 60 * 1000;  // 7 days

  private ScheduledTasks<TableIdentifier, TableOrphanFileClean> cleanTasks;

  @Override
  public synchronized void checkOrphanFilesCleanTasks() {
    LOG.info("Schedule Orphan Cleaner");
    if (cleanTasks == null) {
      cleanTasks = new ScheduledTasks<>(ThreadPool.Type.ORPHAN);
    }

    Set<TableIdentifier> tableIds = CatalogUtil.loadTablesFromCatalog();
    cleanTasks.checkRunningTask(tableIds,
        t -> CHECK_INTERVAL,
        TableOrphanFileClean::new,
        true);
    LOG.info("Schedule Orphan Cleaner finished with {} tasks", tableIds.size());
  }

  public static class TableOrphanFileClean implements ScheduledTasks.Task {
    private final TableIdentifier tableIdentifier;

    public TableOrphanFileClean(TableIdentifier tableIdentifier) {
      this.tableIdentifier = tableIdentifier;
    }

    @Override
    public void run() {
      try {
        LOG.info("{} clean orphan files", tableIdentifier);
        ArcticCatalog catalog =
            CatalogLoader.load(ServiceContainer.getTableMetastoreHandler(), tableIdentifier.getCatalog());
        ArcticTable arcticTable = catalog.loadTable(tableIdentifier);

        boolean needOrphanClean = CompatiblePropertyUtil.propertyAsBoolean(arcticTable.properties(),
            TableProperties.ENABLE_ORPHAN_CLEAN,
            TableProperties.ENABLE_ORPHAN_CLEAN_DEFAULT);

        if (!needOrphanClean) {
          return;
        }

        long keepTime = Long.parseLong(arcticTable.properties()
            .getOrDefault(TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME,
                TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT)) * 60 * 1000;

        LOG.info("{} clean orphan files, keepTime={}", tableIdentifier, keepTime);
        // clear data files
        clean(arcticTable, System.currentTimeMillis() - keepTime, true, "all", false);

        arcticTable = catalog.loadTable(tableIdentifier);
        // clear metadata files
        clean(arcticTable, System.currentTimeMillis() - keepTime, true, "all", true);
      } catch (Throwable t) {
        LOG.error("{} orphan file clean unexpected error", tableIdentifier, t);
      }
    }
  }

  public static void clean(ArcticTable arcticTable, long lastTime, boolean execute,
                           String mode, boolean metadata) {
    if (arcticTable.isKeyedTable()) {
      KeyedTable keyedArcticTable = arcticTable.asKeyedTable();
      if (Constants.INNER_TABLE_BASE.equals(mode)) {
        clearInternalTable(keyedArcticTable, keyedArcticTable.baseTable(), lastTime, execute, metadata);
      } else if (Constants.INNER_TABLE_CHANGE.equals(mode)) {
        if (keyedArcticTable.primaryKeySpec().primaryKeyExisted()) {
          clearInternalTable(keyedArcticTable, keyedArcticTable.changeTable(), lastTime, execute, metadata);
        } else {
          throw new IllegalStateException("no pk table, only support mode=all/base");
        }
      } else if ("all".equals(mode)) {
        clearInternalTable(keyedArcticTable, keyedArcticTable.baseTable(), lastTime, execute, metadata);
        clearInternalTable(keyedArcticTable, keyedArcticTable.changeTable(), lastTime, execute, metadata);
      } else {
        throw new IllegalStateException("only support mode=all/base/change");
      }
    } else {
      clearInternalTable(arcticTable, arcticTable.asUnkeyedTable(), lastTime, execute, metadata);
    }
  }

  private static void clearInternalTable(ArcticTable table, UnkeyedTable internalTable, long lastTime,
                                         boolean execute, boolean metadata) {
    if (metadata) {
      clearInternalTableMetadata(table, internalTable, lastTime, execute);
    } else {
      clearInternalTableDataFiles(table, internalTable, lastTime, execute);
    }
  }

  private static void clearInternalTableDataFiles(ArcticTable table, UnkeyedTable internalTable, long lastTime,
                                                  boolean execute) {
    Set<String> validFiles = getValidDataFiles(table.id(), table.io(), internalTable);
    LOG.info("{} table get {} valid files", table.id(), validFiles.size());
    int deleteFilesCnt = 0;
    Set<String> exclude = new HashSet<>();
    if (internalTable instanceof BaseTable) {
      List<DataFileInfo> dataFilesInfo = ServiceContainer.getFileInfoCacheService()
          .getOptimizeDatafiles(table.id().buildTableIdentifier(), Constants.INNER_TABLE_CHANGE);
      exclude = dataFilesInfo.stream().map(DataFileInfo::getPath).collect(Collectors.toSet());
    } else if (internalTable instanceof ChangeTable) {
      List<DataFileInfo> dataFilesInfo = ServiceContainer.getFileInfoCacheService()
          .getOptimizeDatafiles(table.id().buildTableIdentifier(), Constants.INNER_TABLE_BASE);
      exclude = dataFilesInfo.stream().map(DataFileInfo::getPath).collect(Collectors.toSet());
    }

    // add hive location to exclude
    exclude.addAll(HiveLocationUtils.getHiveLocation(table));

    String dataLocation = internalTable.location() + File.separator + DATA_FOLDER_NAME;
    if (table.io().exists(dataLocation)) {
      for (FileStatus fileStatus : table.io().list(dataLocation)) {
        deleteFilesCnt += deleteInvalidDataFiles(table.io(),
            fileStatus,
            validFiles,
            lastTime,
            exclude,
            execute);
      }
    }
    LOG.info("{} total delete[execute={}] {} files", table.id(), execute, deleteFilesCnt);
  }

  private static void clearInternalTableMetadata(ArcticTable table, UnkeyedTable internalTable, long lastTime,
                                                 boolean execute) {
    Set<String> validFiles = getValidMetadataFiles(table.id(), table.io(), internalTable);
    LOG.info("{} table get {} valid files", table.id(), validFiles.size());
    int deleteFilesCnt = 0;
    String metadataLocation = internalTable.location() + File.separator + METADATA_FOLDER_NAME;
    LOG.info("start orphan files clean in {}", metadataLocation);
    for (FileStatus fileStatus : table.io().list(metadataLocation)) {
      deleteFilesCnt += deleteInvalidMetadata(table.io(),
          fileStatus,
          validFiles,
          lastTime,
          execute);
    }
    LOG.info("{} total delete[execute={}] {} manifestList/manifest/metadata files", table.id(), execute,
        deleteFilesCnt);
  }

  private static String formatTime(long timestamp) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString();
  }

  private static int deleteInvalidDataFiles(ArcticFileIO io,
                                            FileStatus fileStatus,
                                            Set<String> validFiles,
                                            Long lastTime,
                                            Set<String> exclude,
                                            boolean execute) {
    String location = TableFileUtils.getUriPath(fileStatus.getPath().toString());
    if (io.isDirectory(location)) {
      if (!io.isEmptyDirectory(location)) {
        LOG.info("start orphan files clean in {}", location);
        int deleteFileCnt = 0;
        for (FileStatus file : io.list(location)) {
          deleteFileCnt += deleteInvalidDataFiles(io, file, validFiles, lastTime, exclude, execute);
        }
        LOG.info("delete[{}] {} files in {}", execute, deleteFileCnt, location);

        if (location.endsWith(METADATA_FOLDER_NAME) || location.endsWith(DATA_FOLDER_NAME)) {
          return 0;
        }
        TableFileUtils.deleteEmptyDirectory(io, location, exclude);
        return deleteFileCnt;
      } else if (io.isEmptyDirectory(location) &&
          fileStatus.getModificationTime() < lastTime) {
        if (location.endsWith(METADATA_FOLDER_NAME) || location.endsWith(DATA_FOLDER_NAME)) {
          return 0;
        }

        if (execute) {
          TableFileUtils.deleteEmptyDirectory(io, location, exclude);
        }
        LOG.info("delete[execute={}] empty dir : {}", location,
            formatTime(fileStatus.getModificationTime()));
        return 0;
      } else {
        return 0;
      }
    } else {
      if (!validFiles.contains(location) &&
          fileStatus.getModificationTime() < lastTime) {
        if (execute &&
            !exclude.contains(location) &&
            !exclude.contains(new Path(location).getParent().toString())) {
          io.deleteFile(location);
        }
        return 1;
      } else {
        return 0;
      }
    }
  }

  private static int deleteInvalidMetadata(ArcticFileIO io,
                                           FileStatus fileStatus,
                                           Set<String> validFiles,
                                           Long lastTime, boolean execute) {
    String location = TableFileUtils.getUriPath(fileStatus.getPath().toString());
    if (io.isDirectory(location)) {
      LOG.warn("unexpected dir in metadata/, {}", location);
      return 0;
    } else {
      if (!validFiles.contains(location) && fileStatus.getModificationTime() < lastTime) {
        if (execute) {
          io.deleteFile(location);
        }
        return 1;
      } else {
        return 0;
      }
    }
  }

  private static Set<String> getValidMetadataFiles(TableIdentifier tableIdentifier, ArcticFileIO io,
                                                   UnkeyedTable internalTable) {
    Set<String> validFiles = new HashSet<>();
    Iterable<Snapshot> snapshots = internalTable.snapshots();
    int size = Iterables.size(snapshots);
    LOG.info("{} get {} snapshots to scan", tableIdentifier, size);
    int cnt = 0;
    for (Snapshot snapshot : snapshots) {
      cnt++;
      int before = validFiles.size();
      String manifestListLocation = snapshot.manifestListLocation();
      
      validFiles.add(TableFileUtils.getUriPath(manifestListLocation));

      io.doAs(() -> {
        // valid data files
        List<ManifestFile> manifestFiles = snapshot.allManifests();
        for (ManifestFile manifestFile : manifestFiles) {
          validFiles.add(TableFileUtils.getUriPath(manifestFile.path()));
        }
        return null;
      });
      LOG.info("{} scan snapshot {}: {} and get {} files, complete {}/{}", tableIdentifier, snapshot.snapshotId(),
          formatTime(snapshot.timestampMillis()), validFiles.size() - before, cnt, size);
    }
    ReachableFileUtil.metadataFileLocations(internalTable, false)
        .forEach(f -> validFiles.add(TableFileUtils.getUriPath(f)));
    validFiles.add(TableFileUtils.getUriPath(ReachableFileUtil.versionHintLocation(internalTable)));

    return validFiles;
  }

  private static Set<String> getValidDataFiles(TableIdentifier tableIdentifier, ArcticFileIO io,
                                               UnkeyedTable internalTable) {
    Set<String> validFiles = new HashSet<>();
    Iterable<Snapshot> snapshots = internalTable.snapshots();
    int size = Iterables.size(snapshots);
    LOG.info("{} get {} snapshots to scan", tableIdentifier, size);
    int cnt = 0;
    for (Snapshot snapshot : snapshots) {
      cnt++;
      int before = validFiles.size();
      io.doAs(() -> {
        // valid data files

        try (CloseableIterable<FileScanTask> fileScanTasks =
                 internalTable.newScan().useSnapshot(snapshot.snapshotId()).planFiles()) {
          for (FileScanTask scanTask : fileScanTasks) {
            if (scanTask.file() != null) {
              validFiles.add(TableFileUtils.getUriPath(scanTask.file().path().toString()));
              scanTask.deletes().forEach(file -> validFiles.add(TableFileUtils.getUriPath(file.path().toString())));
            }
          }
          return null;
        }
      });
      LOG.info("{} scan snapshot {}: {} and get {} files, complete {}/{}", tableIdentifier, snapshot.snapshotId(),
          formatTime(snapshot.timestampMillis()), validFiles.size() - before, cnt, size);
    }

    return validFiles;
  }

}

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

import com.google.common.base.Strings;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.PathInfo;
import com.netease.arctic.io.SupportsFileSystemOperations;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.HiveLocationUtil;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.TableFileUtil;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.ReachableFileUtil;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.io.FileInfo;
import org.apache.iceberg.io.SupportsPrefixOperations;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class OrphanFilesCleaningExecutor extends BaseTableExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(OrphanFilesCleaningExecutor.class);
  // same as org.apache.iceberg.flink.sink.IcebergFilesCommitter#FLINK_JOB_ID
  public static final String FLINK_JOB_ID = "flink.job-id";
  public static final String METADATA_FOLDER_NAME = "metadata";
  public static final String DATA_FOLDER_NAME = "data";
  // 1 days
  private static final long INTERVAL = 24 * 60 * 60 * 1000L;

  public OrphanFilesCleaningExecutor(TableManager tableRuntimes, int poolSize) {
    super(tableRuntimes, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().isCleanOrphanEnabled();
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  public void execute(TableRuntime tableRuntime) {
    try {
      LOG.info("{} start cleaning orphan files", tableRuntime.getTableIdentifier());
      TableConfiguration tableConfiguration = tableRuntime.getTableConfiguration();

      if (!tableConfiguration.isCleanOrphanEnabled()) {
        return;
      }

      long keepTime = tableConfiguration.getOrphanExistingMinutes() * 60 * 1000;

      // clear data files
      ArcticTable arcticTable = loadTable(tableRuntime);
      cleanContentFiles(arcticTable, System.currentTimeMillis() - keepTime);

      // it may cost a long time to clean content files, so refresh the table to the current snapshot before cleaning 
      // the metadata files
      arcticTable.refresh();
      // clear metadata files
      cleanMetadata(arcticTable, System.currentTimeMillis() - keepTime);

      if (!tableConfiguration.isDeleteDanglingDeleteFilesEnabled()) {
        return;
      }
      // refresh to the current snapshot before clean dangling delete files
      arcticTable.refresh();
      // clear dangling delete files
      cleanDanglingDeleteFiles(arcticTable);
    } catch (Throwable t) {
      LOG.error("{} failed to clean orphan file", tableRuntime.getTableIdentifier(), t);
    }
  }

  public static void cleanContentFiles(ArcticTable arcticTable, long lastTime) {
    // For clean data files, should get valid files in the base store and the change store, so acquire in advance
    // to prevent repeated acquisition
    Set<String> validFiles = getValidContentFiles(arcticTable);
    if (arcticTable.isKeyedTable()) {
      KeyedTable keyedArcticTable = arcticTable.asKeyedTable();
      LOG.info("{} start cleaning content files of base store", arcticTable.id());
      int deleteFilesCnt = clearInternalTableContentsFiles(keyedArcticTable.baseTable(), lastTime, validFiles);
      LOG.info("{} deleted {} content files from base store", arcticTable.id(), deleteFilesCnt);

      LOG.info("{} start cleaning content files of change store", arcticTable.id());
      deleteFilesCnt = clearInternalTableContentsFiles(keyedArcticTable.changeTable(), lastTime, validFiles);
      LOG.info("{} deleted {} content files from change store", arcticTable.id(), deleteFilesCnt);
    } else {
      LOG.info("{} start cleaning content files", arcticTable.id());
      int deleteFilesCnt = clearInternalTableContentsFiles(arcticTable.asUnkeyedTable(), lastTime, validFiles);
      LOG.info("{} deleted {} content files", arcticTable.id(), deleteFilesCnt);
    }
  }

  public static void cleanMetadata(ArcticTable arcticTable, long lastTime) {
    if (arcticTable.isKeyedTable()) {
      KeyedTable keyedArcticTable = arcticTable.asKeyedTable();
      LOG.info("{} start cleaning metadata files of base store", arcticTable.id());
      int deleteFilesCnt = clearInternalTableMetadata(keyedArcticTable.baseTable(), lastTime);
      LOG.info("{} deleted {} metadata files from base store", arcticTable.id(), deleteFilesCnt);

      LOG.info("{} start cleaning metadata files of change store", arcticTable.id());
      deleteFilesCnt = clearInternalTableMetadata(keyedArcticTable.changeTable(), lastTime);
      LOG.info("{} deleted {} metadata files from change store", arcticTable.id(), deleteFilesCnt);
    } else {
      LOG.info("{} start cleaning metadata files", arcticTable.id());
      int deleteFilesCnt = clearInternalTableMetadata(arcticTable.asUnkeyedTable(), lastTime);
      LOG.info("{} deleted {} metadata files", arcticTable.id(), deleteFilesCnt);
    }
  }

  public static void cleanDanglingDeleteFiles(ArcticTable arcticTable) {
    if (!arcticTable.isKeyedTable()) {
      LOG.info("{} start deleting dangling delete files", arcticTable.id());
      int danglingDeleteFilesCnt = clearInternalTableDanglingDeleteFiles(arcticTable.asUnkeyedTable());
      LOG.info("{} deleted {} dangling delete files", arcticTable.id(), danglingDeleteFilesCnt);
    }
  }

  private static Set<String> getValidContentFiles(ArcticTable arcticTable) {
    Set<String> validFiles = new HashSet<>();
    if (arcticTable.isKeyedTable()) {
      Set<String> baseValidFiles = IcebergTableUtil.getAllContentFilePath(arcticTable.asKeyedTable().baseTable());
      LOG.info("{} found {} valid files in the base store", arcticTable.id(), baseValidFiles.size());
      Set<String> changeValidFiles = IcebergTableUtil.getAllContentFilePath(arcticTable.asKeyedTable().changeTable());
      LOG.info("{} found {} valid files in the change store", arcticTable.id(), baseValidFiles.size());
      validFiles.addAll(baseValidFiles);
      validFiles.addAll(changeValidFiles);
    } else {
      Set<String> baseValidFiles = IcebergTableUtil.getAllContentFilePath(arcticTable.asUnkeyedTable());
      validFiles.addAll(baseValidFiles);
      LOG.info("{} found {} valid files", arcticTable.id(), validFiles.size());
    }

    // add hive location to exclude
    Set<String> hiveValidLocations = HiveLocationUtil.getHiveLocation(arcticTable);
    if (hiveValidLocations.size() > 0) {
      validFiles.addAll(hiveValidLocations);
      LOG.info("{} found {} valid locations in the Hive location", arcticTable.id(), hiveValidLocations.size());
    }

    return validFiles;
  }

  private static int clearInternalTableContentsFiles(
      UnkeyedTable internalTable, long lastTime, Set<String> exclude) {
    String dataLocation = internalTable.location() + File.separator + DATA_FOLDER_NAME;

    try (ArcticFileIO io = internalTable.io()) {
      // listPrefix will not return the directory and the orphan file clean should clean the empty dir.
      if (io.supportFileSystemOperations()) {
        SupportsFileSystemOperations fio = io.asFileSystemIO();
        return deleteInvalidFilesInFs(fio, dataLocation, lastTime, exclude);
      } else if (io.supportPrefixOperations()) {
        SupportsPrefixOperations pio = io.asPrefixFileIO();
        return deleteInvalidFilesByPrefix(pio, dataLocation, lastTime, exclude);
      } else {
        LOG.warn(String.format(
            "Table %s doesn't support a fileIo with listDirectory or listPrefix, so skip clear files.",
            internalTable.name()
        ));
      }
    }

    return 0;
  }

  private static int deleteInvalidFilesInFs(
      SupportsFileSystemOperations fio, String location, long lastTime, Set<String> excludes
  ) {
    if (!fio.exists(location)) {
      return 0;
    }

    int deleteCount = 0;
    for (PathInfo p : fio.listDirectory(location)) {
      String uriPath = TableFileUtil.getUriPath(p.location());
      if (p.isDirectory()) {
        int deleted = deleteInvalidFilesInFs(fio, p.location(), lastTime, excludes);
        deleteCount += deleted;
        if (!p.location().endsWith(METADATA_FOLDER_NAME) &&
            !p.location().endsWith(DATA_FOLDER_NAME) &&
            p.createdAtMillis() < lastTime &&
            fio.isEmptyDirectory(p.location())) {
          TableFileUtil.deleteEmptyDirectory(fio, p.location(), excludes);
        }
      } else {
        String parentLocation = TableFileUtil.getParent(p.location());
        String parentUriPath = TableFileUtil.getUriPath(parentLocation);
        if (!excludes.contains(uriPath) &&
            !excludes.contains(parentUriPath) &&
            p.createdAtMillis() < lastTime) {
          fio.deleteFile(p.location());
          deleteCount += 1;
        }
      }
    }
    return deleteCount;
  }

  private static int deleteInvalidFilesByPrefix(
      SupportsPrefixOperations pio, String prefix, long lastTime, Set<String> excludes
  ) {
    int deleteCount = 0;
    for (FileInfo fileInfo : pio.listPrefix(prefix)) {
      String uriPath = TableFileUtil.getUriPath(fileInfo.location());
      if (!excludes.contains(uriPath) && fileInfo.createdAtMillis() < lastTime) {
        pio.deleteFile(fileInfo.location());
        deleteCount += 1;
      }
    }
    return deleteCount;
  }

  private static int clearInternalTableMetadata(UnkeyedTable internalTable, long lastTime) {
    Set<String> validFiles = getValidMetadataFiles(internalTable);
    LOG.info("{} found {} valid metadata files", internalTable.id(), validFiles.size());
    Pattern excludeFileNameRegex = getExcludeFileNameRegex(internalTable);
    LOG.info("{} generated exclude file name pattern {}", internalTable.id(), excludeFileNameRegex);
    String metadataLocation = internalTable.location() + File.separator + METADATA_FOLDER_NAME;

    try (ArcticFileIO io = internalTable.io()) {
      if (io.supportPrefixOperations()) {
        SupportsPrefixOperations pio = io.asPrefixFileIO();
        return deleteInvalidMetadataFile(pio, metadataLocation, lastTime, validFiles, excludeFileNameRegex);
      } else {
        LOG.warn(String.format(
            "Table %s doesn't support a fileIo with listDirectory or listPrefix, so skip clear files.",
            internalTable.name()
        ));
      }
    }
    return 0;
  }

  private static int clearInternalTableDanglingDeleteFiles(UnkeyedTable internalTable) {
    Set<DeleteFile> danglingDeleteFiles = IcebergTableUtil.getDanglingDeleteFiles(internalTable);
    if (danglingDeleteFiles.isEmpty()) {
      return 0;
    }
    RewriteFiles rewriteFiles = internalTable.newRewrite();
    rewriteFiles.rewriteFiles(Collections.emptySet(), danglingDeleteFiles,
        Collections.emptySet(), Collections.emptySet());
    try {
      rewriteFiles.commit();
    } catch (ValidationException e) {
      LOG.warn("Iceberg RewriteFiles commit failed on clear danglingDeleteFiles, but ignore", e);
      return 0;
    }
    return danglingDeleteFiles.size();
  }

  private static Set<String> getValidMetadataFiles(UnkeyedTable internalTable) {
    TableIdentifier tableIdentifier = internalTable.id();
    Set<String> validFiles = new HashSet<>();
    Iterable<Snapshot> snapshots = internalTable.snapshots();
    int size = Iterables.size(snapshots);
    LOG.info("{} needs to scan {} snapshots to find all validate metadata files", tableIdentifier, size);
    int cnt = 0;
    for (Snapshot snapshot : snapshots) {
      cnt++;
      int before = validFiles.size();
      String manifestListLocation = snapshot.manifestListLocation();

      validFiles.add(TableFileUtil.getUriPath(manifestListLocation));

      // valid data files
      List<ManifestFile> manifestFiles = snapshot.allManifests(internalTable.io());
      for (ManifestFile manifestFile : manifestFiles) {
        validFiles.add(TableFileUtil.getUriPath(manifestFile.path()));
      }

      LOG.info(
          "{} scan snapshot {}: {} and get {} files, complete {}/{}",
          tableIdentifier,
          snapshot.snapshotId(),
          formatTime(snapshot.timestampMillis()),
          validFiles.size() - before,
          cnt,
          size);
    }
    Stream.concat(
            ReachableFileUtil.metadataFileLocations(internalTable, false).stream(),
            Stream.of(ReachableFileUtil.versionHintLocation(internalTable)))
        .map(TableFileUtil::getUriPath)
        .forEach(validFiles::add);

    return validFiles;
  }

  private static Pattern getExcludeFileNameRegex(UnkeyedTable table) {
    String latestFlinkJobId = null;
    for (Snapshot snapshot : table.snapshots()) {
      String flinkJobId = snapshot.summary().get(FLINK_JOB_ID);
      if (!Strings.isNullOrEmpty(flinkJobId)) {
        latestFlinkJobId = flinkJobId;
      }
    }
    if (latestFlinkJobId != null) {
      // file name starting with flink.job-id should not be deleted
      return Pattern.compile(latestFlinkJobId + ".*");
    }
    return null;
  }

  private static int deleteInvalidMetadataFile(
      SupportsPrefixOperations pio, String location, long lastTime, Set<String> exclude, Pattern excludeRegex
  ) {
    int count = 0;
    for (FileInfo fileInfo : pio.listPrefix(location)) {
      String uriPath = TableFileUtil.getUriPath(fileInfo.location());
      if (!exclude.contains(uriPath) &&
          fileInfo.createdAtMillis() < lastTime &&
          (excludeRegex == null || !excludeRegex.matcher(
              TableFileUtil.getFileName(fileInfo.location())).matches())) {
        pio.deleteFile(fileInfo.location());
        count += 1;
      }
    }
    return count;
  }

  private static String formatTime(long timestamp) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString();
  }
}


/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing.maintainer;

import static org.apache.iceberg.relocated.com.google.common.primitives.Longs.min;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.arctic.ams.api.CommitMetaProducer;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.PathInfo;
import com.netease.arctic.io.SupportsFileSystemOperations;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.table.DataExpirationConfig;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.utils.TableFileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.ContentScanTask;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.ReachableFileUtil;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.Literal;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileInfo;
import org.apache.iceberg.io.SupportsPrefixOperations;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Table maintainer for iceberg tables. */
public class IcebergTableMaintainer implements TableMaintainer {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergTableMaintainer.class);

  public static final String METADATA_FOLDER_NAME = "metadata";
  public static final String DATA_FOLDER_NAME = "data";
  // same as org.apache.iceberg.flink.sink.IcebergFilesCommitter#FLINK_JOB_ID
  public static final String FLINK_JOB_ID = "flink.job-id";
  // same as org.apache.iceberg.flink.sink.IcebergFilesCommitter#MAX_COMMITTED_CHECKPOINT_ID
  public static final String FLINK_MAX_COMMITTED_CHECKPOINT_ID =
      "flink.max-committed-checkpoint-id";

  public static final String EXPIRE_TIMESTAMP_MS = "TIMESTAMP_MS";
  public static final String EXPIRE_TIMESTAMP_S = "TIMESTAMP_S";

  protected Table table;

  public IcebergTableMaintainer(Table table) {
    this.table = table;
  }

  @Override
  public void cleanOrphanFiles(TableRuntime tableRuntime) {
    TableConfiguration tableConfiguration = tableRuntime.getTableConfiguration();

    if (!tableConfiguration.isCleanOrphanEnabled()) {
      return;
    }

    long keepTime = tableConfiguration.getOrphanExistingMinutes() * 60 * 1000;

    cleanContentFiles(System.currentTimeMillis() - keepTime);

    // refresh
    table.refresh();

    // clear metadata files
    cleanMetadata(System.currentTimeMillis() - keepTime);
  }

  @Override
  public void cleanDanglingDeleteFiles(TableRuntime tableRuntime) {
    TableConfiguration tableConfiguration = tableRuntime.getTableConfiguration();

    if (!tableConfiguration.isDeleteDanglingDeleteFilesEnabled()) {
      return;
    }

    Snapshot currentSnapshot = table.currentSnapshot();
    java.util.Optional<String> totalDeleteFiles =
        java.util.Optional.ofNullable(
            currentSnapshot.summary().get(SnapshotSummary.TOTAL_DELETE_FILES_PROP));
    if (totalDeleteFiles.isPresent() && Long.parseLong(totalDeleteFiles.get()) > 0) {
      // clear dangling delete files
      cleanDanglingDeleteFiles();
    } else {
      LOG.debug(
          "Table {} does not have any delete files, so there is no need to clean dangling delete file",
          table.name());
    }
  }

  @Override
  public void expireSnapshots(TableRuntime tableRuntime) {
    if (!expireSnapshotEnabled(tableRuntime)) {
      return;
    }
    expireSnapshots(mustOlderThan(tableRuntime, System.currentTimeMillis()));
  }

  protected boolean expireSnapshotEnabled(TableRuntime tableRuntime) {
    TableConfiguration tableConfiguration = tableRuntime.getTableConfiguration();
    return tableConfiguration.isExpireSnapshotEnabled();
  }

  @VisibleForTesting
  void expireSnapshots(long mustOlderThan) {
    expireSnapshots(mustOlderThan, expireSnapshotNeedToExcludeFiles());
  }

  private void expireSnapshots(long olderThan, Set<String> exclude) {
    LOG.debug("start expire snapshots older than {}, the exclude is {}", olderThan, exclude);
    final AtomicInteger toDeleteFiles = new AtomicInteger(0);
    final AtomicInteger deleteFiles = new AtomicInteger(0);
    Set<String> parentDirectory = new HashSet<>();
    table
        .expireSnapshots()
        .retainLast(1)
        .expireOlderThan(olderThan)
        .deleteWith(
            file -> {
              try {
                if (exclude.isEmpty()) {
                  arcticFileIO().deleteFile(file);
                } else {
                  String fileUriPath = TableFileUtil.getUriPath(file);
                  if (!exclude.contains(fileUriPath)
                      && !exclude.contains(new Path(fileUriPath).getParent().toString())) {
                    arcticFileIO().deleteFile(file);
                  }
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
    if (arcticFileIO().supportFileSystemOperations()) {
      parentDirectory.forEach(
          parent -> TableFileUtil.deleteEmptyDirectory(arcticFileIO(), parent, exclude));
    }
    LOG.info("to delete {} files, success delete {} files", toDeleteFiles.get(), deleteFiles.get());
  }

  @Override
  public void expireData(TableRuntime tableRuntime) {
    try {
      DataExpirationConfig expirationConfig =
          tableRuntime.getTableConfiguration().getExpiringDataConfig();
      Types.NestedField field = table.schema().findField(expirationConfig.getExpirationField());
      if (!expirationConfig.isValid(field, table.name())) {
        return;
      }

      expireDataFrom(expirationConfig, expireBaseOnRule(expirationConfig, field));
    } catch (Throwable t) {
      LOG.error("Unexpected purge error for table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }

  protected Instant expireBaseOnRule(
      DataExpirationConfig expirationConfig, Types.NestedField field) {
    switch (expirationConfig.getBaseOnRule()) {
      case CURRENT_TIME:
        return Instant.now().atZone(getDefaultZoneId(field)).toInstant();
      case LAST_COMMIT_TIME:
        Snapshot snapshot = IcebergTableUtil.getSnapshot(getTable(), false);
        long lastCommitTimestamp = fetchLatestNonOptimizedSnapshotTime(getTable());
        // if the table does not exist any non-optimized snapshots, should skip the expiration
        if (lastCommitTimestamp != Long.MAX_VALUE) {
          // snapshot timestamp should be UTC
          return Instant.ofEpochMilli(lastCommitTimestamp).atZone(ZoneOffset.UTC).toInstant();
        } else {
          return Instant.MIN;
        }
      default:
        return Instant.MIN;
    }
  }

  /**
   * Purge data older than the specified UTC timestamp
   *
   * @param expirationConfig expiration configs
   * @param instant timestamp/timestampz/long field type uses UTC, others will use the local time
   *     zone
   */
  @VisibleForTesting
  public void expireDataFrom(DataExpirationConfig expirationConfig, Instant instant) {
    if (instant.equals(Instant.MIN)) {
      return;
    }

    long expireTimestamp = instant.minusMillis(expirationConfig.getRetentionTime()).toEpochMilli();
    LOG.info(
        "Expiring data older than {} in table {} ",
        Instant.ofEpochMilli(expireTimestamp)
            .atZone(
                getDefaultZoneId(table.schema().findField(expirationConfig.getExpirationField())))
            .toLocalDateTime(),
        table.name());

    Expression dataFilter = getDataExpression(table.schema(), expirationConfig, expireTimestamp);

    ExpireFiles expiredFiles = expiredFileScan(expirationConfig, dataFilter, expireTimestamp);
    expireFiles(expiredFiles, expireTimestamp);
  }

  @Override
  public void autoCreateTags(TableRuntime tableRuntime) {
    new AutoCreateIcebergTagAction(
            table, tableRuntime.getTableConfiguration().getTagConfiguration(), LocalDateTime.now())
        .execute();
  }

  protected void cleanContentFiles(long lastTime) {
    // For clean data files, should getRuntime valid files in the base store and the change store,
    // so acquire in advance
    // to prevent repeated acquisition
    Set<String> validFiles = orphanFileCleanNeedToExcludeFiles();
    LOG.info("{} start clean content files of change store", table.name());
    int deleteFilesCnt = clearInternalTableContentsFiles(lastTime, validFiles);
    LOG.info("{} total delete {} files from change store", table.name(), deleteFilesCnt);
  }

  protected void cleanMetadata(long lastTime) {
    LOG.info("{} start clean metadata files", table.name());
    int deleteFilesCnt = clearInternalTableMetadata(lastTime);
    LOG.info("{} total delete {} metadata files", table.name(), deleteFilesCnt);
  }

  protected void cleanDanglingDeleteFiles() {
    LOG.info("{} start delete dangling delete files", table.name());
    int danglingDeleteFilesCnt = clearInternalTableDanglingDeleteFiles();
    LOG.info("{} total delete {} dangling delete files", table.name(), danglingDeleteFilesCnt);
  }

  protected long mustOlderThan(TableRuntime tableRuntime, long now) {
    return min(
        // The snapshots keep time
        now - snapshotsKeepTime(tableRuntime),
        // The snapshot optimizing plan based should not be expired for committing
        fetchOptimizingPlanSnapshotTime(table, tableRuntime),
        // The latest non-optimized snapshot should not be expired for data expiring
        fetchLatestNonOptimizedSnapshotTime(table),
        // The latest flink committed snapshot should not be expired for recovering flink job
        fetchLatestFlinkCommittedSnapshotTime(table));
  }

  protected long snapshotsKeepTime(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().getSnapshotTTLMinutes() * 60 * 1000;
  }

  protected Set<String> expireSnapshotNeedToExcludeFiles() {
    return Collections.emptySet();
  }

  protected Set<String> orphanFileCleanNeedToExcludeFiles() {
    return Sets.union(
        IcebergTableUtil.getAllContentFilePath(table),
        IcebergTableUtil.getAllStatisticsFilePath(table));
  }

  protected ArcticFileIO arcticFileIO() {
    return (ArcticFileIO) table.io();
  }

  private int clearInternalTableContentsFiles(long lastTime, Set<String> exclude) {
    String dataLocation = table.location() + File.separator + DATA_FOLDER_NAME;

    try (ArcticFileIO io = arcticFileIO()) {
      // listPrefix will not return the directory and the orphan file clean should clean the empty
      // dir.
      if (io.supportFileSystemOperations()) {
        SupportsFileSystemOperations fio = io.asFileSystemIO();
        return deleteInvalidFilesInFs(fio, dataLocation, lastTime, exclude);
      } else if (io.supportPrefixOperations()) {
        SupportsPrefixOperations pio = io.asPrefixFileIO();
        return deleteInvalidFilesByPrefix(pio, dataLocation, lastTime, exclude);
      } else {
        LOG.warn(
            String.format(
                "Table %s doesn't support a fileIo with listDirectory or listPrefix, so skip clear files.",
                table.name()));
      }
    }

    return 0;
  }

  private int clearInternalTableMetadata(long lastTime) {
    Set<String> validFiles = getValidMetadataFiles(table);
    LOG.info("{} table getRuntime {} valid files", table.name(), validFiles.size());
    Pattern excludeFileNameRegex = getExcludeFileNameRegex(table);
    LOG.info(
        "{} table getRuntime exclude file name pattern {}", table.name(), excludeFileNameRegex);
    String metadataLocation = table.location() + File.separator + METADATA_FOLDER_NAME;
    LOG.info("start orphan files clean in {}", metadataLocation);

    try (ArcticFileIO io = arcticFileIO()) {
      if (io.supportPrefixOperations()) {
        SupportsPrefixOperations pio = io.asPrefixFileIO();
        return deleteInvalidMetadataFile(
            pio, metadataLocation, lastTime, validFiles, excludeFileNameRegex);
      } else {
        LOG.warn(
            String.format(
                "Table %s doesn't support a fileIo with listDirectory or listPrefix, so skip clear files.",
                table.name()));
      }
    }
    return 0;
  }

  private int clearInternalTableDanglingDeleteFiles() {
    Set<DeleteFile> danglingDeleteFiles = IcebergTableUtil.getDanglingDeleteFiles(table);
    if (danglingDeleteFiles.isEmpty()) {
      return 0;
    }
    RewriteFiles rewriteFiles = table.newRewrite();
    rewriteFiles.rewriteFiles(
        Collections.emptySet(),
        danglingDeleteFiles,
        Collections.emptySet(),
        Collections.emptySet());
    try {
      rewriteFiles.commit();
    } catch (ValidationException e) {
      LOG.warn("Iceberg RewriteFiles commit failed on clear danglingDeleteFiles, but ignore", e);
      return 0;
    }
    return danglingDeleteFiles.size();
  }

  /**
   * When committing a snapshot, Flink will write a checkpoint id into the snapshot summary, which
   * will be used when Flink job recovers from the checkpoint.
   *
   * @param table table
   * @return commit time of snapshot with the latest flink checkpointId in summary, return
   *     Long.MAX_VALUE if not exist
   */
  public static long fetchLatestFlinkCommittedSnapshotTime(Table table) {
    Snapshot snapshot = findLatestSnapshotContainsKey(table, FLINK_MAX_COMMITTED_CHECKPOINT_ID);
    return snapshot == null ? Long.MAX_VALUE : snapshot.timestampMillis();
  }

  /**
   * When the current optimizing process not committed, get the time of snapshot for optimizing
   * process planned based. This snapshot will be used when optimizing process committing.
   *
   * @param table table
   * @param tableRuntime table runtime
   * @return time of snapshot for optimizing process planned based, return Long.MAX_VALUE if no
   *     optimizing process exists
   */
  public static long fetchOptimizingPlanSnapshotTime(Table table, TableRuntime tableRuntime) {
    if (tableRuntime.getOptimizingStatus().isProcessing()) {
      long fromSnapshotId = tableRuntime.getOptimizingProcess().getTargetSnapshotId();

      for (Snapshot snapshot : table.snapshots()) {
        if (snapshot.snapshotId() == fromSnapshotId) {
          return snapshot.timestampMillis();
        }
      }
    }
    return Long.MAX_VALUE;
  }

  public static Snapshot findLatestSnapshotContainsKey(Table table, String summaryKey) {
    Snapshot latestSnapshot = null;
    for (Snapshot snapshot : table.snapshots()) {
      if (snapshot.summary().containsKey(summaryKey)) {
        latestSnapshot = snapshot;
      }
    }
    return latestSnapshot;
  }

  /**
   * When expiring historic data and `data-expire.base-on-rule` is `LAST_COMMIT_TIME`, the latest
   * snapshot should not be produced by Amoro optimizing.
   *
   * @param table iceberg table
   * @return the latest non-optimized snapshot timestamp
   */
  public static long fetchLatestNonOptimizedSnapshotTime(Table table) {
    Optional<Snapshot> snapshot =
        IcebergTableUtil.findSnapshotDesc(
            table, s -> !s.summary().containsValue(CommitMetaProducer.OPTIMIZE.name()));
    return snapshot.map(Snapshot::timestampMillis).orElse(Long.MAX_VALUE);
  }

  private static int deleteInvalidFilesInFs(
      SupportsFileSystemOperations fio, String location, long lastTime, Set<String> excludes) {
    if (!fio.exists(location)) {
      return 0;
    }

    int deleteCount = 0;
    for (PathInfo p : fio.listDirectory(location)) {
      String uriPath = TableFileUtil.getUriPath(p.location());
      if (p.isDirectory()) {
        int deleted = deleteInvalidFilesInFs(fio, p.location(), lastTime, excludes);
        deleteCount += deleted;
        if (!p.location().endsWith(METADATA_FOLDER_NAME)
            && !p.location().endsWith(DATA_FOLDER_NAME)
            && p.createdAtMillis() < lastTime
            && fio.isEmptyDirectory(p.location())) {
          TableFileUtil.deleteEmptyDirectory(fio, p.location(), excludes);
        }
      } else {
        String parentLocation = TableFileUtil.getParent(p.location());
        String parentUriPath = TableFileUtil.getUriPath(parentLocation);
        if (!excludes.contains(uriPath)
            && !excludes.contains(parentUriPath)
            && p.createdAtMillis() < lastTime) {
          fio.deleteFile(p.location());
          deleteCount += 1;
        }
      }
    }
    return deleteCount;
  }

  private static int deleteInvalidFilesByPrefix(
      SupportsPrefixOperations pio, String prefix, long lastTime, Set<String> excludes) {
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

  private static Set<String> getValidMetadataFiles(Table internalTable) {
    String tableName = internalTable.name();
    Set<String> validFiles = new HashSet<>();
    Iterable<Snapshot> snapshots = internalTable.snapshots();
    int size = Iterables.size(snapshots);
    LOG.info("{} getRuntime {} snapshots to scan", tableName, size);
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
          "{} scan snapshot {}: {} and getRuntime {} files, complete {}/{}",
          tableName,
          snapshot.snapshotId(),
          formatTime(snapshot.timestampMillis()),
          validFiles.size() - before,
          cnt,
          size);
    }
    Stream.of(
            ReachableFileUtil.metadataFileLocations(internalTable, false).stream(),
            ReachableFileUtil.statisticsFilesLocations(internalTable).stream(),
            Stream.of(ReachableFileUtil.versionHintLocation(internalTable)))
        .reduce(Stream::concat)
        .orElse(Stream.empty())
        .map(TableFileUtil::getUriPath)
        .forEach(validFiles::add);

    return validFiles;
  }

  private static Pattern getExcludeFileNameRegex(Table table) {
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
      SupportsPrefixOperations pio,
      String location,
      long lastTime,
      Set<String> exclude,
      Pattern excludeRegex) {
    int count = 0;
    for (FileInfo fileInfo : pio.listPrefix(location)) {
      String uriPath = TableFileUtil.getUriPath(fileInfo.location());
      if (!exclude.contains(uriPath)
          && fileInfo.createdAtMillis() < lastTime
          && (excludeRegex == null
              || !excludeRegex.matcher(TableFileUtil.getFileName(fileInfo.location())).matches())) {
        pio.deleteFile(fileInfo.location());
        count += 1;
      }
    }
    return count;
  }

  private static String formatTime(long timestamp) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        .toString();
  }

  CloseableIterable<FileEntry> fileScan(
      Table table, Expression dataFilter, DataExpirationConfig expirationConfig) {
    TableScan tableScan = table.newScan().filter(dataFilter).includeColumnStats();

    CloseableIterable<FileScanTask> tasks;
    Snapshot snapshot = IcebergTableUtil.getSnapshot(table, false);
    long snapshotId = snapshot.snapshotId();
    if (snapshotId == ArcticServiceConstants.INVALID_SNAPSHOT_ID) {
      tasks = tableScan.planFiles();
    } else {
      tasks = tableScan.useSnapshot(snapshotId).planFiles();
    }
    long deleteFileCnt =
        Long.parseLong(
            snapshot
                .summary()
                .getOrDefault(org.apache.iceberg.SnapshotSummary.TOTAL_DELETE_FILES_PROP, "0"));
    CloseableIterable<DataFile> dataFiles =
        CloseableIterable.transform(tasks, ContentScanTask::file);
    CloseableIterable<FileScanTask> hasDeleteTask =
        deleteFileCnt > 0
            ? CloseableIterable.filter(tasks, t -> !t.deletes().isEmpty())
            : CloseableIterable.empty();

    Set<DeleteFile> deleteFiles =
        StreamSupport.stream(hasDeleteTask.spliterator(), true)
            .flatMap(e -> e.deletes().stream())
            .collect(Collectors.toSet());

    Types.NestedField field = table.schema().findField(expirationConfig.getExpirationField());
    return CloseableIterable.transform(
        CloseableIterable.withNoopClose(Iterables.concat(dataFiles, deleteFiles)),
        contentFile -> {
          Literal<Long> literal =
              getExpireTimestampLiteral(
                  contentFile,
                  field,
                  DateTimeFormatter.ofPattern(
                      expirationConfig.getDateTimePattern(), Locale.getDefault()),
                  expirationConfig.getNumberDateFormat());
          return new FileEntry(contentFile.copyWithoutStats(), literal);
        });
  }

  protected ExpireFiles expiredFileScan(
      DataExpirationConfig expirationConfig, Expression dataFilter, long expireTimestamp) {
    Map<StructLike, DataFileFreshness> partitionFreshness = Maps.newConcurrentMap();
    ExpireFiles expiredFiles = new ExpireFiles();
    try (CloseableIterable<FileEntry> entries = fileScan(table, dataFilter, expirationConfig)) {
      Queue<FileEntry> fileEntries = new LinkedTransferQueue<>();
      entries.forEach(
          e -> {
            if (mayExpired(e, partitionFreshness, expireTimestamp)) {
              fileEntries.add(e);
            }
          });
      fileEntries
          .parallelStream()
          .filter(e -> willNotRetain(e, expirationConfig, partitionFreshness))
          .forEach(expiredFiles::addFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return expiredFiles;
  }

  /**
   * Create a filter expression for expired files for the `FILE` level. For the `PARTITION` level,
   * we need to collect the oldest files to determine if the partition is obsolete, so we will not
   * filter for expired files at the scanning stage
   *
   * @param expirationConfig expiration configuration
   * @param expireTimestamp expired timestamp
   */
  protected static Expression getDataExpression(
      Schema schema, DataExpirationConfig expirationConfig, long expireTimestamp) {
    if (expirationConfig.getExpirationLevel().equals(DataExpirationConfig.ExpireLevel.PARTITION)) {
      return Expressions.alwaysTrue();
    }

    Types.NestedField field = schema.findField(expirationConfig.getExpirationField());
    Type.TypeID typeID = field.type().typeId();
    switch (typeID) {
      case TIMESTAMP:
        return Expressions.lessThanOrEqual(field.name(), expireTimestamp * 1000);
      case LONG:
        if (expirationConfig.getNumberDateFormat().equals(EXPIRE_TIMESTAMP_MS)) {
          return Expressions.lessThanOrEqual(field.name(), expireTimestamp);
        } else if (expirationConfig.getNumberDateFormat().equals(EXPIRE_TIMESTAMP_S)) {
          return Expressions.lessThanOrEqual(field.name(), expireTimestamp / 1000);
        } else {
          return Expressions.alwaysTrue();
        }
      case STRING:
        String expireDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(expireTimestamp), getDefaultZoneId(field))
                .format(
                    DateTimeFormatter.ofPattern(
                        expirationConfig.getDateTimePattern(), Locale.getDefault()));
        return Expressions.lessThanOrEqual(field.name(), expireDateTime);
      default:
        return Expressions.alwaysTrue();
    }
  }

  void expireFiles(ExpireFiles expiredFiles, long expireTimestamp) {
    long snapshotId = IcebergTableUtil.getSnapshotId(table, false);
    Queue<DataFile> dataFiles = expiredFiles.dataFiles;
    Queue<DeleteFile> deleteFiles = expiredFiles.deleteFiles;
    if (dataFiles.isEmpty() && deleteFiles.isEmpty()) {
      return;
    }
    // expire data files
    DeleteFiles delete = table.newDelete();
    dataFiles.forEach(delete::deleteFile);
    delete.set(com.netease.arctic.op.SnapshotSummary.SNAPSHOT_PRODUCER, "DATA_EXPIRATION");
    delete.commit();
    // expire delete files
    if (!deleteFiles.isEmpty()) {
      RewriteFiles rewriteFiles = table.newRewrite().validateFromSnapshot(snapshotId);
      deleteFiles.forEach(rewriteFiles::deleteFile);
      rewriteFiles.set(com.netease.arctic.op.SnapshotSummary.SNAPSHOT_PRODUCER, "DATA_EXPIRATION");
      rewriteFiles.commit();
    }

    // TODO: persistent table expiration record. Contains some meta information such as table_id,
    // snapshotId,
    //  file_infos(file_content, path, recordCount, fileSizeInBytes, equalityFieldIds,
    // partitionPath,
    //  sequenceNumber) and expireTimestamp...

    LOG.info(
        "Expired {} files older than {}, {} data files[{}] and {} delete files[{}]",
        table.name(),
        expireTimestamp,
        dataFiles.size(),
        dataFiles.stream().map(ContentFile::path).collect(Collectors.joining(",")),
        deleteFiles.size(),
        deleteFiles.stream().map(ContentFile::path).collect(Collectors.joining(",")));
  }

  public static class ExpireFiles {
    Queue<DataFile> dataFiles;
    Queue<DeleteFile> deleteFiles;

    ExpireFiles() {
      this.dataFiles = new LinkedTransferQueue<>();
      this.deleteFiles = new LinkedTransferQueue<>();
    }

    void addFile(FileEntry entry) {
      ContentFile<?> file = entry.getFile();
      switch (file.content()) {
        case DATA:
          dataFiles.add((DataFile) file.copyWithoutStats());
          break;
        case EQUALITY_DELETES:
        case POSITION_DELETES:
          deleteFiles.add((DeleteFile) file.copyWithoutStats());
          break;
        default:
          throw new IllegalArgumentException(file.content().name() + "cannot be expired");
      }
    }
  }

  public static class DataFileFreshness {
    long latestExpiredSeq;
    long latestUpdateMillis;
    long expiredDataFileCount;
    long totalDataFileCount;

    DataFileFreshness(long sequenceNumber, long latestUpdateMillis) {
      this.latestExpiredSeq = sequenceNumber;
      this.latestUpdateMillis = latestUpdateMillis;
    }

    DataFileFreshness updateLatestMillis(long ts) {
      this.latestUpdateMillis = ts;
      return this;
    }

    DataFileFreshness updateExpiredSeq(Long seq) {
      this.latestExpiredSeq = seq;
      return this;
    }

    DataFileFreshness incTotalCount() {
      totalDataFileCount++;
      return this;
    }

    DataFileFreshness incExpiredCount() {
      expiredDataFileCount++;
      return this;
    }
  }

  static boolean mayExpired(
      FileEntry fileEntry,
      Map<StructLike, DataFileFreshness> partitionFreshness,
      Long expireTimestamp) {
    ContentFile<?> contentFile = fileEntry.getFile();
    StructLike partition = contentFile.partition();

    boolean expired = true;
    if (contentFile.content().equals(FileContent.DATA)) {
      Literal<Long> literal = fileEntry.getTsBound();
      if (partitionFreshness.containsKey(partition)) {
        DataFileFreshness freshness = partitionFreshness.get(partition).incTotalCount();
        if (freshness.latestUpdateMillis <= literal.value()) {
          partitionFreshness.put(partition, freshness.updateLatestMillis(literal.value()));
        }
      } else {
        partitionFreshness.putIfAbsent(
            partition,
            new DataFileFreshness(fileEntry.getFile().dataSequenceNumber(), literal.value())
                .incTotalCount());
      }
      expired = literal.comparator().compare(expireTimestamp, literal.value()) >= 0;
      if (expired) {
        partitionFreshness.computeIfPresent(
            partition,
            (k, v) ->
                v.updateExpiredSeq(fileEntry.getFile().dataSequenceNumber()).incExpiredCount());
      }
    }

    return expired;
  }

  static boolean willNotRetain(
      FileEntry fileEntry,
      DataExpirationConfig expirationConfig,
      Map<StructLike, DataFileFreshness> partitionFreshness) {
    ContentFile<?> contentFile = fileEntry.getFile();

    switch (expirationConfig.getExpirationLevel()) {
      case PARTITION:
        // if only partial expired files in a partition, all the files in that partition should be
        // preserved
        return partitionFreshness.containsKey(contentFile.partition())
            && partitionFreshness.get(contentFile.partition()).expiredDataFileCount
                == partitionFreshness.get(contentFile.partition()).totalDataFileCount;
      case FILE:
        if (!contentFile.content().equals(FileContent.DATA)) {
          long seqUpperBound =
              partitionFreshness.getOrDefault(
                      contentFile.partition(),
                      new DataFileFreshness(Long.MIN_VALUE, Long.MAX_VALUE))
                  .latestExpiredSeq;
          // only expire delete files with sequence-number less or equal to expired data file
          // there may be some dangling delete files, they will be cleaned by
          // OrphanFileCleaningExecutor
          return fileEntry.getFile().dataSequenceNumber() <= seqUpperBound;
        } else {
          return true;
        }
      default:
        return false;
    }
  }

  private static Literal<Long> getExpireTimestampLiteral(
      ContentFile<?> contentFile,
      Types.NestedField field,
      DateTimeFormatter formatter,
      String numberDateFormatter) {
    Type type = field.type();
    Object upperBound =
        Conversions.fromByteBuffer(type, contentFile.upperBounds().get(field.fieldId()));
    Literal<Long> literal = Literal.of(Long.MAX_VALUE);
    if (null == upperBound) {
      return literal;
    } else if (upperBound instanceof Long) {
      switch (type.typeId()) {
        case TIMESTAMP:
          // nanosecond -> millisecond
          literal = Literal.of((Long) upperBound / 1000);
          break;
        default:
          if (numberDateFormatter.equals(EXPIRE_TIMESTAMP_MS)) {
            literal = Literal.of((Long) upperBound);
          } else if (numberDateFormatter.equals(EXPIRE_TIMESTAMP_S)) {
            // second -> millisecond
            literal = Literal.of((Long) upperBound * 1000);
          }
      }
    } else if (type.typeId().equals(Type.TypeID.STRING)) {
      literal =
          Literal.of(
              LocalDate.parse(upperBound.toString(), formatter)
                  .atStartOfDay()
                  .atZone(getDefaultZoneId(field))
                  .toInstant()
                  .toEpochMilli());
    }
    return literal;
  }

  public Table getTable() {
    return table;
  }

  public static ZoneId getDefaultZoneId(Types.NestedField expireField) {
    Type type = expireField.type();
    if (type.typeId() == Type.TypeID.STRING) {
      return ZoneId.systemDefault();
    }
    return ZoneOffset.UTC;
  }

  public static class FileEntry {
    private final ContentFile<?> file;
    private final Literal<Long> tsBound;

    FileEntry(ContentFile<?> file, Literal<Long> tsBound) {
      this.file = file;
      this.tsBound = tsBound;
    }

    public ContentFile<?> getFile() {
      return file;
    }

    public Literal<Long> getTsBound() {
      return tsBound;
    }
  }
}

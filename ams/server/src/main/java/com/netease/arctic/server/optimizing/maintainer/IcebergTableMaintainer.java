package com.netease.arctic.server.optimizing.maintainer;

import com.google.common.base.Strings;
import com.netease.arctic.TableSnapshot;
import com.netease.arctic.formats.iceberg.IcebergSnapshot;
import com.netease.arctic.formats.mixed.iceberg.MixedIcebergSnapshot;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.PathInfo;
import com.netease.arctic.io.SupportsFileSystemOperations;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.TableFileUtil;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.ReachableFileUtil;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.io.FileInfo;
import org.apache.iceberg.io.SupportsPrefixOperations;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.iceberg.relocated.com.google.common.primitives.Longs.min;

public class IcebergTableMaintainer implements TableMaintainer {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergTableMaintainer.class);

  public static final String METADATA_FOLDER_NAME = "metadata";
  public static final String DATA_FOLDER_NAME = "data";

  public static final String FLINK_JOB_ID = "flink.job-id";

  public static final String FLINK_MAX_COMMITTED_CHECKPOINT_ID = "flink.max-committed-checkpoint-id";

  protected Table table;

  public IcebergTableMaintainer(Table table) {
    this.table = table;
  }

  public void cleanContentFiles(long lastTime) {
    // For clean data files, should getRuntime valid files in the base store and the change store, so acquire in advance
    // to prevent repeated acquisition
    Set<String> validFiles = orphanFileCleanNeedToExcludeFiles();
    LOG.info("{} start clean content files of change store", table.name());
    int deleteFilesCnt = clearInternalTableContentsFiles(lastTime, validFiles);
    LOG.info("{} total delete {} files from change store", table.name(), deleteFilesCnt);
  }

  public void cleanMetadata(long lastTime) {
    LOG.info("{} start clean metadata files", table.name());
    int deleteFilesCnt = clearInternalTableMetadata(lastTime);
    LOG.info("{} total delete {} metadata files", table.name(), deleteFilesCnt);
  }

  public void cleanDanglingDeleteFiles() {
    LOG.info("{} start delete dangling delete files", table.name());
    int danglingDeleteFilesCnt = clearInternalTableDanglingDeleteFiles();
    LOG.info("{} total delete {} dangling delete files", table.name(), danglingDeleteFilesCnt);
  }

  public void expireSnapshots(TableRuntime tableRuntime) {
    expireSnapshots(olderThanSnapshotNeedToExpire(tableRuntime));
  }

  public void expireSnapshots(long mustOlderThan) {
    expireSnapshots(olderThanSnapshotNeedToExpire(mustOlderThan), expireSnapshotNeedToExcludeFiles());
  }

  protected long olderThanSnapshotNeedToExpire(TableRuntime tableRuntime) {
    long optimizingSnapshotTime = fetchOptimizingSnapshotTime(table, tableRuntime);
    return olderThanSnapshotNeedToExpire(optimizingSnapshotTime);
  }

  protected long olderThanSnapshotNeedToExpire(long mustOlderThan) {
    long baseSnapshotsKeepTime = CompatiblePropertyUtil.propertyAsLong(
        table.properties(),
        TableProperties.BASE_SNAPSHOT_KEEP_MINUTES,
        TableProperties.BASE_SNAPSHOT_KEEP_MINUTES_DEFAULT) * 60 * 1000;
    // Latest checkpoint of flink need retain. If Flink does not continuously commit new snapshots,
    // it can lead to issues with table partitions not expiring.
    long latestFlinkCommitTime = fetchLatestFlinkCommittedSnapshotTime(table);
    long olderThan = System.currentTimeMillis() - baseSnapshotsKeepTime;
    return min(latestFlinkCommitTime, mustOlderThan, olderThan);
  }

  protected Set<String> expireSnapshotNeedToExcludeFiles() {
    return Collections.emptySet();
  }


  public Set<String> orphanFileCleanNeedToExcludeFiles() {
    return IcebergTableUtil.getAllContentFilePath(table);
  }

  //todo Need rename when ArcticFileIO renaming.
  public ArcticFileIO arcticFileIO() {
    return (ArcticFileIO) table.io();
  }

  private void expireSnapshots(long olderThan, Set<String> exclude) {
    LOG.debug("start expire snapshots older than {}, the exclude is {}", olderThan, exclude);
    final AtomicInteger toDeleteFiles = new AtomicInteger(0);
    final AtomicInteger deleteFiles = new AtomicInteger(0);
    Set<String> parentDirectory = new HashSet<>();
    table.expireSnapshots()
        .retainLast(1)
        .expireOlderThan(olderThan)
        .deleteWith(file -> {
          try {
            String filePath = TableFileUtil.getUriPath(file);
            if (!exclude.contains(filePath) && !exclude.contains(new Path(filePath).getParent().toString())) {
              arcticFileIO().deleteFile(file);
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
      parentDirectory.forEach(parent -> TableFileUtil.deleteEmptyDirectory(arcticFileIO(), parent, exclude));
    }
    LOG.info("to delete {} files, success delete {} files", toDeleteFiles.get(), deleteFiles.get());
  }

  private int clearInternalTableContentsFiles(long lastTime, Set<String> exclude) {
    String dataLocation = table.location() + File.separator + DATA_FOLDER_NAME;

    try (ArcticFileIO io = arcticFileIO()) {
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
            table.name()
        ));
      }
    }

    return 0;
  }

  private int clearInternalTableMetadata(long lastTime) {
    Set<String> validFiles = getValidMetadataFiles(table);
    LOG.info("{} table getRuntime {} valid files", table.name(), validFiles.size());
    Pattern excludeFileNameRegex = getExcludeFileNameRegex(table);
    LOG.info("{} table getRuntime exclude file name pattern {}", table.name(), excludeFileNameRegex);
    String metadataLocation = table.location() + File.separator + METADATA_FOLDER_NAME;
    LOG.info("start orphan files clean in {}", metadataLocation);

    try (ArcticFileIO io = arcticFileIO()) {
      if (io.supportPrefixOperations()) {
        SupportsPrefixOperations pio = io.asPrefixFileIO();
        return deleteInvalidMetadataFile(pio, metadataLocation, lastTime, validFiles, excludeFileNameRegex);
      } else {
        LOG.warn(String.format(
            "Table %s doesn't support a fileIo with listDirectory or listPrefix, so skip clear files.",
            table.name()
        ));
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
        Collections.emptySet(), danglingDeleteFiles,
        Collections.emptySet(), Collections.emptySet());
    try {
      rewriteFiles.commit();
    } catch (ValidationException e) {
      LOG.warn("Iceberg RewriteFiles commit failed on clear danglingDeleteFiles, but ignore", e);
      return 0;
    }
    return danglingDeleteFiles.size();
  }

  /**
   * When committing a snapshot, Flink will write a checkpoint id into the snapshot summary.
   * The latest snapshot with checkpoint id should not be expired or the flink job can't recover from state.
   *
   * @param table -
   * @return commit time of snapshot with the latest flink checkpointId in summary
   */
  public static long fetchLatestFlinkCommittedSnapshotTime(Table table) {
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
  public static long fetchOptimizingSnapshotTime(Table table, TableRuntime tableRuntime) {
    if (tableRuntime.getOptimizingStatus().isProcessing()) {
      long fromSnapshotId = getFromSnapshotId(tableRuntime.getOptimizingProcess().getFromSnapshot());

      for (Snapshot snapshot : table.snapshots()) {
        if (snapshot.snapshotId() == fromSnapshotId) {
          return snapshot.timestampMillis();
        }
      }
    }
    return Long.MAX_VALUE;
  }

  private static long getFromSnapshotId(TableSnapshot fromSnapshot) {
    long fromSnapshotId;
    if (fromSnapshot instanceof MixedIcebergSnapshot) {
      fromSnapshotId = ((MixedIcebergSnapshot)fromSnapshot).getBaseSnapshotId();
    } else {
      fromSnapshotId = ((IcebergSnapshot)fromSnapshot).getSnapshot().snapshotId();
    }
    return fromSnapshotId;
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
          fio.deleteFile(uriPath);
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
    Stream.concat(
            ReachableFileUtil.metadataFileLocations(internalTable, false).stream(),
            Stream.of(ReachableFileUtil.versionHintLocation(internalTable)))
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
      SupportsPrefixOperations pio, String location, long lastTime, Set<String> exclude, Pattern excludeRegex
  ) {
    int count = 0;
    for (FileInfo fileInfo : pio.listPrefix(location)) {
      String uriPath = TableFileUtil.getUriPath(fileInfo.location());
      if (!exclude.contains(uriPath) &&
          fileInfo.createdAtMillis() < lastTime &&
          (excludeRegex == null || !excludeRegex.matcher(
              TableFileUtil.getFileName(fileInfo.location())).matches())) {
        pio.deleteFile(uriPath);
        count += 1;
      }
    }
    return count;
  }

  private static String formatTime(long timestamp) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString();
  }
}

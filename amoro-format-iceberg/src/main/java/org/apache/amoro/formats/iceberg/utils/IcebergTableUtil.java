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

package org.apache.amoro.formats.iceberg.utils;

import org.apache.amoro.IcebergFileEntry;
import org.apache.amoro.iceberg.Constants;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.scan.TableEntriesScan;
import org.apache.amoro.shade.guava32.com.google.common.base.Predicate;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.utils.TableFileUtil;
import org.apache.hadoop.fs.Path;
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
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.FileInfo;
import org.apache.iceberg.io.SupportsPrefixOperations;
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

/** Util class for Iceberg table operations in format-iceberg module. */
public class IcebergTableUtil {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergTableUtil.class);

  private IcebergTableUtil() {}

  public static long getSnapshotId(Table table, boolean refresh) {
    Snapshot currentSnapshot = getSnapshot(table, refresh);
    if (currentSnapshot == null) {
      return Constants.INVALID_SNAPSHOT_ID;
    } else {
      return currentSnapshot.snapshotId();
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

  /**
   * Find the latest optimizing snapshot in the table.
   *
   * @param table the Iceberg table
   * @return Optional snapshot
   */
  public static Optional<Snapshot> findLatestOptimizingSnapshot(Table table) {
    return IcebergTableUtil.findFirstMatchSnapshot(
        table,
        snapshot ->
            snapshot.summary().containsValue("OPTIMIZE")
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
   * Fetch all manifest files of an Iceberg Table.
   *
   * @param table An iceberg table, or maybe base store or change store of mixed-iceberg format.
   * @return Path set of all valid manifest files.
   */
  public static Set<String> getAllManifestFiles(Table table) {
    TableOperations ops = ((HasTableOperations) table).operations();

    Table allManifest =
        MetadataTableUtils.createMetadataTableInstance(
            ops,
            table.name(),
            table.name() + "#" + MetadataTableType.ALL_MANIFESTS.name(),
            MetadataTableType.ALL_MANIFESTS);

    Set<String> allManifestFiles =
        Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap());
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

  private static boolean isMetadataJson(String name) {
    return name.endsWith(".metadata.json") || name.endsWith(".metadata.json.gz");
  }

  /**
   * Returns {@code true} if another Iceberg table appears to share the same metadata location as
   * the given table.
   *
   * <p>Lists the table's metadata directory via its own (storage-agnostic) {@link FileIO} and, for
   * every {@code metadata.json} not in this table's own history (current + {@code
   * previousFiles()}), reads its {@code table-uuid}:
   *
   * <ul>
   *   <li>same uuid &rarr; older version of this table, ignored;
   *   <li>different uuid &rarr; another table shares the location, returns {@code true};
   *   <li>uuid missing/unreadable (legacy, corrupt, or compressed) &rarr; treated as a conflict,
   *       returns {@code true} (fail-safe).
   * </ul>
   *
   * <p>Requires {@link SupportsPrefixOperations} for FileIO; otherwise a {@link
   * ValidationException} is thrown. The caller may choose to skip the check when the storage
   * backend is known to be used by a single table. On the no-conflict path only a single listing is
   * done and no metadata file is read.
   *
   * @param table the table whose location is about to be cleaned
   * @throws ValidationException if the table's {@code FileIO} does not support prefix operations
   */
  public static boolean hasOtherTableInLocation(Table table) {
    TableOperations ops = ((HasTableOperations) table).operations();
    TableMetadata current = ops.current();

    String myUuid = current.uuid();
    Set<String> myMetadataFiles = Sets.newHashSet();
    myMetadataFiles.add(new Path(current.metadataFileLocation()).getName());
    for (TableMetadata.MetadataLogEntry entry : current.previousFiles()) {
      myMetadataFiles.add(new Path(entry.file()).getName());
    }

    Path metadataDir = new Path(current.metadataFileLocation()).getParent();

    AuthenticatedFileIO io = (AuthenticatedFileIO) table.io();
    if (!io.supportPrefixOperations()) {
      String msg =
          String.format(
              "Cannot detect location conflicts: the table's FileIO (%s) does not support prefix "
                  + "operations, which are required to inspect the metadata directory '%s'.",
              io.getClass().getName(), metadataDir);
      throw new ValidationException(msg);
    }

    String prefix = metadataDir.toString();
    if (!prefix.endsWith("/")) {
      prefix = prefix + "/";
    }

    SupportsPrefixOperations prefixIo = (SupportsPrefixOperations) io;
    for (FileInfo info : prefixIo.listPrefix(prefix)) {
      String name = new Path(info.location()).getName();
      if (!isMetadataJson(name) || myMetadataFiles.contains(name)) {
        continue;
      }

      String otherUuid = readTableUuid(table, info.location());
      if (otherUuid == null || !otherUuid.equals(myUuid)) {
        LOG.warn(
            "Another table (uuid={}) belonging to metadata file {} shares the metadata location with this table (uuid={}); "
                + "treating the location as conflicting.",
            otherUuid,
            info.location(),
            myUuid);
        return true;
      }
    }

    return false;
  }

  /**
   * Reads the {@code table-uuid} from a metadata file, transparently handling gzip-compressed
   * metadata ({@code .metadata.json.gz}). Returns {@code null} if the uuid cannot be determined
   * (legacy file without a uuid, corrupt file, or any read failure).
   */
  private static String readTableUuid(Table table, String metadataLocation) {
    try {
      return TableMetadataParser.read(table.io(), metadataLocation).uuid();
    } catch (Exception e) {
      LOG.warn("Failed to read table-uuid from {}; treating as unreadable", metadataLocation, e);
      return null;
    }
  }
}

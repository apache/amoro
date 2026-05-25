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

package org.apache.amoro.formats.paimon;

import static org.apache.paimon.operation.FileStoreScan.Plan.groupByPartFiles;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.formats.paimon.utils.SnapShotsScanUtils;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.ProcessTaskStatus;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.descriptor.AMSColumnInfo;
import org.apache.amoro.table.descriptor.AMSPartitionField;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.ConsumerInfo;
import org.apache.amoro.table.descriptor.DDLInfo;
import org.apache.amoro.table.descriptor.DDLReverser;
import org.apache.amoro.table.descriptor.FilesStatistics;
import org.apache.amoro.table.descriptor.FilesStatisticsBuilder;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.OperationType;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.amoro.table.descriptor.OptimizingTaskInfo;
import org.apache.amoro.table.descriptor.PartitionBaseInfo;
import org.apache.amoro.table.descriptor.PartitionFileBaseInfo;
import org.apache.amoro.table.descriptor.ServerTableMeta;
import org.apache.amoro.table.descriptor.TableSummary;
import org.apache.amoro.table.descriptor.TagOrBranchInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.FileStore;
import org.apache.paimon.Snapshot;
import org.apache.paimon.consumer.ConsumerManager;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.manifest.FileKind;
import org.apache.paimon.manifest.ManifestEntry;
import org.apache.paimon.manifest.ManifestFile;
import org.apache.paimon.manifest.ManifestFileMeta;
import org.apache.paimon.manifest.ManifestList;
import org.apache.paimon.table.DataTable;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.utils.BranchManager;
import org.apache.paimon.utils.FileStorePathFactory;
import org.apache.paimon.utils.SnapshotManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** Descriptor for Paimon format tables. */
public class PaimonTableDescriptor implements FormatTableDescriptor {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonTableDescriptor.class);

  public static final String PAIMON_MAIN_BRANCH_NAME = "main";

  private static final String PAIMON_OPTIMIZING_TYPE_FULL = "FULL";
  private static final String PAIMON_OPTIMIZING_TYPE_MINOR = "MINOR";

  private ExecutorService executor;

  @Override
  public void withIoExecutor(ExecutorService ioExecutor) {
    this.executor = ioExecutor;
  }

  @Override
  public List<TableFormat> supportFormat() {
    return Lists.newArrayList(TableFormat.PAIMON);
  }

  @Override
  public ServerTableMeta getTableDetail(AmoroTable<?> amoroTable) {
    return doAs(amoroTable, () -> getTableDetailInternal(amoroTable));
  }

  private ServerTableMeta getTableDetailInternal(AmoroTable<?> amoroTable) {
    FileStoreTable table = getTable(amoroTable);
    FileStore<?> store = table.store();

    ServerTableMeta serverTableMeta = new ServerTableMeta();
    serverTableMeta.setTableIdentifier(amoroTable.id());
    serverTableMeta.setTableType(amoroTable.format().name());

    // schema
    serverTableMeta.setSchema(
        table.rowType().getFields().stream()
            .map(
                s ->
                    new AMSColumnInfo(
                        s.name(), s.type().asSQLString(), !s.type().isNullable(), s.description()))
            .collect(Collectors.toList()));

    // primary key
    Set<String> primaryKeyNames = new HashSet<>(table.primaryKeys());
    List<AMSColumnInfo> primaryKeys =
        serverTableMeta.getSchema().stream()
            .filter(s -> primaryKeyNames.contains(s.getField()))
            .collect(Collectors.toList());
    serverTableMeta.setPkList(primaryKeys);

    // partition
    List<AMSPartitionField> partitionFields =
        store.partitionType().getFields().stream()
            .map(f -> new AMSPartitionField(f.name(), null, null, f.id(), null))
            .collect(Collectors.toList());
    serverTableMeta.setPartitionColumnList(partitionFields);

    // properties
    serverTableMeta.setProperties(table.options());

    Map<String, Object> baseMetric = new HashMap<>();
    // table summary
    TableSummary tableSummary;
    if (table.comment().isPresent()) {
      serverTableMeta.setComment(table.comment().get());
    }
    Snapshot snapshot = store.snapshotManager().latestSnapshot();
    if (snapshot != null) {
      long fileCount = currentDataFileCount(store, snapshot);
      Long totalRecordCount = snapshot.totalRecordCount();
      long records = totalRecordCount == null ? 0L : totalRecordCount;

      tableSummary = new TableSummary(fileCount, null, null, records, "paimon");

      baseMetric.put("totalSize", null);
      baseMetric.put("fileCount", fileCount);
      baseMetric.put("averageFileSize", null);
      baseMetric.put("lastCommitTime", snapshot.timeMillis());
      Long watermark = snapshot.watermark();
      if (watermark != null && watermark > 0) {
        baseMetric.put("baseWatermark", watermark);
      }
    } else {
      tableSummary = new TableSummary(0, null, null, 0, "paimon");

      baseMetric.put("totalSize", null);
      baseMetric.put("fileCount", 0);
      baseMetric.put("averageFileSize", null);
    }
    serverTableMeta.setTableSummary(tableSummary);
    serverTableMeta.setBaseMetrics(baseMetric);

    return serverTableMeta;
  }

  private long currentDataFileCount(FileStore<?> store, Snapshot snapshot) {
    ManifestList manifestList = store.manifestListFactory().create();
    return manifestList.readDataManifests(snapshot).stream()
        .mapToLong(manifest -> manifest.numAddedFiles() - manifest.numDeletedFiles())
        .sum();
  }

  @Override
  public Pair<List<AmoroSnapshotsOfTable>, Long> getSnapshots(
      AmoroTable<?> amoroTable,
      String ref,
      OperationType operationType,
      int limit,
      int offset,
      String lastSnapshot) {
    return doAs(
        amoroTable,
        () -> getSnapshotsInternal(amoroTable, ref, operationType, limit, offset, lastSnapshot));
  }

  private Pair<List<AmoroSnapshotsOfTable>, Long> getSnapshotsInternal(
      AmoroTable<?> amoroTable,
      String ref,
      OperationType operationType,
      int limit,
      int offset,
      String lastSnapshot) {
    FileStoreTable table = getTable(amoroTable);
    long total = 0;
    if (table.branchManager().branchExists(ref) || BranchManager.isMainBranch(ref)) {
      SnapshotManager snapshotManager = table.snapshotManager().copyWithBranch(ref);
      // Guard empty table: a freshly created Paimon table has no snapshots and
      // latestSnapshotId() returns null. The previous unboxed subtraction NPE'd.
      Long latestId = snapshotManager.latestSnapshotId();
      if (latestId == null) {
        return Pair.of(Collections.emptyList(), 0L);
      }
      // Compute the correct total. The previous (latest - earliest) expression
      // was off-by-one (ignores the earliest id itself) and ignored operationType
      // entirely, so OPTIMIZING / NON_OPTIMIZING pagination totals were lifted
      // straight from the unfiltered id range. For the unfiltered path we use
      // the kernel's snapshotCount() which is an O(1) directory listing (see
      // SnapshotManager#snapshotIdStream — it only lists the snapshot dir, does
      // not parse any snapshot JSON). For filtered paths we must iterate and
      // apply the same predicate the paginator uses; no cheaper kernel shortcut
      // exists and correctness matters more than speed for the non-default view.
      if (operationType == null || operationType == OperationType.ALL) {
        try {
          total = snapshotManager.snapshotCount();
        } catch (IOException e) {
          throw new RuntimeException(
              "Failed to count snapshots for branch " + ref + " of " + amoroTable.id(), e);
        }
      } else {
        try {
          Iterator<Snapshot> it = snapshotManager.snapshots();
          long filtered = 0;
          while (it.hasNext()) {
            Snapshot s = it.next();
            if (matchesOperationType(s, operationType)) {
              filtered++;
            }
          }
          total = filtered;
        } catch (IOException e) {
          throw new RuntimeException(
              "Failed to iterate snapshots for branch " + ref + " of " + amoroTable.id(), e);
        }
      }
      // Parse lastSnapshot if provided
      Long lastSnapshotId =
          (lastSnapshot != null && !lastSnapshot.trim().isEmpty())
              ? Long.valueOf(lastSnapshot)
              : null;

      // Use the enhanced pagination method to get snapshots with filtering
      // The pagination method already handles operation type filtering internally
      Pair<List<Snapshot>, Integer> result =
          SnapShotsScanUtils.getSnapshotsWithPaginationForGeneral(
              snapshotManager, null, operationType, limit, offset, lastSnapshotId);
      List<Snapshot> snapshots = result.getLeft();

      // Convert to AmoroSnapshotsOfTable in parallel with improved error handling
      FileStore<?> store = table.store();
      List<CompletableFuture<AmoroSnapshotsOfTable>> futures = new ArrayList<>();

      for (Snapshot snapshot : snapshots) {
        futures.add(
            CompletableFuture.supplyAsync(
                () -> doAs(amoroTable, () -> getSnapshotsOfTable(store, snapshot)), executor));
      }

      // Collect results and maintain order with better error handling
      List<AmoroSnapshotsOfTable> snapshotsOfTables = new ArrayList<>();
      for (CompletableFuture<AmoroSnapshotsOfTable> future : futures) {
        try {
          AmoroSnapshotsOfTable resultSnapshot = future.get();
          if (resultSnapshot != null) {
            snapshotsOfTables.add(resultSnapshot);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Snapshot processing interrupted", e);
        } catch (ExecutionException e) {
          throw new RuntimeException("Failed to process snapshot", e.getCause());
        }
      }
      // The snapshots are already in correct order from the pagination method
      return Pair.of(snapshotsOfTables, total);
    } else {
      // Handle tag-based snapshots - simplified single snapshot processing
      Snapshot tagSnapshot = table.tagManager().getOrThrow(ref).trimToSnapshot();
      FileStore<?> store = table.store();
      AmoroSnapshotsOfTable snapshotOfTable = getSnapshotsOfTable(store, tagSnapshot);

      // Apply operation type filter for single snapshot
      if (operationType == OperationType.ALL) {
        return Pair.of(Collections.singletonList(snapshotOfTable), 1L);
      } else if (operationType == OperationType.OPTIMIZING) {
        return tagSnapshot.commitKind() == Snapshot.CommitKind.COMPACT
            ? Pair.of(Collections.singletonList(snapshotOfTable), 1L)
            : Pair.of(Collections.emptyList(), 0L);
      } else { // NON_OPTIMIZING
        return tagSnapshot.commitKind() != Snapshot.CommitKind.COMPACT
            ? Pair.of(Collections.singletonList(snapshotOfTable), 1L)
            : Pair.of(Collections.emptyList(), 0L);
      }
    }
  }

  /**
   * Mirrors the predicate used inside {@link SnapShotsScanUtils} so that the total count computed
   * here for filtered views stays consistent with the paginated result. {@code OPTIMIZING} maps to
   * COMPACT commits; {@code NON_OPTIMIZING} maps to everything else; {@code ALL} and {@code null}
   * match everything.
   */
  private static boolean matchesOperationType(Snapshot snapshot, OperationType operationType) {
    if (operationType == null || operationType == OperationType.ALL) {
      return true;
    }
    if (operationType == OperationType.OPTIMIZING) {
      return snapshot.commitKind() == Snapshot.CommitKind.COMPACT;
    }
    // NON_OPTIMIZING
    return snapshot.commitKind() != Snapshot.CommitKind.COMPACT;
  }

  @Override
  public List<PartitionFileBaseInfo> getSnapshotDetail(
      AmoroTable<?> amoroTable, String snapshotId, String ref) {
    return doAs(amoroTable, () -> getSnapshotDetailInternal(amoroTable, snapshotId, ref));
  }

  private List<PartitionFileBaseInfo> getSnapshotDetailInternal(
      AmoroTable<?> amoroTable, String snapshotId, String ref) {
    FileStoreTable table = getTable(amoroTable);
    List<PartitionFileBaseInfo> amsDataFileInfos = new ArrayList<>();
    long commitId = Long.parseLong(snapshotId);
    Snapshot snapshot;
    if (BranchManager.isMainBranch(ref) || table.branchManager().branchExists(ref)) {
      snapshot = table.snapshotManager().copyWithBranch(ref).snapshot(commitId);
    } else {
      snapshot = table.tagManager().getOrThrow(ref).trimToSnapshot();
    }

    FileStore<?> store = table.store();
    FileStorePathFactory fileStorePathFactory = store.pathFactory();
    ManifestList manifestList = store.manifestListFactory().create();
    ManifestFile manifestFile = store.manifestFileFactory().create();

    List<ManifestFileMeta> manifestFileMetas = manifestList.readDeltaManifests(snapshot);
    for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
      List<ManifestEntry> manifestEntries = manifestFile.read(manifestFileMeta.fileName());
      for (ManifestEntry entry : manifestEntries) {
        amsDataFileInfos.add(
            new PartitionFileBaseInfo(
                null,
                "BASE_FILE",
                getFileCreationTimeMillis(entry),
                partitionString(entry.partition(), entry.bucket(), fileStorePathFactory),
                fullFilePath(store, entry),
                entry.file().fileSize(),
                entry.kind().name()));
      }
    }

    return amsDataFileInfos;
  }

  @Override
  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) {
    return doAs(amoroTable, () -> getTableOperationsInternal(amoroTable));
  }

  private List<DDLInfo> getTableOperationsInternal(AmoroTable<?> amoroTable) {
    DataTable table = getTable(amoroTable);
    PaimonTableMetaExtract extract = new PaimonTableMetaExtract();
    DDLReverser<DataTable> ddlReverser = new DDLReverser<>(extract);
    return ddlReverser.reverse(table, amoroTable.id());
  }

  @Override
  public List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable) {
    return doAs(amoroTable, () -> getTablePartitionsInternal(amoroTable));
  }

  private List<PartitionBaseInfo> getTablePartitionsInternal(AmoroTable<?> amoroTable) {
    FileStoreTable table = getTable(amoroTable);
    FileStore<?> store = table.store();
    FileStorePathFactory fileStorePathFactory = store.pathFactory();
    List<ManifestEntry> files = store.newScan().plan().files(FileKind.ADD);
    Map<BinaryRow, Map<Integer, List<ManifestEntry>>> groupByPartFiles = groupByPartFiles(files);

    List<PartitionBaseInfo> partitionBaseInfoList = new ArrayList<>();
    for (Map.Entry<BinaryRow, Map<Integer, List<ManifestEntry>>> groupByPartitionEntry :
        groupByPartFiles.entrySet()) {
      for (Map.Entry<Integer, List<ManifestEntry>> groupByBucketEntry :
          groupByPartitionEntry.getValue().entrySet()) {
        String partitionSt =
            partitionString(
                groupByPartitionEntry.getKey(), groupByBucketEntry.getKey(), fileStorePathFactory);
        int fileCount = 0;
        long fileSize = 0;
        long lastCommitTime = 0;
        for (ManifestEntry manifestEntry : groupByBucketEntry.getValue()) {
          fileCount++;
          fileSize += manifestEntry.file().fileSize();
          lastCommitTime = Math.max(lastCommitTime, getFileCreationTimeMillis(manifestEntry));
        }
        partitionBaseInfoList.add(
            new PartitionBaseInfo(partitionSt, 0, fileCount, fileSize, lastCommitTime));
      }
    }
    return partitionBaseInfoList;
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFiles(
      AmoroTable<?> amoroTable, String partition, Integer specId) {
    return doAs(amoroTable, () -> getTableFilesInternal(amoroTable, partition, specId));
  }

  private List<PartitionFileBaseInfo> getTableFilesInternal(
      AmoroTable<?> amoroTable, String partition, Integer specId) {
    FileStoreTable table = getTable(amoroTable);
    FileStore<?> store = table.store();

    // Cache file add snapshot id
    Map<DataFileMeta, Long> fileSnapshotIdMap = new ConcurrentHashMap<>();
    ManifestList manifestList = store.manifestListFactory().create();
    ManifestFile manifestFile = store.manifestFileFactory().create();
    Iterator<Snapshot> snapshots;
    try {
      snapshots = store.snapshotManager().snapshots();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    List<CompletableFuture<Void>> futures = new ArrayList<>();
    while (snapshots.hasNext()) {
      Snapshot snapshot = snapshots.next();
      futures.add(
          CompletableFuture.runAsync(
              () ->
                  doAs(
                      amoroTable,
                      () -> {
                        List<ManifestFileMeta> deltaManifests =
                            manifestList.readDeltaManifests(snapshot);
                        for (ManifestFileMeta manifestFileMeta : deltaManifests) {
                          List<ManifestEntry> manifestEntries =
                              manifestFile.read(manifestFileMeta.fileName());
                          for (ManifestEntry manifestEntry : manifestEntries) {
                            fileSnapshotIdMap.put(manifestEntry.file(), snapshot.id());
                          }
                        }
                        return null;
                      }),
              executor));
    }

    try {
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
    } catch (InterruptedException e) {
      // ignore
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }

    FileStorePathFactory fileStorePathFactory = store.pathFactory();
    List<ManifestEntry> files = store.newScan().plan().files(FileKind.ADD);
    List<PartitionFileBaseInfo> partitionFileBases = new ArrayList<>();
    for (ManifestEntry manifestEntry : files) {
      String partitionSt =
          partitionString(manifestEntry.partition(), manifestEntry.bucket(), fileStorePathFactory);
      if (partition != null && !table.partitionKeys().isEmpty() && !partition.equals(partitionSt)) {
        continue;
      }
      Long snapshotId = fileSnapshotIdMap.get(manifestEntry.file());
      partitionFileBases.add(
          new PartitionFileBaseInfo(
              snapshotId == null ? null : snapshotId.toString(),
              "INSERT_FILE",
              getFileCreationTimeMillis(manifestEntry),
              partitionSt,
              0,
              fullFilePath(store, manifestEntry),
              manifestEntry.file().fileSize()));
    }

    return partitionFileBases;
  }

  /**
   * Returns a bounded paged view of the table's COMPACT-commit processes.
   *
   * <p>This is intentionally stateless: it reads one bounded snapshot-id window through Paimon's
   * {@link SnapshotManager#snapshotsWithinRange(Optional, Optional)} path, identifies COMPACT
   * snapshots from {@link Snapshot#commitKind()}, and then materialises only those compact
   * snapshots into process rows. The returned total is an upper-bound estimate, not a global exact
   * count; exact filtered totals would require scanning the full snapshot history.
   */
  @Override
  public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable,
      String type,
      ProcessStatus status,
      int limit,
      int offset,
      String lastSnapshot) {
    return doAs(
        amoroTable,
        () ->
            getOptimizingProcessesInfoInternal(
                amoroTable, type, status, limit, offset, lastSnapshot));
  }

  private Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfoInternal(
      AmoroTable<?> amoroTable,
      String type,
      ProcessStatus status,
      int limit,
      int offset,
      String lastSnapshot) {
    // Temporary solution for Paimon. TODO: Get compaction info from Paimon compaction task
    TableIdentifier tableIdentifier = amoroTable.id();
    FileStoreTable fileStoreTable = (FileStoreTable) amoroTable.originalTable();
    FileStore<?> store = fileStoreTable.store();
    boolean isPrimaryTable = !fileStoreTable.primaryKeys().isEmpty();
    int maxLevel = CoreOptions.fromMap(fileStoreTable.options()).numLevels() - 1;
    SnapshotManager snapshotManager = store.snapshotManager();

    // Empty-table guard: a freshly-created Paimon table has no snapshots and
    // latestSnapshotId() returns null. The old heuristic total — (latest -
    // earliest) / 2L * 0.6 — unboxed that null and threw NPE before any
    // result could be produced. Short-circuit here keeps the scan off the
    // critical path and returns an empty, stable container.
    if (snapshotManager.latestSnapshotId() == null) {
      return Pair.of(Collections.emptyList(), 0);
    }
    if (status != null && status != ProcessStatus.SUCCESS) {
      return Pair.of(Collections.emptyList(), 0);
    }

    ManifestFile manifestFile = store.manifestFileFactory().create();
    ManifestList manifestList = store.manifestListFactory().create();

    Long lastSnapshotId =
        (lastSnapshot != null && !lastSnapshot.trim().isEmpty())
            ? Long.valueOf(lastSnapshot)
            : null;
    Pair<List<Snapshot>, Integer> compactSnapshots =
        SnapShotsScanUtils.getSnapshotsWithBoundedPagination(
            snapshotManager,
            s -> s.commitKind() == Snapshot.CommitKind.COMPACT,
            limit,
            offset,
            lastSnapshotId);

    List<OptimizingProcessInfo> processes =
        compactSnapshots.getLeft().stream()
            .map(
                s ->
                    toOptimizingProcessInfo(
                        s, tableIdentifier, manifestFile, manifestList, isPrimaryTable, maxLevel))
            .collect(Collectors.toList());

    List<OptimizingProcessInfo> page =
        processes.stream()
            .filter(p -> StringUtils.isBlank(type) || type.equalsIgnoreCase(p.getOptimizingType()))
            .filter(p -> status == null || status == p.getStatus())
            .collect(Collectors.toList());

    int total =
        page.isEmpty()
            ? compactSnapshots.getRight()
            : Math.max(compactSnapshots.getRight(), Math.max(offset, 0) + page.size());

    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "getOptimizingProcessesInfo for {}: bounded compact candidates {}, estimated total {}, type={}, status={}, offset={}, limit={}, returned {}",
          tableIdentifier,
          compactSnapshots.getLeft().size(),
          total,
          type,
          status,
          offset,
          limit,
          page.size());
    }

    return Pair.of(page, total);
  }

  /**
   * Builds a single {@link OptimizingProcessInfo} from a COMPACT snapshot by walking its delta
   * manifests. Extracted so bounded pagination can reuse the same per-snapshot materialisation
   * logic after selecting compact candidates.
   */
  private OptimizingProcessInfo toOptimizingProcessInfo(
      Snapshot s,
      TableIdentifier tableIdentifier,
      ManifestFile manifestFile,
      ManifestList manifestList,
      boolean isPrimaryTable,
      int maxLevel) {
    OptimizingProcessInfo optimizingProcessInfo = new OptimizingProcessInfo();
    optimizingProcessInfo.setProcessId(String.valueOf(s.id()));
    optimizingProcessInfo.setCatalogName(tableIdentifier.getCatalog());
    optimizingProcessInfo.setDbName(tableIdentifier.getDatabase());
    optimizingProcessInfo.setTableName(tableIdentifier.getTableName());
    optimizingProcessInfo.setStatus(ProcessStatus.SUCCESS);
    optimizingProcessInfo.setFinishTime(s.timeMillis());
    FilesStatisticsBuilder inputBuilder = new FilesStatisticsBuilder();
    FilesStatisticsBuilder outputBuilder = new FilesStatisticsBuilder();
    List<ManifestFileMeta> manifestFileMetas = manifestList.readDeltaManifests(s);
    boolean hasMaxLevels = false;
    long minCreateTime = Long.MAX_VALUE;
    long maxCreateTime = Long.MIN_VALUE;
    Set<Integer> buckets = new HashSet<>();
    HashMap<String, String> summary = new HashMap<>(8);
    Long totalAddRowCount = 0L;
    Long deleteAddRowCount = 0L;
    for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
      List<ManifestEntry> compactManifestEntries = manifestFile.read(manifestFileMeta.fileName());
      for (ManifestEntry compactManifestEntry : compactManifestEntries) {
        long addRowCount = compactManifestEntry.file().rowCount();
        totalAddRowCount += addRowCount;
        Optional<Long> deleteRowCount = compactManifestEntry.file().deleteRowCount();
        if (deleteRowCount.isPresent()) {
          deleteAddRowCount += deleteRowCount.get();
        }
        if (compactManifestEntry.file().level() == maxLevel) {
          hasMaxLevels = true;
        }
        buckets.add(compactManifestEntry.bucket());
        if (compactManifestEntry.kind() == FileKind.DELETE) {
          inputBuilder.addFile(compactManifestEntry.file().fileSize());
        } else {
          minCreateTime = Math.min(minCreateTime, getFileCreationTimeMillis(compactManifestEntry));
          maxCreateTime = Math.max(maxCreateTime, getFileCreationTimeMillis(compactManifestEntry));
          outputBuilder.addFile(compactManifestEntry.file().fileSize());
        }
      }
    }
    if (isPrimaryTable && hasMaxLevels) {
      optimizingProcessInfo.setOptimizingType(PAIMON_OPTIMIZING_TYPE_FULL);
    } else {
      optimizingProcessInfo.setOptimizingType(PAIMON_OPTIMIZING_TYPE_MINOR);
    }
    optimizingProcessInfo.setSuccessTasks(buckets.size());
    optimizingProcessInfo.setTotalTasks(buckets.size());
    optimizingProcessInfo.setStartTime(minCreateTime);
    optimizingProcessInfo.setDuration(s.timeMillis() - minCreateTime);
    optimizingProcessInfo.setInputFiles(inputBuilder.build());
    optimizingProcessInfo.setOutputFiles(outputBuilder.build());
    summary.put(
        "input-data-files(rewrite)",
        String.valueOf(optimizingProcessInfo.getInputFiles().getFileCnt()));
    summary.put(
        "input-data-size(rewrite)",
        String.valueOf(optimizingProcessInfo.getInputFiles().getTotalSize()));
    summary.put("input-data-records(rewrite)", String.valueOf(totalAddRowCount));
    summary.put(
        "output-data-files", String.valueOf(optimizingProcessInfo.getOutputFiles().getFileCnt()));
    summary.put(
        "output-data-size", String.valueOf(optimizingProcessInfo.getOutputFiles().getTotalSize()));
    summary.put("output-data-records", String.valueOf(deleteAddRowCount));
    optimizingProcessInfo.setSummary(summary);
    return optimizingProcessInfo;
  }

  @Override
  public Map<String, String> getTableOptimizingTypes(AmoroTable<?> amoroTable) {
    Map<String, String> types = Maps.newHashMap();
    types.put("FULL", "FULL");
    types.put("MINOR", "MINOR");
    types.put("INTERNAL", "INTERNAL");
    return types;
  }

  @Override
  public List<OptimizingTaskInfo> getOptimizingTaskInfos(
      AmoroTable<?> amoroTable, String processId) {
    return doAs(amoroTable, () -> getOptimizingTaskInfosInternal(amoroTable, processId));
  }

  private List<OptimizingTaskInfo> getOptimizingTaskInfosInternal(
      AmoroTable<?> amoroTable, String processId) {
    long id = Long.parseLong(processId);
    FileStoreTable table = getTable(amoroTable);
    FileStore<?> store = table.store();
    Snapshot snapshot = null;
    try {
      SnapshotManager snapshotManager = store.snapshotManager();
      ManifestList manifestList = store.manifestListFactory().create();
      ManifestFile manifestFile = store.manifestFileFactory().create();
      snapshot = snapshotManager.tryGetSnapshot(id);
      Snapshot.CommitKind commitKind = snapshot.commitKind();
      if (commitKind == Snapshot.CommitKind.COMPACT) {
        List<ManifestFileMeta> manifestFileMetas = manifestList.readDeltaManifests(snapshot);

        // Group manifest entries by bucket for per-bucket statistics
        Map<Integer, BucketStatistics> bucketStatsMap = new HashMap<>();

        for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
          List<ManifestEntry> compactManifestEntries =
              manifestFile.read(manifestFileMeta.fileName());
          for (ManifestEntry compactManifestEntry : compactManifestEntries) {
            int bucket = compactManifestEntry.bucket();
            BucketStatistics bucketStats =
                bucketStatsMap.computeIfAbsent(bucket, k -> new BucketStatistics());
            if (compactManifestEntry.kind() == FileKind.DELETE) {
              bucketStats.deletedFiles++;
              bucketStats.inputSize += compactManifestEntry.file().fileSize();
              bucketStats.removedFilePaths.add(fullFilePath(store, compactManifestEntry));
            } else {
              bucketStats.outputSize += compactManifestEntry.file().fileSize();
              bucketStats.addedFilePaths.add(fullFilePath(store, compactManifestEntry));
            }
          }
        }

        // Convert bucket statistics to OptimizingTaskInfo objects
        List<OptimizingTaskInfo> results = new ArrayList<>();
        int taskId = 1;
        for (Map.Entry<Integer, BucketStatistics> entry : bucketStatsMap.entrySet()) {
          Integer bucket = entry.getKey();
          BucketStatistics stats = entry.getValue();

          // Create FilesStatistics for input and output
          FilesStatistics inputFiles =
              FilesStatistics.builder().addFiles(stats.inputSize, stats.deletedFiles).build();

          FilesStatistics outputFiles =
              FilesStatistics.builder()
                  .addFiles(stats.outputSize, stats.addedFilePaths.size())
                  .build();

          // Create summary with bucket information
          Map<String, String> summary = new HashMap<>();
          summary.put("bucket", String.valueOf(bucket));
          summary.put("deleted-files-count", String.valueOf(stats.deletedFiles));
          summary.put("added-files-count", String.valueOf(stats.addedFilePaths.size()));
          summary.put("removed-files", String.join(",", stats.removedFilePaths));
          summary.put("added-files", String.join(",", stats.addedFilePaths));

          // Create OptimizingTaskInfo
          OptimizingTaskInfo taskInfo =
              new OptimizingTaskInfo(
                  null, // tableId
                  processId, // processId
                  taskId++, // taskId
                  "bucket-" + bucket, // partitionData
                  ProcessTaskStatus.SUCCESS, // status
                  0, // retryNum
                  "paimon-internal-optimizer", // optimizerToken
                  0, // threadId
                  snapshot.timeMillis(), // startTime
                  snapshot.timeMillis(), // endTime
                  0, // costTime (calculated automatically)
                  null, // failReason
                  inputFiles, // inputFiles
                  outputFiles, // outputFiles
                  summary, // summary
                  new HashMap<>() // properties
                  );

          results.add(taskInfo);
        }
        return results;
      }
      return Collections.emptyList();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Helper class to collect bucket-level statistics
  private static class BucketStatistics {
    int deletedFiles = 0;
    long inputSize = 0;
    long outputSize = 0;
    List<String> removedFilePaths = new ArrayList<>();
    List<String> addedFilePaths = new ArrayList<>();

    public BucketStatistics() {}
  }

  @NotNull
  private AmoroSnapshotsOfTable getSnapshotsOfTable(FileStore<?> store, Snapshot snapshot) {
    Map<String, String> summary = new HashMap<>();
    summary.put("commitUser", snapshot.commitUser());
    summary.put("commitIdentifier", String.valueOf(snapshot.commitIdentifier()));
    if (snapshot.watermark() != null) {
      summary.put("watermark", String.valueOf(snapshot.watermark()));
    }

    // record number
    summary.put("total-records", String.valueOf(snapshot.totalRecordCount()));
    summary.put("delta-records", String.valueOf(snapshot.deltaRecordCount()));
    if (snapshot.changelogRecordCount() != null) {
      summary.put("changelog-records", String.valueOf(snapshot.changelogRecordCount()));
    }

    // file number
    AmoroSnapshotsOfTable deltaSnapshotsOfTable =
        manifestListInfo(store, snapshot, ManifestList::readDeltaManifests);
    int deltaFileCount = deltaSnapshotsOfTable.getFileCount();
    int dataFileCount =
        manifestListInfo(store, snapshot, ManifestList::readDataManifests).getFileCount();
    long changeLogFileCount =
        manifestListInfo(store, snapshot, ManifestList::readChangelogManifests).getFileCount();
    summary.put("delta-files", String.valueOf(deltaFileCount));
    summary.put("data-files", String.valueOf(dataFileCount));
    summary.put("changelogs", String.valueOf(changeLogFileCount));

    // Summary in chart
    Map<String, String> recordsSummaryForChat =
        extractSummary(summary, "total-records", "delta-records", "changelog-records");
    deltaSnapshotsOfTable.setRecordsSummaryForChart(recordsSummaryForChat);

    Map<String, String> filesSummaryForChat =
        extractSummary(summary, "delta-files", "data-files", "changelogs");
    deltaSnapshotsOfTable.setFilesSummaryForChart(filesSummaryForChat);

    deltaSnapshotsOfTable.setSummary(summary);
    return deltaSnapshotsOfTable;
  }

  @NotNull
  private static Map<String, String> extractSummary(Map<String, String> summary, String... keys) {
    Set<String> keySet = new HashSet<>(Arrays.asList(keys));
    return summary.entrySet().stream()
        .filter(e -> keySet.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public List<TagOrBranchInfo> getTableTags(AmoroTable<?> amoroTable) {
    return doAs(amoroTable, () -> getTableTagsInternal(amoroTable));
  }

  private List<TagOrBranchInfo> getTableTagsInternal(AmoroTable<?> amoroTable) {
    FileStoreTable table = getTable(amoroTable);
    SortedMap<Snapshot, List<String>> tags = table.tagManager().tags();
    List<TagOrBranchInfo> tagOrBranchInfos = new ArrayList<>();
    tags.forEach(
        (snapshot, tagList) -> {
          for (String tagName : tagList) {
            tagOrBranchInfos.add(
                new TagOrBranchInfo(tagName, snapshot.id(), 0, 0L, 0L, TagOrBranchInfo.TAG));
          }
        });
    return tagOrBranchInfos;
  }

  @Override
  public List<TagOrBranchInfo> getTableBranches(AmoroTable<?> amoroTable) {
    return doAs(amoroTable, () -> getTableBranchesInternal(amoroTable));
  }

  private List<TagOrBranchInfo> getTableBranchesInternal(AmoroTable<?> amoroTable) {
    FileStoreTable table = getTable(amoroTable);
    List<String> branches = table.branchManager().branches();
    List<TagOrBranchInfo> branchInfos =
        branches.stream()
            .map(name -> new TagOrBranchInfo(name, -1, -1, 0L, 0L, TagOrBranchInfo.BRANCH))
            .collect(Collectors.toList());
    branchInfos.add(TagOrBranchInfo.MAIN_BRANCH);
    return branchInfos;
  }

  @Override
  public List<ConsumerInfo> getTableConsumerInfos(AmoroTable<?> amoroTable) {
    return doAs(amoroTable, () -> getTableConsumerInfosInternal(amoroTable));
  }

  private List<ConsumerInfo> getTableConsumerInfosInternal(AmoroTable<?> amoroTable) {
    FileStoreTable table = getTable(amoroTable);
    FileStore<?> store = table.store();
    ConsumerManager consumerManager = new ConsumerManager(table.fileIO(), table.location());
    List<ConsumerInfo> consumerInfos = new ArrayList<>();
    try {
      consumerManager
          .consumers()
          .forEach(
              (consumerId, nextSnapshotId) -> {
                long currentSnapshotId = nextSnapshotId;
                if (!table.snapshotManager().snapshotExists(currentSnapshotId)) {
                  // if not exits,maybe steaming scan is running,so need to nextSnapshotId -1
                  currentSnapshotId = nextSnapshotId - 1;
                }
                Snapshot snapshot = table.snapshotManager().snapshot(currentSnapshotId);
                AmoroSnapshotsOfTable amoroSnapshotsOfTable = getSnapshotsOfTable(store, snapshot);
                consumerInfos.add(
                    new ConsumerInfo(consumerId, nextSnapshotId, amoroSnapshotsOfTable));
              });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return consumerInfos;
  }

  private AmoroSnapshotsOfTable manifestListInfo(
      FileStore<?> store,
      Snapshot snapshot,
      BiFunction<ManifestList, Snapshot, List<ManifestFileMeta>> biFunction) {
    ManifestList manifestList = store.manifestListFactory().create();
    ManifestFile manifestFile = store.manifestFileFactory().create();
    List<ManifestFileMeta> manifestFileMetas = biFunction.apply(manifestList, snapshot);
    int fileCount = 0;
    long fileSize = 0;
    for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
      List<ManifestEntry> manifestEntries = manifestFile.read(manifestFileMeta.fileName());
      for (ManifestEntry entry : manifestEntries) {
        if (entry.kind() == FileKind.ADD) {
          fileSize += entry.file().fileSize();
          fileCount++;
        } else {
          fileSize -= entry.file().fileSize();
          fileCount--;
        }
      }
    }
    Long totalRecordCount = snapshot.totalRecordCount();
    return new AmoroSnapshotsOfTable(
        String.valueOf(snapshot.id()),
        fileCount,
        fileSize,
        totalRecordCount == null ? 0L : totalRecordCount,
        snapshot.timeMillis(),
        snapshot.commitKind().toString(),
        snapshot.commitKind() == Snapshot.CommitKind.COMPACT
            ? CommitMetaProducer.OPTIMIZE.name()
            : CommitMetaProducer.INGESTION.name(),
        new HashMap<>());
  }

  private String partitionString(
      BinaryRow partition, Integer bucket, FileStorePathFactory fileStorePathFactory) {
    String partitionString = fileStorePathFactory.getPartitionString(partition);
    return partitionString + "/bucket-" + bucket;
  }

  private String fullFilePath(FileStore<?> store, ManifestEntry manifestEntry) {
    return store
        .pathFactory()
        .createDataFilePathFactory(manifestEntry.partition(), manifestEntry.bucket())
        .toPath(manifestEntry.file())
        .toString();
  }

  private FileStoreTable getTable(AmoroTable<?> amoroTable) {
    return (FileStoreTable) amoroTable.originalTable();
  }

  private <T> T doAs(AmoroTable<?> amoroTable, Callable<T> callable) {
    if (amoroTable instanceof PaimonTable) {
      return ((PaimonTable) amoroTable).doAs(callable);
    }
    return call(callable);
  }

  private <T> T call(Callable<T> callable) {
    try {
      return callable.call();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(
          "Run with Paimon table descriptor authentication context failed.", e);
    }
  }

  private long getFileCreationTimeMillis(ManifestEntry manifestEntry) {
    // DataFileMeta.creationTimeEpochMillis() may mislead user about the creation time
    // See: https://github.com/apache/paimon/issues/7151
    return manifestEntry.file().creationTime().getMillisecond();
  }
}

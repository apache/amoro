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
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Streams;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.descriptor.AMSColumnInfo;
import org.apache.amoro.table.descriptor.AMSPartitionField;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.ConsumerInfo;
import org.apache.amoro.table.descriptor.DDLInfo;
import org.apache.amoro.table.descriptor.DDLReverser;
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
import org.apache.amoro.utils.CommonUtil;
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
import org.apache.paimon.utils.FileStorePathFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Descriptor for Paimon format tables. */
public class PaimonTableDescriptor implements FormatTableDescriptor {

  public static final String PAIMON_MAIN_BRANCH_NAME = "main";

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
    Snapshot snapshot = store.snapshotManager().latestSnapshot();
    if (snapshot != null) {
      AmoroSnapshotsOfTable snapshotsOfTable =
          manifestListInfo(store, snapshot, (m, s) -> s.dataManifests(m));
      long fileSize = snapshotsOfTable.getOriginalFileSize();
      String totalSize = CommonUtil.byteToXB(fileSize);
      int fileCount = snapshotsOfTable.getFileCount();

      String averageFileSize = CommonUtil.byteToXB(fileCount == 0 ? 0 : fileSize / fileCount);

      tableSummary =
          new TableSummary(
              fileCount, totalSize, averageFileSize, snapshotsOfTable.getRecords(), "paimon");

      baseMetric.put("totalSize", totalSize);
      baseMetric.put("fileCount", fileCount);
      baseMetric.put("averageFileSize", averageFileSize);
      baseMetric.put("lastCommitTime", snapshot.timeMillis());
      Long watermark = snapshot.watermark();
      if (watermark != null && watermark > 0) {
        baseMetric.put("baseWatermark", watermark);
      }
    } else {
      tableSummary = new TableSummary(0, "0", "0", 0, "paimon");

      baseMetric.put("totalSize", 0);
      baseMetric.put("fileCount", 0);
      baseMetric.put("averageFileSize", 0);
    }
    serverTableMeta.setTableSummary(tableSummary);
    serverTableMeta.setBaseMetrics(baseMetric);

    return serverTableMeta;
  }

  @Override
  public List<AmoroSnapshotsOfTable> getSnapshots(
      AmoroTable<?> amoroTable, String ref, OperationType operationType) {
    FileStoreTable table = getTable(amoroTable);
    List<AmoroSnapshotsOfTable> snapshotsOfTables = new ArrayList<>();
    Iterator<Snapshot> snapshots;
    if (PAIMON_MAIN_BRANCH_NAME.equals(ref)) {
      try {
        snapshots = table.snapshotManager().snapshots();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      snapshots = Collections.singleton(table.tagManager().taggedSnapshot(ref)).iterator();
    }

    FileStore<?> store = table.store();
    List<CompletableFuture<AmoroSnapshotsOfTable>> futures = new ArrayList<>();
    Predicate<Snapshot> predicate =
        operationType == OperationType.ALL
            ? s -> true
            : operationType == OperationType.OPTIMIZING
                ? s -> s.commitKind() == Snapshot.CommitKind.COMPACT
                : s -> s.commitKind() != Snapshot.CommitKind.COMPACT;
    while (snapshots.hasNext()) {
      Snapshot snapshot = snapshots.next();
      if (!predicate.test(snapshot)) {
        continue;
      }
      futures.add(
          CompletableFuture.supplyAsync(() -> getSnapshotsOfTable(store, snapshot), executor));
    }
    for (CompletableFuture<AmoroSnapshotsOfTable> completableFuture : futures) {
      try {
        snapshotsOfTables.add(completableFuture.get());
      } catch (InterruptedException e) {
        // ignore
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
    return snapshotsOfTables.stream()
        .sorted((o1, o2) -> Long.compare(o2.getCommitTime(), o1.getCommitTime()))
        .collect(Collectors.toList());
  }

  @Override
  public List<PartitionFileBaseInfo> getSnapshotDetail(
      AmoroTable<?> amoroTable, String snapshotId) {
    FileStoreTable table = getTable(amoroTable);
    List<PartitionFileBaseInfo> amsDataFileInfos = new ArrayList<>();
    long commitId = Long.parseLong(snapshotId);
    Snapshot snapshot = table.snapshotManager().snapshot(commitId);
    FileStore<?> store = table.store();
    FileStorePathFactory fileStorePathFactory = store.pathFactory();
    ManifestList manifestList = store.manifestListFactory().create();
    ManifestFile manifestFile = store.manifestFileFactory().create();

    List<ManifestFileMeta> manifestFileMetas = snapshot.deltaManifests(manifestList);
    for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
      manifestFileMeta.fileSize();
      List<ManifestEntry> manifestEntries = manifestFile.read(manifestFileMeta.fileName());
      for (ManifestEntry entry : manifestEntries) {
        amsDataFileInfos.add(
            new PartitionFileBaseInfo(
                null,
                "BASE_FILE",
                entry.file().creationTimeEpochMillis(),
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
    DataTable table = getTable(amoroTable);
    PaimonTableMetaExtract extract = new PaimonTableMetaExtract();
    DDLReverser<DataTable> ddlReverser = new DDLReverser<>(extract);
    return ddlReverser.reverse(table, amoroTable.id());
  }

  @Override
  public List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable) {
    FileStoreTable table = getTable(amoroTable);
    FileStore<?> store = table.store();
    FileStorePathFactory fileStorePathFactory = store.pathFactory();
    List<ManifestEntry> files = store.newScan().plan().files(FileKind.ADD);
    Map<BinaryRow, Map<Integer, List<DataFileMeta>>> groupByPartFiles = groupByPartFiles(files);

    List<PartitionBaseInfo> partitionBaseInfoList = new ArrayList<>();
    for (Map.Entry<BinaryRow, Map<Integer, List<DataFileMeta>>> groupByPartitionEntry :
        groupByPartFiles.entrySet()) {
      for (Map.Entry<Integer, List<DataFileMeta>> groupByBucketEntry :
          groupByPartitionEntry.getValue().entrySet()) {
        String partitionSt =
            partitionString(
                groupByPartitionEntry.getKey(), groupByBucketEntry.getKey(), fileStorePathFactory);
        int fileCount = 0;
        long fileSize = 0;
        long lastCommitTime = 0;
        for (DataFileMeta dataFileMeta : groupByBucketEntry.getValue()) {
          fileCount++;
          fileSize += dataFileMeta.fileSize();
          lastCommitTime = Math.max(lastCommitTime, dataFileMeta.creationTimeEpochMillis());
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
              () -> {
                List<ManifestFileMeta> deltaManifests = snapshot.deltaManifests(manifestList);
                for (ManifestFileMeta manifestFileMeta : deltaManifests) {
                  List<ManifestEntry> manifestEntries =
                      manifestFile.read(manifestFileMeta.fileName());
                  for (ManifestEntry manifestEntry : manifestEntries) {
                    fileSnapshotIdMap.put(manifestEntry.file(), snapshot.id());
                  }
                }
              },
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
              manifestEntry.file().creationTimeEpochMillis(),
              partitionSt,
              0,
              fullFilePath(store, manifestEntry),
              manifestEntry.file().fileSize()));
    }

    return partitionFileBases;
  }

  @Override
  public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable, int limit, int offset) {
    // Temporary solution for Paimon. TODO: Get compaction info from Paimon compaction task
    List<OptimizingProcessInfo> processInfoList = new ArrayList<>();
    TableIdentifier tableIdentifier = amoroTable.id();
    FileStoreTable fileStoreTable = (FileStoreTable) amoroTable.originalTable();
    FileStore<?> store = fileStoreTable.store();
    boolean isPrimaryTable = !fileStoreTable.primaryKeys().isEmpty();
    int maxLevel = CoreOptions.fromMap(fileStoreTable.options()).numLevels() - 1;
    int total;
    try {
      List<Snapshot> compactSnapshots =
          Streams.stream(store.snapshotManager().snapshots())
              .filter(s -> s.commitKind() == Snapshot.CommitKind.COMPACT)
              .collect(Collectors.toList());
      total = compactSnapshots.size();
      processInfoList =
          compactSnapshots.stream()
              .sorted(Comparator.comparing(Snapshot::id).reversed())
              .skip(offset)
              .limit(limit)
              .map(
                  s -> {
                    OptimizingProcessInfo optimizingProcessInfo = new OptimizingProcessInfo();
                    optimizingProcessInfo.setProcessId(String.valueOf(s.id()));
                    optimizingProcessInfo.setCatalogName(tableIdentifier.getCatalog());
                    optimizingProcessInfo.setDbName(tableIdentifier.getDatabase());
                    optimizingProcessInfo.setTableName(tableIdentifier.getTableName());
                    optimizingProcessInfo.setStatus(ProcessStatus.SUCCESS);
                    optimizingProcessInfo.setFinishTime(s.timeMillis());
                    FilesStatisticsBuilder inputBuilder = new FilesStatisticsBuilder();
                    FilesStatisticsBuilder outputBuilder = new FilesStatisticsBuilder();
                    ManifestFile manifestFile = store.manifestFileFactory().create();
                    ManifestList manifestList = store.manifestListFactory().create();
                    List<ManifestFileMeta> manifestFileMetas = s.deltaManifests(manifestList);
                    boolean hasMaxLevels = false;
                    long minCreateTime = Long.MAX_VALUE;
                    long maxCreateTime = Long.MIN_VALUE;
                    Set<Integer> buckets = new HashSet<>();
                    for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
                      List<ManifestEntry> compactManifestEntries =
                          manifestFile.read(manifestFileMeta.fileName());
                      for (ManifestEntry compactManifestEntry : compactManifestEntries) {
                        if (compactManifestEntry.file().level() == maxLevel) {
                          hasMaxLevels = true;
                        }
                        buckets.add(compactManifestEntry.bucket());
                        if (compactManifestEntry.kind() == FileKind.DELETE) {
                          inputBuilder.addFile(compactManifestEntry.file().fileSize());
                        } else {
                          minCreateTime =
                              Math.min(
                                  minCreateTime,
                                  compactManifestEntry.file().creationTimeEpochMillis());
                          maxCreateTime =
                              Math.max(
                                  maxCreateTime,
                                  compactManifestEntry.file().creationTimeEpochMillis());
                          outputBuilder.addFile(compactManifestEntry.file().fileSize());
                        }
                      }
                    }
                    if (isPrimaryTable && hasMaxLevels) {
                      optimizingProcessInfo.setOptimizingType("FULL");
                    } else {
                      optimizingProcessInfo.setOptimizingType("MINOR");
                    }
                    optimizingProcessInfo.setSuccessTasks(buckets.size());
                    optimizingProcessInfo.setTotalTasks(buckets.size());
                    optimizingProcessInfo.setStartTime(minCreateTime);
                    optimizingProcessInfo.setDuration(s.timeMillis() - minCreateTime);
                    optimizingProcessInfo.setInputFiles(inputBuilder.build());
                    optimizingProcessInfo.setOutputFiles(outputBuilder.build());
                    optimizingProcessInfo.setSummary(Collections.emptyMap());
                    return optimizingProcessInfo;
                  })
              .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Pair.of(processInfoList, total);
  }

  @Override
  public List<OptimizingTaskInfo> getOptimizingTaskInfos(
      AmoroTable<?> amoroTable, String processId) {
    throw new UnsupportedOperationException();
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
    if (snapshot.totalRecordCount() != null) {
      summary.put("total-records", String.valueOf(snapshot.totalRecordCount()));
    }
    if (snapshot.deltaRecordCount() != null) {
      summary.put("delta-records", String.valueOf(snapshot.deltaRecordCount()));
    }
    if (snapshot.changelogRecordCount() != null) {
      summary.put("changelog-records", String.valueOf(snapshot.changelogRecordCount()));
    }

    // file number
    AmoroSnapshotsOfTable deltaSnapshotsOfTable =
        manifestListInfo(store, snapshot, (m, s) -> s.deltaManifests(m));
    int deltaFileCount = deltaSnapshotsOfTable.getFileCount();
    int dataFileCount =
        manifestListInfo(store, snapshot, (m, s) -> s.dataManifests(m)).getFileCount();
    int changeLogFileCount =
        manifestListInfo(store, snapshot, (m, s) -> s.changelogManifests(m)).getFileCount();
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
    return ImmutableList.of(TagOrBranchInfo.MAIN_BRANCH);
  }

  @Override
  public List<ConsumerInfo> getTableConsumerInfos(AmoroTable<?> amoroTable) {
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
        .toPath(manifestEntry.file().fileName())
        .toString();
  }

  private FileStoreTable getTable(AmoroTable<?> amoroTable) {
    return (FileStoreTable) amoroTable.originalTable();
  }
}

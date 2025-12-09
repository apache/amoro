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

package org.apache.amoro.server.dashboard;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.FileNameRules;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.ProcessTaskStatus;
import org.apache.amoro.server.dashboard.component.reverser.IcebergTableMetaExtract;
import org.apache.amoro.server.dashboard.model.TableBasicInfo;
import org.apache.amoro.server.dashboard.model.TableStatistics;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.dashboard.utils.TableStatCollector;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.OptimizingTaskMeta;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.table.descriptor.AMSColumnInfo;
import org.apache.amoro.table.descriptor.AMSPartitionField;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.ConsumerInfo;
import org.apache.amoro.table.descriptor.DDLInfo;
import org.apache.amoro.table.descriptor.DDLReverser;
import org.apache.amoro.table.descriptor.FilesStatistics;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.OperationType;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.amoro.table.descriptor.OptimizingTaskInfo;
import org.apache.amoro.table.descriptor.PartitionBaseInfo;
import org.apache.amoro.table.descriptor.PartitionFileBaseInfo;
import org.apache.amoro.table.descriptor.ServerTableMeta;
import org.apache.amoro.table.descriptor.TableSummary;
import org.apache.amoro.table.descriptor.TagOrBranchInfo;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.IcebergFindFiles;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.MetadataTableUtils;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.SnapshotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Descriptor for Mixed-Hive, Mixed-Iceberg, Iceberg format tables. */
public class MixedAndIcebergTableDescriptor extends PersistentBase
    implements FormatTableDescriptor {

  private static final Logger LOG = LoggerFactory.getLogger(MixedAndIcebergTableDescriptor.class);

  private ExecutorService executorService;

  @Override
  public void withIoExecutor(ExecutorService ioExecutor) {
    this.executorService = ioExecutor;
  }

  @Override
  public List<TableFormat> supportFormat() {
    return Arrays.asList(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE);
  }

  @Override
  public ServerTableMeta getTableDetail(AmoroTable<?> amoroTable) {
    MixedTable table = getTable(amoroTable);
    String tableFormat = decorateTableFormat(amoroTable);
    // set basic info
    TableBasicInfo tableBasicInfo = getTableBasicInfo(table);
    ServerTableMeta serverTableMeta = getServerTableMeta(table);
    long tableSize = 0;
    long tableFileCnt = 0;
    Map<String, Object> baseMetrics = Maps.newHashMap();
    FilesStatistics baseFilesStatistics = tableBasicInfo.getBaseStatistics().getTotalFilesStat();
    Map<String, String> baseSummary = tableBasicInfo.getBaseStatistics().getSummary();
    baseMetrics.put("lastCommitTime", AmsUtil.longOrNull(baseSummary.get("visibleTime")));
    baseMetrics.put("totalSize", AmsUtil.byteToXB(baseFilesStatistics.getTotalSize()));
    baseMetrics.put("fileCount", baseFilesStatistics.getFileCnt());
    baseMetrics.put("averageFileSize", AmsUtil.byteToXB(baseFilesStatistics.getAverageSize()));
    if (tableBasicInfo.getChangeStatistics() == null) {
      baseMetrics.put("baseWatermark", AmsUtil.longOrNull(serverTableMeta.getTableWatermark()));
    } else {
      baseMetrics.put("baseWatermark", AmsUtil.longOrNull(serverTableMeta.getBaseWatermark()));
    }
    tableSize += baseFilesStatistics.getTotalSize();
    tableFileCnt += baseFilesStatistics.getFileCnt();
    serverTableMeta.setBaseMetrics(baseMetrics);

    if (table.isKeyedTable()) {
      Map<String, Object> changeMetrics = Maps.newHashMap();
      if (tableBasicInfo.getChangeStatistics() != null) {
        FilesStatistics changeFilesStatistics =
            tableBasicInfo.getChangeStatistics().getTotalFilesStat();
        Map<String, String> changeSummary = tableBasicInfo.getChangeStatistics().getSummary();
        changeMetrics.put("lastCommitTime", AmsUtil.longOrNull(changeSummary.get("visibleTime")));
        changeMetrics.put("totalSize", AmsUtil.byteToXB(changeFilesStatistics.getTotalSize()));
        changeMetrics.put("fileCount", changeFilesStatistics.getFileCnt());
        changeMetrics.put(
            "averageFileSize", AmsUtil.byteToXB(changeFilesStatistics.getAverageSize()));
        changeMetrics.put(
            "tableWatermark", AmsUtil.longOrNull(serverTableMeta.getTableWatermark()));
        tableSize += changeFilesStatistics.getTotalSize();
        tableFileCnt += changeFilesStatistics.getFileCnt();
      } else {
        changeMetrics.put("lastCommitTime", null);
        changeMetrics.put("totalSize", null);
        changeMetrics.put("fileCount", null);
        changeMetrics.put("averageFileSize", null);
        changeMetrics.put("tableWatermark", null);
      }
      serverTableMeta.setChangeMetrics(changeMetrics);
    }
    String averageFileSize = AmsUtil.byteToXB(tableFileCnt == 0 ? 0 : tableSize / tableFileCnt);
    long records = getRecordsOfTable(table);
    TableSummary tableSummary =
        new TableSummary(
            tableFileCnt, AmsUtil.byteToXB(tableSize), averageFileSize, records, tableFormat);
    serverTableMeta.setTableSummary(tableSummary);
    return serverTableMeta;
  }

  private String decorateTableFormat(AmoroTable<?> table) {
    StringBuilder sb = new StringBuilder();
    sb.append(AmsUtil.formatString(table.format().name()));
    if (table.format().equals(TableFormat.ICEBERG)) {
      int formatVersion =
          ((HasTableOperations) table.originalTable()).operations().current().formatVersion();
      sb.append("(V");
      sb.append(formatVersion);
      sb.append(")");
    }
    return sb.toString();
  }

  private long getRecordsOfTable(MixedTable mixedTable) {
    long totalRecords = 0L;
    if (mixedTable.isKeyedTable()) {
      Snapshot changeSnapshot =
          SnapshotUtil.latestSnapshot(mixedTable.asKeyedTable().changeTable(), null);
      Snapshot baseSnapshot =
          SnapshotUtil.latestSnapshot(mixedTable.asKeyedTable().baseTable(), null);
      if (changeSnapshot != null) {
        totalRecords +=
            PropertyUtil.propertyAsLong(
                changeSnapshot.summary(), SnapshotSummary.TOTAL_RECORDS_PROP, 0L);
      }
      if (baseSnapshot != null) {
        totalRecords +=
            PropertyUtil.propertyAsLong(
                baseSnapshot.summary(), SnapshotSummary.TOTAL_RECORDS_PROP, 0L);
      }
    } else {
      Snapshot latestSnapshot = SnapshotUtil.latestSnapshot(mixedTable.asUnkeyedTable(), null);
      if (latestSnapshot != null) {
        totalRecords =
            PropertyUtil.propertyAsLong(
                latestSnapshot.summary(), SnapshotSummary.TOTAL_RECORDS_PROP, 0L);
      }
    }
    return totalRecords;
  }

  private Long snapshotIdOfTableRef(Table table, String ref) {
    if (ref == null) {
      ref = SnapshotRef.MAIN_BRANCH;
    }
    Snapshot snapshot = table.snapshot(ref);
    if (snapshot == null) {
      return null;
    }
    return snapshot.snapshotId();
  }

  @Override
  public List<AmoroSnapshotsOfTable> getSnapshots(
      AmoroTable<?> amoroTable, String ref, OperationType operationType) {
    MixedTable mixedTable = getTable(amoroTable);
    List<AmoroSnapshotsOfTable> snapshotsOfTables = new ArrayList<>();
    List<Pair<Table, Long>> tableAndSnapshotIdList = new ArrayList<>();
    if (mixedTable.isKeyedTable()) {
      tableAndSnapshotIdList.add(
          Pair.of(
              mixedTable.asKeyedTable().changeTable(),
              snapshotIdOfTableRef(mixedTable.asKeyedTable().changeTable(), ref)));
      tableAndSnapshotIdList.add(
          Pair.of(
              mixedTable.asKeyedTable().baseTable(),
              snapshotIdOfTableRef(mixedTable.asKeyedTable().baseTable(), ref)));
    } else {
      tableAndSnapshotIdList.add(
          Pair.of(
              mixedTable.asUnkeyedTable(), snapshotIdOfTableRef(mixedTable.asUnkeyedTable(), ref)));
    }
    tableAndSnapshotIdList.forEach(
        tableAndSnapshotId -> collectSnapshots(snapshotsOfTables, tableAndSnapshotId));
    return snapshotsOfTables.stream()
        .filter(s -> validOperationType(s, operationType))
        .sorted((o1, o2) -> Long.compare(o2.getCommitTime(), o1.getCommitTime()))
        .collect(Collectors.toList());
  }

  private boolean validOperationType(AmoroSnapshotsOfTable snapshot, OperationType operationType) {
    switch (operationType) {
      case ALL:
        return true;
      case OPTIMIZING:
        return CommitMetaProducer.OPTIMIZE.name().equals(snapshot.getProducer());
      case NON_OPTIMIZING:
        return !CommitMetaProducer.OPTIMIZE.name().equals(snapshot.getProducer());
      default:
        throw new IllegalArgumentException(
            "invalid operation: " + operationType + ", only support all/optimizing/non-optimizing");
    }
  }

  private void collectSnapshots(
      List<AmoroSnapshotsOfTable> snapshotsOfTables, Pair<Table, Long> tableAndSnapshotId) {
    Table table = tableAndSnapshotId.getLeft();
    Long snapshotId = tableAndSnapshotId.getRight();
    if (snapshotId != null) {
      SnapshotUtil.ancestorsOf(snapshotId, table::snapshot)
          .forEach(
              snapshot -> {
                Map<String, String> summary = snapshot.summary();
                if (summary.containsKey(
                    org.apache.amoro.op.SnapshotSummary.TRANSACTION_BEGIN_SIGNATURE)) {
                  return;
                }
                AmoroSnapshotsOfTable amoroSnapshotsOfTable = new AmoroSnapshotsOfTable();
                amoroSnapshotsOfTable.setSnapshotId(String.valueOf(snapshot.snapshotId()));
                int fileCount =
                    PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DELETE_FILES_PROP, 0)
                        + PropertyUtil.propertyAsInt(
                            summary, SnapshotSummary.TOTAL_DATA_FILES_PROP, 0);
                amoroSnapshotsOfTable.setFileCount(fileCount);
                amoroSnapshotsOfTable.setFileSize(
                    PropertyUtil.propertyAsLong(summary, SnapshotSummary.ADDED_FILE_SIZE_PROP, 0L)
                        + PropertyUtil.propertyAsLong(
                            summary, SnapshotSummary.REMOVED_FILE_SIZE_PROP, 0L));
                long totalRecords =
                    PropertyUtil.propertyAsLong(summary, SnapshotSummary.TOTAL_RECORDS_PROP, 0L);
                amoroSnapshotsOfTable.setRecords(totalRecords);
                amoroSnapshotsOfTable.setCommitTime(snapshot.timestampMillis());
                amoroSnapshotsOfTable.setOperation(snapshot.operation());
                amoroSnapshotsOfTable.setProducer(
                    PropertyUtil.propertyAsString(
                        summary,
                        org.apache.amoro.op.SnapshotSummary.SNAPSHOT_PRODUCER,
                        org.apache.amoro.op.SnapshotSummary.SNAPSHOT_PRODUCER_DEFAULT));

                // normalize summary
                Map<String, String> normalizeSummary = Maps.newHashMap(summary);
                normalizeSummary.computeIfPresent(
                    SnapshotSummary.TOTAL_FILE_SIZE_PROP,
                    (k, v) -> AmsUtil.byteToXB(Long.parseLong(summary.get(k))));
                normalizeSummary.computeIfPresent(
                    SnapshotSummary.ADDED_FILE_SIZE_PROP,
                    (k, v) -> AmsUtil.byteToXB(Long.parseLong(summary.get(k))));
                normalizeSummary.computeIfPresent(
                    SnapshotSummary.REMOVED_FILE_SIZE_PROP,
                    (k, v) -> AmsUtil.byteToXB(Long.parseLong(summary.get(k))));
                amoroSnapshotsOfTable.setSummary(normalizeSummary);

                // Metric in chart
                Map<String, String> recordsSummaryForChat = new HashMap<>();
                recordsSummaryForChat.put("total-records", totalRecords + "");
                recordsSummaryForChat.put(
                    "eq-delete-records", summary.get(SnapshotSummary.TOTAL_EQ_DELETES_PROP));
                recordsSummaryForChat.put(
                    "pos-delete-records", summary.get(SnapshotSummary.TOTAL_POS_DELETES_PROP));
                amoroSnapshotsOfTable.setRecordsSummaryForChart(recordsSummaryForChat);

                Map<String, String> filesSummaryForChat = new HashMap<>();
                filesSummaryForChat.put(
                    "data-files", summary.get(SnapshotSummary.TOTAL_DATA_FILES_PROP));
                filesSummaryForChat.put(
                    "delete-files", summary.get(SnapshotSummary.TOTAL_DELETE_FILES_PROP));
                filesSummaryForChat.put("total-files", fileCount + "");
                amoroSnapshotsOfTable.setFilesSummaryForChart(filesSummaryForChat);

                snapshotsOfTables.add(amoroSnapshotsOfTable);
              });
    }
  }

  @Override
  public List<PartitionFileBaseInfo> getSnapshotDetail(
      AmoroTable<?> amoroTable, String snapshotId, @Nullable String ref) {
    MixedTable mixedTable = getTable(amoroTable);
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    long commitId = Long.parseLong(snapshotId);
    Snapshot snapshot;
    if (mixedTable.isKeyedTable()) {
      snapshot = mixedTable.asKeyedTable().changeTable().snapshot(commitId);
      if (snapshot == null) {
        snapshot = mixedTable.asKeyedTable().baseTable().snapshot(commitId);
      }
    } else {
      snapshot = mixedTable.asUnkeyedTable().snapshot(commitId);
    }
    if (snapshot == null) {
      throw new IllegalArgumentException(
          "unknown snapshot " + snapshotId + " of " + amoroTable.id());
    }
    final long snapshotTime = snapshot.timestampMillis();
    snapshot
        .addedDataFiles(mixedTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        snapshotId,
                        DataFileType.ofContentId(f.content().id()).name(),
                        snapshotTime,
                        MixedTableUtil.getMixedTablePartitionSpecById(mixedTable, f.specId())
                            .partitionToPath(f.partition()),
                        f.path().toString(),
                        f.fileSizeInBytes(),
                        "add")));
    snapshot
        .removedDataFiles(mixedTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        snapshotId,
                        DataFileType.ofContentId(f.content().id()).name(),
                        snapshotTime,
                        MixedTableUtil.getMixedTablePartitionSpecById(mixedTable, f.specId())
                            .partitionToPath(f.partition()),
                        f.path().toString(),
                        f.fileSizeInBytes(),
                        "remove")));
    snapshot
        .addedDeleteFiles(mixedTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        snapshotId,
                        DataFileType.ofContentId(f.content().id()).name(),
                        snapshotTime,
                        MixedTableUtil.getMixedTablePartitionSpecById(mixedTable, f.specId())
                            .partitionToPath(f.partition()),
                        f.path().toString(),
                        f.fileSizeInBytes(),
                        "add")));
    snapshot
        .removedDeleteFiles(mixedTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        snapshotId,
                        DataFileType.ofContentId(f.content().id()).name(),
                        snapshotTime,
                        MixedTableUtil.getMixedTablePartitionSpecById(mixedTable, f.specId())
                            .partitionToPath(f.partition()),
                        f.path().toString(),
                        f.fileSizeInBytes(),
                        "remove")));
    return result;
  }

  @Override
  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) {
    MixedTable mixedTable = getTable(amoroTable);
    Table table;
    if (mixedTable.isKeyedTable()) {
      table = mixedTable.asKeyedTable().baseTable();
    } else {
      table = mixedTable.asUnkeyedTable();
    }

    IcebergTableMetaExtract extract = new IcebergTableMetaExtract();
    DDLReverser<Table> ddlReverser = new DDLReverser<>(extract);
    return ddlReverser.reverse(table, amoroTable.id());
  }

  @Override
  public List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable) {
    MixedTable mixedTable = getTable(amoroTable);
    if (mixedTable.spec().isUnpartitioned()) {
      return new ArrayList<>();
    }

    List<PartitionBaseInfo> result = new ArrayList<>();

    // For keyed tables, we need to collect partitions from both change and base tables
    if (mixedTable.isKeyedTable()) {
      Map<String, PartitionBaseInfo> partitionMap = new HashMap<>();

      // Collect from change table
      List<PartitionBaseInfo> changePartitions =
          collectPartitionsFromTable(mixedTable.asKeyedTable().changeTable());
      for (PartitionBaseInfo partition : changePartitions) {
        partitionMap.put(partition.getPartition(), partition);
      }

      // Collect from base table and merge
      List<PartitionBaseInfo> basePartitions =
          collectPartitionsFromTable(mixedTable.asKeyedTable().baseTable());
      for (PartitionBaseInfo basePartition : basePartitions) {
        if (partitionMap.containsKey(basePartition.getPartition())) {
          PartitionBaseInfo existing = partitionMap.get(basePartition.getPartition());
          existing.setFileCount(existing.getFileCount() + basePartition.getFileCount());
          existing.setFileSize(existing.getFileSize() + basePartition.getFileSize());
        } else {
          partitionMap.put(basePartition.getPartition(), basePartition);
        }
      }

      result.addAll(partitionMap.values());
    } else {
      result = collectPartitionsFromTable(mixedTable.asUnkeyedTable());
    }

    return result;
  }

  /**
   * Collect partition information from an Iceberg table using the PARTITIONS metadata table. This
   * is much more efficient than scanning all data files, especially for tables with many files.
   *
   * @param table The Iceberg table to collect partitions from
   * @return List of partition information
   */
  private List<PartitionBaseInfo> collectPartitionsFromTable(Table table) {
    List<PartitionBaseInfo> partitions = new ArrayList<>();

    try {
      Preconditions.checkArgument(
          table instanceof HasTableOperations, "table must support table operations");
      TableOperations ops = ((HasTableOperations) table).operations();

      // Use PARTITIONS metadata table for efficient partition statistics
      Table partitionsTable =
          MetadataTableUtils.createMetadataTableInstance(
              ops,
              table.name(),
              table.name() + "#" + MetadataTableType.PARTITIONS.name(),
              MetadataTableType.PARTITIONS);

      TableScan scan = partitionsTable.newScan();
      try (CloseableIterable<FileScanTask> tasks = scan.planFiles()) {
        CloseableIterable<CloseableIterable<StructLike>> transform =
            CloseableIterable.transform(tasks, task -> task.asDataTask().rows());

        try (CloseableIterable<StructLike> rows = CloseableIterable.concat(transform)) {
          for (StructLike row : rows) {
            PartitionBaseInfo partitionInfo = new PartitionBaseInfo();

            // Get partition field - it's a struct
            StructLike partition = row.get(0, StructLike.class);
            int specId = row.get(1, Integer.class);

            // Convert partition struct to path string
            PartitionSpec spec = table.specs().get(specId);
            String partitionPath = spec.partitionToPath(partition);

            partitionInfo.setPartition(partitionPath);
            partitionInfo.setSpecId(specId);

            // Get file statistics from the partition metadata table
            // Schema: partition, spec_id, record_count, file_count,
            // total_data_file_size_in_bytes,
            // position_delete_record_count, position_delete_file_count,
            // equality_delete_record_count, equality_delete_file_count,
            // last_updated_at, last_updated_snapshot_id
            Integer dataFileCount = row.get(3, Integer.class);
            Long totalDataFileSize = row.get(4, Long.class);
            Integer posDeleteFileCount = row.get(6, Integer.class);
            Integer eqDeleteFileCount = row.get(8, Integer.class);
            Long lastUpdatedAt = row.get(9, Long.class);

            // Total file count = data files + position delete files + equality delete files
            int totalFileCount =
                (dataFileCount != null ? dataFileCount : 0)
                    + (posDeleteFileCount != null ? posDeleteFileCount : 0)
                    + (eqDeleteFileCount != null ? eqDeleteFileCount : 0);
            partitionInfo.setFileCount(totalFileCount);
            partitionInfo.setFileSize(totalDataFileSize != null ? totalDataFileSize : 0L);
            partitionInfo.setLastCommitTime(lastUpdatedAt != null ? lastUpdatedAt : 0L);

            partitions.add(partitionInfo);
          }
        }
      }
    } catch (Exception e) {
      LOG.warn(
          "Failed to use PARTITIONS metadata table, falling back to file scanning. Error: {}",
          e.getMessage());
      // Fallback to the old approach if metadata table access fails
      return collectPartitionsFromFileScan(table);
    }

    return partitions;
  }

  /**
   * Fallback method to collect partitions by scanning all files. This is kept for backward
   * compatibility and error cases.
   *
   * @param table The Iceberg table to scan
   * @return List of partition information
   */
  protected List<PartitionBaseInfo> collectPartitionsFromFileScan(Table table) {
    Map<String, PartitionBaseInfo> partitionMap = new HashMap<>();

    IcebergFindFiles manifestReader =
        new IcebergFindFiles(table).ignoreDeleted().planWith(executorService);

    try (CloseableIterable<IcebergFindFiles.IcebergManifestEntry> entries =
        manifestReader.entries()) {

      for (IcebergFindFiles.IcebergManifestEntry entry : entries) {
        ContentFile<?> file = entry.getFile();
        PartitionSpec spec = table.specs().get(file.specId());
        String partitionPath = spec.partitionToPath(file.partition());

        if (!partitionMap.containsKey(partitionPath)) {
          PartitionBaseInfo partitionInfo = new PartitionBaseInfo();
          partitionInfo.setPartition(partitionPath);
          partitionInfo.setSpecId(file.specId());
          partitionInfo.setFileCount(0);
          partitionInfo.setFileSize(0L);
          partitionInfo.setLastCommitTime(0L);
          partitionMap.put(partitionPath, partitionInfo);
        }

        PartitionBaseInfo partitionInfo = partitionMap.get(partitionPath);
        partitionInfo.setFileCount(partitionInfo.getFileCount() + 1);
        partitionInfo.setFileSize(partitionInfo.getFileSize() + file.fileSizeInBytes());

        long snapshotId = entry.getSnapshotId();
        if (table.snapshot(snapshotId) != null) {
          long commitTime = table.snapshot(snapshotId).timestampMillis();
          partitionInfo.setLastCommitTime(Math.max(partitionInfo.getLastCommitTime(), commitTime));
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to scan files for partition information", e);
    }

    return new ArrayList<>(partitionMap.values());
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFiles(
      AmoroTable<?> amoroTable, String partition, Integer specId) {
    CloseableIterable<PartitionFileBaseInfo> tableFilesIterable =
        getTableFilesInternal(amoroTable, partition, specId);
    try {
      List<PartitionFileBaseInfo> result = new ArrayList<>();
      Iterables.addAll(result, tableFilesIterable);
      return result;
    } finally {
      try {
        tableFilesIterable.close();
      } catch (IOException e) {
        LOG.warn("Failed to close the manifest reader.", e);
      }
    }
  }

  @Override
  public List<TagOrBranchInfo> getTableTags(AmoroTable<?> amoroTable) {
    return getTableTagsOrBranches(amoroTable, SnapshotRef::isTag);
  }

  @Override
  public List<TagOrBranchInfo> getTableBranches(AmoroTable<?> amoroTable) {
    return getTableTagsOrBranches(amoroTable, SnapshotRef::isBranch);
  }

  @Override
  public List<ConsumerInfo> getTableConsumerInfos(AmoroTable<?> amoroTable) {
    return Collections.emptyList();
  }

  @Override
  public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable, String type, ProcessStatus status, int limit, int offset) {
    TableIdentifier tableIdentifier = amoroTable.id();
    ServerTableIdentifier identifier =
        getAs(
            TableMetaMapper.class,
            m ->
                m.selectTableIdentifier(
                    tableIdentifier.getCatalog(),
                    tableIdentifier.getDatabase(),
                    tableIdentifier.getTableName()));
    if (identifier == null) {
      return Pair.of(Collections.emptyList(), 0);
    }
    int total = 0;
    // page helper is 1-based
    int pageNumber = (offset / limit) + 1;
    List<TableProcessMeta> processMetaList = Collections.emptyList();
    try (Page<?> ignored = PageHelper.startPage(pageNumber, limit, true)) {
      processMetaList =
          getAs(
              TableProcessMapper.class,
              mapper -> mapper.listProcessMeta(identifier.getId(), type, status));
      PageInfo<TableProcessMeta> pageInfo = new PageInfo<>(processMetaList);
      total = (int) pageInfo.getTotal();
      LOG.info(
          "Get optimizing processes total : {} , pageNumber:{}, limit:{}, offset:{}",
          total,
          pageNumber,
          limit,
          offset);
      if (pageInfo.getSize() == 0) {
        return Pair.of(Collections.emptyList(), 0);
      }
    }
    List<Long> processIds =
        processMetaList.stream().map(TableProcessMeta::getProcessId).collect(Collectors.toList());
    Map<Long, List<OptimizingTaskMeta>> optimizingTasks =
        getAs(OptimizingProcessMapper.class, mapper -> mapper.selectOptimizeTaskMetas(processIds))
            .stream()
            .collect(Collectors.groupingBy(OptimizingTaskMeta::getProcessId));

    LOG.info("Get {} optimizing tasks. ", optimizingTasks.size());
    return Pair.of(
        processMetaList.stream()
            .map(
                p ->
                    buildOptimizingProcessInfo(
                        identifier, p, optimizingTasks.get(p.getProcessId())))
            .collect(Collectors.toList()),
        total);
  }

  @Override
  public Map<String, String> getTableOptimizingTypes(AmoroTable<?> amoroTable) {
    Map<String, String> types = Maps.newHashMap();
    for (OptimizingType type : OptimizingType.values()) {
      types.put(type.name(), OptimizingStatus.ofOptimizingType(type).displayValue());
    }
    return types;
  }

  @Override
  public List<OptimizingTaskInfo> getOptimizingTaskInfos(
      AmoroTable<?> amoroTable, String processId) {
    long id = Long.parseLong(processId);
    List<OptimizingTaskMeta> optimizingTaskMetaList =
        getAs(
            OptimizingProcessMapper.class,
            mapper -> mapper.selectOptimizeTaskMetas(Collections.singletonList(id)));
    if (CollectionUtils.isEmpty(optimizingTaskMetaList)) {
      return Collections.emptyList();
    }
    return optimizingTaskMetaList.stream()
        .map(
            taskMeta ->
                new OptimizingTaskInfo(
                    taskMeta.getTableId(),
                    String.valueOf(taskMeta.getProcessId()),
                    taskMeta.getTaskId(),
                    taskMeta.getPartitionData(),
                    ProcessTaskStatus.valueOf(taskMeta.getStatus().name()),
                    taskMeta.getRetryNum(),
                    taskMeta.getOptimizerToken(),
                    taskMeta.getThreadId(),
                    taskMeta.getStartTime(),
                    taskMeta.getEndTime(),
                    taskMeta.getCostTime(),
                    taskMeta.getFailReason(),
                    taskMeta.getMetricsSummary().getInputFilesStatistics(),
                    taskMeta.getMetricsSummary().getOutputFilesStatistics(),
                    taskMeta.getMetricsSummary().summaryAsMap(true),
                    taskMeta.getProperties()))
        .collect(Collectors.toList());
  }

  private CloseableIterable<PartitionFileBaseInfo> getTableFilesInternal(
      AmoroTable<?> amoroTable, String partition, Integer specId) {
    MixedTable mixedTable = getTable(amoroTable);
    if (mixedTable.isKeyedTable()) {
      return CloseableIterable.concat(
          Arrays.asList(
              collectFileInfo(mixedTable.asKeyedTable().changeTable(), true, partition, specId),
              collectFileInfo(mixedTable.asKeyedTable().baseTable(), false, partition, specId)));
    } else {
      return collectFileInfo(mixedTable.asUnkeyedTable(), false, partition, specId);
    }
  }

  private CloseableIterable<PartitionFileBaseInfo> collectFileInfo(
      Table table, boolean isChangeTable, String partition, Integer specId) {
    Map<Integer, PartitionSpec> specs = table.specs();

    IcebergFindFiles manifestReader =
        new IcebergFindFiles(table).ignoreDeleted().planWith(executorService);

    if (table.spec().isPartitioned() && partition != null && specId != null) {
      GenericRecord partitionData = MixedDataFiles.data(specs.get(specId), partition);
      manifestReader.inPartitions(specs.get(specId), partitionData);
    }

    CloseableIterable<IcebergFindFiles.IcebergManifestEntry> entries = manifestReader.entries();

    return CloseableIterable.transform(
        entries,
        entry -> {
          ContentFile<?> contentFile = entry.getFile();
          long snapshotId = entry.getSnapshotId();

          PartitionSpec partitionSpec = specs.get(contentFile.specId());
          String partitionPath = partitionSpec.partitionToPath(contentFile.partition());
          long fileSize = contentFile.fileSizeInBytes();
          DataFileType dataFileType =
              isChangeTable
                  ? FileNameRules.parseFileTypeForChange(contentFile.path().toString())
                  : DataFileType.ofContentId(contentFile.content().id());
          long commitTime = -1;
          if (table.snapshot(snapshotId) != null) {
            commitTime = table.snapshot(snapshotId).timestampMillis();
          }
          return new PartitionFileBaseInfo(
              String.valueOf(snapshotId),
              dataFileType.name(),
              commitTime,
              partitionPath,
              contentFile.specId(),
              contentFile.path().toString(),
              fileSize);
        });
  }

  private TableBasicInfo getTableBasicInfo(MixedTable table) {
    try {
      TableBasicInfo tableBasicInfo = new TableBasicInfo();
      tableBasicInfo.setTableIdentifier(table.id());
      TableStatistics changeInfo = null;
      TableStatistics baseInfo;

      if (table.isUnkeyedTable()) {
        UnkeyedTable unkeyedTable = table.asUnkeyedTable();
        baseInfo = new TableStatistics();
        TableStatCollector.fillTableStatistics(baseInfo, unkeyedTable, table);
      } else if (table.isKeyedTable()) {
        KeyedTable keyedTable = table.asKeyedTable();
        changeInfo = TableStatCollector.collectChangeTableInfo(keyedTable);
        baseInfo = TableStatCollector.collectBaseTableInfo(keyedTable);
      } else {
        throw new IllegalStateException("unknown type of table");
      }

      tableBasicInfo.setChangeStatistics(changeInfo);
      tableBasicInfo.setBaseStatistics(baseInfo);
      tableBasicInfo.setTableStatistics(TableStatCollector.union(changeInfo, baseInfo));

      long createTime =
          PropertyUtil.propertyAsLong(
              table.properties(),
              TableProperties.TABLE_CREATE_TIME,
              TableProperties.TABLE_CREATE_TIME_DEFAULT);
      if (createTime != TableProperties.TABLE_CREATE_TIME_DEFAULT) {
        if (tableBasicInfo.getTableStatistics() != null) {
          if (tableBasicInfo.getTableStatistics().getSummary() == null) {
            tableBasicInfo.getTableStatistics().setSummary(new HashMap<>());
          } else {
            LOG.warn("{} summary is null", table.id());
          }
          tableBasicInfo
              .getTableStatistics()
              .getSummary()
              .put("createTime", String.valueOf(createTime));
        } else {
          LOG.warn("{} table statistics is null {}", table.id(), tableBasicInfo);
        }
      }
      return tableBasicInfo;
    } catch (Throwable t) {
      LOG.error("{} failed to build table basic info", table.id(), t);
      throw t;
    }
  }

  private ServerTableMeta getServerTableMeta(MixedTable table) {
    ServerTableMeta serverTableMeta = new ServerTableMeta();
    serverTableMeta.setTableType(table.format().toString());
    serverTableMeta.setTableIdentifier(table.id());
    serverTableMeta.setBaseLocation(table.location());
    Map<String, String> tableProperties = Maps.newHashMap(table.properties());
    fillTableProperties(serverTableMeta, tableProperties);
    if (tableProperties.containsKey(TableProperties.COMMENT)) {
      String comment = tableProperties.get(TableProperties.COMMENT);
      serverTableMeta.setComment(comment);
    }

    serverTableMeta.setPartitionColumnList(
        table.spec().fields().stream()
            .map(item -> buildPartitionFieldFromPartitionSpec(table.spec().schema(), item))
            .collect(Collectors.toList()));
    serverTableMeta.setSchema(
        table.schema().columns().stream()
            .map(MixedAndIcebergTableDescriptor::buildColumnInfoFromNestedField)
            .collect(Collectors.toList()));

    serverTableMeta.setFilter(null);
    LOG.debug("Table {} is keyedTable: {}", table.name(), table instanceof KeyedTable);
    if (table.isKeyedTable()) {
      KeyedTable kt = table.asKeyedTable();
      if (kt.primaryKeySpec() != null) {
        serverTableMeta.setPkList(
            kt.primaryKeySpec().fields().stream()
                .map(item -> buildColumnInfoFromPartitionSpec(table.spec().schema(), item))
                .collect(Collectors.toList()));
      }
    } else {
      serverTableMeta.setPkList(
          serverTableMeta.getSchema().stream()
              .filter(s -> table.schema().identifierFieldNames().contains(s.getField()))
              .collect(Collectors.toList()));
    }
    return serverTableMeta;
  }

  private void fillTableProperties(
      ServerTableMeta serverTableMeta, Map<String, String> properties) {
    serverTableMeta.setTableWatermark(properties.remove(TableProperties.WATERMARK_TABLE));
    serverTableMeta.setBaseWatermark(properties.remove(TableProperties.WATERMARK_BASE_STORE));
    serverTableMeta.setCreateTime(
        PropertyUtil.propertyAsLong(
            properties,
            TableProperties.TABLE_CREATE_TIME,
            TableProperties.TABLE_CREATE_TIME_DEFAULT));
    properties.remove(TableProperties.TABLE_CREATE_TIME);

    TableProperties.READ_PROTECTED_PROPERTIES.forEach(properties::remove);
    serverTableMeta.setProperties(properties);
  }

  private MixedTable getTable(AmoroTable<?> amoroTable) {
    return (MixedTable) amoroTable.originalTable();
  }

  private List<TagOrBranchInfo> getTableTagsOrBranches(
      AmoroTable<?> amoroTable, Predicate<SnapshotRef> predicate) {
    MixedTable mixedTable = getTable(amoroTable);
    List<TagOrBranchInfo> result = new ArrayList<>();
    Map<String, SnapshotRef> snapshotRefs;
    if (mixedTable.isKeyedTable()) {
      // todo temporarily responds to the problem of Mixed Format table.
      if (predicate.test(SnapshotRef.branchBuilder(-1).build())) {
        return ImmutableList.of(TagOrBranchInfo.MAIN_BRANCH);
      } else {
        return Collections.emptyList();
      }
    } else {
      snapshotRefs = mixedTable.asUnkeyedTable().refs();
      snapshotRefs.forEach(
          (name, snapshotRef) -> {
            if (predicate.test(snapshotRef)) {
              result.add(buildTagOrBranchInfo(name, snapshotRef));
            }
          });
      result.sort(Comparator.comparing(TagOrBranchInfo::getName));
      return result;
    }
  }

  private static AMSColumnInfo buildColumnInfoFromNestedField(Types.NestedField field) {
    if (field == null) {
      return null;
    }
    return new AMSColumnInfo.Builder()
        .field(field.name())
        .type(field.type().toString())
        .required(field.isRequired())
        .comment(field.doc())
        .build();
  }

  /** Construct ColumnInfo based on schema and primary key field. */
  private static AMSColumnInfo buildColumnInfoFromPartitionSpec(
      Schema schema, PrimaryKeySpec.PrimaryKeyField pkf) {
    return buildColumnInfoFromNestedField(schema.findField(pkf.fieldName()));
  }

  private static AMSPartitionField buildPartitionFieldFromPartitionSpec(
      Schema schema, PartitionField pf) {
    return new AMSPartitionField.Builder()
        .field(pf.name())
        .sourceField(schema.findColumnName(pf.sourceId()))
        .transform(pf.transform().toString())
        .fieldId(pf.fieldId())
        .sourceFieldId(pf.sourceId())
        .build();
  }

  private static TagOrBranchInfo buildTagOrBranchInfo(String name, SnapshotRef snapshotRef) {
    String type = null;
    if (snapshotRef.isTag()) {
      type = TagOrBranchInfo.TAG;
    } else if (snapshotRef.isBranch()) {
      type = TagOrBranchInfo.BRANCH;
    } else {
      throw new RuntimeException("Invalid snapshot ref: " + snapshotRef);
    }
    return new TagOrBranchInfo(
        name,
        snapshotRef.snapshotId(),
        snapshotRef.minSnapshotsToKeep(),
        snapshotRef.maxSnapshotAgeMs(),
        snapshotRef.maxRefAgeMs(),
        type);
  }

  private static OptimizingProcessInfo buildOptimizingProcessInfo(
      ServerTableIdentifier identifier,
      TableProcessMeta meta,
      List<OptimizingTaskMeta> optimizingTaskStats) {
    if (meta == null) {
      return null;
    }
    OptimizingProcessInfo result = new OptimizingProcessInfo();

    if (optimizingTaskStats != null) {
      int successTasks = 0;
      int runningTasks = 0;
      for (OptimizingTaskMeta optimizingTaskStat : optimizingTaskStats) {
        TaskRuntime.Status status = optimizingTaskStat.getStatus();
        switch (status) {
          case SUCCESS:
            successTasks++;
            break;
          case SCHEDULED:
          case ACKED:
            runningTasks++;
            break;
        }
      }
      result.setTotalTasks(optimizingTaskStats.size());
      result.setSuccessTasks(successTasks);
      result.setRunningTasks(runningTasks);
    }
    MetricsSummary summary = MetricsSummary.fromMap(meta.getSummary());
    result.setInputFiles(summary.getInputFilesStatistics());
    result.setOutputFiles(summary.getOutputFilesStatistics());
    result.setTableId(meta.getTableId());
    result.setCatalogName(identifier.getCatalog());
    result.setDbName(identifier.getDatabase());
    result.setTableName(identifier.getTableName());

    result.setProcessId(String.valueOf(meta.getProcessId()));
    result.setStartTime(meta.getCreateTime());
    result.setOptimizingType(meta.getProcessType());
    result.setStatus(ProcessStatus.valueOf(meta.getStatus().name()));
    result.setFailReason(meta.getFailMessage());
    result.setDuration(
        meta.getFinishTime() > 0
            ? meta.getFinishTime() - meta.getCreateTime()
            : System.currentTimeMillis() - meta.getCreateTime());
    result.setFinishTime(meta.getFinishTime());
    result.setSummary(meta.getSummary());
    return result;
  }
}

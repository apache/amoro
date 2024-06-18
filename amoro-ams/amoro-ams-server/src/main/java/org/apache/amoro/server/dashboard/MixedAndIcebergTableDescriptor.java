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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.FileNameRules;
import org.apache.amoro.server.dashboard.component.reverser.DDLReverser;
import org.apache.amoro.server.dashboard.component.reverser.IcebergTableMetaExtract;
import org.apache.amoro.server.dashboard.model.AMSColumnInfo;
import org.apache.amoro.server.dashboard.model.AMSPartitionField;
import org.apache.amoro.server.dashboard.model.AmoroSnapshotsOfTable;
import org.apache.amoro.server.dashboard.model.DDLInfo;
import org.apache.amoro.server.dashboard.model.FilesStatistics;
import org.apache.amoro.server.dashboard.model.OperationType;
import org.apache.amoro.server.dashboard.model.OptimizingProcessInfo;
import org.apache.amoro.server.dashboard.model.OptimizingTaskInfo;
import org.apache.amoro.server.dashboard.model.PartitionBaseInfo;
import org.apache.amoro.server.dashboard.model.PartitionFileBaseInfo;
import org.apache.amoro.server.dashboard.model.ServerTableMeta;
import org.apache.amoro.server.dashboard.model.TableBasicInfo;
import org.apache.amoro.server.dashboard.model.TableStatistics;
import org.apache.amoro.server.dashboard.model.TagOrBranchInfo;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.dashboard.utils.TableStatCollector;
import org.apache.amoro.server.optimizing.OptimizingProcessMeta;
import org.apache.amoro.server.optimizing.OptimizingTaskMeta;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.IcebergFindFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.Pair;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.SnapshotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final ExecutorService executorService;

  public MixedAndIcebergTableDescriptor(ExecutorService executorService) {
    this.executorService = executorService;
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
    Map<String, Object> tableSummary = new HashMap<>();
    tableSummary.put("size", AmsUtil.byteToXB(tableSize));
    tableSummary.put("file", tableFileCnt);
    tableSummary.put(
        "averageFile", AmsUtil.byteToXB(tableFileCnt == 0 ? 0 : tableSize / tableFileCnt));
    tableSummary.put("tableFormat", tableFormat);
    serverTableMeta.setTableSummary(tableSummary);
    return serverTableMeta;
  }

  private String decorateTableFormat(AmoroTable table) {
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
    Table table = tableAndSnapshotId.first();
    Long snapshotId = tableAndSnapshotId.second();
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
  public List<PartitionFileBaseInfo> getSnapshotDetail(AmoroTable<?> amoroTable, long snapshotId) {
    MixedTable mixedTable = getTable(amoroTable);
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    Snapshot snapshot;
    if (mixedTable.isKeyedTable()) {
      snapshot = mixedTable.asKeyedTable().changeTable().snapshot(snapshotId);
      if (snapshot == null) {
        snapshot = mixedTable.asKeyedTable().baseTable().snapshot(snapshotId);
      }
    } else {
      snapshot = mixedTable.asUnkeyedTable().snapshot(snapshotId);
    }
    if (snapshot == null) {
      throw new IllegalArgumentException(
          "unknown snapshot " + snapshotId + " of " + amoroTable.id());
    }
    final long snapshotTime = snapshot.timestampMillis();
    String commitId = String.valueOf(snapshotId);
    snapshot
        .addedDataFiles(mixedTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        commitId,
                        DataFileType.ofContentId(f.content().id()),
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
                        commitId,
                        DataFileType.ofContentId(f.content().id()),
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
                        commitId,
                        DataFileType.ofContentId(f.content().id()),
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
                        commitId,
                        DataFileType.ofContentId(f.content().id()),
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
    Map<String, PartitionBaseInfo> partitionBaseInfoHashMap = new HashMap<>();

    CloseableIterable<PartitionFileBaseInfo> tableFiles =
        getTableFilesInternal(amoroTable, null, null);
    try {
      for (PartitionFileBaseInfo fileInfo : tableFiles) {
        if (!partitionBaseInfoHashMap.containsKey(fileInfo.getPartition())) {
          PartitionBaseInfo partitionBaseInfo = new PartitionBaseInfo();
          partitionBaseInfo.setPartition(fileInfo.getPartition());
          partitionBaseInfo.setSpecId(fileInfo.getSpecId());
          partitionBaseInfoHashMap.put(fileInfo.getPartition(), partitionBaseInfo);
        }
        PartitionBaseInfo partitionInfo = partitionBaseInfoHashMap.get(fileInfo.getPartition());
        partitionInfo.setFileCount(partitionInfo.getFileCount() + 1);
        partitionInfo.setFileSize(partitionInfo.getFileSize() + fileInfo.getFileSize());
        partitionInfo.setLastCommitTime(
            partitionInfo.getLastCommitTime() > fileInfo.getCommitTime()
                ? partitionInfo.getLastCommitTime()
                : fileInfo.getCommitTime());
      }
    } finally {
      try {
        tableFiles.close();
      } catch (IOException e) {
        LOG.warn("Failed to close the manifest reader.", e);
      }
    }

    return new ArrayList<>(partitionBaseInfoHashMap.values());
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
  public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable, int limit, int offset) {
    TableIdentifier tableIdentifier = amoroTable.id();
    List<OptimizingProcessMeta> processMetaList =
        getAs(
            OptimizingMapper.class,
            mapper ->
                mapper.selectOptimizingProcesses(
                    tableIdentifier.getCatalog(),
                    tableIdentifier.getDatabase(),
                    tableIdentifier.getTableName()));
    int total = processMetaList.size();
    processMetaList =
        processMetaList.stream().skip(offset).limit(limit).collect(Collectors.toList());
    if (CollectionUtils.isEmpty(processMetaList)) {
      return Pair.of(Collections.emptyList(), 0);
    }
    List<Long> processIds =
        processMetaList.stream()
            .map(OptimizingProcessMeta::getProcessId)
            .collect(Collectors.toList());
    Map<Long, List<OptimizingTaskMeta>> optimizingTasks =
        getAs(OptimizingMapper.class, mapper -> mapper.selectOptimizeTaskMetas(processIds)).stream()
            .collect(Collectors.groupingBy(OptimizingTaskMeta::getProcessId));

    return Pair.of(
        processMetaList.stream()
            .map(p -> OptimizingProcessInfo.build(p, optimizingTasks.get(p.getProcessId())))
            .collect(Collectors.toList()),
        total);
  }

  @Override
  public List<OptimizingTaskInfo> getOptimizingTaskInfos(AmoroTable<?> amoroTable, long processId) {
    List<OptimizingTaskMeta> optimizingTaskMetaList =
        getAs(
            OptimizingMapper.class,
            mapper -> mapper.selectOptimizeTaskMetas(Collections.singletonList(processId)));
    if (CollectionUtils.isEmpty(optimizingTaskMetaList)) {
      return Collections.emptyList();
    }
    return optimizingTaskMetaList.stream()
        .map(
            taskMeta ->
                new OptimizingTaskInfo(
                    taskMeta.getTableId(),
                    taskMeta.getProcessId(),
                    taskMeta.getTaskId(),
                    taskMeta.getPartitionData(),
                    taskMeta.getStatus(),
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
              dataFileType,
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
    fillTableProperties(serverTableMeta, table.properties());
    serverTableMeta.setPartitionColumnList(
        table.spec().fields().stream()
            .map(item -> AMSPartitionField.buildFromPartitionSpec(table.spec().schema(), item))
            .collect(Collectors.toList()));
    serverTableMeta.setSchema(
        table.schema().columns().stream()
            .map(AMSColumnInfo::buildFromNestedField)
            .collect(Collectors.toList()));

    serverTableMeta.setFilter(null);
    LOG.debug("Table {} is keyedTable: {}", table.name(), table instanceof KeyedTable);
    if (table.isKeyedTable()) {
      KeyedTable kt = table.asKeyedTable();
      if (kt.primaryKeySpec() != null) {
        serverTableMeta.setPkList(
            kt.primaryKeySpec().fields().stream()
                .map(item -> AMSColumnInfo.buildFromPartitionSpec(table.spec().schema(), item))
                .collect(Collectors.toList()));
      }
    }
    if (serverTableMeta.getPkList() == null) {
      serverTableMeta.setPkList(new ArrayList<>());
    }
    return serverTableMeta;
  }

  private void fillTableProperties(
      ServerTableMeta serverTableMeta, Map<String, String> tableProperties) {
    Map<String, String> properties = Maps.newHashMap(tableProperties);
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
              result.add(new TagOrBranchInfo(name, snapshotRef));
            }
          });
      result.sort(Comparator.comparing(TagOrBranchInfo::getName));
      return result;
    }
  }
}

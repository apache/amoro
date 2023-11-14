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

package com.netease.arctic.server.dashboard;

import static com.netease.arctic.server.dashboard.utils.AmsUtil.byteToXB;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.FileNameRules;
import com.netease.arctic.server.dashboard.component.reverser.DDLReverser;
import com.netease.arctic.server.dashboard.component.reverser.IcebergTableMetaExtract;
import com.netease.arctic.server.dashboard.model.AMSColumnInfo;
import com.netease.arctic.server.dashboard.model.AMSPartitionField;
import com.netease.arctic.server.dashboard.model.AmoroSnapshotsOfTable;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.FilesStatistics;
import com.netease.arctic.server.dashboard.model.OptimizingProcessInfo;
import com.netease.arctic.server.dashboard.model.OptimizingTaskInfo;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TableBasicInfo;
import com.netease.arctic.server.dashboard.model.TableStatistics;
import com.netease.arctic.server.dashboard.model.TagOrBranchInfo;
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.dashboard.utils.TableStatCollector;
import com.netease.arctic.server.optimizing.OptimizingProcessMeta;
import com.netease.arctic.server.optimizing.OptimizingTaskMeta;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataOperations;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.MetadataTableUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.Table;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.Pair;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.SnapshotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    ArcticTable table = getTable(amoroTable);
    // set basic info
    TableBasicInfo tableBasicInfo = getTableBasicInfo(table);
    ServerTableMeta serverTableMeta = getServerTableMeta(table);
    long tableSize = 0;
    long tableFileCnt = 0;
    Map<String, Object> baseMetrics = Maps.newHashMap();
    FilesStatistics baseFilesStatistics = tableBasicInfo.getBaseStatistics().getTotalFilesStat();
    Map<String, String> baseSummary = tableBasicInfo.getBaseStatistics().getSummary();
    baseMetrics.put("lastCommitTime", AmsUtil.longOrNull(baseSummary.get("visibleTime")));
    baseMetrics.put("totalSize", byteToXB(baseFilesStatistics.getTotalSize()));
    baseMetrics.put("fileCount", baseFilesStatistics.getFileCnt());
    baseMetrics.put("averageFileSize", byteToXB(baseFilesStatistics.getAverageSize()));
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
        changeMetrics.put("totalSize", byteToXB(changeFilesStatistics.getTotalSize()));
        changeMetrics.put("fileCount", changeFilesStatistics.getFileCnt());
        changeMetrics.put("averageFileSize", byteToXB(changeFilesStatistics.getAverageSize()));
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
    tableSummary.put("size", byteToXB(tableSize));
    tableSummary.put("file", tableFileCnt);
    tableSummary.put("averageFile", byteToXB(tableFileCnt == 0 ? 0 : tableSize / tableFileCnt));
    tableSummary.put("tableFormat", AmsUtil.formatString(amoroTable.format().name()));
    serverTableMeta.setTableSummary(tableSummary);
    return serverTableMeta;
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
  public List<AmoroSnapshotsOfTable> getSnapshots(AmoroTable<?> amoroTable, String ref) {
    ArcticTable arcticTable = getTable(amoroTable);
    List<AmoroSnapshotsOfTable> snapshotsOfTables = new ArrayList<>();
    List<Pair<Table, Long>> tableAndSnapshotIdList = new ArrayList<>();
    if (arcticTable.isKeyedTable()) {
      tableAndSnapshotIdList.add(
          Pair.of(
              arcticTable.asKeyedTable().changeTable(),
              snapshotIdOfTableRef(arcticTable.asKeyedTable().changeTable(), ref)));
      tableAndSnapshotIdList.add(
          Pair.of(
              arcticTable.asKeyedTable().baseTable(),
              snapshotIdOfTableRef(arcticTable.asKeyedTable().baseTable(), ref)));
    } else {
      tableAndSnapshotIdList.add(
          Pair.of(
              arcticTable.asUnkeyedTable(),
              snapshotIdOfTableRef(arcticTable.asUnkeyedTable(), ref)));
    }
    tableAndSnapshotIdList.forEach(
        tableAndSnapshotId -> collectSnapshots(snapshotsOfTables, tableAndSnapshotId));
    snapshotsOfTables.sort((o1, o2) -> Long.compare(o2.getCommitTime(), o1.getCommitTime()));
    return snapshotsOfTables;
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
                    com.netease.arctic.op.SnapshotSummary.TRANSACTION_BEGIN_SIGNATURE)) {
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
                        com.netease.arctic.op.SnapshotSummary.SNAPSHOT_PRODUCER,
                        com.netease.arctic.op.SnapshotSummary.SNAPSHOT_PRODUCER_DEFAULT));

                // normalize summary
                Map<String, String> normalizeSummary =
                    com.google.common.collect.Maps.newHashMap(summary);
                normalizeSummary.computeIfPresent(
                    SnapshotSummary.TOTAL_FILE_SIZE_PROP,
                    (k, v) -> byteToXB(Long.parseLong(summary.get(k))));
                normalizeSummary.computeIfPresent(
                    SnapshotSummary.ADDED_FILE_SIZE_PROP,
                    (k, v) -> byteToXB(Long.parseLong(summary.get(k))));
                normalizeSummary.computeIfPresent(
                    SnapshotSummary.REMOVED_FILE_SIZE_PROP,
                    (k, v) -> byteToXB(Long.parseLong(summary.get(k))));
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
    ArcticTable arcticTable = getTable(amoroTable);
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    Snapshot snapshot;
    if (arcticTable.isKeyedTable()) {
      snapshot = arcticTable.asKeyedTable().changeTable().snapshot(snapshotId);
      if (snapshot == null) {
        snapshot = arcticTable.asKeyedTable().baseTable().snapshot(snapshotId);
      }
    } else {
      snapshot = arcticTable.asUnkeyedTable().snapshot(snapshotId);
    }
    if (snapshot == null) {
      throw new IllegalArgumentException(
          "unknown snapshot " + snapshotId + " of " + amoroTable.id());
    }
    final long snapshotTime = snapshot.timestampMillis();
    String commitId = String.valueOf(snapshotId);
    snapshot
        .addedDataFiles(arcticTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        commitId,
                        DataFileType.ofContentId(f.content().id()),
                        snapshotTime,
                        arcticTable.spec().partitionToPath(f.partition()),
                        f.path().toString(),
                        f.fileSizeInBytes(),
                        "add")));
    snapshot
        .removedDataFiles(arcticTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        commitId,
                        DataFileType.ofContentId(f.content().id()),
                        snapshotTime,
                        arcticTable.spec().partitionToPath(f.partition()),
                        f.path().toString(),
                        f.fileSizeInBytes(),
                        "remove")));
    snapshot
        .addedDeleteFiles(arcticTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        commitId,
                        DataFileType.ofContentId(f.content().id()),
                        snapshotTime,
                        arcticTable.spec().partitionToPath(f.partition()),
                        f.path().toString(),
                        f.fileSizeInBytes(),
                        "add")));
    snapshot
        .removedDeleteFiles(arcticTable.io())
        .forEach(
            f ->
                result.add(
                    new PartitionFileBaseInfo(
                        commitId,
                        DataFileType.ofContentId(f.content().id()),
                        snapshotTime,
                        arcticTable.spec().partitionToPath(f.partition()),
                        f.path().toString(),
                        f.fileSizeInBytes(),
                        "remove")));
    return result;
  }

  @Override
  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) {
    ArcticTable arcticTable = getTable(amoroTable);
    Table table;
    if (arcticTable.isKeyedTable()) {
      table = arcticTable.asKeyedTable().baseTable();
    } else {
      table = arcticTable.asUnkeyedTable();
    }

    IcebergTableMetaExtract extract = new IcebergTableMetaExtract();
    DDLReverser<Table> ddlReverser = new DDLReverser<>(extract);
    return ddlReverser.reverse(table, amoroTable.id());
  }

  @Override
  public List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable) {
    ArcticTable arcticTable = getTable(amoroTable);
    if (arcticTable.spec().isUnpartitioned()) {
      return new ArrayList<>();
    }
    Map<String, PartitionBaseInfo> partitionBaseInfoHashMap = new HashMap<>();
    getTableFiles(amoroTable, null)
        .forEach(
            fileInfo -> {
              if (!partitionBaseInfoHashMap.containsKey(fileInfo.getPartition())) {
                partitionBaseInfoHashMap.put(fileInfo.getPartition(), new PartitionBaseInfo());
                partitionBaseInfoHashMap
                    .get(fileInfo.getPartition())
                    .setPartition(fileInfo.getPartition());
              }
              PartitionBaseInfo partitionInfo =
                  partitionBaseInfoHashMap.get(fileInfo.getPartition());
              partitionInfo.setFileCount(partitionInfo.getFileCount() + 1);
              partitionInfo.setFileSize(partitionInfo.getFileSize() + fileInfo.getFileSize());
              partitionInfo.setLastCommitTime(
                  partitionInfo.getLastCommitTime() > fileInfo.getCommitTime()
                      ? partitionInfo.getLastCommitTime()
                      : fileInfo.getCommitTime());
            });

    return new ArrayList<>(partitionBaseInfoHashMap.values());
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFiles(AmoroTable<?> amoroTable, String partition) {
    ArcticTable arcticTable = getTable(amoroTable);
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    if (arcticTable.isKeyedTable()) {
      result.addAll(collectFileInfo(arcticTable.asKeyedTable().changeTable(), true, partition));
      result.addAll(collectFileInfo(arcticTable.asKeyedTable().baseTable(), false, partition));
    } else {
      result.addAll(collectFileInfo(arcticTable.asUnkeyedTable(), false, partition));
    }
    return result;
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
                    taskMeta.getThreadId(),
                    taskMeta.getStartTime(),
                    taskMeta.getEndTime(),
                    taskMeta.getCostTime(),
                    taskMeta.getFailReason(),
                    taskMeta.getMetricsSummary(),
                    taskMeta.getProperties()))
        .collect(Collectors.toList());
  }

  private List<PartitionFileBaseInfo> collectFileInfo(
      Table table, boolean isChangeTable, String partition) {
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    Map<Integer, PartitionSpec> specs = table.specs();

    Map<Long, Long> seqToSnapshotId = new HashMap<>();
    Iterable<Snapshot> snapshots = table.snapshots();
    for (Snapshot snapshot : snapshots) {
      seqToSnapshotId.put(snapshot.sequenceNumber(), snapshot.snapshotId());
    }
    Collection<ContentFile<?>> allContent = IcebergTableUtil.getAllContent(table, executorService);

    for (ContentFile<?> contentFile : allContent) {
      Long snapshotId = seqToSnapshotId.get(contentFile.fileSequenceNumber());

      PartitionSpec partitionSpec = specs.get(contentFile.specId());
      String partitionPath = partitionSpec.partitionToPath(contentFile.partition());
      if (partition != null && partitionSpec.isPartitioned() && !partition.equals(partitionPath)) {
        continue;
      }
      Long fileSize = contentFile.fileSizeInBytes();
      DataFileType dataFileType =
          isChangeTable
              ? FileNameRules.parseFileTypeForChange(contentFile.path().toString())
              : DataFileType.ofContentId(contentFile.content().id());
      long commitTime = -1;
      if (table.snapshot(snapshotId) != null) {
        commitTime = table.snapshot(snapshotId).timestampMillis();
      }
      result.add(
          new PartitionFileBaseInfo(
              String.valueOf(snapshotId),
              dataFileType,
              commitTime,
              partitionPath,
              contentFile.path().toString(),
              fileSize));
    }
    return result;
  }

  private TableBasicInfo getTableBasicInfo(ArcticTable table) {
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

  private ServerTableMeta getServerTableMeta(ArcticTable table) {
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
    Map<String, String> properties = com.google.common.collect.Maps.newHashMap(tableProperties);
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

  private ArcticTable getTable(AmoroTable<?> amoroTable) {
    return (ArcticTable) amoroTable.originalTable();
  }

  private List<TagOrBranchInfo> getTableTagsOrBranches(
      AmoroTable<?> amoroTable, Predicate<SnapshotRef> predicate) {
    ArcticTable arcticTable = getTable(amoroTable);
    List<TagOrBranchInfo> result = new ArrayList<>();
    Map<String, SnapshotRef> snapshotRefs;
    if (arcticTable.isKeyedTable()) {
      // todo temporarily responds to the problem of Mixed Format table.
      if (predicate.test(SnapshotRef.branchBuilder(-1).build())) {
        return ImmutableList.of(TagOrBranchInfo.MAIN_BRANCH);
      } else {
        return Collections.emptyList();
      }
    } else {
      snapshotRefs = arcticTable.asUnkeyedTable().refs();
      snapshotRefs.forEach(
          (name, snapshotRef) -> {
            if (predicate.test(snapshotRef)) {
              result.add(new TagOrBranchInfo(name, snapshotRef));
            }
          });
      return result;
    }
  }
}

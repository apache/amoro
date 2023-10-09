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

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.FileNameRules;
import com.netease.arctic.op.SnapshotSummary;
import com.netease.arctic.server.dashboard.model.AMSColumnInfo;
import com.netease.arctic.server.dashboard.model.AMSPartitionField;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.FilesStatistics;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TableBasicInfo;
import com.netease.arctic.server.dashboard.model.TableStatistics;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.dashboard.utils.TableStatCollector;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.ManifestEntryFields;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataOperations;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.HistoryEntry;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.MetadataTableUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Descriptor for Mixed-Hive,Mixed-Iceberg, Iceberg format.
 */
public class MixedAndIcebergTableDescriptor implements FormatTableDescriptor {

  private static final Logger LOG = LoggerFactory.getLogger(MixedAndIcebergTableDescriptor.class);

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

    Map<String, Object> changeMetrics = Maps.newHashMap();
    if (tableBasicInfo.getChangeStatistics() != null) {
      FilesStatistics changeFilesStatistics = tableBasicInfo.getChangeStatistics().getTotalFilesStat();
      Map<String, String> changeSummary = tableBasicInfo.getChangeStatistics().getSummary();
      changeMetrics.put("lastCommitTime", AmsUtil.longOrNull(changeSummary.get("visibleTime")));
      changeMetrics.put("totalSize", AmsUtil.byteToXB(changeFilesStatistics.getTotalSize()));
      changeMetrics.put("fileCount", changeFilesStatistics.getFileCnt());
      changeMetrics.put("averageFileSize", AmsUtil.byteToXB(changeFilesStatistics.getAverageSize()));
      changeMetrics.put("tableWatermark", AmsUtil.longOrNull(serverTableMeta.getTableWatermark()));
      tableSize += changeFilesStatistics.getTotalSize();
      tableFileCnt += changeFilesStatistics.getFileCnt();
    } else {
      changeMetrics.put("lastCommitTime", null);
      changeMetrics.put("totalSize", null);
      changeMetrics.put("fileCount", null);
      changeMetrics.put("averageFileSize", null);
      changeMetrics.put("tableWatermark", null);
    }
    Map<String, Object> tableSummary = new HashMap<>();
    tableSummary.put("size", AmsUtil.byteToXB(tableSize));
    tableSummary.put("file", tableFileCnt);
    tableSummary.put("averageFile", AmsUtil.byteToXB(tableFileCnt == 0 ? 0 : tableSize / tableFileCnt));
    tableSummary.put("tableFormat", AmsUtil.formatString(amoroTable.format().name()));
    serverTableMeta.setTableSummary(tableSummary);
    return serverTableMeta;
  }

  public List<TransactionsOfTable> getTransactions(AmoroTable<?> amoroTable) {
    ArcticTable arcticTable = getTable(amoroTable);
    List<TransactionsOfTable> transactionsOfTables = new ArrayList<>();
    List<Table> tables = new ArrayList<>();
    if (arcticTable.isKeyedTable()) {
      tables.add(arcticTable.asKeyedTable().changeTable());
      tables.add(arcticTable.asKeyedTable().baseTable());
    } else {
      tables.add(arcticTable.asUnkeyedTable());
    }
    tables.forEach(table -> table.snapshots().forEach(snapshot -> {
      if (snapshot.operation().equals(DataOperations.REPLACE)) {
        return;
      }
      if (snapshot.summary().containsKey(SnapshotSummary.TRANSACTION_BEGIN_SIGNATURE)) {
        return;
      }
      TransactionsOfTable transactionsOfTable = new TransactionsOfTable();
      transactionsOfTable.setTransactionId(snapshot.snapshotId());
      int fileCount = PropertyUtil
          .propertyAsInt(snapshot.summary(), org.apache.iceberg.SnapshotSummary.ADDED_FILES_PROP, 0);
      fileCount += PropertyUtil
          .propertyAsInt(snapshot.summary(), org.apache.iceberg.SnapshotSummary.ADDED_DELETE_FILES_PROP, 0);
      fileCount += PropertyUtil
          .propertyAsInt(snapshot.summary(), org.apache.iceberg.SnapshotSummary.DELETED_FILES_PROP, 0);
      fileCount += PropertyUtil
          .propertyAsInt(snapshot.summary(), org.apache.iceberg.SnapshotSummary.REMOVED_DELETE_FILES_PROP, 0);
      transactionsOfTable.setFileCount(fileCount);
      transactionsOfTable.setFileSize(PropertyUtil
          .propertyAsLong(snapshot.summary(), org.apache.iceberg.SnapshotSummary.ADDED_FILE_SIZE_PROP, 0) +
          PropertyUtil
              .propertyAsLong(snapshot.summary(), org.apache.iceberg.SnapshotSummary.REMOVED_FILE_SIZE_PROP, 0));
      transactionsOfTable.setCommitTime(snapshot.timestampMillis());
      transactionsOfTable.setOperation(snapshot.operation());
      transactionsOfTable.setSummary(snapshot.summary());
      transactionsOfTables.add(transactionsOfTable);
    }));
    transactionsOfTables.sort((o1, o2) -> Long.compare(o2.commitTime, o1.commitTime));
    return transactionsOfTables;
  }

  public List<PartitionFileBaseInfo> getTransactionDetail(AmoroTable<?> amoroTable, long transactionId) {
    ArcticTable arcticTable = getTable(amoroTable);
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    Snapshot snapshot;
    if (arcticTable.isKeyedTable()) {
      snapshot = arcticTable.asKeyedTable().changeTable().snapshot(transactionId);
      if (snapshot == null) {
        snapshot = arcticTable.asKeyedTable().baseTable().snapshot(transactionId);
      }
    } else {
      snapshot = arcticTable.asUnkeyedTable().snapshot(transactionId);
    }
    if (snapshot == null) {
      throw new IllegalArgumentException("unknown snapshot " + transactionId + " of " + amoroTable.id());
    }
    final long snapshotTime = snapshot.timestampMillis();
    String commitId = String.valueOf(transactionId);
    snapshot.addedDataFiles(arcticTable.io()).forEach(f -> result.add(new PartitionFileBaseInfo(
        commitId,
        DataFileType.ofContentId(f.content().id()),
        snapshotTime,
        arcticTable.spec().partitionToPath(f.partition()),
        f.path().toString(),
        f.fileSizeInBytes(),
        "add")));
    snapshot.removedDataFiles(arcticTable.io()).forEach(f -> result.add(new PartitionFileBaseInfo(
        commitId,
        DataFileType.ofContentId(f.content().id()),
        snapshotTime,
        arcticTable.spec().partitionToPath(f.partition()),
        f.path().toString(),
        f.fileSizeInBytes(),
        "remove")));
    snapshot.addedDeleteFiles(arcticTable.io()).forEach(f -> result.add(new PartitionFileBaseInfo(
        commitId,
        DataFileType.ofContentId(f.content().id()),
        snapshotTime,
        arcticTable.spec().partitionToPath(f.partition()),
        f.path().toString(),
        f.fileSizeInBytes(),
        "add")));
    snapshot.removedDeleteFiles(arcticTable.io()).forEach(f -> result.add(new PartitionFileBaseInfo(
        commitId,
        DataFileType.ofContentId(f.content().id()),
        snapshotTime,
        arcticTable.spec().partitionToPath(f.partition()),
        f.path().toString(),
        f.fileSizeInBytes(),
        "remove")));
    return result;
  }

  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) {
    ArcticTable arcticTable = getTable(amoroTable);
    List<DDLInfo> result = new ArrayList<>();
    Table table;
    if (arcticTable.isKeyedTable()) {
      table = arcticTable.asKeyedTable().baseTable();
    } else {
      table = arcticTable.asUnkeyedTable();
    }
    List<HistoryEntry> snapshotLog = ((HasTableOperations) table).operations().current().snapshotLog();
    List<org.apache.iceberg.TableMetadata.MetadataLogEntry> metadataLogEntries =
        ((HasTableOperations) table).operations().current().previousFiles();
    Set<Long> time = new HashSet<>();
    snapshotLog.forEach(e -> time.add(e.timestampMillis()));
    String lastMetadataLogEntryFile = null;
    org.apache.iceberg.TableMetadata lastTableMetadata = null;
    for (int i = 1; i < metadataLogEntries.size(); i++) {
      org.apache.iceberg.TableMetadata.MetadataLogEntry currentEntry = metadataLogEntries.get(i);
      if (!time.contains(currentEntry.timestampMillis())) {
        org.apache.iceberg.TableMetadata.MetadataLogEntry previousEntry = metadataLogEntries.get(i - 1);
        org.apache.iceberg.TableMetadata oldTableMetadata;
        if (lastMetadataLogEntryFile == null || !lastMetadataLogEntryFile.equals(previousEntry.file())) {
          oldTableMetadata = TableMetadataParser.read(table.io(), previousEntry.file());
        } else {
          oldTableMetadata = lastTableMetadata;
        }

        org.apache.iceberg.TableMetadata
            newTableMetadata = TableMetadataParser.read(table.io(), currentEntry.file());
        lastMetadataLogEntryFile = currentEntry.file();
        lastTableMetadata = newTableMetadata;

        DDLInfo.Generator generator = new DDLInfo.Generator();
        result.addAll(generator.tableIdentify(arcticTable.id())
            .oldMeta(oldTableMetadata)
            .newMeta(newTableMetadata)
            .generate());
      }
    }
    if (metadataLogEntries.size() > 0) {
      org.apache.iceberg.TableMetadata.MetadataLogEntry previousEntry = metadataLogEntries
          .get(metadataLogEntries.size() - 1);
      org.apache.iceberg.TableMetadata oldTableMetadata;

      if (lastMetadataLogEntryFile == null || !lastMetadataLogEntryFile.equals(previousEntry.file())) {
        oldTableMetadata = TableMetadataParser.read(table.io(), previousEntry.file());
      } else {
        oldTableMetadata = lastTableMetadata;
      }

      org.apache.iceberg.TableMetadata newTableMetadata = ((HasTableOperations) table).operations().current();
      DDLInfo.Generator generator = new DDLInfo.Generator();
      result.addAll(generator.tableIdentify(arcticTable.id())
          .oldMeta(oldTableMetadata)
          .newMeta(newTableMetadata)
          .generate());
    }
    return result;
  }

  @Override
  public List<PartitionBaseInfo> getTablePartition(AmoroTable<?> amoroTable) {
    ArcticTable arcticTable = getTable(amoroTable);
    if (arcticTable.spec().isUnpartitioned()) {
      return new ArrayList<>();
    }
    Map<String, PartitionBaseInfo> partitionBaseInfoHashMap = new HashMap<>();
    getTableFile(amoroTable, null).forEach(fileInfo -> {
      if (!partitionBaseInfoHashMap.containsKey(fileInfo.getPartition())) {
        partitionBaseInfoHashMap.put(fileInfo.getPartition(), new PartitionBaseInfo());
        partitionBaseInfoHashMap.get(fileInfo.getPartition()).setPartition(fileInfo.getPartition());
      }
      PartitionBaseInfo partitionInfo = partitionBaseInfoHashMap.get(fileInfo.getPartition());
      partitionInfo.setFileCount(partitionInfo.getFileCount() + 1);
      partitionInfo.setFileSize(partitionInfo.getFileSize() + fileInfo.getFileSize());
      partitionInfo.setLastCommitTime(partitionInfo.getLastCommitTime() > fileInfo.getCommitTime() ?
          partitionInfo.getLastCommitTime() :
          fileInfo.getCommitTime());
    });

    return new ArrayList<>(partitionBaseInfoHashMap.values());
  }

  public List<PartitionFileBaseInfo> getTableFile(AmoroTable<?> amoroTable, String partition) {
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

  private List<PartitionFileBaseInfo> collectFileInfo(Table table, boolean isChangeTable, String partition) {
    PartitionSpec spec = table.spec();
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    Table entriesTable = MetadataTableUtils.createMetadataTableInstance(((HasTableOperations) table).operations(),
        table.name(), table.name() + "#ENTRIES",
        MetadataTableType.ENTRIES);
    try (CloseableIterable<Record> manifests = IcebergGenerics.read(entriesTable)
        .where(Expressions.notEqual(ManifestEntryFields.STATUS.name(), ManifestEntryFields.Status.DELETED.id()))
        .build()) {
      for (Record record : manifests) {
        long snapshotId = (long) record.getField(ManifestEntryFields.SNAPSHOT_ID.name());
        GenericRecord dataFile = (GenericRecord) record.getField(ManifestEntryFields.DATA_FILE_FIELD_NAME);
        Integer contentId = (Integer) dataFile.getField(DataFile.CONTENT.name());
        String filePath = (String) dataFile.getField(DataFile.FILE_PATH.name());
        String partitionPath = null;
        GenericRecord parRecord = (GenericRecord) dataFile.getField(DataFile.PARTITION_NAME);
        if (parRecord != null) {
          InternalRecordWrapper wrapper = new InternalRecordWrapper(parRecord.struct());
          partitionPath = spec.partitionToPath(wrapper.wrap(parRecord));
        }
        if (partition != null && spec.isPartitioned() && !partition.equals(partitionPath)) {
          continue;
        }
        Long fileSize = (Long) dataFile.getField(DataFile.FILE_SIZE.name());
        DataFileType dataFileType =
            isChangeTable ? FileNameRules.parseFileTypeForChange(filePath) : DataFileType.ofContentId(contentId);
        long commitTime = -1;
        if (table.snapshot(snapshotId) != null) {
          commitTime = table.snapshot(snapshotId).timestampMillis();
        }
        result.add(new PartitionFileBaseInfo(String.valueOf(snapshotId), dataFileType, commitTime,
            partitionPath, filePath, fileSize));
      }
    } catch (IOException exception) {
      LOG.error("close manifest file error", exception);
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
        if (!PrimaryKeySpec.noPrimaryKey().equals(keyedTable.primaryKeySpec())) {
          changeInfo = TableStatCollector.collectChangeTableInfo(keyedTable);
        }
        baseInfo = TableStatCollector.collectBaseTableInfo(keyedTable);
      } else {
        throw new IllegalStateException("unknown type of table");
      }

      tableBasicInfo.setChangeStatistics(changeInfo);
      tableBasicInfo.setBaseStatistics(baseInfo);
      tableBasicInfo.setTableStatistics(TableStatCollector.union(changeInfo, baseInfo));

      long createTime
          = PropertyUtil.propertyAsLong(table.properties(), TableProperties.TABLE_CREATE_TIME,
          TableProperties.TABLE_CREATE_TIME_DEFAULT);
      if (createTime != TableProperties.TABLE_CREATE_TIME_DEFAULT) {
        if (tableBasicInfo.getTableStatistics() != null) {
          if (tableBasicInfo.getTableStatistics().getSummary() == null) {
            tableBasicInfo.getTableStatistics().setSummary(new HashMap<>());
          } else {
            LOG.warn("{} summary is null", table.id());
          }
          tableBasicInfo.getTableStatistics().getSummary()
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
    serverTableMeta.setPartitionColumnList(table
        .spec()
        .fields()
        .stream()
        .map(item -> AMSPartitionField.buildFromPartitionSpec(table.spec().schema(), item))
        .collect(Collectors.toList()));
    serverTableMeta.setSchema(table
        .schema()
        .columns()
        .stream()
        .map(AMSColumnInfo::buildFromNestedField)
        .collect(Collectors.toList()));

    serverTableMeta.setFilter(null);
    LOG.debug("Table {} is keyedTable: {}", table.name(), table instanceof KeyedTable);
    if (table.isKeyedTable()) {
      KeyedTable kt = table.asKeyedTable();
      if (kt.primaryKeySpec() != null) {
        serverTableMeta.setPkList(kt
            .primaryKeySpec()
            .fields()
            .stream()
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
      ServerTableMeta serverTableMeta,
      Map<String, String> tableProperties) {
    Map<String, String> properties = com.google.common.collect.Maps.newHashMap(tableProperties);
    serverTableMeta.setTableWatermark(properties.remove(TableProperties.WATERMARK_TABLE));
    serverTableMeta.setBaseWatermark(properties.remove(TableProperties.WATERMARK_BASE_STORE));
    serverTableMeta.setCreateTime(PropertyUtil.propertyAsLong(properties, TableProperties.TABLE_CREATE_TIME,
        TableProperties.TABLE_CREATE_TIME_DEFAULT));
    properties.remove(TableProperties.TABLE_CREATE_TIME);

    TableProperties.READ_PROTECTED_PROPERTIES.forEach(properties::remove);
    serverTableMeta.setProperties(properties);
  }

  private ArcticTable getTable(AmoroTable<?> amoroTable) {
    return (ArcticTable) amoroTable.originalTable();
  }
}

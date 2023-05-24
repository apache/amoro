package com.netease.arctic.server.dashboard;

import com.netease.arctic.server.dashboard.model.AMSDataFileInfo;
import com.netease.arctic.server.dashboard.model.BaseMajorCompactRecord;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.FilesStatistics;
import com.netease.arctic.server.dashboard.model.TableOptimizingProcess;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import com.netease.arctic.server.optimizing.MetricsSummary;
import com.netease.arctic.server.optimizing.TaskRuntime;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.trace.SnapshotSummary;
import org.apache.iceberg.DataOperations;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.HistoryEntry;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.util.PropertyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerTableDescriptor extends PersistentBase {

  private final TableService tableService;

  public ServerTableDescriptor(TableService tableService) {
    this.tableService = tableService;
  }

  private ServerTableIdentifier getTable(String catalog, String db, String table) {
    return getAs(TableMetaMapper.class, mapper -> mapper.selectTableIdentifier(catalog, db, table));
  }

  public List<TransactionsOfTable> getTransactions(ServerTableIdentifier tableIdentifier) {
    List<TransactionsOfTable> transactionsOfTables = new ArrayList<>();
    ArcticTable arcticTable = tableService.loadTable(tableIdentifier);
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
      transactionsOfTables.add(transactionsOfTable);
    }));
    Collections.reverse(transactionsOfTables);
    return transactionsOfTables;
  }

  public List<AMSDataFileInfo> getTransactionDetail(ServerTableIdentifier tableIdentifier, long transactionId) {
    List<AMSDataFileInfo> result = new ArrayList<>();
    ArcticTable arcticTable = tableService.loadTable(tableIdentifier);
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
      throw new IllegalArgumentException("unknown snapshot " + transactionId + " of " + tableIdentifier);
    }
    final long snapshotTime = snapshot.timestampMillis();
    snapshot.addedDataFiles(arcticTable.io()).forEach(f -> {
      result.add(new AMSDataFileInfo(
          f.path().toString(),
          arcticTable.spec(), f.partition(),
          f.content(),
          f.fileSizeInBytes(),
          snapshotTime,
          "add"));
    });
    snapshot.removedDataFiles(arcticTable.io()).forEach(f -> {
      result.add(new AMSDataFileInfo(
          f.path().toString(),
          arcticTable.spec(), f.partition(),
          f.content(),
          f.fileSizeInBytes(),
          snapshotTime,
          "remove"));
    });
    snapshot.addedDeleteFiles(arcticTable.io()).forEach(f -> {
      result.add(new AMSDataFileInfo(
          f.path().toString(),
          arcticTable.spec(), f.partition(),
          f.content(),
          f.fileSizeInBytes(),
          snapshotTime,
          "add"));
    });
    snapshot.removedDeleteFiles(arcticTable.io()).forEach(f -> {
      result.add(new AMSDataFileInfo(
          f.path().toString(),
          arcticTable.spec(), f.partition(),
          f.content(),
          f.fileSizeInBytes(),
          snapshotTime,
          "remove"));
    });
    return result;
  }

  public List<DDLInfo> getTableOperations(ServerTableIdentifier tableIdentifier) {
    List<DDLInfo> result = new ArrayList<>();
    ArcticTable arcticTable = tableService.loadTable(tableIdentifier);
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
    for (int i = 1; i < metadataLogEntries.size(); i++) {
      org.apache.iceberg.TableMetadata.MetadataLogEntry e = metadataLogEntries.get(i);
      if (!time.contains(e.timestampMillis())) {
        org.apache.iceberg.TableMetadata
            oldTableMetadata = TableMetadataParser.read(table.io(), metadataLogEntries.get(i - 1).file());
        org.apache.iceberg.TableMetadata
            newTableMetadata = TableMetadataParser.read(table.io(), metadataLogEntries.get(i).file());
        DDLInfo.Generator generator = new DDLInfo.Generator();
        result.addAll(generator.tableIdentify(arcticTable.id())
            .oldMeta(oldTableMetadata)
            .newMeta(newTableMetadata)
            .generate());
      }
    }
    if (metadataLogEntries.size() > 0) {
      org.apache.iceberg.TableMetadata oldTableMetadata = TableMetadataParser.read(
          table.io(),
          metadataLogEntries.get(metadataLogEntries.size() - 1).file());
      org.apache.iceberg.TableMetadata newTableMetadata = ((HasTableOperations) table).operations().current();
      DDLInfo.Generator generator = new DDLInfo.Generator();
      result.addAll(generator.tableIdentify(arcticTable.id())
          .oldMeta(oldTableMetadata)
          .newMeta(newTableMetadata)
          .generate());
    }
    return result;
  }

  public List<BaseMajorCompactRecord> getOptimizeInfo(String catalog, String db, String table) {
    List<TableOptimizingProcess> tableOptimizingProcesses = getAs(
        OptimizingMapper.class,
        mapper -> mapper.selectSuccessOptimizingProcesses(catalog, db, table));
    return tableOptimizingProcesses.stream().map(e -> {
      BaseMajorCompactRecord record = new BaseMajorCompactRecord();
      List<TaskRuntime> taskRuntimes = getAs(
          OptimizingMapper.class,
          mapper -> mapper.selectTaskRuntimes(e.getTableId(), e.getProcessId())).stream()
          .filter(taskRuntime -> TaskRuntime.Status.SUCCESS.equals(taskRuntime.getStatus()))
          .collect(Collectors.toList());
      MetricsSummary metricsSummary = new MetricsSummary(taskRuntimes);
      record.setCommitTime(e.getEndTime());
      record.setPlanTime(e.getPlanTime());
      record.setDuration(e.getEndTime() - e.getPlanTime());
      record.setTableIdentifier(TableIdentifier.of(e.getCatalogName(), e.getDbName(), e.getTableName()));
      record.setOptimizeType(e.getOptimizingType());
      record.setTotalFilesStatBeforeCompact(FilesStatistics.builder()
          .addFiles(metricsSummary.getEqualityDeleteSize(), metricsSummary.getEqDeleteFileCnt())
          .addFiles(metricsSummary.getPositionalDeleteSize(), metricsSummary.getPosDeleteFileCnt())
          .addFiles(metricsSummary.getRewriteDataSize(), metricsSummary.getRewriteDataFileCnt())
          .build());
      record.setTotalFilesStatAfterCompact(FilesStatistics.build(
          metricsSummary.getNewFileCnt(),
          metricsSummary.getNewFileSize()));
      return record;
    }).collect(Collectors.toList());
  }
}

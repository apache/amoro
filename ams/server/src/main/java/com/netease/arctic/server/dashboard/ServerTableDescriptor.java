package com.netease.arctic.server.dashboard;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.OptimizingProcessInfo;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import com.netease.arctic.server.dashboard.utils.FilesStatisticsBuilder;
import com.netease.arctic.server.optimizing.OptimizingProcess;
import com.netease.arctic.server.optimizing.OptimizingProcessMeta;
import com.netease.arctic.server.optimizing.OptimizingTaskMeta;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.relocated.com.google.common.collect.Streams;
import org.apache.paimon.AbstractFileStore;
import org.apache.paimon.Snapshot;
import org.apache.paimon.manifest.FileKind;
import org.apache.paimon.manifest.ManifestEntry;
import org.apache.paimon.manifest.ManifestFile;
import org.apache.paimon.manifest.ManifestFileMeta;
import org.apache.paimon.manifest.ManifestList;
import org.apache.paimon.table.FileStoreTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerTableDescriptor extends PersistentBase {

  private final Map<TableFormat, FormatTableDescriptor> formatDescriptorMap = new HashMap<>();

  private final TableService tableService;

  public ServerTableDescriptor(TableService tableService) {
    this.tableService = tableService;
    FormatTableDescriptor[] formatTableDescriptors = new FormatTableDescriptor[]{
        new MixedAndIcebergTableDescriptor(),
        new PaimonTableDescriptor()
    };
    for (FormatTableDescriptor formatTableDescriptor : formatTableDescriptors) {
      for (TableFormat format : formatTableDescriptor.supportFormat()) {
        formatDescriptorMap.put(format, formatTableDescriptor);
      }
    }
  }

  public ServerTableMeta getTableDetail(ServerTableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableDetail(amoroTable);
  }

  public List<TransactionsOfTable> getTransactions(ServerTableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTransactions(amoroTable);
  }

  public List<PartitionFileBaseInfo> getTransactionDetail(ServerTableIdentifier tableIdentifier, long transactionId) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTransactionDetail(amoroTable, transactionId);
  }

  public List<DDLInfo> getTableOperations(ServerTableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableOperations(amoroTable);
  }

  public List<PartitionBaseInfo> getTablePartition(ServerTableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTablePartition(amoroTable);
  }

  public List<PartitionFileBaseInfo> getTableFile(ServerTableIdentifier tableIdentifier, String partition) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableFile(amoroTable, partition);
  }

  public List<OptimizingProcessMeta> getOptimizingProcesses(String catalog, String db, String table) {
    return getAs(
        OptimizingMapper.class,
        mapper -> mapper.selectOptimizingProcesses(catalog, db, table));
  }

  public List<OptimizingTaskMeta> getOptimizingTasks(List<OptimizingProcessMeta> processMetaList) {
    if (CollectionUtils.isEmpty(processMetaList)) {
      return Collections.emptyList();
    }
    List<Long> processIds = processMetaList.stream()
        .map(OptimizingProcessMeta::getProcessId).collect(Collectors.toList());
    return getAs(
        OptimizingMapper.class,
        mapper -> mapper.selectOptimizeTaskMetas(processIds));
  }

  public List<OptimizingProcessInfo> getPaimonOptimizingProcesses(
      AmoroTable<?> amoroTable, ServerTableIdentifier tableIdentifier) {
    // Temporary solution for Paimon. TODO: Get compaction info from Paimon compaction task
    List<OptimizingProcessInfo> processInfoList = new ArrayList<>();
    FileStoreTable fileStoreTable = (FileStoreTable) amoroTable.originalTable();
    AbstractFileStore<?> store = (AbstractFileStore<?>) fileStoreTable.store();
    try {
      Streams.stream(store.snapshotManager().snapshots())
          .filter(s -> s.commitKind() == Snapshot.CommitKind.COMPACT)
          .forEach(s -> {
            OptimizingProcessInfo optimizingProcessInfo = new OptimizingProcessInfo();
            optimizingProcessInfo.setProcessId(s.id());
            ServerTableIdentifier tableIdentifierWithTableId = getAs(TableMetaMapper.class,
                mapper -> mapper.selectTableIdentifier(tableIdentifier.getCatalog(),
                    tableIdentifier.getDatabase(),
                    tableIdentifier.getTableName()));
            optimizingProcessInfo.setTableId(tableIdentifierWithTableId.getId());
            optimizingProcessInfo.setCatalogName(tableIdentifierWithTableId.getCatalog());
            optimizingProcessInfo.setDbName(tableIdentifierWithTableId.getDatabase());
            optimizingProcessInfo.setTableName(tableIdentifierWithTableId.getTableName());
            optimizingProcessInfo.setStatus(OptimizingProcess.Status.SUCCESS);
            optimizingProcessInfo.setFinishTime(s.timeMillis());
            FilesStatisticsBuilder inputBuilder = new FilesStatisticsBuilder();
            FilesStatisticsBuilder outputBuilder = new FilesStatisticsBuilder();
            ManifestFile manifestFile = store.manifestFileFactory().create();
            ManifestList manifestList = store.manifestListFactory().create();
            List<ManifestFileMeta> manifestFileMetas = s.deltaManifests(manifestList);
            for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
              List<ManifestEntry> compactManifestEntries = manifestFile.read(manifestFileMeta.fileName());
              for (ManifestEntry compactManifestEntry : compactManifestEntries) {
                if (compactManifestEntry.kind() == FileKind.DELETE) {
                  inputBuilder.addFile(compactManifestEntry.file().fileSize());
                } else {
                  outputBuilder.addFile(compactManifestEntry.file().fileSize());
                }
              }
            }
            optimizingProcessInfo.setInputFiles(inputBuilder.build());
            optimizingProcessInfo.setOutputFiles(outputBuilder.build());
            processInfoList.add(optimizingProcessInfo);
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return processInfoList;
  }
}

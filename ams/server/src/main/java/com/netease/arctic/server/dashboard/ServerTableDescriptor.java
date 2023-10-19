package com.netease.arctic.server.dashboard;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import com.netease.arctic.server.optimizing.OptimizingProcessMeta;
import com.netease.arctic.server.optimizing.OptimizingTaskMeta;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableService;
import org.apache.commons.collections.CollectionUtils;

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
    FormatTableDescriptor[] formatTableDescriptors = new FormatTableDescriptor[] {
        new MixedAndIcebergTableDescriptor(),
        new PaimonTableDescriptor()
    };
    for (FormatTableDescriptor formatTableDescriptor : formatTableDescriptors) {
      for (TableFormat format : formatTableDescriptor.supportFormat()) {
        formatDescriptorMap.put(format, formatTableDescriptor);
      }
    }
  }

  public ServerTableMeta getTableDetail(TableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableDetail(amoroTable);
  }

  public List<TransactionsOfTable> getTransactions(TableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTransactions(amoroTable);
  }

  public List<PartitionFileBaseInfo> getTransactionDetail(TableIdentifier tableIdentifier, long transactionId) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTransactionDetail(amoroTable, transactionId);
  }

  public List<DDLInfo> getTableOperations(TableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableOperations(amoroTable);
  }

  public List<PartitionBaseInfo> getTablePartition(TableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTablePartitions(amoroTable);
  }

  public List<PartitionFileBaseInfo> getTableFile(TableIdentifier tableIdentifier, String partition) {
    AmoroTable<?> amoroTable = tableService.loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableFiles(amoroTable, partition);
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
}

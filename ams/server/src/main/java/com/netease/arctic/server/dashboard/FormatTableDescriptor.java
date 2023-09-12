package com.netease.arctic.server.dashboard;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import java.util.List;

public interface FormatTableDescriptor {
  List<TableFormat> supportFormat();

  ServerTableMeta getTableDetail(AmoroTable<?> amoroTable);

  List<TransactionsOfTable> getTransactions(AmoroTable<?> amoroTable);

  List<PartitionFileBaseInfo> getTransactionDetail(AmoroTable<?> amoroTable, long transactionId);

  List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable);

  List<PartitionBaseInfo> getTablePartition(AmoroTable<?> amoroTable);

  List<PartitionFileBaseInfo> getTableFile(AmoroTable<?> amoroTable, String partition);
}
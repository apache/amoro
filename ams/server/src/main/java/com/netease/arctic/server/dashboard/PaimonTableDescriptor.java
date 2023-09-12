package com.netease.arctic.server.dashboard;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.List;

public class PaimonTableDescriptor implements FormatTableDescriptor {
  @Override
  public List<TableFormat> supportFormat() {
    return Lists.newArrayList(TableFormat.PAIMON);
  }

  @Override
  public ServerTableMeta getTableDetail(AmoroTable<?> amoroTable) {
    return null;
  }

  @Override
  public List<TransactionsOfTable> getTransactions(AmoroTable<?> amoroTable) {
    return null;
  }

  @Override
  public List<PartitionFileBaseInfo> getTransactionDetail(AmoroTable<?> amoroTable, long transactionId) {
    return null;
  }

  @Override
  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) {
    return null;
  }

  @Override
  public List<PartitionBaseInfo> getTablePartition(AmoroTable<?> amoroTable) {
    return null;
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFile(AmoroTable<?> amoroTable, String partition) {
    return null;
  }
}

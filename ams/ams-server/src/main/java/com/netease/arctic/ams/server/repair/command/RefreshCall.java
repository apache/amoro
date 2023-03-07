package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.table.TableIdentifier;
import org.apache.thrift.TException;

public class RefreshCall implements CallCommand {

  private ArcticTableMetastore.Iface client;
  private String tablePath;
  private static final String OK_CODE = "OK";

  public RefreshCall(ArcticTableMetastore.Iface client, String tablePath) {
    this.client = client;
    this.tablePath = tablePath;
  }

  @Override
  public String call(Context context) throws TException, FullTableNameException {
    TableIdentifier identifier = CallCommand.fullTableName(context, tablePath);
    client.refreshTable(identifier.buildTableIdentifier());
    return OK_CODE;
  }
}

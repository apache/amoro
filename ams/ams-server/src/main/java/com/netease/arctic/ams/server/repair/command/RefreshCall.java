package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.table.TableIdentifier;
import org.apache.thrift.TException;

public class RefreshCall implements CallCommand {

  private AmsClient client;
  private String tablePath;

  public RefreshCall(AmsClient client, String tablePath) {
    this.client = client;
    this.tablePath = tablePath;
  }

  @Override
  public String call(Context context) throws TException, FullTableNameException {
    TableIdentifier identifier = fullTableName(context, tablePath);
    client.refreshTable(identifier.buildTableIdentifier());
    return "OK";
  }
}

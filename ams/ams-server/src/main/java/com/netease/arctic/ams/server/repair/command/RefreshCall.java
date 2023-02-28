package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.catalog.ArcticCatalog;
import org.apache.thrift.TException;

public class RefreshCall implements CallCommand {

  private AmsClient client;
  private String tablePath;

  public RefreshCall(AmsClient client, String tablePath) {
    this.client = client;
    this.tablePath = tablePath;
  }

  @Override
  public String call(Context context) throws TException {
    com.netease.arctic.table.TableIdentifier tableIdentifier = com.netease.arctic.table.TableIdentifier.of(tablePath);
    if (tableIdentifier.getDatabase() == null) {
      tableIdentifier.setDatabase(context.getDb());
    }
    tableIdentifier.setCatalog(context.getCatalog());
    client.refreshTable(tableIdentifier.buildTableIdentifier());
    return "OK";
  }
}

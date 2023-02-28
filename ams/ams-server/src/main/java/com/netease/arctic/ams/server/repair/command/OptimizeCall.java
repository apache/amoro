package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.OptimizeManager;
import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.table.TableIdentifier;
import java.util.stream.Collectors;
import org.apache.thrift.TException;

public class OptimizeCall implements CallCommand {

  private String tablePath;
  private OptimizeManager.Iface client;
  private Action action;

  public OptimizeCall(OptimizeManager.Iface client, Action action, String tablePath) {
    this.client = client;
    this.action = action;
    this.tablePath = tablePath;
  }

  @Override
  public String call(Context context) throws TException {
    TableIdentifier tableIdentifier = TableIdentifier.of(tablePath);
    if (tableIdentifier.getDatabase() == null) {
      tableIdentifier.setDatabase(context.getDb());
    }
    tableIdentifier.setCatalog(context.getCatalog());

    switch (this.action) {
      case START:
        client.startOptimize(tableIdentifier.buildTableIdentifier());
        return "optimize has started";
      case STOP:
        client.stopOptimize(tableIdentifier.buildTableIdentifier());
        return "optimize has stopped";
      default:
        throw new UnsupportedOperationException("not support optimize operation named:" + this.action);
    }
  }

  public enum Action {
    START, STOP
  }
}

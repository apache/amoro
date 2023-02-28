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
  public String call(Context context) throws TException, FullTableNameException {
    TableIdentifier identifier = CallCommand.fullTableName(context, tablePath);

    switch (this.action) {
      case START:
        client.startOptimize(identifier.buildTableIdentifier());
        return "optimize has started";
      case STOP:
        client.stopOptimize(identifier.buildTableIdentifier());
        return "optimize has stopped";
      default:
        throw new UnsupportedOperationException("Don't support optimize operation named:" + this.action);
    }
  }

  public enum Action {
    START, STOP
  }
}

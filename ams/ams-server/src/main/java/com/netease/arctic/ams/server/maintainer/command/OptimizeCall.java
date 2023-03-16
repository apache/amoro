package com.netease.arctic.ams.server.maintainer.command;

import com.netease.arctic.ams.api.client.OptimizeManagerClient;
import com.netease.arctic.ams.server.maintainer.Context;
import com.netease.arctic.table.TableIdentifier;
import org.apache.thrift.TException;

public class OptimizeCall implements CallCommand {

  private String tablePath;
  private OptimizeManagerClient client;
  private Action action;
  private static final String START_RESULT = "optimize has started";
  private static final String STOP_RESULT = "optimize has stopped";

  public OptimizeCall(OptimizeManagerClient client, Action action, String tablePath) {
    this.client = client;
    this.action = action;
    this.tablePath = tablePath;
  }

  @Override
  public String call(Context context) throws TException, FullTableNameException {
    TableIdentifier identifier = fullTableName(context, tablePath);

    switch (this.action) {
      case START:
        client.startOptimize(identifier.buildTableIdentifier());
        return START_RESULT;
      case STOP:
        client.stopOptimize(identifier.buildTableIdentifier());
        return STOP_RESULT;
      default:
        throw new UnsupportedOperationException("Don't support optimize operation named:" + this.action);
    }
  }

  public enum Action {
    START, STOP
  }
}

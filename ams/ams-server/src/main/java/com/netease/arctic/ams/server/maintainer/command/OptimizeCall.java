package com.netease.arctic.ams.server.maintainer.command;

import com.netease.arctic.ams.api.OptimizeManager;
import com.netease.arctic.table.TableIdentifier;

public class OptimizeCall implements CallCommand {

  private String tablePath;
  private OptimizeManager.Iface entrypoint;
  private Action action;
  private static final String START_RESULT = "optimize has started";
  private static final String STOP_RESULT = "optimize has stopped";

  public OptimizeCall(OptimizeManager.Iface entrypoint, Action action, String tablePath) {
    this.entrypoint = entrypoint;
    this.action = action;
    this.tablePath = tablePath;
  }

  @Override
  public String call(Context context) throws Exception {
    TableIdentifier identifier = fullTableName(context, tablePath);

    switch (this.action) {
      case START:
        entrypoint.startOptimize(identifier.buildTableIdentifier());
        return START_RESULT;
      case STOP:
        entrypoint.stopOptimize(identifier.buildTableIdentifier());
        return STOP_RESULT;
      default:
        throw new UnsupportedOperationException("Don't support optimize operation named:" + this.action);
    }
  }

  public enum Action {
    START, STOP
  }
}

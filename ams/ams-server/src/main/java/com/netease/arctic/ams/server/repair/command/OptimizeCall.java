package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;

public class OptimizeCall implements CallCommand {

  /**
   * Start, Stop
   */
  public enum action {
    START, STOP
  }

  private String tableName;

  @Override
  public String call(Context context) {
    return null;
  }
}

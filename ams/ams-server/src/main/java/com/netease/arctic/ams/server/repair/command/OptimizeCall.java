package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;

public class OptimizeCall implements CallCommand {

  public enum action {
    START, STOP
  }

  private String tableName;

  private action action;

  @Override
  public String call(Context context) {
    return null;
  }
}

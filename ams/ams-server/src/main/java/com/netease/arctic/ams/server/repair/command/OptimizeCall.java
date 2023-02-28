package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;

public class OptimizeCall implements CallCommand {

  private String tableName;

  private action action;

  @Override
  public String call(Context context) {
    return null;
  }
  
  public enum action {
    START, STOP
  }
}

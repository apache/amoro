package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;

public class UseCall implements CallCommand {

  private String tableName;

  public UseCall(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public String call(Context context) {
    return null;
  }
}

package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;

public class ShowCall implements CallCommand{

  /**
   * DATABASES, TABLES
   */
  public enum namespaces {
    DATABASES, TABLES
  }

  @Override
  public String call(Context context) {
    return null;
  }
}

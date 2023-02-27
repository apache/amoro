package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;

public class ShowCall implements CallCommand{

  public enum namespaces {
    DATABASES, TABLES
  }

  private namespaces namespaces;

  @Override
  public String call(Context context) {
    return null;
  }
}

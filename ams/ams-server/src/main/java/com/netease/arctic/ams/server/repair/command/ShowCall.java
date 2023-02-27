package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;

public class ShowCall implements CallCommand{

  private namespaces namespaces;

  @Override
  public String call(Context context) {
    return null;
  }
  
  public enum namespaces {
    DATABASES, TABLES
  }
}

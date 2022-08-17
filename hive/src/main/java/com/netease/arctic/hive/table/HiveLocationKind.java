package com.netease.arctic.hive.table;

import com.netease.arctic.table.LocationKind;

public class HiveLocationKind implements LocationKind {

  public static final LocationKind INSTANT = new HiveLocationKind();

  private HiveLocationKind() {
  }
}

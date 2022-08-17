package com.netease.arctic.hive.utils;

import com.netease.arctic.hive.table.KeyedHiveTable;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.table.ArcticTable;

public class HiveTableUtil {

  public static boolean isHive(ArcticTable arcticTable) {
    return arcticTable instanceof SupportHive;
  }
}

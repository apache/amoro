package com.netease.arctic.utils;

import com.netease.arctic.catalog.BasicIcebergCatalog;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;

public class ArcticTableUtil {
  /**
   * check arctic table is iceberg table format
   * @param arcticTable target arctic table
   * @return Whether iceberg table format
   */
  public static boolean isIcebergTableFormat(ArcticTable arcticTable) {
    return arcticTable instanceof BasicIcebergCatalog.BasicIcebergTable;
  }

  public static UnkeyedTable baseStore(ArcticTable arcticTable) {
    if (arcticTable.isKeyedTable()) {
      return arcticTable.asKeyedTable().baseTable();
    } else {
      return arcticTable.asUnkeyedTable();
    }
  }
}

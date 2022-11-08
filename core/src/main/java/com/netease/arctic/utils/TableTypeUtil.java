package com.netease.arctic.utils;

import com.netease.arctic.catalog.BaseIcebergCatalog;
import com.netease.arctic.table.ArcticTable;

/**
 * Used for check arctic table type
 */
public class TableTypeUtil {
  /**
   * check arctic table is native iceberg table
   * @param arcticTable target arctic table
   * @return Whether native iceberg table. true is native iceberg table, false isn't native iceberg table.
   */
  public static boolean isNativeIceberg(ArcticTable arcticTable) {
    return arcticTable instanceof BaseIcebergCatalog.BaseIcebergTable;
  }
}

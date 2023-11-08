package com.netease.arctic.utils;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;

public class ArcticTableUtil {

  /** Return the base store of the arctic table. */
  public static UnkeyedTable baseStore(ArcticTable arcticTable) {
    if (arcticTable.isKeyedTable()) {
      return arcticTable.asKeyedTable().baseTable();
    } else {
      return arcticTable.asUnkeyedTable();
    }
  }

  /** Return the table root location of the arctic table. */
  public static String tableRootLocation(ArcticTable arcticTable) {
    String tableRootLocation;
    if (TableFormat.ICEBERG != arcticTable.format() && arcticTable.isUnkeyedTable()) {
      tableRootLocation = TableFileUtil.getFileDir(arcticTable.location());
    } else {
      tableRootLocation = arcticTable.location();
    }
    return tableRootLocation;
  }
}

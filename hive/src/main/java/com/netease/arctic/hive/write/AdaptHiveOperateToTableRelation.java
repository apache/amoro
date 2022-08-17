package com.netease.arctic.hive.write;

import com.netease.arctic.hive.table.HiveLocationKind;
import com.netease.arctic.hive.table.KeyedHiveTable;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseLocationKind;
import com.netease.arctic.table.ChangeLocationKind;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.OperateKinds;

public class AdaptHiveOperateToTableRelation implements OperateToTableRelation {

  public static final AdaptHiveOperateToTableRelation INSTANT = new AdaptHiveOperateToTableRelation();

  @Override
  public LocationKind getLocationKindsFromOperateKind(
      ArcticTable arcticTable, OperateKinds operateKinds) {
    if (arcticTable.isKeyedTable()) {
      if (arcticTable instanceof KeyedHiveTable) {
        switch (operateKinds) {
          case APPEND:
            return ChangeLocationKind.INSTANT;
          case MINOR_OPTIMIZE:
          case MAJOR_OPTIMIZE:
            return BaseLocationKind.INSTANT;
          case OVERWRITE:
          case FULL_OPTIMIZE:
            return HiveLocationKind.INSTANT;
        }
      } else {
        switch (operateKinds) {
          case APPEND:
            return ChangeLocationKind.INSTANT;
          case MINOR_OPTIMIZE:
          case MAJOR_OPTIMIZE:
          case OVERWRITE:
          case FULL_OPTIMIZE:
            return BaseLocationKind.INSTANT;
        }
      }
    } else {
      if (arcticTable instanceof UnkeyedHiveTable) {
        switch (operateKinds) {
          case APPEND:
          case MAJOR_OPTIMIZE:
            return BaseLocationKind.INSTANT;
          case OVERWRITE:
          case FULL_OPTIMIZE:
            return HiveLocationKind.INSTANT;
        }
      } else {
        return BaseLocationKind.INSTANT;
      }
    }
    return null;
  }
}

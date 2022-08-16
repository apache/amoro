package com.netease.arctic.hive.write;

import com.netease.arctic.hive.table.KeyedHiveTable;
import com.netease.arctic.hive.table.TableKinds;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.OperateKinds;

import static com.netease.arctic.hive.table.TableKinds.BASE;
import static com.netease.arctic.hive.table.TableKinds.CHANGE;
import static com.netease.arctic.hive.table.TableKinds.HIVE;

public class CommonOperateToTableRelation implements OperateToTableRelation{

  @Override
  public TableKinds getTableKindsFromOperateKind(
      ArcticTable arcticTable, OperateKinds operateKinds) {
    if (arcticTable.isKeyedTable()){
      if (arcticTable instanceof KeyedHiveTable) {
        switch (operateKinds){
          case APPEND:
            return CHANGE;
          case MINOR_OPTIMIZE:
          case MAJOR_OPTIMIZE:
            return BASE;
          case OVERWRITE:
          case FULL_OPTIMIZE:
            return HIVE;
        }
      } else {
        switch (operateKinds){
          case APPEND:
            return CHANGE;
          case MINOR_OPTIMIZE:
          case MAJOR_OPTIMIZE:
          case OVERWRITE:
          case FULL_OPTIMIZE:
            return BASE;
        }
      }
    } else {
      if (arcticTable instanceof UnkeyedHiveTable){
        switch (operateKinds){
          case APPEND:
          case MAJOR_OPTIMIZE:
            return BASE;
          case OVERWRITE:
          case FULL_OPTIMIZE:
            return HIVE;
        }
      } else {
        return BASE;
      }
    }
    return null;
  }
}

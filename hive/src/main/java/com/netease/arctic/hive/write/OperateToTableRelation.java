package com.netease.arctic.hive.write;

import com.netease.arctic.hive.table.TableKinds;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.OperateKinds;

public interface OperateToTableRelation {

  TableKinds getTableKindsFromOperateKind(ArcticTable arcticTable, OperateKinds operateKinds);
}

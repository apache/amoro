package com.netease.arctic.hive.write;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.OperateKinds;

public interface OperateToTableRelation {

  LocationKind getLocationKindsFromOperateKind(ArcticTable arcticTable, OperateKinds operateKinds);
}

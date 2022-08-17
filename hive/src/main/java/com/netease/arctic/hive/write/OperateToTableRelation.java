package com.netease.arctic.hive.write;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.OperateKinds;

/**
 * Resolve the relation between {@link OperateKinds} and {@link LocationKind}
 */
public interface OperateToTableRelation {

  LocationKind getLocationKindsFromOperateKind(ArcticTable arcticTable, OperateKinds operateKinds);
}

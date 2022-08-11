package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.table.ArcticTable;

import java.util.List;
import java.util.Map;

public class AdaptHiveCommit extends BaseOptimizeCommit {
  public AdaptHiveCommit(ArcticTable arcticTable, Map<String, List<OptimizeTaskItem>> optimizeTasksToCommit) {
    super(arcticTable, optimizeTasksToCommit);
  }

  @Override
  public long commit(TableOptimizeRuntime tableOptimizeRuntime) throws Exception {
    super.commit(tableOptimizeRuntime);

    // for keyed table, alter table/partition location
    if (arcticTable.isKeyedTable()) {

    } else {
      // for unKeyed table, move new files to table/partition location

    }

    return System.currentTimeMillis();
  }
}

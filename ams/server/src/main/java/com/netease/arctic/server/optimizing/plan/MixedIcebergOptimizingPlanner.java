package com.netease.arctic.server.optimizing.plan;

import com.google.common.base.Preconditions;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.server.table.TableRuntime;

public class MixedIcebergOptimizingPlanner extends OptimizingPlanner {

  private String hiveLocation;

  public MixedIcebergOptimizingPlanner(TableRuntime tableRuntime, double availableCore) {
    super(tableRuntime, availableCore);
  }

  protected AbstractPartitionPlan buildEvaluator(String partitionPath) {
    if (TableTypeUtil.isHive(arcticTable)) {
      return new MixedHivePartitionPlan(tableRuntime, arcticTable, partitionPath, getHiveLocation());
    } else {
      return new MixedIcebergPartitionPlan(tableRuntime, arcticTable, partitionPath);
    }
  }

  private String getHiveLocation() {
    if (this.hiveLocation == null) {
      Preconditions.checkArgument(TableTypeUtil.isHive(arcticTable), "The table not support hive");
      this.hiveLocation = (((SupportHive) arcticTable).hiveLocation());
    }
    return this.hiveLocation;
  }
}

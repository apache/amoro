package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.server.table.TableRuntime;

public class MixedIcebergOptimizingPlanner extends OptimizingPlanner {
  public MixedIcebergOptimizingPlanner(TableRuntime tableRuntime, double availableCore) {
    super(tableRuntime, availableCore);
  }

  protected AbstractPartitionPlan buildEvaluator(String partitionPath) {
    return new MixedIcebergPartitionPlan(tableRuntime, arcticTable, partitionPath);
  }
}

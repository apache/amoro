package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.utils.SequenceNumberFetcher;

public class IcebergOptimizingPlanner extends OptimizingPlanner {
  private SequenceNumberFetcher sequenceNumberFetcher;
  public IcebergOptimizingPlanner(TableRuntime tableRuntime, double availableCore) {
    super(tableRuntime, availableCore);
    this.sequenceNumberFetcher = new SequenceNumberFetcher(arcticTable.asUnkeyedTable(), targetSnapshotId);
  }

  protected AbstractPartitionPlan buildEvaluator(String partitionPath) {
    return new IcebergPartitionPlan(tableRuntime, partitionPath, arcticTable, sequenceNumberFetcher);
  }
}

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OptimizePlan {
  long getCurrentSnapshotId();

  long getCurrentChangeSnapshotId();

  Set<String> getCurrentPartitions();

  List<BaseOptimizeTask> plan();

  Map<String, OptimizeType> getPartitionOptimizeType();
}

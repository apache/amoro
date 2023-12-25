package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.process.OptimizingStage;

public enum OptimizingType {
  MINOR(OptimizingStage.MINOR_OPTIMIZING),
  MAJOR(OptimizingStage.MAJOR_OPTIMIZING),
  FULL(OptimizingStage.FULL_OPTIMIZING);

  private final OptimizingStage status;

  OptimizingType(OptimizingStage status) {
    this.status = status;
  }

  public OptimizingStage getStatus() {
    return status;
  }
}

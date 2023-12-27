package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.process.OptimizingStage;

public enum OptimizingType {
  MINOR(OptimizingStage.MINOR_OPTIMIZING, Action.MINOR_OPTIMIZING),
  MAJOR(OptimizingStage.MAJOR_OPTIMIZING, Action.MAJOR_OPTIMIZING),
  FULL(OptimizingStage.FULL_OPTIMIZING, Action.MAJOR_OPTIMIZING);

  private final OptimizingStage status;
  private final Action action;

  OptimizingType(OptimizingStage status, Action action) {
    this.status = status;
    this.action = action;
  }

  public OptimizingStage getStatus() {
    return status;
  }

  public Action getAction() {
    return action;
  }
}

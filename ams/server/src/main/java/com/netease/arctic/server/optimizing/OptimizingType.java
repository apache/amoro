package com.netease.arctic.server.optimizing;

public enum OptimizingType {
  MINOR(OptimizingStatus.MINOR_OPTIMIZING),
  MAJOR(OptimizingStatus.MAJOR_OPTIMIZING),
  FULL(OptimizingStatus.FULL_OPTIMIZING);

  private final OptimizingStatus status;

  OptimizingType(OptimizingStatus status) {
    this.status = status;
  }

  public OptimizingStatus getStatus() {
    return status;
  }
}


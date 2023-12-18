package com.netease.arctic.server.process.optimizing;

public enum OptimizingStage {
  FULL_OPTIMIZING("full", true),
  MAJOR_OPTIMIZING("major", true),
  MINOR_OPTIMIZING("minor", true),
  COMMITTING("committing", true),
  PLANNING("planning", false),
  PENDING("pending", false),
  IDLE("idle", false);
  private final String displayValue;

  private final boolean isProcessing;

  OptimizingStage(String displayValue, boolean isProcessing) {
    this.displayValue = displayValue;
    this.isProcessing = isProcessing;
  }

  public boolean isOptimizing() {
    return isProcessing;
  }

  public String displayValue() {
    return displayValue;
  }
}

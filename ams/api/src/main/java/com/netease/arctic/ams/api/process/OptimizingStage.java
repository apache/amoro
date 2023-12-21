package com.netease.arctic.ams.api.process;

public enum OptimizingStage {
  FULL_OPTIMIZING("full", true),
  MAJOR_OPTIMIZING("major", true),
  MINOR_OPTIMIZING("minor", true),
  COMMITTING("committing", true),
  PLANNING("planning", false),
  PENDING("pending", false),
  IDLE("idle", false),
  SUSPENDING("suspending", false),
  SUBMITTING("submitting", false);

  private final String displayValue;

  private final boolean isOptimizing;

  OptimizingStage(String displayValue, boolean isProcessing) {
    this.displayValue = displayValue;
    this.isOptimizing = isProcessing;
  }

  public boolean isOptimizing() {
    return isOptimizing;
  }

  public String displayValue() {
    return displayValue;
  }
}

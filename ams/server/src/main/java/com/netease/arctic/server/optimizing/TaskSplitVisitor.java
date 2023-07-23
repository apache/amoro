package com.netease.arctic.server.optimizing;

public class TaskSplitVisitor {
  private final double availableCore;
  private final int parallelism;
  private long actualInputSize;

  private TaskSplitVisitor(double availableCore, int parallelism) {
    this.availableCore = availableCore;
    this.parallelism = parallelism;
  }

  public double getAvailableCore() {
    return availableCore;
  }

  public int getParallelism() {
    return parallelism;
  }

  public void setActualInputSize(long actualInputSize) {
    this.actualInputSize = actualInputSize;
  }

  public int calculateAvgThreadSize() {
    double avgThreadCost = actualInputSize / availableCore;
    return (int) (actualInputSize / avgThreadCost);
  }

  public static class Builder {
    private double availableCore;
    private int parallelism;

    Builder setAvailableCore(double availableCore) {
      this.availableCore = availableCore;
      return this;
    }

    Builder setParallelism(int parallelism) {
      this.parallelism = parallelism;
      return this;
    }

    TaskSplitVisitor build() {
      return new TaskSplitVisitor(availableCore, parallelism);
    }
  }
}

package com.netease.arctic.server.optimizing;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.netease.arctic.table.TableProperties;

public class TaskSplitVisitor {
  private final double availableCore;
  private final int parallelism;
  private long actualInputSize;

  private TaskSplitVisitor(double availableCore, int parallelism) {
    this.availableCore = availableCore;
    this.parallelism = parallelism;
  }

  @VisibleForTesting
  public static TaskSplitVisitor asDefault() {
    return new TaskSplitVisitor(TableProperties.SELF_OPTIMIZING_QUOTA_DEFAULT, 1);
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
      Preconditions.checkArgument(availableCore != 0, "Available core must be positive");
      Preconditions.checkArgument(parallelism != 0, "Available core must be positive");
      return new TaskSplitVisitor(availableCore, parallelism);
    }
  }
}

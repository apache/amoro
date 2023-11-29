package com.netease.arctic.server.optimizing;

public interface OptimizingProcess {

  long getProcessId();

  void close();

  void failed(String failedReason, long endTime);

  boolean isClosed();

  long getTargetSnapshotId();

  long getTargetChangeSnapshotId();

  long getPlanTime();

  long getDuration();

  OptimizingType getOptimizingType();

  Status getStatus();

  long getRunningQuotaTime(long calculatingStartTime, long calculatingEndTime);

  void commit();

  MetricsSummary getSummary();

  enum Status {
    PLANNING,
    RUNNING,
    CLOSED,
    SUCCESS,
    FAILED
  }
}

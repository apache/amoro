package com.netease.arctic.ams.server.optimizing;

import com.netease.arctic.table.ArcticTable;

public interface OptimizingProcess {

  long getProcessId();

  void close();

  boolean isClosed();

  long getTargetSnapshotId();

  long getPlanTime();

  long getDuration();

  OptimizingType getOptimizingType();

  Status getStatus();

  long getQuotaTime(long calculatingStartTime, long calculatingEndTime);

  void commit();

  MetricsSummary getSummary();

  enum Status {
    RUNNING,
    CLOSED,
    SUCCESS,
    FAILED;
  }
}

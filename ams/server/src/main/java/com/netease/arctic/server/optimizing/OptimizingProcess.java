package com.netease.arctic.server.optimizing;

import com.netease.arctic.TableSnapshot;

public interface OptimizingProcess {

  long getProcessId();

  void close();

  boolean isClosed();

  TableSnapshot getFromSnapshot();

  long getPlanTime();

  long getDuration();

  OptimizingType getOptimizingType();

  Status getStatus();

  long getRunningQuotaTime(long calculatingStartTime, long calculatingEndTime);

  void commit();

  MetricsSummary getSummary();

  enum Status {
    RUNNING,
    CLOSED,
    SUCCESS,
    FAILED;
  }
}

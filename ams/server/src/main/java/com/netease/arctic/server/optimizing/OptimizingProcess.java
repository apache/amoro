package com.netease.arctic.server.optimizing;

import java.util.Map;

public interface OptimizingProcess {

  long getProcessId();

  void close();

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

  Map<String, Long> getFromSequence();

  Map<String, Long> getToSequence();

  enum Status {
    RUNNING,
    CLOSED,
    SUCCESS,
    FAILED;
  }
}

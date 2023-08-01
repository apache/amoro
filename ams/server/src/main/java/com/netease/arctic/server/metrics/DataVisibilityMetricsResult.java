package com.netease.arctic.server.metrics;

import com.google.common.base.Preconditions;
import org.apache.iceberg.metrics.TimerResult;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
public interface DataVisibilityMetricsResult {

  TimerResult tableDataVisibilityLatency();

  @Nullable
  TimerResult hiveDataVisibilityLatency();

  static DataVisibilityMetricsResult from(DataVisibilityMetrics metrics) {
    Preconditions.checkNotNull(metrics, "Invalid data visibility metrics: null");
    ImmutableDataVisibilityMetricsResult.Builder builder = ImmutableDataVisibilityMetricsResult.builder();
    builder.tableDataVisibilityLatency(TimerResult.fromTimer(metrics.tableDataVisibilityLatency()));
    if (metrics.hiveDataVisibilityLatency() != null) {
      builder.hiveDataVisibilityLatency(TimerResult.fromTimer(metrics.hiveDataVisibilityLatency()));
    }
    return builder.build();
  }
}

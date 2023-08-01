package com.netease.arctic.server.metrics;

import com.google.common.base.Preconditions;
import org.apache.iceberg.metrics.TimerResult;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
public interface OptimizingMetricsResult {

  @Nullable
  TimerResult taskTotalCostTime();

  @Nullable
  TimerResult planDuration();

  @Nullable
  TimerResult statusDuration();

  static OptimizingMetricsResult from(OptimizingMetrics metrics) {
    Preconditions.checkNotNull(metrics, "Invalid optimizing metrics: null");
    ImmutableOptimizingMetricsResult.Builder builder = ImmutableOptimizingMetricsResult.builder();
    if (metrics.taskTotalCostTime() != null) {
      builder.taskTotalCostTime(TimerResult.fromTimer(metrics.taskTotalCostTime()));
    }
    if (metrics.planDuration() != null) {
      builder.planDuration(TimerResult.fromTimer(metrics.planDuration()));
    }
    if (metrics.statusDuration() != null) {
      builder.statusDuration(TimerResult.fromTimer(metrics.statusDuration()));
    }
    return builder.build();
  }
}

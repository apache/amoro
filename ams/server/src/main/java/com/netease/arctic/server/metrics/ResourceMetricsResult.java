package com.netease.arctic.server.metrics;

import com.google.common.base.Preconditions;
import org.apache.iceberg.metrics.CounterResult;
import org.immutables.value.Value;

@Value.Immutable
public interface ResourceMetricsResult {

  CounterResult optimizerExecutionOccupy();

  static ResourceMetricsResult from(ResourceMetrics metrics) {
    Preconditions.checkNotNull(metrics, "Invalid resource metrics: null");
    return ImmutableResourceMetricsResult.builder()
        .optimizerExecutionOccupy(CounterResult.fromCounter(metrics.quotaTime()))
        .build();
  }
}

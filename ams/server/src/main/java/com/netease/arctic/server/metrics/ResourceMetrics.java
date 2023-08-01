package com.netease.arctic.server.metrics;

import org.apache.iceberg.metrics.Counter;
import org.apache.iceberg.metrics.MetricsContext;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ResourceMetrics {
  public static final String QUOTA_TIME = "quota-time";

  public abstract MetricsContext metricsContext();

  @Value.Derived
  public Counter quotaTime() {
    return metricsContext().counter(QUOTA_TIME);
  }

  public static ResourceMetrics of(MetricsContext metricsContext) {
    return ImmutableResourceMetrics.builder().metricsContext(metricsContext).build();
  }
}

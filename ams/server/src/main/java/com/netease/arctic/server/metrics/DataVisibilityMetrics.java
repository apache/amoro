package com.netease.arctic.server.metrics;

import org.apache.iceberg.metrics.MetricsContext;
import org.apache.iceberg.metrics.Timer;
import org.immutables.value.Value;

import java.util.concurrent.TimeUnit;

@Value.Immutable
public abstract class DataVisibilityMetrics {
  public static final String TABLE_DATA_VISIBILITY_LATENESS = "table-data-visibility-lateness-second";
  public static final String HIVE_DATA_VISIBILITY_LATENESS = "hive-data-visibility-lateness-second";

  public abstract MetricsContext metricsContext();

  @Value.Derived
  public Timer tableDataVisibilityLatency() {
    return metricsContext().timer(TABLE_DATA_VISIBILITY_LATENESS, TimeUnit.SECONDS);
  }

  @Value.Derived
  public Timer hiveDataVisibilityLatency() {
    return metricsContext().timer(HIVE_DATA_VISIBILITY_LATENESS, TimeUnit.SECONDS);
  }

  public static DataVisibilityMetrics of(MetricsContext metricsContext) {
    return ImmutableDataVisibilityMetrics.builder().metricsContext(metricsContext).build();
  }
}

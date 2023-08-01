package com.netease.arctic.server.metrics;

import org.apache.iceberg.metrics.MetricsContext;
import org.apache.iceberg.metrics.Timer;
import org.immutables.value.Value;

import java.util.concurrent.TimeUnit;

@Value.Immutable
public abstract class OptimizingMetrics {
  public static final String TABLE_OPTIMIZING_COST_TIME_TOTAL = "table-optimizing-total-cost-time-second";
  public static final String TABLE_OPTIMIZING_PLAN_TIME = "table-optimizing-plan-duration-second";
  public static final String TABLE_OPTIMIZING_IDLE_TIME = "table-optimizing-idle-duration-second";
  public static final String TABLE_OPTIMIZING_PENDING_TIME = "table-optimizing-pending-duration-second";
  public static final String TABLE_OPTIMIZING_COMMIT_TIME = "table-optimizing-commit-duration-second";
  private static final String STATUS_DURATION = "status-duration-second";

  public abstract MetricsContext metricsContext();

  @Value.Derived
  public Timer taskTotalCostTime() {
    return metricsContext().timer(TABLE_OPTIMIZING_COST_TIME_TOTAL, TimeUnit.SECONDS);
  }

  @Value.Derived
  public Timer planDuration() {
    return metricsContext().timer(TABLE_OPTIMIZING_PLAN_TIME, TimeUnit.SECONDS);
  }

  @Value.Derived
  public Timer statusDuration() {
    return metricsContext().timer(STATUS_DURATION, TimeUnit.SECONDS);
  }

  public static DataVisibilityMetrics of(MetricsContext metricsContext) {
    return ImmutableDataVisibilityMetrics.builder().metricsContext(metricsContext).build();
  }
}

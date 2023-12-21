package com.netease.arctic.server.table;

import static com.netease.arctic.ams.api.metrics.MetricDefine.defineGauge;

import com.netease.arctic.ams.api.metrics.*;
import com.netease.arctic.server.metrics.MetricRegistry;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.List;

public class TableMetrics {
  /** Table is no need optimizing. */
  public static final String STATE_IDLE = "idle";

  /** Table is need optimizing, but waiting for resource */
  public static final String STATE_PENDING = "pending";

  /** Table is doing optimizing process planing. */
  public static final String STATE_PLANING = "planing";

  /** Table is executing optimizing process */
  public static final String STATE_EXECUTING = "executing";

  /** All optimizing process task is done, and process is committing. */
  public static final String STATE_COMMITTING = "committing";

  // table optimizing status duration metrics
  public static final MetricDefine TABLE_OPTIMIZING_STATE_IDLE_DURATION =
      defineGauge("table_optimizing_status_idle_duration_seconds")
          .withDescription("Duration in seconds after table be in idle state")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATE_PENDING_DURATION =
      defineGauge("table_optimizing_status_pending_duration_seconds")
          .withDescription("Duration in seconds after table be in pending state")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATE_PLANNING_DURATION =
      defineGauge("table_optimizing_status_planning_duration_seconds")
          .withDescription("Duration in seconds after table be in planning state")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATE_EXECUTING_DURATION =
      defineGauge("table_optimizing_status_executing_duration_seconds")
          .withDescription("Duration in seconds after table be in executing state")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATE_COMMITTING_DURATION =
      defineGauge("table_optimizing_status_committing_duration_seconds")
          .withDescription("Duration in seconds after table be in committing state")
          .withTags("catalog", "database", "table")
          .build();

  private final ServerTableIdentifier identifier;

  private String state = STATE_IDLE;
  private long stateSetTimestamp = System.currentTimeMillis();
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();
  private boolean register = false;

  public TableMetrics(ServerTableIdentifier identifier) {
    this.identifier = identifier;
  }

  private void registerMetric(MetricRegistry registry, MetricDefine define, Metric metric) {
    MetricKey key =
        registry.register(
            define,
            ImmutableList.of(
                identifier.getCatalog(), identifier.getDatabase(), identifier.getTableName()),
            metric);
    registeredMetricKeys.add(key);
  }

  public void register(MetricRegistry registry) {
    if (!register) {
      registerMetric(
          registry, TABLE_OPTIMIZING_STATE_IDLE_DURATION, new StateDurationGauge(STATE_IDLE));
      registerMetric(
          registry, TABLE_OPTIMIZING_STATE_PENDING_DURATION, new StateDurationGauge(STATE_PENDING));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_STATE_PLANNING_DURATION,
          new StateDurationGauge(STATE_PLANING));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_STATE_EXECUTING_DURATION,
          new StateDurationGauge(STATE_EXECUTING));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_STATE_COMMITTING_DURATION,
          new StateDurationGauge(STATE_COMMITTING));
      register = true;
    }
  }

  public void unregister(MetricRegistry registry) {
    registeredMetricKeys.forEach(registry::unregister);
    registeredMetricKeys.clear();
  }

  public void stateChanged(OptimizingStatus state, long stateSetTimestamp) {
    this.state = state.name();
    this.stateSetTimestamp = stateSetTimestamp;
  }

  class StateDurationGauge implements Gauge<Integer> {
    final String targetState;

    StateDurationGauge(String targetState) {
      this.targetState = targetState;
    }

    @Override
    public Integer getValue() {
      if (targetState.equals(state)) {
        return stateDuration();
      }
      return 0;
    }

    private Integer stateDuration() {
      return (int) ((System.currentTimeMillis() - stateSetTimestamp) / 1000);
    }
  }
}

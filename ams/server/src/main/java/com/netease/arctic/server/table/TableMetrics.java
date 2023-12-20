package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.metrics.*;
import com.netease.arctic.server.metrics.MetricRegistry;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;

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

  private final ServerTableIdentifier identifier;

  private String state = STATE_IDLE;
  private long stateSetTimestamp = System.currentTimeMillis();

  private boolean register = false;
  private final MetricRegistry tableMetricRegistry = new MetricRegistry();

  public TableMetrics(ServerTableIdentifier identifier) {
    this.identifier = identifier;
    createOptimizingMetric(
        MetricDefines.TABLE_OPTIMIZING_STATE_IDLE_DURATION, new StateDurationGauge(STATE_IDLE));
    createOptimizingMetric(
        MetricDefines.TABLE_OPTIMIZING_STATE_PENDING_DURATION,
        new StateDurationGauge(STATE_PENDING));
    createOptimizingMetric(
        MetricDefines.TABLE_OPTIMIZING_STATE_PLANNING_DURATION,
        new StateDurationGauge(STATE_PLANING));
    createOptimizingMetric(
        MetricDefines.TABLE_OPTIMIZING_STATE_EXECUTING_DURATION,
        new StateDurationGauge(STATE_EXECUTING));
    createOptimizingMetric(
        MetricDefines.TABLE_OPTIMIZING_STATE_COMMITTING_DURATION,
        new StateDurationGauge(STATE_COMMITTING));
  }

  private void createOptimizingMetric(MetricDefine define, Metric metric) {
    tableMetricRegistry.register(
        define,
        ImmutableList.of(
            identifier.getCatalog(), identifier.getDatabase(), identifier.getTableName()),
        metric);
  }

  public void register(MetricRegistry registry) {
    if (!register) {
      registry.registerAll(tableMetricRegistry);
      register = true;
    }
  }

  public void unregister(MetricRegistry registry) {
    tableMetricRegistry.getMetrics().keySet().forEach(registry::unregister);
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

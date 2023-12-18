package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.metrics.Counter;
import com.netease.arctic.ams.api.metrics.Gauge;
import com.netease.arctic.ams.api.metrics.Metric;
import com.netease.arctic.ams.api.metrics.MetricDefine;
import com.netease.arctic.ams.api.metrics.MetricType;
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

  private final Counter processTotalCount = new Counter();
  private final Counter processFailedCount = new Counter();

  private String state = STATE_IDLE;
  private long stateSetTimestamp = System.currentTimeMillis();

  private boolean register = false;
  private MetricRegistry tableMetricRegistry = new MetricRegistry();

  private final Gauge<Integer> idleDuration = new StateDurationGauge(STATE_IDLE);
  private final Gauge<Integer> pendingDuration = new StateDurationGauge(STATE_PENDING);
  private final Gauge<Integer> planDuration = new StateDurationGauge(STATE_PLANING);
  private final Gauge<Integer> executingDuration = new StateDurationGauge(STATE_EXECUTING);
  private final Gauge<Integer> committingDuration = new StateDurationGauge(STATE_COMMITTING);

  public TableMetrics(ServerTableIdentifier identifier) {
    this.identifier = identifier;
    createMetric(
        "table_optimizing_process_total_count",
        processFailedCount,
        "Total process  count after AMS started.");
    createMetric(
        "table_optimizing_process_failed_count",
        processTotalCount,
        "Total process failed count after AMS started.");
    createMetric(
        "table_optimizing_status_idle_duration_seconds",
        idleDuration,
        "Duration in seconds after table be in idle state");
    createMetric(
        "table_optimizing_status_pending_duration_seconds",
        pendingDuration,
        "Duration in seconds after table be in pending state");
    createMetric(
        "table_optimizing_status_planning_duration_seconds",
        planDuration,
        "Duration in seconds after table be in planning state");
    createMetric(
        "table_optimizing_status_executing_duration_seconds",
        executingDuration,
        "Duration in seconds after table be in executing state");
    createMetric(
        "table_optimizing_status_committing_duration_seconds",
        committingDuration,
        "Duration in seconds after table be in committing state");
  }

  private void createMetric(String name, Metric metric, String description) {
    MetricType metricType = MetricType.ofType(metric);

    MetricDefine define =
        tableMetricRegistry.defineMetric(
            name, metricType, description, "catalog", "database", "table_name");

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

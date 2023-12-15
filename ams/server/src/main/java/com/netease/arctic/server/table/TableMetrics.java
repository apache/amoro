package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.metrics.Counter;
import com.netease.arctic.ams.api.metrics.Gauge;
import com.netease.arctic.ams.api.metrics.MetricName;
import com.netease.arctic.server.metrics.MetricRegistry;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.table.ServerTableIdentifier;
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
    tableMetricRegistry.register(explicitMetricName("table_optimizing_process_total_count"), processFailedCount);
    tableMetricRegistry.register(explicitMetricName("table_optimizing_process_failed_count"), processTotalCount);
    tableMetricRegistry.register(explicitMetricName("table_optimizing_status_idle_duration_seconds"), idleDuration);
    tableMetricRegistry.register(explicitMetricName("table_optimizing_status_pending_duration_seconds"), pendingDuration);
    tableMetricRegistry.register(explicitMetricName("table_optimizing_status_planning_duration_seconds"), planDuration);
    tableMetricRegistry.register(explicitMetricName("table_optimizing_status_executing_duration_seconds"), executingDuration);
    tableMetricRegistry.register(explicitMetricName("table_optimizing_status_committing_duration_seconds"), committingDuration);
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


  private MetricName explicitMetricName(String name) {
    return new MetricName(name, ImmutableList.of(
        "catalog", "database", "table_name"
    ), ImmutableList.of(
        identifier.getCatalog(), identifier.getDatabase(), identifier.getTableName()
    ));
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

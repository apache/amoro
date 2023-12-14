package com.netease.arctic.server.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.metrics.MetricName;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;

import static com.netease.arctic.ams.api.events.TableOptimizingStateChangedEventContent.STATE_COMMITTING;
import static com.netease.arctic.ams.api.events.TableOptimizingStateChangedEventContent.STATE_EXECUTING;
import static com.netease.arctic.ams.api.events.TableOptimizingStateChangedEventContent.STATE_IDLE;
import static com.netease.arctic.ams.api.events.TableOptimizingStateChangedEventContent.STATE_PENDING;
import static com.netease.arctic.ams.api.events.TableOptimizingStateChangedEventContent.STATE_PLANING;

public class TableMetrics {

  private final TableIdentifier identifier;

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

  public TableMetrics(TableIdentifier identifier) {
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
    tableMetricRegistry.getMetrics().keySet().forEach(registry::remove);
  }


  public void stateChanged(String state, long stateSetTimestamp) {
    this.state = state;
    this.stateSetTimestamp = stateSetTimestamp;
  }


  private String explicitMetricName(String name) {
    return new MetricName(name, ImmutableList.of(
        "catalog", "database", "table_name"
    ), ImmutableList.of(
        identifier.getCatalog(), identifier.getDatabase(), identifier.getTableName()
    )).getExplicitMetricName();
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

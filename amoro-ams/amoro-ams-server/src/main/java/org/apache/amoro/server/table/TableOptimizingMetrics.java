/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.table;

import static org.apache.amoro.api.metrics.MetricDefine.defineCounter;
import static org.apache.amoro.api.metrics.MetricDefine.defineGauge;

import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.metrics.Counter;
import org.apache.amoro.api.metrics.Gauge;
import org.apache.amoro.api.metrics.Metric;
import org.apache.amoro.api.metrics.MetricDefine;
import org.apache.amoro.api.metrics.MetricKey;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.OptimizingType;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;

import java.util.List;

/** Table self optimizing metrics */
public class TableOptimizingMetrics {
  /** Table is no need optimizing. */
  public static final String STATUS_IDLE = "idle";

  /** Table is need optimizing, but waiting for resource */
  public static final String STATUS_PENDING = "pending";

  /** Table is doing optimizing process planing. */
  public static final String STATUS_PLANING = "planing";

  /** Table is executing optimizing process */
  public static final String STATUS_EXECUTING = "executing";

  /** All optimizing process task is done, and process is committing. */
  public static final String STATUS_COMMITTING = "committing";

  // table optimizing status duration metrics
  public static final MetricDefine TABLE_OPTIMIZING_STATUS_IDLE_DURATION =
      defineGauge("table_optimizing_status_idle_duration_mills")
          .withDescription("Duration in milliseconds after table be in idle status")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATUS_PENDING_DURATION =
      defineGauge("table_optimizing_status_pending_duration_mills")
          .withDescription("Duration in milliseconds after table be in pending status")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATUS_PLANNING_DURATION =
      defineGauge("table_optimizing_status_planning_duration_mills")
          .withDescription("Duration in milliseconds after table be in planning status")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATUS_EXECUTING_DURATION =
      defineGauge("table_optimizing_status_executing_duration_mills")
          .withDescription("Duration in milliseconds after table be in executing status")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATUS_COMMITTING_DURATION =
      defineGauge("table_optimizing_status_committing_duration_mills")
          .withDescription("Duration in milliseconds after table be in committing status")
          .withTags("catalog", "database", "table")
          .build();

  // table optimizing process count metrics
  public static final MetricDefine TABLE_OPTIMIZING_PROCESS_TOTAL_COUNT =
      defineCounter("table_optimizing_process_total_count")
          .withDescription("Count of all optimizing process since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_PROCESS_FAILED_COUNT =
      defineCounter("table_optimizing_process_failed_count")
          .withDescription("Count of failed optimizing process since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_MINOR_TOTAL_COUNT =
      defineCounter("table_optimizing_minor_total_count")
          .withDescription("Count of minor optimizing process since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_MINOR_FAILED_COUNT =
      defineCounter("table_optimizing_minor_failed_count")
          .withDescription("Count of failed minor optimizing process since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_MAJOR_TOTAL_COUNT =
      defineCounter("table_optimizing_major_total_count")
          .withDescription("Count of major optimizing process since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_MAJOR_FAILED_COUNT =
      defineCounter("table_optimizing_major_failed_count")
          .withDescription("Count of failed major optimizing process since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_FULL_TOTAL_COUNT =
      defineCounter("table_optimizing_full_total_count")
          .withDescription("Count of full optimizing process since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_FULL_FAILED_COUNT =
      defineCounter("table_optimizing_full_failed_count")
          .withDescription("Count of failed full optimizing process since ams started")
          .withTags("catalog", "database", "table")
          .build();

  // table optimizing process status metrics
  public static final MetricDefine TABLE_OPTIMIZING_STATUS_IN_IDLE =
      defineGauge("table_optimizing_status_in_idle")
          .withDescription("If currently table is in idle status")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATUS_IN_PENDING =
      defineGauge("table_optimizing_status_in_pending")
          .withDescription("If currently table is in pending status")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATUS_IN_PLANNING =
      defineGauge("table_optimizing_status_in_planning")
          .withDescription("If currently table is in planning status")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATUS_IN_EXECUTING =
      defineGauge("table_optimizing_status_in_executing")
          .withDescription("If currently table is in executing status")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATUS_IN_COMMITTING =
      defineGauge("table_optimizing_status_in_committing")
          .withDescription("If currently table is in committing status")
          .withTags("catalog", "database", "table")
          .build();

  private final Counter processTotalCount = new Counter();
  private final Counter processFailedCount = new Counter();
  private final Counter minorTotalCount = new Counter();
  private final Counter minorFailedCount = new Counter();
  private final Counter majorTotalCount = new Counter();
  private final Counter majorFailedCount = new Counter();
  private final Counter fullTotalCount = new Counter();
  private final Counter fullFailedCount = new Counter();

  private final ServerTableIdentifier identifier;

  private OptimizingStatus optimizingStatus = OptimizingStatus.IDLE;
  private long statusSetTimestamp = System.currentTimeMillis();
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();
  private MetricRegistry globalRegistry;

  public TableOptimizingMetrics(ServerTableIdentifier identifier) {
    this.identifier = identifier;
  }

  private void registerMetric(MetricRegistry registry, MetricDefine define, Metric metric) {
    MetricKey key =
        registry.register(
            define,
            ImmutableMap.of(
                "catalog",
                identifier.getCatalog(),
                "database",
                identifier.getDatabase(),
                "table",
                identifier.getTableName()),
            metric);
    registeredMetricKeys.add(key);
  }

  public void register(MetricRegistry registry) {
    if (globalRegistry == null) {
      // register status duration metrics
      registerMetric(
          registry, TABLE_OPTIMIZING_STATUS_IDLE_DURATION, new StatusDurationGauge(STATUS_IDLE));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_STATUS_PENDING_DURATION,
          new StatusDurationGauge(STATUS_PENDING));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_STATUS_PLANNING_DURATION,
          new StatusDurationGauge(STATUS_PLANING));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_STATUS_EXECUTING_DURATION,
          new StatusDurationGauge(STATUS_EXECUTING));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_STATUS_COMMITTING_DURATION,
          new StatusDurationGauge(STATUS_COMMITTING));

      // register table in status metrics
      registerMetric(registry, TABLE_OPTIMIZING_STATUS_IN_IDLE, new IsInStatusGauge(STATUS_IDLE));
      registerMetric(
          registry, TABLE_OPTIMIZING_STATUS_IN_PENDING, new IsInStatusGauge(STATUS_PENDING));
      registerMetric(
          registry, TABLE_OPTIMIZING_STATUS_IN_PLANNING, new IsInStatusGauge(STATUS_PLANING));
      registerMetric(
          registry, TABLE_OPTIMIZING_STATUS_IN_EXECUTING, new IsInStatusGauge(STATUS_EXECUTING));
      registerMetric(
          registry, TABLE_OPTIMIZING_STATUS_IN_COMMITTING, new IsInStatusGauge(STATUS_COMMITTING));

      // register table process count metrics
      registerMetric(registry, TABLE_OPTIMIZING_PROCESS_TOTAL_COUNT, processTotalCount);
      registerMetric(registry, TABLE_OPTIMIZING_PROCESS_FAILED_COUNT, processFailedCount);
      registerMetric(registry, TABLE_OPTIMIZING_MINOR_TOTAL_COUNT, minorTotalCount);
      registerMetric(registry, TABLE_OPTIMIZING_MINOR_FAILED_COUNT, minorFailedCount);
      registerMetric(registry, TABLE_OPTIMIZING_MAJOR_TOTAL_COUNT, majorTotalCount);
      registerMetric(registry, TABLE_OPTIMIZING_MAJOR_FAILED_COUNT, majorFailedCount);
      registerMetric(registry, TABLE_OPTIMIZING_FULL_TOTAL_COUNT, fullTotalCount);
      registerMetric(registry, TABLE_OPTIMIZING_FULL_FAILED_COUNT, fullFailedCount);

      globalRegistry = registry;
    }
  }

  public void unregister() {
    registeredMetricKeys.forEach(globalRegistry::unregister);
    registeredMetricKeys.clear();
    globalRegistry = null;
  }

  /**
   * Handle table self optimizing status change event.
   *
   * @param optimizingStatus new optimizing status
   * @param statusSetTimestamp timestamp of status changed.
   */
  public void statusChanged(OptimizingStatus optimizingStatus, long statusSetTimestamp) {
    this.optimizingStatus = optimizingStatus;
    this.statusSetTimestamp = statusSetTimestamp;
  }

  /**
   * Handle table self optimizing process completed event.
   *
   * @param processType optimizing process type.
   * @param success is optimizing process success.
   */
  public void processComplete(OptimizingType processType, boolean success) {
    processTotalCount.inc();
    Counter totalCounter = null;
    Counter failedCounter = null;
    switch (processType) {
      case MINOR:
        totalCounter = minorTotalCount;
        failedCounter = minorFailedCount;
        break;
      case MAJOR:
        totalCounter = majorTotalCount;
        failedCounter = majorFailedCount;
        break;
      case FULL:
        totalCounter = fullTotalCount;
        failedCounter = fullFailedCount;
        break;
    }
    if (totalCounter != null) {
      totalCounter.inc();
    }
    if (!success && failedCounter != null) {
      failedCounter.inc();
    }
  }

  private String getOptimizingStatusDesc(OptimizingStatus status) {
    switch (status) {
      case IDLE:
        return STATUS_IDLE;
      case PENDING:
        return STATUS_PENDING;
      case PLANNING:
        return STATUS_PLANING;
      case FULL_OPTIMIZING:
      case MAJOR_OPTIMIZING:
      case MINOR_OPTIMIZING:
        return STATUS_EXECUTING;
      case COMMITTING:
        return STATUS_COMMITTING;
      default:
        return status.name();
    }
  }

  class StatusDurationGauge implements Gauge<Long> {
    final String targetStatus;

    StatusDurationGauge(String targetStatus) {
      this.targetStatus = targetStatus;
    }

    @Override
    public Long getValue() {
      String status = getOptimizingStatusDesc(optimizingStatus);
      if (targetStatus.equals(status)) {
        return statusDuration();
      }
      return 0L;
    }

    private Long statusDuration() {
      return System.currentTimeMillis() - statusSetTimestamp;
    }
  }

  class IsInStatusGauge implements Gauge<Long> {
    final String targetStatus;

    IsInStatusGauge(String targetStatus) {
      this.targetStatus = targetStatus;
    }

    @Override
    public Long getValue() {
      String status = getOptimizingStatusDesc(optimizingStatus);
      if (targetStatus.equals(status)) {
        return 1L;
      }
      return 0L;
    }
  }
}

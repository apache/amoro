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

import static org.apache.amoro.metrics.MetricDefine.defineCounter;
import static org.apache.amoro.metrics.MetricDefine.defineGauge;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.maintainer.IcebergTableMaintainer;
import org.apache.amoro.shade.guava32.com.google.common.primitives.Longs;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotSummary;

/** Table self optimizing metrics */
public class TableOptimizingMetrics extends AbstractTableMetrics {
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

  public static final MetricDefine TABLE_OPTIMIZING_SINCE_LAST_MINOR_OPTIMIZATION =
      defineGauge("table_optimizing_since_last_minor_optimization_mills")
          .withDescription("Duration in milliseconds since last successful minor optimization")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_SINCE_LAST_MAJOR_OPTIMIZATION =
      defineGauge("table_optimizing_since_last_major_optimization_mills")
          .withDescription("Duration in milliseconds since last successful major optimization")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_SINCE_LAST_FULL_OPTIMIZATION =
      defineGauge("table_optimizing_since_last_full_optimization_mills")
          .withDescription("Duration in milliseconds since last successful full optimization")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_SINCE_LAST_OPTIMIZATION =
      defineGauge("table_optimizing_since_last_optimization_mills")
          .withDescription("Duration in milliseconds since last successful optimization")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_LAG_DURATION =
      defineGauge("table_optimizing_lag_duration_mills")
          .withDescription(
              "Duration in milliseconds between last self-optimizing snapshot and refreshed snapshot")
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

  private OptimizingStatus optimizingStatus = OptimizingStatus.IDLE;
  private long statusSetTimestamp = System.currentTimeMillis();
  private long lastMinorTime, lastMajorTime, lastFullTime;
  private long lastNonMaintainedTime = AmoroServiceConstants.INVALID_TIME;
  private long lastOptimizingTime = AmoroServiceConstants.INVALID_TIME;

  public TableOptimizingMetrics(ServerTableIdentifier identifier) {
    super(identifier);
  }

  @Override
  public void registerMetrics(MetricRegistry registry) {
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

      // register last optimizing duration metrics
      registerMetric(
          registry,
          TABLE_OPTIMIZING_SINCE_LAST_MINOR_OPTIMIZATION,
          new LastOptimizingDurationGauge(OptimizingType.MINOR));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_SINCE_LAST_MAJOR_OPTIMIZATION,
          new LastOptimizingDurationGauge(OptimizingType.MAJOR));
      registerMetric(
          registry,
          TABLE_OPTIMIZING_SINCE_LAST_FULL_OPTIMIZATION,
          new LastOptimizingDurationGauge(OptimizingType.FULL));
      registerMetric(
          registry, TABLE_OPTIMIZING_SINCE_LAST_OPTIMIZATION, new LastOptimizingDurationGauge());
      registerMetric(registry, TABLE_OPTIMIZING_LAG_DURATION, new OptimizingLagDurationGauge());

      globalRegistry = registry;
    }
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
   * Handle table self optimizing process complete event.
   *
   * @param processType optimizing process type.
   * @param lastTime last optimizing timestamp.
   */
  public void lastOptimizingTime(OptimizingType processType, long lastTime) {
    switch (processType) {
      case MINOR:
        this.lastMinorTime = lastTime;
        break;
      case MAJOR:
        this.lastMajorTime = lastTime;
        break;
      case FULL:
        this.lastFullTime = lastTime;
        break;
    }
  }

  public void nonMaintainedSnapshotTime(Snapshot snapshot) {
    if (snapshot == null) {
      return;
    }
    // ignore snapshot which is created by amoro maintain commits or no files added
    if (snapshot.summary().values().stream()
            .anyMatch(IcebergTableMaintainer.AMORO_MAINTAIN_COMMITS::contains)
        || Long.parseLong(snapshot.summary().getOrDefault(SnapshotSummary.ADDED_FILES_PROP, "0"))
            == 0) {
      return;
    }

    this.lastNonMaintainedTime = Longs.max(lastNonMaintainedTime, snapshot.timestampMillis());
  }

  public void lastOptimizingSnapshotTime(Snapshot snapshot) {
    if (snapshot == null) {
      return;
    }

    this.lastOptimizingTime =
        Longs.max(
            lastOptimizingTime,
            snapshot.timestampMillis(),
            Longs.max(lastMinorTime, lastMajorTime, lastFullTime));
  }

  /**
   * Handle table self optimizing process completed event.
   *
   * @param processType optimizing process type.
   * @param success is optimizing process success.
   */
  public void processComplete(OptimizingType processType, boolean success, long planTime) {
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
    if (success) {
      lastOptimizingTime(processType, planTime);
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

  class LastOptimizingDurationGauge implements Gauge<Long> {
    final OptimizingType optimizingType;

    LastOptimizingDurationGauge(OptimizingType optimizingType) {
      this.optimizingType = optimizingType;
    }

    LastOptimizingDurationGauge() {
      optimizingType = null;
    }

    @Override
    public Long getValue() {
      if (optimizingType == null) {
        return optimizingInterval(lastOptimizingTime);
      }

      switch (optimizingType) {
        case MINOR:
          return optimizingInterval(lastMinorTime);
        case MAJOR:
          return optimizingInterval(lastMajorTime);
        case FULL:
          return optimizingInterval(lastFullTime);
        default:
          return AmoroServiceConstants.INVALID_TIME;
      }
    }

    private long optimizingInterval(long lastOptimizedTime) {
      return lastOptimizedTime > 0
          ? System.currentTimeMillis() - lastOptimizedTime
          : AmoroServiceConstants.INVALID_TIME;
    }
  }

  class OptimizingLagDurationGauge implements Gauge<Long> {
    @Override
    public Long getValue() {
      if (lastNonMaintainedTime == AmoroServiceConstants.INVALID_TIME
          || lastOptimizingTime == AmoroServiceConstants.INVALID_TIME) {
        return AmoroServiceConstants.INVALID_TIME;
      } else {
        return lastNonMaintainedTime - lastOptimizingTime;
      }
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

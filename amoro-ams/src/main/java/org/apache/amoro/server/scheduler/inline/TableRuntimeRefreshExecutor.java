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
 *
 * Modified by Datazip Inc. in 2026
 */

package org.apache.amoro.server.scheduler.inline;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.CronUtils;

/**
 * Minute-tick scheduler that drives all self-optimizing purely through cron expressions.
 *
 * <p>Each tick (default: every 1 minute) it:
 *
 * <ol>
 *   <li>Refreshes the table's snapshot metadata.
 *   <li>Evaluates full → major → minor cron expressions in priority order.
 *   <li>For each fired cron, checks whether the optimization is still "necessary":
 *       <ul>
 *         <li>A type is <em>unnecessary</em> when the snapshot has not changed since the last
 *             optimization AND the last optimization type already covers this type (FULL covers
 *             all; MAJOR covers major + minor; MINOR covers only minor).
 *       </ul>
 *   <li>If necessary → transitions the table to PENDING so {@code OptimizingQueue} will plan it.
 *   <li>If unnecessary → writes a {@link ProcessStatus#SKIPPED} record and falls through to the
 *       next lower-priority type.
 * </ol>
 *
 * <p>Snapshot changes do <em>not</em> trigger optimization on their own; every optimization cycle
 * must be backed by a cron expression that has fired since the last run of that type.
 */
public class TableRuntimeRefreshExecutor extends PeriodicTableScheduler {

  // 1 minutes
  private final long interval;
  private final int maxPendingPartitions;

  public TableRuntimeRefreshExecutor(
      TableService tableService, int poolSize, long interval, int maxPendingPartitions) {
    super(tableService, poolSize);
    this.interval = interval;
    this.maxPendingPartitions = maxPendingPartitions;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime instanceof DefaultTableRuntime;
  }

  // cron tick time interval (which checks if any optimization cron passes or not)
  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return interval;
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
    if (originalConfig.getOptimizingConfig().isEnabled()
        && !tableRuntime.getTableConfiguration().getOptimizingConfig().isEnabled()) {
      OptimizingProcess optimizingProcess = defaultTableRuntime.getOptimizingProcess();
      if (optimizingProcess != null && optimizingProcess.getStatus() == ProcessStatus.RUNNING) {
        optimizingProcess.close(false);
      }
    }
  }

  @Override
  protected long getExecutorDelay() {
    return 0;
  }

  @Override
  public void execute(TableRuntime tableRuntime) {
    try {
      Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
      DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;

      AmoroTable<?> table = loadTable(tableRuntime);
      defaultTableRuntime.refresh(table);

      evaluateCronTriggers(defaultTableRuntime, (MixedTable) table.originalTable());
    } catch (Throwable throwable) {
      logger.error("Refreshing table {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
  }

  /**
   * Core cron-tick logic. Iterates FULL → MAJOR → MINOR in priority order. The first type whose
   * cron has fired AND whose optimization is still necessary transitions the table to PENDING and
   * stops. Types whose crons fired but are unnecessary get a SKIPPED process record and the loop
   * falls through to the next type.
   */
  private void evaluateCronTriggers(DefaultTableRuntime tableRuntime, MixedTable mixedTable) {
    OptimizingConfig cfg = tableRuntime.getOptimizingConfig();
    if (!cfg.isEnabled()) {
      return;
    }

    // Only consider tables that are idle; already-processing / planning / pending tables
    // will be handled by the existing OptimizingQueue machinery.
    OptimizingStatus status = tableRuntime.getOptimizingStatus();
    if (status != OptimizingStatus.IDLE) {
      return;
    }

    boolean snapshotChanged = isSnapshotChanged(tableRuntime, mixedTable);
    OptimizingType lastType = tableRuntime.getLastOptimizingType();

    for (OptimizingType candidate :
        new OptimizingType[] {OptimizingType.FULL, OptimizingType.MAJOR, OptimizingType.MINOR}) {

      String cronExpr = cronExpressionFor(cfg, candidate);

      if (CronUtils.hasFiredInLastMinute(cronExpr)) {
        if (snapshotChanged || isNecessary(candidate, lastType)) {
          logger.info(
              "[cron-trigger] table={} scheduling {} optimization (snapshotChanged={}, lastType={})",
              tableRuntime.getTableIdentifier(),
              candidate,
              snapshotChanged,
              lastType);

          tableRuntime.markAsPending(candidate);
        } else {
          String reason = buildSkipReason(candidate, lastType);
          logger.info(
              "[cron-skip] table={} type={} skipped: {}",
              tableRuntime.getTableIdentifier(),
              candidate,
              reason);
          tableRuntime.recordSkippedOptimization(candidate, reason);
        }
        break;
      }
    }
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  /**
   * Returns {@code true} when the table's current snapshot differs from the snapshot that was
   * current when the last optimization completed.
   */
  private boolean isSnapshotChanged(DefaultTableRuntime tableRuntime, MixedTable mixedTable) {
    long lastOptSnapshotId = tableRuntime.getLastOptimizedSnapshotId();
    long lastOptChangeSnapshotId = tableRuntime.getLastOptimizedChangeSnapshotId();
    if (mixedTable.isKeyedTable()) {
      return lastOptSnapshotId != tableRuntime.getCurrentSnapshotId()
          || lastOptChangeSnapshotId != tableRuntime.getCurrentChangeSnapshotId();
    }
    return lastOptSnapshotId != tableRuntime.getCurrentSnapshotId();
  }

  /**
   * A type is "necessary" when the snapshot is changed OR the last completed optimization already
   * covers this type:
   *
   * <ul>
   *   <li>FULL is covered only when last is Minor or Major.
   *   <li>MAJOR is covered only when last is Minor.
   * </ul>
   */
  private boolean isNecessary(OptimizingType candidate, OptimizingType lastType) {
    if (lastType == null) {
      return false;
    }
    switch (candidate) {
      case FULL:
        return lastType == OptimizingType.MINOR || lastType == OptimizingType.MAJOR;
      case MAJOR:
        return lastType == OptimizingType.MINOR;
      default:
        return false;
    }
  }

  private String buildSkipReason(OptimizingType candidate, OptimizingType lastType) {
    return String.format(
        "cron fired for %s but snapshot unchanged since last %s optimization — no new data to process",
        candidate, lastType);
  }

  private String cronExpressionFor(OptimizingConfig cfg, OptimizingType type) {
    switch (type) {
      case FULL:
        return cfg.getFullTriggerCron();
      case MAJOR:
        return cfg.getMajorTriggerCron();
      case MINOR:
        return cfg.getMinorTriggerCron();
      default:
        return null;
    }
  }
}

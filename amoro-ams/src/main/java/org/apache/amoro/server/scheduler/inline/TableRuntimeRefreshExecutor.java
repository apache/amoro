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

package org.apache.amoro.server.scheduler.inline;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.optimizing.evaluation.MetadataBasedEvaluationEvent;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.MixedTable;

/** Executor that refreshes table runtimes and evaluates optimizing status periodically. */
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

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;

    if (defaultTableRuntime.getOptimizingConfig().isRefreshTableAdaptiveEnabled(interval)) {
      long newInterval = defaultTableRuntime.getLatestRefreshInterval();
      if (newInterval > 0) {
        return newInterval;
      }
    }

    return Math.min(
        defaultTableRuntime.getOptimizingConfig().getMinorLeastInterval() * 4L / 5, interval);
  }

  private boolean tryEvaluatingPendingInput(DefaultTableRuntime tableRuntime, MixedTable table) {
    OptimizingConfig optimizingConfig = tableRuntime.getOptimizingConfig();
    boolean optimizingEnabled = optimizingConfig.isEnabled();
    if (optimizingEnabled && tableRuntime.getOptimizingStatus().equals(OptimizingStatus.IDLE)) {
      // Evaluate pending input and collect table summary when optimizing is enabled and idle
      if (optimizingConfig.isMetadataBasedTriggerEnabled()
          && !MetadataBasedEvaluationEvent.isEvaluatingNecessary(
              optimizingConfig, table, tableRuntime.getLastPlanTime())) {
        logger.debug(
            "{} optimizing is not necessary due to metadata based trigger",
            tableRuntime.getTableIdentifier());
        // indicates no optimization demand now
        return false;
      }

      AbstractOptimizingEvaluator evaluator =
          IcebergTableUtil.createOptimizingEvaluator(tableRuntime, table, maxPendingPartitions);
      boolean evaluatorIsNecessary = evaluator.isNecessary();
      if (evaluatorIsNecessary) {
        AbstractOptimizingEvaluator.PendingInput pendingInput =
            evaluator.getOptimizingPendingInput();
        logger.debug(
            "{} optimizing is necessary and get pending input {}",
            tableRuntime.getTableIdentifier(),
            pendingInput);
        tableRuntime.setPendingInput(pendingInput);
      } else {
        tableRuntime.optimizingNotNecessary();
      }

      tableRuntime.setTableSummary(evaluator.getPendingInput());
      return evaluatorIsNecessary;
    } else if (!optimizingEnabled && optimizingConfig.isTableSummaryEnabled()) {
      // Collect table summary metrics even when optimizing is disabled
      logger.debug(
          "{} collecting table summary (optimizing disabled, tableSummary enabled)",
          tableRuntime.getTableIdentifier());
      AbstractOptimizingEvaluator evaluator =
          IcebergTableUtil.createOptimizingEvaluator(tableRuntime, table, maxPendingPartitions);
      AbstractOptimizingEvaluator.PendingInput summary = evaluator.getPendingInput();
      logger.debug("{} table summary collected: {}", tableRuntime.getTableIdentifier(), summary);
      tableRuntime.setTableSummary(summary);
      return false;
    } else if (!optimizingEnabled) {
      logger.debug(
          "{} optimizing is not enabled, skip evaluating pending input",
          tableRuntime.getTableIdentifier());
      return false;
    } else {
      logger.debug(
          "{} optimizing is processing or is in preparation", tableRuntime.getTableIdentifier());
      // indicates optimization demand exists (preparation or processing),
      // even though we don't trigger a new evaluation in this loop.
      return true;
    }
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
    // After disabling self-optimizing, close the currently running optimizing process.
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

      long lastOptimizedSnapshotId = defaultTableRuntime.getLastOptimizedSnapshotId();
      long lastOptimizedChangeSnapshotId = defaultTableRuntime.getLastOptimizedChangeSnapshotId();
      AmoroTable<?> table = loadTable(tableRuntime);
      defaultTableRuntime.refresh(table);
      MixedTable mixedTable = (MixedTable) table.originalTable();
      boolean snapshotChanged =
          (mixedTable.isKeyedTable()
                  && (lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId()
                      || lastOptimizedChangeSnapshotId
                          != defaultTableRuntime.getCurrentChangeSnapshotId()))
              || (mixedTable.isUnkeyedTable()
                  && lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId());
      OptimizingConfig optimizingConfig = defaultTableRuntime.getOptimizingConfig();
      boolean tableSummaryOnly =
          !optimizingConfig.isEnabled() && optimizingConfig.isTableSummaryEnabled();
      boolean hasOptimizingDemand = false;
      if (snapshotChanged) {
        hasOptimizingDemand = tryEvaluatingPendingInput(defaultTableRuntime, mixedTable);
      } else {
        logger.debug("{} optimizing is not necessary", defaultTableRuntime.getTableIdentifier());
      }

      // Update adaptive interval according to evaluated result.
      // Skip adaptive interval for table-summary-only mode to maintain fixed collection interval.
      if (!tableSummaryOnly
          && defaultTableRuntime.getOptimizingConfig().isRefreshTableAdaptiveEnabled(interval)) {
        defaultTableRuntime.setLatestEvaluatedNeedOptimizing(hasOptimizingDemand);
        long newInterval = getAdaptiveExecutingInterval(defaultTableRuntime);
        defaultTableRuntime.setLatestRefreshInterval(newInterval);
      }

      // issue #4172: a RUNNING process whose tasks all succeeded may remain stuck in
      // *_OPTIMIZING when a previous beginCommitting() failed transiently (e.g. DB lock
      // wait timeout). Detect it here and re-drive the transition to COMMITTING so that
      // OptimizingCommitExecutor can take over normally.
      tryHealStuckCommitting(defaultTableRuntime);
    } catch (Throwable throwable) {
      logger.error("Refreshing table {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
  }

  /**
   * Detects and heals a {@code RUNNING} optimizing process whose tasks have all succeeded but the
   * table's status never transitioned to {@link OptimizingStatus#COMMITTING} (e.g. because the
   * previous {@code beginCommitting()} DB update failed transiently). Without this self-heal, the
   * table stays in {@code *_OPTIMIZING} forever until AMS is restarted - see issue #4172.
   *
   * <p>The recovery path in {@code OptimizingQueue#initTableRuntime} already handles this case on
   * AMS restart; this method handles it while AMS is still running, by re-invoking {@link
   * DefaultTableRuntime#beginCommitting()} from the periodic refresh loop. On success, the status
   * transition will fire {@code handleTableChanged} and the normal {@code OptimizingCommitExecutor}
   * pipeline will resume. On failure (e.g. the DB is still unhealthy), this method logs and
   * returns; the next refresh cycle will try again.
   */
  private void tryHealStuckCommitting(DefaultTableRuntime tableRuntime) {
    OptimizingProcess process = tableRuntime.getOptimizingProcess();
    if (process == null
        || process.getStatus() != ProcessStatus.RUNNING
        || tableRuntime.getOptimizingStatus() == OptimizingStatus.COMMITTING
        || !tableRuntime.getOptimizingStatus().isProcessing()) {
      return;
    }
    if (!process.allTasksPrepared()) {
      return;
    }
    logger.warn(
        "{} detected stuck RUNNING optimizing process (processId={}, status={}): all tasks have "
            + "succeeded but the table never transitioned to COMMITTING. Self-healing by "
            + "re-driving beginCommitting() (issue #4172).",
        tableRuntime.getTableIdentifier(),
        process.getProcessId(),
        tableRuntime.getOptimizingStatus());
    try {
      tableRuntime.beginCommitting();
    } catch (Exception e) {
      logger.warn(
          "{} self-heal beginCommitting() failed, will retry on the next refresh cycle.",
          tableRuntime.getTableIdentifier(),
          e);
    }
  }

  /**
   * Calculate adaptive execution interval based on table optimization status.
   *
   * <p>Uses AIMD (Additive Increase Multiplicative Decrease) algorithm inspired by TCP congestion
   * control:
   *
   * <ul>
   *   <li>If table does not need to be optimized: additive increase - gradually extend interval to
   *       reduce resource consumption
   *   <li>If table needs optimization: multiplicative decrease - rapidly reduce interval for quick
   *       response
   * </ul>
   *
   * <p>Interval is bounded by [interval_min, interval_max] and kept in memory only (resets to
   * interval_min on restart).
   *
   * @param tableRuntime The table runtime information containing current status and configuration
   * @return The next execution interval in milliseconds
   */
  @VisibleForTesting
  public long getAdaptiveExecutingInterval(DefaultTableRuntime tableRuntime) {
    final long minInterval = interval;
    final long maxInterval =
        tableRuntime.getOptimizingConfig().getRefreshTableAdaptiveMaxIntervalMs();
    long currentInterval = tableRuntime.getLatestRefreshInterval();

    // Initialize interval on first run or after restart
    if (currentInterval == 0) {
      currentInterval = minInterval;
    }

    // Determine whether table needs optimization
    boolean needOptimizing = tableRuntime.getLatestEvaluatedNeedOptimizing();

    long nextInterval;
    if (needOptimizing) {
      nextInterval = decreaseInterval(currentInterval, minInterval);
      logger.debug(
          "Table {} needs optimization, decreasing interval from {}ms to {}ms",
          tableRuntime.getTableIdentifier(),
          currentInterval,
          nextInterval);
    } else {
      nextInterval = increaseInterval(tableRuntime, currentInterval, maxInterval);
      logger.debug(
          "Table {} does not need optimization, increasing interval from {}ms to {}ms",
          tableRuntime.getTableIdentifier(),
          currentInterval,
          nextInterval);
    }

    return nextInterval;
  }

  /**
   * Decrease interval when table needs optimization.
   *
   * <p>Uses multiplicative decrease (halving) inspired by TCP Fast Recovery algorithm for rapid
   * response to table health issues.
   *
   * @param currentInterval Current refresh interval in milliseconds
   * @param minInterval Minimum allowed interval in milliseconds
   * @return New interval after decrease.
   */
  private long decreaseInterval(long currentInterval, long minInterval) {
    long newInterval = currentInterval / 2;
    long boundedInterval = Math.max(newInterval, minInterval);
    if (newInterval < minInterval) {
      logger.debug(
          "Interval reached minimum boundary: attempted {}ms, capped at {}ms",
          newInterval,
          minInterval);
    }

    return boundedInterval;
  }

  /**
   * Increase interval when table does not need optimization.
   *
   * <p>Uses additive increase inspired by TCP Congestion Avoidance algorithm for gradual and stable
   * growth.
   *
   * @param tableRuntime The table runtime information containing configuration
   * @param currentInterval Current refresh interval in milliseconds
   * @param maxInterval Maximum allowed interval in milliseconds
   * @return New interval after increase.
   */
  private long increaseInterval(
      DefaultTableRuntime tableRuntime, long currentInterval, long maxInterval) {
    long step = tableRuntime.getOptimizingConfig().getRefreshTableAdaptiveIncreaseStepMs();
    long newInterval = currentInterval + step;
    long boundedInterval = Math.min(newInterval, maxInterval);
    if (newInterval > maxInterval) {
      logger.debug(
          "Interval reached maximum boundary: currentInterval is {}ms, attempted {}ms, capped at {}ms",
          currentInterval,
          newInterval,
          maxInterval);
    }

    return boundedInterval;
  }
}

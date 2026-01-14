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

    if (defaultTableRuntime.getOptimizingConfig().getRefreshTableAdaptiveEnabled()) {
      long newInterval = defaultTableRuntime.getLatestRefreshInterval();
      if (newInterval > 0) {
        return newInterval;
      }
    }

    return Math.min(
        defaultTableRuntime.getOptimizingConfig().getMinorLeastInterval() * 4L / 5, interval);
  }

  private void tryEvaluatingPendingInput(DefaultTableRuntime tableRuntime, MixedTable table) {
    // only evaluate pending input when optimizing is enabled and in idle state
    OptimizingConfig optimizingConfig = tableRuntime.getOptimizingConfig();
    boolean optimizingEnabled = optimizingConfig.isEnabled();
    if (optimizingEnabled && tableRuntime.getOptimizingStatus().equals(OptimizingStatus.IDLE)) {

      if (optimizingConfig.isMetadataBasedTriggerEnabled()
          && !MetadataBasedEvaluationEvent.isEvaluatingNecessary(
              optimizingConfig, table, tableRuntime.getLastPlanTime())) {
        tableRuntime.setLatestEvaluatedNeedOptimizing(false);

        logger.debug(
            "{} optimizing is not necessary due to metadata based trigger",
            tableRuntime.getTableIdentifier());
        return;
      }

      AbstractOptimizingEvaluator evaluator =
          IcebergTableUtil.createOptimizingEvaluator(tableRuntime, table, maxPendingPartitions);
      if (evaluator.isNecessary()) {
        AbstractOptimizingEvaluator.PendingInput pendingInput =
            evaluator.getOptimizingPendingInput();
        logger.debug(
            "{} optimizing is necessary and get pending input {}",
            tableRuntime.getTableIdentifier(),
            pendingInput);
        tableRuntime.setPendingInput(pendingInput);
      } else {
        tableRuntime.optimizingNotNecessary();
        tableRuntime.setTableSummary(evaluator.getPendingInput());
      }
    } else if (!optimizingEnabled) {
      tableRuntime.setLatestEvaluatedNeedOptimizing(false);
      logger.debug(
          "{} optimizing is not enabled, skip evaluating pending input",
          tableRuntime.getTableIdentifier());
    } else {
      tableRuntime.setLatestEvaluatedNeedOptimizing(true);
      logger.debug(
          "{} optimizing is processing or is in preparation", tableRuntime.getTableIdentifier());
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
      if ((mixedTable.isKeyedTable()
              && (lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId()
                  || lastOptimizedChangeSnapshotId
                      != defaultTableRuntime.getCurrentChangeSnapshotId()))
          || (mixedTable.isUnkeyedTable()
              && lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId())) {
        tryEvaluatingPendingInput(defaultTableRuntime, mixedTable);
      } else {
        logger.debug("{} optimizing is not necessary", defaultTableRuntime.getTableIdentifier());
        defaultTableRuntime.setLatestEvaluatedNeedOptimizing(false);
      }

      // Update adaptive interval according to evaluating result.
      if (defaultTableRuntime.getOptimizingConfig().getRefreshTableAdaptiveEnabled()) {
        long newInterval = getAdaptiveExecutingInterval(defaultTableRuntime);
        defaultTableRuntime.setLatestRefreshInterval(newInterval);
      }

    } catch (Throwable throwable) {
      logger.error("Refreshing table {} failed.", tableRuntime.getTableIdentifier(), throwable);
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
  private long getAdaptiveExecutingInterval(DefaultTableRuntime tableRuntime) {
    final long minInterval = interval;
    final long maxInterval =
        tableRuntime.getOptimizingConfig().getRefreshTableAdaptiveMaxInterval();
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
    long step = tableRuntime.getOptimizingConfig().getRefreshTableAdaptiveIncreaseStep();
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

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
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultOptimizingState;
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
    return Math.min(
        defaultTableRuntime.getOptimizingState().getOptimizingConfig().getMinorLeastInterval()
            * 4L
            / 5,
        interval);
  }

  private void tryEvaluatingPendingInput(DefaultTableRuntime tableRuntime, MixedTable table) {
    DefaultOptimizingState optimizingState = tableRuntime.getOptimizingState();
    // When planning tasks, tables may be ignored if their plan intervals are too small,
    // resulting in this optimization being ineffective， so skip evaluator in advance
    boolean canPlan =
        tableRuntime.getOptimizingState().getLastPlanTime() - System.currentTimeMillis()
            > tableRuntime.getTableConfiguration().getOptimizingConfig().getMinPlanInterval();
    if (optimizingState.isOptimizingEnabled()
        && !optimizingState.getOptimizingStatus().isProcessing()
        && canPlan) {
      AbstractOptimizingEvaluator evaluator =
          IcebergTableUtil.createOptimizingEvaluator(tableRuntime, table, maxPendingPartitions);
      if (evaluator.isNecessary()) {
        AbstractOptimizingEvaluator.PendingInput pendingInput =
            evaluator.getOptimizingPendingInput();
        logger.debug(
            "{} optimizing is necessary and get pending input {}",
            tableRuntime.getTableIdentifier(),
            pendingInput);
        optimizingState.setPendingInput(pendingInput);
      } else {
        optimizingState.optimizingNotNecessary();
      }
      optimizingState.setTableSummary(evaluator.getPendingInput());
    }
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
    // After disabling self-optimizing, close the currently running optimizing process.
    if (originalConfig.getOptimizingConfig().isEnabled()
        && !tableRuntime.getTableConfiguration().getOptimizingConfig().isEnabled()) {
      OptimizingProcess optimizingProcess =
          defaultTableRuntime.getOptimizingState().getOptimizingProcess();
      if (optimizingProcess != null && optimizingProcess.getStatus() == ProcessStatus.RUNNING) {
        optimizingProcess.close();
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

      DefaultOptimizingState optimizingState = defaultTableRuntime.getOptimizingState();
      long lastOptimizedSnapshotId = optimizingState.getLastOptimizedSnapshotId();
      long lastOptimizedChangeSnapshotId = optimizingState.getLastOptimizedChangeSnapshotId();
      AmoroTable<?> table = loadTable(tableRuntime);
      optimizingState.refresh(table);
      MixedTable mixedTable = (MixedTable) table.originalTable();
      if ((mixedTable.isKeyedTable()
              && (lastOptimizedSnapshotId != optimizingState.getCurrentSnapshotId()
                  || lastOptimizedChangeSnapshotId != optimizingState.getCurrentChangeSnapshotId()))
          || (mixedTable.isUnkeyedTable()
              && lastOptimizedSnapshotId != optimizingState.getCurrentSnapshotId())) {
        tryEvaluatingPendingInput(defaultTableRuntime, mixedTable);
      }
    } catch (Throwable throwable) {
      logger.error("Refreshing table {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
  }
}

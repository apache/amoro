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

package org.apache.amoro.server.table.executor;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.plan.OptimizingEvaluator;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.table.MixedTable;

/** Executor that refreshes table runtimes and evaluates optimizing status periodically. */
public class TableRuntimeRefreshExecutor extends BaseTableExecutor {

  // 1 minutes
  private final long interval;

  public TableRuntimeRefreshExecutor(TableManager tableRuntimes, int poolSize, long interval) {
    super(tableRuntimes, poolSize);
    this.interval = interval;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return true;
  }

  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return Math.min(tableRuntime.getOptimizingConfig().getMinorLeastInterval() * 4L / 5, interval);
  }

  private void tryEvaluatingPendingInput(TableRuntime tableRuntime, MixedTable table) {
    if (tableRuntime.isOptimizingEnabled() && !tableRuntime.getOptimizingStatus().isProcessing()) {
      OptimizingEvaluator evaluator = new OptimizingEvaluator(tableRuntime, table);
      if (evaluator.isNecessary()) {
        OptimizingEvaluator.PendingInput pendingInput = evaluator.getOptimizingPendingInput();
        logger.debug(
            "{} optimizing is necessary and get pending input {}",
            tableRuntime.getTableIdentifier(),
            pendingInput);
        tableRuntime.setPendingInput(pendingInput);
      } else {
        tableRuntime.optimizingNotNecessary();
      }
      tableRuntime.setTableSummary(evaluator.getPendingInput());
    }
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    // After disabling self-optimizing, close the currently running optimizing process.
    if (originalConfig.getOptimizingConfig().isEnabled()
        && !tableRuntime.getTableConfiguration().getOptimizingConfig().isEnabled()) {
      OptimizingProcess optimizingProcess = tableRuntime.getOptimizingProcess();
      if (optimizingProcess != null
          && optimizingProcess.getStatus() == OptimizingProcess.Status.RUNNING) {
        optimizingProcess.close();
      }
    }
  }

  @Override
  public void execute(TableRuntime tableRuntime) {
    try {
      long lastOptimizedSnapshotId = tableRuntime.getLastOptimizedSnapshotId();
      long lastOptimizedChangeSnapshotId = tableRuntime.getLastOptimizedChangeSnapshotId();
      AmoroTable<?> table = loadTable(tableRuntime);
      tableRuntime.refresh(table);
      MixedTable mixedTable = (MixedTable) table.originalTable();
      if ((mixedTable.isKeyedTable()
              && (lastOptimizedSnapshotId != tableRuntime.getCurrentSnapshotId()
                  || lastOptimizedChangeSnapshotId != tableRuntime.getCurrentChangeSnapshotId()))
          || (mixedTable.isUnkeyedTable()
              && lastOptimizedSnapshotId != tableRuntime.getCurrentSnapshotId())) {
        tryEvaluatingPendingInput(tableRuntime, mixedTable);
      }
      // issue #4172: a RUNNING process whose tasks all succeeded may remain stuck in
      // *_OPTIMIZING when a previous beginCommitting() failed transiently (e.g. DB lock
      // wait timeout). Detect it here and re-drive the transition to COMMITTING so that
      // OptimizingCommitExecutor can take over normally.
      tryHealStuckCommitting(tableRuntime);
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
   * <p>When a stuck state is detected, this method re-invokes {@link
   * TableRuntime#beginCommitting()}. On success, the status transition will fire {@code
   * handleTableChanged} and the normal {@link OptimizingCommitExecutor} pipeline will resume. On
   * failure (e.g. the DB is still unhealthy), this method logs and returns; the next refresh cycle
   * will try again.
   */
  private void tryHealStuckCommitting(TableRuntime tableRuntime) {
    OptimizingProcess process = tableRuntime.getOptimizingProcess();
    if (process == null
        || process.getStatus() != OptimizingProcess.Status.RUNNING
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
}

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
import org.apache.amoro.ServerTableIdentifier;
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
import org.apache.amoro.table.TableIdentifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Executor that refreshes table runtimes and evaluates optimizing status periodically. */
public class TableRuntimeRefreshExecutor extends PeriodicTableScheduler {

  // 1 minutes
  private final long interval;
  // 1 hour
  private final long maxInterval;
  private final int maxPendingPartitions;
  // Tables configured to be triggered by events
  protected final Map<TableIdentifier, ServerTableIdentifier> managedEventTriggerTables =
      new ConcurrentHashMap<>();
  // Tables to be refreshed in the next execution schedule
  protected final Set<ServerTableIdentifier> pendingRefreshTables =
      Collections.synchronizedSet(new HashSet<>());

  public TableRuntimeRefreshExecutor(
      TableService tableService,
      int poolSize,
      long interval,
      int maxPendingPartitions,
      long maxInterval) {
    super(tableService, poolSize);
    this.interval = interval;
    this.maxInterval = maxInterval;
    this.maxPendingPartitions = maxPendingPartitions;
  }

  @Override
  protected void initHandler(List<TableRuntime> tableRuntimeList) {
    tableRuntimeList.stream()
        .filter(this::enabled)
        .filter(
            tableRuntime ->
                tableRuntime
                    .getTableConfiguration()
                    .getOptimizingConfig()
                    .isEventTriggeredRefresh())
        .forEach(
            tableRuntime -> {
              managedEventTriggerTables.put(
                  tableRuntime.getTableIdentifier().getIdentifier(),
                  tableRuntime.getTableIdentifier());
            });
    super.initHandler(tableRuntimeList);
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime instanceof DefaultTableRuntime;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
    return Math.min(
        defaultTableRuntime.getOptimizingConfig().getMinorLeastInterval() * 4L / 5, interval);
  }

  private void tryEvaluatingPendingInput(DefaultTableRuntime tableRuntime, MixedTable table) {
    // only evaluate pending input when optimizing is enabled and in idle state
    OptimizingConfig optimizingConfig = tableRuntime.getOptimizingConfig();
    if (optimizingConfig.isEnabled()
        && tableRuntime.getOptimizingStatus().equals(OptimizingStatus.IDLE)) {

      if (optimizingConfig.isMetadataBasedTriggerEnabled()
          && !MetadataBasedEvaluationEvent.isEvaluatingNecessary(
              optimizingConfig, table, tableRuntime.getLastPlanTime())) {
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
      }
      tableRuntime.setTableSummary(evaluator.getPendingInput());
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
    // Add or remove managed event trigger table when the configuration changes
    if (defaultTableRuntime
        .getTableConfiguration()
        .getOptimizingConfig()
        .isEventTriggeredRefresh()) {
      addManagedEventTriggerTable(defaultTableRuntime);
    } else {
      removeManagedEventTriggerTable(defaultTableRuntime);
    }
  }

  @Override
  public void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
    Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
    if (tableRuntime.getTableConfiguration().getOptimizingConfig().isEventTriggeredRefresh()) {
      addManagedEventTriggerTable(defaultTableRuntime);
    }
    super.handleTableAdded(table, tableRuntime);
  }

  @Override
  public void handleTableRemoved(TableRuntime tableRuntime) {
    Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
    if (defaultTableRuntime
        .getTableConfiguration()
        .getOptimizingConfig()
        .isEventTriggeredRefresh()) {
      removeManagedEventTriggerTable(defaultTableRuntime);
    }
    super.handleTableRemoved(tableRuntime);
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

      if (defaultTableRuntime.getOptimizingConfig().isEventTriggeredRefresh()) {
        if (!reachMaxInterval(defaultTableRuntime)
            && !pendingRefreshTables.contains(defaultTableRuntime.getTableIdentifier())) {
          // If the table refresh is configured refreshing by event but has not been triggered, or
          // the interval between the
          // last refresh has not reached the maximum interval, skip refreshing
          return;
        }
      }
      // continue the following table refresh process and remove it from the pending refresh tables
      removeTableToRefresh(defaultTableRuntime.getTableIdentifier());

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
      }
    } catch (Throwable throwable) {
      logger.error("Refreshing table {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
  }

  private boolean reachMaxInterval(DefaultTableRuntime tableRuntime) {
    long currentTime = System.currentTimeMillis();
    long lastRefreshTime = tableRuntime.getLastRefreshTime();
    return currentTime - lastRefreshTime >= maxInterval;
  }

  private void addManagedEventTriggerTable(DefaultTableRuntime tableRuntime) {
    managedEventTriggerTables.put(
        tableRuntime.getTableIdentifier().getIdentifier(), tableRuntime.getTableIdentifier());
  }

  private void removeManagedEventTriggerTable(DefaultTableRuntime tableRuntime) {
    managedEventTriggerTables.remove(tableRuntime.getTableIdentifier().getIdentifier());
    removeTableToRefresh(tableRuntime.getTableIdentifier());
  }

  public boolean addTableToRefresh(TableIdentifier tableIdentifier) {
    if (!managedEventTriggerTables.containsKey(tableIdentifier)) {
      logger.warn(
          "Table {} is not managed by event trigger, cannot add to refresh list.", tableIdentifier);
      return false;
    }
    pendingRefreshTables.add(managedEventTriggerTables.get(tableIdentifier));
    return true;
  }

  public void addTableToRefresh(ServerTableIdentifier serverTableIdentifier) {
    this.pendingRefreshTables.add(serverTableIdentifier);
    logger.debug("Add table {} to refresh pending list.", serverTableIdentifier);
  }

  public void removeTableToRefresh(ServerTableIdentifier serverTableIdentifier) {
    this.pendingRefreshTables.remove(serverTableIdentifier);
    logger.debug("Remove table {} from refresh pending list.", serverTableIdentifier);
  }
}

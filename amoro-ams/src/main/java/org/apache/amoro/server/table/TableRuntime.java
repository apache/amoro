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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.StateField;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.OptimizingType;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.optimizing.plan.OptimizingEvaluator;
import org.apache.amoro.server.persistence.StatedPersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.table.blocker.TableBlocker;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TableRuntime extends StatedPersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(TableRuntime.class);

  private final TableRuntimeHandler tableHandler;
  private final ServerTableIdentifier tableIdentifier;
  private final List<TaskRuntime.TaskQuota> taskQuotas =
      Collections.synchronizedList(new ArrayList<>());

  // for unKeyedTable or base table
  @StateField private volatile long currentSnapshotId = AmoroServiceConstants.INVALID_SNAPSHOT_ID;

  @StateField
  private volatile long lastOptimizedSnapshotId = AmoroServiceConstants.INVALID_SNAPSHOT_ID;

  @StateField
  private volatile long lastOptimizedChangeSnapshotId = AmoroServiceConstants.INVALID_SNAPSHOT_ID;
  // for change table
  @StateField
  private volatile long currentChangeSnapshotId = AmoroServiceConstants.INVALID_SNAPSHOT_ID;

  @StateField private volatile OptimizingStatus optimizingStatus = OptimizingStatus.IDLE;
  @StateField private volatile long currentStatusStartTime = System.currentTimeMillis();
  @StateField private volatile long lastMajorOptimizingTime;
  @StateField private volatile long lastFullOptimizingTime;
  @StateField private volatile long lastMinorOptimizingTime;
  @StateField private volatile String optimizerGroup;
  @StateField private volatile OptimizingProcess optimizingProcess;
  @StateField private volatile TableConfiguration tableConfiguration;
  @StateField private volatile long processId;
  @StateField private volatile OptimizingEvaluator.PendingInput pendingInput;
  @StateField private volatile OptimizingEvaluator.PendingInput tableSummary;
  private volatile long lastPlanTime;
  private final TableOptimizingMetrics optimizingMetrics;
  private final TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics;
  private final TableSummaryMetrics tableSummaryMetrics;

  protected TableRuntime(
      ServerTableIdentifier tableIdentifier,
      TableRuntimeHandler tableHandler,
      Map<String, String> properties) {
    Preconditions.checkNotNull(tableIdentifier, "ServerTableIdentifier must not be null.");
    Preconditions.checkNotNull(tableHandler, "TableRuntimeHandler must not be null.");
    this.tableHandler = tableHandler;
    this.tableIdentifier = tableIdentifier;
    this.tableConfiguration = TableConfigurations.parseTableConfig(properties);
    this.optimizerGroup = tableConfiguration.getOptimizingConfig().getOptimizerGroup();
    persistTableRuntime();
    optimizingMetrics = new TableOptimizingMetrics(tableIdentifier);
    orphanFilesCleaningMetrics = new TableOrphanFilesCleaningMetrics(tableIdentifier);
    tableSummaryMetrics = new TableSummaryMetrics(tableIdentifier);
  }

  protected TableRuntime(TableRuntimeMeta tableRuntimeMeta, TableRuntimeHandler tableHandler) {
    Preconditions.checkNotNull(tableRuntimeMeta, "TableRuntimeMeta must not be null.");
    Preconditions.checkNotNull(tableHandler, "TableRuntimeHandler must not be null.");
    this.tableHandler = tableHandler;
    this.tableIdentifier =
        ServerTableIdentifier.of(
            tableRuntimeMeta.getTableId(),
            tableRuntimeMeta.getCatalogName(),
            tableRuntimeMeta.getDbName(),
            tableRuntimeMeta.getTableName(),
            tableRuntimeMeta.getFormat());
    this.currentSnapshotId = tableRuntimeMeta.getCurrentSnapshotId();
    this.lastOptimizedSnapshotId = tableRuntimeMeta.getLastOptimizedSnapshotId();
    this.lastOptimizedChangeSnapshotId = tableRuntimeMeta.getLastOptimizedChangeSnapshotId();
    this.currentChangeSnapshotId = tableRuntimeMeta.getCurrentChangeSnapshotId();
    this.currentStatusStartTime = tableRuntimeMeta.getCurrentStatusStartTime();
    this.lastMinorOptimizingTime = tableRuntimeMeta.getLastMinorOptimizingTime();
    this.lastMajorOptimizingTime = tableRuntimeMeta.getLastMajorOptimizingTime();
    this.lastFullOptimizingTime = tableRuntimeMeta.getLastFullOptimizingTime();
    this.optimizerGroup = tableRuntimeMeta.getOptimizerGroup();
    this.tableConfiguration = tableRuntimeMeta.getTableConfig();
    this.processId = tableRuntimeMeta.getOptimizingProcessId();
    this.optimizingStatus =
        tableRuntimeMeta.getTableStatus() == OptimizingStatus.PLANNING
            ? OptimizingStatus.PENDING
            : tableRuntimeMeta.getTableStatus();
    this.pendingInput = tableRuntimeMeta.getPendingInput();
    this.tableSummary = tableRuntimeMeta.getTableSummary();
    optimizingMetrics = new TableOptimizingMetrics(tableIdentifier);
    optimizingMetrics.statusChanged(optimizingStatus, this.currentStatusStartTime);
    optimizingMetrics.lastOptimizingTime(OptimizingType.MINOR, this.lastMinorOptimizingTime);
    optimizingMetrics.lastOptimizingTime(OptimizingType.MAJOR, this.lastMajorOptimizingTime);
    optimizingMetrics.lastOptimizingTime(OptimizingType.FULL, this.lastFullOptimizingTime);
    orphanFilesCleaningMetrics = new TableOrphanFilesCleaningMetrics(tableIdentifier);
    tableSummaryMetrics = new TableSummaryMetrics(tableIdentifier);
    tableSummaryMetrics.refresh(tableSummary);
  }

  public void recover(OptimizingProcess optimizingProcess) {
    if (!optimizingStatus.isProcessing()
        || !Objects.equals(optimizingProcess.getProcessId(), processId)) {
      throw new IllegalStateException("Table runtime and processing are not matched!");
    }
    this.optimizingProcess = optimizingProcess;
  }

  public void registerMetric(MetricRegistry metricRegistry) {
    this.optimizingMetrics.register(metricRegistry);
    this.orphanFilesCleaningMetrics.register(metricRegistry);
    this.tableSummaryMetrics.register(metricRegistry);
  }

  public void dispose() {
    invokeInStateLock(
        () -> {
          doAsTransaction(
              () -> Optional.ofNullable(optimizingProcess).ifPresent(OptimizingProcess::close),
              () ->
                  doAs(
                      TableMetaMapper.class,
                      mapper -> mapper.deleteOptimizingRuntime(tableIdentifier.getId())));
        });
    optimizingMetrics.unregister();
    orphanFilesCleaningMetrics.unregister();
    tableSummaryMetrics.unregister();
  }

  public void beginPlanning() {
    invokeConsistency(
        () -> {
          OptimizingStatus originalStatus = optimizingStatus;
          updateOptimizingStatus(OptimizingStatus.PLANNING);
          persistUpdatingRuntime();
          tableHandler.handleTableChanged(this, originalStatus);
        });
  }

  public void planFailed() {
    invokeConsistency(
        () -> {
          OptimizingStatus originalStatus = optimizingStatus;
          updateOptimizingStatus(OptimizingStatus.PENDING);
          persistUpdatingRuntime();
          tableHandler.handleTableChanged(this, originalStatus);
        });
  }

  public void beginProcess(OptimizingProcess optimizingProcess) {
    invokeConsistency(
        () -> {
          OptimizingStatus originalStatus = optimizingStatus;
          this.optimizingProcess = optimizingProcess;
          this.processId = optimizingProcess.getProcessId();
          updateOptimizingStatus(optimizingProcess.getOptimizingType().getStatus());
          this.pendingInput = null;
          persistUpdatingRuntime();
          tableHandler.handleTableChanged(this, originalStatus);
        });
  }

  public void beginCommitting() {
    invokeConsistency(
        () -> {
          OptimizingStatus originalStatus = optimizingStatus;
          updateOptimizingStatus(OptimizingStatus.COMMITTING);
          persistUpdatingRuntime();
          tableHandler.handleTableChanged(this, originalStatus);
        });
  }

  public void setPendingInput(OptimizingEvaluator.PendingInput pendingInput) {
    invokeConsistency(
        () -> {
          this.pendingInput = pendingInput;
          if (optimizingStatus == OptimizingStatus.IDLE) {
            updateOptimizingStatus(OptimizingStatus.PENDING);
            persistUpdatingRuntime();
            LOG.info(
                "{} status changed from idle to pending with pendingInput {}",
                tableIdentifier,
                pendingInput);
            tableHandler.handleTableChanged(this, OptimizingStatus.IDLE);
          }
        });
  }

  public TableRuntime refresh(AmoroTable<?> table) {
    return invokeConsistency(
        () -> {
          TableConfiguration configuration = tableConfiguration;
          boolean configChanged = updateConfigInternal(table.properties());
          if (refreshSnapshots(table) || configChanged) {
            persistUpdatingRuntime();
          }
          if (configChanged) {
            tableHandler.handleTableChanged(this, configuration);
          }
          return this;
        });
  }

  public void setTableSummary(OptimizingEvaluator.PendingInput tableSummary) {
    invokeConsistency(
        () -> {
          this.tableSummary = tableSummary;
          tableSummaryMetrics.refresh(tableSummary);
          persistUpdatingRuntime();
        });
  }

  /**
   * When there is no task to be optimized, clean pendingInput and update `lastOptimizedSnapshotId`
   * to `currentSnapshotId`.
   */
  public void completeEmptyProcess() {
    invokeConsistency(
        () -> {
          pendingInput = null;
          if (optimizingStatus == OptimizingStatus.PLANNING
              || optimizingStatus == OptimizingStatus.PENDING) {
            updateOptimizingStatus(OptimizingStatus.IDLE);
            lastOptimizedSnapshotId = currentSnapshotId;
            lastOptimizedChangeSnapshotId = currentChangeSnapshotId;
            persistUpdatingRuntime();
            tableHandler.handleTableChanged(this, optimizingStatus);
          }
        });
  }

  public void optimizingNotNecessary() {
    invokeConsistency(
        () -> {
          if (optimizingStatus == OptimizingStatus.IDLE) {
            lastOptimizedSnapshotId = currentSnapshotId;
            lastOptimizedChangeSnapshotId = currentChangeSnapshotId;
            persistUpdatingRuntime();
          }
        });
  }

  /**
   * TODO: this is not final solution
   *
   * @param startTimeMills
   */
  public void resetTaskQuotas(long startTimeMills) {
    invokeInStateLock(
        () -> {
          taskQuotas.clear();
          taskQuotas.addAll(
              getAs(
                  OptimizingMapper.class,
                  mapper ->
                      mapper.selectTaskQuotasByTime(tableIdentifier.getId(), startTimeMills)));
        });
  }

  public void completeProcess(boolean success) {
    invokeConsistency(
        () -> {
          OptimizingStatus originalStatus = optimizingStatus;
          OptimizingType processType = optimizingProcess.getOptimizingType();
          if (success) {
            lastOptimizedSnapshotId = optimizingProcess.getTargetSnapshotId();
            lastOptimizedChangeSnapshotId = optimizingProcess.getTargetChangeSnapshotId();
            if (processType == OptimizingType.MINOR) {
              lastMinorOptimizingTime = optimizingProcess.getPlanTime();
            } else if (processType == OptimizingType.MAJOR) {
              lastMajorOptimizingTime = optimizingProcess.getPlanTime();
            } else if (processType == OptimizingType.FULL) {
              lastFullOptimizingTime = optimizingProcess.getPlanTime();
            }
          }
          optimizingMetrics.processComplete(processType, success, optimizingProcess.getPlanTime());
          updateOptimizingStatus(OptimizingStatus.IDLE);
          optimizingProcess = null;
          persistUpdatingRuntime();
          tableHandler.handleTableChanged(this, originalStatus);
        });
  }

  private void updateOptimizingStatus(OptimizingStatus status) {
    this.optimizingStatus = status;
    this.currentStatusStartTime = System.currentTimeMillis();
    this.optimizingMetrics.statusChanged(status, currentStatusStartTime);
  }

  private boolean refreshSnapshots(AmoroTable<?> amoroTable) {
    MixedTable table = (MixedTable) amoroTable.originalTable();
    tableSummaryMetrics.refreshSnapshots(table);
    long lastSnapshotId = currentSnapshotId;
    if (table.isKeyedTable()) {
      long changeSnapshotId = currentChangeSnapshotId;
      ChangeTable changeTable = table.asKeyedTable().changeTable();
      BaseTable baseTable = table.asKeyedTable().baseTable();

      currentChangeSnapshotId = doRefreshSnapshots(changeTable);
      currentSnapshotId = doRefreshSnapshots(baseTable);

      if (currentSnapshotId != lastSnapshotId || currentChangeSnapshotId != changeSnapshotId) {
        LOG.info(
            "Refreshing table {} with base snapshot id {} and change snapshot id {}",
            tableIdentifier,
            currentSnapshotId,
            currentChangeSnapshotId);
        return true;
      }
    } else {
      currentSnapshotId = doRefreshSnapshots((UnkeyedTable) table);
      if (currentSnapshotId != lastSnapshotId) {
        LOG.info(
            "Refreshing table {} with base snapshot id {}", tableIdentifier, currentSnapshotId);
        return true;
      }
    }
    return false;
  }

  /**
   * Refresh snapshots for table.
   *
   * @param table - table
   * @return refreshed snapshotId
   */
  private long doRefreshSnapshots(UnkeyedTable table) {
    long currentSnapshotId = AmoroServiceConstants.INVALID_SNAPSHOT_ID;
    Snapshot currentSnapshot = IcebergTableUtil.getSnapshot(table, false);
    if (currentSnapshot != null) {
      currentSnapshotId = currentSnapshot.snapshotId();
    }

    optimizingMetrics.nonMaintainedSnapshotTime(currentSnapshot);
    optimizingMetrics.lastOptimizingSnapshotTime(
        IcebergTableUtil.findLatestOptimizingSnapshot(table).orElse(null));

    return currentSnapshotId;
  }

  public OptimizingEvaluator.PendingInput getPendingInput() {
    return pendingInput;
  }

  public OptimizingEvaluator.PendingInput getTableSummary() {
    return tableSummary;
  }

  private boolean updateConfigInternal(Map<String, String> properties) {
    TableConfiguration newTableConfig = TableConfigurations.parseTableConfig(properties);
    if (tableConfiguration.equals(newTableConfig)) {
      return false;
    }
    if (!Objects.equals(
        this.optimizerGroup, newTableConfig.getOptimizingConfig().getOptimizerGroup())) {
      if (optimizingProcess != null) {
        optimizingProcess.close();
      }
      this.optimizerGroup = newTableConfig.getOptimizingConfig().getOptimizerGroup();
    }
    this.tableConfiguration = newTableConfig;
    return true;
  }

  public void addTaskQuota(TaskRuntime.TaskQuota taskQuota) {
    doAs(OptimizingMapper.class, mapper -> mapper.insertTaskQuota(taskQuota));
    taskQuotas.add(taskQuota);
    long validTime = System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME;
    this.taskQuotas.removeIf(task -> task.checkExpired(validTime));
  }

  private void persistTableRuntime() {
    doAs(TableMetaMapper.class, mapper -> mapper.insertTableRuntime(this));
  }

  private void persistUpdatingRuntime() {
    doAs(TableMetaMapper.class, mapper -> mapper.updateTableRuntime(this));
  }

  public OptimizingProcess getOptimizingProcess() {
    return optimizingProcess;
  }

  public long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public void updateCurrentChangeSnapshotId(long snapshotId) {
    this.currentChangeSnapshotId = snapshotId;
  }

  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public TableFormat getFormat() {
    return tableIdentifier.getFormat();
  }

  public OptimizingStatus getOptimizingStatus() {
    return optimizingStatus;
  }

  public long getLastOptimizedSnapshotId() {
    return lastOptimizedSnapshotId;
  }

  public long getLastOptimizedChangeSnapshotId() {
    return lastOptimizedChangeSnapshotId;
  }

  public long getCurrentChangeSnapshotId() {
    return currentChangeSnapshotId;
  }

  public long getCurrentStatusStartTime() {
    return currentStatusStartTime;
  }

  public long getLastMajorOptimizingTime() {
    return lastMajorOptimizingTime;
  }

  public long getLastFullOptimizingTime() {
    return lastFullOptimizingTime;
  }

  public long getLastMinorOptimizingTime() {
    return lastMinorOptimizingTime;
  }

  public TableConfiguration getTableConfiguration() {
    return tableConfiguration;
  }

  public OptimizingConfig getOptimizingConfig() {
    return tableConfiguration.getOptimizingConfig();
  }

  public boolean isOptimizingEnabled() {
    return tableConfiguration.getOptimizingConfig().isEnabled();
  }

  public Double getTargetQuota() {
    return tableConfiguration.getOptimizingConfig().getTargetQuota();
  }

  public String getOptimizerGroup() {
    return optimizerGroup;
  }

  public TableOrphanFilesCleaningMetrics getOrphanFilesCleaningMetrics() {
    return orphanFilesCleaningMetrics;
  }

  public void setCurrentChangeSnapshotId(long currentChangeSnapshotId) {
    this.currentChangeSnapshotId = currentChangeSnapshotId;
  }

  public int getMaxExecuteRetryCount() {
    return tableConfiguration.getOptimizingConfig().getMaxExecuteRetryCount();
  }

  public long getNewestProcessId() {
    return processId;
  }

  public long getLastPlanTime() {
    return lastPlanTime;
  }

  public void setLastPlanTime(long lastPlanTime) {
    this.lastPlanTime = lastPlanTime;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableIdentifier", tableIdentifier)
        .add("currentSnapshotId", currentSnapshotId)
        .add("lastOptimizedSnapshotId", lastOptimizedSnapshotId)
        .add("lastOptimizedChangeSnapshotId", lastOptimizedChangeSnapshotId)
        .add("optimizingStatus", optimizingStatus)
        .add("currentStatusStartTime", currentStatusStartTime)
        .add("lastMajorOptimizingTime", lastMajorOptimizingTime)
        .add("lastFullOptimizingTime", lastFullOptimizingTime)
        .add("lastMinorOptimizingTime", lastMinorOptimizingTime)
        .add("tableConfiguration", tableConfiguration)
        .toString();
  }

  public long getQuotaTime() {
    long calculatingEndTime = System.currentTimeMillis();
    long calculatingStartTime = calculatingEndTime - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME;
    taskQuotas.removeIf(task -> task.checkExpired(calculatingStartTime));
    long finishedTaskQuotaTime =
        taskQuotas.stream()
            .mapToLong(taskQuota -> taskQuota.getQuotaTime(calculatingStartTime))
            .sum();
    return optimizingProcess == null
        ? finishedTaskQuotaTime
        : finishedTaskQuotaTime
            + optimizingProcess.getRunningQuotaTime(calculatingStartTime, calculatingEndTime);
  }

  public double calculateQuotaOccupy() {
    return new BigDecimal(
            (double) getQuotaTime()
                / AmoroServiceConstants.QUOTA_LOOK_BACK_TIME
                / tableConfiguration.getOptimizingConfig().getTargetQuota())
        .setScale(4, RoundingMode.HALF_UP)
        .doubleValue();
  }

  /**
   * Check if operation are blocked now.
   *
   * @param operation - operation to check
   * @return true if blocked
   */
  public boolean isBlocked(BlockableOperation operation) {
    List<TableBlocker> tableBlockers =
        getAs(
            TableBlockerMapper.class,
            mapper ->
                mapper.selectBlockers(
                    tableIdentifier.getCatalog(),
                    tableIdentifier.getDatabase(),
                    tableIdentifier.getTableName(),
                    System.currentTimeMillis()));
    return TableBlocker.conflict(operation, tableBlockers);
  }
}

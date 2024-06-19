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
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.StateField;
import org.apache.amoro.api.config.OptimizingConfig;
import org.apache.amoro.api.config.TableConfiguration;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.exception.BlockerConflictException;
import org.apache.amoro.server.exception.ObjectNotExistsException;
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
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.blocker.RenewableBlocker;
import org.apache.iceberg.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
  private volatile long lastPlanTime;
  private final TableOptimizingMetrics optimizingMetrics;
  private final ReentrantLock blockerLock = new ReentrantLock();

  protected TableRuntime(
      ServerTableIdentifier tableIdentifier,
      TableRuntimeHandler tableHandler,
      Map<String, String> properties) {
    Preconditions.checkNotNull(tableIdentifier, "ServerTableIdentifier must not be null.");
    Preconditions.checkNotNull(tableHandler, "TableRuntimeHandler must not be null.");
    this.tableHandler = tableHandler;
    this.tableIdentifier = tableIdentifier;
    this.tableConfiguration = TableConfiguration.parseConfig(properties);
    this.optimizerGroup = tableConfiguration.getOptimizingConfig().getOptimizerGroup();
    persistTableRuntime();
    optimizingMetrics = new TableOptimizingMetrics(tableIdentifier);
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
    optimizingMetrics = new TableOptimizingMetrics(tableIdentifier);
    optimizingMetrics.statusChanged(optimizingStatus, this.currentStatusStartTime);
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
            persistUpdatingRuntime();
            tableHandler.handleTableChanged(this, optimizingStatus);
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
          updateOptimizingStatus(OptimizingStatus.IDLE);
          optimizingProcess = null;
          persistUpdatingRuntime();
          optimizingMetrics.processComplete(processType, success);
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
    if (table.isKeyedTable()) {
      long lastSnapshotId = currentSnapshotId;
      long changeSnapshotId = currentChangeSnapshotId;
      currentSnapshotId = IcebergTableUtil.getSnapshotId(table.asKeyedTable().baseTable(), false);
      currentChangeSnapshotId =
          IcebergTableUtil.getSnapshotId(table.asKeyedTable().changeTable(), false);
      if (currentSnapshotId != lastSnapshotId || currentChangeSnapshotId != changeSnapshotId) {
        LOG.info(
            "Refreshing table {} with base snapshot id {} and change snapshot id {}",
            tableIdentifier,
            currentSnapshotId,
            currentChangeSnapshotId);
        return true;
      }
    } else {
      long lastSnapshotId = currentSnapshotId;
      Snapshot currentSnapshot = table.asUnkeyedTable().currentSnapshot();
      currentSnapshotId = currentSnapshot == null ? -1 : currentSnapshot.snapshotId();
      if (currentSnapshotId != lastSnapshotId) {
        LOG.info(
            "Refreshing table {} with base snapshot id {}", tableIdentifier, currentSnapshotId);
        return true;
      }
    }
    return false;
  }

  public OptimizingEvaluator.PendingInput getPendingInput() {
    return pendingInput;
  }

  private boolean updateConfigInternal(Map<String, String> properties) {
    TableConfiguration newTableConfig = TableConfiguration.parseConfig(properties);
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
   * Get all valid blockers.
   *
   * @return all valid blockers
   */
  public List<TableBlocker> getBlockers() {
    blockerLock.lock();
    try {
      return getAs(
          TableBlockerMapper.class,
          mapper -> mapper.selectBlockers(tableIdentifier, System.currentTimeMillis()));
    } finally {
      blockerLock.unlock();
    }
  }

  /**
   * Block some operations for table.
   *
   * @param operations - operations to be blocked
   * @param properties -
   * @param blockerTimeout -
   * @return TableBlocker if success
   */
  public TableBlocker block(
      List<BlockableOperation> operations,
      @Nonnull Map<String, String> properties,
      long blockerTimeout) {
    Preconditions.checkNotNull(operations, "operations should not be null");
    Preconditions.checkArgument(!operations.isEmpty(), "operations should not be empty");
    Preconditions.checkArgument(blockerTimeout > 0, "blocker timeout must > 0");
    blockerLock.lock();
    try {
      long now = System.currentTimeMillis();
      List<TableBlocker> tableBlockers =
          getAs(TableBlockerMapper.class, mapper -> mapper.selectBlockers(tableIdentifier, now));
      if (conflict(operations, tableBlockers)) {
        throw new BlockerConflictException(operations + " is conflict with " + tableBlockers);
      }
      TableBlocker tableBlocker =
          buildTableBlocker(tableIdentifier, operations, properties, now, blockerTimeout);
      doAs(TableBlockerMapper.class, mapper -> mapper.insertBlocker(tableBlocker));
      return tableBlocker;
    } finally {
      blockerLock.unlock();
    }
  }

  /**
   * Renew blocker.
   *
   * @param blockerId - blockerId
   * @param blockerTimeout - timeout
   * @throws IllegalStateException if blocker not exist
   */
  public long renew(String blockerId, long blockerTimeout) {
    blockerLock.lock();
    try {
      long now = System.currentTimeMillis();
      TableBlocker tableBlocker =
          getAs(
              TableBlockerMapper.class,
              mapper -> mapper.selectBlocker(Long.parseLong(blockerId), now));
      if (tableBlocker == null) {
        throw new ObjectNotExistsException("Blocker " + blockerId + " of " + tableIdentifier);
      }
      long expirationTime = now + blockerTimeout;
      doAs(
          TableBlockerMapper.class,
          mapper -> mapper.updateBlockerExpirationTime(Long.parseLong(blockerId), expirationTime));
      return expirationTime;
    } finally {
      blockerLock.unlock();
    }
  }

  /**
   * Release blocker, succeed when blocker not exist.
   *
   * @param blockerId - blockerId
   */
  public void release(String blockerId) {
    blockerLock.lock();
    try {
      doAs(TableBlockerMapper.class, mapper -> mapper.deleteBlocker(Long.parseLong(blockerId)));
    } finally {
      blockerLock.unlock();
    }
  }

  /**
   * Check if operation are blocked now.
   *
   * @param operation - operation to check
   * @return true if blocked
   */
  public boolean isBlocked(BlockableOperation operation) {
    blockerLock.lock();
    try {
      List<TableBlocker> tableBlockers =
          getAs(
              TableBlockerMapper.class,
              mapper -> mapper.selectBlockers(tableIdentifier, System.currentTimeMillis()));
      return conflict(operation, tableBlockers);
    } finally {
      blockerLock.unlock();
    }
  }

  private boolean conflict(
      List<BlockableOperation> blockableOperations, List<TableBlocker> blockers) {
    return blockableOperations.stream().anyMatch(operation -> conflict(operation, blockers));
  }

  private boolean conflict(BlockableOperation blockableOperation, List<TableBlocker> blockers) {
    return blockers.stream()
        .anyMatch(blocker -> blocker.getOperations().contains(blockableOperation.name()));
  }

  private TableBlocker buildTableBlocker(
      ServerTableIdentifier tableIdentifier,
      List<BlockableOperation> operations,
      Map<String, String> properties,
      long now,
      long blockerTimeout) {
    TableBlocker tableBlocker = new TableBlocker();
    tableBlocker.setTableIdentifier(tableIdentifier);
    tableBlocker.setCreateTime(now);
    tableBlocker.setExpirationTime(now + blockerTimeout);
    tableBlocker.setOperations(
        operations.stream().map(BlockableOperation::name).collect(Collectors.toList()));
    HashMap<String, String> propertiesOfTableBlocker = new HashMap<>(properties);
    propertiesOfTableBlocker.put(RenewableBlocker.BLOCKER_TIMEOUT, blockerTimeout + "");
    tableBlocker.setProperties(propertiesOfTableBlocker);
    return tableBlocker;
  }
}

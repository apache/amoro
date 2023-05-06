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

package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.optimizing.OptimizingConfig;
import com.netease.arctic.server.optimizing.OptimizingProcess;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.optimizing.TaskRuntime;
import com.netease.arctic.server.optimizing.plan.OptimizingEvaluator;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.persistence.mapper.TableBlockerMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.blocker.TableBlocker;
import com.netease.arctic.server.utils.IcebergTableUtils;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.blocker.RenewableBlocker;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class TableRuntime extends PersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(TableRuntime.class);

  private final TableRuntimeInitializer initializer;
  private final TableRuntimeHandler headHandler;
  private final ServerTableIdentifier tableIdentifier;
  private final List<TaskRuntime.TaskQuota> taskQuotas = Collections.synchronizedList(new ArrayList<>());
  private final Lock lock = new ReentrantLock();
  // for unKeyedTable or base table
  private volatile long currentSnapshotId = ArcticServiceConstants.INVALID_SNAPSHOT_ID;
  private volatile long lastOptimizedSnapshotId = ArcticServiceConstants.INVALID_SNAPSHOT_ID;
  // for change table
  private volatile long currentChangeSnapshotId = ArcticServiceConstants.INVALID_SNAPSHOT_ID;
  private volatile OptimizingStatus optimizingStatus = OptimizingStatus.IDLE;
  private volatile long currentStatusStartTime = System.currentTimeMillis();
  private volatile long lastMajorOptimizingTime;
  private volatile long lastFullOptimizingTime;
  private volatile long lastMinorOptimizingTime;
  private volatile String optimizerGroup;
  private volatile OptimizingProcess optimizingProcess;
  private volatile TableConfiguration tableConfiguration;
  private volatile long processId;
  private volatile OptimizingEvaluator.PendingInput pendingInput;

  private final ReentrantLock blockerLock = new ReentrantLock();

  protected TableRuntime(ServerTableIdentifier tableIdentifier, TableRuntimeInitializer initializer) {
    ArcticTable table = initializer.loadTable(tableIdentifier);
    this.initializer = initializer;
    this.headHandler = initializer.getHeadHandler();
    this.tableIdentifier = tableIdentifier;
    this.tableConfiguration = TableConfiguration.parseConfig(table.properties());
    this.optimizerGroup = tableConfiguration.getOptimizingConfig().getOptimizerGroup();
    if (headHandler != null) {
      headHandler.fireTableAdded(table, this);
    }
    persistTableRuntime();
  }

  protected TableRuntime(TableRuntimeMeta tableRuntimeMeta, TableRuntimeInitializer initializer) {
    this.initializer = initializer;
    this.headHandler = initializer.getHeadHandler();
    this.tableIdentifier = ServerTableIdentifier.of(tableRuntimeMeta.getTableId(), tableRuntimeMeta.getCatalogName(),
        tableRuntimeMeta.getDbName(), tableRuntimeMeta.getTableName());
    this.currentSnapshotId = tableRuntimeMeta.getCurrentSnapshotId();
    this.lastOptimizedSnapshotId = tableRuntimeMeta.getLastOptimizedSnapshotId();
    this.currentChangeSnapshotId = tableRuntimeMeta.getCurrentChangeSnapshotId();
    this.currentStatusStartTime = tableRuntimeMeta.getCurrentStatusStartTime();
    this.lastMinorOptimizingTime = tableRuntimeMeta.getLastMinorOptimizingTime();
    this.lastMajorOptimizingTime = tableRuntimeMeta.getLastMajorOptimizingTime();
    this.lastFullOptimizingTime = tableRuntimeMeta.getLastFullOptimizingTime();
    this.optimizerGroup = tableRuntimeMeta.getOptimizerGroup();
    this.tableConfiguration = tableRuntimeMeta.getTableConfig();
    this.processId = tableRuntimeMeta.getOptimizingProcessId();
    this.optimizingStatus = tableRuntimeMeta.getTableStatus();
  }

  protected void recover(OptimizingProcess optimizingProcess) {
    if (!optimizingStatus.isProcessing() || !Objects.equals(optimizingProcess.getProcessId(), processId)) {
      throw new IllegalStateException("Table runtime and processing are not matched!");
    }
    this.optimizingProcess = optimizingProcess;
  }

  public void dispose() {
    lock.lock();
    try {
      doAsTransaction(
          () -> Optional.ofNullable(optimizingProcess).ifPresent(OptimizingProcess::close),
          () -> doAs(TableMetaMapper.class, mapper ->
              mapper.deleteOptimizingRuntime(tableIdentifier.getId()))
      );
    } finally {
      lock.unlock();
    }
    if (headHandler != null) {
      headHandler.fireTableRemoved(this);
    }
  }

  public void refresh() {
    lock.lock();
    try {
      if (optimizingStatus == OptimizingStatus.IDLE) {
        ArcticTable table = initializer.loadTable(tableIdentifier);
        boolean hasNewSnapshots = false;
        if (table.isKeyedTable()) {
          long lastSnapshotId = currentSnapshotId;
          long changeSnapshotId = currentChangeSnapshotId;
          currentSnapshotId = IcebergTableUtils.getSnapshotId(table.asKeyedTable().baseTable(), false);
          currentChangeSnapshotId = IcebergTableUtils.getSnapshotId(table.asKeyedTable().changeTable(), false);
          if (currentSnapshotId != lastSnapshotId || currentChangeSnapshotId != changeSnapshotId) {
            hasNewSnapshots = true;
            LOG.info("Refreshing table {} with base snapshot id {} and change snapshot id {}", tableIdentifier,
                currentSnapshotId, currentChangeSnapshotId);
          }
        } else {
          long lastSnapshotId = currentSnapshotId;
          Snapshot currentSnapshot = table.asUnkeyedTable().currentSnapshot();
          currentSnapshotId = currentSnapshot == null ? -1 : currentSnapshot.snapshotId();
          if (currentSnapshotId != lastSnapshotId) {
            hasNewSnapshots = true;
            LOG.info("Refreshing table {} with base snapshot id {}", tableIdentifier, currentSnapshotId);
          }
        }
        if (hasNewSnapshots) {
          evaluatePendingInput(table);
        }
        TableConfiguration configuration = tableConfiguration;
        if (hasNewSnapshots || updateConfigInternal(table.properties())) {
          persistUpdatingRuntime();
        }
        if (configuration != tableConfiguration && headHandler != null) {
          headHandler.fireConfigChanged(this, configuration);
        }
        if (optimizingStatus == OptimizingStatus.PENDING && headHandler != null) {
          headHandler.fireStatusChanged(this, OptimizingStatus.IDLE);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  public ArcticTable loadTable() {
    return initializer.loadTable(tableIdentifier);
  }

  public OptimizingEvaluator.PendingInput getPendingInput() {
    return pendingInput;
  }

  private void evaluatePendingInput(ArcticTable table) {
    OptimizingEvaluator evaluator = new OptimizingEvaluator(this, table);
    if (evaluator.isNecessary()) {
      pendingInput = evaluator.getPendingInput();
      optimizingStatus = OptimizingStatus.PENDING;
    }
  }

  public void tryUpdatingConfig(Map<String, String> properties) {
    lock.lock();
    TableConfiguration originalConfig = this.tableConfiguration;
    try {
      if (updateConfigInternal(properties)) {
        persistUpdatingRuntime();
        if (headHandler != null) {
          headHandler.fireConfigChanged(this, originalConfig);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  private boolean updateConfigInternal(Map<String, String> properties) {
    TableConfiguration newTableConfig = TableConfiguration.parseConfig(properties);
    if (!tableConfiguration.equals(newTableConfig)) {
      if (!Objects.equals(this.optimizerGroup, getOptimizerGroup())) {
        if (optimizingProcess != null) {
          optimizingProcess.close();
        }
        this.optimizerGroup = newTableConfig.getOptimizingConfig().getOptimizerGroup();
      }
      this.tableConfiguration = newTableConfig;
      return true;
    }
    return false;
  }

  public void beginProcess(OptimizingProcess optimizingProcess) {
    lock.lock();
    try {
      OptimizingStatus originalStatus = optimizingStatus;
      this.optimizingProcess = optimizingProcess;
      this.processId = optimizingProcess.getProcessId();
      this.currentStatusStartTime = System.currentTimeMillis();
      this.optimizingStatus = optimizingProcess.getOptimizingType().getStatus();
      persistUpdatingRuntime();
      if (headHandler != null) {
        headHandler.fireStatusChanged(this, originalStatus);
      }
    } finally {
      lock.unlock();
    }
  }

  public void beginCommitting() {
    lock.lock();
    try {
      OptimizingStatus originalStatus = optimizingStatus;
      this.currentStatusStartTime = System.currentTimeMillis();
      this.optimizingStatus = OptimizingStatus.COMMITTING;
      persistUpdatingRuntime();
      if (headHandler != null) {
        headHandler.fireStatusChanged(this, originalStatus);
      }
    } finally {
      lock.unlock();
    }
  }

  public void addTaskQuota(TaskRuntime.TaskQuota taskQuota) {
    doAs(OptimizingMapper.class, mapper -> mapper.insertTaskQuota(taskQuota));
    taskQuotas.add(taskQuota);
    long validTime = System.currentTimeMillis() - ArcticServiceConstants.QUOTA_LOOK_BACK_TIME;
    this.taskQuotas.removeIf(task -> task.checkExpired(validTime));
  }

  public void resetTaskQuotas(long startTimeMills) {
    lock.lock();
    try {
      taskQuotas.clear();
      taskQuotas.addAll(getAs(OptimizingMapper.class, mapper ->
          mapper.selectTaskQuotasByTime(tableIdentifier.getId(), startTimeMills)));
    } finally {
      lock.unlock();
    }
  }

  public void completeProcess(boolean success) {
    lock.lock();
    try {
      OptimizingStatus originalStatus = optimizingStatus;
      currentStatusStartTime = System.currentTimeMillis();
      optimizingStatus = OptimizingStatus.IDLE;
      if (success) {
        lastOptimizedSnapshotId = optimizingProcess.getTargetSnapshotId();
        if (optimizingProcess.getOptimizingType() == OptimizingType.MINOR) {
          lastMinorOptimizingTime = optimizingProcess.getPlanTime();
        } else if (optimizingProcess.getOptimizingType() == OptimizingType.MAJOR) {
          lastMajorOptimizingTime = optimizingProcess.getPlanTime();
        } else if (optimizingProcess.getOptimizingType() == OptimizingType.FULL_MAJOR) {
          lastFullOptimizingTime = optimizingProcess.getPlanTime();
        }
      }
      optimizingProcess = null;
      evaluatePendingInput(loadTable());
      persistUpdatingRuntime();
      if (headHandler != null) {
        headHandler.fireStatusChanged(this, originalStatus);
      }
    } finally {
      lock.unlock();
    }
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

  public boolean hasNewSnapshot() {
    return currentSnapshotId != lastOptimizedSnapshotId;
  }

  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public OptimizingStatus getOptimizingStatus() {
    return optimizingStatus;
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

  public long getTargetSize() {
    return tableConfiguration.getOptimizingConfig().getTargetSize();
  }

  public long getCurrentChangeSnapshotId() {
    return currentChangeSnapshotId;
  }

  public void setCurrentChangeSnapshotId(long currentChangeSnapshotId) {
    this.currentChangeSnapshotId = currentChangeSnapshotId;
  }

  public int getMaxRetryCount() {
    return tableConfiguration.getOptimizingConfig().getMaxRetryCount();
  }

  public long getNewestProcessId() {
    return processId;
  }

  @Override
  public String toString() {
    return "TableOptimizingRuntime{" +
        "tableIdentifier=" + tableIdentifier +
        ", currentSnapshotId=" + currentSnapshotId +
        ", currentChangeSnapshotId=" + currentChangeSnapshotId +
        ", status=" + optimizingStatus +
        ", currentStatusStartTime=" + currentStatusStartTime +
        ", lastMajorOptimizingTime=" + lastMajorOptimizingTime +
        ", lastFullOptimizingTime=" + lastFullOptimizingTime +
        ", lastMinorOptimizingTime=" + lastMinorOptimizingTime +
        ", tableConfiguration=" + tableConfiguration +
        '}';
  }

  public long getQuotaTime() {
    long calculatingEndTime = System.currentTimeMillis();
    long calculatingStartTime = calculatingEndTime - ArcticServiceConstants.QUOTA_LOOK_BACK_TIME;
    taskQuotas.removeIf(task -> task.checkExpired(calculatingStartTime));
    return taskQuotas.stream().mapToLong(taskQuota -> taskQuota.getQuotaTime(calculatingStartTime)).sum() +
        optimizingProcess.getQuotaTime(calculatingStartTime, calculatingEndTime);
  }

  public double calculateQuotaOccupy() {
    return getQuotaTime() / tableConfiguration.getOptimizingConfig().getTargetQuota();
  }

  /**
   * Get all valid blockers.
   *
   * @return all valid blockers
   */
  public List<TableBlocker> getBlockers() {
    blockerLock.lock();
    try {
      return getAs(TableBlockerMapper.class,
          mapper -> mapper.selectBlockers(tableIdentifier, System.currentTimeMillis()));
    } catch (Exception e) {
      LOG.error("failed to get blockers for {}", tableIdentifier, e);
      throw e;
    } finally {
      blockerLock.unlock();
    }
  }

  /**
   * Block some operations for table.
   *
   * @param operations     - operations to be blocked
   * @param properties     -
   * @param blockerTimeout -
   * @return TableBlocker if success
   * @throws OperationConflictException when operations have been blocked
   */
  public TableBlocker block(List<BlockableOperation> operations, @Nonnull Map<String, String> properties,
                            long blockerTimeout) throws OperationConflictException {
    Preconditions.checkNotNull(operations, "operations should not be null");
    Preconditions.checkArgument(!operations.isEmpty(), "operations should not be empty");
    Preconditions.checkArgument(blockerTimeout > 0, "blocker timeout must > 0");
    blockerLock.lock();
    try {
      long now = System.currentTimeMillis();
      List<TableBlocker> tableBlockers =
          getAs(TableBlockerMapper.class, mapper -> mapper.selectBlockers(tableIdentifier, now));
      if (conflict(operations, tableBlockers)) {
        throw new OperationConflictException(operations + " is conflict with " + tableBlockers);
      }
      TableBlocker tableBlocker = buildTableBlocker(tableIdentifier, operations, properties, now, blockerTimeout);
      doAs(TableBlockerMapper.class, mapper -> mapper.insertBlocker(tableBlocker));
      return tableBlocker;
    } catch (OperationConflictException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("failed to block {} for {}", operations, tableIdentifier, e);
      throw e;
    } finally {
      blockerLock.unlock();
    }
  }

  /**
   * Renew blocker.
   *
   * @param blockerId      - blockerId
   * @param blockerTimeout - timeout
   * @throws IllegalStateException if blocker not exist
   */
  public long renew(String blockerId, long blockerTimeout) throws NoSuchObjectException {
    blockerLock.lock();
    try {
      long now = System.currentTimeMillis();
      TableBlocker tableBlocker =
          getAs(TableBlockerMapper.class, mapper -> mapper.selectBlocker(Long.parseLong(blockerId), now));
      if (tableBlocker == null) {
        throw new NoSuchObjectException(
            tableIdentifier + " illegal blockerId " + blockerId + ", it may be released or expired");
      }
      long expirationTime = now + blockerTimeout;
      doAs(TableBlockerMapper.class,
          mapper -> mapper.updateBlockerExpirationTime(Long.parseLong(blockerId), expirationTime));
      return expirationTime;
    } catch (NoSuchObjectException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("failed to renew blocker {} for {}", blockerId, tableIdentifier, e);
      throw e;
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
    } catch (Exception e) {
      LOG.error("failed to release blocker {} for {}", blockerId, tableIdentifier, e);
      throw e;
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
          getAs(TableBlockerMapper.class, mapper -> mapper.selectBlockers(tableIdentifier, System.currentTimeMillis()));
      return conflict(operation, tableBlockers);
    } catch (Exception e) {
      LOG.error("failed to check is blocked for {} {}", tableIdentifier, operation, e);
      throw e;
    } finally {
      blockerLock.unlock();
    }
  }

  private boolean conflict(List<BlockableOperation> blockableOperations, List<TableBlocker> blockers) {
    return blockableOperations.stream()
        .anyMatch(operation -> conflict(operation, blockers));
  }

  private boolean conflict(BlockableOperation blockableOperation, List<TableBlocker> blockers) {
    return blockers.stream()
        .anyMatch(blocker -> blocker.getOperations().contains(blockableOperation.name()));
  }

  private TableBlocker buildTableBlocker(ServerTableIdentifier tableIdentifier, List<BlockableOperation> operations,
                                         Map<String, String> properties, long now, long blockerTimeout) {
    TableBlocker tableBlocker = new TableBlocker();
    tableBlocker.setTableIdentifier(tableIdentifier);
    tableBlocker.setCreateTime(now);
    tableBlocker.setExpirationTime(now + blockerTimeout);
    tableBlocker.setOperations(operations.stream().map(BlockableOperation::name).collect(Collectors.toList()));
    HashMap<String, String> propertiesOfTableBlocker = new HashMap<>(properties);
    propertiesOfTableBlocker.put(RenewableBlocker.BLOCKER_TIMEOUT, blockerTimeout + "");
    tableBlocker.setProperties(propertiesOfTableBlocker);
    return tableBlocker;
  }
}

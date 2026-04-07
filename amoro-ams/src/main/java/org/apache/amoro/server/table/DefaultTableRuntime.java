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

package org.apache.amoro.server.table;

import org.apache.amoro.Action;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.SupportsProcessPlugins;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.iceberg.Constants;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TableRuntimeOptimizingState;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.process.AmoroProcess;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.blocker.TableBlocker;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.amoro.server.table.cleanup.TableRuntimeCleanupState;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.server.utils.SnowflakeIdGenerator;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeStore;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/** Default table runtime implementation. */
public class DefaultTableRuntime extends AbstractTableRuntime
    implements TableRuntime, SupportsProcessPlugins {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTableRuntime.class);

  private static final SnowflakeIdGenerator ID_GENERATOR = new SnowflakeIdGenerator();

  private static final StateKey<TableRuntimeOptimizingState> OPTIMIZING_STATE_KEY =
      StateKey.stateKey("optimizing_state")
          .jsonType(TableRuntimeOptimizingState.class)
          .defaultValue(new TableRuntimeOptimizingState());

  private static final StateKey<AbstractOptimizingEvaluator.PendingInput> PENDING_INPUT_KEY =
      StateKey.stateKey("pending_input")
          .jsonType(AbstractOptimizingEvaluator.PendingInput.class)
          .defaultValue(new AbstractOptimizingEvaluator.PendingInput());

  private static final StateKey<TableRuntimeCleanupState> CLEANUP_STATE_KEY =
      StateKey.stateKey("cleanup_state")
          .jsonType(TableRuntimeCleanupState.class)
          .defaultValue(new TableRuntimeCleanupState());

  private static final StateKey<Long> PROCESS_ID_KEY =
      StateKey.stateKey("process_id").longType().defaultValue(0L);

  public static final List<StateKey<?>> REQUIRED_STATES =
      Lists.newArrayList(
          OPTIMIZING_STATE_KEY, PENDING_INPUT_KEY, PROCESS_ID_KEY, CLEANUP_STATE_KEY);

  private final Map<Action, TableProcessContainer> processContainerMap = Maps.newConcurrentMap();
  private final TableOptimizingMetrics optimizingMetrics;
  private final TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics;
  private final TableSummaryMetrics tableSummaryMetrics;
  private volatile long lastPlanTime;
  private volatile OptimizingProcess optimizingProcess;
  private volatile OptimizingType pendingCronType;
  private final List<TaskRuntime.TaskQuota> taskQuotas = new CopyOnWriteArrayList<>();

  public DefaultTableRuntime(TableRuntimeStore store) {
    super(store);
    this.optimizingMetrics =
        new TableOptimizingMetrics(store.getTableIdentifier(), store.getGroupName());
    this.orphanFilesCleaningMetrics =
        new TableOrphanFilesCleaningMetrics(store.getTableIdentifier());
    this.tableSummaryMetrics = new TableSummaryMetrics(store.getTableIdentifier());
  }

  public void recover(OptimizingProcess optimizingProcess) {
    if (!getOptimizingStatus().isProcessing()
        || !Objects.equals(optimizingProcess.getProcessId(), getProcessId())) {
      throw new IllegalStateException("Table runtime and processing are not matched!");
    }
    this.optimizingProcess = optimizingProcess;
    if (this.optimizingProcess.getStatus() == ProcessStatus.SUCCESS) {
      completeProcess(true);
    }
  }

  @Override
  public void registerMetric(MetricRegistry metricRegistry) {
    // TODO: extract method to interface.
    this.optimizingMetrics.register(metricRegistry);
    this.orphanFilesCleaningMetrics.register(metricRegistry);
    this.tableSummaryMetrics.register(metricRegistry);
  }

  @Override
  public AmoroProcess trigger(Action action) {
    return Optional.ofNullable(processContainerMap.get(action))
        .map(container -> container.trigger(action))
        // Define a related exception
        .orElseThrow(() -> new IllegalArgumentException("No ProcessFactory for action " + action));
  }

  @Override
  public void install(Action action, ProcessFactory processFactory) {
    if (processContainerMap.putIfAbsent(action, new TableProcessContainer(processFactory))
        != null) {
      throw new IllegalStateException("ProcessFactory for action " + action + " already exists");
    }
  }

  @Override
  public boolean enabled(Action action) {
    return processContainerMap.get(action) != null;
  }

  @Override
  public List<TableProcessStore> getProcessStates() {
    return processContainerMap.values().stream()
        .flatMap(container -> container.getProcessStates().stream())
        .collect(Collectors.toList());
  }

  @Override
  public List<TableProcessStore> getProcessStates(Action action) {
    return processContainerMap.get(action).getProcessStates();
  }

  public TableOrphanFilesCleaningMetrics getOrphanFilesCleaningMetrics() {
    return orphanFilesCleaningMetrics;
  }

  public long getCurrentSnapshotId() {
    return store().getState(OPTIMIZING_STATE_KEY).getCurrentSnapshotId();
  }

  public long getCurrentChangeSnapshotId() {
    return store().getState(OPTIMIZING_STATE_KEY).getCurrentChangeSnapshotId();
  }

  public long getLastPlanTime() {
    return lastPlanTime;
  }

  public void setLastPlanTime(long lastPlanTime) {
    this.lastPlanTime = lastPlanTime;
  }

  public OptimizingStatus getOptimizingStatus() {
    return OptimizingStatus.ofCode(getStatusCode());
  }

  public long getLastMajorOptimizingTime() {
    return store().getState(OPTIMIZING_STATE_KEY).getLastMajorOptimizingTime();
  }

  public long getLastFullOptimizingTime() {
    return store().getState(OPTIMIZING_STATE_KEY).getLastFullOptimizingTime();
  }

  public long getLastMinorOptimizingTime() {
    return store().getState(OPTIMIZING_STATE_KEY).getLastMinorOptimizingTime();
  }

  /**
   * Returns the type of the last successfully completed optimization, or {@code null} if none has
   * ever run. Used by the cron-tick scheduler to determine which types are still "necessary".
   */
  public OptimizingType getLastOptimizingType() {
    String raw = store().getState(OPTIMIZING_STATE_KEY).getLastOptimizingType();
    if (raw == null || raw.isEmpty()) {
      return null;
    }
    try {
      return OptimizingType.valueOf(raw);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public long getLastOptimizedChangeSnapshotId() {
    return store().getState(OPTIMIZING_STATE_KEY).getLastOptimizedChangeSnapshotId();
  }

  public long getLastOptimizedSnapshotId() {
    return store().getState(OPTIMIZING_STATE_KEY).getLastOptimizedSnapshotId();
  }

  public OptimizingConfig getOptimizingConfig() {
    return getTableConfiguration().getOptimizingConfig();
  }

  public AbstractOptimizingEvaluator.PendingInput getPendingInput() {
    return store().getState(PENDING_INPUT_KEY);
  }

  public long getProcessId() {
    return store().getState(PROCESS_ID_KEY);
  }

  public OptimizingProcess getOptimizingProcess() {
    return optimizingProcess;
  }

  public void addTaskQuota(TaskRuntime.TaskQuota taskQuota) {
    doAsIgnoreError(OptimizingProcessMapper.class, mapper -> mapper.insertTaskQuota(taskQuota));
    taskQuotas.add(taskQuota);
    long validTime = System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME;
    this.taskQuotas.removeIf(task -> task.checkExpired(validTime));
  }

  /**
   * TODO: this is not final solution
   *
   * @param startTimeMills
   */
  public void resetTaskQuotas(long startTimeMills) {
    store()
        .synchronizedInvoke(
            () -> {
              long minProcessId = SnowflakeIdGenerator.getMinSnowflakeId(startTimeMills);
              taskQuotas.clear();
              taskQuotas.addAll(
                  getAs(
                      OptimizingProcessMapper.class,
                      mapper ->
                          mapper.selectTaskQuotasByTime(
                              getTableIdentifier().getId(), minProcessId)));
            });
  }

  public double calculateQuotaOccupy() {
    double targetQuota = getOptimizingConfig().getTargetQuota();
    int targetQuotaLimit =
        targetQuota > 1 ? (int) targetQuota : (int) Math.ceil(targetQuota * getThreadCount());
    return (double) getQuotaTime() / AmoroServiceConstants.QUOTA_LOOK_BACK_TIME / targetQuotaLimit;
  }

  public boolean isAllowPartialCommit() {
    return getOptimizingConfig().isAllowPartialCommit();
  }

  public void setPendingInput(AbstractOptimizingEvaluator.PendingInput pendingInput) {
    long pendingFileSize =
        pendingInput.getDataFileSize()
            + pendingInput.getEqualityDeleteBytes()
            + pendingInput.getPositionalDeleteBytes();
    int pendingFileCount =
        pendingInput.getDataFileCount()
            + pendingInput.getEqualityDeleteFileCount()
            + pendingInput.getPositionalDeleteFileCount();
    store()
        .begin()
        .updateState(PENDING_INPUT_KEY, i -> pendingInput)
        .updateStatusCode(
            code -> {
              if (code == OptimizingStatus.IDLE.getCode()) {
                LOG.info(
                    "{} status changed from idle to pending with pendingInput {}",
                    getTableIdentifier(),
                    pendingInput);
                return OptimizingStatus.PENDING.getCode();
              }
              return code;
            })
        .updateTableSummary(
            summary -> {
              summary.setTotalFileSize(pendingFileSize);
              summary.setTotalFileCount(pendingFileCount);
            })
        .commit();
  }

  public void setTableSummary(AbstractOptimizingEvaluator.PendingInput tableSummary) {
    store()
        .begin()
        .updateTableSummary(
            summary -> {
              summary.setHealthScore(tableSummary.getHealthScore());
              summary.setSmallFileScore(tableSummary.getSmallFileScore());
              summary.setEqualityDeleteScore(tableSummary.getEqualityDeleteScore());
              summary.setPositionalDeleteScore(tableSummary.getPositionalDeleteScore());
              summary.setTotalFileCount(tableSummary.getTotalFileCount());
              summary.setTotalFileSize(tableSummary.getTotalFileSize());
            })
        .commit();
    tableSummaryMetrics.refresh(tableSummary);
  }

  public DefaultTableRuntime refresh(AmoroTable<?> table) {
    Map<String, String> tableConfig = table.properties();
    TableConfiguration newConfiguration = TableConfigurations.parseTableConfig(tableConfig);
    String newGroupName = newConfiguration.getOptimizingConfig().getOptimizerGroup();

    if (!Objects.equals(getGroupName(), newGroupName)) {
      if (optimizingProcess != null) {
        optimizingProcess.close(false);
      }
      this.optimizingMetrics.optimizerGroupChanged(getGroupName());
    }

    store()
        .begin()
        .updateTableConfig(
            config -> {
              config.clear();
              config.putAll(tableConfig);
            })
        .updateGroup(g -> newGroupName)
        .updateState(
            OPTIMIZING_STATE_KEY,
            s -> {
              refreshSnapshots(table, s);
              return s;
            })
        .commit();
    return this;
  }

  public void beginPlanning() {
    OptimizingStatus originalStatus = getOptimizingStatus();
    store().begin().updateStatusCode(code -> OptimizingStatus.PLANNING.getCode()).commit();
  }

  public void planFailed() {
    OptimizingStatus originalStatus = getOptimizingStatus();
    store().begin().updateStatusCode(code -> OptimizingStatus.PENDING.getCode()).commit();
  }

  public void beginProcess(OptimizingProcess optimizingProcess) {
    OptimizingStatus originalStatus = getOptimizingStatus();
    this.optimizingProcess = optimizingProcess;

    store()
        .begin()
        .updateState(PROCESS_ID_KEY, any -> optimizingProcess.getProcessId())
        .updateStatusCode(
            code ->
                OptimizingStatus.ofOptimizingType(optimizingProcess.getOptimizingType()).getCode())
        .updateState(PENDING_INPUT_KEY, any -> new AbstractOptimizingEvaluator.PendingInput())
        .commit();
  }

  public long getLastCleanTime(CleanupOperation operation) {
    TableRuntimeCleanupState state = store().getState(CLEANUP_STATE_KEY);
    switch (operation) {
      case ORPHAN_FILES_CLEANING:
        return state.getLastOrphanFilesCleanTime();
      case DANGLING_DELETE_FILES_CLEANING:
        return state.getLastDanglingDeleteFilesCleanTime();
      case DATA_EXPIRING:
        return state.getLastDataExpiringTime();
      case SNAPSHOTS_EXPIRING:
        return state.getLastSnapshotsExpiringTime();
      default:
        return 0L;
    }
  }

  public void updateLastCleanTime(CleanupOperation operation, long time) {
    store()
        .begin()
        .updateState(
            CLEANUP_STATE_KEY,
            state -> {
              switch (operation) {
                case ORPHAN_FILES_CLEANING:
                  state.setLastOrphanFilesCleanTime(time);
                  break;
                case DANGLING_DELETE_FILES_CLEANING:
                  state.setLastDanglingDeleteFilesCleanTime(time);
                  break;
                case DATA_EXPIRING:
                  state.setLastDataExpiringTime(time);
                  break;
                case SNAPSHOTS_EXPIRING:
                  state.setLastSnapshotsExpiringTime(time);
                  break;
              }
              return state;
            })
        .commit();
  }

  public void completeProcess(boolean success) {
    OptimizingType processType = optimizingProcess.getOptimizingType();

    store()
        .begin()
        .updateState(
            OPTIMIZING_STATE_KEY,
            state -> {
              if (success) {
                // Only advance the snapshot checkpoints on success. Leaving them at their previous
                // values on failure ensures the next cron tick sees a snapshot change and
                // re-schedules the optimization instead of treating the table as already optimized.
                state.setLastOptimizedSnapshotId(optimizingProcess.getTargetSnapshotId());
                state.setLastOptimizedChangeSnapshotId(
                    optimizingProcess.getTargetChangeSnapshotId());
                state.setLastOptimizingType(processType.name());
              }

              if (processType == OptimizingType.MINOR) {
                state.setLastMinorOptimizingTime(optimizingProcess.getPlanTime());
              } else if (processType == OptimizingType.MAJOR) {
                state.setLastMajorOptimizingTime(optimizingProcess.getPlanTime());
              } else if (processType == OptimizingType.FULL) {
                state.setLastFullOptimizingTime(optimizingProcess.getPlanTime());
              }
              return state;
            })
        .updateStatusCode(code -> OptimizingStatus.IDLE.getCode())
        .commit();

    optimizingMetrics.processComplete(processType, success, optimizingProcess.getPlanTime());
    optimizingProcess = null;
    this.pendingCronType = null;
  }

  public void completeEmptyProcess() {
    OptimizingStatus originalStatus = getOptimizingStatus();
    boolean needUpdate =
        originalStatus == OptimizingStatus.PLANNING || originalStatus == OptimizingStatus.PENDING;
    if (needUpdate) {
      OptimizingType cronType = this.pendingCronType;
      if (cronType != null) {
        recordSkippedOptimization(
            cronType,
            String.format(
                "cron fired for %s but planner evaluated and found no data to process", cronType));
      }
      store()
          .begin()
          .updateStatusCode(code -> OptimizingStatus.IDLE.getCode())
          .updateState(
              OPTIMIZING_STATE_KEY,
              state -> {
                state.setLastOptimizedSnapshotId(state.getCurrentSnapshotId());
                state.setLastOptimizedChangeSnapshotId(state.getCurrentChangeSnapshotId());
                if (cronType != null) {
                  state.setLastOptimizingType(cronType.name());
                }
                return state;
              })
          .updateState(PENDING_INPUT_KEY, any -> new AbstractOptimizingEvaluator.PendingInput())
          .commit();
      this.pendingCronType = null;
    }
  }

  public void optimizingNotNecessary() {
    if (getOptimizingStatus() == OptimizingStatus.IDLE) {
      store()
          .begin()
          .updateState(
              OPTIMIZING_STATE_KEY,
              state -> {
                state.setLastOptimizedSnapshotId(state.getCurrentSnapshotId());
                state.setLastOptimizedChangeSnapshotId(state.getCurrentChangeSnapshotId());
                return state;
              })
          .commit();
    }
  }

  /**
   * Transitions this table from IDLE to PENDING without performing a file scan. Used by the
   * cron-tick scheduler after determining that an optimization type is eligible and necessary. The
   * actual file analysis is deferred to the planner inside {@code OptimizingQueue.planInternal}.
   *
   * @param cronType the optimization type whose cron triggered this transition — stored so that
   *     {@link #completeEmptyProcess()} can update the correct per-type timestamp when the planner
   *     determines no work is needed.
   */
  public void markAsPending(OptimizingType cronType) {
    this.pendingCronType = cronType;
    store()
        .begin()
        .updateStatusCode(
            code -> {
              if (code == OptimizingStatus.IDLE.getCode()) {
                LOG.info(
                    "{} status changed from idle to pending (cron-triggered, type={})",
                    getTableIdentifier(),
                    cronType);
                return OptimizingStatus.PENDING.getCode();
              }
              return code;
            })
        .commit();
  }

  /**
   * Writes a {@link ProcessStatus#SKIPPED} record to the {@code table_process} table so that the UI
   * can show why a cron-triggered optimization did not run. Both the insert and the subsequent
   * update (which sets {@code finish_time} and {@code fail_message}) run in a single transaction so
   * a partial write can never occur.
   *
   * @param type the optimization type whose cron fired
   * @param reason human-readable skip reason
   */
  public void recordSkippedOptimization(OptimizingType type, String reason) {
    long now = System.currentTimeMillis();
    long processId = ID_GENERATOR.generateId();
    Map<String, String> summary = new java.util.HashMap<>();
    summary.put("skipReason", reason);
    summary.put("optimizingType", type.name());
    doAs(
        TableProcessMapper.class,
        mapper -> {
          mapper.insertProcess(
              getTableIdentifier().getId(),
              processId,
              "",
              ProcessStatus.SKIPPED,
              type.name().toUpperCase(),
              ProcessStatus.SKIPPED.name().toLowerCase(),
              "AMORO",
              0,
              now,
              new java.util.HashMap<>(),
              summary);
          mapper.updateProcess(
              getTableIdentifier().getId(),
              processId,
              "",
              ProcessStatus.SKIPPED,
              ProcessStatus.SKIPPED.name().toLowerCase(),
              0,
              now,
              reason,
              new java.util.HashMap<>(),
              summary);
        });
    LOG.info(
        "[cron-skip] table={} type={} skip record persisted, reason: {}",
        getTableIdentifier(),
        type,
        reason);
  }

  public void beginCommitting() {
    OptimizingStatus originalStatus = getOptimizingStatus();
    store().begin().updateStatusCode(code -> OptimizingStatus.COMMITTING.getCode()).commit();
  }

  @Override
  public void unregisterMetric() {
    tableSummaryMetrics.unregister();
    orphanFilesCleaningMetrics.unregister();
    optimizingMetrics.unregister();
  }

  @Override
  public void dispose() {
    unregisterMetric();
    store()
        .synchronizedInvoke(
            () -> {
              Optional.ofNullable(optimizingProcess).ifPresent(process -> process.close(false));
            });
    super.dispose();
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
                    getTableIdentifier().getCatalog(),
                    getTableIdentifier().getDatabase(),
                    getTableIdentifier().getTableName(),
                    System.currentTimeMillis()));
    return TableBlocker.conflict(operation, tableBlockers);
  }

  private long getQuotaTime() {
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

  private int getThreadCount() {
    List<OptimizerInstance> instances = getAs(OptimizerMapper.class, OptimizerMapper::selectAll);
    if (instances == null || instances.isEmpty()) {
      return 1;
    }
    String groupName = getGroupName();
    return Math.max(
        instances.stream()
            .filter(instance -> Objects.equals(groupName, instance.getGroupName()))
            .mapToInt(OptimizerInstance::getThreadCount)
            .sum(),
        1);
  }

  private boolean refreshSnapshots(AmoroTable<?> amoroTable, TableRuntimeOptimizingState state) {
    OptimizingConfig optimizingConfig = this.getOptimizingConfig();
    if (!optimizingConfig.isEnabled()) {
      return true;
    }

    MixedTable table = (MixedTable) amoroTable.originalTable();
    tableSummaryMetrics.refreshSnapshots(table);
    long lastSnapshotId = state.getCurrentSnapshotId();
    if (table.isKeyedTable()) {
      long changeSnapshotId = state.getCurrentChangeSnapshotId();
      ChangeTable changeTable = table.asKeyedTable().changeTable();
      BaseTable baseTable = table.asKeyedTable().baseTable();

      long currentChangeSnapshotId = doRefreshSnapshots(changeTable);
      long currentSnapshotId = doRefreshSnapshots(baseTable);

      if (currentSnapshotId != lastSnapshotId || currentChangeSnapshotId != changeSnapshotId) {
        LOG.debug(
            "Refreshing table {} with base snapshot id {} and change snapshot id {}",
            getTableIdentifier(),
            currentSnapshotId,
            currentChangeSnapshotId);
        state.setCurrentChangeSnapshotId(currentChangeSnapshotId);
        state.setCurrentSnapshotId(currentSnapshotId);
        return true;
      }
    } else {
      long currentSnapshotId = doRefreshSnapshots((UnkeyedTable) table);
      if (currentSnapshotId != lastSnapshotId) {
        LOG.debug(
            "Refreshing table {} with base snapshot id {}",
            getTableIdentifier(),
            currentSnapshotId);
        state.setCurrentSnapshotId(currentSnapshotId);
        return true;
      }
    }
    return false;
  }

  private long doRefreshSnapshots(UnkeyedTable table) {
    long currentSnapshotId = Constants.INVALID_SNAPSHOT_ID;
    Snapshot currentSnapshot = IcebergTableUtil.getSnapshot(table, false);
    if (currentSnapshot != null) {
      currentSnapshotId = currentSnapshot.snapshotId();
    }

    optimizingMetrics.nonMaintainedSnapshotTime(currentSnapshot);
    optimizingMetrics.lastOptimizingSnapshotTime(
        IcebergTableUtil.findLatestOptimizingSnapshot(table).orElse(null));

    return currentSnapshotId;
  }

  private class TableProcessContainer {
    private final Lock processLock = new ReentrantLock();
    private final ProcessFactory processFactory;
    private final Map<Long, AmoroProcess> processMap = Maps.newConcurrentMap();

    TableProcessContainer(ProcessFactory processFactory) {
      this.processFactory = processFactory;
    }

    public AmoroProcess trigger(Action action) {
      processLock.lock();
      try {
        AmoroProcess process = processFactory.create(DefaultTableRuntime.this, action);
        process.getCompleteFuture().whenCompleted(() -> processMap.remove(process.getId()));
        processMap.put(process.getId(), process);
        return process;
      } finally {
        processLock.unlock();
      }
    }

    public List<TableProcessStore> getProcessStates() {
      return processMap.values().stream().map(AmoroProcess::store).collect(Collectors.toList());
    }
  }
}

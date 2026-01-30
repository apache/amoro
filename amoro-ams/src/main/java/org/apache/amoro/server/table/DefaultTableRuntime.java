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
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
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
  private volatile long latestRefreshInterval = AmoroServiceConstants.INVALID_TIME;
  private volatile boolean latestEvaluatedNeedOptimizing = true;
  private volatile OptimizingProcess optimizingProcess;
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

  public long getLatestRefreshInterval() {
    return latestRefreshInterval;
  }

  public void setLatestRefreshInterval(long latestRefreshInterval) {
    this.latestRefreshInterval = latestRefreshInterval;
  }

  public boolean getLatestEvaluatedNeedOptimizing() {
    return this.latestEvaluatedNeedOptimizing;
  }

  public void setLatestEvaluatedNeedOptimizing(boolean latestEvaluatedNeedOptimizing) {
    this.latestEvaluatedNeedOptimizing = latestEvaluatedNeedOptimizing;
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
    TableConfiguration oldConfiguration = getTableConfiguration();
    boolean configChanged = !newConfiguration.equals(oldConfiguration);

    if (!Objects.equals(
        getGroupName(), newConfiguration.getOptimizingConfig().getOptimizerGroup())) {
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
    OptimizingStatus originalStatus = getOptimizingStatus();
    OptimizingType processType = optimizingProcess.getOptimizingType();

    store()
        .begin()
        .updateState(
            OPTIMIZING_STATE_KEY,
            state -> {
              state.setLastOptimizedSnapshotId(optimizingProcess.getTargetSnapshotId());
              state.setLastOptimizedChangeSnapshotId(optimizingProcess.getTargetChangeSnapshotId());
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
  }

  public void completeEmptyProcess() {
    OptimizingStatus originalStatus = getOptimizingStatus();
    boolean needUpdate =
        originalStatus == OptimizingStatus.PLANNING || originalStatus == OptimizingStatus.PENDING;
    if (needUpdate) {
      store()
          .begin()
          .updateStatusCode(code -> OptimizingStatus.IDLE.getCode())
          .updateState(
              OPTIMIZING_STATE_KEY,
              state -> {
                state.setLastOptimizedSnapshotId(state.getCurrentSnapshotId());
                state.setLastOptimizedChangeSnapshotId(state.getCurrentChangeSnapshotId());
                return state;
              })
          .updateState(PENDING_INPUT_KEY, any -> new AbstractOptimizingEvaluator.PendingInput())
          .commit();
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

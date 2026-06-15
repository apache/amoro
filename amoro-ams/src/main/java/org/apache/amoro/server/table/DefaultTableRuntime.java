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
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.optimizing.OptimizationContext;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.PendingInputResult;
import org.apache.amoro.optimizing.TableRuntimeOptimizingState;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.TableRuntimeState;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.blocker.TableBlocker;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.amoro.server.table.cleanup.TableRuntimeCleanupState;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.FormatPendingInput;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeStore;
import org.apache.amoro.utils.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/** Default table runtime implementation. */
public class DefaultTableRuntime extends AbstractTableRuntime implements OptimizationContext {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTableRuntime.class);

  protected static final StateKey<TableRuntimeOptimizingState> OPTIMIZING_STATE_KEY =
      StateKey.stateKey("optimizing_state")
          .jsonType(TableRuntimeOptimizingState.class)
          .defaultValue(new TableRuntimeOptimizingState());

  /** Default pending-input key for Iceberg-based formats. */
  public static final StateKey<AbstractOptimizingEvaluator.PendingInput> DEFAULT_PENDING_INPUT_KEY =
      StateKey.stateKey("pending_input")
          .jsonType(AbstractOptimizingEvaluator.PendingInput.class)
          .defaultValue(new AbstractOptimizingEvaluator.PendingInput());

  @SuppressWarnings("unchecked")
  private final StateKey<FormatPendingInput> pendingInputKey;

  public static final StateKey<TableRuntimeCleanupState> CLEANUP_STATE_KEY =
      StateKey.stateKey("cleanup_state")
          .jsonType(TableRuntimeCleanupState.class)
          .defaultValue(new TableRuntimeCleanupState());

  protected static final StateKey<Long> PROCESS_ID_KEY =
      StateKey.stateKey("process_id").longType().defaultValue(0L);

  public static final List<StateKey<?>> REQUIRED_STATES =
      Lists.newArrayList(OPTIMIZING_STATE_KEY, PROCESS_ID_KEY, CLEANUP_STATE_KEY);
  private final TableOptimizingMetrics optimizingMetrics;
  private final TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics;
  protected final TableSummaryMetrics tableSummaryMetrics;
  private volatile long lastPlanTime;
  private volatile long latestRefreshInterval = AmoroServiceConstants.INVALID_TIME;
  private volatile boolean latestEvaluatedNeedOptimizing = true;
  protected volatile OptimizingProcess optimizingProcess;
  private final List<TaskRuntime.TaskQuota> taskQuotas = new CopyOnWriteArrayList<>();

  private final Supplier<AmoroTable<?>> loader;

  public DefaultTableRuntime(
      TableRuntimeStore store,
      Supplier<AmoroTable<?>> loader,
      StateKey<? extends FormatPendingInput> pendingInputKey) {
    super(store);
    this.pendingInputKey = (StateKey<FormatPendingInput>) pendingInputKey;
    this.optimizingMetrics =
        new TableOptimizingMetrics(store.getTableIdentifier(), store.getGroupName());
    this.orphanFilesCleaningMetrics =
        new TableOrphanFilesCleaningMetrics(store.getTableIdentifier());
    this.tableSummaryMetrics = new TableSummaryMetrics(store.getTableIdentifier());
    this.loader = loader;
  }

  /** Convenience constructor using the default Iceberg pending-input key. */
  public DefaultTableRuntime(TableRuntimeStore store, Supplier<AmoroTable<?>> loader) {
    this(store, loader, DEFAULT_PENDING_INPUT_KEY);
  }

  public void recover(OptimizingProcess optimizingProcess) {
    if (!Objects.equals(optimizingProcess.getProcessId(), getProcessId())) {
      throw new IllegalStateException("Table runtime and processing are not matched!");
    }
    this.optimizingProcess = optimizingProcess;
    if (this.optimizingProcess.getStatus() == ProcessStatus.SUCCESS) {
      completeProcess(optimizingProcess, true);
    }
  }

  @Override
  public void registerMetric(MetricRegistry metricRegistry) {
    this.optimizingMetrics.register(metricRegistry);
    this.orphanFilesCleaningMetrics.register(metricRegistry);
    this.tableSummaryMetrics.register(metricRegistry);
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

  public FormatPendingInput getPendingInput() {
    return store().getState(pendingInputKey);
  }

  // ---- OptimizationContext implementation ----

  @Override
  public boolean isIdle() {
    return getOptimizingStatus() == OptimizingStatus.IDLE;
  }

  @Override
  public void updateNonMaintainedSnapshotTime(long timestampMillis) {
    optimizingMetrics.nonMaintainedSnapshotTime(timestampMillis);
  }

  @Override
  public void updateLastOptimizingSnapshotTime(long timestampMillis) {
    optimizingMetrics.lastOptimizingSnapshotTime(timestampMillis);
  }

  /**
   * Evaluate pending input and transition state if necessary.
   *
   * <p>Called by {@code TableRuntimeRefreshExecutor} when a snapshot change is detected. Uses
   * {@link AmoroTable#evaluatePendingInput} for format-specific evaluation. Each format's
   * AmoroTable implementation provides its own evaluation logic.
   *
   * @param table the current AmoroTable for evaluation
   * @param maxPendingPartitions max partitions to scan when evaluating pending input
   * @return true if optimizing demand exists, false otherwise
   */
  public boolean evaluatePendingInputAndTransition(AmoroTable<?> table, int maxPendingPartitions) {
    OptimizingConfig config = getOptimizingConfig();

    if (!config.isEnabled()) {
      if (config.isTableSummaryEnabled()) {
        table
            .evaluatePendingInput(this, maxPendingPartitions)
            .map(PendingInputResult::pendingInput)
            .ifPresent(this::setTableSummary);
      }
      clearPendingSummary();
      return false;
    }

    if (!isIdle()) {
      return true;
    }

    Optional<PendingInputResult> result = table.evaluatePendingInput(this, maxPendingPartitions);
    if (!result.isPresent()) {
      optimizingNotNecessary();
      return false;
    }

    PendingInputResult evalResult = result.get();
    if (evalResult.optimizingNecessary()) {
      // Keep master semantics: set pending info first, then refresh summary from full input.
      setPendingInput(evalResult.optimizingPendingInput());
      setTableSummary(evalResult.pendingInput());
      return true;
    } else {
      setTableSummary(evalResult.pendingInput());
      optimizingNotNecessary();
      return false;
    }
  }

  public long getProcessId() {
    TableRuntimeState state =
        getAs(
            TableRuntimeMapper.class,
            mapper ->
                mapper.getState(
                    getTableIdentifier().getId(), DefaultTableRuntime.PROCESS_ID_KEY.getKey()));
    if (state == null || state.getStateValue() == null) {
      return 0L;
    }
    return Long.parseLong(state.getStateValue());
  }

  public boolean tryAcquireProcessOwner(long processId) {
    return compareAndSetProcessOwner(0L, processId);
  }

  public boolean tryReleaseProcessOwner(long processId) {
    return compareAndSetProcessOwner(processId, 0L);
  }

  public boolean normalizeProcessOwner(long processId) {
    return compareAndSetProcessOwner(processId, 0L);
  }

  private boolean compareAndSetProcessOwner(long expected, long next) {
    TableRuntimeState currentState =
        getAs(
            TableRuntimeMapper.class,
            mapper -> mapper.getState(getTableIdentifier().getId(), PROCESS_ID_KEY.getKey()));
    if (currentState == null || currentState.getStateValue() == null) {
      return expected == 0L && next == 0L;
    }
    if (!String.valueOf(expected).equals(currentState.getStateValue())) {
      return false;
    }
    long updated =
        updateAs(
            TableRuntimeMapper.class,
            mapper ->
                mapper.setStateValueIfVersion(
                    getTableIdentifier().getId(),
                    PROCESS_ID_KEY.getKey(),
                    currentState.getStateVersion(),
                    String.valueOf(next)));
    return updated == 1L;
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

  public void setPendingInput(FormatPendingInput pendingInput) {
    store()
        .begin()
        .updateState(pendingInputKey, i -> pendingInput)
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
              summary.setTotalFileSize(pendingInput.getTotalFileSize());
              summary.setTotalFileCount(pendingInput.getTotalFileCount());
              summary.setPendingFileSize(pendingInput.getTotalFileSize());
              summary.setPendingFileCount(pendingInput.getTotalFileCount());
            })
        .commit();
  }

  public void setTableSummary(FormatPendingInput tableSummary) {
    store()
        .begin()
        .updateTableSummary(
            summary -> {
              summary.setHealthScore(tableSummary.getHealthScore());
              summary.setTotalFileCount(tableSummary.getTotalFileCount());
              summary.setTotalFileSize(tableSummary.getTotalFileSize());
              if (tableSummary instanceof AbstractOptimizingEvaluator.PendingInput) {
                AbstractOptimizingEvaluator.PendingInput iceInput =
                    (AbstractOptimizingEvaluator.PendingInput) tableSummary;
                summary.setSmallFileScore(iceInput.getSmallFileScore());
                summary.setEqualityDeleteScore(iceInput.getEqualityDeleteScore());
                summary.setPositionalDeleteScore(iceInput.getPositionalDeleteScore());
              }
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
    Objects.requireNonNull(optimizingProcess, "optimizingProcess is null when beginning process");
    if (!tryAcquireProcessOwner(optimizingProcess.getProcessId())) {
      throw new OptimizingOwnerConflictException(
          "acquire", getTableIdentifier(), optimizingProcess.getProcessId(), getProcessId());
    }
    this.optimizingProcess = optimizingProcess;

    store()
        .begin()
        .updateStatusCode(
            code ->
                OptimizingStatus.ofOptimizingType(optimizingProcess.getOptimizingType()).getCode())
        .updateState(pendingInputKey, any -> pendingInputKey.getDefaultValue())
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
    completeProcess(
        Objects.requireNonNull(
            optimizingProcess, "optimizingProcess is null when completing table process"),
        success);
  }

  public void completeProcess(OptimizingProcess process, boolean success) {
    Objects.requireNonNull(process, "process is null when completing table process");
    if (!tryReleaseProcessOwner(process.getProcessId())) {
      long currentOwner = getProcessId();
      if (currentOwner != process.getProcessId()) {
        LOG.warn(
            "Skip completing process {} for table {} because current owner is {}",
            process.getProcessId(),
            getTableIdentifier(),
            currentOwner);
        return;
      }
      throw new OptimizingOwnerConflictException(
          "release", getTableIdentifier(), process.getProcessId(), currentOwner);
    }
    OptimizingType processType = process.getOptimizingType();
    long planTime = process.getPlanTime();

    store()
        .begin()
        .updateState(
            OPTIMIZING_STATE_KEY,
            state -> {
              if (success) {
                state.setLastOptimizedSnapshotId(process.getTargetSnapshotId());
                state.setLastOptimizedChangeSnapshotId(process.getTargetChangeSnapshotId());
              }
              if (processType == OptimizingType.MINOR) {
                state.setLastMinorOptimizingTime(planTime);
              } else if (processType == OptimizingType.MAJOR) {
                state.setLastMajorOptimizingTime(planTime);
              } else if (processType == OptimizingType.FULL) {
                state.setLastFullOptimizingTime(planTime);
              }
              return state;
            })
        .updateTableSummary(
            summary -> {
              summary.setPendingFileSize(0L);
              summary.setPendingFileCount(0);
            })
        .updateStatusCode(code -> OptimizingStatus.IDLE.getCode())
        .commit();

    optimizingMetrics.processComplete(processType, success, planTime);
    if (optimizingProcess == null || optimizingProcess.getProcessId() == process.getProcessId()) {
      optimizingProcess = null;
    }
  }

  /**
   * Resets the table to IDLE from any non-IDLE state. This is used both when planning determines
   * that optimization is unnecessary (from PLANNING state) and during startup recovery to reset
   * tables with unrecoverable processes (from any processing state).
   */
  public void completeEmptyProcess() {
    OptimizingStatus originalStatus = getOptimizingStatus();
    if (originalStatus == OptimizingStatus.IDLE) {
      return;
    }
    long processId = getProcessId();
    if (processId != 0L && !normalizeProcessOwner(processId)) {
      throw new IllegalStateException(
          String.format(
              "failed to normalize optimizing owner for table %s, expected owner %d, current owner %d",
              getTableIdentifier(), processId, getProcessId()));
    }
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
        .updateTableSummary(
            summary -> {
              summary.setPendingFileSize(0L);
              summary.setPendingFileCount(0);
            })
        .updateState(pendingInputKey, any -> pendingInputKey.getDefaultValue())
        .commit();
    optimizingProcess = null;
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
          .updateTableSummary(
              summary -> {
                summary.setPendingFileSize(0L);
                summary.setPendingFileCount(0);
              })
          .commit();
    }
  }

  private void clearPendingSummary() {
    store()
        .begin()
        .updateTableSummary(
            summary -> {
              summary.setPendingFileSize(0L);
              summary.setPendingFileCount(0);
            })
        .commit();
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

  @Override
  public AmoroTable<?> loadTable() {
    return loader.get();
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
    tableSummaryMetrics.refreshSnapshots(amoroTable);
    amoroTable.refreshOptimizingMetrics(this);
    return amoroTable.refreshOptimizingState(state);
  }
}

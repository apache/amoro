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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.exception.OptimizingClosedException;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.exception.TaskNotFoundException;
import org.apache.amoro.optimizing.BaseOptimizingInput;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.amoro.optimizing.TableOptimizingPlanner;
import org.apache.amoro.optimizing.TaskMetricsSummary;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.TaskRuntime.Status;
import org.apache.amoro.server.persistence.OptimizingProcessState;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TaskFilesPersistence;
import org.apache.amoro.server.persistence.converter.TaskDescriptorRecoveryTypes;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.ProcessFactoryRouter;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.OptimizingOwnerConflictException;
import org.apache.amoro.server.table.blocker.TableBlocker;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OptimizingQueue extends PersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingQueue.class);
  private static final long STALE_OWNER_RECYCLE_MILLIS = TimeUnit.MINUTES.toMillis(15);
  private static final Set<ProcessStatus> ACTIVE_PROCESS_STATUSES =
      EnumSet.of(
          ProcessStatus.PENDING,
          ProcessStatus.SUBMITTED,
          ProcessStatus.RUNNING,
          ProcessStatus.CANCELING);

  private final QuotaProvider quotaProvider;
  private final Queue<TableOptimizingProcess> tableQueue = new LinkedTransferQueue<>();
  private final SchedulingPolicy scheduler;
  private final CatalogManager catalogManager;
  private final Executor planExecutor;
  // Keep all planning table identifiers
  private final Set<ServerTableIdentifier> planningTables = ConcurrentHashMap.newKeySet();
  private final Lock scheduleLock = new ReentrantLock();
  private final Condition planningCompleted = scheduleLock.newCondition();
  private final int maxPlanningParallelism;
  private final Semaphore planningSlots;
  private final OptimizerGroupMetrics metrics;
  private ResourceGroup optimizerGroup;
  private final ProcessFactoryRouter router;
  private final Map<ServerTableIdentifier, AtomicInteger> optimizingTasksMap =
      new ConcurrentHashMap<>();
  private final Set<ServerTableIdentifier> warmingTables = ConcurrentHashMap.newKeySet();
  private final Set<ServerTableIdentifier> warmedTables = ConcurrentHashMap.newKeySet();

  public enum WarmupResult {
    WARMED,
    SKIPPED,
    DUPLICATE,
    FAILED
  }

  public OptimizingQueue(
      CatalogManager catalogManager,
      ResourceGroup optimizerGroup,
      QuotaProvider quotaProvider,
      Executor planExecutor,
      List<DefaultTableRuntime> tableRuntimeList,
      int maxPlanningParallelism,
      ProcessFactoryRouter router) {
    Preconditions.checkNotNull(optimizerGroup, "Optimizer group can not be null");
    this.planExecutor = planExecutor;
    this.optimizerGroup = optimizerGroup;
    this.quotaProvider = quotaProvider;
    this.scheduler = new SchedulingPolicy(optimizerGroup);
    this.catalogManager = catalogManager;
    Preconditions.checkArgument(
        maxPlanningParallelism >= 0, "Max planning parallelism can not be negative");
    this.maxPlanningParallelism = maxPlanningParallelism;
    this.planningSlots = new Semaphore(this.maxPlanningParallelism);
    this.router = router;
    this.metrics =
        new OptimizerGroupMetrics(
            optimizerGroup.getName(), MetricManager.getInstance().getGlobalRegistry(), this);
    this.metrics.register();
  }

  public WarmupResult warmupTable(DefaultTableRuntime tableRuntime) {
    ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
    if (warmedTables.contains(identifier)) {
      return WarmupResult.DUPLICATE;
    }
    if (!warmingTables.add(identifier)) {
      return WarmupResult.DUPLICATE;
    }
    try {
      WarmupResult result = doWarmupTable(tableRuntime);
      if (result == WarmupResult.WARMED || result == WarmupResult.SKIPPED) {
        warmedTables.add(identifier);
      }
      return result;
    } catch (Exception e) {
      LOG.error("Failed to warm up table runtime for table {}, skipping", identifier, e);
      return WarmupResult.FAILED;
    } finally {
      warmingTables.remove(identifier);
    }
  }

  private WarmupResult doWarmupTable(DefaultTableRuntime tableRuntime) {
    TableOptimizingProcess process = loadProcess(tableRuntime);

    if (!tableRuntime.getOptimizingConfig().isEnabled()) {
      closeProcessIfRunning(process);
      return WarmupResult.SKIPPED;
    }

    if (!isFormatSupported(tableRuntime)) {
      closeProcessIfRunning(process);
      return WarmupResult.SKIPPED;
    }

    tableRuntime.resetTaskQuotas(
        System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME);

    if (canReplayPaimonCommittingProcess(process, tableRuntime)) {
      LOG.info(
          "Paimon process {} on table {} is already COMMITTING with completed tasks during"
              + " recovery, keeping it for commit replay",
          process.getProcessId(),
          tableRuntime.getTableIdentifier());
    } else if (canResumeProcess(process, tableRuntime)) {
      if (process.allTasksPrepared()) {
        LOG.info(
            "All tasks already completed for process {} on table {} during recovery,"
                + " triggering commit",
            process.getProcessId(),
            tableRuntime.getTableIdentifier());
        tableRuntime.beginCommitting();
      } else {
        tableQueue.offer(process);
      }
    } else {
      resetTableForRecovery(process, tableRuntime);
      scheduler.addTable(tableRuntime);
    }
    return WarmupResult.WARMED;
  }

  private TableOptimizingProcess loadProcess(DefaultTableRuntime tableRuntime) {
    if (tableRuntime.getProcessId() == 0) {
      return null;
    }
    TableProcessMeta meta =
        getAs(
            TableProcessMapper.class, mapper -> mapper.getProcessMeta(tableRuntime.getProcessId()));
    if (meta == null) {
      return null;
    }
    OptimizingProcessState state =
        getAs(
            OptimizingProcessMapper.class,
            mapper -> mapper.getProcessState(tableRuntime.getProcessId()));
    if (state == null) {
      LOG.warn(
          "No optimizing process state found for process {} on table {}, skipping process recovery",
          tableRuntime.getProcessId(),
          tableRuntime.getTableIdentifier());
      return null;
    }
    return new TableOptimizingProcess(tableRuntime, meta, state);
  }

  private boolean canResumeProcess(
      TableOptimizingProcess process, DefaultTableRuntime tableRuntime) {
    return process != null
        && process.getStatus() == ProcessStatus.RUNNING
        && tableRuntime.getOptimizingStatus().isProcessing()
        && tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING;
  }

  private boolean canReplayPaimonCommittingProcess(
      TableOptimizingProcess process, DefaultTableRuntime tableRuntime) {
    return tableRuntime.getFormat() == TableFormat.PAIMON
        && process != null
        && process.getStatus() == ProcessStatus.RUNNING
        && tableRuntime.getOptimizingStatus() == OptimizingStatus.COMMITTING
        && process.allTasksPrepared();
  }

  private void resetTableForRecovery(
      TableOptimizingProcess process, DefaultTableRuntime tableRuntime) {
    closeProcessIfRunning(process);
    if (tableRuntime.getOptimizingStatus() != OptimizingStatus.IDLE
        && tableRuntime.getOptimizingStatus() != OptimizingStatus.PENDING) {
      LOG.warn(
          "Resetting table {} from {} to IDLE during recovery (process: {})",
          tableRuntime.getTableIdentifier(),
          tableRuntime.getOptimizingStatus(),
          process != null ? process.getStatus() : "null");
      tableRuntime.completeEmptyProcess();
    }
  }

  private void closeProcessIfRunning(TableOptimizingProcess process) {
    if (process != null && process.getStatus() == ProcessStatus.RUNNING) {
      process.close(false);
    }
  }

  public String getContainerName() {
    return optimizerGroup.getContainer();
  }

  public void refreshTable(DefaultTableRuntime tableRuntime) {
    if (!isFormatSupported(tableRuntime)) {
      return;
    }
    if (tableRuntime.getOptimizingConfig().isEnabled()
        && !tableRuntime.getOptimizingStatus().isProcessing()) {
      LOG.debug(
          "Bind queue {} success with table {}",
          optimizerGroup.getName(),
          tableRuntime.getTableIdentifier());
      tableRuntime.resetTaskQuotas(
          System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME);
      scheduler.addTable(tableRuntime);
    }
  }

  private boolean isFormatSupported(DefaultTableRuntime tableRuntime) {
    TableFormat format = tableRuntime.getFormat();
    if (!router.supportedFormats().contains(format)) {
      LOG.debug(
          "Skip table {} with unsupported format {} for queue {}",
          tableRuntime.getTableIdentifier(),
          format,
          optimizerGroup.getName());
      return false;
    }
    return true;
  }

  public void releaseTable(DefaultTableRuntime tableRuntime) {
    clearWarmupState(tableRuntime);
    scheduler.removeTable(tableRuntime);
    List<OptimizingProcess> processList =
        tableQueue.stream()
            .filter(process -> process.getTableId() == tableRuntime.getTableIdentifier().getId())
            .collect(Collectors.toList());
    for (OptimizingProcess process : processList) {
      process.close(false);
      clearProcess(process);
    }
    LOG.info(
        "Release queue {} with table {}",
        optimizerGroup.getName(),
        tableRuntime.getTableIdentifier());
  }

  private void clearProcess(OptimizingProcess optimizingProcess) {
    tableQueue.removeIf(process -> process.getProcessId() == optimizingProcess.getProcessId());
  }

  public TaskRuntime<?> pollTask(
      OptimizerThread thread, long maxWaitTime, boolean breakQuotaLimit) {
    resetStaleTasksForThread(thread);
    long deadline = calculateDeadline(maxWaitTime);
    TaskRuntime<?> task = fetchScheduledTask(thread, true);
    while (task == null) {
      fillPlanningSlots();
      task = fetchScheduledTask(thread, true);
      if (task != null || !awaitPlanningCompleted(deadline)) {
        break;
      }
      task = fetchScheduledTask(thread, true);
    }
    if (task == null && breakQuotaLimit && planningTables.isEmpty()) {
      task = fetchScheduledTask(thread, false);
    }
    return task;
  }

  public TaskRuntime<?> pollTask(OptimizerThread thread, long maxWaitTime) {
    return pollTask(thread, maxWaitTime, true);
  }

  private long calculateDeadline(long maxWaitTime) {
    long deadline = System.currentTimeMillis() + maxWaitTime;
    return deadline <= 0 ? Long.MAX_VALUE : deadline;
  }

  private boolean awaitPlanningCompleted(long waitDeadline) {
    scheduleLock.lock();
    try {
      long currentTime = System.currentTimeMillis();
      return waitDeadline > currentTime
          && planningCompleted.await(waitDeadline - currentTime, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      LOG.error("Schedule table interrupted", e);
      Thread.currentThread().interrupt();
      return false;
    } finally {
      scheduleLock.unlock();
    }
  }

  private TaskRuntime<?> fetchScheduledTask(OptimizerThread thread, boolean needQuotaChecking) {
    return tableQueue.stream()
        .map(process -> process.poll(thread, needQuotaChecking))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private void fillPlanningSlots() {
    int availableSlots = planningSlots.availablePermits();
    if (availableSlots <= 0) {
      return;
    }

    Set<ServerTableIdentifier> skipTables = new HashSet<>(planningTables);
    skipBlockedTables(skipTables);

    List<DefaultTableRuntime> candidates = scheduler.scheduleTables(skipTables, availableSlots);
    for (DefaultTableRuntime tableRuntime : candidates) {
      ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
      if (!planningSlots.tryAcquire()) {
        return;
      }
      if (!planningTables.add(identifier)) {
        planningSlots.release();
        continue;
      }
      skipTables.add(identifier);
      if (!triggerAsyncPlanning(tableRuntime, new HashSet<>(skipTables))) {
        planningTables.remove(identifier);
        planningSlots.release();
        signalPlanningCompleted();
      }
    }
  }

  private void skipBlockedTables(Set<ServerTableIdentifier> skipTables) {
    List<TableBlocker> tableBlockerList =
        getAs(
            TableBlockerMapper.class,
            mapper -> mapper.selectAllBlockers(System.currentTimeMillis()));
    Map<TableIdentifier, ServerTableIdentifier> identifierMap = Maps.newHashMap();
    for (ServerTableIdentifier identifier : scheduler.tableIdentifiersSnapshot()) {
      identifierMap.put(identifier.getIdentifier(), identifier);
    }
    tableBlockerList.stream()
        .filter(blocker -> TableBlocker.conflict(BlockableOperation.OPTIMIZE, blocker))
        .map(
            blocker ->
                TableIdentifier.of(
                    blocker.getCatalog(), blocker.getDatabase(), blocker.getTableName()))
        .map(identifierMap::get)
        .filter(Objects::nonNull)
        .forEach(skipTables::add);
  }

  private boolean triggerAsyncPlanning(
      DefaultTableRuntime tableRuntime, Set<ServerTableIdentifier> skipTables) {
    long startTime = System.currentTimeMillis();
    LOG.info(
        "Trigger planning table {} by policy {}",
        tableRuntime.getTableIdentifier(),
        scheduler.name());
    try {
      CompletableFuture.supplyAsync(() -> planInternal(tableRuntime), planExecutor)
          .whenComplete(
              (process, throwable) ->
                  completePlanning(tableRuntime, process, throwable, skipTables, startTime));
      return true;
    } catch (RuntimeException e) {
      LOG.error("Failed to submit planning table {}", tableRuntime.getTableIdentifier(), e);
      return false;
    }
  }

  private void completePlanning(
      DefaultTableRuntime tableRuntime,
      TableOptimizingProcess process,
      Throwable throwable,
      Set<ServerTableIdentifier> skipTables,
      long startTime) {
    long currentTime = System.currentTimeMillis();
    try {
      if (throwable != null) {
        LOG.error("Failed to plan table {}", tableRuntime.getTableIdentifier(), throwable);
      }
      if (process != null) {
        tableQueue.offer(process);
        String skipIds =
            skipTables.stream()
                .map(ServerTableIdentifier::getId)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        LOG.info(
            "Completed planning on table {} with {} tasks with a total cost of {} ms, skipping {} tables.",
            tableRuntime.getTableIdentifier(),
            process.getTaskMap().size(),
            currentTime - startTime,
            skipTables.size());
        LOG.debug("Skipped planning table IDs:{}", skipIds);
      } else if (throwable == null) {
        LOG.info(
            "Skipping planning table {} with a total cost of {} ms.",
            tableRuntime.getTableIdentifier(),
            currentTime - startTime);
      }
      tableRuntime.setLastPlanTime(currentTime);
    } finally {
      planningTables.remove(tableRuntime.getTableIdentifier());
      planningSlots.release();
      signalPlanningCompleted();
    }
  }

  private void signalPlanningCompleted() {
    scheduleLock.lock();
    try {
      planningCompleted.signalAll();
    } finally {
      scheduleLock.unlock();
    }
  }

  private boolean prepareOwnerForPlanning(DefaultTableRuntime tableRuntime) {
    for (int attempts = 0; attempts < 3; attempts++) {
      long ownerProcessId = tableRuntime.getProcessId();
      if (ownerProcessId == 0L) {
        return true;
      }

      TableProcessMeta processMeta =
          getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(ownerProcessId));
      if (processMeta == null || !isActiveProcess(processMeta.getStatus())) {
        if (tableRuntime.normalizeProcessOwner(ownerProcessId)) {
          LOG.info(
              "Normalized owner process {} for table {} before planning",
              ownerProcessId,
              tableRuntime.getTableIdentifier());
          return true;
        }
        continue;
      }

      if (shouldRecycleStaleOwner(processMeta)
          && recycleStaleOwner(tableRuntime, processMeta, ownerProcessId)) {
        continue;
      }

      LOG.info(
          "Skip planning table {} because owner process {} is still active with status {}",
          tableRuntime.getTableIdentifier(),
          ownerProcessId,
          processMeta.getStatus());
      return false;
    }

    if (tableRuntime.getProcessId() == 0L) {
      return true;
    }
    LOG.warn(
        "Skip planning table {} because owner normalization exceeded retry budget",
        tableRuntime.getTableIdentifier());
    return false;
  }

  private boolean shouldRecycleStaleOwner(TableProcessMeta processMeta) {
    return System.currentTimeMillis() - processMeta.getCreateTime() > STALE_OWNER_RECYCLE_MILLIS;
  }

  private boolean recycleStaleOwner(
      DefaultTableRuntime tableRuntime, TableProcessMeta processMeta, long ownerProcessId) {
    int unfinishedTasks =
        getAs(
            OptimizingProcessMapper.class,
            mapper ->
                mapper.countUnfinishedTasks(
                    tableRuntime.getTableIdentifier().getId(), ownerProcessId));
    if (unfinishedTasks > 0) {
      return false;
    }

    String staleReason =
        String.format(
            "recycled stale optimizing owner process %d after %d ms without unfinished tasks",
            ownerProcessId, System.currentTimeMillis() - processMeta.getCreateTime());
    doAsTransaction(
        () ->
            doAs(
                TableProcessMapper.class,
                mapper ->
                    mapper.updateProcess(
                        tableRuntime.getTableIdentifier().getId(),
                        ownerProcessId,
                        processMeta.getExternalProcessIdentifier(),
                        ProcessStatus.FAILED,
                        tableRuntime.getOptimizingStatus().name().toLowerCase(),
                        processMeta.getRetryNumber(),
                        System.currentTimeMillis(),
                        staleReason,
                        processMeta.getProcessParameters() == null
                            ? new HashMap<>()
                            : processMeta.getProcessParameters(),
                        processMeta.getSummary() == null
                            ? new HashMap<>()
                            : processMeta.getSummary())),
        () -> {
          if (!tableRuntime.normalizeProcessOwner(ownerProcessId)) {
            throw new IllegalStateException(
                String.format(
                    "failed to clear stale owner process %d for table %s",
                    ownerProcessId, tableRuntime.getTableIdentifier()));
          }
        },
        () ->
            tableRuntime
                .store()
                .begin()
                .updateStatusCode(code -> OptimizingStatus.PENDING.getCode())
                .commit());
    LOG.warn(
        "Recycled stale owner process {} for table {}",
        ownerProcessId,
        tableRuntime.getTableIdentifier());
    return true;
  }

  private boolean isActiveProcess(ProcessStatus status) {
    return ACTIVE_PROCESS_STATUSES.contains(status);
  }

  private TableOptimizingProcess planInternal(DefaultTableRuntime tableRuntime) {
    if (!prepareOwnerForPlanning(tableRuntime)) {
      return null;
    }
    tableRuntime.beginPlanning();
    try {
      ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
      AmoroTable<?> table = catalogManager.loadTable(identifier.getIdentifier());
      tableRuntime.refresh(table);

      if (!isFormatSupported(tableRuntime)) {
        tableRuntime.completeEmptyProcess();
        return null;
      }

      TableOptimizingPlanner planner =
          router
              .forFormat(tableRuntime.getFormat())
              .createPlanner(tableRuntime, table, getAvailableCore(), maxInputSizePerThread());
      if (planner.isNecessary()) {
        OptimizingPlanResult planResult = planner.plan();
        if (planResult.getTasks() == null || planResult.getTasks().isEmpty()) {
          LOG.info(
              "Planner {} returned empty tasks despite isNecessary=true; skip creating empty process for {}",
              planner.getClass().getSimpleName(),
              tableRuntime.getTableIdentifier());
          tableRuntime.completeEmptyProcess();
          return null;
        }
        return new TableOptimizingProcess(planResult, tableRuntime);
      } else {
        tableRuntime.completeEmptyProcess();
        return null;
      }
    } catch (Throwable throwable) {
      if (containsCause(throwable, OptimizingOwnerConflictException.class)) {
        long currentOwner = tableRuntime.getProcessId();
        if (currentOwner == 0L) {
          // Defensive fallback for rare races where owner changed after beginPlanning but before
          // exception handling. Keep the table schedulable instead of leaving it in PLANNING.
          tableRuntime.planFailed();
          LOG.warn(
              "Owner conflict observed for table {} but owner is already cleared, reset status to PENDING",
              tableRuntime.getTableIdentifier());
        } else {
          LOG.info(
              "Skip planning table {} because optimizing owner {} is already held by another process",
              tableRuntime.getTableIdentifier(),
              currentOwner);
        }
        return null;
      }
      tableRuntime.planFailed();
      LOG.error("Planning table {} failed", tableRuntime.getTableIdentifier(), throwable);
      throw throwable;
    }
  }

  private boolean containsCause(Throwable throwable, Class<? extends Throwable> causeClass) {
    Throwable current = throwable;
    while (current != null) {
      if (causeClass.isInstance(current)) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  public void ackTask(OptimizingTaskId taskId, OptimizerThread thread) {
    findProcess(taskId).ackTask(taskId, thread);
  }

  public void completeTask(OptimizerThread thread, OptimizingTaskResult result) {
    findProcess(result.getTaskId()).completeTask(thread, result);
  }

  public List<TaskRuntime<?>> collectTasks() {
    return tableQueue.stream()
        .flatMap(p -> p.getTaskMap().values().stream())
        .collect(Collectors.toList());
  }

  public List<TaskRuntime<?>> collectTasks(Predicate<TaskRuntime<?>> predicate) {
    return tableQueue.stream()
        .flatMap(p -> p.getTaskMap().values().stream())
        .filter(predicate)
        .collect(Collectors.toList());
  }

  public void retryTask(TaskRuntime<?> taskRuntime) {
    findProcess(taskRuntime.getTaskId()).resetTask(taskRuntime);
  }

  private void resetStaleTasksForThread(OptimizerThread thread) {
    // Only reset ACKED tasks: if the same (token, threadId) is polling again,
    // the executor must have finished execution (poll → ack → execute → complete → poll).
    // SCHEDULED tasks are NOT reset because the executor can still ack them normally,
    // even after AMS restart.
    collectTasks(
            task ->
                task.getStatus() == TaskRuntime.Status.ACKED
                    && Objects.equals(task.getToken(), thread.getToken())
                    && task.getThreadId() == thread.getThreadId())
        .forEach(
            task -> {
              LOG.warn(
                  "Resetting stale ACKED task {} because optimizer thread {} is polling new task",
                  task.getTaskId(),
                  thread);
              retryTask(task);
            });
  }

  public ResourceGroup getOptimizerGroup() {
    return optimizerGroup;
  }

  public void updateOptimizerGroup(ResourceGroup optimizerGroup) {
    Preconditions.checkArgument(
        this.optimizerGroup.getName().equals(optimizerGroup.getName()),
        "optimizer group name mismatch");
    this.optimizerGroup = optimizerGroup;
    scheduler.setTableSorterIfNeeded(optimizerGroup);
  }

  public void addOptimizer(OptimizerInstance optimizerInstance) {
    this.metrics.addOptimizer(optimizerInstance);
  }

  public void removeOptimizer(OptimizerInstance optimizerInstance) {
    this.metrics.removeOptimizer(optimizerInstance);
  }

  public void dispose() {
    warmingTables.clear();
    warmedTables.clear();
    this.metrics.unregister();
  }

  private TableOptimizingProcess findProcess(OptimizingTaskId taskId) {
    return tableQueue.stream()
        .filter(p -> p.getProcessId() == taskId.getProcessId())
        .findFirst()
        .orElseThrow(() -> new TaskNotFoundException(taskId));
  }

  private double getAvailableCore() {
    // the available core should be at least 1
    return Math.max(quotaProvider.getTotalQuota(optimizerGroup.getName()), 1);
  }

  private long maxInputSizePerThread() {
    return CompatiblePropertyUtil.propertyAsLong(
        optimizerGroup.getProperties(),
        OptimizerProperties.MAX_INPUT_FILE_SIZE_PER_THREAD,
        OptimizerProperties.MAX_INPUT_FILE_SIZE_PER_THREAD_DEFAULT);
  }

  @VisibleForTesting
  SchedulingPolicy getSchedulingPolicy() {
    return scheduler;
  }

  private void clearWarmupState(DefaultTableRuntime tableRuntime) {
    ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
    warmingTables.remove(identifier);
    warmedTables.remove(identifier);
  }

  @VisibleForTesting
  boolean containsWarmupStateForTest(ServerTableIdentifier identifier) {
    return warmingTables.contains(identifier) || warmedTables.contains(identifier);
  }

  private class TableOptimizingProcess implements OptimizingProcess {

    private final Lock lock = new ReentrantLock();
    private final long processId;
    private final TableFormat format;
    private final OptimizingType optimizingType;
    private final DefaultTableRuntime tableRuntime;
    private final long planTime;
    private final long targetSnapshotId;
    private final long targetChangeSnapshotId;
    // Widened to StagedTaskDescriptor<?,?,?> so non-Iceberg formats (Paimon today, Mixed-Hive
    // tomorrow) share the same process/queue machinery without unchecked downcasts.
    private final Map<OptimizingTaskId, TaskRuntime<? extends StagedTaskDescriptor<?, ?, ?>>>
        taskMap = Maps.newHashMap();
    private final Queue<TaskRuntime<? extends StagedTaskDescriptor<?, ?, ?>>> taskQueue =
        new LinkedList<>();
    private volatile ProcessStatus status = ProcessStatus.RUNNING;
    private volatile String failedReason;
    private long endTime = AmoroServiceConstants.INVALID_TIME;
    private Map<String, Long> fromSequence = Maps.newHashMap();
    private Map<String, Long> toSequence = Maps.newHashMap();
    private boolean hasCommitted = false;

    public TaskRuntime<?> poll(OptimizerThread thread, boolean needQuotaChecking) {
      try {
        // Wait 10ms here for some light operation like poll/ack
        if (lock.tryLock(10, TimeUnit.MILLISECONDS)) {
          try {
            TaskRuntime<?> task = null;
            if (status != ProcessStatus.KILLED && status != ProcessStatus.FAILED) {
              int actualQuota = getActualQuota();
              int quotaLimit = getQuotaLimit();
              if (!needQuotaChecking || actualQuota < quotaLimit) {
                task = taskQueue.poll();
              }
            }
            if (task != null) {
              optimizingTasksMap
                  .computeIfAbsent(tableRuntime.getTableIdentifier(), k -> new AtomicInteger(0))
                  .incrementAndGet();
              task.schedule(thread);
            }
            return task;
          } finally {
            lock.unlock();
          }
        }
      } catch (InterruptedException e) {
        // ignore it.
      }
      return null;
    }

    public TableOptimizingProcess(
        OptimizingPlanResult planResult, DefaultTableRuntime tableRuntime) {
      processId = planResult.getProcessId();
      this.tableRuntime = tableRuntime;
      this.format = tableRuntime.getFormat();
      optimizingType = planResult.getOptimizingType();
      planTime = planResult.getPlanTime();
      targetSnapshotId = planResult.getTargetSnapshotId();
      targetChangeSnapshotId = planResult.getTargetChangeSnapshotId();
      loadTaskRuntimes(planResult.getTasks());
      fromSequence = planResult.getFromSequence();
      toSequence = planResult.getToSequence();
      beginAndPersistProcess();
    }

    public TableOptimizingProcess(
        DefaultTableRuntime tableRuntime,
        TableProcessMeta processMeta,
        OptimizingProcessState processState) {
      this.tableRuntime = tableRuntime;
      this.format = tableRuntime.getFormat();
      processId = tableRuntime.getProcessId();
      optimizingType = OptimizingType.valueOf(processMeta.getProcessType());
      targetSnapshotId = processState.getTargetSnapshotId();
      targetChangeSnapshotId = processState.getTargetChangeSnapshotId();
      planTime = processMeta.getCreateTime();
      if (processState.getFromSequence() != null) {
        fromSequence = processState.getFromSequence();
      }
      if (processState.getToSequence() != null) {
        toSequence = processState.getToSequence();
      }
      this.status = processMeta.getStatus();
      tableRuntime.recover(this);
      loadTaskRuntimes(this);
    }

    private int getQuotaLimit() {
      double targetQuota = tableRuntime.getOptimizingConfig().getTargetQuota();
      return targetQuota > 1
          ? (int) targetQuota
          : (int) Math.ceil(targetQuota * getAvailableCore());
    }

    @Override
    public long getTableId() {
      return tableRuntime.getTableIdentifier().getId();
    }

    @Override
    public long getProcessId() {
      return processId;
    }

    TableFormat getFormat() {
      return format;
    }

    @Override
    public OptimizingType getOptimizingType() {
      return optimizingType;
    }

    @Override
    public ProcessStatus getStatus() {
      return status;
    }

    @Override
    public void close(boolean needCommit) {
      lock.lock();
      try {
        if (this.status != ProcessStatus.RUNNING) {
          return;
        }
        if (tableRuntime.isAllowPartialCommit() && needCommit) {
          tableRuntime.beginCommitting();
        } else {
          this.status = ProcessStatus.CLOSED;
          this.endTime = System.currentTimeMillis();
          persistAndSetCompleted(false);
        }
      } finally {
        lock.unlock();
      }
    }

    private void ackTask(OptimizingTaskId taskId, OptimizerThread thread) {
      TaskRuntime<?> taskRuntime = getTaskRuntime(taskId);
      lock.lock();
      try {
        taskRuntime.ack(thread);
      } finally {
        lock.unlock();
      }
    }

    private void completeTask(OptimizerThread thread, OptimizingTaskResult result) {
      TaskRuntime<?> taskRuntime = getTaskRuntime(result.getTaskId());
      lock.lock();
      try {
        taskRuntime.complete(thread, result);
      } finally {
        lock.unlock();
      }
    }

    private TaskRuntime<?> getTaskRuntime(OptimizingTaskId taskId) {
      TaskRuntime<?> taskRuntime = getTaskMap().get(taskId);
      if (taskRuntime == null) {
        throw new TaskNotFoundException(taskId);
      }
      return taskRuntime;
    }

    private void acceptResult(TaskRuntime<?> taskRuntime) {
      lock.lock();
      try {
        optimizingTasksMap.computeIfPresent(
            tableRuntime.getTableIdentifier(),
            (k, v) -> {
              if (v.get() > 0) {
                v.decrementAndGet();
              }
              return v;
            });
        // task cancel means persistAndSetCompleted has been called and start/end may be absent.
        if (taskRuntime.getStatus() == TaskRuntime.Status.CANCELED) {
          return;
        }
        try {
          tableRuntime.addTaskQuota(taskRuntime.getCurrentQuota());
        } catch (Throwable throwable) {
          LOG.warn(
              "{} failed to add task quota {}, ignore it",
              tableRuntime.getTableIdentifier(),
              taskRuntime.getTaskId(),
              throwable);
        }
        if (isClosed()) {
          throw new OptimizingClosedException(processId);
        }
        if (taskRuntime.getStatus() == TaskRuntime.Status.SUCCESS) {
          // the lock of TableOptimizingProcess makes it thread-safe
          if (allTasksPrepared()
              && tableRuntime.getOptimizingStatus().isProcessing()
              && tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
            tableRuntime.beginCommitting();
          }
        } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
          if (taskRuntime.getRetry()
              < tableRuntime.getOptimizingConfig().getMaxExecuteRetryCount()) {
            LOG.info(
                "Put task {} to retry queue, because {}",
                taskRuntime.getTaskId(),
                taskRuntime.getFailReason());
            retryTask(taskRuntime);
          } else {
            if (tableRuntime.isAllowPartialCommit()
                && tableRuntime.getOptimizingStatus().isProcessing()
                && tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
              LOG.info(
                  "Task {} has reached the max execute retry count. Process {} cancels unfinished tasks and commits SUCCESS tasks.",
                  taskRuntime.getTaskId(),
                  processId);
              failedReason = taskRuntime.getFailReason();
              tableRuntime.beginCommitting();
            } else {
              LOG.info(
                  "Task {} has reached the max execute retry count. Process {} failed.",
                  taskRuntime.getTaskId(),
                  processId);
              this.failedReason = taskRuntime.getFailReason();
              this.status = ProcessStatus.FAILED;
              this.endTime = taskRuntime.getEndTime();
              persistAndSetCompleted(false);
            }
          }
        }
      } finally {
        lock.unlock();
      }
    }

    private void resetTask(TaskRuntime<?> taskRuntime) {
      lock.lock();
      try {
        taskRuntime.reset();
        // Cast is safe: only descriptors that already entered the widened taskMap end up here; the
        // public retryTask(TaskRuntime<?>) entry point forwards whatever pollTask previously
        // returned, and pollTask only hands out entries sourced from taskMap itself.
        @SuppressWarnings("unchecked")
        TaskRuntime<? extends StagedTaskDescriptor<?, ?, ?>> widened =
            (TaskRuntime<? extends StagedTaskDescriptor<?, ?, ?>>) taskRuntime;
        taskQueue.add(widened);
      } finally {
        lock.unlock();
      }
    }

    @Override
    public boolean isClosed() {
      return status == ProcessStatus.KILLED;
    }

    @Override
    public long getPlanTime() {
      return planTime;
    }

    @Override
    public long getDuration() {
      long dur =
          endTime == AmoroServiceConstants.INVALID_TIME
              ? System.currentTimeMillis() - planTime
              : endTime - planTime;
      return Math.max(0, dur);
    }

    @Override
    public long getTargetSnapshotId() {
      return targetSnapshotId;
    }

    @Override
    public long getTargetChangeSnapshotId() {
      return targetChangeSnapshotId;
    }

    public String getFailedReason() {
      return failedReason;
    }

    private Map<OptimizingTaskId, TaskRuntime<? extends StagedTaskDescriptor<?, ?, ?>>>
        getTaskMap() {
      return taskMap;
    }

    /**
     * if all tasks are Prepared
     *
     * @return true if tasks is not empty and all Prepared
     */
    private boolean allTasksPrepared() {
      if (!taskMap.isEmpty()) {
        return taskMap.values().stream().allMatch(t -> t.getStatus() == TaskRuntime.Status.SUCCESS);
      }
      return false;
    }

    /**
     * Get optimizeRuntime.
     *
     * @return -
     */
    @Override
    public long getRunningQuotaTime(long calculatingStartTime, long calculatingEndTime) {
      return taskMap.values().stream()
          .filter(t -> !t.finished())
          .mapToLong(task -> task.getQuotaTime(calculatingStartTime, calculatingEndTime))
          .sum();
    }

    public int getActualQuota() {
      return optimizingTasksMap
          .getOrDefault(tableRuntime.getTableIdentifier(), new AtomicInteger(0))
          .get();
    }

    @Override
    public void commit() {
      List<TaskRuntime<? extends StagedTaskDescriptor<?, ?, ?>>> successTasks =
          taskMap.values().stream()
              .filter(task -> task.getStatus() == Status.SUCCESS)
              .collect(Collectors.toList());
      LOG.debug(
          "{} get {} tasks to commit", tableRuntime.getTableIdentifier(), successTasks.size());

      lock.lock();
      try {
        if (hasCommitted) {
          LOG.warn("{} has already committed, give up", tableRuntime.getTableIdentifier());
          try {
            persistAndSetCompleted(status == ProcessStatus.SUCCESS);
          } catch (Exception ignored) {
          }
          throw new IllegalStateException("repeat commit, and last error " + failedReason);
        }
        try {
          hasCommitted = true;
          if (successTasks.isEmpty()) {
            status = ProcessStatus.FAILED;
            failedReason =
                "No successful task is available for commit, stop building committer to avoid"
                    + " invalid commit identity.";
            endTime = System.currentTimeMillis();
            persistAndSetCompleted(false);
            return;
          }
          buildCommit().commit();
          if (allTasksPrepared()) {
            status = ProcessStatus.SUCCESS;
          } else if (taskMap.values().stream()
              .anyMatch(task -> task.getStatus() == TaskRuntime.Status.FAILED)) {
            status = ProcessStatus.FAILED;
          } else {
            status = ProcessStatus.CLOSED;
          }
          endTime = System.currentTimeMillis();
          persistAndSetCompleted(status == ProcessStatus.SUCCESS);
        } catch (PersistenceException e) {
          LOG.warn(
              "{} failed to persist process completed, will retry next commit",
              tableRuntime.getTableIdentifier(),
              e);
        } catch (Throwable t) {
          LOG.error("{} Commit optimizing failed ", tableRuntime.getTableIdentifier(), t);
          status = ProcessStatus.FAILED;
          failedReason = ExceptionUtil.getErrorMessage(t, 4000);
          endTime = System.currentTimeMillis();
          persistAndSetCompleted(false);
        }
      } finally {
        lock.unlock();
      }
    }

    @Override
    public MetricsSummary getSummary() {
      // Route through StagedTaskDescriptor.toMetricsSummary() so non-Iceberg descriptors (e.g.
      // PaimonCompactionTask) contribute to the aggregate without OptimizingQueue needing to know
      // their concrete type. MetricsSummary#aggregate handles the mixed list.
      List<TaskMetricsSummary> taskSummaries =
          taskMap.values().stream()
              .map(TaskRuntime::getTaskDescriptor)
              .map(StagedTaskDescriptor::toMetricsSummary)
              .collect(Collectors.toList());

      return MetricsSummary.aggregate(taskSummaries);
    }

    private TableOptimizingCommitter buildCommit() {
      AmoroTable<?> table =
          catalogManager.loadTable(tableRuntime.getTableIdentifier().getIdentifier());
      List<? extends StagedTaskDescriptor<?, ?, ?>> taskDescriptors =
          taskMap.values().stream()
              .filter(task -> task.getStatus() == Status.SUCCESS)
              .map(TaskRuntime::getTaskDescriptor)
              .collect(Collectors.toList());
      return router
          .forFormat(format)
          .createCommitter(
              table,
              targetSnapshotId,
              targetChangeSnapshotId,
              taskDescriptors,
              fromSequence,
              toSequence);
    }

    private void beginAndPersistProcess() {
      doAsTransaction(
          () ->
              doAs(
                  TableProcessMapper.class,
                  mapper ->
                      mapper.insertProcess(
                          tableRuntime.getTableIdentifier().getId(),
                          processId,
                          "",
                          status,
                          optimizingType.name().toUpperCase(),
                          tableRuntime.getOptimizingStatus().name().toLowerCase(),
                          "AMORO",
                          0,
                          planTime,
                          new HashMap<>(),
                          getSummary().summaryAsMap(false))),
          () ->
              doAs(
                  OptimizingProcessMapper.class,
                  mapper ->
                      mapper.insertInternalProcessState(
                          tableRuntime.getTableIdentifier().getId(),
                          processId,
                          targetSnapshotId,
                          targetChangeSnapshotId,
                          fromSequence,
                          toSequence)),
          () ->
              doAs(
                  OptimizingProcessMapper.class,
                  mapper -> mapper.insertTaskRuntimes(typedTaskRuntimes())),
          () -> TaskFilesPersistence.persistTaskInputs(processId, typedTaskRuntimes()),
          () -> tableRuntime.beginProcess(this));
    }

    private void persistAndSetCompleted(boolean success) {
      doAsTransaction(
          () -> {
            if (!success) {
              cancelTasks();
            }
          },
          () ->
              doAs(
                  TableProcessMapper.class,
                  mapper ->
                      mapper.updateProcess(
                          tableRuntime.getTableIdentifier().getId(),
                          processId,
                          "",
                          status,
                          tableRuntime.getOptimizingStatus().name().toLowerCase(),
                          0,
                          System.currentTimeMillis(),
                          getFailedReason(),
                          new HashMap<>(),
                          getSummary().summaryAsMap(false))),
          () -> tableRuntime.completeProcess(this, success),
          () -> clearProcess(this));
    }

    private void cancelTasks() {
      taskMap.values().forEach(TaskRuntime::tryCanceling);
    }

    /**
     * Narrow the task-map values to the bound expected by {@code insertTaskRuntimes} and {@code
     * TaskFilesPersistence}. Every descriptor that enters {@code taskMap} extends {@code
     * StagedTaskDescriptor<I,?,?>} where {@code I extends BaseOptimizingInput}, so the unchecked
     * cast is safe.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<TaskRuntime<? extends StagedTaskDescriptor<? extends BaseOptimizingInput, ?, ?>>>
        typedTaskRuntimes() {
      return (List) Lists.newArrayList(taskMap.values());
    }

    /**
     * Bind a loaded {@link BaseOptimizingInput} back into its descriptor. The helper exists solely
     * to "capture" the descriptor's {@code I} type parameter so we can pass the polymorphic input
     * through {@link StagedTaskDescriptor#setInput} without the caller knowing the concrete
     * subclass. Type safety is guaranteed by {@code TaskDescriptorTypeConverter} pairing the
     * descriptor class with the input written by {@code TaskFilesPersistence#persistTaskInputs}.
     *
     * <p>A null {@code input} is passed through unchanged (preserving the pre-C4 behaviour where a
     * missing entry in the loaded map would null out the descriptor's input — each {@code
     * calculateSummary} override tolerates this).
     */
    private <I extends BaseOptimizingInput> void bindLoadedInput(
        StagedTaskDescriptor<I, ?, ?> descriptor, BaseOptimizingInput input) {
      @SuppressWarnings("unchecked")
      I typedInput = (I) input;
      descriptor.setInput(typedInput);
    }

    /**
     * Back-fill {@link TaskProperties#TASK_EXECUTOR_FACTORY_IMPL} on descriptors restored from
     * pre-0.9 Iceberg rows whose {@code properties} column predates the multi-format refactor.
     *
     * <p>The injection is format-driven via {@link LegacyExecutorFactoryDefaults}: Iceberg /
     * Mixed-Iceberg / Mixed-Hive get their canonical executor factory class name; Paimon and
     * unknown formats are logged and skipped (Paimon planners always emit the key, so a missing
     * entry for Paimon indicates a corrupted row the optimizer cannot safely resume).
     *
     * <p>Idempotent: rows that already carry the key are untouched, so new-format rows co-located
     * with legacy Iceberg rows in the same process still work.
     */
    private void injectLegacyExecutorFactoryDefaults(
        List<TaskRuntime<? extends StagedTaskDescriptor<? extends BaseOptimizingInput, ?, ?>>>
            taskRuntimes) {
      for (TaskRuntime<? extends StagedTaskDescriptor<? extends BaseOptimizingInput, ?, ?>>
          taskRuntime : taskRuntimes) {
        StagedTaskDescriptor<? extends BaseOptimizingInput, ?, ?> descriptor =
            taskRuntime.getTaskDescriptor();
        if (descriptor == null) {
          continue;
        }
        Map<String, String> props = descriptor.getProperties();
        if (props != null && props.containsKey(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL)) {
          continue;
        }
        String defaultImpl =
            LegacyExecutorFactoryDefaults.resolveDefaultExecutorFactoryImpl(
                tableRuntime.getFormat());
        if (defaultImpl == null) {
          continue;
        }
        if (descriptor.ensureExecutorFactoryImpl(defaultImpl)) {
          LOG.info(
              "Restored legacy task {} with default {}={} (format={})",
              taskRuntime.getTaskId(),
              TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
              defaultImpl,
              tableRuntime.getFormat());
        }
      }
    }

    private void loadTaskRuntimes(OptimizingProcess optimizingProcess) {
      try {
        // Recovery is format-agnostic after C3/C4: TaskDescriptorTypeConverter resolves the
        // descriptor subclass from TASK_EXECUTOR_FACTORY_IMPL, and TaskFilesPersistence returns
        // a Map<Integer, BaseOptimizingInput> whose concrete values carry their own runtime
        // class. We widen the compile-time view to StagedTaskDescriptor<? extends
        // BaseOptimizingInput, ?, ?> so the same call recovers both Iceberg and Paimon rows.
        List<TaskRuntime<? extends StagedTaskDescriptor<? extends BaseOptimizingInput, ?, ?>>>
            taskRuntimes =
                getAs(
                    OptimizingProcessMapper.class,
                    mapper ->
                        mapper.selectTaskRuntimes(
                            tableRuntime.getTableIdentifier().getId(), processId));
        Map<Integer, BaseOptimizingInput> inputs = TaskFilesPersistence.loadTaskInputs(processId);
        // Compensation for pre-0.9 Iceberg rows: TaskDescriptorTypeConverter correctly routes rows
        // with a missing TASK_EXECUTOR_FACTORY_IMPL to RewriteStageTask, but the key itself is
        // still absent from the descriptor's properties map on restore. OptimizerExecutor later
        // reads that key via reflection to build the executor factory — without this injection it
        // would NPE. Idempotent: we never overwrite an existing key (e.g. for Paimon rows where
        // the Paimon planner already populated it).
        injectLegacyExecutorFactoryDefaults(taskRuntimes);
        taskRuntimes.forEach(
            taskRuntime -> {
              taskRuntime.getCompletedFuture().whenCompleted(() -> acceptResult(taskRuntime));
              BaseOptimizingInput input = inputs.get(taskRuntime.getTaskId().getTaskId());
              TaskDescriptorRecoveryTypes.validateRecoveredTask(
                  taskRuntime.getTaskDescriptor(), input, tableRuntime.getFormat());
              bindLoadedInput(taskRuntime.getTaskDescriptor(), input);
              taskMap.put(taskRuntime.getTaskId(), taskRuntime);
              if (taskRuntime.getStatus() == TaskRuntime.Status.PLANNED) {
                taskQueue.offer(taskRuntime);
              } else if (taskRuntime.getStatus() == TaskRuntime.Status.SCHEDULED
                  || taskRuntime.getStatus() == TaskRuntime.Status.ACKED) {
                // Don't reset — let the optimizer finish execution and report
                // the result. If the optimizer is dead, OptimizerKeeper will
                // detect the stale token and reset the task via retryTask().
                LOG.debug(
                    "Keeping task {} in {} during recovery, waiting for optimizer to complete",
                    taskRuntime.getTaskId(),
                    taskRuntime.getStatus());
              } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
                retryTask(taskRuntime);
              }
            });
      } catch (IllegalArgumentException | ClassCastException e) {
        LOG.warn(
            "Load task inputs failed, close the optimizing process : {}",
            optimizingProcess.getProcessId(),
            e);
        optimizingProcess.close(false);
      }
    }

    private <T extends StagedTaskDescriptor<?, ?, ?>> void loadTaskRuntimes(
        List<T> taskDescriptors) {
      int taskId = 1;
      for (T taskDescriptor : taskDescriptors) {
        TaskRuntime<T> taskRuntime =
            new TaskRuntime<>(new OptimizingTaskId(processId, taskId++), taskDescriptor);
        LOG.info(
            "{} plan new task {}, summary {}",
            tableRuntime.getTableIdentifier(),
            taskRuntime.getTaskId(),
            taskRuntime.getSummary());
        taskRuntime.getCompletedFuture().whenCompleted(() -> acceptResult(taskRuntime));
        taskMap.put(taskRuntime.getTaskId(), taskRuntime);
        taskQueue.offer(taskRuntime);
      }
    }
  }
}

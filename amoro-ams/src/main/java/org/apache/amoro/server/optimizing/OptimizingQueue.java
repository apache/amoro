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
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.exception.TaskNotFoundException;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.plan.AbstractOptimizingPlanner;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.persistence.OptimizingProcessState;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.blocker.TableBlocker;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OptimizingQueue extends PersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingQueue.class);

  private final QuotaProvider quotaProvider;
  private final Queue<OptimizingTableProcess> tableQueue = new LinkedTransferQueue<>();
  private final SchedulingPolicy scheduler;
  private final CatalogManager catalogManager;
  private final Executor planExecutor;
  // Keep all planning table identifiers
  private final Set<ServerTableIdentifier> planningTables = new HashSet<>();
  private final Lock scheduleLock = new ReentrantLock();
  private final Condition planningCompleted = scheduleLock.newCondition();
  private final int maxPlanningParallelism;
  private final OptimizerGroupMetrics metrics;
  private ResourceGroup optimizerGroup;
  private final Map<ServerTableIdentifier, AtomicInteger> optimizingTasksMap =
      new ConcurrentHashMap<>();

  public OptimizingQueue(
      CatalogManager catalogManager,
      ResourceGroup optimizerGroup,
      QuotaProvider quotaProvider,
      Executor planExecutor,
      List<DefaultTableRuntime> tableRuntimeList,
      int maxPlanningParallelism) {
    Preconditions.checkNotNull(optimizerGroup, "Optimizer group can not be null");
    this.planExecutor = planExecutor;
    this.optimizerGroup = optimizerGroup;
    this.quotaProvider = quotaProvider;
    this.scheduler = new SchedulingPolicy(optimizerGroup);
    this.catalogManager = catalogManager;
    this.maxPlanningParallelism = maxPlanningParallelism;
    this.metrics =
        new OptimizerGroupMetrics(
            optimizerGroup.getName(), MetricManager.getInstance().getGlobalRegistry(), this);
    this.metrics.register();
    tableRuntimeList.forEach(this::initTableRuntime);
  }

  private void initTableRuntime(DefaultTableRuntime tableRuntime) {
    try {
      OptimizingTableProcess process = loadProcess(tableRuntime);

      if (!tableRuntime.getOptimizingConfig().isEnabled()) {
        closeProcessIfRunning(process);
        return;
      }

      tableRuntime.resetTaskQuotas(
          System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME);

      if (canResumeProcess(process, tableRuntime)) {
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
    } catch (Exception e) {
      LOG.error(
          "Failed to initialize table runtime for table {}, skipping",
          tableRuntime.getTableIdentifier(),
          e);
    }
  }

  private OptimizingTableProcess loadProcess(DefaultTableRuntime tableRuntime) {
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
    return new OptimizingTableProcess(
        tableRuntime, meta, state, catalogManager, quotaProvider, optimizingTasksMap, null);
  }

  private boolean canResumeProcess(
      OptimizingTableProcess process, DefaultTableRuntime tableRuntime) {
    return process != null
        && process.getStatus() == ProcessStatus.RUNNING
        && tableRuntime.getOptimizingStatus().isProcessing()
        && tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING;
  }

  private void resetTableForRecovery(
      OptimizingTableProcess process, DefaultTableRuntime tableRuntime) {
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

  private void closeProcessIfRunning(OptimizingTableProcess process) {
    if (process != null && process.getStatus() == ProcessStatus.RUNNING) {
      process.close(false);
    }
  }

  public String getContainerName() {
    return optimizerGroup.getContainer();
  }

  public void refreshTable(DefaultTableRuntime tableRuntime) {
    if (tableRuntime.getOptimizingConfig().isEnabled()
        && !tableRuntime.getOptimizingStatus().isProcessing()) {
      LOG.info(
          "Bind queue {} success with table {}",
          optimizerGroup.getName(),
          tableRuntime.getTableIdentifier());
      tableRuntime.resetTaskQuotas(
          System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME);
      scheduler.addTable(tableRuntime);
    }
  }

  public void releaseTable(DefaultTableRuntime tableRuntime) {
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
    while (task == null && waitTask(deadline)) {
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

  private boolean waitTask(long waitDeadline) {
    scheduleLock.lock();
    try {
      long currentTime = System.currentTimeMillis();
      scheduleTableIfNecessary(currentTime);
      return waitDeadline > currentTime
          && planningCompleted.await(waitDeadline - currentTime, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      LOG.error("Schedule table interrupted", e);
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

  private void scheduleTableIfNecessary(long startTime) {
    if (planningTables.size() < maxPlanningParallelism) {
      Set<ServerTableIdentifier> skipTables = new HashSet<>(planningTables);
      skipBlockedTables(skipTables);
      Optional.ofNullable(scheduler.scheduleTable(skipTables))
          .ifPresent(tableRuntime -> triggerAsyncPlanning(tableRuntime, skipTables, startTime));
    }
  }

  private void skipBlockedTables(Set<ServerTableIdentifier> skipTables) {
    List<TableBlocker> tableBlockerList =
        getAs(
            TableBlockerMapper.class,
            mapper -> mapper.selectAllBlockers(System.currentTimeMillis()));
    Map<TableIdentifier, ServerTableIdentifier> identifierMap = Maps.newHashMap();
    for (ServerTableIdentifier identifier : scheduler.getTableRuntimeMap().keySet()) {
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

  private void triggerAsyncPlanning(
      DefaultTableRuntime tableRuntime, Set<ServerTableIdentifier> skipTables, long startTime) {
    LOG.info(
        "Trigger planning table {} by policy {}",
        tableRuntime.getTableIdentifier(),
        scheduler.name());
    planningTables.add(tableRuntime.getTableIdentifier());
    CompletableFuture.supplyAsync(() -> planInternal(tableRuntime), planExecutor)
        .whenComplete(
            (process, throwable) -> {
              if (throwable != null) {
                LOG.error("Failed to plan table {}", tableRuntime.getTableIdentifier(), throwable);
              }
              long currentTime = System.currentTimeMillis();
              scheduleLock.lock();
              try {
                tableRuntime.setLastPlanTime(currentTime);
                planningTables.remove(tableRuntime.getTableIdentifier());
                if (process != null) {
                  tableQueue.offer(process);
                  String skipIds =
                      skipTables.stream()
                          .map(ServerTableIdentifier::getId)
                          .sorted()
                          .map(item -> item + "")
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
                planningCompleted.signalAll();
              } finally {
                scheduleLock.unlock();
              }
            });
  }

  private OptimizingTableProcess planInternal(DefaultTableRuntime tableRuntime) {
    tableRuntime.beginPlanning();
    try {
      ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
      AmoroTable<?> table = catalogManager.loadTable(identifier.getIdentifier());
      AbstractOptimizingPlanner planner =
          IcebergTableUtil.createOptimizingPlanner(
              tableRuntime.refresh(table),
              (MixedTable) table.originalTable(),
              getAvailableCore(),
              maxInputSizePerThread());
      if (planner.isNecessary()) {
        return new OptimizingTableProcess(
            planner,
            tableRuntime,
            catalogManager,
            quotaProvider,
            optimizingTasksMap,
            () -> clearProcessById(planner.getProcessId()));
      } else {
        tableRuntime.completeEmptyProcess();
        return null;
      }
    } catch (Throwable throwable) {
      tableRuntime.planFailed();
      LOG.error("Planning table {} failed", tableRuntime.getTableIdentifier(), throwable);
      throw throwable;
    }
  }

  private void clearProcessById(long processId) {
    tableQueue.removeIf(process -> process.getProcessId() == processId);
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
    findProcess(taskRuntime.getTaskId()).resetTask((TaskRuntime<RewriteStageTask>) taskRuntime);
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
    this.metrics.unregister();
  }

  private OptimizingTableProcess findProcess(OptimizingTaskId taskId) {
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
}

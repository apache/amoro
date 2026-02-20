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
import org.apache.amoro.exception.OptimizingClosedException;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.exception.TaskNotFoundException;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.plan.AbstractOptimizingPlanner;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.TaskRuntime.Status;
import org.apache.amoro.server.persistence.OptimizingProcessState;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TaskFilesPersistence;
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
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.ExceptionUtil;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
  private final Queue<TableOptimizingProcess> tableQueue = new LinkedTransferQueue<>();
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
    TableOptimizingProcess process = null;
    if (tableRuntime.getOptimizingStatus().isProcessing() && tableRuntime.getProcessId() != 0) {
      TableProcessMeta meta =
          getAs(
              TableProcessMapper.class,
              mapper -> mapper.getProcessMeta(tableRuntime.getProcessId()));
      OptimizingProcessState state =
          getAs(
              OptimizingProcessMapper.class,
              mapper -> mapper.getProcessState(tableRuntime.getProcessId()));
      process = new TableOptimizingProcess(tableRuntime, meta, state);
      tableRuntime.recover(process);
    }

    if (tableRuntime.getOptimizingConfig().isEnabled()) {
      tableRuntime.resetTaskQuotas(
          System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME);
      // Close the committing process to avoid duplicate commit on the table.
      if (tableRuntime.getOptimizingStatus() == OptimizingStatus.COMMITTING) {
        if (process != null) {
          LOG.warn(
              "Close the committing process {} on table {}",
              process.getProcessId(),
              tableRuntime.getTableIdentifier());
          process.close(false);
        }
      }
      if (!tableRuntime.getOptimizingStatus().isProcessing()) {
        scheduler.addTable(tableRuntime);
      } else if (process != null) {
        tableQueue.offer(process);
      }
    } else {
      if (process != null) {
        process.close(false);
      }
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

  private TableOptimizingProcess planInternal(DefaultTableRuntime tableRuntime) {
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
        return new TableOptimizingProcess(planner, tableRuntime);
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

  private class TableOptimizingProcess implements OptimizingProcess {

    private final Lock lock = new ReentrantLock();
    private final long processId;
    private final OptimizingType optimizingType;
    private final DefaultTableRuntime tableRuntime;
    private final long planTime;
    private final long targetSnapshotId;
    private final long targetChangeSnapshotId;
    private final Map<OptimizingTaskId, TaskRuntime<RewriteStageTask>> taskMap = Maps.newHashMap();
    private final Queue<TaskRuntime<RewriteStageTask>> taskQueue = new LinkedList<>();
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
        AbstractOptimizingPlanner planner, DefaultTableRuntime tableRuntime) {
      processId = planner.getProcessId();
      this.tableRuntime = tableRuntime;
      optimizingType = planner.getOptimizingType();
      planTime = planner.getPlanTime();
      targetSnapshotId = planner.getTargetSnapshotId();
      targetChangeSnapshotId = planner.getTargetChangeSnapshotId();
      loadTaskRuntimes(planner.planTasks());
      fromSequence = planner.getFromSequence();
      toSequence = planner.getToSequence();
      beginAndPersistProcess();
    }

    public TableOptimizingProcess(
        DefaultTableRuntime tableRuntime,
        TableProcessMeta processMeta,
        OptimizingProcessState processState) {
      this.tableRuntime = tableRuntime;
      processId = tableRuntime.getProcessId();
      optimizingType = getOptimizingTypeFromAction(processMeta.getAction());
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
      if (this.status != ProcessStatus.KILLED) {
        tableRuntime.recover(this);
      }
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

    @Override
    public OptimizingType getOptimizingType() {
      return optimizingType;
    }

    /**
     * Convert OptimizingType to corresponding Action.
     *
     * @param optimizingType optimizing type
     * @return corresponding Action
     */
    private org.apache.amoro.Action getOptimizingAction(OptimizingType optimizingType) {
      switch (optimizingType) {
        case MINOR:
          return org.apache.amoro.IcebergActions.OPTIMIZING_MINOR;
        case MAJOR:
          return org.apache.amoro.IcebergActions.OPTIMIZING_MAJOR;
        case FULL:
          return org.apache.amoro.IcebergActions.OPTIMIZING_FULL;
        default:
          throw new IllegalArgumentException("Unknown optimizing type: " + optimizingType);
      }
    }

    /**
     * Convert Action to corresponding OptimizingType.
     *
     * @param action action
     * @return corresponding OptimizingType
     */
    private OptimizingType getOptimizingTypeFromAction(org.apache.amoro.Action action) {
      if (action == null) {
        throw new IllegalArgumentException("Action cannot be null");
      }
      String actionName = action.getName();
      if (org.apache.amoro.IcebergActions.OPTIMIZING_MINOR.getName().equals(actionName)) {
        return OptimizingType.MINOR;
      } else if (org.apache.amoro.IcebergActions.OPTIMIZING_MAJOR.getName().equals(actionName)) {
        return OptimizingType.MAJOR;
      } else if (org.apache.amoro.IcebergActions.OPTIMIZING_FULL.getName().equals(actionName)) {
        return OptimizingType.FULL;
      } else {
        // Fallback to old behavior for backward compatibility
        try {
          return OptimizingType.valueOf(actionName.toUpperCase());
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(
              "Cannot convert action " + actionName + " to OptimizingType", e);
        }
      }
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
        try {
          tableRuntime.addTaskQuota(taskRuntime.getCurrentQuota());
        } catch (Throwable throwable) {
          LOG.warn(
              "{} failed to add task quota {}, ignore it",
              tableRuntime.getTableIdentifier(),
              taskRuntime.getTaskId(),
              throwable);
        }
        // task cancel means persistAndSetCompleted has been called
        if (taskRuntime.getStatus() == TaskRuntime.Status.CANCELED) {
          return;
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

    private void resetTask(TaskRuntime<RewriteStageTask> taskRuntime) {
      lock.lock();
      try {
        taskRuntime.reset();
        taskQueue.add(taskRuntime);
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

    private Map<OptimizingTaskId, TaskRuntime<RewriteStageTask>> getTaskMap() {
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
      List<TaskRuntime<RewriteStageTask>> successTasks =
          taskMap.values().stream()
              .filter(task -> task.getStatus() == Status.SUCCESS)
              .collect(Collectors.toList());
      LOG.debug(
          "{} get {} tasks of {} partitions to commit",
          tableRuntime.getTableIdentifier(),
          successTasks.size(),
          successTasks.stream()
              .map(task -> task.getTaskDescriptor().getPartition())
              .distinct()
              .count());

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
      List<MetricsSummary> taskSummaries =
          taskMap.values().stream()
              .map(TaskRuntime::getTaskDescriptor)
              .map(RewriteStageTask::getSummary)
              .collect(Collectors.toList());

      return new MetricsSummary(taskSummaries);
    }

    private UnKeyedTableCommit buildCommit() {
      MixedTable table =
          (MixedTable)
              catalogManager
                  .loadTable(tableRuntime.getTableIdentifier().getIdentifier())
                  .originalTable();
      if (table.isUnkeyedTable()) {
        return new UnKeyedTableCommit(targetSnapshotId, table, taskMap.values());
      } else {
        return new KeyedTableCommit(
            table,
            taskMap.values(),
            targetSnapshotId,
            convertPartitionSequence(table, fromSequence),
            convertPartitionSequence(table, toSequence));
      }
    }

    private StructLikeMap<Long> convertPartitionSequence(
        MixedTable table, Map<String, Long> partitionSequence) {
      PartitionSpec spec = table.spec();
      StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
      partitionSequence.forEach(
          (partition, sequence) -> {
            if (spec.isUnpartitioned()) {
              results.put(TablePropertyUtil.EMPTY_STRUCT, sequence);
            } else {
              StructLike partitionData = MixedDataFiles.data(spec, partition);
              results.put(partitionData, sequence);
            }
          });
      return results;
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
                          getOptimizingAction(optimizingType),
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
                  mapper -> mapper.insertTaskRuntimes(Lists.newArrayList(taskMap.values()))),
          () -> TaskFilesPersistence.persistTaskInputs(processId, taskMap.values()),
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
          () -> tableRuntime.completeProcess(success),
          () -> clearProcess(this));
    }

    private void cancelTasks() {
      taskMap.values().forEach(TaskRuntime::tryCanceling);
    }

    private void loadTaskRuntimes(OptimizingProcess optimizingProcess) {
      try {
        List<TaskRuntime<RewriteStageTask>> taskRuntimes =
            getAs(
                OptimizingProcessMapper.class,
                mapper ->
                    mapper.selectTaskRuntimes(
                        tableRuntime.getTableIdentifier().getId(), processId));
        Map<Integer, RewriteFilesInput> inputs = TaskFilesPersistence.loadTaskInputs(processId);
        taskRuntimes.forEach(
            taskRuntime -> {
              taskRuntime.getCompletedFuture().whenCompleted(() -> acceptResult(taskRuntime));
              taskRuntime
                  .getTaskDescriptor()
                  .setInput(inputs.get(taskRuntime.getTaskId().getTaskId()));
              taskMap.put(taskRuntime.getTaskId(), taskRuntime);
              if (taskRuntime.getStatus() == TaskRuntime.Status.PLANNED) {
                taskQueue.offer(taskRuntime);
              } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
                retryTask(taskRuntime);
              }
            });
      } catch (IllegalArgumentException e) {
        LOG.warn(
            "Load task inputs failed, close the optimizing process : {}",
            optimizingProcess.getProcessId(),
            e);
        optimizingProcess.close(false);
      }
    }

    private void loadTaskRuntimes(List<RewriteStageTask> taskDescriptors) {
      int taskId = 1;
      for (RewriteStageTask taskDescriptor : taskDescriptors) {
        TaskRuntime<RewriteStageTask> taskRuntime =
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

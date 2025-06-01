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
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.exception.OptimizingClosedException;
import org.apache.amoro.exception.PersistenceException;
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
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TaskFilesPersistence;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.DefaultOptimizingState;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.ExceptionUtil;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OptimizingQueue extends PersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingQueue.class);

  private final QuotaProvider quotaProvider;
  private final Queue<TableOptimizingProcess> tableQueue = new LinkedTransferQueue<>();
  private final Queue<TaskRuntime<?>> retryTaskQueue = new LinkedTransferQueue<>();
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
    DefaultOptimizingState optimizingState = tableRuntime.getOptimizingState();
    if (optimizingState.getOptimizingStatus().isProcessing()
        && optimizingState.getProcessId() != 0) {
      tableRuntime.recover(new TableOptimizingProcess(tableRuntime));
    }

    if (optimizingState.isOptimizingEnabled()) {
      optimizingState.resetTaskQuotas(
          System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME);
      // Close the committing process to avoid duplicate commit on the table.
      if (optimizingState.getOptimizingStatus() == OptimizingStatus.COMMITTING) {
        OptimizingProcess process = optimizingState.getOptimizingProcess();
        if (process != null) {
          LOG.warn(
              "Close the committing process {} on table {}",
              process.getProcessId(),
              tableRuntime.getTableIdentifier());
          process.close();
        }
      }
      if (!optimizingState.getOptimizingStatus().isProcessing()) {
        scheduler.addTable(tableRuntime);
      } else {
        tableQueue.offer(new TableOptimizingProcess(tableRuntime));
      }
    } else {
      OptimizingProcess process = optimizingState.getOptimizingProcess();
      if (process != null) {
        process.close();
      }
    }
  }

  public String getContainerName() {
    return optimizerGroup.getContainer();
  }

  public void refreshTable(DefaultTableRuntime tableRuntime) {
    DefaultOptimizingState optimizingState = tableRuntime.getOptimizingState();
    if (optimizingState.isOptimizingEnabled()
        && !optimizingState.getOptimizingStatus().isProcessing()) {
      LOG.info(
          "Bind queue {} success with table {}",
          optimizerGroup.getName(),
          tableRuntime.getTableIdentifier());
      optimizingState.resetTaskQuotas(
          System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME);
      scheduler.addTable(tableRuntime);
    }
  }

  public void releaseTable(DefaultTableRuntime tableRuntime) {
    scheduler.removeTable(tableRuntime);
    List<OptimizingProcess> processList =
        tableQueue.stream()
            .filter(process -> process.optimizingState == tableRuntime.getOptimizingState())
            .collect(Collectors.toList());
    for (OptimizingProcess process : processList) {
      process.close();
      clearProcess(process);
    }
    LOG.info(
        "Release queue {} with table {}",
        optimizerGroup.getName(),
        tableRuntime.getTableIdentifier());
  }

  public boolean containsTable(ServerTableIdentifier identifier) {
    return scheduler.getTableRuntime(identifier) != null;
  }

  private void clearProcess(OptimizingProcess optimizingProcess) {
    tableQueue.removeIf(process -> process.getProcessId() == optimizingProcess.getProcessId());
    retryTaskQueue.removeIf(
        taskRuntime -> taskRuntime.getTaskId().getProcessId() == optimizingProcess.getProcessId());
  }

  public TaskRuntime<?> pollTask(long maxWaitTime) {
    long deadline = calculateDeadline(maxWaitTime);
    TaskRuntime<?> task = fetchTask();
    while (task == null && waitTask(deadline)) {
      task = fetchTask();
    }
    return task;
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

  private TaskRuntime<?> fetchTask() {
    TaskRuntime<?> task = retryTaskQueue.poll();
    return task != null ? task : fetchScheduledTask();
  }

  private TaskRuntime<?> fetchScheduledTask() {
    return tableQueue.stream()
        .map(TableOptimizingProcess::poll)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private void scheduleTableIfNecessary(long startTime) {
    if (planningTables.size() < maxPlanningParallelism) {
      Set<ServerTableIdentifier> skipTables = new HashSet<>(planningTables);
      Optional.ofNullable(scheduler.scheduleTable(skipTables))
          .ifPresent(tableRuntime -> triggerAsyncPlanning(tableRuntime, skipTables, startTime));
    }
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
                tableRuntime.getOptimizingState().setLastPlanTime(currentTime);
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
                      "Completed planning on table {} with {} tasks with a total cost of {} ms, skipping {} tables,"
                          + " id list:{}",
                      tableRuntime.getTableIdentifier(),
                      process.getTaskMap().size(),
                      currentTime - startTime,
                      skipTables.size(),
                      skipIds);
                } else if (throwable == null) {
                  LOG.info(
                      "Skip planning table {} with a total cost of {} ms.",
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
    tableRuntime.getOptimizingState().beginPlanning();
    try {
      ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
      AmoroTable<?> table = catalogManager.loadTable(identifier.getIdentifier());
      AbstractOptimizingPlanner planner =
          IcebergTableUtil.createOptimizingPlanner(
              tableRuntime.getOptimizingState().refresh(table),
              (MixedTable) table.originalTable(),
              getAvailableCore(),
              maxInputSizePerThread());
      if (planner.isNecessary()) {
        return new TableOptimizingProcess(planner, tableRuntime);
      } else {
        tableRuntime.getOptimizingState().completeEmptyProcess();
        return null;
      }
    } catch (Throwable throwable) {
      tableRuntime.getOptimizingState().planFailed();
      LOG.error("Planning table {} failed", tableRuntime.getTableIdentifier(), throwable);
      throw throwable;
    }
  }

  public TaskRuntime<?> getTask(OptimizingTaskId taskId) {
    return tableQueue.stream()
        .filter(p -> p.getProcessId() == taskId.getProcessId())
        .findFirst()
        .map(p -> p.getTaskMap().get(taskId))
        .orElse(null);
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
    taskRuntime.reset();
    retryTaskQueue.offer(taskRuntime);
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
    private final DefaultOptimizingState optimizingState;
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

    public TaskRuntime<?> poll() {
      if (lock.tryLock()) {
        try {
          return status != ProcessStatus.KILLED && status != ProcessStatus.FAILED
              ? taskQueue.poll()
              : null;
        } finally {
          lock.unlock();
        }
      }
      return null;
    }

    public TableOptimizingProcess(
        AbstractOptimizingPlanner planner, DefaultTableRuntime tableRuntime) {
      processId = planner.getProcessId();
      optimizingState = tableRuntime.getOptimizingState();
      optimizingType = planner.getOptimizingType();
      planTime = planner.getPlanTime();
      targetSnapshotId = planner.getTargetSnapshotId();
      targetChangeSnapshotId = planner.getTargetChangeSnapshotId();
      loadTaskRuntimes(planner.planTasks());
      fromSequence = planner.getFromSequence();
      toSequence = planner.getToSequence();
      beginAndPersistProcess();
    }

    public TableOptimizingProcess(DefaultTableRuntime tableRuntime) {
      optimizingState = tableRuntime.getOptimizingState();
      processId = optimizingState.getProcessId();
      optimizingType = optimizingState.getOptimizingType();
      targetSnapshotId = optimizingState.getTargetSnapshotId();
      targetChangeSnapshotId = optimizingState.getTargetChangeSnapshotId();
      planTime = optimizingState.getLastPlanTime();
      if (optimizingState.getFromSequence() != null) {
        fromSequence = optimizingState.getFromSequence();
      }
      if (optimizingState.getToSequence() != null) {
        toSequence = optimizingState.getToSequence();
      }
      if (this.status != ProcessStatus.KILLED) {
        tableRuntime.recover(this);
      }
      loadTaskRuntimes(this);
    }

    @Override
    public long getProcessId() {
      return processId;
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
    public void close() {
      lock.lock();
      try {
        if (this.status != ProcessStatus.RUNNING) {
          return;
        }
        this.status = ProcessStatus.CLOSED;
        this.endTime = System.currentTimeMillis();
        persistAndSetCompleted(false);
      } finally {
        lock.unlock();
      }
    }

    private void acceptResult(TaskRuntime<?> taskRuntime) {
      lock.lock();
      try {
        try {
          optimizingState.addTaskQuota(taskRuntime.getCurrentQuota());
        } catch (Throwable throwable) {
          LOG.warn(
              "{} failed to add task quota {}, ignore it",
              optimizingState.getTableIdentifier(),
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
              && optimizingState.getOptimizingStatus().isProcessing()
              && optimizingState.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
            optimizingState.beginCommitting();
          }
        } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
          if (taskRuntime.getRetry() < optimizingState.getMaxExecuteRetryCount()) {
            LOG.info(
                "Put task {} to retry queue, because {}",
                taskRuntime.getTaskId(),
                taskRuntime.getFailReason());
            retryTask(taskRuntime);
          } else {
            this.failedReason = taskRuntime.getFailReason();
            this.status = ProcessStatus.FAILED;
            this.endTime = taskRuntime.getEndTime();
            persistAndSetCompleted(false);
          }
        }
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

    @Override
    public void commit() {
      LOG.debug(
          "{} get {} tasks of {} partitions to commit",
          optimizingState.getTableIdentifier(),
          taskMap.size(),
          taskMap.values());

      lock.lock();
      try {
        if (hasCommitted) {
          LOG.warn("{} has already committed, give up", optimizingState.getTableIdentifier());
          try {
            persistAndSetCompleted(status == ProcessStatus.SUCCESS);
          } catch (Exception ignored) {
          }
          throw new IllegalStateException("repeat commit, and last error " + failedReason);
        }
        try {
          hasCommitted = true;
          buildCommit().commit();
          status = ProcessStatus.SUCCESS;
          endTime = System.currentTimeMillis();
          persistAndSetCompleted(true);
        } catch (PersistenceException e) {
          LOG.warn(
              "{} failed to persist process completed, will retry next commit",
              optimizingState.getTableIdentifier(),
              e);
        } catch (Throwable t) {
          LOG.error("{} Commit optimizing failed ", optimizingState.getTableIdentifier(), t);
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
                  .loadTable(optimizingState.getTableIdentifier().getIdentifier())
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
                  OptimizingMapper.class,
                  mapper ->
                      mapper.insertOptimizingProcess(
                          optimizingState.getTableIdentifier(),
                          processId,
                          targetSnapshotId,
                          targetChangeSnapshotId,
                          status,
                          optimizingType,
                          planTime,
                          getSummary(),
                          fromSequence,
                          toSequence)),
          () ->
              doAs(
                  OptimizingMapper.class,
                  mapper -> mapper.insertTaskRuntimes(Lists.newArrayList(taskMap.values()))),
          () -> TaskFilesPersistence.persistTaskInputs(processId, taskMap.values()),
          () -> optimizingState.beginProcess(this));
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
                  OptimizingMapper.class,
                  mapper ->
                      mapper.updateOptimizingProcess(
                          optimizingState.getTableIdentifier().getId(),
                          processId,
                          status,
                          endTime,
                          getSummary(),
                          getFailedReason())),
          () -> optimizingState.completeProcess(success),
          () -> clearProcess(this));
    }

    private void cancelTasks() {
      taskMap.values().forEach(TaskRuntime::tryCanceling);
    }

    private void loadTaskRuntimes(OptimizingProcess optimizingProcess) {
      try {
        List<TaskRuntime<RewriteStageTask>> taskRuntimes =
            getAs(
                OptimizingMapper.class,
                mapper ->
                    mapper.selectTaskRuntimes(
                        optimizingState.getTableIdentifier().getId(), processId));
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
        optimizingProcess.close();
      }
    }

    private void loadTaskRuntimes(List<RewriteStageTask> taskDescriptors) {
      int taskId = 1;
      for (RewriteStageTask taskDescriptor : taskDescriptors) {
        TaskRuntime<RewriteStageTask> taskRuntime =
            new TaskRuntime<>(new OptimizingTaskId(processId, taskId++), taskDescriptor);
        LOG.info(
            "{} plan new task {}, summary {}",
            optimizingState.getTableIdentifier(),
            taskRuntime.getTaskId(),
            taskRuntime.getSummary());
        taskRuntime.getCompletedFuture().whenCompleted(() -> acceptResult(taskRuntime));
        taskMap.put(taskRuntime.getTaskId(), taskRuntime);
        taskQueue.offer(taskRuntime);
      }
    }
  }
}

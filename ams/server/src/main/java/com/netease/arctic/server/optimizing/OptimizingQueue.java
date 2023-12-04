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

package com.netease.arctic.server.optimizing;

import com.google.common.collect.Maps;
import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.exception.OptimizingClosedException;
import com.netease.arctic.server.optimizing.plan.OptimizingPlanner;
import com.netease.arctic.server.optimizing.plan.TaskDescriptor;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.TaskFilesPersistence;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.resource.QuotaProvider;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.ArcticDataFiles;
import com.netease.arctic.utils.ExceptionUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
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
  private final Queue<TaskRuntime> retryTaskQueue = new LinkedTransferQueue<>();
  private final SchedulingPolicy scheduler;
  private final TableManager tableManager;
  private final Executor planExecutor;
  // Keep all planning table identifiers
  private final Set<ServerTableIdentifier> planningTables = new HashSet<>();
  private final Lock scheduleLock = new ReentrantLock();
  private final Condition planningCompleted = scheduleLock.newCondition();
  private final int maxPlanningParallelism;
  private ResourceGroup optimizerGroup;

  public OptimizingQueue(
      TableManager tableManager,
      ResourceGroup optimizerGroup,
      QuotaProvider quotaProvider,
      Executor planExecutor,
      List<TableRuntimeMeta> tableRuntimeMetaList,
      int maxPlanningParallelism) {
    Preconditions.checkNotNull(optimizerGroup, "Optimizer group can not be null");
    this.planExecutor = planExecutor;
    this.optimizerGroup = optimizerGroup;
    this.quotaProvider = quotaProvider;
    this.scheduler = new SchedulingPolicy(optimizerGroup);
    this.tableManager = tableManager;
    this.maxPlanningParallelism = maxPlanningParallelism;
    tableRuntimeMetaList.forEach(this::initTableRuntime);
  }

  private void initTableRuntime(TableRuntimeMeta tableRuntimeMeta) {
    TableRuntime tableRuntime = tableRuntimeMeta.getTableRuntime();
    if (tableRuntime.getOptimizingStatus().isProcessing()
        && tableRuntimeMeta.getOptimizingProcessId() != 0) {
      tableRuntime.recover(new TableOptimizingProcess(tableRuntimeMeta));
    }

    if (tableRuntime.isOptimizingEnabled()) {
      tableRuntime.resetTaskQuotas(
          System.currentTimeMillis() - ArcticServiceConstants.QUOTA_LOOK_BACK_TIME);
      if (!tableRuntime.getOptimizingStatus().isProcessing()) {
        scheduler.addTable(tableRuntime);
      } else if (tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
        tableQueue.offer(new TableOptimizingProcess(tableRuntimeMeta));
      }
    } else {
      OptimizingProcess process = tableRuntime.getOptimizingProcess();
      if (process != null) {
        process.close();
      }
    }
  }

  public String getContainerName() {
    return optimizerGroup.getContainer();
  }

  public void refreshTable(TableRuntime tableRuntime) {
    if (tableRuntime.isOptimizingEnabled() && !tableRuntime.getOptimizingStatus().isProcessing()) {
      LOG.info(
          "Bind queue {} success with table {}",
          optimizerGroup.getName(),
          tableRuntime.getTableIdentifier());
      tableRuntime.resetTaskQuotas(
          System.currentTimeMillis() - ArcticServiceConstants.QUOTA_LOOK_BACK_TIME);
      scheduler.addTable(tableRuntime);
    }
  }

  public void releaseTable(TableRuntime tableRuntime) {
    scheduler.removeTable(tableRuntime);
    LOG.info(
        "Release queue {} with table {}",
        optimizerGroup.getName(),
        tableRuntime.getTableIdentifier());
  }

  public boolean containsTable(ServerTableIdentifier identifier) {
    return scheduler.getTableRuntime(identifier) != null;
  }

  private void clearProcess(TableOptimizingProcess optimizingProcess) {
    tableQueue.removeIf(process -> process.getProcessId() == optimizingProcess.getProcessId());
    retryTaskQueue.removeIf(
        taskRuntime -> taskRuntime.getTaskId().getProcessId() == optimizingProcess.getProcessId());
  }

  public TaskRuntime pollTask(long maxWaitTime) {
    long deadline = calculateDeadline(maxWaitTime);
    TaskRuntime task = fetchTask();
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

  private TaskRuntime fetchTask() {
    return Optional.ofNullable(retryTaskQueue.poll()).orElse(fetchScheduledTask());
  }

  private TaskRuntime fetchScheduledTask() {
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
      TableRuntime tableRuntime, Set<ServerTableIdentifier> skipTables, long startTime) {
    LOG.info("Trigger planning table {} by policy {}", tableRuntime.getTableIdentifier(), scheduler.name());
    planningTables.add(tableRuntime.getTableIdentifier());
    CompletableFuture.supplyAsync(() -> planInternal(tableRuntime), planExecutor)
        .whenComplete(
            (process, throwable) -> {
              long currentTime = System.currentTimeMillis();
              scheduleLock.lock();
              try {
                tableRuntime.setLastPlanTime(currentTime);
                planningTables.remove(tableRuntime.getTableIdentifier());
                if (process != null) {
                  tableQueue.offer(process);
                  LOG.info(
                      "Completed planning on table {} with {} tasks with a total cost of {} ms, skipping tables {}",
                      tableRuntime.getTableIdentifier(),
                      process.getTaskMap().size(),
                      currentTime - startTime,
                      skipTables);
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

  private TableOptimizingProcess planInternal(TableRuntime tableRuntime) {
    tableRuntime.beginPlanning();
    try {
      AmoroTable<?> table = tableManager.loadTable(tableRuntime.getTableIdentifier());
      OptimizingPlanner planner =
          new OptimizingPlanner(
              tableRuntime.refresh(table), (ArcticTable) table.originalTable(), getAvailableCore());
      if (planner.isNecessary()) {
        return new TableOptimizingProcess(planner);
      } else {
        tableRuntime.cleanPendingInput();
        return null;
      }
    } catch (Throwable throwable) {
      tableRuntime.planFailed();
      LOG.error("Planning table {} failed", tableRuntime.getTableIdentifier(), throwable);
      throw throwable;
    }
  }

  public TaskRuntime getTask(OptimizingTaskId taskId) {
    return tableQueue.stream()
        .filter(p -> p.getProcessId() == taskId.getProcessId())
        .findFirst()
        .map(p -> p.getTaskMap().get(taskId))
        .orElse(null);
  }

  public List<TaskRuntime> collectTasks() {
    return tableQueue.stream()
        .flatMap(p -> p.getTaskMap().values().stream())
        .collect(Collectors.toList());
  }

  public List<TaskRuntime> collectTasks(Predicate<TaskRuntime> predicate) {
    return tableQueue.stream()
        .flatMap(p -> p.getTaskMap().values().stream())
        .filter(predicate)
        .collect(Collectors.toList());
  }

  public void retryTask(TaskRuntime taskRuntime) {
    taskRuntime.reset();
    retryTaskQueue.offer(taskRuntime);
  }

  public void updateOptimizerGroup(ResourceGroup optimizerGroup) {
    Preconditions.checkArgument(
        this.optimizerGroup.getName().equals(optimizerGroup.getName()),
        "optimizer group name mismatch");
    this.optimizerGroup = optimizerGroup;
    scheduler.setTableSorterIfNeeded(optimizerGroup);
  }

  private double getAvailableCore() {
    // the available core should be at least 1
    return Math.max(quotaProvider.getTotalQuota(optimizerGroup.getName()), 1);
  }

  @VisibleForTesting
  SchedulingPolicy getSchedulingPolicy() {
    return scheduler;
  }

  private class TableOptimizingProcess implements OptimizingProcess, TaskRuntime.TaskOwner {
    private final long processId;
    private final OptimizingType optimizingType;
    private final TableRuntime tableRuntime;
    private final long planTime;
    private final long targetSnapshotId;
    private final long targetChangeSnapshotId;
    private final Map<OptimizingTaskId, TaskRuntime> taskMap = Maps.newHashMap();
    private final Queue<TaskRuntime> taskQueue = new LinkedList<>();
    private final Lock lock = new ReentrantLock();
    private volatile Status status = OptimizingProcess.Status.RUNNING;
    private volatile String failedReason;
    private long endTime = ArcticServiceConstants.INVALID_TIME;
    private Map<String, Long> fromSequence = Maps.newHashMap();
    private Map<String, Long> toSequence = Maps.newHashMap();
    private boolean hasCommitted = false;

    public TaskRuntime poll() {
      lock.lock();
      try {
        return taskQueue.poll();
      } finally {
        lock.unlock();
      }
    }

    public TableOptimizingProcess(OptimizingPlanner planner) {
      processId = planner.getProcessId();
      tableRuntime = planner.getTableRuntime();
      optimizingType = planner.getOptimizingType();
      planTime = planner.getPlanTime();
      targetSnapshotId = planner.getTargetSnapshotId();
      targetChangeSnapshotId = planner.getTargetChangeSnapshotId();
      loadTaskRuntimes(planner.planTasks());
      fromSequence = planner.getFromSequence();
      toSequence = planner.getToSequence();
      beginAndPersistProcess();
    }

    public TableOptimizingProcess(TableRuntimeMeta tableRuntimeMeta) {
      processId = tableRuntimeMeta.getOptimizingProcessId();
      tableRuntime = tableRuntimeMeta.getTableRuntime();
      optimizingType = tableRuntimeMeta.getOptimizingType();
      targetSnapshotId = tableRuntimeMeta.getTargetSnapshotId();
      targetChangeSnapshotId = tableRuntimeMeta.getTargetSnapshotId();
      planTime = tableRuntimeMeta.getPlanTime();
      if (tableRuntimeMeta.getFromSequence() != null) {
        fromSequence = tableRuntimeMeta.getFromSequence();
      }
      if (tableRuntimeMeta.getToSequence() != null) {
        toSequence = tableRuntimeMeta.getToSequence();
      }
      loadTaskRuntimes();
      tableRuntimeMeta.getTableRuntime().recover(this);
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
    public Status getStatus() {
      return status;
    }

    @Override
    public void close() {
      lock.lock();
      try {
        persistProcessCompleted(false);
        clearProcess(this);
        this.status = OptimizingProcess.Status.CLOSED;
        this.endTime = System.currentTimeMillis();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void acceptResult(TaskRuntime taskRuntime) {
      lock.lock();
      try {
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
            clearProcess(this);
          }
        } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
          if (taskRuntime.getRetry() < tableRuntime.getMaxExecuteRetryCount()) {
            retryTask(taskRuntime);
          } else {
            clearProcess(this);
            this.failedReason = taskRuntime.getFailReason();
            this.status = OptimizingProcess.Status.FAILED;
            this.endTime = taskRuntime.getEndTime();
            persistProcessCompleted(false);
            System.out.println("/n/n #### task status {}" + taskRuntime.getStatus());
          }
        }
      } catch (Exception e) {
        LOG.error("accept result error:", e);
      } finally {
        lock.unlock();
      }
    }

    @Override
    public boolean isClosed() {
      return status == OptimizingProcess.Status.CLOSED;
    }

    @Override
    public long getPlanTime() {
      return planTime;
    }

    @Override
    public long getDuration() {
      long dur =
          endTime == ArcticServiceConstants.INVALID_TIME
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

    private Map<OptimizingTaskId, TaskRuntime> getTaskMap() {
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
          tableRuntime.getTableIdentifier(),
          taskMap.size(),
          taskMap.values());

      lock.lock();
      try {
        if (hasCommitted) {
          LOG.warn("{} has already committed, give up", tableRuntime.getTableIdentifier());
          throw new IllegalStateException("repeat commit, and last error " + failedReason);
        }
        hasCommitted = true;
        buildCommit().commit();
        status = Status.SUCCESS;
        endTime = System.currentTimeMillis();
        persistProcessCompleted(true);
      } catch (Exception e) {
        LOG.warn("{} Commit optimizing failed ", tableRuntime.getTableIdentifier(), e);
        status = Status.FAILED;
        failedReason = ExceptionUtil.getErrorMessage(e, 4000);
        endTime = System.currentTimeMillis();
        persistProcessCompleted(false);
      } finally {
        clearProcess(this);
        lock.unlock();
      }
    }

    @Override
    public MetricsSummary getSummary() {
      return new MetricsSummary(taskMap.values());
    }

    private UnKeyedTableCommit buildCommit() {
      ArcticTable table =
          (ArcticTable) tableManager.loadTable(tableRuntime.getTableIdentifier()).originalTable();
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
        ArcticTable table, Map<String, Long> partitionSequence) {
      PartitionSpec spec = table.spec();
      StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
      partitionSequence.forEach(
          (partition, sequence) -> {
            if (spec.isUnpartitioned()) {
              results.put(TablePropertyUtil.EMPTY_STRUCT, sequence);
            } else {
              StructLike partitionData = ArcticDataFiles.data(spec, partition);
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
                          tableRuntime.getTableIdentifier(),
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
          () -> tableRuntime.beginProcess(this));
    }

    private void persistProcessCompleted(boolean success) {
      if (!success) {
        doAsTransaction(
            () -> taskMap.values().forEach(TaskRuntime::tryCanceling),
            () ->
                doAs(
                    OptimizingMapper.class,
                    mapper ->
                        mapper.updateOptimizingProcess(
                            tableRuntime.getTableIdentifier().getId(),
                            processId,
                            status,
                            endTime,
                            getSummary(),
                            getFailedReason())),
            () -> tableRuntime.completeProcess(false));
      } else {
        doAsTransaction(
            () ->
                doAs(
                    OptimizingMapper.class,
                    mapper ->
                        mapper.updateOptimizingProcess(
                            tableRuntime.getTableIdentifier().getId(),
                            processId,
                            status,
                            endTime,
                            getSummary(),
                            getFailedReason())),
            () -> tableRuntime.completeProcess(true));
      }
    }

    private void loadTaskRuntimes() {
      List<TaskRuntime> taskRuntimes =
          getAs(
              OptimizingMapper.class,
              mapper ->
                  mapper.selectTaskRuntimes(tableRuntime.getTableIdentifier().getId(), processId));
      Map<Integer, RewriteFilesInput> inputs = TaskFilesPersistence.loadTaskInputs(processId);
      taskRuntimes.forEach(
          taskRuntime -> {
            taskRuntime.claimOwnership(this);
            taskRuntime.setInput(inputs.get(taskRuntime.getTaskId().getTaskId()));
            taskMap.put(taskRuntime.getTaskId(), taskRuntime);
            if (taskRuntime.getStatus() == TaskRuntime.Status.PLANNED) {
              taskQueue.offer(taskRuntime);
            } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
              retryTask(taskRuntime);
            }
          });
    }

    private void loadTaskRuntimes(List<TaskDescriptor> taskDescriptors) {
      int taskId = 1;
      for (TaskDescriptor taskDescriptor : taskDescriptors) {
        TaskRuntime taskRuntime =
            new TaskRuntime(
                new OptimizingTaskId(processId, taskId++),
                taskDescriptor,
                taskDescriptor.properties());
        LOG.info(
            "{} plan new task {}, summary {}",
            tableRuntime.getTableIdentifier(),
            taskRuntime.getTaskId(),
            taskRuntime.getSummary());
        taskMap.put(taskRuntime.getTaskId(), taskRuntime.claimOwnership(this));
        taskQueue.offer(taskRuntime);
      }
    }
  }
}

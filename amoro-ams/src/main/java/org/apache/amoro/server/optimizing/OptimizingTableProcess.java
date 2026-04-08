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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.exception.OptimizingClosedException;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.exception.TaskNotFoundException;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.amoro.optimizing.plan.AbstractOptimizingPlanner;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.optimizing.TaskRuntime.Status;
import org.apache.amoro.server.persistence.OptimizingProcessState;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TaskFilesPersistence;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.ExceptionUtil;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Standalone class for managing the lifecycle of an optimizing process for a table. Extracted from
 * OptimizingQueue.TableOptimizingProcess inner class.
 */
public class OptimizingTableProcess extends PersistentBase implements OptimizingProcess {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingTableProcess.class);

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

  // Injected dependencies (previously accessed from enclosing OptimizingQueue)
  private final CatalogManager catalogManager;
  private final QuotaProvider quotaProvider;
  private final Map<ServerTableIdentifier, AtomicInteger> optimizingTasksMap;
  private final Runnable onProcessCompleted;
  private IcebergOptimizingProcessFactory processFactory;

  /** Constructor for creating a new process from a planner result. */
  public OptimizingTableProcess(
      AbstractOptimizingPlanner planner,
      DefaultTableRuntime tableRuntime,
      CatalogManager catalogManager,
      QuotaProvider quotaProvider,
      Map<ServerTableIdentifier, AtomicInteger> optimizingTasksMap,
      Runnable onProcessCompleted) {
    this.tableRuntime = tableRuntime;
    this.catalogManager = catalogManager;
    this.quotaProvider = quotaProvider;
    this.optimizingTasksMap = optimizingTasksMap;
    this.onProcessCompleted = onProcessCompleted;
    this.processId = planner.getProcessId();
    this.optimizingType = planner.getOptimizingType();
    this.planTime = planner.getPlanTime();
    this.targetSnapshotId = planner.getTargetSnapshotId();
    this.targetChangeSnapshotId = planner.getTargetChangeSnapshotId();
    loadTaskRuntimes(planner.planTasks());
    this.fromSequence = planner.getFromSequence();
    this.toSequence = planner.getToSequence();
    beginAndPersistProcess();
  }

  /** Constructor for recovering a process from persisted state. */
  public OptimizingTableProcess(
      DefaultTableRuntime tableRuntime,
      TableProcessMeta processMeta,
      OptimizingProcessState processState,
      CatalogManager catalogManager,
      QuotaProvider quotaProvider,
      Map<ServerTableIdentifier, AtomicInteger> optimizingTasksMap,
      Runnable onProcessCompleted) {
    this.tableRuntime = tableRuntime;
    this.catalogManager = catalogManager;
    this.quotaProvider = quotaProvider;
    this.optimizingTasksMap = optimizingTasksMap;
    this.onProcessCompleted = onProcessCompleted;
    this.processId = tableRuntime.getProcessId();
    this.optimizingType = OptimizingType.valueOf(processMeta.getProcessType());
    this.targetSnapshotId = processState.getTargetSnapshotId();
    this.targetChangeSnapshotId = processState.getTargetChangeSnapshotId();
    this.planTime = processMeta.getCreateTime();
    if (processState.getFromSequence() != null) {
      this.fromSequence = processState.getFromSequence();
    }
    if (processState.getToSequence() != null) {
      this.toSequence = processState.getToSequence();
    }
    this.status = processMeta.getStatus();
    tableRuntime.recover(this);
    loadTaskRuntimes(this);
  }

  public TaskRuntime<?> poll(OptimizerThread thread, boolean needQuotaChecking) {
    try {
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

  public void ackTask(OptimizingTaskId taskId, OptimizerThread thread) {
    TaskRuntime<?> taskRuntime = getTaskRuntime(taskId);
    lock.lock();
    try {
      taskRuntime.ack(thread);
    } finally {
      lock.unlock();
    }
  }

  public void completeTask(OptimizerThread thread, OptimizingTaskResult result) {
    TaskRuntime<?> taskRuntime = getTaskRuntime(result.getTaskId());
    lock.lock();
    try {
      taskRuntime.complete(thread, result);
    } finally {
      lock.unlock();
    }
  }

  public void resetTask(TaskRuntime<RewriteStageTask> taskRuntime) {
    lock.lock();
    try {
      taskRuntime.reset();
      taskQueue.add(taskRuntime);
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
      if (taskRuntime.getStatus() == TaskRuntime.Status.CANCELED) {
        return;
      }
      if (isClosed()) {
        throw new OptimizingClosedException(processId);
      }
      if (taskRuntime.getStatus() == TaskRuntime.Status.SUCCESS) {
        if (allTasksPrepared()
            && tableRuntime.getOptimizingStatus().isProcessing()
            && tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
          tableRuntime.beginCommitting();
        }
      } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
        if (taskRuntime.getRetry() < tableRuntime.getOptimizingConfig().getMaxExecuteRetryCount()) {
          LOG.info(
              "Put task {} to retry queue, because {}",
              taskRuntime.getTaskId(),
              taskRuntime.getFailReason());
          resetTask((TaskRuntime<RewriteStageTask>) taskRuntime);
        } else {
          if (tableRuntime.isAllowPartialCommit()
              && tableRuntime.getOptimizingStatus().isProcessing()
              && tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
            LOG.info(
                "Task {} has reached the max execute retry count. Process {} cancels "
                    + "unfinished tasks and commits SUCCESS tasks.",
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

  private int getQuotaLimit() {
    double targetQuota = tableRuntime.getOptimizingConfig().getTargetQuota();
    return targetQuota > 1 ? (int) targetQuota : (int) Math.ceil(targetQuota * getAvailableCore());
  }

  private double getAvailableCore() {
    return Math.max(quotaProvider.getTotalQuota(tableRuntime.getGroupName()), 1);
  }

  // ===== OptimizingProcess interface implementation =====

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

  public void setProcessFactory(IcebergOptimizingProcessFactory processFactory) {
    this.processFactory = processFactory;
  }

  public Map<OptimizingTaskId, TaskRuntime<RewriteStageTask>> getTaskMap() {
    return taskMap;
  }

  public boolean allTasksPrepared() {
    if (!taskMap.isEmpty()) {
      return taskMap.values().stream().allMatch(t -> t.getStatus() == TaskRuntime.Status.SUCCESS);
    }
    return false;
  }

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

  // ===== Private methods =====

  private TableOptimizingCommitter buildCommit() {
    MixedTable table =
        (MixedTable)
            catalogManager
                .loadTable(tableRuntime.getTableIdentifier().getIdentifier())
                .originalTable();
    // Use factory callback for commit if available, otherwise fallback to direct creation
    if (processFactory != null) {
      return processFactory.createCommitter(
          table, targetSnapshotId, taskMap.values(), fromSequence, toSequence);
    }
    // Fallback: direct creation (for backwards compatibility during migration)
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
        () -> {
          if (onProcessCompleted != null) {
            onProcessCompleted.run();
          }
        });
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
                  mapper.selectTaskRuntimes(tableRuntime.getTableIdentifier().getId(), processId));
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
            } else if (taskRuntime.getStatus() == TaskRuntime.Status.SCHEDULED
                || taskRuntime.getStatus() == TaskRuntime.Status.ACKED) {
              LOG.debug(
                  "Keeping task {} in {} during recovery, waiting for optimizer to complete",
                  taskRuntime.getTaskId(),
                  taskRuntime.getStatus());
            } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
              resetTask(taskRuntime);
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

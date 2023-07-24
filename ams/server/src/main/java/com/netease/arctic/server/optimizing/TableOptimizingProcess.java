package com.netease.arctic.server.optimizing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.exception.OptimizingClosedException;
import com.netease.arctic.server.optimizing.plan.TaskDescriptor;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.TaskFilesPersistence;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.ArcticDataFiles;
import com.netease.arctic.utils.ExceptionUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TableOptimizingProcess extends PersistentBase implements OptimizingProcess, TaskRuntime.TaskOwner {

  private static final Logger LOG = LoggerFactory.getLogger(TableOptimizingProcess.class);

  private final long processId;
  private final OptimizingType optimizingType;
  private final TableRuntime tableRuntime;
  private final long planTime;
  private final long targetSnapshotId;
  private final long targetChangeSnapshotId;
  private final Map<OptimizingTaskId, TaskRuntime> taskMap = Maps.newConcurrentMap();
  private final Lock lock = new ReentrantLock();
  private volatile Status status = OptimizingProcess.Status.RUNNING;
  private volatile String failedReason;
  private long endTime = ArcticServiceConstants.INVALID_TIME;

  private Map<String, Long> fromSequence = Maps.newHashMap();
  private Map<String, Long> toSequence = Maps.newHashMap();

  private boolean hasCommitted = false;

  private Consumer<OptimizingProcess> clearTask;
  private BiConsumer<TaskRuntime, Boolean> retryTask;

  public TableOptimizingProcess(
      long processId,
      OptimizingType optimizingType,
      TableRuntime tableRuntime,
      long planTime,
      long targetSnapshotId,
      long targetChangeSnapshotId,
      List<TaskDescriptor> tasks,
      Map<String, Long> fromSequence,
      Map<String, Long> toSequence) {
    this.processId = processId;
    this.tableRuntime = tableRuntime;
    this.optimizingType = optimizingType;
    this.planTime = planTime;
    this.targetSnapshotId = targetSnapshotId;
    this.targetChangeSnapshotId = targetChangeSnapshotId;
    loadTaskRuntimes(tasks);
    this.fromSequence = fromSequence;
    this.toSequence = toSequence;
    persistProcess();
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
      clearTask.accept(this);
      this.status = OptimizingProcess.Status.CLOSED;
      this.endTime = System.currentTimeMillis();
      persistProcessCompleted(false);
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
      } catch (Throwable t) {
        LOG.warn("{} failed to add task quota {}, ignore it", tableRuntime.getTableIdentifier(),
            taskRuntime.getTaskId(), t);
      }
      if (isClosed()) {
        throw new OptimizingClosedException(processId);
      }
      if (taskRuntime.getStatus() == TaskRuntime.Status.SUCCESS) {
        // the lock of TableOptimizingProcess makes it thread-safe
        if (allTasksPrepared() && tableRuntime.getOptimizingStatus().isProcessing() &&
            tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
          tableRuntime.beginCommitting();
        }
      } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
        if (taskRuntime.getRetry() <= tableRuntime.getMaxExecuteRetryCount()) {
          retryTask.accept(taskRuntime, true);
        } else {
          clearTask.accept(this);
          this.failedReason = taskRuntime.getFailReason();
          this.status = OptimizingProcess.Status.FAILED;
          this.endTime = taskRuntime.getEndTime();
          persistProcessCompleted(false);
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
    long dur = endTime == ArcticServiceConstants.INVALID_TIME ?
        System.currentTimeMillis() - planTime :
        endTime - planTime;
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

  public Map<OptimizingTaskId, TaskRuntime> getTaskMap() {
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
    return taskMap.values()
        .stream()
        .filter(t -> !t.finished())
        .mapToLong(task -> task.getQuotaTime(calculatingStartTime, calculatingEndTime))
        .sum();
  }

  @Override
  public void commit(ArcticTable table) {
    LOG.debug("{} get {} tasks of {} partitions to commit", tableRuntime.getTableIdentifier(),
        taskMap.size(), taskMap.values());

    lock.lock();
    try {
      if (hasCommitted) {
        LOG.warn("{} process {} has already committed, give up", tableRuntime.getTableIdentifier(), processId);
        throw new IllegalStateException("repeat commit, and last error " + failedReason);
      }
      hasCommitted = true;
      buildCommit(table).commit();
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
      lock.unlock();
    }
  }

  @Override
  public MetricsSummary getSummary() {
    return new MetricsSummary(taskMap.values());
  }

  private UnKeyedTableCommit buildCommit(ArcticTable table) {
    if (table.isUnkeyedTable()) {
      return new UnKeyedTableCommit(targetSnapshotId, table, taskMap.values(), processId);
    } else {
      return new KeyedTableCommit(table, taskMap.values(), targetSnapshotId, processId,
          convertPartitionSequence(table, fromSequence), convertPartitionSequence(table, toSequence));
    }
  }

  private StructLikeMap<Long> convertPartitionSequence(ArcticTable table, Map<String, Long> partitionSequence) {
    PartitionSpec spec = table.spec();
    StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
    partitionSequence.forEach((partition, sequence) -> {
      if (spec.isUnpartitioned()) {
        results.put(TablePropertyUtil.EMPTY_STRUCT, sequence);
      } else {
        StructLike partitionData = ArcticDataFiles.data(spec, partition);
        results.put(partitionData, sequence);
      }
    });
    return results;
  }

  private void persistProcess() {
    doAsTransaction(
        () -> doAs(OptimizingMapper.class, mapper ->
            mapper.insertOptimizingProcess(tableRuntime.getTableIdentifier(),
                processId, targetSnapshotId, targetChangeSnapshotId, status, optimizingType, planTime, getSummary(),
                fromSequence, toSequence)),
        () -> doAs(OptimizingMapper.class, mapper ->
            mapper.insertTaskRuntimes(Lists.newArrayList(taskMap.values()))),
        () -> TaskFilesPersistence.persistTaskInputs(processId, taskMap.values())
    );
  }

  private void persistProcessCompleted(boolean success) {
    if (!success) {
      doAsTransaction(
          () -> taskMap.values().forEach(TaskRuntime::tryCanceling),
          () -> doAs(OptimizingMapper.class, mapper ->
              mapper.updateOptimizingProcess(tableRuntime.getTableIdentifier().getId(), processId, status, endTime,
                  getSummary(), getFailedReason())),
          () -> tableRuntime.completeProcess(false)
      );
    } else {
      doAsTransaction(
          () -> doAs(OptimizingMapper.class, mapper ->
              mapper.updateOptimizingProcess(tableRuntime.getTableIdentifier().getId(), processId, status, endTime,
                  getSummary(), getFailedReason())),
          () -> tableRuntime.completeProcess(true)
      );
    }
  }

  private void loadTaskRuntimes() {
    List<TaskRuntime> taskRuntimes = getAs(
        OptimizingMapper.class,
        mapper -> mapper.selectTaskRuntimes(tableRuntime.getTableIdentifier().getId(), processId));
    Map<Integer, RewriteFilesInput> inputs = TaskFilesPersistence.loadTaskInputs(processId);
    taskRuntimes.forEach(taskRuntime -> {
      taskRuntime.claimOwnership(this);
      taskRuntime.setInput(inputs.get(taskRuntime.getTaskId().getTaskId()));
      taskMap.put(taskRuntime.getTaskId(), taskRuntime);
    });
  }

  private void loadTaskRuntimes(List<TaskDescriptor> taskDescriptors) {
    int taskId = 1;
    for (TaskDescriptor taskDescriptor : taskDescriptors) {
      TaskRuntime taskRuntime = new TaskRuntime(new OptimizingTaskId(processId, taskId++),
          taskDescriptor, taskDescriptor.properties());
      LOG.debug("{} plan new task {}, summary {}", tableRuntime.getTableIdentifier(), taskRuntime.getTaskId(),
          taskRuntime.getSummary());
      taskMap.put(taskRuntime.getTaskId(), taskRuntime.claimOwnership(this));
    }
  }

  public TableOptimizingProcess handleTaskClear(Consumer<OptimizingProcess> clearTask) {
    this.clearTask = clearTask;
    return this;
  }

  public TableOptimizingProcess handleTaskRetry(BiConsumer<TaskRuntime, Boolean> retryTask) {
    this.retryTask = retryTask;
    return this;
  }
}

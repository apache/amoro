package com.netease.arctic.server.optimizing;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.OptimizingService;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.exception.OptimizingClosedException;
import com.netease.arctic.server.exception.OptimizingCommitException;
import com.netease.arctic.server.exception.PluginRetryAuthException;
import com.netease.arctic.server.exception.TaskNotFoundException;
import com.netease.arctic.server.optimizing.plan.OptimizingPlanner;
import com.netease.arctic.server.optimizing.plan.TaskDescriptor;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.TaskFilesPersistence;
import com.netease.arctic.server.persistence.mapper.OptimizerMapper;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.resource.OptimizerInstance;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class OptimizingQueue extends PersistentBase implements OptimizingService.Iface {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingQueue.class);
  private final Lock planLock = new ReentrantLock();
  private final ResourceGroup optimizerGroup;
  private final Queue<TaskRuntime> taskQueue = new LinkedTransferQueue<>();
  private final Queue<TaskRuntime> retryQueue = new LinkedTransferQueue<>();
  private final SchedulingPolicy schedulingPolicy = new SchedulingPolicy();
  private final Map<OptimizingTaskId, TaskRuntime> taskMap = new ConcurrentHashMap<>();
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();

  public OptimizingQueue(ResourceGroup optimizerGroup, List<TableRuntimeMeta> tableRuntimeMetaList) {
    if (optimizerGroup == null) {
      throw new IllegalStateException("optimizer can not be null");
    }
    this.optimizerGroup = optimizerGroup;
    tableRuntimeMetaList.forEach(this::initTableRuntime);
  }

  private void initTableRuntime(TableRuntimeMeta tableRuntimeMeta) {
    TableRuntime tableRuntime = tableRuntimeMeta.getTableRuntime();
    if (tableRuntime.isOptimizingEnabled()) {
      //TODO: load task quotas
      tableRuntime.resetTaskQuotas(System.currentTimeMillis() - ArcticServiceConstants.QUOTA_LOOK_BACK_TIME);
      if (tableRuntime.getOptimizingStatus() == OptimizingStatus.IDLE ||
          tableRuntime.getOptimizingStatus() == OptimizingStatus.PENDING) {
        schedulingPolicy.addTable(tableRuntime);
      } else if (tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
        TableOptimizingProcess process = new TableOptimizingProcess(tableRuntimeMeta);
        process.getTaskMap().values().stream()
            .filter(task -> task.getStatus() == TaskRuntime.Status.PLANNED)
            .forEach(taskQueue::offer);
      }
    } else if (tableRuntime.getOptimizingStatus().isProcessing() &&
        tableRuntimeMeta.getOptimizingProcessId() != 0) {
      TableOptimizingProcess process = new TableOptimizingProcess(tableRuntimeMeta);
      process.close();
    }
  }

  public void refreshTable(TableRuntime tableRuntime) {
    if (tableRuntime.isOptimizingEnabled() && !tableRuntime.getOptimizingStatus().isProcessing()) {
      LOG.info("Bind queue {} success with table {}", optimizerGroup.getName(), tableRuntime.getTableIdentifier());
      //TODO: load task quotas
      tableRuntime.resetTaskQuotas(System.currentTimeMillis() - ArcticServiceConstants.QUOTA_LOOK_BACK_TIME);
      schedulingPolicy.addTable(tableRuntime);
    }
  }

  public void releaseTable(TableRuntime tableRuntime) {
    schedulingPolicy.removeTable(tableRuntime);
    LOG.info("Release queue {} with table {}", optimizerGroup.getName(), tableRuntime.getTableIdentifier());
  }

  public List<OptimizerInstance> getOptimizers() {
    return ImmutableList.copyOf(authOptimizers.values());
  }

  private void clearTasks(TableOptimizingProcess optimizingProcess) {
    retryQueue.removeIf(taskRuntime -> taskRuntime.getProcessId() == optimizingProcess.getProcessId());
    taskQueue.removeIf(taskRuntime -> taskRuntime.getProcessId() == optimizingProcess.getProcessId());
  }

  @Override
  public void ping() {
  }

  @Override
  public void touch(String authToken) {
    OptimizerInstance optimizer = getAuthenticatedOptimizer(authToken).touch();
    if (LOG.isDebugEnabled()) {
      LOG.debug("Optimizer {} touch time: {}", optimizer.getToken(), optimizer.getTouchTime());
    }
    doAs(OptimizerMapper.class, mapper -> mapper.updateTouchTime(optimizer.getToken()));
  }

  private OptimizerInstance getAuthenticatedOptimizer(String authToken) {
    Preconditions.checkArgument(authToken != null, "authToken can not be null");
    return Optional.ofNullable(authOptimizers.get(authToken))
        .orElseThrow(() -> new PluginRetryAuthException("Optimizer has not been authenticated"));
  }

  @Override
  public OptimizingTask pollTask(String authToken, int threadId) {
    TaskRuntime task = Optional.ofNullable(retryQueue.poll())
        .orElseGet(this::pollOrPlan);

    if (task != null) {
      safelySchedule(task, new OptimizingThread(authToken, threadId));
      taskMap.putIfAbsent(task.getTaskId(), task);
    }
    return task != null ? task.getOptimizingTask() : null;
  }

  private void safelySchedule(TaskRuntime task, OptimizingThread thread) {
    try {
      task.schedule(thread);
    } catch (Throwable throwable) {
      retryTask(task);
      throw throwable;
    }
  }

  private void retryTask(TaskRuntime taskRuntime) {
    taskRuntime.addRetryCount();
    taskRuntime.reset();
    retryQueue.offer(taskRuntime);
  }

  @Override
  public void ackTask(String authToken, int threadId, OptimizingTaskId taskId) {
    Optional.ofNullable(taskMap.get(taskId))
        .orElseThrow(() -> new TaskNotFoundException(taskId))
        .ack(new OptimizingThread(authToken, threadId));
  }

  @Override
  public void completeTask(String authToken, OptimizingTaskResult taskResult) {
    OptimizingThread thread = new OptimizingThread(authToken, taskResult.getThreadId());
    Optional.ofNullable(taskMap.remove(taskResult.getTaskId()))
        .orElseThrow(() -> new TaskNotFoundException(taskResult.getTaskId()))
        .complete(thread, taskResult);
  }

  @Override
  public String authenticate(OptimizerRegisterInfo registerInfo) {
    OptimizerInstance optimizer = new OptimizerInstance(registerInfo, optimizerGroup.getContainer());
    if (LOG.isDebugEnabled()) {
      LOG.debug("Register optimizer: " + optimizer);
    }
    doAs(OptimizerMapper.class, mapper -> mapper.insertOptimizer(optimizer));
    authOptimizers.put(optimizer.getToken(), optimizer);
    return optimizer.getToken();
  }

  public void checkSuspending() {
    long currentTime = System.currentTimeMillis();
    List<String> expiredOptimizers = authOptimizers.values().stream()
        .filter(optimizer -> currentTime - optimizer.getTouchTime() >
            ArcticServiceConstants.OPTIMIZER_TOUCH_TIMEOUT)
        .map(OptimizerInstance::getToken)
        .collect(Collectors.toList());

    expiredOptimizers.forEach(optimizerToken ->
        doAs(OptimizerMapper.class, mapper -> mapper.deleteOptimizer(optimizerToken)));
    expiredOptimizers.forEach(authOptimizers.keySet()::remove);

    List<TaskRuntime> suspendingTasks = taskMap.values().stream()
        .filter(task -> task.isSuspending(currentTime) ||
            expiredOptimizers.contains(task.getOptimizingThread().getToken()))
        .collect(Collectors.toList());
    suspendingTasks.forEach(task -> {
      taskMap.remove(task.getTaskId());
      retryTask(task);
    });
  }

  private TaskRuntime pollOrPlan() {
    planLock.lock();
    try {
      if (taskQueue.isEmpty()) {
        planTasks();
      }
      return taskQueue.poll();
    } finally {
      planLock.unlock();
    }
  }

  private void planTasks() {
    List<TableRuntime> scheduledTables = schedulingPolicy.scheduleTables();
    if (LOG.isDebugEnabled()) {
      LOG.debug("Calculating and sorting tables by quota:" + scheduledTables);
    }

    for (TableRuntime tableRuntime : scheduledTables) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Planning table " + tableRuntime.getTableIdentifier());
      }
      try {
        OptimizingPlanner planner = new OptimizingPlanner(tableRuntime, getAvailableCore(tableRuntime));
        if (planner.isNecessary()) {
          TableOptimizingProcess optimizingProcess = new TableOptimizingProcess(planner);
          if (LOG.isDebugEnabled()) {
            LOG.debug("{} after plan get {} tasks", tableRuntime.getTableIdentifier(),
                optimizingProcess.getTaskMap().size());
          }
          optimizingProcess.taskMap.values().forEach(taskQueue::offer);
        }
      } catch (Throwable e) {
        LOG.error(tableRuntime.getTableIdentifier() + " plan failed, continue", e);
      }
    }
  }

  private double getAvailableCore(TableRuntime tableRuntime) {
    return tableRuntime.getOptimizingConfig().getTargetQuota();
  }

  private class TableOptimizingProcess implements OptimizingProcess, TaskRuntime.TaskOwner {
    private final long processId;
    private final OptimizingType optimizingType;
    private final TableRuntime tableRuntime;
    private final long planTime;
    private final long targetSnapshotId;
    private final Map<OptimizingTaskId, TaskRuntime> taskMap = Maps.newHashMap();
    private final MetricsSummary metricsSummary;
    private final Lock lock = new ReentrantLock();
    private volatile Status status = OptimizingProcess.Status.RUNNING;
    private volatile String failedReason;
    private long endTime = ArcticServiceConstants.INVALID_TIME;
    private int retryCommitCount = 0;

    // TODO persist
    private Map<String, Long> fromSequence = Maps.newHashMap();
    private Map<String, Long> toSequence = Maps.newHashMap();

    public TableOptimizingProcess(OptimizingPlanner planner) {
      processId = planner.getProcessId();
      tableRuntime = planner.getTableRuntime();
      optimizingType = planner.getOptimizingType();
      planTime = planner.getPlanTime();
      targetSnapshotId = planner.getTargetSnapshotId();
      metricsSummary = new MetricsSummary(taskMap.values());
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
      planTime = tableRuntimeMeta.getPlanTime();
      metricsSummary = new MetricsSummary(taskMap.values());
      loadTaskRuntimes();
      tableRuntimeMeta.constructTableRuntime(this);
    }

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
        clearTasks(this);
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
        if (isClosed()) {
          throw new OptimizingClosedException(processId);
        }
        if (taskRuntime.getStatus() == TaskRuntime.Status.SUCCESS && allTasksPrepared()) {
          tableRuntime.beginCommitting();
        } else if (taskRuntime.getStatus() == TaskRuntime.Status.FAILED) {
          if (taskRuntime.getRetry() <= tableRuntime.getMaxExecuteRetryCount()) {
            retryTask(taskRuntime);
          } else {
            clearTasks(this);
            this.failedReason = taskRuntime.getFailReason();
            this.status = OptimizingProcess.Status.FAILED;
            this.endTime = System.currentTimeMillis();
            persistProcessCompleted(false);
          }
        }
      } finally {
        tableRuntime.addTaskQuota(taskRuntime.getCurrentQuota());
        lock.unlock();
      }
    }

    public boolean isClosed() {
      return status == OptimizingProcess.Status.CLOSED;
    }

    public long getPlanTime() {
      return planTime;
    }

    public long getDuration() {
      long dur = endTime == ArcticServiceConstants.INVALID_TIME ?
          System.currentTimeMillis() - planTime :
          endTime - planTime;
      return Math.max(0, dur);
    }

    public long getTargetSnapshotId() {
      return targetSnapshotId;
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
    public long getQuotaTime(long calculatingStartTime, long calculatingEndTime) {
      return taskMap.values().stream()
          .mapToLong(task -> task.getQuotaTime(calculatingStartTime, calculatingEndTime)).sum();
    }

    @Override
    public void commit() {
      if (LOG.isDebugEnabled()) {
        LOG.debug("{} get {} tasks of {} partitions to commit", tableRuntime.getTableIdentifier(),
            taskMap.size(), taskMap.values());
      }
      try {
        buildCommit().commit();
        status = Status.SUCCESS;
        endTime = System.currentTimeMillis();
        persistProcessCompleted(true);
      } catch (OptimizingCommitException e) {
        LOG.warn("Commit optimizing failed. inner message is " + e.getMessage(), e);
        if (!e.isRetryable() || ++retryCommitCount <= tableRuntime.getMaxCommitRetryCount()) {
          status = Status.FAILED;
          failedReason = e.getMessage();
          endTime = System.currentTimeMillis();
          persistProcessCompleted(false);
        }
      }
    }

    @Override
    public MetricsSummary getSummary() {
      return metricsSummary;
    }

    @Override
    public Map<String, Long> getFromSequence() {
      return fromSequence;
    }

    @Override
    public Map<String, Long> getToSequence() {
      return toSequence;
    }

    private IcebergCommit buildCommit() {
      ArcticTable table = tableRuntime.loadTable();
      switch (table.format()) {
        case ICEBERG:
          return new IcebergCommit(targetSnapshotId, table, taskMap.values());
        case MIXED_ICEBERG:
        case MIXED_HIVE:
          //todo Add args
          return new MixedIcebergCommit(table, taskMap.values(), targetSnapshotId, null, null);
        default:
          throw new IllegalStateException();
      }
    }

    private void beginAndPersistProcess() {
      doAsTransaction(
          () -> doAs(OptimizingMapper.class, mapper ->
              mapper.insertOptimizingProcess(tableRuntime.getTableIdentifier(),
                  processId, targetSnapshotId, status, optimizingType, planTime, getSummary())),
          () -> doAs(OptimizingMapper.class, mapper ->
              mapper.insertTaskRuntimes(Lists.newArrayList(taskMap.values()))),
          () -> TaskFilesPersistence.persistTaskInputs(tableRuntime, processId, taskMap.values()),
          () -> tableRuntime.beginProcess(this)
      );
    }

    private void persistProcessCompleted(boolean success) {
      if (!success) {
        doAsTransaction(
            () -> taskMap.values().forEach(TaskRuntime::tryCanceling),
            () -> doAs(OptimizingMapper.class, mapper ->
                mapper.updateOptimizingProcess(tableRuntime.getTableIdentifier().getId(), processId, status, endTime)),
            () -> tableRuntime.completeProcess(true)
        );
      } else {
        doAsTransaction(
            () -> doAs(OptimizingMapper.class, mapper ->
                mapper.updateOptimizingProcess(tableRuntime.getTableIdentifier().getId(), processId, status, endTime)),
            () -> tableRuntime.completeProcess(true)
        );
      }
    }

    private void loadTaskRuntimes() {
      List<TaskRuntime> taskRuntimes = getAs(
          OptimizingMapper.class,
          mapper -> mapper.selectTaskRuntimes(tableRuntime.getTableIdentifier().getId(), processId));
      RewriteFilesInput inputs = TaskFilesPersistence.loadTaskInputs(processId);
      taskRuntimes.forEach(taskRuntime -> {
        taskRuntime.claimOwership(this);
        taskRuntime.setInput(inputs);
        taskMap.put(taskRuntime.getTaskId(), taskRuntime);
      });
    }

    private void loadTaskRuntimes(List<TaskDescriptor> taskDescriptors) {
      int taskId = 1;
      for (TaskDescriptor taskDescriptor : taskDescriptors) {
        OptimizingTaskId id = new OptimizingTaskId(processId, taskId++);

        TaskRuntime taskRuntime = new TaskRuntime(id, taskDescriptor,
            tableRuntime.getTableIdentifier().getId(), taskDescriptor.properties());
        taskMap.put(id, taskRuntime.claimOwership(this));
      }
    }
  }

  public static class OptimizingThread {

    private final String token;
    private final int threadId;

    public OptimizingThread(String token, int threadId) {
      this.token = token;
      this.threadId = threadId;
    }

    public String getToken() {
      return token;
    }

    public int getThreadId() {
      return threadId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      OptimizingThread that = (OptimizingThread) o;
      return threadId == that.threadId && Objects.equal(token, that.token);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(token, threadId);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("token", token)
          .add("threadId", threadId)
          .toString();
    }
  }
}

package com.netease.arctic.server.optimizing;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.OptimizingService;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.exception.PluginRetryAuthException;
import com.netease.arctic.server.exception.TaskNotFoundException;
import com.netease.arctic.server.optimizing.plan.OptimizingPlanner;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizerMapper;
import com.netease.arctic.server.resource.OptimizerInstance;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
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
  private final long optimizerTouchTimeout;
  private final long taskAckTimeout;
  private final Lock planLock = new ReentrantLock();
  private ResourceGroup optimizerGroup;
  private final Queue<TaskRuntime> taskQueue = new LinkedTransferQueue<>();
  private final Queue<TaskRuntime> retryQueue = new LinkedTransferQueue<>();
  private final SchedulingPolicy schedulingPolicy;
  // keeps the SCHEDULED and ACKED tasks
  private final Map<OptimizingTaskId, TaskRuntime> executingTaskMap = new ConcurrentHashMap<>();
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();

  private final TableManager tableManager;

  public OptimizingQueue(
      TableManager tableManager,
      ResourceGroup optimizerGroup,
      List<TableRuntimeMeta> tableRuntimeMetaList,
      List<OptimizerInstance> authOptimizers,
      long optimizerTouchTimeout,
      long taskAckTimeout) {
    Preconditions.checkNotNull(optimizerGroup, "optimizerGroup can not be null");
    this.optimizerTouchTimeout = optimizerTouchTimeout;
    this.taskAckTimeout = taskAckTimeout;
    this.optimizerGroup = optimizerGroup;
    this.schedulingPolicy = new SchedulingPolicy(optimizerGroup);
    this.tableManager = tableManager;
    this.authOptimizers.putAll(authOptimizers.stream().collect(Collectors.toMap(
        OptimizerInstance::getToken, optimizer -> optimizer)));
    tableRuntimeMetaList.forEach(this::initTableRuntime);
  }

  private void initTableRuntime(TableRuntimeMeta tableRuntimeMeta) {
    TableRuntime tableRuntime = tableRuntimeMeta.getTableRuntime();
    if (tableRuntime.getOptimizingStatus().isProcessing() &&
        tableRuntimeMeta.getOptimizingProcessId() != 0) {
      tableRuntime.recover(new TableOptimizingProcess(tableRuntimeMeta)
          .handleTaskClear(this::clearTasks)
          .handleTaskRetry(this::retryTask));
    }

    if (tableRuntime.isOptimizingEnabled()) {
      //TODO: load task quotas
      tableRuntime.resetTaskQuotas(System.currentTimeMillis() - ArcticServiceConstants.QUOTA_LOOK_BACK_TIME);
      if (tableRuntime.getOptimizingStatus() == OptimizingStatus.IDLE ||
          tableRuntime.getOptimizingStatus() == OptimizingStatus.PENDING) {
        schedulingPolicy.addTable(tableRuntime);
      } else if (tableRuntime.getOptimizingStatus() != OptimizingStatus.COMMITTING) {
        TableOptimizingProcess process = new TableOptimizingProcess(tableRuntimeMeta)
            .handleTaskClear(this::clearTasks)
            .handleTaskRetry(this::retryTask);
        process.getTaskMap().entrySet().stream().filter(
                entry -> entry.getValue().getStatus() == TaskRuntime.Status.SCHEDULED ||
                    entry.getValue().getStatus() == TaskRuntime.Status.ACKED)
            .forEach(entry -> executingTaskMap.put(entry.getKey(), entry.getValue()));
        process.getTaskMap().values().stream()
            .filter(task -> task.getStatus() == TaskRuntime.Status.PLANNED)
            .forEach(taskQueue::offer);
      }
    } else {
      OptimizingProcess process = tableRuntime.getOptimizingProcess();
      if (process != null) {
        process.close();
      }
    }
  }

  public void refreshTable(TableRuntime tableRuntime) {
    if (tableRuntime.isOptimizingEnabled() && !tableRuntime.getOptimizingStatus().isProcessing()) {
      LOG.info("Bind queue {} success with table {}", optimizerGroup.getName(), tableRuntime.getTableIdentifier());
      tableRuntime.resetTaskQuotas(System.currentTimeMillis() - ArcticServiceConstants.QUOTA_LOOK_BACK_TIME);
      schedulingPolicy.addTable(tableRuntime);
    }
  }

  public void releaseTable(TableRuntime tableRuntime) {
    schedulingPolicy.removeTable(tableRuntime);
    LOG.info("Release queue {} with table {}", optimizerGroup.getName(), tableRuntime.getTableIdentifier());
  }

  public boolean containsTable(ServerTableIdentifier identifier) {
    return this.schedulingPolicy.containsTable(identifier);
  }

  public List<OptimizerInstance> getOptimizers() {
    return ImmutableList.copyOf(authOptimizers.values());
  }

  public void removeOptimizer(String resourceId) {
    authOptimizers.entrySet().removeIf(op -> op.getValue().getResourceId().equals(resourceId));
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
      executingTaskMap.putIfAbsent(task.getTaskId(), task);
    }
    return task != null ? task.getOptimizingTask() : null;
  }

  private void safelySchedule(TaskRuntime task, OptimizingThread thread) {
    try {
      task.schedule(thread);
    } catch (Throwable throwable) {
      retryTask(task, false);
      throw throwable;
    }
  }

  private void retryTask(TaskRuntime taskRuntime, boolean incRetryCount) {
    taskRuntime.reset(incRetryCount);
    retryQueue.offer(taskRuntime);
  }

  @Override
  public void ackTask(String authToken, int threadId, OptimizingTaskId taskId) {
    Optional.ofNullable(executingTaskMap.get(taskId))
        .orElseThrow(() -> new TaskNotFoundException(taskId))
        .ack(new OptimizingThread(authToken, threadId));
  }

  @Override
  public void completeTask(String authToken, OptimizingTaskResult taskResult) {
    OptimizingThread thread = new OptimizingThread(authToken, taskResult.getThreadId());
    Optional.ofNullable(executingTaskMap.get(taskResult.getTaskId()))
        .orElseThrow(() -> new TaskNotFoundException(taskResult.getTaskId()))
        .complete(thread, taskResult);
    executingTaskMap.remove(taskResult.getTaskId());
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

  public List<String> checkSuspending() {
    long currentTime = System.currentTimeMillis();
    List<String> expiredOptimizers = authOptimizers.values().stream()
        .filter(optimizer -> currentTime - optimizer.getTouchTime() > optimizerTouchTimeout)
        .map(OptimizerInstance::getToken)
        .collect(Collectors.toList());

    expiredOptimizers.forEach(authOptimizers.keySet()::remove);

    List<TaskRuntime> suspendingTasks = executingTaskMap.values().stream()
        .filter(task -> task.isSuspending(currentTime, taskAckTimeout) ||
            expiredOptimizers.contains(task.getOptimizingThread().getToken()) ||
            !authOptimizers.containsKey(task.getOptimizingThread().getToken()))
        .collect(Collectors.toList());
    suspendingTasks.forEach(task -> {
      executingTaskMap.remove(task.getTaskId());
      //optimizing task of suspending optimizer would not be counted for retrying
      retryTask(task, false);
    });
    return expiredOptimizers;
  }

  public void updateOptimizerGroup(ResourceGroup optimizerGroup) {
    Preconditions.checkArgument(
        this.optimizerGroup.getName().equals(optimizerGroup.getName()),
        "optimizer group name mismatch");
    this.optimizerGroup = optimizerGroup;
  }

  @VisibleForTesting
  Map<OptimizingTaskId, TaskRuntime> getExecutingTaskMap() {
    return executingTaskMap;
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
    LOG.debug("Calculating and sorting tables by quota:" + scheduledTables);

    for (TableRuntime tableRuntime : scheduledTables) {
      if (tableRuntime.getOptimizingStatus().isProcessing()) {
        continue;
      }
      LOG.debug("Planning table " + tableRuntime.getTableIdentifier());
      try {
        ArcticTable table = tableManager.loadTable(tableRuntime.getTableIdentifier());
        OptimizingPlanner planner = new OptimizingPlanner(
            tableRuntime.refresh(table),
            table,
            new TaskSplitVisitor.Builder()
                .setAvailableCore(getAvailableCore(tableRuntime))
                .setParallelism(getTotalOptimizerParallelism())
                .build()
        );
        if (tableRuntime.isBlocked(BlockableOperation.OPTIMIZE)) {
          LOG.info("{} optimize is blocked, continue", tableRuntime.getTableIdentifier());
          continue;
        }
        if (planner.isNecessary()) {
          OptimizingProcessIterator processIterator = new OptimizingProcessIterator.Builder()
              .fromPlanner(planner)
              .handleTaskClear(this::clearTasks)
              .handleTaskRetry(this::retryTask)
              .handleTaskOffer(taskQueue::offer)
              .iterator();
          LOG.info("{} after plan get {} processes", tableRuntime.getTableIdentifier(), processIterator.size());
          tableRuntime.startProcess(processIterator);
          break;
        } else {
          tableRuntime.cleanPendingInput();
        }
      } catch (Throwable e) {
        LOG.error(tableRuntime.getTableIdentifier() + " plan failed, continue", e);
      }
    }
  }

  private double getAvailableCore(TableRuntime tableRuntime) {
    return tableRuntime.getOptimizingConfig().getTargetQuota();
  }

  private int getTotalOptimizerParallelism() {
    return this.authOptimizers.values()
        .stream()
        .mapToInt(Resource::getThreadCount)
        .sum();
  }

  @VisibleForTesting
  SchedulingPolicy getSchedulingPolicy() {
    return schedulingPolicy;
  }

  private void clearTasks(OptimizingProcess optimizingProcess) {
    retryQueue.removeIf(taskRuntime -> taskRuntime.getProcessId() == optimizingProcess.getProcessId());
    taskQueue.removeIf(taskRuntime -> taskRuntime.getProcessId() == optimizingProcess.getProcessId());
  }

  public static class OptimizingThread {

    private String token;
    private int threadId;

    public OptimizingThread(String token, int threadId) {
      this.token = token;
      this.threadId = threadId;
    }

    public OptimizingThread() {
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

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

package org.apache.amoro.server;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.api.OptimizingService;
import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.exception.ForbiddenException;
import org.apache.amoro.exception.IllegalTaskStateException;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.exception.PluginRetryAuthException;
import org.apache.amoro.exception.TaskNotFoundException;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingProcessMeta;
import org.apache.amoro.server.optimizing.OptimizingQueue;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.StatedPersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.server.persistence.mapper.ResourceMapper;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * DefaultOptimizingService is implementing the OptimizerManager Thrift service, which manages the
 * optimizing tasks for tables. It includes methods for authenticating optimizers, polling tasks
 * from the optimizing queue, acknowledging tasks,and completing tasks. The code uses several data
 * structures, including maps for optimizing queues ,task runtimes, and authenticated optimizers.
 *
 * <p>The code also includes a TimerTask for detecting and removing expired optimizers and
 * suspending tasks.
 */
public class DefaultOptimizingService extends StatedPersistentBase
    implements OptimizingService.Iface, QuotaProvider {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizingService.class);

  private final long optimizerTouchTimeout;
  private final long taskAckTimeout;
  private final int maxPlanningParallelism;
  private final long pollingTimeout;
  private final long refreshGroupInterval;
  private final Map<String, OptimizingQueue> optimizingQueueByGroup = new ConcurrentHashMap<>();
  private final Map<String, OptimizingQueue> optimizingQueueByToken = new ConcurrentHashMap<>();
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();
  private final OptimizerKeeper optimizerKeeper = new OptimizerKeeper();
  private final OptimizingConfigWatcher optimizingConfigWatcher = new OptimizingConfigWatcher();
  private final CatalogManager catalogManager;
  private final OptimizerManager optimizerManager;
  private final TableService tableService;
  private final RuntimeHandlerChain tableHandlerChain;
  private final ExecutorService planExecutor;

  public DefaultOptimizingService(
      Configurations serviceConfig,
      CatalogManager catalogManager,
      OptimizerManager optimizerManager,
      TableService tableService) {
    this.optimizerTouchTimeout =
        serviceConfig.get(AmoroManagementConf.OPTIMIZER_HB_TIMEOUT).toMillis();
    this.taskAckTimeout =
        serviceConfig.get(AmoroManagementConf.OPTIMIZER_TASK_ACK_TIMEOUT).toMillis();
    this.refreshGroupInterval =
        serviceConfig.get(AmoroManagementConf.OPTIMIZING_REFRESH_GROUP_INTERVAL).toMillis();
    this.maxPlanningParallelism =
        serviceConfig.getInteger(AmoroManagementConf.OPTIMIZER_MAX_PLANNING_PARALLELISM);
    this.pollingTimeout =
        serviceConfig.get(AmoroManagementConf.OPTIMIZER_POLLING_TIMEOUT).toMillis();
    this.tableService = tableService;
    this.catalogManager = catalogManager;
    this.optimizerManager = optimizerManager;
    this.tableHandlerChain = new TableRuntimeHandlerImpl();
    this.planExecutor =
        Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("plan-executor-thread-%d")
                .setDaemon(true)
                .build());
  }

  public RuntimeHandlerChain getTableRuntimeHandler() {
    return tableHandlerChain;
  }

  private void loadOptimizingQueues(List<TableRuntime> tableRuntimeMetaList) {
    List<ResourceGroup> optimizerGroups =
        getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups);
    List<OptimizerInstance> optimizers = getAs(OptimizerMapper.class, OptimizerMapper::selectAll);
    Map<String, List<TableRuntime>> groupToTableRuntimes =
        tableRuntimeMetaList.stream()
            .collect(Collectors.groupingBy(TableRuntime::getOptimizerGroup));
    optimizerGroups.forEach(
        group -> {
          String groupName = group.getName();
          List<TableRuntime> tableRuntimes = groupToTableRuntimes.remove(groupName);
          OptimizingQueue optimizingQueue =
              new OptimizingQueue(
                  catalogManager,
                  group,
                  this,
                  planExecutor,
                  Optional.ofNullable(tableRuntimes).orElseGet(ArrayList::new),
                  maxPlanningParallelism);
          optimizingQueueByGroup.put(groupName, optimizingQueue);
        });
    optimizers.forEach(optimizer -> registerOptimizer(optimizer, false));
    groupToTableRuntimes
        .keySet()
        .forEach(groupName -> LOG.warn("Unloaded task runtime in group {}", groupName));
  }

  private void registerOptimizer(OptimizerInstance optimizer, boolean needPersistent) {
    if (needPersistent) {
      doAs(OptimizerMapper.class, mapper -> mapper.insertOptimizer(optimizer));
    }

    OptimizingQueue optimizingQueue = optimizingQueueByGroup.get(optimizer.getGroupName());
    optimizingQueue.addOptimizer(optimizer);
    authOptimizers.put(optimizer.getToken(), optimizer);
    optimizingQueueByToken.put(optimizer.getToken(), optimizingQueue);
    optimizerKeeper.keepInTouch(optimizer);
  }

  private void unregisterOptimizer(String token) {
    doAs(OptimizerMapper.class, mapper -> mapper.deleteOptimizer(token));
    OptimizingQueue optimizingQueue = optimizingQueueByToken.remove(token);
    OptimizerInstance optimizer = authOptimizers.remove(token);
    if (optimizingQueue != null) {
      optimizingQueue.removeOptimizer(optimizer);
    }
  }

  @Override
  public void ping() {}

  public List<TaskRuntime<?>> listTasks(String optimizerGroup) {
    return getQueueByGroup(optimizerGroup).collectTasks();
  }

  @Override
  public void touch(String authToken) {
    OptimizerInstance optimizer = getAuthenticatedOptimizer(authToken).touch();
    LOG.debug("Optimizer {} touch time: {}", optimizer.getToken(), optimizer.getTouchTime());
    doAs(OptimizerMapper.class, mapper -> mapper.updateTouchTime(optimizer.getToken()));
  }

  private OptimizerInstance getAuthenticatedOptimizer(String authToken) {
    Preconditions.checkArgument(authToken != null, "authToken can not be null");
    return Optional.ofNullable(authOptimizers.get(authToken))
        .orElseThrow(() -> new PluginRetryAuthException("Optimizer has not been authenticated"));
  }

  @Override
  public OptimizingTask pollTask(String authToken, int threadId) {
    LOG.debug("Optimizer {} (threadId {}) try polling task", authToken, threadId);
    OptimizingQueue queue = getQueueByToken(authToken);
    return Optional.ofNullable(queue.pollTask(pollingTimeout))
        .map(task -> extractOptimizingTask(task, authToken, threadId, queue))
        .orElse(null);
  }

  private OptimizingTask extractOptimizingTask(
      TaskRuntime<?> task, String authToken, int threadId, OptimizingQueue queue) {
    try {
      OptimizerThread optimizerThread = getAuthenticatedOptimizer(authToken).getThread(threadId);
      task.schedule(optimizerThread);
      LOG.info("OptimizerThread {} polled task {}", optimizerThread, task.getTaskId());
      return task.extractProtocolTask();
    } catch (Throwable throwable) {
      LOG.error("Schedule task {} failed, put it to retry queue", task.getTaskId(), throwable);
      queue.retryTask(task);
      return null;
    }
  }

  @Override
  public void ackTask(String authToken, int threadId, OptimizingTaskId taskId) {
    LOG.info("Ack task {} by optimizer {} (threadId {})", taskId, authToken, threadId);
    OptimizingQueue queue = getQueueByToken(authToken);
    Optional.ofNullable(queue.getTask(taskId))
        .orElseThrow(() -> new TaskNotFoundException(taskId))
        .ack(getAuthenticatedOptimizer(authToken).getThread(threadId));
  }

  @Override
  public void completeTask(String authToken, OptimizingTaskResult taskResult) {
    LOG.info(
        "Optimizer {} (threadId {}) complete task {}",
        authToken,
        taskResult.getThreadId(),
        taskResult.getTaskId());
    OptimizingQueue queue = getQueueByToken(authToken);
    OptimizerThread thread =
        getAuthenticatedOptimizer(authToken).getThread(taskResult.getThreadId());
    Optional.ofNullable(queue.getTask(taskResult.getTaskId()))
        .orElseThrow(() -> new TaskNotFoundException(taskResult.getTaskId()))
        .complete(thread, taskResult);
  }

  @Override
  public String authenticate(OptimizerRegisterInfo registerInfo) {
    LOG.info("Register optimizer {}.", registerInfo);
    Optional.ofNullable(
            registerInfo.getProperties().get(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL))
        .ifPresent(
            interval -> {
              if (Long.parseLong(interval) >= optimizerTouchTimeout) {
                throw new ForbiddenException(
                    String.format(
                        "The %s:%s configuration should be less than AMS's %s:%s",
                        OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL,
                        interval,
                        AmoroManagementConf.OPTIMIZER_HB_TIMEOUT.key(),
                        optimizerTouchTimeout));
              }
            });

    OptimizingQueue queue = getQueueByGroup(registerInfo.getGroupName());
    OptimizerInstance optimizer = new OptimizerInstance(registerInfo, queue.getContainerName());
    registerOptimizer(optimizer, true);
    return optimizer.getToken();
  }

  @Override
  public boolean cancelProcess(long processId) throws TException {
    OptimizingProcessMeta processMeta =
        getAs(OptimizingMapper.class, m -> m.getOptimizingProcess(processId));
    if (processMeta == null) {
      return false;
    }
    long tableId = processMeta.getTableId();
    TableRuntime tableRuntime = tableService.getRuntime(tableId);
    if (tableRuntime == null) {
      return false;
    }
    OptimizingProcess process = tableRuntime.getOptimizingProcess();
    if (process == null || process.getProcessId() != processId) {
      return false;
    }
    process.close();
    return true;
  }

  /**
   * Get optimizing queue.
   *
   * @return OptimizeQueueItem
   */
  private OptimizingQueue getQueueByGroup(String optimizerGroup) {
    return getOptionalQueueByGroup(optimizerGroup)
        .orElseThrow(() -> new ObjectNotExistsException("Optimizer group " + optimizerGroup));
  }

  private Optional<OptimizingQueue> getOptionalQueueByGroup(String optimizerGroup) {
    Preconditions.checkArgument(optimizerGroup != null, "optimizerGroup can not be null");
    return Optional.ofNullable(optimizingQueueByGroup.get(optimizerGroup));
  }

  private OptimizingQueue getQueueByToken(String token) {
    Preconditions.checkArgument(token != null, "optimizer token can not be null");
    return Optional.ofNullable(optimizingQueueByToken.get(token))
        .orElseThrow(() -> new PluginRetryAuthException("Optimizer has not been authenticated"));
  }

  public void deleteOptimizer(String group, String resourceId) {
    List<OptimizerInstance> deleteOptimizers =
        getAs(OptimizerMapper.class, mapper -> mapper.selectByResourceId(resourceId));
    deleteOptimizers.forEach(
        optimizer -> {
          String token = optimizer.getToken();
          unregisterOptimizer(token);
        });
  }

  public void createResourceGroup(ResourceGroup resourceGroup) {
    doAsTransaction(
        () -> {
          OptimizingQueue optimizingQueue =
              new OptimizingQueue(
                  catalogManager,
                  resourceGroup,
                  this,
                  planExecutor,
                  new ArrayList<>(),
                  maxPlanningParallelism);
          optimizingQueueByGroup.put(resourceGroup.getName(), optimizingQueue);
        });
  }

  public void deleteResourceGroup(String groupName) {
    OptimizingQueue optimizingQueue = optimizingQueueByGroup.remove(groupName);
    optimizingQueue.dispose();
  }

  public void updateResourceGroup(ResourceGroup resourceGroup) {
    Optional.ofNullable(optimizingQueueByGroup.get(resourceGroup.getName()))
        .ifPresent(queue -> queue.updateOptimizerGroup(resourceGroup));
  }

  public void dispose() {
    optimizerKeeper.dispose();
    tableHandlerChain.dispose();
    optimizingQueueByGroup.clear();
    optimizingQueueByToken.clear();
    authOptimizers.clear();
    planExecutor.shutdown();
    optimizingConfigWatcher.dispose();
  }

  @Override
  public int getTotalQuota(String resourceGroup) {
    return authOptimizers.values().stream()
        .filter(optimizer -> optimizer.getGroupName().equals(resourceGroup))
        .mapToInt(OptimizerInstance::getThreadCount)
        .sum();
  }

  private class TableRuntimeHandlerImpl extends RuntimeHandlerChain {

    @Override
    public void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
      if (!tableRuntime.getOptimizingStatus().isProcessing()) {
        getOptionalQueueByGroup(tableRuntime.getOptimizerGroup())
            .ifPresent(q -> q.refreshTable(tableRuntime));
      }
    }

    @Override
    public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
      String originalGroup = originalConfig.getOptimizingConfig().getOptimizerGroup();
      if (!tableRuntime.getOptimizerGroup().equals(originalGroup)) {
        getOptionalQueueByGroup(originalGroup).ifPresent(q -> q.releaseTable(tableRuntime));
      }
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup())
          .ifPresent(q -> q.refreshTable(tableRuntime));
    }

    @Override
    public void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup())
          .ifPresent(q -> q.refreshTable(tableRuntime));
    }

    @Override
    public void handleTableRemoved(TableRuntime tableRuntime) {
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup())
          .ifPresent(queue -> queue.releaseTable(tableRuntime));
    }

    @Override
    protected void initHandler(List<TableRuntime> tableRuntimeList) {
      LOG.info("OptimizerManagementService begin initializing");
      loadOptimizingQueues(tableRuntimeList);
      optimizerKeeper.start();
      optimizingConfigWatcher.start();
      LOG.info("SuspendingDetector for Optimizer has been started.");
      LOG.info("OptimizerManagementService initializing has completed");
    }

    @Override
    protected void doDispose() {}
  }

  private class OptimizerKeepingTask implements Delayed {

    private final OptimizerInstance optimizerInstance;
    private final long lastTouchTime;

    public OptimizerKeepingTask(OptimizerInstance optimizer) {
      this.optimizerInstance = optimizer;
      this.lastTouchTime = optimizer.getTouchTime();
    }

    public boolean tryKeeping() {
      return Objects.equals(optimizerInstance, authOptimizers.get(optimizerInstance.getToken()))
          && lastTouchTime != optimizerInstance.getTouchTime();
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
      return unit.convert(
          lastTouchTime + optimizerTouchTimeout - System.currentTimeMillis(),
          TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
      OptimizerKeepingTask another = (OptimizerKeepingTask) o;
      return Long.compare(lastTouchTime, another.lastTouchTime);
    }

    public String getToken() {
      return optimizerInstance.getToken();
    }

    public OptimizingQueue getQueue() {
      return optimizingQueueByGroup.get(optimizerInstance.getGroupName());
    }

    public OptimizerInstance getOptimizer() {
      return optimizerInstance;
    }
  }

  private class OptimizerKeeper implements Runnable {

    private volatile boolean stopped = false;
    private final Thread thread = new Thread(this, "optimizer-keeper-thread");
    private final DelayQueue<OptimizerKeepingTask> suspendingQueue = new DelayQueue<>();

    public OptimizerKeeper() {
      thread.setDaemon(true);
    }

    public void keepInTouch(OptimizerInstance optimizerInstance) {
      Preconditions.checkNotNull(optimizerInstance, "token can not be null");
      suspendingQueue.add(new OptimizerKeepingTask(optimizerInstance));
    }

    public void start() {
      thread.start();
    }

    public void dispose() {
      stopped = true;
      thread.interrupt();
    }

    @Override
    public void run() {
      while (!stopped) {
        try {
          OptimizerKeepingTask keepingTask = suspendingQueue.take();
          String token = keepingTask.getToken();
          boolean isExpired = !keepingTask.tryKeeping();
          Optional.ofNullable(keepingTask.getQueue())
              .ifPresent(
                  queue ->
                      queue
                          .collectTasks(buildSuspendingPredication(authOptimizers.keySet()))
                          .forEach(task -> retryTask(task, queue)));
          if (isExpired) {
            LOG.info("Optimizer {} has been expired, unregister it", keepingTask.getOptimizer());
            unregisterOptimizer(token);
          } else {
            LOG.debug("Optimizer {} is being touched, keep it", keepingTask.getOptimizer());
            keepInTouch(keepingTask.getOptimizer());
          }
        } catch (InterruptedException ignored) {
        } catch (Throwable t) {
          LOG.error("OptimizerKeeper has encountered a problem.", t);
        }
      }
    }

    private void retryTask(TaskRuntime<?> task, OptimizingQueue queue) {
      LOG.info(
          "Task {} is suspending, since it's optimizer is expired, put it to retry queue, optimizer {}",
          task.getTaskId(),
          task.getResourceDesc());
      // optimizing task of suspending optimizer would not be counted for retrying
      try {
        queue.retryTask(task);
      } catch (IllegalTaskStateException e) {
        LOG.error(
            "Retry task {} failed due to {}, will check it in next round",
            task.getTaskId(),
            e.getMessage());
      }
    }

    private Predicate<TaskRuntime<?>> buildSuspendingPredication(Set<String> activeTokens) {
      return task ->
          StringUtils.isNotBlank(task.getToken())
                  && !activeTokens.contains(task.getToken())
                  && task.getStatus() != TaskRuntime.Status.SUCCESS
              || task.getStatus() == TaskRuntime.Status.SCHEDULED
                  && task.getStartTime() + taskAckTimeout < System.currentTimeMillis();
    }
  }

  private class OptimizingConfigWatcher implements Runnable {
    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("resource-group-watcher-%d").build());

    void start() {
      run();
      scheduler.scheduleAtFixedRate(
          this, refreshGroupInterval, refreshGroupInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
      syncGroups();
    }

    private void syncGroups() {
      try {
        List<ResourceGroup> resourceGroups = optimizerManager.listResourceGroups();
        Set<String> groupNames =
            resourceGroups.stream().map(ResourceGroup::getName).collect(Collectors.toSet());
        Sets.difference(optimizingQueueByGroup.keySet(), groupNames)
            .forEach(DefaultOptimizingService.this::deleteResourceGroup);
        resourceGroups.forEach(
            resourceGroup -> {
              boolean newGroup = !optimizingQueueByGroup.containsKey(resourceGroup.getName());
              if (newGroup) {
                createResourceGroup(resourceGroup);
              } else {
                if (!optimizingQueueByGroup
                    .get(resourceGroup.getName())
                    .getOptimizerGroup()
                    .equals(resourceGroup)) {
                  updateResourceGroup(resourceGroup);
                }
              }
            });
      } catch (Throwable t) {
        LOG.error("Sync optimizer groups failed, will retry later.", t);
      }
    }

    void dispose() {
      scheduler.shutdown();
    }
  }
}

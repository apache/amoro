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

package com.netease.arctic.server;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.OptimizingService;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.exception.PluginRetryAuthException;
import com.netease.arctic.server.exception.TaskNotFoundException;
import com.netease.arctic.server.optimizing.OptimizingQueue;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.optimizing.TaskRuntime;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizerMapper;
import com.netease.arctic.server.persistence.mapper.ResourceMapper;
import com.netease.arctic.server.resource.OptimizerInstance;
import com.netease.arctic.server.resource.OptimizerManager;
import com.netease.arctic.server.resource.OptimizerThread;
import com.netease.arctic.server.resource.QuotaProvider;
import com.netease.arctic.server.table.DefaultTableService;
import com.netease.arctic.server.table.RuntimeHandlerChain;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * DefaultOptimizingService is implementing the OptimizerManager Thrift service, which manages the
 * optimization tasks for ArcticTable. It includes methods for authenticating optimizers, polling
 * tasks from the optimizing queue, acknowledging tasks,and completing tasks. The code uses several
 * data structures, including maps for optimizing queues ,task runtimes, and authenticated
 * optimizers.
 *
 * <p>The code also includes a TimerTask for detecting and removing expired optimizers and
 * suspending tasks.
 */
public class DefaultOptimizingService extends StatedPersistentBase
    implements OptimizingService.Iface, OptimizerManager, QuotaProvider {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizingService.class);

  private final long optimizerTouchTimeout;
  private final long taskAckTimeout;
  private final int maxPlanningParallelism;
  private final long pollingTimeout;
  private final long minPlanningInterval;
  private final Map<String, OptimizingQueue> optimizingQueueByGroup = new ConcurrentHashMap<>();
  private final Map<String, OptimizingQueue> optimizingQueueByToken = new ConcurrentHashMap<>();
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();
  private final OptimizerKeeper optimizerKeeper = new OptimizerKeeper();
  private final TableService tableService;
  private final RuntimeHandlerChain tableHandlerChain;
  private final Executor planExecutor;

  public DefaultOptimizingService(Configurations serviceConfig, DefaultTableService tableService) {
    this.optimizerTouchTimeout = serviceConfig.getLong(ArcticManagementConf.OPTIMIZER_HB_TIMEOUT);
    this.taskAckTimeout = serviceConfig.getLong(ArcticManagementConf.OPTIMIZER_TASK_ACK_TIMEOUT);
    this.maxPlanningParallelism =
        serviceConfig.getInteger(ArcticManagementConf.OPTIMIZER_MAX_PLANNING_PARALLELISM);
    this.pollingTimeout = serviceConfig.getLong(ArcticManagementConf.OPTIMIZER_POLLING_TIMEOUT);
    this.minPlanningInterval =
        serviceConfig.getLong(ArcticManagementConf.GLOBAL_MIN_PLANNING_INTERVAL);
    this.tableService = tableService;
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

  private void loadOptimizingQueues(List<TableRuntimeMeta> tableRuntimeMetaList) {
    List<ResourceGroup> optimizerGroups =
        getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups);
    List<OptimizerInstance> optimizers = getAs(OptimizerMapper.class, OptimizerMapper::selectAll);
    Map<String, List<TableRuntimeMeta>> groupToTableRuntimes =
        tableRuntimeMetaList.stream()
            .collect(Collectors.groupingBy(TableRuntimeMeta::getOptimizerGroup));
    optimizerGroups.forEach(
        group -> {
          String groupName = group.getName();
          List<TableRuntimeMeta> tableRuntimeMetas = groupToTableRuntimes.remove(groupName);
          OptimizingQueue optimizingQueue =
              new OptimizingQueue(
                  tableService,
                  group,
                  this,
                  planExecutor,
                  Optional.ofNullable(tableRuntimeMetas).orElseGet(ArrayList::new),
                  maxPlanningParallelism,
                  minPlanningInterval);
          optimizingQueueByGroup.put(groupName, optimizingQueue);
        });
    optimizers.forEach(optimizer -> registerOptimizer(optimizer, false));
    groupToTableRuntimes
        .keySet()
        .forEach(groupName -> LOG.warn("Unloaded task runtime in group " + groupName));
  }

  private void registerOptimizer(OptimizerInstance optimizer, boolean needPersistency) {
    if (needPersistency) {
      doAs(OptimizerMapper.class, mapper -> mapper.insertOptimizer(optimizer));
    }
    authOptimizers.put(optimizer.getToken(), optimizer);
    optimizingQueueByToken.put(
        optimizer.getToken(), optimizingQueueByGroup.get(optimizer.getGroupName()));
    optimizerKeeper.keepInTouch(optimizer);
  }

  private void unregisterOptimizer(String token) {
    doAs(OptimizerMapper.class, mapper -> mapper.deleteOptimizer(token));
    optimizingQueueByToken.remove(token);
    authOptimizers.remove(token);
  }

  @Override
  public void ping() {}

  public List<TaskRuntime> listTasks(String optimizerGroup) {
    return getQueueByGroup(optimizerGroup).collectTasks();
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
    Preconditions.checkArgument(
        authToken != null, "authToken can not be null");
    return Optional.ofNullable(authOptimizers.get(authToken))
        .orElseThrow(() -> new PluginRetryAuthException("Optimizer has not been authenticated"));
  }

  @Override
  public OptimizingTask pollTask(String authToken, int threadId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Optimizer {} (threadId {}) try polling task", authToken, threadId);
    }
    OptimizingQueue queue = getQueueByToken(authToken);
    return Optional.ofNullable(queue.pollTask(pollingTimeout))
        .map(
            task ->
                extractOptimizingTask(
                    task, getAuthenticatedOptimizer(authToken).getThread(threadId), queue))
        .orElse(null);
  }

  private OptimizingTask extractOptimizingTask(
      TaskRuntime task, OptimizerThread optimizerThread, OptimizingQueue queue) {
    try {
      task.schedule(optimizerThread);
      LOG.info("OptimizerThread {} polled task {}", optimizerThread, task.getTaskId());
      return task.getOptimizingTask();
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
    LOG.info("Optimizer {} complete task {}", authToken, taskResult.getTaskId());
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
    OptimizingQueue queue = getQueueByGroup(registerInfo.getGroupName());
    OptimizerInstance optimizer = new OptimizerInstance(registerInfo, queue.getContainerName());
    registerOptimizer(optimizer, true);
    return optimizer.getToken();
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

  @Override
  public List<OptimizerInstance> listOptimizers() {
    return ImmutableList.copyOf(authOptimizers.values());
  }

  @Override
  public List<OptimizerInstance> listOptimizers(String group) {
    return authOptimizers.values().stream()
        .filter(optimizer -> optimizer.getGroupName().equals(group))
        .collect(Collectors.toList());
  }

  @Override
  public void deleteOptimizer(String group, String resourceId) {
    List<OptimizerInstance> deleteOptimizers =
        getAs(OptimizerMapper.class, mapper -> mapper.selectByResourceId(resourceId));
    deleteOptimizers.forEach(
        optimizer -> {
          String token = optimizer.getToken();
          unregisterOptimizer(token);
        });
  }

  @Override
  public void createResourceGroup(ResourceGroup resourceGroup) {
    doAsTransaction(
        () -> {
          doAs(ResourceMapper.class, mapper -> mapper.insertResourceGroup(resourceGroup));
          OptimizingQueue optimizingQueue =
              new OptimizingQueue(
                  tableService,
                  resourceGroup,
                  this,
                  planExecutor,
                  new ArrayList<>(),
                  maxPlanningParallelism,
                  minPlanningInterval);
          optimizingQueueByGroup.put(resourceGroup.getName(), optimizingQueue);
        });
  }

  @Override
  public void deleteResourceGroup(String groupName) {
    if (canDeleteResourceGroup(groupName)) {
      doAs(ResourceMapper.class, mapper -> mapper.deleteResourceGroup(groupName));
      optimizingQueueByGroup.remove(groupName);
    } else {
      throw new RuntimeException(
          String.format(
              "The resource group %s cannot be deleted because it is currently in " + "use.",
              groupName));
    }
  }

  @Override
  public void updateResourceGroup(ResourceGroup resourceGroup) {
    Preconditions.checkNotNull(resourceGroup, "The resource group cannot be null.");
    Optional.ofNullable(optimizingQueueByGroup.get(resourceGroup.getName()))
        .ifPresent(queue -> queue.updateOptimizerGroup(resourceGroup));
    doAs(ResourceMapper.class, mapper -> mapper.updateResourceGroup(resourceGroup));
  }

  @Override
  public void createResource(Resource resource) {
    doAs(ResourceMapper.class, mapper -> mapper.insertResource(resource));
  }

  @Override
  public void deleteResource(String resourceId) {
    doAs(ResourceMapper.class, mapper -> mapper.deleteResource(resourceId));
  }

  @Override
  public List<ResourceGroup> listResourceGroups() {
    return getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups);
  }

  @Override
  public List<ResourceGroup> listResourceGroups(String containerName) {
    return getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups).stream()
        .filter(group -> group.getContainer().equals(containerName))
        .collect(Collectors.toList());
  }

  @Override
  public ResourceGroup getResourceGroup(String groupName) {
    return getAs(ResourceMapper.class, mapper -> mapper.selectResourceGroup(groupName));
  }

  @Override
  public List<Resource> listResourcesByGroup(String groupName) {
    return getAs(ResourceMapper.class, mapper -> mapper.selectResourcesByGroup(groupName));
  }

  @Override
  public Resource getResource(String resourceId) {
    return getAs(ResourceMapper.class, mapper -> mapper.selectResource(resourceId));
  }

  @Override
  public void dispose() {
    optimizerKeeper.dispose();
    tableHandlerChain.dispose();
    optimizingQueueByGroup.clear();
    optimizingQueueByToken.clear();
    authOptimizers.clear();
  }

  public boolean canDeleteResourceGroup(String name) {
    for (CatalogMeta catalogMeta : tableService.listCatalogMetas()) {
      if (catalogMeta.getCatalogProperties() != null
          && catalogMeta
              .getCatalogProperties()
              .getOrDefault(
                  CatalogMetaProperties.TABLE_PROPERTIES_PREFIX
                      + TableProperties.SELF_OPTIMIZING_GROUP,
                  TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT)
              .equals(name)) {
        return false;
      }
    }
    for (OptimizerInstance optimizer : listOptimizers()) {
      if (optimizer.getGroupName().equals(name)) {
        return false;
      }
    }
    for (ServerTableIdentifier identifier : tableService.listManagedTables()) {
      if (optimizingQueueByGroup.containsKey(name)
          && optimizingQueueByGroup.get(name).containsTable(identifier)) {
        return false;
      }
    }
    return true;
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
    protected void initHandler(List<TableRuntimeMeta> tableRuntimeMetaList) {
      LOG.info("OptimizerManagementService begin initializing");
      loadOptimizingQueues(tableRuntimeMetaList);
      optimizerKeeper.start();
      LOG.info("SuspendingDetector for Optimizer has been started.");
      LOG.info("OptimizerManagementService initializing has completed");
    }

    @Override
    protected void doDispose() {
      DefaultOptimizingService.this.dispose();
    }
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
          Optional.ofNullable(optimizingQueueByToken.get(token))
              .ifPresent(
                  queue ->
                      queue
                          .collectTasks(buildSuspendingPredication(token, isExpired))
                          .forEach(task -> retryTask(task, queue)));
          if (isExpired) {
            LOG.info("Optimizer {} has been expired, unregister it", keepingTask.getOptimizer());
            unregisterOptimizer(token);
          } else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Optimizer {} is being touched, keep it", keepingTask.getOptimizer());
            }
            keepInTouch(keepingTask.getOptimizer());
          }
        } catch (InterruptedException ignored) {
        } catch (Throwable t) {
          LOG.error("OptimizerKeeper has encountered a problem.", t);
        }
      }
    }

    private void retryTask(TaskRuntime task, OptimizingQueue queue) {
      LOG.info(
          "Task {} is suspending, since it's optimizer is expired, put it to retry queue, optimizer {}",
          task.getTaskId(),
          task.getResourceDesc());
      // optimizing task of suspending optimizer would not be counted for retrying
      queue.retryTask(task);
    }

    private Predicate<TaskRuntime> buildSuspendingPredication(
        String token, boolean isOptimizerExpired) {
      return task -> {
        if (isOptimizerExpired) {
          return token.equals(task.getToken());
        } else {
          return token.equals(task.getToken())
              && task.getStatus() == TaskRuntime.Status.SCHEDULED
              && task.getStartTime() + taskAckTimeout < System.currentTimeMillis();
        }
      };
    }
  }
}

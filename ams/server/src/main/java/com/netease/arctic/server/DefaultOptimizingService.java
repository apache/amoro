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

import com.google.common.base.Preconditions;
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
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * DefaultOptimizingService is implementing the OptimizerManager Thrift service, which manages the optimization tasks
 * for ArcticTable. It includes methods for authenticating optimizers, polling tasks from the optimizing queue,
 * acknowledging tasks,and completing tasks. The code uses several data structures, including maps for optimizing queues
 * ,task runtimes, and authenticated optimizers.
 * <p>
 * The code also includes a TimerTask for detecting and removing expired optimizers and suspending tasks.
 */
public class DefaultOptimizingService extends StatedPersistentBase implements OptimizingService.Iface,
    OptimizerManager, QuotaProvider {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizingService.class);
  private static final long POLLING_TIMEOUT_MS = 10000;

  private final long optimizerTouchTimeout;
  private final long taskAckTimeout;
  private final int maxPlanningParallelism;
  private final Map<String, OptimizingQueue> optimizingQueueByGroup = new ConcurrentHashMap<>();
  private final Map<String, OptimizingQueue> optimizingQueueByToken = new ConcurrentHashMap<>();
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();
  private final TableService tableService;
  private final RuntimeHandlerChain tableHandlerChain;
  private final Executor planExecutor;
  private Timer optimizerMonitorTimer;

  public DefaultOptimizingService(Configurations serviceConfig, DefaultTableService tableService) {
    this.optimizerTouchTimeout = serviceConfig.getLong(ArcticManagementConf.OPTIMIZER_HB_TIMEOUT);
    this.taskAckTimeout = serviceConfig.getLong(ArcticManagementConf.OPTIMIZER_TASK_ACK_TIMEOUT);
    this.maxPlanningParallelism = serviceConfig.getInteger(ArcticManagementConf.OPTIMIZER_MAX_PLANNING_PARALLELISM);
    this.tableService = tableService;
    this.tableHandlerChain = new TableRuntimeHandlerImpl();
    this.planExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
      private final AtomicInteger threadId = new AtomicInteger(0);
      @Override
      public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(r, "plan-executor-thread-" + threadId.incrementAndGet());
        thread.setDaemon(true);
        return thread;
      }
    });
  }

  public RuntimeHandlerChain getTableRuntimeHandler() {
    return tableHandlerChain;
  }

  private void loadOptimizingQueues(List<TableRuntimeMeta> tableRuntimeMetaList) {
    List<ResourceGroup> optimizerGroups = getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups);
    List<OptimizerInstance> optimizers = getAs(OptimizerMapper.class, OptimizerMapper::selectAll);
    Map<String, List<TableRuntimeMeta>> groupToTableRuntimes = tableRuntimeMetaList.stream()
        .collect(Collectors.groupingBy(TableRuntimeMeta::getOptimizerGroup));
    optimizerGroups.forEach(group -> {
      String groupName = group.getName();
      List<TableRuntimeMeta> tableRuntimeMetas = groupToTableRuntimes.remove(groupName);
      OptimizingQueue optimizingQueue = new OptimizingQueue(
          tableService,
          group,
          this,
          planExecutor,
          Optional.ofNullable(tableRuntimeMetas).orElseGet(ArrayList::new),
          maxPlanningParallelism);
      optimizingQueueByGroup.put(groupName, optimizingQueue);
    });
    optimizers.forEach(this::registerOptimizer);
    groupToTableRuntimes.keySet().forEach(groupName -> LOG.warn("Unloaded task runtime in group " + groupName));
  }

  private void registerOptimizer(OptimizerInstance optimizer) {
    String token = optimizer.getToken();
    authOptimizers.put(token, optimizer);
    optimizingQueueByToken.put(token, optimizingQueueByGroup.get(optimizer.getGroupName()));
  }

  private void unregisterOptimizer(String token) {
    optimizingQueueByToken.remove(token);
    authOptimizers.remove(token);
  }

  @Override
  public void ping() {
  }

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
    org.apache.iceberg.relocated.com.google.common.base.Preconditions.checkArgument(authToken != null,
        "authToken can not be null");
    return Optional.ofNullable(authOptimizers.get(authToken))
        .orElseThrow(() -> new PluginRetryAuthException("Optimizer has not been authenticated"));
  }

  @Override
  public OptimizingTask pollTask(String authToken, int threadId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Optimizer {} (threadId {}) try polling task", authToken, threadId);
    }
    OptimizingQueue queue = getQueueByToken(authToken);
    return Optional.ofNullable(queue.pollTask(POLLING_TIMEOUT_MS))
        .map(task -> extractOptimizingTask(task,
            getAuthenticatedOptimizer(authToken).getThread(threadId), queue))
        .orElse(null);
  }

  private OptimizingTask extractOptimizingTask(TaskRuntime task,
                                               OptimizerThread optimizerThread,
                                               OptimizingQueue queue) {
    try {
      task.schedule(optimizerThread);
      LOG.info("OptimizerThread {} polled task {}", optimizerThread, task.getTaskId());
      return task.getOptimizingTask();
    } catch (Throwable throwable) {
      LOG.error("Schedule task {} failed, put it to retry queue", task.getTaskId(), throwable);
      queue.retryTask(task, false);
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
    OptimizerThread thread = getAuthenticatedOptimizer(authToken).getThread(taskResult.getThreadId());
    Optional.ofNullable(queue.getTask(taskResult.getTaskId()))
        .orElseThrow(() -> new TaskNotFoundException(taskResult.getTaskId()))
        .complete(thread, taskResult);
  }

  @Override
  public String authenticate(OptimizerRegisterInfo registerInfo) {
    LOG.info("Register optimizer {}.", registerInfo);
    OptimizingQueue queue = getQueueByGroup(registerInfo.getGroupName());
    OptimizerInstance optimizer = new OptimizerInstance(registerInfo, queue.getContainerName());
    doAs(OptimizerMapper.class, mapper -> mapper.insertOptimizer(optimizer));
    registerOptimizer(optimizer);
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
    Preconditions.checkArgument(
        optimizerGroup != null,
        "optimizerGroup can not be null");
    return Optional.ofNullable(optimizingQueueByGroup.get(optimizerGroup));
  }

  private OptimizingQueue getQueueByToken(String token) {
    Preconditions.checkArgument(
        token != null,
        "optimizer token can not be null");
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
    deleteOptimizers.forEach(optimizer -> {
      String token = optimizer.getToken();
      doAs(OptimizerMapper.class, mapper -> mapper.deleteOptimizer(token));
      unregisterOptimizer(token);
    });
  }

  @Override
  public void createResourceGroup(ResourceGroup resourceGroup) {
    doAsTransaction(() -> {
      doAs(ResourceMapper.class, mapper -> mapper.insertResourceGroup(resourceGroup));
      OptimizingQueue optimizingQueue = new OptimizingQueue(
          tableService,
          resourceGroup,
          this,
          planExecutor,
          new ArrayList<>(),
          maxPlanningParallelism);
      optimizingQueueByGroup.put(resourceGroup.getName(), optimizingQueue);
    });
  }

  @Override
  public void deleteResourceGroup(String groupName) {
    if (canDeleteResourceGroup(groupName)) {
      doAs(ResourceMapper.class, mapper -> mapper.deleteResourceGroup(groupName));
      optimizingQueueByGroup.remove(groupName);
    } else {
      throw new RuntimeException(String.format("The resource group %s cannot be deleted because it is currently in " +
          "use.", groupName));
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
    tableHandlerChain.dispose();
    optimizingQueueByGroup.clear();
    optimizingQueueByToken.clear();
    authOptimizers.clear();
  }

  public boolean canDeleteResourceGroup(String name) {
    for (CatalogMeta catalogMeta : tableService.listCatalogMetas()) {
      if (catalogMeta.getCatalogProperties() != null &&
          catalogMeta.getCatalogProperties()
              .getOrDefault(
                  CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.SELF_OPTIMIZING_GROUP,
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
      if (optimizingQueueByGroup.containsKey(name) && optimizingQueueByGroup.get(name).containsTable(identifier)) {
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
        getOptionalQueueByGroup(tableRuntime.getOptimizerGroup()).ifPresent(q -> q.refreshTable(tableRuntime));
      }
    }

    @Override
    public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
      String originalGroup = originalConfig.getOptimizingConfig().getOptimizerGroup();
      if (!tableRuntime.getOptimizerGroup().equals(originalGroup)) {
        getOptionalQueueByGroup(originalGroup).ifPresent(q -> q.releaseTable(tableRuntime));
      }
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup()).ifPresent(q -> q.refreshTable(tableRuntime));
    }

    @Override
    public void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup()).ifPresent(q -> q.refreshTable(tableRuntime));
    }

    @Override
    public void handleTableRemoved(TableRuntime tableRuntime) {
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup()).ifPresent(queue -> queue.releaseTable(tableRuntime));
    }

    @Override
    protected void initHandler(List<TableRuntimeMeta> tableRuntimeMetaList) {
      LOG.info("OptimizerManagementService begin initializing");
      loadOptimizingQueues(tableRuntimeMetaList);
      optimizerMonitorTimer = new Timer("OptimizerMonitor", true);
      optimizerMonitorTimer.schedule(
          new SuspendingDetector(),
          optimizerTouchTimeout,
          ArcticServiceConstants.OPTIMIZER_CHECK_INTERVAL);
      LOG.info("init SuspendingDetector for Optimizer with delay {} ms, interval {} ms", optimizerTouchTimeout,
          ArcticServiceConstants.OPTIMIZER_CHECK_INTERVAL);
      LOG.info("OptimizerManagementService initializing has completed");
    }

    @Override
    protected void doDispose() {
      if (Objects.nonNull(optimizerMonitorTimer)) {
        optimizerMonitorTimer.cancel();
      }
    }
  }

  private class SuspendingDetector extends TimerTask {

    @Override
    public void run() {
      try {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredTokens = authOptimizers.values().stream()
            .filter(optimizer -> currentTime - optimizer.getTouchTime() > optimizerTouchTimeout)
            .map(OptimizerInstance::getToken)
            .collect(Collectors.toSet());

        expiredTokens.forEach(authOptimizers.keySet()::remove);
        if (!expiredTokens.isEmpty()) {
          LOG.info("Expired optimizers: {}", expiredTokens);
        }

        for (OptimizingQueue queue : optimizingQueueByGroup.values()) {
          queue.collectRunningTasks().stream()
            .filter(task -> isTaskExpired(task, currentTime, expiredTokens, authOptimizers.keySet()))
            .forEach(task -> {
              LOG.info("Task {} is suspending, since it's optimizer is expired, put it to retry queue, optimizer {}",
                  task.getTaskId(), task.getResourceDesc());
              //optimizing task of suspending optimizer would not be counted for retrying
              queue.retryTask(task, false);
            });
        }

        expiredTokens.forEach(token -> {
          doAs(OptimizerMapper.class, mapper -> mapper.deleteOptimizer(token));
          unregisterOptimizer(token);
        });
      } catch (RuntimeException e) {
        LOG.error("Update optimizer status abnormal failed. try next round", e);
      }
    }

    private boolean isTaskExpired(TaskRuntime task, long currentTime,
                                  Set<String> expiredTokens, Set<String> authTokens) {
      return task.getStatus() == TaskRuntime.Status.SCHEDULED
          && currentTime - task.getStartTime() > taskAckTimeout ||
          expiredTokens.contains(task.getToken()) ||
          !authTokens.contains(task.getToken());
    }
  }
}

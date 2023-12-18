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
import com.netease.arctic.ams.api.Action;
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
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizerMapper;
import com.netease.arctic.server.persistence.mapper.ResourceMapper;
import com.netease.arctic.server.process.TaskRuntime;
import com.netease.arctic.server.resource.OptimizerInstance;
import com.netease.arctic.server.resource.OptimizerManager;
import com.netease.arctic.server.resource.OptimizerThread;
import com.netease.arctic.server.resource.QuotaProvider;
import com.netease.arctic.server.table.DefaultTableService;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.server.table.TableWatcher;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.base.Predicate;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
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
  private final static Set<Action> MAINTAINING_ACTIONS = Sets.newHashSet(
      Action.REFRESH_SNAPSHOT,
      Action.EXPIRE_SNAPSHOTS,
      Action.CLEAN_ORPHANED_FILES,
      Action.HIVE_COMMIT_SYNC);

  private final long optimizerTouchTimeout;
  private final long taskAckTimeout;
  private final int maxPlanningParallelism;
  private final Map<String, AbstractScheduler<?>> schedulersByGroup = new ConcurrentHashMap<>();
  private final Map<String, AbstractScheduler<?>> schedulersByToken = new ConcurrentHashMap<>();
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();
  private final OptimizerKeeper optimizerKeeper = new OptimizerKeeper();
  private final TableService tableService;

  public DefaultOptimizingService(Configurations serviceConfig, DefaultTableService defaultTableService) {
    optimizerTouchTimeout = serviceConfig.getLong(ArcticManagementConf.OPTIMIZER_HB_TIMEOUT);
    taskAckTimeout = serviceConfig.getLong(ArcticManagementConf.OPTIMIZER_TASK_ACK_TIMEOUT);
    maxPlanningParallelism =
        serviceConfig.getInteger(ArcticManagementConf.OPTIMIZER_MAX_PLANNING_PARALLELISM);
    tableService = defaultTableService;
    tableService.addTableWatcher(new TableRuntimeWatcher());
    installProcessFactories(tableService.listTableRuntimes());
  }

  private void installProcessFactories(List<TableRuntime> tableRuntimeList) {
    List<ResourceGroup> optimizerGroups =
        getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups);
    List<OptimizerInstance> optimizers = getAs(OptimizerMapper.class, OptimizerMapper::selectAll);
    optimizerGroups.forEach(
        group -> {
          String groupName = group.getName();
          if (group.getActions().contains(Action.OPTIMIZING)) {
            OptimizingScheduler optimizingScheduler = new OptimizingScheduler(group, maxPlanningParallelism);
            tableRuntimeList.stream()
                .filter(table -> table.getOptimizerGroup().equals(groupName))
                .forEach(table -> table.register(optimizingScheduler));
          } else if (!Sets.intersection(group.getActions(), MAINTAINING_ACTIONS).isEmpty()) {
            MaintainingScheduler maintainingScheduler = new MaintainingScheduler(group);
            schedulersByGroup.put(groupName, maintainingScheduler);
            tableRuntimeList.forEach(table -> table.register(group.getActions(), maintainingScheduler));
          }
        });
    optimizers.forEach(this::registerOptimizer);
  }

  private void registerOptimizer(OptimizerInstance optimizer) {
    authOptimizers.put(optimizer.getToken(), optimizer);
    schedulersByToken.put(
        optimizer.getToken(), schedulersByGroup.get(optimizer.getGroupName()));
    optimizerKeeper.keepInTouch(optimizer);
  }

  private void unregisterOptimizer(String token) {
    doAs(OptimizerMapper.class, mapper -> mapper.deleteOptimizer(token));
    schedulersByToken.remove(token);
    authOptimizers.remove(token);
  }

  @Override
  public void ping() {}

  public List<TaskRuntime<?, ?>> listTasks(String optimizerGroup) {
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
    AbstractScheduler<?> scheduler = getSchedulerByToken(authToken);
    return Optional.ofNullable(scheduler.scheduleTask())
        .map(
            task ->
                extractOptimizingTask(
                    task, getAuthenticatedOptimizer(authToken).getThread(threadId)))
        .orElse(null);
  }

  private OptimizingTask extractOptimizingTask(TaskRuntime<?, ?> task, OptimizerThread optimizerThread) {
    try {
      task.schedule(optimizerThread);
      LOG.info("OptimizerThread {} polled task {}", optimizerThread, task.getTaskId());
      return task.formatTask();
    } catch (Throwable throwable) {
      LOG.error("Schedule task {} failed, put it to retry queue", task.getTaskId(), throwable);
      task.reset();
      return null;
    }
  }

  @Override
  public void ackTask(String authToken, int threadId, OptimizingTaskId taskId) {
    LOG.info("Ack task {} by optimizer {} (threadId {})", taskId, authToken, threadId);
    AbstractScheduler<?> scheduler = getSchedulerByToken(authToken);
    Optional.ofNullable(scheduler.getTask(taskId))
        .orElseThrow(() -> new TaskNotFoundException(taskId))
        .ack(getAuthenticatedOptimizer(authToken).getThread(threadId));
  }

  @Override
  public void completeTask(String authToken, OptimizingTaskResult taskResult) {
    LOG.info("Optimizer {} complete task {}", authToken, taskResult.getTaskId());
    AbstractScheduler<?> scheduler = getSchedulerByToken(authToken);
    OptimizerThread thread =
        getAuthenticatedOptimizer(authToken).getThread(taskResult.getThreadId());
    Optional.ofNullable(scheduler.getTask(taskResult.getTaskId()))
        .orElseThrow(() -> new TaskNotFoundException(taskResult.getTaskId()))
        .complete(thread, taskResult);
  }

  @Override
  public String authenticate(OptimizerRegisterInfo registerInfo) {
    LOG.info("Register optimizer {}.", registerInfo);
    AbstractScheduler<?> scheduler = getQueueByGroup(registerInfo.getGroupName());
    OptimizerInstance optimizer = new OptimizerInstance(registerInfo, scheduler.getContainerName());
    doAs(OptimizerMapper.class, mapper -> mapper.insertOptimizer(optimizer));
    registerOptimizer(optimizer);
    return optimizer.getToken();
  }

  /**
   * Get optimizing queue.
   *
   * @return OptimizeQueueItem
   */
  private AbstractScheduler<?> getQueueByGroup(String optimizerGroup) {
    return getOptionalQueueByGroup(optimizerGroup)
        .orElseThrow(() -> new ObjectNotExistsException("Optimizer group " + optimizerGroup));
  }

  private Optional<AbstractScheduler<?>> getOptionalQueueByGroup(String optimizerGroup) {
    Preconditions.checkArgument(optimizerGroup != null, "optimizerGroup can not be null");
    return Optional.ofNullable(schedulersByGroup.get(optimizerGroup));
  }

  private AbstractScheduler<?> getSchedulerByToken(String token) {
    Preconditions.checkArgument(token != null, "optimizer token can not be null");
    return Optional.ofNullable(schedulersByToken.get(token))
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
          OptimizingScheduler defaultOptimizingScheduler =
              new OptimizingScheduler(
                  resourceGroup,
                  maxPlanningParallelism);
          schedulersByGroup.put(resourceGroup.getName(), defaultOptimizingScheduler);
        });
  }

  @Override
  public void deleteResourceGroup(String groupName) {
    if (canDeleteResourceGroup(groupName)) {
      doAs(ResourceMapper.class, mapper -> mapper.deleteResourceGroup(groupName));
      schedulersByGroup.remove(groupName);
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
    Optional.ofNullable(schedulersByGroup.get(resourceGroup.getName()))
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
    schedulersByGroup.clear();
    schedulersByToken.clear();
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
      if (schedulersByGroup.containsKey(name)
          && schedulersByGroup.get(name).containsTable(identifier)) {
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

  private class TableRuntimeWatcher implements TableWatcher {

    @Override
    public void tableChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
      String originalGroup = originalConfig.getOptimizingConfig().getOptimizerGroup();
      if (!tableRuntime.getOptimizerGroup().equals(originalGroup)) {
        getOptionalQueueByGroup(originalGroup).ifPresent(q -> q.releaseTable(tableRuntime));
      }
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup())
          .ifPresent(q -> q.refreshTable(tableRuntime));
    }

    @Override
    public void start() {
      optimizerKeeper.start();
    }

    @Override
    public void tableAdded(TableRuntime tableRuntime, AmoroTable<?> table) {
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup())
          .ifPresent(q -> q.refreshTable(tableRuntime));
    }

    @Override
    public void tableRemoved(TableRuntime tableRuntime) {
      getOptionalQueueByGroup(tableRuntime.getOptimizerGroup())
          .ifPresent(queue -> queue.releaseTable(tableRuntime));
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

    public AbstractScheduler<?> getScheduler() {
      return schedulersByGroup.get(optimizerInstance.getGroupName());
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
          Optional.ofNullable(keepingTask.getScheduler())
              .ifPresent(
                  scheduler ->
                      scheduler
                          .collectTasks(buildSuspendingPredication(token, isExpired))
                          .forEach(this::retryTask));
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

    private void retryTask(TaskRuntime<?, ?> task) {
      LOG.info(
          "Task {} is suspending, since it's optimizer is expired, put it to retry queue, optimizer {}",
          task.getTaskId(),
          task.getResourceDesc());
      // optimizing task of suspending optimizer would not be counted for retrying
      task.reset();
    }

    private Predicate<TaskRuntime<?, ?>> buildSuspendingPredication(
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

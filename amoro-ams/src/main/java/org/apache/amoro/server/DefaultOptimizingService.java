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
import org.apache.amoro.TableRuntime;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.api.OptimizingService;
import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.exception.ForbiddenException;
import org.apache.amoro.exception.IllegalTaskStateException;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.exception.PluginRetryAuthException;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceContainer;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.resource.ResourceType;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.model.OptimizerResourceInfo;
import org.apache.amoro.server.ha.HighAvailabilityContainer;
import org.apache.amoro.server.manager.AbstractOptimizerContainer;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingQueue;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.optimizing.dra.DynamicAllocationConfig;
import org.apache.amoro.server.optimizing.dra.DynamicAllocationMetrics;
import org.apache.amoro.server.optimizing.dra.DynamicAllocationState;
import org.apache.amoro.server.optimizing.dra.PendingRegistrations;
import org.apache.amoro.server.persistence.StatedPersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.ResourceMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.resource.Containers;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

  private final long groupMinParallelismCheckInterval;
  private final int groupMaxKeepingAttempts;
  private final long optimizerTouchTimeout;
  private final long taskAckTimeout;
  private final long taskExecuteTimeout;
  private final int maxPlanningParallelism;
  private final long pollingTimeout;
  private final boolean breakQuotaLimit;
  private final long refreshGroupInterval;
  private final Map<String, OptimizingQueue> optimizingQueueByGroup = new ConcurrentHashMap<>();
  private final Map<String, OptimizingQueue> optimizingQueueByToken = new ConcurrentHashMap<>();
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();

  /**
   * Tokens of draining optimizers (AIP-5 scale-down): {@link #pollTask} returns {@code null} for
   * them, blocking new assignments while in-flight tasks complete normally.
   */
  private final Set<String> pendingRemovalTokens = ConcurrentHashMap.newKeySet();

  /** Force-removal deadline per draining token, the {@code drain-timeout} safety net. */
  private final Map<String, Long> drainDeadlines = new ConcurrentHashMap<>();

  private final OptimizerKeeper optimizerKeeper = new OptimizerKeeper("optimizer-keeper-thread");
  private final OptimizerGroupKeeper optimizerGroupKeeper =
      new OptimizerGroupKeeper("optimizer-group-keeper-thread");
  private final OptimizerScaleKeeper optimizerScaleKeeper =
      new OptimizerScaleKeeper("optimizer-scale-keeper-thread");
  private final OptimizingConfigWatcher optimizingConfigWatcher = new OptimizingConfigWatcher();
  private final CatalogManager catalogManager;
  private final OptimizerManager optimizerManager;
  private final TableService tableService;
  private final RuntimeHandlerChain tableHandlerChain;
  private final ExecutorService planExecutor;
  private final BucketAssignStore bucketAssignStore;
  private final HighAvailabilityContainer haContainer;
  private final boolean isMasterSlaveMode;

  public DefaultOptimizingService(
      Configurations serviceConfig,
      CatalogManager catalogManager,
      OptimizerManager optimizerManager,
      TableService tableService,
      BucketAssignStore bucketAssignStore,
      HighAvailabilityContainer haContainer) {
    this.optimizerTouchTimeout =
        serviceConfig.getDurationInMillis(AmoroManagementConf.OPTIMIZER_HB_TIMEOUT);
    this.taskAckTimeout =
        serviceConfig.getDurationInMillis(AmoroManagementConf.OPTIMIZER_TASK_ACK_TIMEOUT);
    this.taskExecuteTimeout =
        serviceConfig.getDurationInMillis(AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT);
    this.refreshGroupInterval =
        serviceConfig.getDurationInMillis(AmoroManagementConf.OPTIMIZING_REFRESH_GROUP_INTERVAL);
    this.maxPlanningParallelism =
        serviceConfig.getInteger(AmoroManagementConf.OPTIMIZER_MAX_PLANNING_PARALLELISM);
    this.pollingTimeout =
        serviceConfig.getDurationInMillis(AmoroManagementConf.OPTIMIZER_POLLING_TIMEOUT);
    this.breakQuotaLimit =
        serviceConfig.getBoolean(AmoroManagementConf.OPTIMIZING_BREAK_QUOTA_LIMIT_ENABLED);
    this.groupMinParallelismCheckInterval =
        serviceConfig.getDurationInMillis(
            AmoroManagementConf.OPTIMIZER_GROUP_MIN_PARALLELISM_CHECK_INTERVAL);
    this.groupMaxKeepingAttempts =
        serviceConfig.getInteger(AmoroManagementConf.OPTIMIZER_GROUP_MAX_KEEPING_ATTEMPTS);
    this.tableService = tableService;
    this.catalogManager = catalogManager;
    this.optimizerManager = optimizerManager;
    this.bucketAssignStore = bucketAssignStore;
    this.haContainer = haContainer;
    this.isMasterSlaveMode =
        haContainer != null
            && serviceConfig.getBoolean(AmoroManagementConf.HA_USE_MASTER_SLAVE_MODE);
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

  private void loadOptimizingQueues(List<DefaultTableRuntime> tableRuntimeList) {
    List<ResourceGroup> optimizerGroups =
        getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups);
    List<OptimizerInstance> optimizers = getAs(OptimizerMapper.class, OptimizerMapper::selectAll);
    Map<String, List<DefaultTableRuntime>> groupToTableRuntimes =
        tableRuntimeList.stream().collect(Collectors.groupingBy(TableRuntime::getGroupName));
    optimizerGroups.forEach(
        group -> {
          String groupName = group.getName();
          List<DefaultTableRuntime> tableRuntimes = groupToTableRuntimes.remove(groupName);
          OptimizingQueue optimizingQueue =
              new OptimizingQueue(
                  catalogManager,
                  group,
                  this,
                  planExecutor,
                  Optional.ofNullable(tableRuntimes).orElseGet(ArrayList::new),
                  maxPlanningParallelism);
          optimizingQueueByGroup.put(groupName, optimizingQueue);
          optimizerGroupKeeper.keepInTouch(groupName, 1);
          optimizerScaleKeeper.watch(group);
        });
    registerOptimizers(optimizers);
    // Avoid keeping the tables in processing/pending status forever in below cases:
    // 1) Resource group does not exist
    // 2) The AMS restarts after the tables disable self-optimizing but before the optimizing
    // process is closed, which may cause the optimizing status of the tables to be still
    // PLANNING/PENDING after AMS is restarted.
    groupToTableRuntimes.forEach(
        (groupName, trs) -> {
          trs.stream()
              .filter(
                  tr ->
                      tr.getOptimizingStatus() == OptimizingStatus.PLANNING
                          || tr.getOptimizingStatus() == OptimizingStatus.PENDING)
              .forEach(
                  tr -> {
                    LOG.warn(
                        "Release {} optimizing process for table {}, since its resource group {} does not exist",
                        tr.getOptimizingStatus().name(),
                        tr.getTableIdentifier(),
                        groupName);
                    tr.completeEmptyProcess();
                  });
        });
  }

  private void registerOptimizer(OptimizerInstance optimizer, boolean needPersistent) {
    if (needPersistent) {
      doAsTransaction(
          () -> {
            String groupName =
                getAs(
                    ResourceMapper.class,
                    mapper -> mapper.selectResourceGroupNameForUpdate(optimizer.getGroupName()));
            if (groupName == null) {
              throw new ObjectNotExistsException("Optimizer group " + optimizer.getGroupName());
            }
            doAs(OptimizerMapper.class, mapper -> mapper.insertOptimizer(optimizer));
          });
    }

    OptimizingQueue optimizingQueue = optimizingQueueByGroup.get(optimizer.getGroupName());
    optimizingQueue.addOptimizer(optimizer);
    authOptimizers.put(optimizer.getToken(), optimizer);
    optimizingQueueByToken.put(optimizer.getToken(), optimizingQueue);
    optimizerKeeper.keepInTouch(optimizer);
    optimizerScaleKeeper.onOptimizerRegistered(optimizer);
  }

  /**
   * Registers optimizers recovered from persistence at startup. A missing local queue may be a
   * stale snapshot in an HA deployment, so a non-empty optimizer group is removed only when its
   * persisted resource group is also absent. Empty group names are always treated as orphaned.
   */
  void registerOptimizers(List<OptimizerInstance> optimizers) {
    for (OptimizerInstance optimizer : optimizers) {
      String groupName = optimizer.getGroupName();
      if (groupName != null
          && !groupName.isEmpty()
          && optimizingQueueByGroup.containsKey(groupName)) {
        registerOptimizer(optimizer, false);
      } else {
        long deleted =
            updateAs(
                OptimizerMapper.class,
                mapper -> mapper.deleteOptimizerIfResourceGroupAbsent(optimizer.getToken()));
        if (deleted == 1) {
          LOG.warn(
              "Remove orphan optimizer {}, its resource group {} does not exist",
              optimizer.getToken(),
              groupName);
        } else {
          LOG.warn(
              "Skip recovering optimizer {} due to a stale local resource group snapshot:"
                  + " group {} is unavailable locally, but keep its shared record because the"
                  + " resource group still exists",
              optimizer.getToken(),
              groupName);
        }
      }
    }
  }

  private void unregisterOptimizer(String token) {
    doAs(OptimizerMapper.class, mapper -> mapper.deleteOptimizer(token));
    OptimizingQueue optimizingQueue = optimizingQueueByToken.remove(token);
    OptimizerInstance optimizer = authOptimizers.remove(token);
    if (optimizer != null) {
      if (optimizingQueue == null) {
        optimizingQueue = optimizingQueueByGroup.get(optimizer.getGroupName());
      }
      if (optimizingQueue != null) {
        optimizingQueue.removeOptimizer(optimizer);
      }
    }
    // An optimizer that dies mid-drain is unregistered here by heartbeat expiry; its token can
    // never be matched again, so leftover drain state would sit in the pending-removal set
    // forever (its replacement pod registers under a fresh token).
    cancelDrain(token);
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
    if (pendingRemovalTokens.contains(authToken)) {
      return null;
    }
    LOG.debug("Optimizer {} (threadId {}) try polling task", authToken, threadId);
    OptimizerThread optimizerThread = getAuthenticatedOptimizer(authToken).getThread(threadId);
    OptimizingQueue queue = getQueueByToken(authToken);
    TaskRuntime<?> task =
        guardDrainedPoll(
            authToken, queue.pollTask(optimizerThread, pollingTimeout, breakQuotaLimit));
    if (task != null) {
      LOG.info("OptimizerThread {} polled task {}", optimizerThread, task.getTaskId());
      return task.extractProtocolTask();
    }
    return null;
  }

  /**
   * Close the long-poll race on drain start: the entry check above cannot stop a thread already
   * parked inside the queue's poll, which may fetch a task after its token entered the
   * pending-removal set. Hand such a task back instead of assigning it to a draining optimizer.
   */
  @VisibleForTesting
  TaskRuntime<?> guardDrainedPoll(String authToken, TaskRuntime<?> task) {
    if (task == null || !pendingRemovalTokens.contains(authToken)) {
      return task;
    }
    OptimizingQueue queue = optimizingQueueByToken.get(authToken);
    if (queue != null) {
      try {
        queue.retryTask(task);
      } catch (Exception e) {
        // The existing suspending-task safety net will still reclaim it after the removal.
        LOG.warn(
            "Failed to hand back task {} from draining optimizer {}",
            task.getTaskId(),
            authToken,
            e);
      }
    }
    return null;
  }

  /** Block new task assignments to the token; in-flight tasks keep completing normally. */
  void beginGracefulDrain(String token, long deadlineMs) {
    drainDeadlines.put(token, deadlineMs);
    pendingRemovalTokens.add(token);
    LOG.info("Optimizer {} begins graceful drain", token);
  }

  /** Re-admit the token to task assignment, e.g. when dynamic allocation is disabled mid-drain. */
  void cancelDrain(String token) {
    pendingRemovalTokens.remove(token);
    drainDeadlines.remove(token);
  }

  @VisibleForTesting
  boolean isDraining(String token) {
    return pendingRemovalTokens.contains(token);
  }

  /**
   * Run one dynamic-allocation round for the group at an injected time. The production cadence is
   * driven by the scale keeper's delay queue with the wall clock; tests inject times because the
   * validated minimum {@code executor-idle-timeout} (30s) puts real idle waits beyond sane test
   * durations.
   */
  @VisibleForTesting
  void evaluateDynamicAllocation(String groupName, long nowMs) {
    ResourceGroup resourceGroup = optimizerManager.getResourceGroup(groupName);
    OptimizingQueue queue = optimizingQueueByGroup.get(groupName);
    optimizerScaleKeeper.scaleIfNeeded(
        resourceGroup, queue, DynamicAllocationConfig.parse(resourceGroup), nowMs);
  }

  /**
   * Remove a drained optimizer: release the container resource, delete the persisted resource row,
   * and unregister. A missing resource row (the pod self-registered after a persist failure, or a
   * manual release raced the row away) is not an error — the instance itself carries the
   * container-side identity, so release through it and only skip the row delete; treating this as a
   * retryable failure would loop forever on a pod whose row can never reappear. A container release
   * failure keeps the drain state so a later round retries the idempotent deletion.
   */
  void executeRemoval(String token) {
    OptimizerInstance optimizer = authOptimizers.get(token);
    if (optimizer == null || optimizer.getResourceId() == null) {
      // Already unregistered, or externally launched: nothing for AMS to release.
      cancelDrain(token);
      return;
    }
    try {
      Resource resource = optimizerManager.getResource(optimizer.getResourceId());
      if (resource != null) {
        resource.getProperties().putAll(optimizer.getProperties());
        ((AbstractOptimizerContainer) Containers.get(resource.getContainerName()))
            .releaseResource(resource);
        optimizerManager.deleteResource(optimizer.getResourceId());
      } else {
        ((AbstractOptimizerContainer) Containers.get(optimizer.getContainerName()))
            .releaseResource(optimizer);
      }
    } catch (Throwable t) {
      LOG.warn(
          "Failed to release optimizer {} (resource {}), will retry",
          token,
          optimizer.getResourceId(),
          t);
      return;
    }
    unregisterOptimizer(token);
    LOG.info("Optimizer {} (resource {}) removed by scale-down", token, optimizer.getResourceId());
  }

  @Override
  public void ackTask(String authToken, int threadId, OptimizingTaskId taskId) {
    LOG.info("Ack task {} by optimizer {} (threadId {})", taskId, authToken, threadId);
    OptimizingQueue queue = getQueueByToken(authToken);
    queue.ackTask(taskId, getAuthenticatedOptimizer(authToken).getThread(threadId));
  }

  @Override
  public void completeTask(String authToken, OptimizingTaskResult taskResult) {
    LOG.info(
        "Optimizer {} (threadId {}) complete task {} (status: {})",
        authToken,
        taskResult.getThreadId(),
        taskResult.getTaskId(),
        taskResult.getErrorMessage() == null ? "SUCCESS" : "FAIL");
    OptimizingQueue queue = getQueueByToken(authToken);
    OptimizerThread thread =
        getAuthenticatedOptimizer(authToken).getThread(taskResult.getThreadId());
    queue.completeTask(thread, taskResult);
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
  public boolean cancelProcess(long processId) {
    TableProcessMeta processMeta =
        getAs(TableProcessMapper.class, m -> m.getProcessMeta(processId));
    if (processMeta == null) {
      return false;
    }
    long tableId = processMeta.getTableId();
    DefaultTableRuntime tableRuntime = (DefaultTableRuntime) tableService.getRuntime(tableId);
    if (tableRuntime == null) {
      return false;
    }
    OptimizingProcess process = tableRuntime.getOptimizingProcess();
    if (process == null || process.getProcessId() != processId) {
      return false;
    }
    process.close(true);
    return true;
  }

  @Override
  public List<String> getOptimizingNodeUrls() {
    if (bucketAssignStore == null) {
      return Collections.emptyList();
    }
    try {
      List<AmsServerInfo> nodes = bucketAssignStore.getAliveNodes();
      List<String> urls = new ArrayList<>(nodes.size());
      for (AmsServerInfo node : nodes) {
        if (node.getHost() != null
            && node.getThriftBindPort() != null
            && node.getThriftBindPort() > 0) {
          urls.add(String.format("thrift://%s:%d", node.getHost(), node.getThriftBindPort()));
        }
      }
      return urls;
    } catch (Exception e) {
      LOG.warn("Failed to get optimizing node URLs from bucket assign store", e);
      return Collections.emptyList();
    }
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
          String groupName = resourceGroup.getName();
          optimizingQueueByGroup.put(groupName, optimizingQueue);
          optimizerGroupKeeper.keepInTouch(groupName, 1);
          optimizerScaleKeeper.watch(resourceGroup);
        });
  }

  public void deleteResourceGroup(String groupName) {
    OptimizingQueue optimizingQueue = optimizingQueueByGroup.remove(groupName);
    optimizingQueue.dispose();
    optimizerScaleKeeper.onGroupDeleted(groupName);
  }

  public void updateResourceGroup(ResourceGroup resourceGroup) {
    Optional.ofNullable(optimizingQueueByGroup.get(resourceGroup.getName()))
        .ifPresent(queue -> queue.updateOptimizerGroup(resourceGroup));
    optimizerScaleKeeper.watch(resourceGroup);
  }

  @VisibleForTesting
  int pendingScaleThreads(String groupName) {
    return optimizerScaleKeeper.pendingThreads(groupName);
  }

  public void dispose() {
    planExecutor.shutdown();
    // shutdown sync group first, stop syncing group
    optimizingConfigWatcher.dispose();
    // dispose all queues
    optimizingQueueByGroup.values().forEach(OptimizingQueue::dispose);
    optimizerKeeper.dispose();
    optimizerGroupKeeper.dispose();
    optimizerScaleKeeper.dispose();
    tableHandlerChain.dispose();
    optimizingQueueByGroup.clear();
    optimizingQueueByToken.clear();
    authOptimizers.clear();
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
      DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
      if (!defaultTableRuntime.getOptimizingStatus().isProcessing()) {
        getOptionalQueueByGroup(defaultTableRuntime.getGroupName())
            .ifPresent(q -> q.refreshTable(defaultTableRuntime));
      }
    }

    @Override
    public void handleConfigChanged(TableRuntime runtime, TableConfiguration originalConfig) {
      DefaultTableRuntime tableRuntime = (DefaultTableRuntime) runtime;
      String originalGroup = originalConfig.getOptimizingConfig().getOptimizerGroup();
      Optional<OptimizingQueue> newQueue = getOptionalQueueByGroup(tableRuntime.getGroupName());
      if (!tableRuntime.getGroupName().equals(originalGroup)) {
        getOptionalQueueByGroup(originalGroup).ifPresent(q -> q.releaseTable(tableRuntime));
        // If the new group doesn't exist, close the process to avoid the table in limbo(PENDING)
        // status.
        if (newQueue.isEmpty()) {
          LOG.warn(
              "Cannot find the resource group: {}, try to release optimizing process of table {} directly",
              tableRuntime.getGroupName(),
              tableRuntime.getTableIdentifier());
          tableRuntime.completeEmptyProcess();
        }
      }

      // Binding new queue if the new group exists
      newQueue.ifPresent(q -> q.refreshTable(tableRuntime));
    }

    @Override
    public void handleTableAdded(AmoroTable<?> table, TableRuntime runtime) {
      DefaultTableRuntime tableRuntime = (DefaultTableRuntime) runtime;
      getOptionalQueueByGroup(tableRuntime.getGroupName())
          .ifPresent(q -> q.refreshTable(tableRuntime));
    }

    @Override
    public void handleTableRemoved(TableRuntime runtime) {
      DefaultTableRuntime tableRuntime = (DefaultTableRuntime) runtime;
      getOptionalQueueByGroup(tableRuntime.getGroupName())
          .ifPresent(queue -> queue.releaseTable(tableRuntime));
    }

    @Override
    protected void initHandler(List<TableRuntime> tableRuntimeList) {
      LOG.info("OptimizerManagementService begin initializing");
      loadOptimizingQueues(
          tableRuntimeList.stream()
              .filter(t -> t instanceof DefaultTableRuntime)
              .map(t -> (DefaultTableRuntime) t)
              .collect(Collectors.toList()));
      optimizerKeeper.start();
      optimizerGroupKeeper.start();
      optimizerScaleKeeper.start();
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

  protected abstract class AbstractKeeper<T extends Delayed> implements Runnable {
    protected volatile boolean stopped = false;
    protected final Thread thread = new Thread(this);
    protected final DelayQueue<T> suspendingQueue = new DelayQueue<>();

    public AbstractKeeper(String threadName) {
      thread.setName(threadName);
      thread.setDaemon(true);
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
      // Use 1/4 of optimizerTouchTimeout as sync interval (default ~30 seconds), used for
      // master-slave follower sync.
      long syncInterval = Math.max(5000, optimizerTouchTimeout / 4);
      // In non-master-slave mode, this node is always the leader.
      boolean wasLeader = !isMasterSlaveMode;
      while (!stopped) {
        try {
          boolean isLeader = !isMasterSlaveMode || haContainer.hasLeadership();
          if (!wasLeader && isLeader) {
            // Follower → Leader transition: subclass takes over monitoring of inherited optimizers.
            onBecomeLeader();
          }
          wasLeader = isLeader;

          if (isLeader) {
            T keepingTask = suspendingQueue.take();
            this.processTask(keepingTask);
          } else {
            // Not leader: let subclass handle follower state (e.g. sync optimizer list from DB)
            onFollowerTick(syncInterval);
          }
        } catch (InterruptedException ignored) {
        } catch (Throwable t) {
          if (!stopped) {
            LOG.error("{} has encountered a problem.", this.getClass().getSimpleName(), t);
          }
        }
      }
    }

    protected abstract void processTask(T task) throws Exception;

    protected void onFollowerTick(long syncInterval) throws InterruptedException {
      Thread.sleep(syncInterval);
    }

    protected void onBecomeLeader() {}
  }

  private class OptimizerKeeper extends AbstractKeeper<OptimizerKeepingTask> {

    public OptimizerKeeper(String threadName) {
      super(threadName);
    }

    public void keepInTouch(OptimizerInstance optimizerInstance) {
      Preconditions.checkNotNull(optimizerInstance, "token can not be null");
      suspendingQueue.add(new OptimizerKeepingTask(optimizerInstance));
    }

    @Override
    protected void processTask(OptimizerKeepingTask keepingTask) {
      String token = keepingTask.getToken();
      boolean isExpired = !keepingTask.tryKeeping();
      if (isExpired) {
        LOG.info("Optimizer {} has been expired, unregister it", keepingTask.getOptimizer());
        unregisterOptimizer(token);
      }
      Optional.ofNullable(keepingTask.getQueue())
          .ifPresent(
              queue ->
                  queue
                      .collectTasks(buildSuspendingPredication(authOptimizers.keySet()))
                      .forEach(task -> retryTask(task, queue)));
      if (!isExpired) {
        LOG.debug("Optimizer {} is being touched, keep it", keepingTask.getOptimizer());
        keepInTouch(keepingTask.getOptimizer());
      }
    }

    @Override
    protected void onFollowerTick(long syncInterval) throws InterruptedException {
      loadOptimizersFromDatabase();
      Thread.sleep(syncInterval);
    }

    @Override
    protected void onBecomeLeader() {
      LOG.info(
          "Became leader, starting heartbeat monitoring for {} inherited optimizers",
          authOptimizers.size());
      // All optimizers in authOptimizers were loaded from DB by the follower sync loop.
      // Their touchTime reflects the latest DB-persisted heartbeat, which is the correct
      // baseline for the new leader's expiry detection.
      authOptimizers.values().forEach(this::keepInTouch);
    }

    /**
     * Load optimizer information from database. This is used in master-slave mode for follower
     * nodes to sync optimizer state from database. This method performs incremental updates by
     * comparing database state with local authOptimizers, only adding new optimizers and removing
     * missing ones.
     */
    private void loadOptimizersFromDatabase() {
      try {
        List<OptimizerInstance> dbOptimizers =
            getAs(OptimizerMapper.class, OptimizerMapper::selectAll);

        Map<String, OptimizerInstance> dbOptimizersByToken = new HashMap<>();
        for (OptimizerInstance optimizer : dbOptimizers) {
          String token = optimizer.getToken();
          if (token != null) {
            dbOptimizersByToken.put(token, optimizer);
          }
        }

        Set<String> localTokens = new HashSet<>(authOptimizers.keySet());
        Set<String> dbTokens = new HashSet<>(dbOptimizersByToken.keySet());
        Set<String> tokensToAdd = new HashSet<>(dbTokens);
        tokensToAdd.removeAll(localTokens);

        Set<String> tokensToRemove = new HashSet<>(localTokens);
        tokensToRemove.removeAll(dbTokens);

        for (String token : tokensToAdd) {
          OptimizerInstance optimizer = dbOptimizersByToken.get(token);
          if (optimizer != null) {
            registerOptimizerWithoutPersist(optimizer);
            LOG.debug("Added optimizer {} from database", token);
          }
        }

        for (String token : tokensToRemove) {
          removeOptimizerFromLocal(token);
          LOG.debug("Removed optimizer {} (not in database)", token);
        }

        LOG.debug(
            "Synced optimizers from database: total={}, added={}, removed={}, current={}",
            dbOptimizersByToken.size(),
            tokensToAdd.size(),
            tokensToRemove.size(),
            authOptimizers.size());
      } catch (Exception e) {
        LOG.error("Failed to load optimizers from database", e);
      }
    }

    private void registerOptimizerWithoutPersist(OptimizerInstance optimizer) {
      OptimizingQueue optimizingQueue = optimizingQueueByGroup.get(optimizer.getGroupName());
      if (optimizingQueue == null) {
        LOG.warn(
            "Cannot register optimizer {}: optimizing queue for group {} not found",
            optimizer.getToken(),
            optimizer.getGroupName());
        return;
      }
      optimizingQueue.addOptimizer(optimizer);
      authOptimizers.put(optimizer.getToken(), optimizer);
      optimizingQueueByToken.put(optimizer.getToken(), optimizingQueue);
    }

    private void removeOptimizerFromLocal(String token) {
      OptimizingQueue optimizingQueue = optimizingQueueByToken.remove(token);
      OptimizerInstance optimizer = authOptimizers.remove(token);
      if (optimizingQueue != null && optimizer != null) {
        optimizingQueue.removeOptimizer(optimizer);
      }
    }

    private void retryTask(TaskRuntime<?> task, OptimizingQueue queue) {
      if (isTaskExecTimeout(task)) {
        LOG.warn(
            "Task {} has been suspended in ACK state for {} (start time: {}), put it to retry queue, optimizer {}. (Note: The task may have finished executing, but ams did not receive the COMPLETE message from the optimizer.)",
            task.getTaskId(),
            Duration.ofMillis(taskExecuteTimeout),
            task.getStartTime(),
            task.getResourceDesc());
      } else {
        LOG.info(
            "Task {} is suspending, since it's optimizer is expired, put it to retry queue, optimizer {}",
            task.getTaskId(),
            task.getResourceDesc());
      }
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
                  && task.getStartTime() + taskAckTimeout < System.currentTimeMillis()
              || isTaskExecTimeout(task);
    }
  }

  private boolean isTaskExecTimeout(TaskRuntime<?> task) {
    return task.getStatus() == TaskRuntime.Status.ACKED
        && taskExecuteTimeout > 0
        && task.getStartTime() + taskExecuteTimeout < System.currentTimeMillis();
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

  private class OptimizerGroupKeepingTask implements Delayed {

    private final String groupName;
    private final long lastCheckTime;
    private final int attempts;

    public OptimizerGroupKeepingTask(String groupName, int attempts) {
      this.groupName = groupName;
      this.lastCheckTime = System.currentTimeMillis();
      this.attempts = attempts;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
      return unit.convert(
          lastCheckTime + groupMinParallelismCheckInterval * attempts - System.currentTimeMillis(),
          TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
      OptimizerGroupKeepingTask another = (OptimizerGroupKeepingTask) o;
      return Long.compare(lastCheckTime, another.lastCheckTime);
    }

    public int getMinParallelism(ResourceGroup resourceGroup) {
      return DynamicAllocationConfig.resolveMinParallelism(resourceGroup);
    }

    public int tryKeeping(ResourceGroup resourceGroup) {
      List<OptimizerInstance> optimizers = optimizerManager.listOptimizers(groupName);
      OptimizerResourceInfo optimizerResourceInfo = new OptimizerResourceInfo();
      optimizers.forEach(
          e -> {
            optimizerResourceInfo.addOccupationCore(e.getThreadCount());
            optimizerResourceInfo.addOccupationMemory(e.getMemoryMb());
          });
      return getMinParallelism(resourceGroup) - optimizerResourceInfo.getOccupationCore();
    }

    public ResourceGroup getResourceGroup() {
      OptimizingQueue optimizingQueue = optimizingQueueByGroup.get(groupName);
      if (optimizingQueue == null) {
        return null;
      }
      return optimizingQueue.getOptimizerGroup();
    }

    public String getGroupName() {
      return groupName;
    }

    public int getAttempts() {
      return attempts;
    }
  }

  /**
   * Optimizer group keeper thread responsible for monitoring resource group status and
   * automatically maintaining optimizer resources.
   */
  private class OptimizerGroupKeeper extends AbstractKeeper<OptimizerGroupKeepingTask> {

    public OptimizerGroupKeeper(String threadName) {
      super(threadName);
    }

    public void keepInTouch(String groupName, int attempts) {
      Preconditions.checkNotNull(groupName, "groupName can not be null");
      Preconditions.checkArgument(attempts > 0, "attempts must be greater than 0");
      if (this.stopped) {
        return;
      }
      suspendingQueue.add(new OptimizerGroupKeepingTask(groupName, attempts));
    }

    @Override
    protected void processTask(OptimizerGroupKeepingTask keepingTask) {
      ResourceGroup resourceGroup = keepingTask.getResourceGroup();
      if (resourceGroup == null) {
        LOG.warn(
            "ResourceGroup:{} may have been deleted, stop keeping it", keepingTask.getGroupName());
        return;
      }

      if (DynamicAllocationConfig.isEffectivelyEnabled(resourceGroup)) {
        // Dynamic allocation owns this group's floor and demand scaling (see
        // OptimizerScaleKeeper); keep watching in case it is disabled later.
        keepInTouch(resourceGroup.getName(), 1);
        return;
      }

      int requiredCores = keepingTask.tryKeeping(resourceGroup);
      if (requiredCores <= 0) {
        LOG.debug(
            "The Resource Group:{} has sufficient resources, keep it", resourceGroup.getName());
        keepInTouch(resourceGroup.getName(), 1);
        return;
      }

      if (keepingTask.getAttempts() > groupMaxKeepingAttempts) {
        int minParallelism = keepingTask.getMinParallelism(resourceGroup);
        LOG.warn(
            "Resource Group:{}, creating optimizer {} times in a row, optimizers still below min-parallel:{}, will reset min-parallel to {}",
            resourceGroup.getName(),
            keepingTask.getAttempts(),
            minParallelism,
            minParallelism - requiredCores);
        resourceGroup
            .getProperties()
            .put(
                DynamicAllocationConfig.effectiveMinParallelismKey(resourceGroup),
                String.valueOf(minParallelism - requiredCores));
        updateResourceGroup(resourceGroup);
        optimizerManager.updateResourceGroup(resourceGroup);
        keepInTouch(resourceGroup.getName(), 1);
        return;
      }

      Resource resource =
          new Resource.Builder(
                  resourceGroup.getContainer(), resourceGroup.getName(), ResourceType.OPTIMIZER)
              .setProperties(resourceGroup.getProperties())
              .setThreadCount(requiredCores)
              .build();
      ResourceContainer rc = Containers.get(resource.getContainerName());
      try {
        ((AbstractOptimizerContainer) rc).requestResource(resource);
        optimizerManager.createResource(resource);
      } finally {
        keepInTouch(resourceGroup.getName(), keepingTask.getAttempts() + 1);
      }
      LOG.info(
          "Resource Group:{} has insufficient resources, created an optimizer with parallelism of {}",
          resourceGroup.getName(),
          requiredCores);
    }
  }

  private class DraScaleTask implements Delayed {

    private final String groupName;
    private final long readyTimeMs;

    private DraScaleTask(String groupName, long delayMs) {
      this.groupName = groupName;
      this.readyTimeMs = System.currentTimeMillis() + delayMs;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
      return unit.convert(readyTimeMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed other) {
      return Long.compare(readyTimeMs, ((DraScaleTask) other).readyTimeMs);
    }
  }

  /**
   * Keeper owning both the floor and the demand scaling of dynamic-allocation-enabled groups
   * (AIP-5). It is separate from {@link OptimizerGroupKeeper}, whose min-parallelism-check cadence
   * (minutes, multiplied by attempts) would render the DRA backlog timeouts (seconds) unreachable;
   * a group's scale evaluations run at its own sustained-backlog-timeout instead.
   */
  private class OptimizerScaleKeeper extends AbstractKeeper<DraScaleTask> {

    // Must exceed a normal pod boot including image pull: evicting a legitimately booting pod
    // from the pending accounting would cause duplicate scale-outs, which is worse than a few
    // conservative rounds with phantom capacity.
    private static final long BOOT_TIMEOUT_MS = 3 * 60 * 1000L;

    // Retry delay after a transient resource-group read failure, when the group's configured
    // cadence is unknown because the group itself could not be loaded.
    private static final long TRANSIENT_RETRY_DELAY_MS = 5_000L;

    private final Map<String, DynamicAllocationState> scaleStates = new ConcurrentHashMap<>();
    private final Map<String, PendingRegistrations> pendingRegistrations =
        new ConcurrentHashMap<>();
    private final Set<String> watchedGroups = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> planningBoundStreaks = new ConcurrentHashMap<>();
    private final Map<String, DynamicAllocationMetrics> metricsByGroup = new ConcurrentHashMap<>();

    public OptimizerScaleKeeper(String threadName) {
      super(threadName);
    }

    /**
     * Start watching a group if dynamic allocation is effectively enabled on it. Idempotent.
     *
     * <p>Watch and unwatch are serialized: metric registration is not idempotent (re-registering a
     * live key throws), so an unlocked watch/unwatch interleaving could strand a registration that
     * the other side never saw — after which every re-watch of the group throws before queueing its
     * scale task, leaving it watched-but-dead until a restart. These are rare control-plane calls;
     * the lock costs nothing on the scaling hot path.
     */
    public synchronized void watch(ResourceGroup resourceGroup) {
      if (stopped) {
        // A watch arriving after dispose — an in-flight config-sync run or the round's re-check
        // racing a leader hand-off — must not register metrics from a dead service: the keys
        // would outlive it in the global registry and fail the next leader's watch.
        return;
      }
      if (!DynamicAllocationConfig.isEffectivelyEnabled(resourceGroup)) {
        // Propagate a disable on the config-entry path itself: the round-driven unwatch runs on
        // the leader only, so a follower relying on it would keep the group's drain blocks and
        // exported metrics until failover.
        unwatch(resourceGroup.getName());
        return;
      }
      if (!watchedGroups.contains(resourceGroup.getName())) {
        // Register metrics before marking the group watched: a failed registration must leave
        // the group rewatchable, not watched-but-dead with every retry swallowed by the entry.
        registerMetrics(resourceGroup.getName());
        watchedGroups.add(resourceGroup.getName());
        suspendingQueue.add(new DraScaleTask(resourceGroup.getName(), 0));
      }
    }

    /**
     * Register the group's DRA gauges and counters, keyed by the keeper's own state: unlike the
     * queue-scoped {@code OptimizerGroupMetrics} they live with the watch, so a group handed back
     * to the legacy floor keeper stops exporting scaling metrics it no longer produces.
     */
    private void registerMetrics(String groupName) {
      DynamicAllocationMetrics metrics =
          new DynamicAllocationMetrics(
              groupName,
              MetricManager.getInstance().getGlobalRegistry(),
              new DynamicAllocationMetrics.Source() {
                @Override
                public int pendingRemovalOptimizers() {
                  return (int)
                      pendingRemovalTokens.stream()
                          .map(authOptimizers::get)
                          .filter(
                              optimizer ->
                                  optimizer != null && groupName.equals(optimizer.getGroupName()))
                          .count();
                }

                @Override
                public int effectiveThreads() {
                  return getTotalQuota(groupName) + pendingThreads(groupName);
                }

                @Override
                public long backlogDurationMs() {
                  DynamicAllocationState state = scaleStates.get(groupName);
                  return state == null ? 0 : state.backlogDurationMs(System.currentTimeMillis());
                }
              });
      metrics.register();
      metricsByGroup.put(groupName, metrics);
    }

    /** Clear the boot-window accounting of a registered optimizer (AMS-launched ones only). */
    public void onOptimizerRegistered(OptimizerInstance optimizer) {
      if (optimizer.getResourceId() == null) {
        return;
      }
      PendingRegistrations pending = pendingRegistrations.get(optimizer.getGroupName());
      if (pending != null) {
        pending.registered(optimizer.getResourceId());
      }
    }

    private synchronized void unwatch(String groupName) {
      watchedGroups.remove(groupName);
      scaleStates.remove(groupName);
      planningBoundStreaks.remove(groupName);
      DynamicAllocationMetrics metrics = metricsByGroup.remove(groupName);
      if (metrics != null) {
        metrics.unregister();
      }
      // A drain block left behind would starve the group's pods forever once the legacy floor
      // keeper resumes duty for the disabled group: re-admit them to task assignment.
      authOptimizers.values().stream()
          .filter(optimizer -> groupName.equals(optimizer.getGroupName()))
          .map(OptimizerInstance::getToken)
          .forEach(DefaultOptimizingService.this::cancelDrain);
      // pendingRegistrations is deliberately kept: a pod requested before a disable survives its
      // boot window, so re-enabling within it does not re-request the same capacity. Entries
      // self-prune past their deadline.
    }

    /**
     * Full cleanup on group deletion. Unlike a disable, a deleted group's boot-window accounting
     * must go too: leaving it would leak the entry and, if a group with the same name is created
     * before the next evaluation, suppress its scale-up with the old group's phantom capacity.
     */
    public synchronized void onGroupDeleted(String groupName) {
      unwatch(groupName);
      pendingRegistrations.remove(groupName);
    }

    /**
     * The global metric registry outlives this service: on a leader hand-off the next leader's
     * fresh service watches the same groups, and any keys left behind here would make that watch
     * throw, leaving the group watched-but-dead until a JVM restart.
     */
    @Override
    public synchronized void dispose() {
      super.dispose();
      metricsByGroup.values().forEach(DynamicAllocationMetrics::unregister);
      metricsByGroup.clear();
    }

    @Override
    protected void processTask(DraScaleTask task) {
      ResourceGroup resourceGroup;
      try {
        resourceGroup = optimizerManager.getResourceGroup(task.groupName);
      } catch (Exception e) {
        // A transient failure (e.g. a database hiccup) must not be treated as deletion: there is
        // no periodic re-watch, so dropping the group here would silently disable its dynamic
        // allocation until the next config change. Keep the task alive and retry.
        LOG.warn(
            "Failed to load resource group {} for dynamic allocation, will retry",
            task.groupName,
            e);
        suspendingQueue.add(new DraScaleTask(task.groupName, TRANSIENT_RETRY_DELAY_MS));
        return;
      }
      if (resourceGroup == null || !DynamicAllocationConfig.isEffectivelyEnabled(resourceGroup)) {
        // Deleted or disabled: stop watching; an update re-enabling DRA re-watches the group.
        unwatch(task.groupName);
        // An update may have re-enabled the group between our read and the unwatch, in which
        // case its watch() call was swallowed by the still-present watchedGroups entry:
        // double-check on a fresh read so such a group is not orphaned until its next change.
        recheckAfterUnwatch(task.groupName);
        return;
      }
      OptimizingQueue queue = optimizingQueueByGroup.get(task.groupName);
      if (queue == null) {
        // The group exists with DRA enabled but its queue is momentarily absent (e.g. a
        // delete/recreate racing the config watcher). Unwatch + rewatch here would spin a
        // delay-0 hot loop until the watcher recreates the queue; treat it as transient.
        suspendingQueue.add(new DraScaleTask(task.groupName, TRANSIENT_RETRY_DELAY_MS));
        return;
      }
      DynamicAllocationConfig config = DynamicAllocationConfig.parse(resourceGroup);
      try {
        scaleIfNeeded(resourceGroup, queue, config, System.currentTimeMillis());
      } catch (Throwable t) {
        LOG.error("Dynamic allocation scale evaluation failed for group {}", task.groupName, t);
      } finally {
        suspendingQueue.add(
            new DraScaleTask(task.groupName, config.getSustainedBacklogTimeout().toMillis()));
      }
    }

    /** Threads still expected to register for the group; testing hook for boot accounting. */
    private int pendingThreads(String groupName) {
      PendingRegistrations pending = pendingRegistrations.get(groupName);
      return pending == null ? 0 : pending.pendingThreads(System.currentTimeMillis());
    }

    /**
     * Advance this group's drains: an entry whose in-flight count reached zero, or whose {@code
     * drain-timeout} deadline passed, executes its removal now (a force-removed instance's orphaned
     * tasks are reclaimed by the existing suspending-task safety net). Returns the thread and
     * busy-task counts of instances still draining afterwards — a failed release keeps its instance
     * in both, since it remains registered.
     */
    private int[] processDrainProgress(
        String groupName, DynamicAllocationState.GroupLoad load, long now) {
      int drainingThreads = 0;
      int drainingBusy = 0;
      for (String token : pendingRemovalTokens) {
        OptimizerInstance optimizer = authOptimizers.get(token);
        if (optimizer == null) {
          // Unregistered mid-drain (e.g. its heartbeat expired): nothing left to remove.
          cancelDrain(token);
          continue;
        }
        if (!groupName.equals(optimizer.getGroupName())) {
          continue;
        }
        int inFlight = load.getInFlightByToken().getOrDefault(token, 0);
        Long deadline = drainDeadlines.get(token);
        if (inFlight == 0 || (deadline != null && now >= deadline)) {
          executeRemoval(token);
          if (!authOptimizers.containsKey(token)) {
            continue;
          }
        }
        drainingThreads += optimizer.getThreadCount();
        drainingBusy += inFlight;
      }
      return new int[] {drainingThreads, drainingBusy};
    }

    private Set<String> registeredTokens(String groupName) {
      return authOptimizers.values().stream()
          .filter(optimizer -> groupName.equals(optimizer.getGroupName()))
          .map(OptimizerInstance::getToken)
          .collect(Collectors.toSet());
    }

    private void evaluateScaleDown(
        String groupName,
        OptimizingQueue queue,
        DynamicAllocationState state,
        DynamicAllocationConfig config,
        int registeredThreads,
        int drainingThreads,
        long now) {
      List<DynamicAllocationState.RemovalCandidate> candidates =
          authOptimizers.values().stream()
              // Externally-registered optimizers (no resourceId) are not AMS's to remove.
              .filter(optimizer -> groupName.equals(optimizer.getGroupName()))
              .filter(optimizer -> optimizer.getResourceId() != null)
              .filter(optimizer -> !pendingRemovalTokens.contains(optimizer.getToken()))
              .map(
                  optimizer ->
                      new DynamicAllocationState.RemovalCandidate(
                          optimizer.getToken(), optimizer.getThreadCount()))
              .collect(Collectors.toList());
      String victim =
          state.computeScaleDown(candidates, registeredThreads, drainingThreads, config, now);
      if (victim == null) {
        return;
      }
      // The drain start is the scale-down action; the eventual removal only completes it.
      DynamicAllocationMetrics metrics = metricsByGroup.get(groupName);
      if (metrics != null) {
        metrics.incScaleDown();
      }
      beginGracefulDrain(victim, now + config.getDrainTimeout().toMillis());
      // Only a snapshot taken after the token entered the pending-removal set can prove idleness:
      // the pre-insert one may miss a task fetched by a long-poll racing the drain start.
      DynamicAllocationState.GroupLoad fresh = queue.collectDynamicAllocationLoad();
      if (fresh.getInFlightByToken().getOrDefault(victim, 0) == 0) {
        executeRemoval(victim);
      }
    }

    private void recheckAfterUnwatch(String groupName) {
      try {
        ResourceGroup fresh = optimizerManager.getResourceGroup(groupName);
        if (fresh != null) {
          watch(fresh);
        }
      } catch (Exception e) {
        // The group became unreadable right after a successful read; its next update watches it.
        LOG.warn("Failed to re-check resource group {} after unwatch", groupName, e);
      }
    }

    /**
     * Warn when the planning-bound state (idle threads, PENDING tables, nothing PLANNED — the
     * bottleneck is {@code optimizer.max-planning-parallelism}, so scaling out would only add idle
     * threads) persists across two consecutive evaluations. A single snapshot can hold this
     * condition transiently while planning is merely in flight, so one round is not evidence;
     * counting registered threads only keeps a booting pod's phantom capacity from being mistaken
     * for idle threads. Warns once per episode.
     */
    private void warnOnPlanningBoundTransition(
        String groupName, int registeredThreads, DynamicAllocationState.GroupLoad load) {
      boolean planningBound =
          DynamicAllocationState.isPlanningBound(
              registeredThreads,
              load.getBusyThreads(),
              load.getServiceablePlanned(),
              load.getPendingTables());
      if (!planningBound) {
        planningBoundStreaks.remove(groupName);
        return;
      }
      int streak = planningBoundStreaks.merge(groupName, 1, Integer::sum);
      if (streak == 2) {
        LOG.warn(
            "Resource group {} is planning-bound: {} idle thread(s) while {} table(s) are "
                + "PENDING and no tasks are PLANNED. Scaling out will not help; consider "
                + "raising {}.",
            groupName,
            registeredThreads - load.getBusyThreads(),
            load.getPendingTables(),
            AmoroManagementConf.OPTIMIZER_MAX_PLANNING_PARALLELISM.key());
      }
    }

    private void scaleIfNeeded(
        ResourceGroup resourceGroup,
        OptimizingQueue queue,
        DynamicAllocationConfig config,
        long now) {
      String groupName = resourceGroup.getName();
      PendingRegistrations pending =
          pendingRegistrations.computeIfAbsent(
              groupName, name -> new PendingRegistrations(BOOT_TIMEOUT_MS));
      DynamicAllocationState state =
          scaleStates.computeIfAbsent(groupName, name -> new DynamicAllocationState());
      DynamicAllocationState.GroupLoad load = queue.collectDynamicAllocationLoad();
      // Drain progress runs before anything else and unconditionally: a completed or expired
      // drain must convert to a removal even in rounds that scale up, or a busy drain would
      // linger to its full timeout while backlog persists.
      int[] draining = processDrainProgress(groupName, load, now);
      int drainingThreads = draining[0];
      int drainingBusy = draining[1];
      int registeredThreads = getTotalQuota(groupName);
      // A draining instance takes no new work, so it is accounted as already gone on both sides:
      // leaving its threads in the capacity undercounts demand by up to their count, and leaving
      // its tasks in the load keeps future demand (busy >= effective) from ever firing mid-drain.
      int effectiveThreads = registeredThreads - drainingThreads + pending.pendingThreads(now);
      int busyThreads = load.getBusyThreads() - drainingBusy;
      // Observed every round, including scale-up ones: an instance busy through a burst must not
      // come out of it looking idle since before the burst began.
      state.observe(registeredTokens(groupName), load.getInFlightByToken(), now);
      warnOnPlanningBoundTransition(groupName, registeredThreads, load);
      int addInstances =
          state.computeScaleUp(
              effectiveThreads,
              busyThreads,
              load.getServiceablePlanned(),
              load.getPendingTables(),
              config,
              now);
      if (addInstances <= 0) {
        if (!state.wasDemandActive()) {
          evaluateScaleDown(
              groupName, queue, state, config, registeredThreads, drainingThreads, now);
        }
        return;
      }
      DynamicAllocationMetrics metrics = metricsByGroup.get(groupName);
      if (metrics != null) {
        metrics.incScaleUp();
      }
      int threadsPerInstance = config.getExecutorParallelism();
      LOG.info(
          "Dynamic allocation scaling out group {}: {} instance(s) of {} thread(s), effective threads {}",
          groupName,
          addInstances,
          threadsPerInstance,
          effectiveThreads);
      for (int i = 0; i < addInstances; i++) {
        Resource resource =
            new Resource.Builder(resourceGroup.getContainer(), groupName, ResourceType.OPTIMIZER)
                .setProperties(resourceGroup.getProperties())
                .setThreadCount(threadsPerInstance)
                .build();
        ResourceContainer resourceContainer = Containers.get(resource.getContainerName());
        pending.requested(resource.getResourceId(), threadsPerInstance, now);
        boolean podRequested = false;
        try {
          ((AbstractOptimizerContainer) resourceContainer).requestResource(resource);
          podRequested = true;
          optimizerManager.createResource(resource);
        } catch (Throwable t) {
          if (podRequested) {
            // The pod was started; only its persistence failed. Keep the pending accounting —
            // the pod will self-register — instead of erasing it and re-requesting a duplicate.
            LOG.warn(
                "Dynamic allocation scale-out of group {} requested resource {} but failed to "
                    + "persist it",
                groupName,
                resource.getResourceId(),
                t);
          } else {
            pending.failed(resource.getResourceId());
            LOG.warn("Dynamic allocation scale-out failed for group {}", groupName, t);
          }
        }
      }
    }
  }
}

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
import org.apache.amoro.resource.InternalResourceContainer;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceContainer;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.resource.ResourceType;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.model.OptimizerResourceInfo;
import org.apache.amoro.server.ha.HighAvailabilityContainer;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingQueue;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
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
  private final boolean autoRestartEnabled;
  private final int autoRestartMaxRetries;
  private final long autoRestartGracePeriodMs;
  private final Map<String, OptimizingQueue> optimizingQueueByGroup = new ConcurrentHashMap<>();
  private final Map<String, OptimizingQueue> optimizingQueueByToken = new ConcurrentHashMap<>();
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();
  private final OptimizerKeeper optimizerKeeper = new OptimizerKeeper("optimizer-keeper-thread");
  private final OptimizerGroupKeeper optimizerGroupKeeper =
      new OptimizerGroupKeeper("optimizer-group-keeper-thread");
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
    this.autoRestartEnabled =
        serviceConfig.getBoolean(AmoroManagementConf.OPTIMIZER_AUTO_RESTART_ENABLED);
    this.autoRestartMaxRetries =
        serviceConfig.getInteger(AmoroManagementConf.OPTIMIZER_AUTO_RESTART_MAX_RETRIES);
    this.autoRestartGracePeriodMs =
        serviceConfig.getDurationInMillis(AmoroManagementConf.OPTIMIZER_AUTO_RESTART_GRACE_PERIOD);
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
        });
    optimizers.forEach(optimizer -> registerOptimizer(optimizer, false));
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
    OptimizerThread optimizerThread = getAuthenticatedOptimizer(authToken).getThread(threadId);
    OptimizingQueue queue = getQueueByToken(authToken);
    TaskRuntime<?> task = queue.pollTask(optimizerThread, pollingTimeout, breakQuotaLimit);
    if (task != null) {
      LOG.info("OptimizerThread {} polled task {}", optimizerThread, task.getTaskId());
      return task.extractProtocolTask();
    }
    return null;
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
    planExecutor.shutdown();
    // shutdown sync group first, stop syncing group
    optimizingConfigWatcher.dispose();
    // dispose all queues
    optimizingQueueByGroup.values().forEach(OptimizingQueue::dispose);
    optimizerKeeper.dispose();
    optimizerGroupKeeper.dispose();
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
          LOG.error("{} has encountered a problem.", this.getClass().getSimpleName(), t);
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
      if (!resourceGroup
          .getProperties()
          .containsKey(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM)) {
        return 0;
      }
      String minParallelism =
          resourceGroup.getProperties().get(OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM);
      try {
        return Integer.parseInt(minParallelism);
      } catch (Throwable t) {
        LOG.warn("Illegal minParallelism : {}, will use default value 0", minParallelism, t);
        return 0;
      }
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

    /**
     * Tracks orphaned resource state. Key is resourceId. Value records the timestamp when the
     * resource was first detected as orphaned and the number of restart attempts so far. Entries
     * are removed when the optimizer successfully registers or when the resource is cleaned up.
     *
     * <p>Accessed only from the single keeper thread — no synchronization needed.
     */
    private final Map<String, OrphanedResourceState> orphanedResourceStates = new HashMap<>();

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

      // Check and restart orphaned resources if auto-restart is enabled
      int orphanedCores = 0;
      if (autoRestartEnabled) {
        orphanedCores = restartOrphanedOptimizers(resourceGroup);
      }

      // rawRequiredCores = minParallelism - currently active optimizer cores.
      // Used for minParallelism reset so that orphaned cores (whose optimizer processes are
      // dead) are NOT counted as "satisfied" capacity.
      int rawRequiredCores = keepingTask.tryKeeping(resourceGroup);

      // Subtract orphaned cores already being restarted to avoid double-provisioning.
      // Clamp to 0: orphanedCores can exceed rawRequiredCores when orphaned resources cover
      // more than the group's parallelism deficit.
      int requiredCores = Math.max(0, rawRequiredCores - orphanedCores);
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
            minParallelism - rawRequiredCores);
        resourceGroup
            .getProperties()
            .put(
                OptimizerProperties.OPTIMIZER_GROUP_MIN_PARALLELISM,
                String.valueOf(minParallelism - rawRequiredCores));
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
        ((InternalResourceContainer) rc).requestResource(resource);
        optimizerManager.createResource(resource);
      } finally {
        keepInTouch(resourceGroup.getName(), keepingTask.getAttempts() + 1);
      }
      LOG.info(
          "Resource Group:{} has insufficient resources, created an optimizer with parallelism of {}",
          resourceGroup.getName(),
          requiredCores);
    }

    /**
     * Detect and restart orphaned resources for a resource group. An orphaned resource is one that
     * exists in the resource table but has no corresponding optimizer instance in the optimizer
     * table, indicating the optimizer process died unexpectedly.
     *
     * <p>A grace period ({@link AmoroManagementConf#OPTIMIZER_AUTO_RESTART_GRACE_PERIOD}) is
     * applied before an orphaned resource is considered for restart, to avoid interfering with
     * resources whose optimizer processes are still starting up (e.g. Flink/Kubernetes).
     *
     * <p>For each orphaned resource past the grace period:
     *
     * <ul>
     *   <li>If retries &lt; max retries: attempt to restart the optimizer via the container
     *   <li>If retries &gt;= max retries: clean up the orphaned resource from the resource table
     * </ul>
     *
     * @return the total thread count of orphaned resources that are pending restart (either in
     *     grace period or actively being restarted), so that {@code tryKeeping} can subtract this
     *     from {@code requiredCores} to avoid double provisioning.
     */
    private int restartOrphanedOptimizers(ResourceGroup resourceGroup) {
      int orphanedThreadCount = 0;
      try {
        String groupName = resourceGroup.getName();
        List<Resource> resources = optimizerManager.listResourcesByGroup(groupName);
        if (resources.isEmpty()) {
          return 0;
        }

        // Collect all optimizer resourceIds in this group.
        // Note: these two DB queries are not in the same transaction. An optimizer could
        // register between the two calls, making it appear orphaned. The grace period
        // is intentionally designed to absorb such transient windows.
        // TODO: consider replacing this DB query with authOptimizers (in-memory map) to
        //  avoid a full table scan per keeper cycle when many groups are active.
        List<OptimizerInstance> optimizers = optimizerManager.listOptimizers(groupName);
        Set<String> activeResourceIds =
            optimizers.stream()
                .map(OptimizerInstance::getResourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Find orphaned resources (resources without any active optimizer)
        List<Resource> orphanedResources =
            resources.stream()
                .filter(r -> !activeResourceIds.contains(r.getResourceId()))
                .collect(Collectors.toList());

        // Clean up tracking for resources that are no longer orphaned
        orphanedResourceStates
            .keySet()
            .removeIf(
                resourceId ->
                    resources.stream().noneMatch(r -> resourceId.equals(r.getResourceId()))
                        || activeResourceIds.contains(resourceId));

        long now = System.currentTimeMillis();
        for (Resource orphanedResource : orphanedResources) {
          String resourceId = orphanedResource.getResourceId();
          OrphanedResourceState state =
              orphanedResourceStates.computeIfAbsent(
                  resourceId, k -> new OrphanedResourceState(now));

          // Grace period: skip restart if the resource was recently first detected or last
          // restarted. This avoids false-positive restarts for newly started optimizers and
          // rate-limits retry attempts after a restart.
          long gracePeriodStart =
              state.lastRestartTime >= 0 ? state.lastRestartTime : state.firstDetectedTime;
          if (now - gracePeriodStart < autoRestartGracePeriodMs) {
            LOG.debug(
                "Orphaned resource {} in group {} is within grace period ({} ms remaining), "
                    + "skipping restart",
                resourceId,
                groupName,
                autoRestartGracePeriodMs - (now - gracePeriodStart));
            orphanedThreadCount += orphanedResource.getThreadCount();
            continue;
          }

          if (state.restartAttempts >= autoRestartMaxRetries) {
            LOG.warn(
                "Orphaned resource {} in group {} has exceeded max restart retries ({}), "
                    + "cleaning up the resource",
                resourceId,
                groupName,
                autoRestartMaxRetries);
            try {
              ResourceContainer rc = Containers.get(orphanedResource.getContainerName());
              if (rc instanceof InternalResourceContainer) {
                ((InternalResourceContainer) rc).releaseResource(orphanedResource);
              }
            } catch (Throwable t) {
              LOG.warn(
                  "Failed to release orphaned resource {} via container, "
                      + "will still remove from DB",
                  resourceId,
                  t);
            }
            // Defensively remove any optimizer that may have registered for this resourceId
            // in the window between our two DB queries (TOCTOU). deleteOptimizer is idempotent.
            optimizerManager.deleteOptimizer(groupName, resourceId);
            optimizerManager.deleteResource(resourceId);
            orphanedResourceStates.remove(resourceId);
          } else {
            LOG.info(
                "Detected orphaned resource {} in group {}, attempting restart (attempt {}/{})",
                resourceId,
                groupName,
                state.restartAttempts + 1,
                autoRestartMaxRetries);
            try {
              ResourceContainer rc = Containers.get(orphanedResource.getContainerName());
              if (rc instanceof InternalResourceContainer) {
                ((InternalResourceContainer) rc).requestResource(orphanedResource);
              } else {
                // Auto-restart is not supported for this container type. Log once to alert
                // operators, then suppress further warnings via lastRestartTime so the
                // grace period silences subsequent cycles. Do NOT consume restartAttempts —
                // the resource requires manual intervention, not automatic cleanup.
                if (state.lastRestartTime < 0) {
                  LOG.warn(
                      "Container {} for orphaned resource {} is not an InternalResourceContainer,"
                          + " auto-restart is not supported. Manual intervention required.",
                      orphanedResource.getContainerName(),
                      resourceId);
                  state.lastRestartTime = now;
                }
                orphanedThreadCount += orphanedResource.getThreadCount();
                continue;
              }
              // Persist updated properties (e.g. new job-id from doScaleOut)
              optimizerManager.updateResource(orphanedResource);
              state.restartAttempts++;
              // Record restart time so the next cycle waits a grace period before retrying
              state.lastRestartTime = now;
              orphanedThreadCount += orphanedResource.getThreadCount();
            } catch (Throwable t) {
              LOG.error(
                  "Failed to restart orphaned resource {} in group {}, attempt {}/{}",
                  resourceId,
                  groupName,
                  state.restartAttempts + 1,
                  autoRestartMaxRetries,
                  t);
              state.restartAttempts++;
              // Record restart time so failed attempts also respect the grace period
              state.lastRestartTime = now;
              // Intentionally do NOT add to orphanedThreadCount on failure: the restart
              // request itself failed, so no optimizer process was launched. Allowing
              // tryKeeping() to provision a new resource provides fast recovery while
              // the orphaned resource is retried in the background. This may temporarily
              // result in both the orphaned record and a new resource coexisting in the DB,
              // but the orphan will eventually be cleaned up after max retries.
            }
          }
        }
      } catch (Throwable t) {
        LOG.error("Failed to check orphaned resources for group {}", resourceGroup.getName(), t);
      }
      return orphanedThreadCount;
    }
  }

  /** Tracks the state of an orphaned resource for auto-restart. */
  private static class OrphanedResourceState {
    final long firstDetectedTime;
    /**
     * Timestamp of the most recent restart attempt (-1 if no restart has been attempted yet). Used
     * to enforce the grace period between consecutive restart attempts.
     */
    long lastRestartTime = -1;

    int restartAttempts;

    OrphanedResourceState(long firstDetectedTime) {
      this.firstDetectedTime = firstDetectedTime;
      this.restartAttempts = 0;
    }
  }
}

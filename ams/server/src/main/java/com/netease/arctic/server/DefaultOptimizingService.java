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
import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.OptimizingService;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.exception.PluginRetryAuthException;
import com.netease.arctic.server.optimizing.OptimizingQueue;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.persistence.mapper.ResourceMapper;
import com.netease.arctic.server.resource.DefaultResourceManager;
import com.netease.arctic.server.resource.OptimizerInstance;
import com.netease.arctic.server.resource.OptimizerManager;
import com.netease.arctic.server.table.DefaultTableService;
import com.netease.arctic.server.table.RuntimeHandlerChain;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.table.ArcticTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * OptimizerManagementService is implementing the OptimizerManager Thrift service, which manages the optimization tasks
 * for ArcticTable. It includes methods for authenticating optimizers, polling tasks from the optimizing queue,
 * acknowledging tasks,and completing tasks. The code uses several data structures, including maps for optimizing queues
 * ,task runtimes, and authenticated optimizers.
 * <p>
 * The code also includes a TimerTask for detecting and removing expired optimizers and suspending tasks.
 */
public class DefaultOptimizingService extends DefaultResourceManager
    implements OptimizingService.Iface, OptimizerManager {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizingService.class);

  private final Map<String, OptimizingQueue> optimizingQueueByGroup = new ConcurrentHashMap<>();
  private final Map<String, OptimizingQueue> optimizingQueueByToken = new ConcurrentHashMap<>();
  private final TableManager tableManager;
  private RuntimeHandlerChain tableHandlerChain;
  private Timer optimizerMonitorTimer;

  public DefaultOptimizingService(DefaultTableService tableService, List<ResourceGroup> resourceGroups) {
    super(resourceGroups);
    this.tableManager = tableService;
    this.tableHandlerChain = new TableRuntimeHandlerImpl();
  }

  public RuntimeHandlerChain getTableRuntimeHandler() {
    return tableHandlerChain;
  }

  //TODO optimizing code
  public void loadOptimizingQueues(List<TableRuntimeMeta> tableRuntimeMetaList) {
    List<ResourceGroup> optimizerGroups = getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups);
    Map<String, List<TableRuntimeMeta>> groupToTableRuntimes = tableRuntimeMetaList.stream()
        .collect(Collectors.groupingBy(TableRuntimeMeta::getOptimizerGroup));
    optimizerGroups.forEach(group -> {
      String groupName = group.getName();
      List<TableRuntimeMeta> tableRuntimeMetas = groupToTableRuntimes.remove(groupName);
      optimizingQueueByGroup.put(groupName, new OptimizingQueue(tableManager, group,
          Optional.ofNullable(tableRuntimeMetas).orElseGet(ArrayList::new)));
    });
    groupToTableRuntimes.keySet().forEach(groupName -> LOG.warn("Unloaded task runtime in group " + groupName));
  }

  @Override
  public void ping() {
  }

  @Override
  public void touch(String authToken) {
    LOG.info("Optimizer {} touching", authToken);
    OptimizingQueue queue = getQueueByToken(authToken);
    queue.touch(authToken);
  }

  @Override
  public OptimizingTask pollTask(String authToken, int threadId) {
    LOG.info("Optimizer {} polling task", authToken);
    OptimizingQueue queue = getQueueByToken(authToken);
    return queue.pollTask(authToken, threadId);
  }

  @Override
  public void ackTask(String authToken, int threadId, OptimizingTaskId taskId) {
    LOG.info("Ack task {} by optimizer {}.", taskId, authToken);
    OptimizingQueue queue = getQueueByToken(authToken);
    queue.ackTask(authToken, threadId, taskId);
  }

  @Override
  public void completeTask(String authToken, OptimizingTaskResult taskResult) {
    LOG.info("Optimizer {} complete task {}", authToken, taskResult.getTaskId());
    OptimizingQueue queue = getQueueByToken(authToken);
    queue.completeTask(authToken, taskResult);
  }

  @Override
  public String authenticate(OptimizerRegisterInfo registerInfo) {
    LOG.info("Register optimizer {}.", registerInfo);
    OptimizingQueue queue = getQueueByGroup(registerInfo.getGroupName());
    String token = queue.authenticate(registerInfo);
    optimizingQueueByToken.put(token, queue);
    return token;
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
    return optimizingQueueByGroup.values()
        .stream()
        .flatMap(queue -> queue.getOptimizers().stream())
        .collect(Collectors.toList());
  }

  @Override
  public List<OptimizerInstance> listOptimizers(String group) {
    return getQueueByGroup(group).getOptimizers();
  }

  @Override
  public void deleteOptimizer(String group, String resourceId) {
    getQueueByGroup(group).removeOptimizer(resourceId);
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
    public void handleTableAdded(ArcticTable table, TableRuntime tableRuntime) {
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
          ArcticServiceConstants.OPTIMIZER_CHECK_INTERVAL,
          ArcticServiceConstants.OPTIMIZER_CHECK_INTERVAL);
      LOG.info("OptimizerManagementService initializing has completed");
    }

    @Override
    protected void doDispose() {
      optimizerMonitorTimer.cancel();
    }
  }

  private class SuspendingDetector extends TimerTask {

    @Override
    public void run() {
      try {
        optimizingQueueByGroup.values().forEach(OptimizingQueue::checkSuspending);
      } catch (RuntimeException e) {
        LOG.error("Update optimizer status abnormal failed. try next round", e);
      }
    }
  }
}
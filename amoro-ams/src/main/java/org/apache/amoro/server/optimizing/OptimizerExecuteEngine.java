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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.process.EngineType;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.optimizing.TaskRuntime.Status;
import org.apache.amoro.server.resource.OptimizerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * ExecuteEngine implementation for the AMORO optimizer pull-based task dispatch model. Manages
 * per-group process queues and provides the poll/ack/complete API that optimizer workers use to
 * fetch and process tasks.
 *
 * <p>Note: The standard ExecuteEngine methods (submitTableProcess/getStatus/tryCancelTableProcess)
 * are provided for framework compatibility, but the primary interaction is through the pull-based
 * task dispatch methods (pollTask/ackTask/completeTask).
 */
public class OptimizerExecuteEngine implements ExecuteEngine {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizerExecuteEngine.class);

  public static final EngineType ENGINE_TYPE = EngineType.of("OPTIMIZER");

  private final Map<String, GroupContext> groupContexts = new ConcurrentHashMap<>();

  @Override
  public String name() {
    return "optimizer-execute-engine";
  }

  @Override
  public EngineType engineType() {
    return ENGINE_TYPE;
  }

  /**
   * Submit a table process to the engine. This is called by the framework when a process needs to
   * be submitted to an engine. For the optimizing engine, we look up the process by its ID in our
   * group queues.
   *
   * <p>Note: OptimizingTableProcess instances are added directly to group queues via {@link
   * #submitOptimizingProcess}. This method is for framework compatibility.
   */
  @Override
  public String submitTableProcess(TableProcess tableProcess) {
    OptimizingTableProcess process = unwrapProcess(tableProcess);
    // Get group name from the adapter's TableRuntime
    String groupName =
        tableProcess.getTableRuntime() != null
            ? ((org.apache.amoro.server.table.DefaultTableRuntime) tableProcess.getTableRuntime())
                .getGroupName()
            : null;
    if (groupName != null) {
      submitOptimizingProcess(process, groupName);
    } else {
      // Fallback: try to find any group context
      for (Map.Entry<String, GroupContext> entry : groupContexts.entrySet()) {
        submitOptimizingProcess(process, entry.getKey());
        break;
      }
    }
    return String.valueOf(process.getProcessId());
  }

  private OptimizingTableProcess unwrapProcess(TableProcess tableProcess) {
    if (tableProcess instanceof OptimizingProcessAdapter) {
      return ((OptimizingProcessAdapter) tableProcess).getDelegate();
    }
    throw new UnsupportedOperationException(
        "Expected OptimizingProcessAdapter, got: " + tableProcess.getClass().getName());
  }

  @Override
  public ProcessStatus getStatus(String processIdentifier) {
    long processId = Long.parseLong(processIdentifier);
    for (GroupContext ctx : groupContexts.values()) {
      Optional<OptimizingTableProcess> found =
          ctx.processQueue.stream().filter(p -> p.getProcessId() == processId).findFirst();
      if (found.isPresent()) {
        return found.get().getStatus();
      }
    }
    return ProcessStatus.CLOSED;
  }

  @Override
  public ProcessStatus tryCancelTableProcess(TableProcess tableProcess, String processIdentifier) {
    long processId = Long.parseLong(processIdentifier);
    OptimizingTableProcess process = unwrapProcess(tableProcess);
    process.close(true);
    return process.getStatus();
  }

  @Override
  public void open(Map<String, String> properties) {
    LOG.info("OptimizerExecuteEngine opened");
  }

  @Override
  public void close() {
    for (Map.Entry<String, GroupContext> entry : groupContexts.entrySet()) {
      GroupContext ctx = entry.getValue();
      ctx.processQueue.forEach(p -> p.close(false));
    }
    groupContexts.clear();
    LOG.info("OptimizerExecuteEngine closed");
  }

  // ===== Optimizing-specific process submission =====

  /**
   * Submit an optimizing process to the appropriate group queue. This is the primary entry point
   * for adding processes to the engine.
   */
  public void submitOptimizingProcess(OptimizingTableProcess process, String groupName) {
    GroupContext ctx = groupContexts.get(groupName);
    if (ctx == null) {
      throw new IllegalStateException("No GroupContext for group: " + groupName);
    }

    ctx.processQueue.offer(process);
    ctx.scheduleLock.lock();
    try {
      ctx.taskAvailable.signalAll();
    } finally {
      ctx.scheduleLock.unlock();
    }

    LOG.info("Submitted optimizing process {} to group {}", process.getProcessId(), groupName);
  }

  // ===== Pull-based task dispatch API (used by Thrift handler) =====

  public TaskRuntime<?> pollTask(
      String groupName, OptimizerThread thread, long maxWaitTime, boolean breakQuotaLimit) {
    GroupContext ctx = groupContexts.get(groupName);
    if (ctx == null) {
      return null;
    }

    resetStaleTasksForThread(groupName, thread);

    long deadline = System.currentTimeMillis() + maxWaitTime;
    if (deadline <= 0) {
      deadline = Long.MAX_VALUE;
    }

    TaskRuntime<?> task = fetchFromProcesses(ctx, thread, true);
    while (task == null && waitForTask(ctx, deadline)) {
      task = fetchFromProcesses(ctx, thread, true);
    }

    if (task == null && breakQuotaLimit && noPendingPlanning(ctx)) {
      task = fetchFromProcesses(ctx, thread, false);
    }

    return task;
  }

  public void ackTask(String groupName, OptimizingTaskId taskId, OptimizerThread thread) {
    findProcess(groupName, taskId).ackTask(taskId, thread);
  }

  public void completeTask(String groupName, OptimizerThread thread, OptimizingTaskResult result) {
    findProcess(groupName, result.getTaskId()).completeTask(thread, result);
  }

  public List<TaskRuntime<?>> collectTasks(String groupName) {
    GroupContext ctx = groupContexts.get(groupName);
    if (ctx == null) {
      return Collections.emptyList();
    }
    return ctx.processQueue.stream()
        .flatMap(p -> p.getTaskMap().values().stream())
        .collect(Collectors.toList());
  }

  public List<TaskRuntime<?>> collectTasks(
      String groupName, java.util.function.Predicate<TaskRuntime<?>> predicate) {
    return collectTasks(groupName).stream().filter(predicate).collect(Collectors.toList());
  }

  public void retryTask(String groupName, TaskRuntime<?> taskRuntime) {
    findProcess(groupName, taskRuntime.getTaskId())
        .resetTask((TaskRuntime<org.apache.amoro.optimizing.RewriteStageTask>) taskRuntime);
  }

  public void removeProcess(String groupName, long processId) {
    GroupContext ctx = groupContexts.get(groupName);
    if (ctx != null) {
      ctx.processQueue.removeIf(p -> p.getProcessId() == processId);
    }
  }

  // ===== Group context management =====

  public void createGroupContext(ResourceGroup group) {
    groupContexts.put(group.getName(), new GroupContext(group));
    LOG.info("Created GroupContext for optimizer group: {}", group.getName());
  }

  public void removeGroupContext(String groupName) {
    GroupContext ctx = groupContexts.remove(groupName);
    if (ctx != null) {
      ctx.processQueue.forEach(p -> p.close(false));
      LOG.info("Removed GroupContext for optimizer group: {}", groupName);
    }
  }

  public void updateGroupContext(ResourceGroup group) {
    GroupContext ctx = groupContexts.get(group.getName());
    if (ctx != null) {
      ctx.group = group;
    }
  }

  public GroupContext getGroupContext(String groupName) {
    return groupContexts.get(groupName);
  }

  // ===== Private helpers =====

  private TaskRuntime<?> fetchFromProcesses(
      GroupContext ctx, OptimizerThread thread, boolean needQuotaChecking) {
    return ctx.processQueue.stream()
        .map(process -> process.poll(thread, needQuotaChecking))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private boolean waitForTask(GroupContext ctx, long deadline) {
    ctx.scheduleLock.lock();
    try {
      long currentTime = System.currentTimeMillis();
      return deadline > currentTime
          && ctx.taskAvailable.await(deadline - currentTime, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      LOG.error("Wait for task interrupted", e);
      return false;
    } finally {
      ctx.scheduleLock.unlock();
    }
  }

  private boolean noPendingPlanning(GroupContext ctx) {
    return ctx.processQueue.stream().allMatch(p -> p.getStatus() == ProcessStatus.RUNNING);
  }

  private OptimizingTableProcess findProcess(String groupName, OptimizingTaskId taskId) {
    GroupContext ctx = groupContexts.get(groupName);
    if (ctx == null) {
      throw new org.apache.amoro.exception.TaskNotFoundException(taskId);
    }
    return ctx.processQueue.stream()
        .filter(p -> p.getProcessId() == taskId.getProcessId())
        .findFirst()
        .orElseThrow(() -> new org.apache.amoro.exception.TaskNotFoundException(taskId));
  }

  private void resetStaleTasksForThread(String groupName, OptimizerThread thread) {
    collectTasks(
            groupName,
            task ->
                task.getStatus() == Status.ACKED
                    && Objects.equals(task.getToken(), thread.getToken())
                    && task.getThreadId() == thread.getThreadId())
        .forEach(
            task -> {
              LOG.warn(
                  "Resetting stale ACKED task {} because optimizer thread {} is polling new task",
                  task.getTaskId(),
                  thread);
              retryTask(groupName, task);
            });
  }

  /** Per-group context holding the process queue and scheduling lock. */
  public static class GroupContext {
    final Queue<OptimizingTableProcess> processQueue =
        new java.util.concurrent.LinkedTransferQueue<>();
    final Lock scheduleLock = new ReentrantLock();
    final Condition taskAvailable = scheduleLock.newCondition();
    volatile ResourceGroup group;

    GroupContext(ResourceGroup group) {
      this.group = group;
    }

    public ResourceGroup getGroup() {
      return group;
    }

    public Queue<OptimizingTableProcess> getProcessQueue() {
      return processQueue;
    }
  }
}

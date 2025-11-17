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

package org.apache.amoro.optimizer.common;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.io.reader.DeleteCache;
import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingExecutorFactory;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.apache.amoro.utils.ExceptionUtil;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.common.DynConstructors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OptimizerExecutor extends AbstractOptimizerOperator {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizerExecutor.class);
  protected static final int ERROR_MESSAGE_MAX_LENGTH = 4000;

  private final int threadId;

  public OptimizerExecutor(OptimizerConfig config, int threadId) {
    super(config);
    this.threadId = threadId;
  }

  public void start() {
    // Check if in master-slave mode
    boolean isMasterSlaveMode = getConfig().isMasterSlaveMode() && getAmsNodeManager() != null;

    if (isMasterSlaveMode) {
      // Master-slave mode: get node list and process tasks from each node
      startMasterSlaveMode();
    } else {
      // Active-standby mode: use original logic
      startSingleNodeMode();
    }
  }

  /** Start in master-slave mode: get node list and process tasks from each AMS node. */
  private void startMasterSlaveMode() {
    while (isStarted()) {
      // Get current AMS node list at the beginning of each iteration
      List<String> amsUrls = getAmsNodeList();
      LOG.info("## startMasterSlaveMode amsUrls: " + amsUrls);

      // Process tasks from each AMS node
      for (String amsUrl : amsUrls) {
        if (!isStarted()) {
          break;
        }

        OptimizingTask ackTask = null;
        OptimizingTaskResult result = null;
        try {
          OptimizingTask task = pollTask(amsUrl);
          if (task != null && ackTask(amsUrl, task)) {
            ackTask = task;
            result = executeTask(task);
          }
        } catch (Throwable t) {
          if (ackTask != null) {
            LOG.error(
                "Optimizer executor[{}] handling task[{}] from AMS {} failed and got an unknown error",
                threadId,
                ackTask.getTaskId(),
                amsUrl,
                t);
            String errorMessage = ExceptionUtil.getErrorMessage(t, ERROR_MESSAGE_MAX_LENGTH);
            result = new OptimizingTaskResult(ackTask.getTaskId(), threadId);
            result.setErrorMessage(errorMessage);
          } else {
            LOG.error(
                "Optimizer executor[{}] got an unexpected error from AMS {}", threadId, amsUrl, t);
          }
        } finally {
          if (result != null) {
            completeTask(amsUrl, result);
          }
        }
      }

      // If no nodes available, wait a bit before retrying
      if (amsUrls.isEmpty()) {
        waitAShortTime();
      }
    }
  }

  /** Start in single node mode: use original logic without node list iteration. */
  private void startSingleNodeMode() {
    while (isStarted()) {
      OptimizingTask ackTask = null;
      OptimizingTaskResult result = null;
      try {
        OptimizingTask task = pollTask();
        if (task != null && ackTask(task)) {
          ackTask = task;
          result = executeTask(task);
        }
      } catch (Throwable t) {
        if (ackTask != null) {
          LOG.error(
              "Optimizer executor[{}] handling task[{}] failed and got an unknown error",
              threadId,
              ackTask.getTaskId(),
              t);
          String errorMessage = ExceptionUtil.getErrorMessage(t, ERROR_MESSAGE_MAX_LENGTH);
          result = new OptimizingTaskResult(ackTask.getTaskId(), threadId);
          result.setErrorMessage(errorMessage);
        } else {
          LOG.error("Optimizer executor[{}] got an unexpected error", threadId, t);
        }
      } finally {
        if (result != null) {
          completeTask(result);
        }
      }
    }
  }

  /**
   * Get the list of AMS nodes to interact with. In master-slave mode, returns all available nodes.
   * In single node mode, returns a list with the configured AMS URL.
   */
  private List<String> getAmsNodeList() {
    if (getAmsNodeManager() != null) {
      List<String> nodes = getAmsNodeManager().getAllAmsUrls();
      if (!nodes.isEmpty()) {
        return nodes;
      }
    }
    // Fallback to single node mode
    return Collections.singletonList(getConfig().getAmsUrl());
  }

  public int getThreadId() {
    return threadId;
  }

  /**
   * Poll task from the specified AMS node (used in master-slave mode).
   *
   * @param amsUrl The AMS node URL to poll task from
   * @return The polled task, or null if no task available
   */
  private OptimizingTask pollTask(String amsUrl) {
    OptimizingTask task = null;
    try {
      task = callAuthenticatedAms(amsUrl, (client, token) -> client.pollTask(token, threadId));
      if (task != null) {
        LOG.info(
            "Optimizer executor[{}] polled task[{}] from AMS {}",
            threadId,
            task.getTaskId(),
            amsUrl);
      }
    } catch (TException exception) {
      LOG.error(
          "Optimizer executor[{}] polled task from AMS {} failed", threadId, amsUrl, exception);
    }
    return task;
  }

  /**
   * Poll task (used in single node mode).
   *
   * @return The polled task, or null if no task available
   */
  private OptimizingTask pollTask() {
    OptimizingTask task = null;
    while (isStarted()) {
      try {
        task = callAuthenticatedAms((client, token) -> client.pollTask(token, threadId));
      } catch (TException exception) {
        LOG.error("Optimizer executor[{}] polled task failed", threadId, exception);
      }
      if (task != null) {
        LOG.info("Optimizer executor[{}] polled task[{}] from ams", threadId, task.getTaskId());
        break;
      } else {
        waitAShortTime();
      }
    }
    return task;
  }

  /**
   * Acknowledge task to the specified AMS node (used in master-slave mode).
   *
   * @param amsUrl The AMS node URL to acknowledge task to
   * @param task The task to acknowledge
   * @return true if acknowledged successfully, false otherwise
   */
  private boolean ackTask(String amsUrl, OptimizingTask task) {
    try {
      callAuthenticatedAms(
          amsUrl,
          (client, token) -> {
            client.ackTask(token, threadId, task.getTaskId());
            return null;
          });
      LOG.info(
          "Optimizer executor[{}] acknowledged task[{}] to AMS {}",
          threadId,
          task.getTaskId(),
          amsUrl);
      return true;
    } catch (TException exception) {
      LOG.error(
          "Optimizer executor[{}] acknowledged task[{}] to AMS {} failed",
          threadId,
          task.getTaskId(),
          amsUrl,
          exception);
      return false;
    }
  }

  /**
   * Acknowledge task (used in single node mode).
   *
   * @param task The task to acknowledge
   * @return true if acknowledged successfully, false otherwise
   */
  private boolean ackTask(OptimizingTask task) {
    try {
      callAuthenticatedAms(
          (client, token) -> {
            client.ackTask(token, threadId, task.getTaskId());
            return null;
          });
      LOG.info("Optimizer executor[{}] acknowledged task[{}] to ams", threadId, task.getTaskId());
      return true;
    } catch (TException exception) {
      LOG.error(
          "Optimizer executor[{}] acknowledged task[{}] failed",
          threadId,
          task.getTaskId(),
          exception);
      return false;
    }
  }

  protected OptimizingTaskResult executeTask(OptimizingTask task) {
    return executeTask(getConfig(), getThreadId(), task, LOG);
  }

  /**
   * Complete task to the specified AMS node (used in master-slave mode).
   *
   * @param amsUrl The AMS node URL to complete task to
   * @param optimizingTaskResult The task result to complete
   */
  protected void completeTask(String amsUrl, OptimizingTaskResult optimizingTaskResult) {
    try {
      callAuthenticatedAms(
          amsUrl,
          (client, token) -> {
            client.completeTask(token, optimizingTaskResult);
            return null;
          });
      LOG.info(
          "Optimizer executor[{}] completed task[{}](status: {}) to AMS {}",
          threadId,
          optimizingTaskResult.getTaskId(),
          optimizingTaskResult.getErrorMessage() == null ? "SUCCESS" : "FAIL",
          amsUrl);
    } catch (Exception exception) {
      LOG.error(
          "Optimizer executor[{}] completed task[{}](status: {}) to AMS {} failed",
          threadId,
          optimizingTaskResult.getTaskId(),
          optimizingTaskResult.getErrorMessage() == null ? "SUCCESS" : "FAIL",
          amsUrl,
          exception);
    }
  }

  /**
   * Complete task (used in single node mode).
   *
   * @param optimizingTaskResult The task result to complete
   */
  protected void completeTask(OptimizingTaskResult optimizingTaskResult) {
    try {
      callAuthenticatedAms(
          (client, token) -> {
            client.completeTask(token, optimizingTaskResult);
            return null;
          });
      LOG.info(
          "Optimizer executor[{}] completed task[{}](status: {}) to ams",
          threadId,
          optimizingTaskResult.getTaskId(),
          optimizingTaskResult.getErrorMessage() == null ? "SUCCESS" : "FAIL");
    } catch (Exception exception) {
      LOG.error(
          "Optimizer executor[{}] completed task[{}](status: {}) failed",
          threadId,
          optimizingTaskResult.getTaskId(),
          optimizingTaskResult.getErrorMessage() == null ? "SUCCESS" : "FAIL",
          exception);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static OptimizingTaskResult executeTask(
      OptimizerConfig config, int threadId, OptimizingTask task, Logger logger) {
    long startTime = System.currentTimeMillis();
    TableOptimizing.OptimizingInput input;
    try {
      Map<String, String> taskProperties = fillTaskProperties(config, task);
      input = SerializationUtil.simpleDeserialize(task.getTaskInput());
      String executorFactoryImpl = taskProperties.get(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL);
      DynConstructors.Ctor<OptimizingExecutorFactory> ctor =
          DynConstructors.builder(OptimizingExecutorFactory.class)
              .impl(executorFactoryImpl)
              .buildChecked();
      OptimizingExecutorFactory factory = ctor.newInstance();

      factory.initialize(taskProperties);
      OptimizingExecutor executor = factory.createExecutor(input);
      TableOptimizing.OptimizingOutput output = executor.execute();
      ByteBuffer outputByteBuffer = SerializationUtil.simpleSerialize(output);
      OptimizingTaskResult result = new OptimizingTaskResult(task.getTaskId(), threadId);
      result.setTaskOutput(outputByteBuffer);
      result.setSummary(output.summary());
      logger.info(
          "Optimizer executor[{}] executed task[{}]({}) and cost {} ms",
          threadId,
          task.getTaskId(),
          input,
          System.currentTimeMillis() - startTime);
      return result;
    } catch (Throwable t) {
      logger.error(
          "Optimizer executor[{}] executed task[{}] failed and cost {} ms",
          threadId,
          task.getTaskId(),
          System.currentTimeMillis() - startTime,
          t);
      OptimizingTaskResult errorResult = new OptimizingTaskResult(task.getTaskId(), threadId);
      errorResult.setErrorMessage(ExceptionUtil.getErrorMessage(t, ERROR_MESSAGE_MAX_LENGTH));
      return errorResult;
    }
  }

  private static Map<String, String> fillTaskProperties(
      OptimizerConfig config, OptimizingTask task) {
    if (config.isCacheEnabled()) {
      System.setProperty(DeleteCache.DELETE_CACHE_ENABLED, "true");
    }
    if (!config.getCacheMaxEntrySize().equals(DeleteCache.DELETE_CACHE_MAX_ENTRY_SIZE_DEFAULT)) {
      System.setProperty(DeleteCache.DELETE_CACHE_MAX_ENTRY_SIZE, config.getCacheMaxEntrySize());
    }
    if (!config.getCacheMaxTotalSize().equals(DeleteCache.DELETE_CACHE_MAX_TOTAL_SIZE_DEFAULT)) {
      System.setProperty(
          DeleteCache.DELETE_CACHE_MAX_TOTAL_SIZE_DEFAULT, config.getCacheMaxTotalSize());
    }
    if (!config.getCacheTimeout().equals(DeleteCache.DELETE_CACHE_TIMEOUT)) {
      System.setProperty(DeleteCache.DELETE_CACHE_TIMEOUT, config.getCacheTimeout());
    }
    Map<String, String> properties = Maps.newHashMap(task.getProperties());
    properties.put(TaskProperties.PROCESS_ID, String.valueOf(task.getTaskId().getProcessId()));
    if (config.isExtendDiskStorage()) {
      properties.put(TaskProperties.EXTEND_DISK_STORAGE, "true");
    }
    properties.put(
        TaskProperties.MEMORY_STORAGE_SIZE,
        String.valueOf(config.getMemoryStorageSize() * 1024 * 1024));
    properties.put(TaskProperties.DISK_STORAGE_PATH, config.getDiskStoragePath());
    return properties;
  }
}

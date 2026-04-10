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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OptimizerExecutor extends AbstractOptimizerOperator {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizerExecutor.class);
  protected static final int ERROR_MESSAGE_MAX_LENGTH = 4000;

  private final int threadId;

  public OptimizerExecutor(OptimizerConfig config, int threadId) {
    super(config);
    this.threadId = threadId;
  }

  public void start() {
    // getAmsNodeUrls() returns all nodes in master-slave mode, or the single configured URL in
    // active-standby mode, so both modes are handled uniformly via the same loop.
    long basePollInterval = TimeUnit.SECONDS.toMillis(1);
    long maxPollInterval = TimeUnit.SECONDS.toMillis(30);
    int consecutiveEmptyPolls = 0;
    final int emptyPollThreshold = 5;
    final Random random = new Random();
    List<String> lastLoggedAmsUrls = Collections.emptyList();

    LOG.info(
        "Optimizer executor[{}] starting, mode: {}, configured AMS URL: {}",
        threadId,
        getConfig().isMasterSlaveMode() ? "master-slave" : "single-node",
        getConfig().getAmsUrl());

    while (isStarted()) {
      List<String> amsUrls = getAmsNodeUrls();

      // Log only when the node list changes, to avoid spamming on every poll cycle.
      if (!amsUrls.equals(lastLoggedAmsUrls)) {
        LOG.info(
            "Optimizer executor[{}] AMS node list changed: {} -> {}",
            threadId,
            lastLoggedAmsUrls.isEmpty() ? "(none)" : lastLoggedAmsUrls,
            amsUrls.isEmpty() ? "(none)" : amsUrls);
        lastLoggedAmsUrls = new ArrayList<>(amsUrls);
      }

      if (amsUrls.isEmpty()) {
        // No AMS nodes discovered yet; warn periodically (first time and every 12 cycles ≈ 1 min).
        if (consecutiveEmptyPolls == 0 || consecutiveEmptyPolls % 12 == 0) {
          LOG.warn(
              "Optimizer executor[{}] no AMS nodes available (mode: {}, url: {}), "
                  + "will retry in {}ms",
              threadId,
              getConfig().isMasterSlaveMode() ? "master-slave" : "single-node",
              getConfig().getAmsUrl(),
              basePollInterval);
        }
        consecutiveEmptyPolls++;
        waitAShortTime(basePollInterval);
        continue;
      }

      // Shuffle to prevent all optimizers from hitting the same AMS node simultaneously
      if (amsUrls.size() > 1) {
        Collections.shuffle(amsUrls, random);
        LOG.debug(
            "Optimizer executor[{}] poll round starting, shuffled {} nodes: {}",
            threadId,
            amsUrls.size(),
            amsUrls);
      }

      boolean hasTask = false;
      for (int i = 0; i < amsUrls.size(); i++) {
        String amsUrl = amsUrls.get(i);
        if (!isStarted()) {
          break;
        }

        LOG.debug(
            "Optimizer executor[{}] polling node [{}/{}]: {}",
            threadId,
            i + 1,
            amsUrls.size(),
            amsUrl);

        OptimizingTask ackTask = null;
        OptimizingTaskResult result = null;
        try {
          OptimizingTask task = pollTask(amsUrl);
          if (task != null && ackTask(amsUrl, task)) {
            ackTask = task;
            hasTask = true;
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

      if (hasTask) {
        if (consecutiveEmptyPolls >= emptyPollThreshold) {
          LOG.info(
              "Optimizer executor[{}] found task after {} consecutive empty polls ({}), "
                  + "resuming normal poll interval",
              threadId,
              consecutiveEmptyPolls,
              amsUrls);
        }
        consecutiveEmptyPolls = 0;
      } else {
        consecutiveEmptyPolls++;
      }

      long waitTime;
      if (consecutiveEmptyPolls >= emptyPollThreshold) {
        int backoffFactor = consecutiveEmptyPolls - emptyPollThreshold + 1;
        waitTime = Math.min(maxPollInterval, basePollInterval * (1L << backoffFactor));
        if (consecutiveEmptyPolls == emptyPollThreshold) {
          LOG.info(
              "Optimizer executor[{}] no tasks found for {} consecutive polls across {} node(s) {}, "
                  + "slowing down poll interval to {}ms (max: {}ms)",
              threadId,
              consecutiveEmptyPolls,
              amsUrls.size(),
              amsUrls,
              waitTime,
              maxPollInterval);
        } else {
          LOG.debug(
              "Optimizer executor[{}] no tasks found for {} consecutive polls, "
                  + "using increased poll interval: {}ms",
              threadId,
              consecutiveEmptyPolls,
              waitTime);
        }
      } else {
        waitTime = basePollInterval;
      }

      waitAShortTime(waitTime);
    }
  }

  public int getThreadId() {
    return threadId;
  }

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

  protected OptimizingTaskResult executeTask(OptimizingTask task) {
    return executeTask(getConfig(), getThreadId(), task, LOG);
  }

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

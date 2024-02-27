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

package com.netease.arctic.optimizer.common;

import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.optimizing.OptimizingExecutor;
import com.netease.arctic.optimizing.OptimizingExecutorFactory;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.utils.ExceptionUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.common.DynConstructors;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class OptimizerExecutor extends AbstractOptimizerOperator {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizerExecutor.class);

  private final int threadId;
  private Map<String, String> runtimeContext = new ConcurrentHashMap<String, String>();
  private Consumer metricsReporter = null;

  public OptimizerExecutor(OptimizerConfig config, int threadId) {
    super(config);
    this.threadId = threadId;
  }

  public void addRuntimeContext(String key, String value) {
    runtimeContext.put(key, value);
  }

  public void setMetricReporter(Consumer<Integer> metricsReporter) {
    this.metricsReporter = metricsReporter;
  }

  public void start() {
    while (isStarted()) {
      try {
        OptimizingTask task = pollTask();
        if (task != null && ackTask(task)) {
          OptimizingTaskResult result = executeTask(task);
          if (metricsReporter != null) {
            // reporter metrics by flink, counter the number of tasks consumed
            metricsReporter.accept(1);
          }
          completeTask(result);
        }
      } catch (Throwable t) {
        LOG.error("Optimizer executor[{}] got an unexpected error", threadId, t);
      }
    }
  }

  public int getThreadId() {
    return threadId;
  }

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
    OptimizingTaskResult result = executeTask(getConfig(), getThreadId(), task, LOG);
    // add optimizer flink runtime info, including application_id, tm_id, host
    StringBuilder sb = new StringBuilder();
    if (StringUtils.isNotEmpty(result.getErrorMessage())) {
      if (runtimeContext != null && runtimeContext.size() > 0) {
        runtimeContext.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
      }
      result.setErrorMessage(sb.toString() + result.getErrorMessage());
    }
    return result;
  }

  protected void completeTask(OptimizingTaskResult optimizingTaskResult) {
    try {
      callAuthenticatedAms(
          (client, token) -> {
            client.completeTask(token, optimizingTaskResult);
            return null;
          });
      LOG.info(
          "Optimizer executor[{}] completed task[{}] to ams",
          threadId,
          optimizingTaskResult.getTaskId());
    } catch (TException exception) {
      LOG.error(
          "Optimizer executor[{}] completed task[{}] failed",
          threadId,
          optimizingTaskResult.getTaskId(),
          exception);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static OptimizingTaskResult executeTask(
      OptimizerConfig config, int threadId, OptimizingTask task, Logger logger) {
    long startTime = System.currentTimeMillis();
    TableOptimizing.OptimizingInput input = null;
    try {
      OptimizingInputProperties properties = OptimizingInputProperties.parse(task.getProperties());
      input = SerializationUtil.simpleDeserialize(task.getTaskInput());
      String executorFactoryImpl = properties.getExecutorFactoryImpl();
      DynConstructors.Ctor<OptimizingExecutorFactory> ctor =
          DynConstructors.builder(OptimizingExecutorFactory.class)
              .impl(executorFactoryImpl)
              .buildChecked();
      OptimizingExecutorFactory factory = ctor.newInstance();

      if (config.isExtendDiskStorage()) {
        properties.enableSpillMap();
      }
      properties.setMaxSizeInMemory(config.getMemoryStorageSize() * 1024 * 1024);
      properties.setSpillMapPath(config.getDiskStoragePath());
      factory.initialize(properties.getProperties());

      OptimizingExecutor executor = factory.createExecutor(input);
      TableOptimizing.OptimizingOutput output = executor.execute();
      ByteBuffer outputByteBuffer = SerializationUtil.simpleSerialize(output);
      OptimizingTaskResult result = new OptimizingTaskResult(task.getTaskId(), threadId);
      result.setTaskOutput(outputByteBuffer);
      result.setSummary(output.summary());
      logger.info(
          "Optimizer executor[{}] executed task[{}]({}) and cost {}",
          threadId,
          task.getTaskId(),
          input,
          System.currentTimeMillis() - startTime);
      return result;
    } catch (Throwable t) {
      logger.error(
          "Optimizer executor[{}] executed task[{}]({}) failed and cost {}",
          threadId,
          task.getTaskId(),
          input,
          System.currentTimeMillis() - startTime,
          t);
      OptimizingTaskResult errorResult = new OptimizingTaskResult(task.getTaskId(), threadId);
      errorResult.setErrorMessage(ExceptionUtil.getErrorMessage(t, 4000));
      return errorResult;
    }
  }
}

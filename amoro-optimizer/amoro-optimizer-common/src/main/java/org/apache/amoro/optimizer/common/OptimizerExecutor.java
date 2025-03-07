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
import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingExecutorFactory;
import org.apache.amoro.optimizing.OptimizingInputProperties;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.apache.amoro.utils.ExceptionUtil;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.common.DynConstructors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class OptimizerExecutor extends AbstractOptimizerOperator {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizerExecutor.class);
  protected static final int ERROR_MESSAGE_MAX_LENGTH = 4000;

  private final int threadId;

  public OptimizerExecutor(OptimizerConfig config, int threadId) {
    super(config);
    this.threadId = threadId;
  }

  public void start() {
    while (isStarted()) {
      try {
        OptimizingTask task = pollTask();
        if (task != null && ackTask(task)) {
          OptimizingTaskResult result = executeTask(task);
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
    return executeTask(getConfig(), getThreadId(), task, LOG);
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
}

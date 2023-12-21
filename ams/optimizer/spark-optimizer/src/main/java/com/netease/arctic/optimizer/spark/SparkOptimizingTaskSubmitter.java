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

package com.netease.arctic.optimizer.spark;

import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.optimizer.common.AbstractOptimizerOperator;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The {@code SparkOptimizingTaskSubmitter} takes OptimizingTask from AMS and wraps it as a spark
 * job, then submit to the spark environment.
 */
public class SparkOptimizingTaskSubmitter extends AbstractOptimizerOperator implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(SparkOptimizingTaskSubmitter.class);
  private final SparkOptimizingTaskExecuteFunction sparkOptimizingTaskExecuteFunction;
  private final JavaSparkContext jsc;
  private final SparkOptimizerConfig sparkOptimizerConfig;
  private final int threadId;

  public SparkOptimizingTaskSubmitter(
      JavaSparkContext jsc, SparkOptimizerConfig config, int threadId) {
    super(config);
    this.sparkOptimizerConfig = config;
    this.jsc = jsc;
    this.threadId = threadId;
    this.sparkOptimizingTaskExecuteFunction =
        new SparkOptimizingTaskExecuteFunction(config, threadId);
  }

  @Override
  public void run() {
    try {
      LOG.info("Starting to poll optimizing tasks.");
      while (isStarted()) {
        OptimizingTask task = pollTask();
        if (task != null && ackTask(task)) {
          submitOptimizingTask(task);
          LOG.info("Polled and submitted the task {}", task);
        }
      }
    } catch (Throwable e) {
      LOG.error("Failed to poll task", e);
    }
  }

  public void submitOptimizingTask(OptimizingTask task) {
    try {
      String threadName = Thread.currentThread().getName();
      long startTime = System.currentTimeMillis();
      LOG.info("Now [{}] execute task {}", threadName, task);
      ImmutableList<OptimizingTask> of = ImmutableList.of(task);
      String jobDesc =
          String.format("%s | TableName(%s)", task.getTaskId(), parseFullTableName(task));
      jsc.setJobDescription(jobDesc);
      List<OptimizingTaskResult> result =
          jsc.parallelize(of, 1).map(sparkOptimizingTaskExecuteFunction).collect();
      result.forEach(SparkOptimizingTaskSubmitter.this::completeTask);
      LOG.info(
          "[{}] execute task {}, completed {} and time {} ms",
          threadName,
          task,
          result.get(0),
          System.currentTimeMillis() - startTime);

    } catch (Throwable r) {
      LOG.error("Failed to execute optimizing task.", r);
    }
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

  private OptimizingTask pollTask() {
    OptimizingTask task = null;
    long startTime = System.currentTimeMillis();
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
      if (System.currentTimeMillis() - startTime > sparkOptimizerConfig.getTaskPollingTimeout()) {
        LOG.warn(
            "Waited for {} s, the sparkOptimizingTaskSubmitter-{} will exit",
            sparkOptimizerConfig.getTaskPollingTimeout(),
            threadId);
        stop();
      }
    }
    return task;
  }

  public void completeTask(OptimizingTaskResult optimizingTaskResult) {
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

  private String parseFullTableName(OptimizingTask task) {
    String tableName = null;
    TableOptimizing.OptimizingInput input =
        SerializationUtil.simpleDeserialize(task.getTaskInput());
    if (input instanceof RewriteFilesInput) {
      tableName = ((RewriteFilesInput) input).getTable().name();
    } else {
      // TODO resolve other input types in future
    }
    return tableName;
  }
}

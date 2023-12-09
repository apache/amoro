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

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.optimizer.common.AbstractOptimizerOperator;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SparkExecutor extends AbstractOptimizerOperator {
  private static final Logger LOG = LoggerFactory.getLogger(SparkExecutor.class);
  private final ThreadPoolExecutor executorService;
  private final SparkFunction sparkFunction;
  private final JavaSparkContext jsc;
  private final int threadId;

  public SparkExecutor(JavaSparkContext jsc, SparkOptimizerConfig config, int threadId) {
    super(config);
    this.jsc = jsc;
    this.threadId = threadId;
    this.sparkFunction = new SparkFunction(config, threadId);
    ThreadFactory executorFactory =
        new ThreadFactoryBuilder()
            .setDaemon(false)
            .setNameFormat("spark-optimizing-executor-%d")
            .build();
    this.executorService =
        new ThreadPoolExecutor(
            0,
            config.getExecutionParallel(),
            30L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(config.getExecutionParallel()),
            executorFactory);
  }

  public ThreadPoolExecutor getExecutorService() {
    return this.executorService;
  }

  public void submitOptimizingTask(OptimizingTask task) {
    executorService.execute(
        () -> {
          try {
            String threadName = Thread.currentThread().getName();
            long startTime = System.currentTimeMillis();
            LOG.info("Now [{}] execute task {}", threadName, task);
            ImmutableList<OptimizingTask> of = ImmutableList.of(task);
            String jobDesc =
                String.format("%s | TableName(%s)", task.getTaskId(), parseFullTableName(task));
            jsc.setJobDescription(jobDesc);
            List<OptimizingTaskResult> result = jsc.parallelize(of, 1).map(sparkFunction).collect();
            result.forEach(SparkExecutor.this::completeTask);
            LOG.info(
                "[{}] execute task {}, completed {} and time {} ms",
                threadName,
                task,
                result.get(0),
                System.currentTimeMillis() - startTime);

          } catch (Throwable r) {
            LOG.error("Failed to execute optimizing task.", r);
          }
        });
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

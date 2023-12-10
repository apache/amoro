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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.PropertyNames;
import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.common.AbstractOptimizerOperator;
import com.netease.arctic.optimizer.common.OptimizerToucher;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.Utils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class SparkOptimizer {
  private static final Logger LOG = LoggerFactory.getLogger(SparkOptimizer.class);
  private static final String APP_NAME = "amoro-spark-optimizer";

  public static void main(String[] args) throws Exception {
    SparkSession spark = SparkSession.builder().appName(APP_NAME).getOrCreate();
    JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
    SparkOptimizerConfig config = new SparkOptimizerConfig(args);
    if (!jsc.getConf().getBoolean("spark.dynamicAllocation.enabled", false)) {
      LOG.warn(
          "To better utilize computing resources, it is recommended to enable 'spark.dynamicAllocation.enabled' "
              + "and set 'spark.executor.cores' * 'spark.dynamicAllocation.maxExecutors' greater than or equal to 'OPTIMIZER_EXECUTION_PARALLEL'");
    }
    int driverMemory = Utils.memoryStringToMb(jsc.getConf().get("spark.driver.memory", "1g"));
    if (config.getMemorySize() < driverMemory) {
      config.setMemorySize(driverMemory);
    }

    OptimizerToucher toucher = new OptimizerToucher(config);
    if (config.getResourceId() != null) {
      toucher.withRegisterProperty(PropertyNames.RESOURCE_ID, config.getResourceId());
    }
    toucher.withRegisterProperty(Resource.PROPERTY_JOB_ID, spark.sparkContext().applicationId());
    LOG.info("Starting optimizer with configuration:{}", config);

    int threadId = Thread.currentThread().hashCode();
    SparkExecutor sparkExecutor = new SparkExecutor(jsc, config, threadId);
    ThreadFactory consumerFactory =
        new ThreadFactoryBuilder()
            .setDaemon(false)
            .setNameFormat("spark-optimizing-puller-%d")
            .build();
    ExecutorService pullerService = Executors.newSingleThreadExecutor(consumerFactory);
    TaskPuller puller = new TaskPuller(config, threadId, sparkExecutor);
    pullerService.execute(puller);
    toucher
        .withTokenChangeListener(
            newToken -> {
              puller.setToken(newToken);
              sparkExecutor.setToken(newToken);
            })
        .start();
  }

  static class TaskPuller extends AbstractOptimizerOperator implements Runnable {
    private final SparkExecutor sparkExecutor;
    private final SparkOptimizerConfig config;
    private final int threadId;

    private TaskPuller(SparkOptimizerConfig config, int threadId, SparkExecutor sparkExecutor) {
      super(config);
      this.config = config;
      this.threadId = threadId;
      this.sparkExecutor = sparkExecutor;
    }

    @Override
    public void run() {
      try {
        ThreadPoolExecutor executorService = sparkExecutor.getExecutorService();
        LOG.info("Starting to poll optimizing tasks.");
        while (true) {
          int activeCount = executorService.getActiveCount();
          if (activeCount >= executorService.getMaximumPoolSize()) {
            LOG.info("Polled {} tasks, the queue is full", activeCount);
            waitAShortTime();
          } else {
            OptimizingTask task = pollTask();
            if (task != null && ackTask(task)) {
              sparkExecutor.submitOptimizingTask(task);
              LOG.info("Polled and submitted the task {}", task);
            }
          }
        }
      } catch (Throwable e) {
        LOG.error("Failed to poll task", e);
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
        if (System.currentTimeMillis() - startTime > config.getPollingTimeout()
            && sparkExecutor.getExecutorService().getActiveCount() == 0) {
          LOG.warn("Waited for {} s, the optimizer will exit", config.getPollingTimeout());
          System.exit(0);
        }
      }
      return task;
    }
  }
}

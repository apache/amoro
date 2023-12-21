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

import com.netease.arctic.ams.api.OptimizerProperties;
import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.common.OptimizerToucher;
import org.apache.iceberg.relocated.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/** The {@code SparkOptimizer} acts as an entrypoint of the spark program */
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
      toucher.withRegisterProperty(OptimizerProperties.RESOURCE_ID, config.getResourceId());
    }
    toucher.withRegisterProperty(Resource.PROPERTY_JOB_ID, spark.sparkContext().applicationId());
    LOG.info("Starting optimizer with configuration:{}", config);

    ThreadFactory executorFactory =
        new ThreadFactoryBuilder()
            .setDaemon(false)
            .setNameFormat("spark-optimizer-task-submitter-%d")
            .build();
    ThreadPoolExecutor executorService =
        new ThreadPoolExecutor(
            config.getExecutionParallel(),
            config.getExecutionParallel(),
            30L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(config.getExecutionParallel()),
            executorFactory);
    SparkOptimizingTaskSubmitter[] submitters =
        new SparkOptimizingTaskSubmitter[config.getExecutionParallel()];
    IntStream.range(0, config.getExecutionParallel())
        .forEach(
            i -> {
              SparkOptimizingTaskSubmitter sparkOptimizingTaskSubmitter =
                  new SparkOptimizingTaskSubmitter(jsc, config, i);
              executorService.execute(sparkOptimizingTaskSubmitter);
              submitters[i] = sparkOptimizingTaskSubmitter;
            });

    // check whether the spark driver can exit normally in the current schedule time
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(
        () -> {
          if (executorService.getActiveCount() == 0) {
            executorService.shutdown();
            System.exit(0);
          }
        },
        0,
        1,
        TimeUnit.MINUTES);

    toucher
        .withTokenChangeListener(
            newToken -> {
              Arrays.stream(submitters)
                  .forEach(optimizerExecutor -> optimizerExecutor.setToken(newToken));
            })
        .start();
  }
}

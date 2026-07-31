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

package org.apache.amoro.optimizer.spark;

import org.apache.amoro.optimizer.common.Optimizer;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.optimizer.common.OptimizerToucher;
import org.apache.amoro.resource.Resource;
import org.apache.hadoop.util.ShutdownHookManager;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/** The {@code SparkOptimizer} acts as an entrypoint of the spark program */
public class SparkOptimizer extends Optimizer {
  private static final Logger LOG = LoggerFactory.getLogger(SparkOptimizer.class);
  private static final String APP_NAME_FORMAT = "amoro-spark-optimizer-%s";

  public SparkOptimizer(OptimizerConfig config, JavaSparkContext jsc) {
    super(
        config,
        () -> new OptimizerToucher(config),
        (i) -> new SparkOptimizerExecutor(jsc, config, i));
  }

  public static void main(String[] args) throws Exception {
    OptimizerConfig config = new OptimizerConfig(args);
    SparkSession spark =
        SparkSession.builder()
            .appName(String.format(APP_NAME_FORMAT, config.getResourceId()))
            .getOrCreate();
    JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
    if (!jsc.getConf().getBoolean("spark.dynamicAllocation.enabled", false)) {
      LOG.warn(
          "To better utilize computing resources, it is recommended to enable 'spark.dynamicAllocation.enabled' "
              + "and set 'spark.dynamicAllocation.maxExecutors' equal to 'OPTIMIZER_EXECUTION_PARALLEL'");
    }

    // calculate optimizer memory allocation
    int driverMemory = Utils.memoryStringToMb(jsc.getConf().get("spark.driver.memory", "1g"));
    int executorMemory = Utils.memoryStringToMb(jsc.getConf().get("spark.executor.memory", "1g"));
    int executorCores = jsc.getConf().getInt("spark.executor.cores", 1);
    int executionParallel = config.getExecutionParallel();
    int executorNum = (int) Math.ceil((double) executionParallel / executorCores);
    config.setMemorySize(driverMemory + executorNum * executorMemory);

    SparkOptimizer optimizer = new SparkOptimizer(config, jsc);
    OptimizerToucher toucher = optimizer.getToucher();
    toucher.withRegisterProperty(Resource.PROPERTY_JOB_ID, spark.sparkContext().applicationId());

    // Register with Hadoop's ShutdownHookManager so that our hook runs to completion
    // before downstream cleanup. ShutdownHookManager runs hooks in priority descending
    // order, sequentially. Two cleanups must come AFTER our graceful shutdown:
    //   - Hadoop FileSystem cache close (priority FS_CACHE = 10) — would otherwise
    //     close in-flight HDFS writers and cause ClosedChannelException on flush.
    //   - SparkContext.stop (Spark's SPARK_CONTEXT_SHUTDOWN_PRIORITY = 50) — would
    //     otherwise tear down executors mid-task, failing in-flight RDD actions.
    // Use SPARK_CONTEXT_SHUTDOWN_PRIORITY + 10 to guarantee both ordering constraints
    // in a single value (60 > 50 > 10).
    // Pass an explicit timeout — the 2-arg overload uses hadoop.service.shutdown.timeout
    // (default 30s), which would cancel our hook well before stopOptimizing's
    // shutdownTimeoutMs deadline.
    int shutdownPriority =
        org.apache.spark.util.ShutdownHookManager.SPARK_CONTEXT_SHUTDOWN_PRIORITY() + 10;
    ShutdownHookManager.get()
        .addShutdownHook(
            () -> {
              LOG.info("Received shutdown signal, initiating graceful shutdown...");
              optimizer.stopOptimizing();
            },
            shutdownPriority,
            config.getShutdownTimeoutMs() + 10_000L,
            TimeUnit.MILLISECONDS);
    LOG.info("Starting the spark optimizer with configuration:{}", config);
    optimizer.startOptimizing();
  }
}

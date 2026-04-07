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
 *
 * Modified by Datazip Inc. in 2026
 */

package org.apache.amoro.optimizer.spark;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.log.OptimizingTaskLogContext;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.optimizer.common.OptimizerExecutor;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * The {@code SparkOptimizingTaskExecuteFunction} defines the whole processing logic that how to
 * execute {@code OptimizingTask}
 */
public class SparkOptimizingTaskFunction implements Function<OptimizingTask, OptimizingTaskResult> {
  private static final Logger LOG = LoggerFactory.getLogger(SparkOptimizingTaskFunction.class);
  private final OptimizerConfig config;
  private final int threadId;

  public SparkOptimizingTaskFunction(OptimizerConfig config, int threadId) {
    this.config = config;
    this.threadId = threadId;
  }

  @Override
  public OptimizingTaskResult call(OptimizingTask task) {
    // Set MDC context on Spark executor for Log4j2 routing
    // Executor logs go to: <LOG_DIR>/<processId>/<taskId>.log
    long processId = task.getTaskId().getProcessId();
    int taskId = task.getTaskId().getTaskId();
    String logFilePath = processId + "/" + taskId;

    // Set OptimizingTaskLogContext FIRST so AbstractRewriteFilesExecutor.execute()
    // sees isContextSet()==true and does NOT override our MDC with its own format.
    OptimizingTaskLogContext.setContext(processId, taskId);

    // Override logFilePath to our desired format
    MDC.put("processId", String.valueOf(processId));
    MDC.put("taskId", String.valueOf(taskId));
    MDC.put("logFilePath", logFilePath);

    try {
      OptimizingTaskResult result = OptimizerExecutor.executeTask(config, threadId, task, LOG);
      return result;
    } catch (Exception e) {
      LOG.error("Task execution failed on executor", e);
      throw e;
    } finally {
      OptimizingTaskLogContext.clearContext();
      MDC.remove("processId");
      MDC.remove("taskId");
      MDC.remove("logFilePath");
    }
  }
}

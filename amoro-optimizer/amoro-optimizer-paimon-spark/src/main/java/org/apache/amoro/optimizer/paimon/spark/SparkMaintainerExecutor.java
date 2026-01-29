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

package org.apache.amoro.optimizer.paimon.spark;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.maintainer.MaintainerInput;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.optimizer.common.OptimizerExecutor;
import org.apache.amoro.optimizer.paimon.spark.maintainer.SparkMaintainerTaskFunction;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.utils.ExceptionUtil;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Executor for maintainer tasks in Spark environment. Similar to SparkOptimizerExecutor but handles
 * maintainer operations.
 */
public class SparkMaintainerExecutor extends OptimizerExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(SparkMaintainerExecutor.class);
  private final JavaSparkContext jsc;
  private final int threadId;

  public SparkMaintainerExecutor(JavaSparkContext jsc, OptimizerConfig config, int threadId) {
    super(config, threadId);
    this.jsc = jsc;
    this.threadId = threadId;
  }

  @Override
  protected OptimizingTaskResult executeTask(OptimizingTask task) {
    OptimizingTaskResult result;
    String threadName = Thread.currentThread().getName();
    long startTime = System.currentTimeMillis();
    try {
      ImmutableList<OptimizingTask> of = ImmutableList.of(task);
      jsc.setJobDescription(jobDescription(task));
      SparkMaintainerTaskFunction taskFunction =
          new SparkMaintainerTaskFunction(getConfig(), threadId);
      List<OptimizingTaskResult> results = jsc.parallelize(of, 1).map(taskFunction).collect();
      result = results.get(0);
      LOG.info(
          "Maintainer executor[{}] executed task[{}] and cost {} ms",
          threadName,
          task.getTaskId(),
          System.currentTimeMillis() - startTime);
      return result;
    } catch (Throwable r) {
      LOG.error(
          "Maintainer executor[{}] executed task[{}] failed, and cost {} ms",
          threadName,
          task.getTaskId(),
          (System.currentTimeMillis() - startTime),
          r);
      result = new OptimizingTaskResult(task.getTaskId(), threadId);
      result.setErrorMessage(ExceptionUtil.getErrorMessage(r, ERROR_MESSAGE_MAX_LENGTH));
      return result;
    }
  }

  private String jobDescription(OptimizingTask task) {
    MaintainerInput input = SerializationUtil.simpleDeserialize(task.getTaskInput());
    return String.format(
        "Amoro Paimon maintainer task, operation: %s, table: %s, task id: %s",
        input.getOperationType(), input.getTableIdentifier(), task.getTaskId());
  }
}

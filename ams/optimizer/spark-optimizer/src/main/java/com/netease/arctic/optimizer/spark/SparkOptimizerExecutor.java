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
import com.netease.arctic.optimizer.common.OptimizerConfig;
import com.netease.arctic.optimizer.common.OptimizerExecutor;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.utils.ExceptionUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The {@code SparkOptimizerExecutor} takes OptimizingTask from AMS and wraps it as a spark job,
 * then submit to the spark environment.
 */
public class SparkOptimizerExecutor extends OptimizerExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(SparkOptimizerExecutor.class);
  private final JavaSparkContext jsc;
  private final int threadId;

  public SparkOptimizerExecutor(JavaSparkContext jsc, OptimizerConfig config, int threadId) {
    super(config, threadId);
    this.jsc = jsc;
    this.threadId = threadId;
  }

  @Override
  protected OptimizingTaskResult executeTask(OptimizingTask task) {
    OptimizingTaskResult result;
    String threadName = Thread.currentThread().getName();
    try {
      long startTime = System.currentTimeMillis();
      ImmutableList<OptimizingTask> of = ImmutableList.of(task);
      jsc.setJobDescription(jobDescription(task));
      SparkOptimizingTaskFunction taskFunction =
          new SparkOptimizingTaskFunction(getConfig(), threadId);
      List<OptimizingTaskResult> results = jsc.parallelize(of, 1).map(taskFunction).collect();
      result = results.get(0);
      LOG.info(
          "Optimizer executor[{}] executed task[{}] and cost {}",
          threadName,
          task.getTaskId(),
          System.currentTimeMillis() - startTime);
      return result;
    } catch (Throwable r) {
      LOG.error(
          "Optimizer executor[{}] executed task[{}] failed, and cost {}", threadName, task, r);
      result = new OptimizingTaskResult(task.getTaskId(), threadId);
      result.setErrorMessage(ExceptionUtil.getErrorMessage(r, 4000));
      return result;
    }
  }

  private String jobDescription(OptimizingTask task) {
    String description;
    TableOptimizing.OptimizingInput input =
        SerializationUtil.simpleDeserialize(task.getTaskInput());
    if (input instanceof RewriteFilesInput) {
      description =
          String.format(
              "Amoro rewrite files task, table name:%s, task id:%s",
              task.getTaskId(), ((RewriteFilesInput) input).getTable().name());
    } else {
      throw new IllegalArgumentException("Unsupported task:" + input.getClass());
    }
    return description;
  }
}

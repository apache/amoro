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

package org.apache.amoro.optimizer.paimon.spark.maintainer;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.maintainer.MaintainerExecutor;
import org.apache.amoro.maintainer.MaintainerExecutorFactory;
import org.apache.amoro.maintainer.MaintainerInput;
import org.apache.amoro.maintainer.MaintainerOutput;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.utils.ExceptionUtil;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.common.DynConstructors;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Spark function to execute maintainer tasks. Similar to SparkOptimizingTaskFunction but for
 * maintainer operations.
 */
public class SparkMaintainerTaskFunction implements Function<OptimizingTask, OptimizingTaskResult> {
  private static final Logger LOG = LoggerFactory.getLogger(SparkMaintainerTaskFunction.class);
  private static final int ERROR_MESSAGE_MAX_LENGTH = 4000;

  private final OptimizerConfig config;
  private final int threadId;

  public SparkMaintainerTaskFunction(OptimizerConfig config, int threadId) {
    this.config = config;
    this.threadId = threadId;
  }

  @Override
  public OptimizingTaskResult call(OptimizingTask task) throws Exception {
    long startTime = System.currentTimeMillis();
    MaintainerInput input;
    try {
      Map<String, String> taskProperties = fillTaskProperties(config, task);
      input = SerializationUtil.simpleDeserialize(task.getTaskInput());

      String factoryImpl = taskProperties.get(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL);
      DynConstructors.Ctor<MaintainerExecutorFactory> ctor =
          DynConstructors.builder(MaintainerExecutorFactory.class).impl(factoryImpl).buildChecked();
      MaintainerExecutorFactory factory = ctor.newInstance();

      factory.initialize(taskProperties);
      MaintainerExecutor executor = factory.createExecutor(input);
      MaintainerOutput output = (MaintainerOutput) executor.execute();

      ByteBuffer outputByteBuffer = SerializationUtil.simpleSerialize(output);
      OptimizingTaskResult result = new OptimizingTaskResult(task.getTaskId(), threadId);
      result.setTaskOutput(outputByteBuffer);
      result.setSummary(output.summary());

      LOG.info(
          "Maintainer executor[{}] executed task[{}]({}) and cost {} ms",
          threadId,
          task.getTaskId(),
          input,
          System.currentTimeMillis() - startTime);
      return result;
    } catch (Throwable t) {
      LOG.error(
          "Maintainer executor[{}] executed task[{}] failed and cost {} ms",
          threadId,
          task.getTaskId(),
          System.currentTimeMillis() - startTime,
          t);
      OptimizingTaskResult errorResult = new OptimizingTaskResult(task.getTaskId(), threadId);
      errorResult.setErrorMessage(ExceptionUtil.getErrorMessage(t, ERROR_MESSAGE_MAX_LENGTH));
      return errorResult;
    }
  }

  private static Map<String, String> fillTaskProperties(
      OptimizerConfig config, OptimizingTask task) {
    Map<String, String> properties = Maps.newHashMap(task.getProperties());
    properties.put(TaskProperties.PROCESS_ID, String.valueOf(task.getTaskId().getProcessId()));
    return properties;
  }
}

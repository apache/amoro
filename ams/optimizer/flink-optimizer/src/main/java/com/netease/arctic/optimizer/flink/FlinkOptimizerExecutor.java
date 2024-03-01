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

package com.netease.arctic.optimizer.flink;

import com.google.common.base.Strings;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.optimizer.common.OptimizerConfig;
import com.netease.arctic.optimizer.common.OptimizerExecutor;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OptimizerExecutor For flink engine only . 1、You can customize special execution logic for Flink
 * here. 2、You can customize logic that will be called after task execution and before reporting
 * task results to AMS.
 */
public class FlinkOptimizerExecutor extends OptimizerExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizerExecutor.class);
  private Map<String, String> runtimeContext = new ConcurrentHashMap<String, String>();
  private MetricGroup operatorMetricGroup;
  private Counter taskCounter = null;

  public FlinkOptimizerExecutor(OptimizerConfig config, int threadId) {
    super(config, threadId);
  }

  public void addRuntimeContext(String key, String value) {
    runtimeContext.put(key, value);
  }

  public void initOperatorMetric(MetricGroup metricGroup) {
    this.operatorMetricGroup = metricGroup;
    taskCounter = this.operatorMetricGroup.addGroup("amoro").addGroup("optimizer").counter("tasks");
  }

  @Override
  public void callBeforeTaskComplete(OptimizingTaskResult result) {
    if (taskCounter != null) {
      // reporter metrics by flink, counter the number of tasks consumed
      taskCounter.inc();
    }
  }

  @Override
  protected OptimizingTaskResult executeTask(OptimizingTask task) {
    OptimizingTaskResult result = executeTask(getConfig(), getThreadId(), task, LOG);
    // add optimizer flink runtime info, including application_id, tm_id
    StringBuilder sb = new StringBuilder();
    if (!Strings.isNullOrEmpty(result.getErrorMessage())) {
      if (runtimeContext != null && runtimeContext.size() > 0) {
        runtimeContext.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
      }
      result.setErrorMessage(sb.toString() + result.getErrorMessage());
    }
    return result;
  }
}

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

package org.apache.amoro.optimizer.flink;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.client.OptimizingClientPools;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.optimizer.common.OptimizerExecutor;
import org.apache.amoro.shade.guava32.com.google.common.base.Strings;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimizer executor For Flink engine supports:
 *
 * <ul>
 *   <li>Add additional content to error messages to help locate execution nodes.
 *   <li>Add some Flink metrics like task number executed.
 * </ul>
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

  /**
   * Best-effort heartbeat with the current token, usable after stop(). Flink cancels the
   * FlinkToucher source before this operator, so the drain in {@link FlinkExecutor#close()} keeps
   * the registration alive by touching AMS directly — otherwise AMS expires the optimizer after its
   * heartbeat timeout and resets the in-flight task, dropping the drained result. Failures are
   * swallowed; the next drain heartbeat simply retries. This never re-registers, consistent with
   * the drain-mode toucher.
   */
  void bestEffortTouch() {
    String currentToken = getToken();
    if (currentToken == null) {
      return;
    }
    try {
      OptimizingClientPools.getClient(getConfig().getAmsUrl()).touch(currentToken);
    } catch (Exception e) {
      LOG.debug("Best-effort touch to AMS failed, will retry on the next drain heartbeat", e);
    }
  }

  public void initOperatorMetric(MetricGroup metricGroup) {
    this.operatorMetricGroup = metricGroup;
    taskCounter = this.operatorMetricGroup.addGroup("amoro").addGroup("optimizer").counter("tasks");
  }

  private void callBeforeTaskComplete() {
    if (taskCounter != null) {
      // reporter metrics by flink, counter the number of tasks consumed
      taskCounter.inc();
    }
  }

  @Override
  protected OptimizingTaskResult executeTask(OptimizingTask task) {
    OptimizingTaskResult result = executeTask(getConfig(), getThreadId(), task, LOG);
    callBeforeTaskComplete();
    // add optimizer flink runtime info, including application_id, tm_id
    StringBuilder sb = new StringBuilder();
    if (!Strings.isNullOrEmpty(result.getErrorMessage())) {
      if (runtimeContext != null && !runtimeContext.isEmpty()) {
        runtimeContext.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
      }
      String errorMsg = sb + result.getErrorMessage();
      result.setErrorMessage(
          errorMsg.substring(0, Math.min(ERROR_MESSAGE_MAX_LENGTH, errorMsg.length())));
    }
    return result;
  }
}

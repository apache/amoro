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

import static org.apache.flink.configuration.HighAvailabilityOptions.HA_CLUSTER_ID;
import static org.apache.flink.configuration.TaskManagerOptions.TASK_MANAGER_RESOURCE_ID;

import org.apache.amoro.optimizer.common.OptimizerExecutor;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;

public class FlinkExecutor extends AbstractStreamOperator<Void>
    implements OneInputStreamOperator<String, Void> {

  private final OptimizerExecutor[] allExecutors;
  private FlinkOptimizerExecutor executor;
  private String optimizeGroupName;
  private Thread optimizerThread;

  public FlinkExecutor(OptimizerExecutor[] allExecutors, String optimizeGroupName) {
    this.allExecutors = allExecutors;
    this.optimizeGroupName = optimizeGroupName;
  }

  @Override
  public void open() throws Exception {
    super.open();
    int subTaskIndex = getRuntimeContext().getIndexOfThisSubtask();
    String taskManagerId =
        getRuntimeContext()
            .getTaskManagerRuntimeInfo()
            .getConfiguration()
            .get(TASK_MANAGER_RESOURCE_ID);
    String applicationId =
        getRuntimeContext().getTaskManagerRuntimeInfo().getConfiguration().getString(HA_CLUSTER_ID);
    executor = (FlinkOptimizerExecutor) allExecutors[subTaskIndex];
    // set optimizer flink runtime info, including application_id, tm_id, host
    if (applicationId != null) {
      executor.addRuntimeContext("application_id", applicationId);
    }
    if (taskManagerId != null) {
      executor.addRuntimeContext("tm_id", taskManagerId);
    }
    executor.addRuntimeContext("subtask_index", String.valueOf(subTaskIndex));
    // add label optimize_group;
    getMetricGroup().getAllVariables().put("<optimizer_group>", optimizeGroupName);
    executor.initOperatorMetric(getMetricGroup());
    optimizerThread =
        new Thread(() -> executor.start(), "flink-optimizer-executor-" + subTaskIndex);
    optimizerThread.setDaemon(true);
    optimizerThread.start();
  }

  @Override
  public void close() throws Exception {
    if (executor != null) {
      executor.stop();
    }
    if (optimizerThread != null && optimizerThread.isAlive()) {
      optimizerThread.interrupt();
      try {
        optimizerThread.join(5000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void processElement(StreamRecord<String> element) {
    executor.setToken(element.getValue());
  }
}

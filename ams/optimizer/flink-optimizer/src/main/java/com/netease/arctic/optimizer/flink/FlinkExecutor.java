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

import static org.apache.flink.configuration.HighAvailabilityOptions.HA_CLUSTER_ID;
import static org.apache.flink.configuration.TaskManagerOptions.TASK_MANAGER_RESOURCE_ID;

import com.netease.arctic.optimizer.common.OptimizerExecutor;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkExecutor extends AbstractStreamOperator<Void>
    implements OneInputStreamOperator<String, Void> {

  private static final Logger LOG = LoggerFactory.getLogger(FlinkExecutor.class);
  private final OptimizerExecutor[] allExecutors;
  private FlinkOptimizerExecutor executor;
  private String optimizeGroupName;

  public FlinkExecutor(OptimizerExecutor[] allExecutors) {
    this.allExecutors = allExecutors;
  }

  public FlinkExecutor(OptimizerExecutor[] allExecutors, String optimizeGroupName) {
    this.allExecutors = allExecutors;
    this.optimizeGroupName = optimizeGroupName;
  }

  @Override
  public void open() throws Exception {
    super.open();
    int subTaskIndex = getRuntimeContext().getIndexOfThisSubtask();
    String taskManagerContainerId =
        getRuntimeContext()
            .getTaskManagerRuntimeInfo()
            .getConfiguration()
            .get(TASK_MANAGER_RESOURCE_ID);
    String applicationId =
        getRuntimeContext().getTaskManagerRuntimeInfo().getConfiguration().getString(HA_CLUSTER_ID);
    executor = (FlinkOptimizerExecutor) allExecutors[subTaskIndex];
    // set optimizer flink runtime info, including application_id, tm_id, host
    executor.addRuntimeContext("application_id", applicationId);
    executor.addRuntimeContext("tm_id", taskManagerContainerId);
    // add label optimize_group;
    getMetricGroup().getAllVariables().put("<optimizer_group>", optimizeGroupName);
    Counter counter = getMetricGroup().addGroup("amoro").addGroup("optimizer").counter("tasks");
    executor.setMetricReporter(x -> counter.inc());
    new Thread(() -> executor.start(), "flink-optimizer-executor-" + subTaskIndex).start();
  }

  @Override
  public void close() throws Exception {
    executor.stop();
  }

  @Override
  public void processElement(StreamRecord<String> element) {
    executor.setToken(element.getValue());
  }
}

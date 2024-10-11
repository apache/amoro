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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.utils.SerializationUtil;

import java.util.Map;

public abstract class StagedTaskDescriptor<I, O, S> {

  protected long tableId;
  protected I input;
  protected volatile O output;
  protected volatile S summary;
  protected Map<String, String> properties;

  protected StagedTaskDescriptor() {}

  public StagedTaskDescriptor(long tableId, I input, Map<String, String> properties) {
    this.tableId = tableId;
    this.input = input;
    this.properties = properties;
    calculateSummary();
  }

  public void reset() {
    this.output = null;
    calculateSummary();
  }

  protected abstract void calculateSummary();

  protected abstract O deserializeOutput(byte[] outputBytes);

  public long getTableId() {
    return tableId;
  }

  public void setOutputBytes(byte[] outputBytes) {
    this.output = deserializeOutput(outputBytes);
    calculateSummary();
  }

  public void setInput(I input) {
    this.input = input;
    calculateSummary();
  }

  public I getInput() {
    return input;
  }

  public O getOutput() {
    return output;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public S getSummary() {
    return summary;
  }

  public OptimizingTask extractProtocolTask(OptimizingTaskId taskId) {
    OptimizingTask optimizingTask = new OptimizingTask(taskId);
    optimizingTask.setTaskInput(SerializationUtil.simpleSerialize(input));
    optimizingTask.setProperties(properties);
    return optimizingTask;
  }
}

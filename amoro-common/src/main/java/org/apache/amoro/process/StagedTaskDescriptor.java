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

package org.apache.amoro.process;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.optimizing.TaskMetricsSummary;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.utils.SerializationUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * StagedTaskDescriptor is a descriptor for a task which contains input, output and summary.
 * StagedTask could contain many stages, such as rewrite, plan, evaluate, commit, etc. Or just a
 * single running stage of {@link org.apache.amoro.process.TableProcess}
 *
 * @param <I> input of the task
 * @param <O> output of the task
 * @param <S> summary of the task
 */
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

  /**
   * Ensure {@link TaskProperties#TASK_EXECUTOR_FACTORY_IMPL} is populated on this descriptor,
   * setting it to {@code defaultFactoryImpl} when (and only when) the key is currently absent.
   *
   * <p>This is the back-fill hook for pre-0.9 Iceberg task rows restored from the DB: the key did
   * not exist before the multi-format refactor, so the row's deserialized {@code properties} map
   * either lacks the entry or is {@code null} entirely. A {@code null} properties map is replaced
   * with a fresh mutable {@link HashMap} before writing the entry.
   *
   * <p>Callers must pass a non-null factory class name — see {@code
   * LegacyExecutorFactoryDefaults#resolveDefaultExecutorFactoryImpl} for the routing table.
   *
   * @return {@code true} iff this call wrote the key (i.e. it was missing), {@code false} if the
   *     key was already present.
   */
  public boolean ensureExecutorFactoryImpl(String defaultFactoryImpl) {
    if (defaultFactoryImpl == null) {
      throw new IllegalArgumentException("defaultFactoryImpl must not be null");
    }
    if (properties == null) {
      properties = new HashMap<>();
    }
    if (properties.containsKey(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL)) {
      return false;
    }
    properties.put(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, defaultFactoryImpl);
    return true;
  }

  public S getSummary() {
    return summary;
  }

  /**
   * Return a format-agnostic, aggregation-friendly view of this task's summary. Each subclass
   * adapts its typed {@code S} summary to a {@link TaskMetricsSummary}; there is deliberately no
   * default implementation so a new descriptor cannot silently drop metrics by forgetting to
   * override.
   */
  public abstract TaskMetricsSummary toMetricsSummary();

  public OptimizingTask extractProtocolTask(OptimizingTaskId taskId) {
    OptimizingTask optimizingTask = new OptimizingTask(taskId);
    optimizingTask.setTaskInput(SerializationUtil.simpleSerialize(input));
    optimizingTask.setProperties(properties);
    return optimizingTask;
  }
}

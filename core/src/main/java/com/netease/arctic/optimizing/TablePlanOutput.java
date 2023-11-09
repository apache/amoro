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

package com.netease.arctic.optimizing;

import com.netease.arctic.optimizing.plan.TaskDescriptor;
import org.apache.iceberg.util.StructLikeMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TablePlanOutput implements TableOptimizing.OptimizingOutput {

  private List<TaskDescriptor> tasks;

  private StructLikeMap<PartitionPlanMetrics> partitionPlanMetrics;

  public List<TaskDescriptor> getTasks() {
    return tasks;
  }

  public void setTasks(List<TaskDescriptor> tasks) {
    this.tasks = tasks;
  }

  public StructLikeMap<PartitionPlanMetrics> getPartitionPlanMetrics() {
    return partitionPlanMetrics;
  }

  public void setPartitionPlanMetrics(StructLikeMap<PartitionPlanMetrics> partitionPlanMetrics) {
    this.partitionPlanMetrics = partitionPlanMetrics;
  }

  @Override
  public Map<String, String> summary() {
    return Collections.emptyMap();
  }

  public static class PartitionPlanMetrics {
    private long fromSequence;
    private long toSequence;

    public long getFromSequence() {
      return fromSequence;
    }

    public void setFromSequence(long fromSequence) {
      this.fromSequence = fromSequence;
    }

    public long getToSequence() {
      return toSequence;
    }

    public void setToSequence(long toSequence) {
      this.toSequence = toSequence;
    }
  }
}

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

package org.apache.amoro.optimizing;

import org.apache.amoro.process.StagedTaskDescriptor;

import java.util.List;
import java.util.Map;

/** DTO carrying the result of an optimizing plan, including tasks and metadata. */
public class OptimizingPlanResult {

  private final long processId;
  private final OptimizingType optimizingType;
  private final long planTime;
  private final long targetSnapshotId;
  private final long targetChangeSnapshotId;
  private final List<? extends StagedTaskDescriptor<?, ?, ?>> tasks;
  private final Map<String, Long> fromSequence;
  private final Map<String, Long> toSequence;

  public OptimizingPlanResult(
      long processId,
      OptimizingType optimizingType,
      long planTime,
      long targetSnapshotId,
      long targetChangeSnapshotId,
      List<? extends StagedTaskDescriptor<?, ?, ?>> tasks,
      Map<String, Long> fromSequence,
      Map<String, Long> toSequence) {
    this.processId = processId;
    this.optimizingType = optimizingType;
    this.planTime = planTime;
    this.targetSnapshotId = targetSnapshotId;
    this.targetChangeSnapshotId = targetChangeSnapshotId;
    this.tasks = tasks;
    this.fromSequence = fromSequence;
    this.toSequence = toSequence;
  }

  public long getProcessId() {
    return processId;
  }

  public OptimizingType getOptimizingType() {
    return optimizingType;
  }

  public long getPlanTime() {
    return planTime;
  }

  public long getTargetSnapshotId() {
    return targetSnapshotId;
  }

  public long getTargetChangeSnapshotId() {
    return targetChangeSnapshotId;
  }

  public List<? extends StagedTaskDescriptor<?, ?, ?>> getTasks() {
    return tasks;
  }

  public Map<String, Long> getFromSequence() {
    return fromSequence;
  }

  public Map<String, Long> getToSequence() {
    return toSequence;
  }
}

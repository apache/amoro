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

import java.util.Map;

/**
 * Internal abstraction for table optimizing planning, used by ProcessFactory implementations. This
 * is NOT a standalone SPI interface; ProcessFactory implementations encapsulate planner creation
 * internally.
 */
public interface TableOptimizingPlanner {

  /** Evaluate whether optimizing is necessary for the current table state. */
  boolean isNecessary();

  /** Execute the full planning and return the plan result including tasks. */
  OptimizingPlanResult plan();

  /** Get the optimizing type determined by the planner. */
  OptimizingType getOptimizingType();

  /** Get the unique process ID for this planning session. */
  long getProcessId();

  /** Get the timestamp when planning started. */
  long getPlanTime();

  /** Get the target snapshot ID that this plan is based on. */
  long getTargetSnapshotId();

  /** Get the target change snapshot ID for keyed tables. */
  long getTargetChangeSnapshotId();

  /** Get the from-sequence map for partition-level sequence tracking. */
  Map<String, Long> getFromSequence();

  /** Get the to-sequence map for partition-level sequence tracking. */
  Map<String, Long> getToSequence();
}

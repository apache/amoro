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

package org.apache.amoro.server.optimizing.dra;

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.resource.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link DynamicAllocationState#computeScaleUp}. Time is injected, so every scenario is
 * deterministic; the returned value is the number of executor-parallelism-thread instances to
 * create in this round.
 */
public class TestComputeScaleUp {

  private static final long T0 = 0L;
  private static final long BACKLOG_MS = 60_000L; // scheduler-backlog-timeout default 1min
  private static final long SUSTAINED_MS = 30_000L; // sustained-backlog-timeout default 30s

  private DynamicAllocationConfig config(int minParallelism, int maxParallelism, int k) {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    props.put(
        OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, String.valueOf(minParallelism));
    props.put(
        OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, String.valueOf(maxParallelism));
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_EXECUTOR_PARALLELISM, String.valueOf(k));
    return DynamicAllocationConfig.parse(
        new ResourceGroup.Builder("group1", "flink").addProperties(props).build());
  }

  // --- floor enforcement: immediate, no timing gate ---

  @Test
  void floorDeficitScalesImmediately() {
    DynamicAllocationState state = new DynamicAllocationState();
    // min=5, K=2: ceil(5/2) = 3 instances, on the very first evaluation.
    Assertions.assertEquals(3, state.computeScaleUp(0, 0, 0, 0, config(5, 100, 2), T0));
  }

  @Test
  void floorNeverExceedsMaxParallelism() {
    DynamicAllocationState state = new DynamicAllocationState();
    // min=5, max=6, K=4: ceil(5/4)=2 instances would be 8 threads > max; cap allows only 1.
    Assertions.assertEquals(1, state.computeScaleUp(0, 0, 0, 0, config(5, 6, 4), T0));
  }

  // --- no demand ---

  @Test
  void noDemandReturnsZero() {
    DynamicAllocationState state = new DynamicAllocationState();
    Assertions.assertEquals(0, state.computeScaleUp(4, 2, 0, 0, config(0, 100, 2), T0));
  }

  @Test
  void idleCapacityCoveringBacklogReturnsZero() {
    DynamicAllocationState state = new DynamicAllocationState();
    // busy(2) + serviceable(3) <= effective(8): idle threads will pick the tasks up.
    Assertions.assertEquals(0, state.computeScaleUp(8, 2, 3, 0, config(0, 100, 2), T0));
  }

  // --- immediate demand (Layer 1): backlog timer, ramp, clamp ---

  @Test
  void immediateBacklogWaitsForSchedulerBacklogTimeout() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 100, 2);
    Assertions.assertEquals(0, state.computeScaleUp(2, 2, 5, 0, config, T0));
    Assertions.assertEquals(0, state.computeScaleUp(2, 2, 5, 0, config, T0 + BACKLOG_MS - 1));
    Assertions.assertEquals(1, state.computeScaleUp(2, 2, 5, 0, config, T0 + BACKLOG_MS));
  }

  @Test
  void exponentialRampAcrossSustainedRounds() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 1000, 2);
    // A large persistent backlog; effective/busy grow by the created threads each round.
    Assertions.assertEquals(0, state.computeScaleUp(2, 2, 100, 0, config, T0));
    Assertions.assertEquals(1, state.computeScaleUp(2, 2, 100, 0, config, T0 + BACKLOG_MS));
    Assertions.assertEquals(
        2, state.computeScaleUp(4, 4, 100, 0, config, T0 + BACKLOG_MS + SUSTAINED_MS));
    Assertions.assertEquals(
        4, state.computeScaleUp(8, 8, 100, 0, config, T0 + BACKLOG_MS + 2 * SUSTAINED_MS));
    Assertions.assertEquals(
        8, state.computeScaleUp(16, 16, 100, 0, config, T0 + BACKLOG_MS + 3 * SUSTAINED_MS));
  }

  @Test
  void sustainedCadenceGatesConsecutiveAdds() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 1000, 2);
    Assertions.assertEquals(0, state.computeScaleUp(2, 2, 100, 0, config, T0));
    Assertions.assertEquals(1, state.computeScaleUp(2, 2, 100, 0, config, T0 + BACKLOG_MS));
    // 10s after the first add: sustained-backlog-timeout (30s) has not elapsed.
    Assertions.assertEquals(
        0, state.computeScaleUp(4, 4, 100, 0, config, T0 + BACKLOG_MS + 10_000));
    Assertions.assertEquals(
        2, state.computeScaleUp(4, 4, 100, 0, config, T0 + BACKLOG_MS + SUSTAINED_MS));
  }

  @Test
  void rampResetsWhenClampBinds() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 1000, 1);
    long t1 = T0 + BACKLOG_MS;
    long t2 = t1 + SUSTAINED_MS;
    long t3 = t2 + SUSTAINED_MS;
    long t4 = t3 + SUSTAINED_MS;
    Assertions.assertEquals(0, state.computeScaleUp(2, 2, 10, 0, config, T0));
    // A: want=10 > ramp=1 -> add 1, ramp doubles to 2.
    Assertions.assertEquals(1, state.computeScaleUp(2, 2, 10, 0, config, t1));
    // B: want=1 < ramp=2 -> clamp binds: add 1 and the ramp resets to 1 (Spark semantics),
    // instead of keeping a grown ramp no demand justified.
    Assertions.assertEquals(1, state.computeScaleUp(3, 3, 1, 0, config, t2));
    // C: demand returns: ramp restarts from 1, not from the stale doubled value.
    Assertions.assertEquals(1, state.computeScaleUp(4, 4, 10, 0, config, t3));
    // D: ramp doubling resumes normally.
    Assertions.assertEquals(2, state.computeScaleUp(5, 5, 10, 0, config, t4));
  }

  @Test
  void addIsCappedByMaxParallelismAndStopsAtCap() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 10, 2);
    Assertions.assertEquals(0, state.computeScaleUp(8, 8, 100, 0, config, T0));
    // Only floor((10-8)/2) = 1 instance fits under the cap.
    Assertions.assertEquals(1, state.computeScaleUp(8, 8, 100, 0, config, T0 + BACKLOG_MS));
    // At the cap: nothing more can be created no matter the backlog.
    Assertions.assertEquals(
        0, state.computeScaleUp(10, 10, 100, 0, config, T0 + BACKLOG_MS + SUSTAINED_MS));
  }

  @Test
  void oscillatingDemandResetsBacklogTimer() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 100, 2);
    // A trickle whose tasks are drained between rounds must not accumulate toward the timeout:
    // arrival keeping pace with processing is not under-capacity.
    Assertions.assertEquals(0, state.computeScaleUp(2, 2, 5, 0, config, T0));
    Assertions.assertEquals(0, state.computeScaleUp(2, 2, 0, 0, config, T0 + 30_000)); // resets
    Assertions.assertEquals(0, state.computeScaleUp(2, 2, 5, 0, config, T0 + 45_000)); // restarts
    Assertions.assertEquals(
        0, state.computeScaleUp(2, 2, 5, 0, config, T0 + 45_000 + BACKLOG_MS - 1));
    Assertions.assertEquals(1, state.computeScaleUp(2, 2, 5, 0, config, T0 + 45_000 + BACKLOG_MS));
  }

  // --- future demand (Layer 2): pending tables, all threads busy, one instance ---

  @Test
  void coldStartWithPendingTablesAddsOneInstance() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 100, 4);
    // Zero optimizers: no pollTask, no planning, no PLANNED tasks — only PENDING tables are
    // observable. Layer 2 must ignite the group.
    Assertions.assertEquals(0, state.computeScaleUp(0, 0, 0, 3, config, T0));
    Assertions.assertEquals(1, state.computeScaleUp(0, 0, 0, 3, config, T0 + BACKLOG_MS));
  }

  @Test
  void futureDemandRequiresAllThreadsBusy() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 100, 2);
    // An idle thread exists: it will drive planning via pollTask, so pending tables alone are
    // not a scale signal.
    Assertions.assertEquals(0, state.computeScaleUp(4, 3, 0, 5, config, T0));
    Assertions.assertEquals(0, state.computeScaleUp(4, 3, 0, 5, config, T0 + BACKLOG_MS));
  }

  @Test
  void futureDemandAddsOneInstanceRegardlessOfPendingTableCount() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 100, 4);
    // Pending tables are not quantified demand (tasks per table are unknown before planning);
    // Layer 2 probes with a single instance and lets Layer 1 take over once tasks materialize.
    Assertions.assertEquals(0, state.computeScaleUp(0, 0, 0, 100, config, T0));
    Assertions.assertEquals(1, state.computeScaleUp(0, 0, 0, 100, config, T0 + BACKLOG_MS));
  }

  @Test
  void futureDemandDoesNotRamp() {
    DynamicAllocationState state = new DynamicAllocationState();
    DynamicAllocationConfig config = config(0, 100, 4);
    Assertions.assertEquals(0, state.computeScaleUp(0, 0, 0, 10, config, T0));
    Assertions.assertEquals(1, state.computeScaleUp(0, 0, 0, 10, config, T0 + BACKLOG_MS));
    // Still pending-only demand after the first instance registered and is busy: another single
    // instance, not an exponentially grown batch.
    Assertions.assertEquals(
        1, state.computeScaleUp(4, 4, 0, 10, config, T0 + BACKLOG_MS + SUSTAINED_MS));
    Assertions.assertEquals(
        1, state.computeScaleUp(8, 8, 0, 10, config, T0 + BACKLOG_MS + 2 * SUSTAINED_MS));
  }
}

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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scale-down decision tests: idle observation from snapshots, longest-idle candidate selection, the
 * min-parallelism floor against registered-minus-draining threads, and cooldown rate limiting.
 */
public class TestComputeScaleDown {

  private static final long T0 = 0L;
  private static final long IDLE_MS = 300_000L; // executor-idle-timeout default 5min
  private static final long COOLDOWN_MS = 60_000L; // scale-down-cooldown default 1min

  private DynamicAllocationConfig config(int minParallelism) {
    Map<String, String> props = new HashMap<>();
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_ENABLED, "true");
    props.put(
        OptimizerProperties.DYNAMIC_ALLOCATION_MIN_PARALLELISM, String.valueOf(minParallelism));
    props.put(OptimizerProperties.DYNAMIC_ALLOCATION_MAX_PARALLELISM, "100");
    return DynamicAllocationConfig.parse(
        new ResourceGroup.Builder("group1", "flink").addProperties(props).build());
  }

  private DynamicAllocationState.RemovalCandidate candidate(String token, int threadCount) {
    return new DynamicAllocationState.RemovalCandidate(token, threadCount);
  }

  private void observeIdle(DynamicAllocationState state, long now, String... tokens) {
    state.observe(
        Arrays.stream(tokens).collect(java.util.stream.Collectors.toSet()),
        Collections.emptyMap(),
        now);
  }

  // --- idle qualification ---

  @Test
  void newTokenIsIdleFromFirstObservation() {
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "a");
    List<DynamicAllocationState.RemovalCandidate> candidates =
        Collections.singletonList(candidate("a", 1));

    Assertions.assertNull(state.computeScaleDown(candidates, 1, 0, config(0), T0 + IDLE_MS - 1));
    Assertions.assertEquals("a", state.computeScaleDown(candidates, 1, 0, config(0), T0 + IDLE_MS));
  }

  @Test
  void busyObservationResetsIdleClock() {
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "a");
    long t1 = T0 + 100_000;
    state.observe(Collections.singleton("a"), Collections.singletonMap("a", 1), t1); // busy at t1
    List<DynamicAllocationState.RemovalCandidate> candidates =
        Collections.singletonList(candidate("a", 1));

    Assertions.assertNull(state.computeScaleDown(candidates, 1, 0, config(0), T0 + IDLE_MS));
    Assertions.assertEquals("a", state.computeScaleDown(candidates, 1, 0, config(0), t1 + IDLE_MS));
  }

  @Test
  void neverObservedTokenIsNotSelected() {
    // A candidate the keeper has not observed yet must not be treated as long-idle.
    DynamicAllocationState state = new DynamicAllocationState();
    Assertions.assertNull(
        state.computeScaleDown(
            Collections.singletonList(candidate("ghost", 1)), 1, 0, config(0), T0 + IDLE_MS));
  }

  @Test
  void reRegisteredTokenIsSeededFresh() {
    // Unregistration prunes the observation; a token that comes back (same optimizer identity
    // reused) must re-earn its idle time instead of inheriting the stale pre-prune timestamp.
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "a");
    observeIdle(state, T0 + 10_000); // "a" unregistered: pruned
    long t2 = T0 + 20_000;
    observeIdle(state, t2, "a"); // back again
    List<DynamicAllocationState.RemovalCandidate> candidates =
        Collections.singletonList(candidate("a", 1));

    Assertions.assertNull(state.computeScaleDown(candidates, 1, 0, config(0), T0 + IDLE_MS));
    Assertions.assertEquals("a", state.computeScaleDown(candidates, 1, 0, config(0), t2 + IDLE_MS));
  }

  // --- candidate selection ---

  @Test
  void longestIdleCandidateWins() {
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "a", "b");
    long t1 = T0 + 50_000;
    state.observe(
        Arrays.stream(new String[] {"a", "b"}).collect(java.util.stream.Collectors.toSet()),
        Collections.singletonMap("a", 1), // a busy at t1, b idle since T0
        t1);

    Assertions.assertEquals(
        "b",
        state.computeScaleDown(
            Arrays.asList(candidate("a", 1), candidate("b", 1)), 2, 0, config(0), t1 + IDLE_MS));
  }

  @Test
  void onlyOneCandidatePerRound() {
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "a", "b");
    String first =
        state.computeScaleDown(
            Arrays.asList(candidate("a", 1), candidate("b", 1)), 2, 0, config(0), T0 + IDLE_MS);
    Assertions.assertNotNull(first);
    // The very next call within the cooldown window returns nothing, even though the other
    // instance is equally idle: one removal per cooldown period.
    Assertions.assertNull(
        state.computeScaleDown(
            Arrays.asList(candidate("a", 1), candidate("b", 1)),
            2,
            1,
            config(0),
            T0 + IDLE_MS + 1));
  }

  // --- floor enforcement ---

  @Test
  void floorBlocksRemoval() {
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "a");
    Assertions.assertNull(
        state.computeScaleDown(
            Collections.singletonList(candidate("a", 1)), 2, 0, config(2), T0 + IDLE_MS));
  }

  @Test
  void floorCountsDrainingThreadsAsAlreadyGone() {
    // registered=3 still includes a draining instance; treating it as capacity would let a second
    // removal pass the floor check and land the group below min-parallelism once both complete.
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "b");
    Assertions.assertNull(
        state.computeScaleDown(
            Collections.singletonList(candidate("b", 1)), 3, 1, config(2), T0 + IDLE_MS));
  }

  @Test
  void nextIdleCandidateIsPickedWhenLongestViolatesFloor() {
    // Heterogeneous thread counts: removing the longest-idle 3-thread instance would break the
    // floor, but the shorter-idle 1-thread instance fits — pick it instead of returning null.
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "big");
    long t1 = T0 + 50_000;
    observeIdle(state, t1, "big", "small");

    Assertions.assertEquals(
        "small",
        state.computeScaleDown(
            Arrays.asList(candidate("big", 3), candidate("small", 1)),
            4,
            0,
            config(2),
            t1 + IDLE_MS));
  }

  // --- cooldown ---

  @Test
  void cooldownRateLimitsRemovals() {
    DynamicAllocationState state = new DynamicAllocationState();
    observeIdle(state, T0, "a", "b");
    List<DynamicAllocationState.RemovalCandidate> candidates =
        Arrays.asList(candidate("a", 1), candidate("b", 1));

    long firstAt = T0 + IDLE_MS;
    Assertions.assertNotNull(state.computeScaleDown(candidates, 2, 0, config(0), firstAt));
    Assertions.assertNull(
        state.computeScaleDown(candidates, 2, 1, config(0), firstAt + COOLDOWN_MS - 1));
    Assertions.assertNotNull(
        state.computeScaleDown(candidates, 2, 1, config(0), firstAt + COOLDOWN_MS));
  }
}

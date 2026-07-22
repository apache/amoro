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

import org.apache.amoro.server.optimizing.TaskRuntime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

public class TestDynamicAllocationState {

  private DynamicAllocationState.TableDemand demand(
      int plannedCount, double targetQuota, int occupiedThreads) {
    return new DynamicAllocationState.TableDemand(plannedCount, targetQuota, occupiedThreads);
  }

  // --- serviceablePlannedCount: proportional mode (targetQuota <= 1) ---
  // The per-table quota limit is ceil(targetQuota * availableCore), so adding threads raises the
  // limit; a quota-blocked backlog is still serviceable demand and counts in full.

  @Test
  void proportionalQuotaCountsAllPlannedTasks() {
    Assertions.assertEquals(
        10,
        DynamicAllocationState.serviceablePlannedCount(
            Collections.singletonList(demand(10, 0.5, 4))));
  }

  @Test
  void targetQuotaOfExactlyOneIsProportional() {
    // Absolute mode starts strictly above 1 (see OptimizingUtil quota resolution).
    Assertions.assertEquals(
        7,
        DynamicAllocationState.serviceablePlannedCount(
            Collections.singletonList(demand(7, 1.0, 3))));
  }

  // --- serviceablePlannedCount: absolute mode (targetQuota > 1) ---
  // The limit is a fixed thread count that scaling cannot raise; only free slots are serviceable.

  @Test
  void absoluteQuotaCountsOnlyFreeSlots() {
    Assertions.assertEquals(
        2,
        DynamicAllocationState.serviceablePlannedCount(
            Collections.singletonList(demand(10, 3.0, 1))));
  }

  @Test
  void absoluteQuotaExhaustedCountsZero() {
    Assertions.assertEquals(
        0,
        DynamicAllocationState.serviceablePlannedCount(
            Collections.singletonList(demand(5, 2.0, 2))));
  }

  @Test
  void absoluteQuotaOverOccupiedDoesNotGoNegative() {
    // occupied can transiently exceed the limit (e.g. after a quota config decrease); the table
    // must contribute zero, not a negative count offsetting other tables.
    Assertions.assertEquals(
        0,
        DynamicAllocationState.serviceablePlannedCount(
            Collections.singletonList(demand(5, 2.0, 3))));
  }

  @Test
  void absoluteQuotaFractionalLimitTruncates() {
    // The limit is (int) targetQuota, matching the poll gate's getQuotaLimit cast: 2.5 -> 2
    // slots, so 2 occupied threads leave zero free.
    Assertions.assertEquals(
        0,
        DynamicAllocationState.serviceablePlannedCount(
            Collections.singletonList(demand(5, 2.5, 2))));
  }

  @Test
  void absoluteQuotaFreeSlotsCappedByPlanned() {
    // Free slots exceed the planned backlog; only actual tasks count.
    Assertions.assertEquals(
        3,
        DynamicAllocationState.serviceablePlannedCount(
            Collections.singletonList(demand(3, 8.0, 1))));
  }

  // --- serviceablePlannedCount: aggregation ---

  @Test
  void mixedModesSumPerTable() {
    Assertions.assertEquals(
        12,
        DynamicAllocationState.serviceablePlannedCount(
            Arrays.asList(demand(10, 0.5, 4), demand(10, 3.0, 1))));
  }

  @Test
  void emptyDemandsCountZero() {
    Assertions.assertEquals(
        0, DynamicAllocationState.serviceablePlannedCount(Collections.emptyList()));
  }

  // --- isPlanningBound ---
  // Idle threads while tables wait as PENDING and no PLANNED tasks materialize means the
  // bottleneck is planning (maxPlanningParallelism), not thread capacity: scaling out would only
  // add more idle threads. The condition is surfaced as a warning, never as a scale-out.

  @Test
  void planningBoundWhenThreadsIdleTablesPendingAndNothingPlanned() {
    Assertions.assertTrue(DynamicAllocationState.isPlanningBound(4, 2, 0, 10));
  }

  @Test
  void notPlanningBoundWhenPlannedTasksAwaitPickup() {
    // Idle threads have work to poll; planning is keeping up.
    Assertions.assertFalse(DynamicAllocationState.isPlanningBound(4, 2, 3, 10));
  }

  @Test
  void notPlanningBoundWhenAllThreadsBusy() {
    // Saturated threads are Layer-2 territory, not a planning bottleneck signal.
    Assertions.assertFalse(DynamicAllocationState.isPlanningBound(4, 4, 0, 10));
  }

  @Test
  void notPlanningBoundWithoutPendingTables() {
    Assertions.assertFalse(DynamicAllocationState.isPlanningBound(4, 2, 0, 0));
  }

  @Test
  void notPlanningBoundOnColdGroup() {
    // Zero optimizers is the cold-start case handled by future demand, not a planning issue.
    Assertions.assertFalse(DynamicAllocationState.isPlanningBound(0, 0, 0, 10));
  }

  // --- occupiesThread ---
  // A task occupies an optimizer thread from the moment it is assigned (SCHEDULED, set by
  // pollTask) until it terminates; counting only ACKED would overestimate headroom during the
  // poll-to-ack window.

  @Test
  void scheduledTaskOccupiesAThread() {
    Assertions.assertTrue(DynamicAllocationState.occupiesThread(TaskRuntime.Status.SCHEDULED));
  }

  @Test
  void ackedTaskOccupiesAThread() {
    Assertions.assertTrue(DynamicAllocationState.occupiesThread(TaskRuntime.Status.ACKED));
  }

  @Test
  void terminalAndQueuedStatusesDoNotOccupyThreads() {
    Assertions.assertFalse(DynamicAllocationState.occupiesThread(TaskRuntime.Status.PLANNED));
    Assertions.assertFalse(DynamicAllocationState.occupiesThread(TaskRuntime.Status.SUCCESS));
    Assertions.assertFalse(DynamicAllocationState.occupiesThread(TaskRuntime.Status.FAILED));
    Assertions.assertFalse(DynamicAllocationState.occupiesThread(TaskRuntime.Status.CANCELED));
  }
}

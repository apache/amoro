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

import java.util.Collection;

/**
 * Demand accounting for dynamic allocation (AIP-5). Pure functions over plain values so the scaling
 * decision logic is testable without a live optimizing queue.
 */
public final class DynamicAllocationState {

  private DynamicAllocationState() {}

  /** Per-table demand snapshot consumed by {@link #serviceablePlannedCount(Collection)}. */
  public static class TableDemand {
    private final int plannedCount;
    private final double targetQuota;
    private final int occupiedThreads;

    public TableDemand(int plannedCount, double targetQuota, int occupiedThreads) {
      this.plannedCount = plannedCount;
      this.targetQuota = targetQuota;
      this.occupiedThreads = occupiedThreads;
    }
  }

  /**
   * Count the PLANNED tasks that adding optimizer capacity could actually drain.
   *
   * <p>A table with a proportional quota ({@code targetQuota <= 1}) is limited to {@code
   * ceil(targetQuota * availableCore)} threads, so scaling up raises its limit and the whole
   * backlog is serviceable. A table with an absolute quota ({@code > 1}) has a fixed thread limit
   * that scaling cannot raise, so only its currently free slots are serviceable.
   */
  public static int serviceablePlannedCount(Collection<TableDemand> demands) {
    int total = 0;
    for (TableDemand demand : demands) {
      if (demand.targetQuota <= 1) {
        total += demand.plannedCount;
      } else {
        int freeSlots = Math.max(0, (int) demand.targetQuota - demand.occupiedThreads);
        total += Math.min(demand.plannedCount, freeSlots);
      }
    }
    return total;
  }

  /**
   * Whether a task in this status occupies an optimizer thread. A thread is occupied from
   * assignment ({@code SCHEDULED}, set by {@code pollTask}) until the task terminates; counting
   * only {@code ACKED} would overestimate headroom during the poll-to-ack window.
   */
  public static boolean occupiesThread(TaskRuntime.Status status) {
    return status == TaskRuntime.Status.SCHEDULED || status == TaskRuntime.Status.ACKED;
  }
}

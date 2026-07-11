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
 * Per-group scale-up decision state for dynamic allocation (AIP-5): the backlog timer, the
 * scale-out cadence, and the exponential ramp. {@link #computeScaleUp} is driven by injected inputs
 * (loads, config, time), so the decision logic is deterministic and testable without a live
 * optimizing queue; the static demand-accounting helpers are pure functions.
 */
public final class DynamicAllocationState {

  /** When demand was first observed; {@code -1} while there is no demand. */
  private long backlogSinceMs = -1;

  /** Earliest time the next scale-out may happen; {@code -1} before the first one. */
  private long nextAllowedAddMs = -1;

  /** Instances to add in the next immediate-demand round (1, 2, 4, 8 ...). */
  private int rampInstances = 1;

  /**
   * Decide how many executor-parallelism-thread optimizer instances to create in this round.
   *
   * <p>Ordered checks: the {@code min-parallelism} floor is enforced immediately (no timing gate);
   * immediate demand ({@code busy + serviceable > effective}) scales exponentially, clamped to the
   * actual need (Spark semantics: the ramp resets when the clamp binds, and a round with no demand
   * resets it too); future demand (pending tables while every thread is busy — including the
   * zero-optimizer cold start, where nothing polls and planning never runs) adds a single probe
   * instance, because pending tables are not quantified demand before planning. Demand must persist
   * for {@code scheduler-backlog-timeout} before the first scale-out; subsequent ones are spaced by
   * {@code sustained-backlog-timeout}. The {@code max-parallelism} cap always wins.
   *
   * @return the number of instances of {@code executor-parallelism} threads to create, {@code >= 0}
   */
  public int computeScaleUp(
      int effectiveThreads,
      int busyThreads,
      int serviceablePlanned,
      int pendingTables,
      DynamicAllocationConfig config,
      long nowMs) {
    int k = config.getExecutorParallelism();
    int allowedInstances = Math.max(0, (config.getMaxParallelism() - effectiveThreads) / k);

    int minParallelism = config.getMinParallelism();
    if (effectiveThreads < minParallelism) {
      int neededInstances = ceilDiv(minParallelism - effectiveThreads, k);
      return Math.min(neededInstances, allowedInstances);
    }

    int actionableNeed = Math.max(busyThreads + serviceablePlanned - effectiveThreads, 0);
    boolean futureDemand = pendingTables > 0 && busyThreads >= effectiveThreads;
    if (actionableNeed <= 0 && !futureDemand) {
      backlogSinceMs = -1;
      nextAllowedAddMs = -1;
      rampInstances = 1;
      return 0;
    }

    if (backlogSinceMs < 0) {
      backlogSinceMs = nowMs;
      nextAllowedAddMs = -1;
    }
    long gate =
        nextAllowedAddMs >= 0
            ? nextAllowedAddMs
            : backlogSinceMs + config.getSchedulerBacklogTimeout().toMillis();
    if (nowMs < gate) {
      return 0;
    }

    int add;
    if (actionableNeed > 0) {
      int wantInstances = ceilDiv(actionableNeed, k);
      add = Math.min(Math.min(wantInstances, rampInstances), allowedInstances);
      if (add <= 0) {
        return 0;
      }
      // Spark semantics: keep doubling only while the ramp is the binding constraint; once the
      // actual need clamps the add, a grown ramp is no longer justified by demand.
      rampInstances = wantInstances > rampInstances ? rampInstances * 2 : 1;
    } else {
      add = Math.min(1, allowedInstances);
      if (add <= 0) {
        return 0;
      }
      rampInstances = 1;
    }
    nextAllowedAddMs = nowMs + config.getSustainedBacklogTimeout().toMillis();
    return add;
  }

  private static int ceilDiv(int value, int divisor) {
    return (value + divisor - 1) / divisor;
  }

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

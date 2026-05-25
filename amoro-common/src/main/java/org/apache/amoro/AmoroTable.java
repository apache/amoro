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

package org.apache.amoro;

import org.apache.amoro.optimizing.OptimizationContext;
import org.apache.amoro.optimizing.PendingInputResult;
import org.apache.amoro.optimizing.TableRuntimeOptimizingState;
import org.apache.amoro.table.TableIdentifier;

import java.util.Map;
import java.util.Optional;

public interface AmoroTable<T> {

  /** Returns the {@link TableIdentifier} of this table */
  TableIdentifier id();

  /** Returns the name of this table */
  default String name() {
    return id().toString();
  }

  /** Returns the {@link TableFormat} of this table */
  TableFormat format();

  /** Returns the properties of this table */
  Map<String, String> properties();

  /** Returns the original of this table */
  T originalTable();

  /** Returns the current snapshot of this table */
  TableSnapshot currentSnapshot();

  /**
   * Refresh optimizing-related snapshot IDs into the given state. Returns true if any snapshot ID
   * changed.
   *
   * <p>Default implementation reads a single snapshot via {@link #currentSnapshot()} and parses its
   * ID as a long. Formats with dual snapshots (e.g., keyed MixedTable) must override this method.
   */
  default boolean refreshOptimizingState(TableRuntimeOptimizingState state) {
    TableSnapshot snapshot = currentSnapshot();
    long currentId = TableRuntimeOptimizingState.INVALID_SNAPSHOT_ID;
    if (snapshot != null) {
      try {
        currentId = Long.parseLong(snapshot.id());
      } catch (NumberFormatException e) {
        return false;
      }
    }
    if (currentId != state.getCurrentSnapshotId()) {
      state.setCurrentSnapshotId(currentId);
      return true;
    }
    return false;
  }

  /**
   * Evaluate whether optimizing is needed for this table.
   *
   * <p>Default returns empty (no evaluation). Format-specific implementations override to provide
   * file scanning and evaluator logic.
   */
  default Optional<PendingInputResult> evaluatePendingInput(
      OptimizationContext context, int maxPendingPartitions) {
    return Optional.empty();
  }

  /** Returns the number of snapshots for metrics. Default returns 0. */
  default long snapshotCount() {
    return 0;
  }

  /**
   * Refresh format-specific optimizing metrics after snapshot state is updated.
   *
   * <p>Default is no-op. Iceberg-based formats override to compute non-maintained and optimizing
   * snapshot timestamps from Iceberg Snapshot objects.
   */
  default void refreshOptimizingMetrics(OptimizationContext context) {}
}

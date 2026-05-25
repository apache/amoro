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

package org.apache.amoro.formats.mixed;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.optimizing.OptimizationContext;
import org.apache.amoro.optimizing.PendingInputResult;
import org.apache.amoro.optimizing.TableRuntimeOptimizingState;
import org.apache.amoro.optimizing.evaluation.MetadataBasedEvaluationEvent;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.optimizing.plan.IcebergOptimizingEvaluatorFactory;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.BasicTableSnapshot;
import org.apache.amoro.table.KeyedTableSnapshot;
import org.apache.amoro.table.TableIdentifier;
import org.apache.iceberg.Snapshot;

import java.util.Map;

public class MixedTable implements AmoroTable<org.apache.amoro.table.MixedTable> {

  private final org.apache.amoro.table.MixedTable mixedTable;
  private final TableFormat format;

  public MixedTable(org.apache.amoro.table.MixedTable mixedTable, TableFormat format) {
    this.mixedTable = mixedTable;
    this.format = format;
  }

  @Override
  public TableIdentifier id() {
    return mixedTable.id();
  }

  @Override
  public TableFormat format() {
    return format;
  }

  @Override
  public Map<String, String> properties() {
    return mixedTable.properties();
  }

  @Override
  public org.apache.amoro.table.MixedTable originalTable() {
    return mixedTable;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    Snapshot changeSnapshot;
    Snapshot baseSnapshot;
    if (mixedTable.isKeyedTable()) {
      changeSnapshot = mixedTable.asKeyedTable().changeTable().currentSnapshot();
      baseSnapshot = mixedTable.asKeyedTable().baseTable().currentSnapshot();
    } else {
      changeSnapshot = null;
      baseSnapshot = mixedTable.asUnkeyedTable().currentSnapshot();
    }

    if (changeSnapshot == null && baseSnapshot == null) {
      return null;
    }

    return new MixedSnapshot(changeSnapshot, baseSnapshot);
  }

  @Override
  public boolean refreshOptimizingState(TableRuntimeOptimizingState state) {
    long lastBase = state.getCurrentSnapshotId();
    long lastChange = state.getCurrentChangeSnapshotId();

    long currentBase = TableRuntimeOptimizingState.INVALID_SNAPSHOT_ID;
    long currentChange = TableRuntimeOptimizingState.INVALID_SNAPSHOT_ID;

    if (mixedTable.isKeyedTable()) {
      Snapshot baseSnap = mixedTable.asKeyedTable().baseTable().currentSnapshot();
      Snapshot changeSnap = mixedTable.asKeyedTable().changeTable().currentSnapshot();
      if (baseSnap != null) {
        currentBase = baseSnap.snapshotId();
      }
      if (changeSnap != null) {
        currentChange = changeSnap.snapshotId();
      }
    } else {
      Snapshot snap = mixedTable.asUnkeyedTable().currentSnapshot();
      if (snap != null) {
        currentBase = snap.snapshotId();
      }
    }

    boolean changed = currentBase != lastBase || currentChange != lastChange;
    if (changed) {
      state.setCurrentSnapshotId(currentBase);
      state.setCurrentChangeSnapshotId(currentChange);
    }
    return changed;
  }

  @Override
  public java.util.Optional<PendingInputResult> evaluatePendingInput(
      OptimizationContext context, int maxPendingPartitions) {
    if (context.getOptimizingConfig().isEnabled()
        && context.isIdle()
        && context.getOptimizingConfig().isMetadataBasedTriggerEnabled()
        && !MetadataBasedEvaluationEvent.isEvaluatingNecessary(
            context.getOptimizingConfig(), mixedTable, context.getLastPlanTime())) {
      return java.util.Optional.empty();
    }

    org.apache.amoro.table.TableSnapshot snapshot = buildCurrentSnapshot();
    ServerTableIdentifier identifier = context.getTableIdentifier();
    AbstractOptimizingEvaluator evaluator =
        IcebergOptimizingEvaluatorFactory.create(
            identifier,
            context.getOptimizingConfig(),
            mixedTable,
            snapshot,
            maxPendingPartitions,
            context.getLastMinorOptimizingTime(),
            context.getLastFullOptimizingTime(),
            context.getLastMajorOptimizingTime());

    boolean necessary = evaluator.isNecessary();
    return java.util.Optional.of(
        new PendingInputResult(
            evaluator.getPendingInput(), evaluator.getOptimizingPendingInput(), necessary));
  }

  private org.apache.amoro.table.TableSnapshot buildCurrentSnapshot() {
    if (mixedTable.isUnkeyedTable()) {
      org.apache.iceberg.Snapshot snap = mixedTable.asUnkeyedTable().currentSnapshot();
      long snapshotId =
          snap != null ? snap.snapshotId() : TableRuntimeOptimizingState.INVALID_SNAPSHOT_ID;
      return new BasicTableSnapshot(snapshotId);
    } else {
      org.apache.iceberg.Snapshot baseSnap =
          mixedTable.asKeyedTable().baseTable().currentSnapshot();
      org.apache.iceberg.Snapshot changeSnap =
          mixedTable.asKeyedTable().changeTable().currentSnapshot();
      long baseId =
          baseSnap != null
              ? baseSnap.snapshotId()
              : TableRuntimeOptimizingState.INVALID_SNAPSHOT_ID;
      long changeId =
          changeSnap != null
              ? changeSnap.snapshotId()
              : TableRuntimeOptimizingState.INVALID_SNAPSHOT_ID;
      return new KeyedTableSnapshot(baseId, changeId);
    }
  }

  @Override
  public long snapshotCount() {
    org.apache.amoro.table.UnkeyedTable ut =
        mixedTable.isKeyedTable()
            ? mixedTable.asKeyedTable().baseTable()
            : mixedTable.asUnkeyedTable();
    return Lists.newArrayList(ut.snapshots().iterator()).size();
  }

  @Override
  public void refreshOptimizingMetrics(OptimizationContext context) {
    org.apache.amoro.table.UnkeyedTable unkeyedTable =
        mixedTable.isKeyedTable()
            ? mixedTable.asKeyedTable().baseTable()
            : mixedTable.asUnkeyedTable();

    org.apache.iceberg.Snapshot currentSnapshot = unkeyedTable.currentSnapshot();
    if (currentSnapshot != null) {
      context.updateNonMaintainedSnapshotTime(
          computeNonMaintainedTime(currentSnapshot, unkeyedTable));
    }

    org.apache.iceberg.Snapshot optimizingSnapshot =
        org.apache.amoro.formats.iceberg.utils.IcebergTableUtil.findLatestOptimizingSnapshot(
                unkeyedTable)
            .orElse(null);
    if (optimizingSnapshot != null) {
      context.updateLastOptimizingSnapshotTime(optimizingSnapshot.timestampMillis());
    }

    if (mixedTable.isKeyedTable()) {
      org.apache.amoro.table.UnkeyedTable changeTable = mixedTable.asKeyedTable().changeTable();
      org.apache.iceberg.Snapshot changeSnapshot = changeTable.currentSnapshot();
      if (changeSnapshot != null) {
        context.updateNonMaintainedSnapshotTime(
            computeNonMaintainedTime(changeSnapshot, changeTable));
      }
    }
  }

  private long computeNonMaintainedTime(
      org.apache.iceberg.Snapshot snapshot, org.apache.amoro.table.UnkeyedTable table) {
    if (snapshot.summary().values().stream()
        .anyMatch(
            org.apache.amoro.formats.iceberg.maintainer.IcebergTableMaintainer
                    .AMORO_MAINTAIN_COMMITS
                ::contains)) {
      return -1;
    }
    if (Long.parseLong(
            snapshot
                .summary()
                .getOrDefault(org.apache.iceberg.SnapshotSummary.ADDED_FILES_PROP, "0"))
        == 0) {
      return -1;
    }
    return snapshot.timestampMillis();
  }
}

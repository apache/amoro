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

package org.apache.amoro.formats.iceberg;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.formats.iceberg.maintainer.IcebergTableMaintainer;
import org.apache.amoro.formats.iceberg.utils.IcebergTableUtil;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.optimizing.OptimizationContext;
import org.apache.amoro.optimizing.PendingInputResult;
import org.apache.amoro.optimizing.TableRuntimeOptimizingState;
import org.apache.amoro.optimizing.evaluation.MetadataBasedEvaluationEvent;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.optimizing.plan.IcebergOptimizingEvaluatorFactory;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.BasicTableSnapshot;
import org.apache.amoro.table.BasicUnkeyedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.Table;

import java.util.Map;
import java.util.Optional;

public class IcebergTable implements AmoroTable<UnkeyedTable> {

  /**
   * build an amoro table implement which table format is iceberg.
   *
   * @param identifier table identifier.
   * @param icebergTable iceberg table
   * @param metaStore auth context
   * @param catalogProperties catalog properties.
   * @return amoro table.
   */
  public static IcebergTable newIcebergTable(
      TableIdentifier identifier,
      Table icebergTable,
      TableMetaStore metaStore,
      Map<String, String> catalogProperties) {
    AuthenticatedFileIO io =
        AuthenticatedFileIOs.buildAdaptIcebergFileIO(metaStore, icebergTable.io());

    UnkeyedTable wrapped =
        new BasicUnkeyedTable(
            identifier,
            MixedFormatCatalogUtil.useMixedTableOperations(
                icebergTable, icebergTable.location(), io, metaStore.getConfiguration()),
            io,
            catalogProperties) {
          @Override
          public TableFormat format() {
            return TableFormat.ICEBERG;
          }
        };
    return new IcebergTable(identifier, wrapped);
  }

  private final TableIdentifier identifier;
  private final UnkeyedTable table;

  protected IcebergTable(TableIdentifier identifier, UnkeyedTable table) {
    this.table = table;
    this.identifier = identifier;
  }

  @Override
  public TableIdentifier id() {
    return this.identifier;
  }

  @Override
  public TableFormat format() {
    return TableFormat.ICEBERG;
  }

  @Override
  public Map<String, String> properties() {
    return table.properties();
  }

  @Override
  public UnkeyedTable originalTable() {
    return table;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    Snapshot snapshot = table.currentSnapshot();
    if (snapshot == null) {
      return null;
    }
    return new IcebergSnapshot(snapshot);
  }

  @Override
  public long snapshotCount() {
    return Lists.newArrayList(table.snapshots().iterator()).size();
  }

  @Override
  public Optional<PendingInputResult> evaluatePendingInput(
      OptimizationContext context, int maxPendingPartitions) {
    if (context.getOptimizingConfig().isEnabled()
        && context.isIdle()
        && context.getOptimizingConfig().isMetadataBasedTriggerEnabled()
        && !MetadataBasedEvaluationEvent.isEvaluatingNecessary(
            context.getOptimizingConfig(), table, context.getLastPlanTime())) {
      return Optional.empty();
    }

    Snapshot current = table.currentSnapshot();
    long snapshotId =
        current != null ? current.snapshotId() : TableRuntimeOptimizingState.INVALID_SNAPSHOT_ID;
    AbstractOptimizingEvaluator evaluator =
        IcebergOptimizingEvaluatorFactory.create(
            context.getTableIdentifier(),
            context.getOptimizingConfig(),
            table,
            new BasicTableSnapshot(snapshotId),
            maxPendingPartitions,
            context.getLastMinorOptimizingTime(),
            context.getLastFullOptimizingTime(),
            context.getLastMajorOptimizingTime());

    boolean necessary = evaluator.isNecessary();
    return Optional.of(
        new PendingInputResult(
            evaluator.getPendingInput(), evaluator.getOptimizingPendingInput(), necessary));
  }

  @Override
  public void refreshOptimizingMetrics(OptimizationContext context) {
    Snapshot currentSnapshot = table.currentSnapshot();
    if (currentSnapshot != null) {
      context.updateNonMaintainedSnapshotTime(computeNonMaintainedTime(currentSnapshot));
    }

    Snapshot optimizingSnapshot = IcebergTableUtil.findLatestOptimizingSnapshot(table).orElse(null);
    if (optimizingSnapshot != null) {
      context.updateLastOptimizingSnapshotTime(optimizingSnapshot.timestampMillis());
    }
  }

  private long computeNonMaintainedTime(Snapshot snapshot) {
    if (snapshot.summary().values().stream()
        .anyMatch(IcebergTableMaintainer.AMORO_MAINTAIN_COMMITS::contains)) {
      return -1;
    }
    if (Long.parseLong(snapshot.summary().getOrDefault(SnapshotSummary.ADDED_FILES_PROP, "0"))
        == 0) {
      return -1;
    }
    return snapshot.timestampMillis();
  }
}

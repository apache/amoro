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

package org.apache.amoro.server.table.paimon;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.iceberg.Constants;
import org.apache.amoro.optimizing.TableRuntimeOptimizingState;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.table.TableRuntimeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Paimon-specific table runtime implementation.
 *
 * <p>Inherits the full optimizing/cleanup state machine from {@link DefaultTableRuntime}; only
 * overrides {@link #refresh(AmoroTable)} so snapshot refresh goes through {@link
 * AmoroTable#currentSnapshot()} (Paimon semantics) instead of the Iceberg-specific {@code
 * MixedTable} cast in the parent.
 *
 * <p>Paimon does not have a separate change-snapshot concept; the inherited {@link
 * TableRuntimeOptimizingState}'s {@code currentChangeSnapshotId} / {@code
 * lastOptimizedChangeSnapshotId} fields stay at their default (invalid) values — harmless for
 * Paimon because all Paimon-facing code paths read only {@code currentSnapshotId}.
 */
public class PaimonTableRuntime extends DefaultTableRuntime {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonTableRuntime.class);

  public PaimonTableRuntime(TableRuntimeStore store, Supplier<AmoroTable<?>> loader) {
    super(store, loader);
  }

  /**
   * Refresh the table runtime with the latest Paimon table metadata.
   *
   * <p>This override avoids the Iceberg-specific {@code MixedTable} cast in {@link
   * DefaultTableRuntime#refresh(AmoroTable)}; snapshot is read via {@link
   * AmoroTable#currentSnapshot()}.
   */
  @Override
  public PaimonTableRuntime refresh(AmoroTable<?> table) {
    Map<String, String> tableConfig = table.properties();
    TableConfiguration newConfiguration = TableConfigurations.parseTableConfig(tableConfig);
    String newGroupName = newConfiguration.getOptimizingConfig().getOptimizerGroup();

    if (!Objects.equals(getGroupName(), newGroupName) && getOptimizingProcess() != null) {
      getOptimizingProcess().close(false);
    }

    store()
        .begin()
        .updateTableConfig(
            config -> {
              config.clear();
              config.putAll(tableConfig);
            })
        .updateGroup(g -> newGroupName)
        .updateState(
            OPTIMIZING_STATE_KEY,
            s -> {
              refreshPaimonSnapshot(table, s);
              return s;
            })
        .commit();
    return this;
  }

  /**
   * Refresh the Paimon table snapshot id into state. Paimon has a single snapshot (no separate
   * change snapshot), so only {@code currentSnapshotId} is updated.
   */
  private void refreshPaimonSnapshot(AmoroTable<?> amoroTable, TableRuntimeOptimizingState state) {
    long lastSnapshotId = state.getCurrentSnapshotId();
    long currentSnapshotId = resolveSnapshotId(amoroTable);

    if (currentSnapshotId != lastSnapshotId) {
      LOG.debug(
          "Refreshing Paimon table {} with snapshot id {}",
          getTableIdentifier(),
          currentSnapshotId);
      state.setCurrentSnapshotId(currentSnapshotId);
    }
  }

  private long resolveSnapshotId(AmoroTable<?> amoroTable) {
    try {
      TableSnapshot snapshot = amoroTable.currentSnapshot();
      if (snapshot != null) {
        return Long.parseLong(snapshot.id());
      }
    } catch (Exception e) {
      LOG.error("Failed to get snapshot id from Paimon table {}", getTableIdentifier(), e);
    }
    return Constants.INVALID_SNAPSHOT_ID;
  }
}

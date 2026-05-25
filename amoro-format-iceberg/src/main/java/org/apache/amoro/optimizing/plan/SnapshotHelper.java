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

package org.apache.amoro.optimizing.plan;

import org.apache.amoro.optimizing.TableRuntimeOptimizingState;
import org.apache.amoro.table.BasicTableSnapshot;
import org.apache.amoro.table.KeyedTableSnapshot;
import org.apache.amoro.table.TableSnapshot;

/**
 * Builds {@link TableSnapshot} from {@link TableRuntimeOptimizingState} data, replacing {@code
 * IcebergTableUtil.getSnapshot(MixedTable, DefaultTableRuntime)}.
 *
 * <p>Lives in amoro-format-iceberg alongside the snapshot implementations.
 */
public class SnapshotHelper {

  public static TableSnapshot buildSnapshot(
      org.apache.amoro.table.MixedTable table, TableRuntimeOptimizingState state) {
    if (table.isUnkeyedTable()) {
      return new BasicTableSnapshot(state.getCurrentSnapshotId());
    } else {
      return new KeyedTableSnapshot(
          state.getCurrentSnapshotId(), state.getCurrentChangeSnapshotId());
    }
  }

  public static TableSnapshot buildSnapshot(
      boolean keyed, long currentSnapshotId, long currentChangeSnapshotId) {
    if (!keyed) {
      return new BasicTableSnapshot(currentSnapshotId);
    } else {
      return new KeyedTableSnapshot(currentSnapshotId, currentChangeSnapshotId);
    }
  }
}

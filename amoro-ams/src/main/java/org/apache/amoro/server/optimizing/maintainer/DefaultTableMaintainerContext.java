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

package org.apache.amoro.server.optimizing.maintainer;

import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.maintainer.MaintainerMetrics;
import org.apache.amoro.maintainer.OptimizingInfo;
import org.apache.amoro.maintainer.TableMaintainerContext;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableOrphanFilesCleaningMetrics;
import org.apache.amoro.server.utils.HiveLocationUtil;
import org.apache.amoro.table.MixedTable;

import java.util.Collections;
import java.util.Set;

/**
 * Default implementation of TableMaintainerContext for AMS. Adapts DefaultTableRuntime to
 * TableMaintainerContext interface.
 */
public class DefaultTableMaintainerContext implements TableMaintainerContext {

  private final DefaultTableRuntime tableRuntime;
  private final MixedTable mixedTable;

  public DefaultTableMaintainerContext(DefaultTableRuntime tableRuntime) {
    this.tableRuntime = tableRuntime;
    this.mixedTable = null;
  }

  public DefaultTableMaintainerContext(DefaultTableRuntime tableRuntime, MixedTable mixedTable) {
    this.tableRuntime = tableRuntime;
    this.mixedTable = mixedTable;
  }

  @Override
  public TableConfiguration getTableConfiguration() {
    return tableRuntime.getTableConfiguration();
  }

  @Override
  public MaintainerMetrics getMetrics() {
    TableOrphanFilesCleaningMetrics metrics = tableRuntime.getOrphanFilesCleaningMetrics();
    return new MaintainerMetrics() {
      @Override
      public void recordOrphanDataFilesCleaned(int expected, int cleaned) {
        metrics.completeOrphanDataFiles(expected, cleaned);
      }

      @Override
      public void recordOrphanMetadataFilesCleaned(int expected, int cleaned) {
        metrics.completeOrphanMetadataFiles(expected, cleaned);
      }
    };
  }

  @Override
  public OptimizingInfo getOptimizingInfo() {
    return new DefaultOptimizingInfo(tableRuntime);
  }

  @Override
  public Set<String> getHiveLocationPaths() {
    // Use HiveLocationUtil to get Hive location paths
    if (mixedTable == null) {
      return Collections.emptySet();
    }
    return HiveLocationUtil.getHiveLocation(mixedTable);
  }

  /** OptimizingInfo implementation based on DefaultTableRuntime. */
  private static class DefaultOptimizingInfo implements OptimizingInfo {

    private final DefaultTableRuntime tableRuntime;

    DefaultOptimizingInfo(DefaultTableRuntime tableRuntime) {
      this.tableRuntime = tableRuntime;
    }

    @Override
    public boolean isProcessing() {
      return tableRuntime.getOptimizingStatus().isProcessing();
    }

    @Override
    public long getTargetSnapshotId() {
      if (!isProcessing()) {
        return Long.MAX_VALUE;
      }
      return tableRuntime.getOptimizingProcess().getTargetSnapshotId();
    }
  }
}

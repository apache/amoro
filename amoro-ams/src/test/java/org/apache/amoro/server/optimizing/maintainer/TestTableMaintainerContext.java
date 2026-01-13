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
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.server.utils.HiveLocationUtil;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;

import java.util.Collections;
import java.util.Set;

/** Utility class for creating test TableMaintainerContext instances. */
public class TestTableMaintainerContext {

  /** Create a test TableMaintainerContext for the given MixedTable. */
  public static TableMaintainerContext of(MixedTable table) {
    return new Impl(TableConfigurations.parseTableConfig(table.properties()), table);
  }

  /** Create a test TableMaintainerContext for the given KeyedTable. */
  public static TableMaintainerContext of(KeyedTable table) {
    return new Impl(TableConfigurations.parseTableConfig(table.properties()));
  }

  /** Create a test TableMaintainerContext for the given UnkeyedTable. */
  public static TableMaintainerContext of(UnkeyedTable table) {
    return new Impl(TableConfigurations.parseTableConfig(table.properties()));
  }

  /** Test implementation of TableMaintainerContext. */
  public static class Impl implements TableMaintainerContext {
    private final TableConfiguration tableConfiguration;
    private OptimizingInfo optimizingInfo;
    private final MixedTable mixedTable;

    public Impl(TableConfiguration tableConfiguration) {
      this.tableConfiguration = tableConfiguration;
      this.optimizingInfo = OptimizingInfo.EMPTY;
      this.mixedTable = null;
    }

    public Impl(TableConfiguration tableConfiguration, MixedTable mixedTable) {
      this.tableConfiguration = tableConfiguration;
      this.optimizingInfo = OptimizingInfo.EMPTY;
      this.mixedTable = mixedTable;
    }

    public void setOptimizingInfo(OptimizingInfo optimizingInfo) {
      this.optimizingInfo = optimizingInfo;
    }

    @Override
    public TableConfiguration getTableConfiguration() {
      return tableConfiguration;
    }

    @Override
    public MaintainerMetrics getMetrics() {
      return MaintainerMetrics.NOOP;
    }

    @Override
    public OptimizingInfo getOptimizingInfo() {
      return optimizingInfo;
    }

    @Override
    public Set<String> getHiveLocationPaths() {
      // Use HiveLocationUtil to get Hive location paths
      if (mixedTable == null) {
        return Collections.emptySet();
      }
      return HiveLocationUtil.getHiveLocation(mixedTable);
    }
  }
}

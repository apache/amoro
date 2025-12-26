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

package org.apache.amoro.maintainer;

import org.apache.amoro.config.TableConfiguration;

import java.util.Set;

/**
 * Context interface for table maintainer operations.
 *
 * <p>Provides necessary configuration and runtime information for table maintenance without
 * depending on AMS-specific implementations.
 */
public interface TableMaintainerContext {

  /**
   * Get the table configuration.
   *
   * @return table configuration containing all maintenance-related settings
   */
  TableConfiguration getTableConfiguration();

  /**
   * Get the metrics collector for maintenance operations.
   *
   * @return metrics collector, may return NoopMaintainerMetrics if metrics not supported
   */
  MaintainerMetrics getMetrics();

  /**
   * Get optimizing process information if available.
   *
   * @return optimizing information, may return EmptyOptimizingInfo if no optimizing process
   */
  OptimizingInfo getOptimizingInfo();

  /**
   * Get Hive table/partition location paths if the table is a Hive-backed table.
   *
   * <p>This is used to exclude Hive-managed files from being cleaned during maintenance operations.
   * For non-Hive tables, returns an empty set.
   *
   * @return set of Hive location paths, or empty set if not a Hive table
   */
  Set<String> getHiveLocationPaths();
}

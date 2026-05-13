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

package org.apache.amoro.optimizing;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.config.OptimizingConfig;

/**
 * Read-only context provided to {@code AmoroTable.evaluatePendingInput()} during refresh
 * evaluation. Implemented by {@code DefaultTableRuntime} in amoro-ams.
 */
public interface OptimizationContext {

  ServerTableIdentifier getTableIdentifier();

  OptimizingConfig getOptimizingConfig();

  boolean isIdle();

  long getLastPlanTime();

  long getLastMinorOptimizingTime();

  long getLastFullOptimizingTime();

  long getLastMajorOptimizingTime();

  /**
   * Record a non-maintained snapshot timestamp for optimizing lag metrics. Only meaningful for
   * Iceberg-based formats; other formats can ignore this.
   */
  default void updateNonMaintainedSnapshotTime(long timestampMillis) {}

  /**
   * Record the latest optimizing snapshot timestamp. Only meaningful for Iceberg-based formats;
   * other formats can ignore this.
   */
  default void updateLastOptimizingSnapshotTime(long timestampMillis) {}
}

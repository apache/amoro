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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.table.TableSnapshot;

/**
 * Creates {@link AbstractOptimizingEvaluator} instances for Iceberg-based formats.
 *
 * <p>Lives in amoro-format-iceberg to avoid amoro-format-iceberg -> amoro-ams dependency cycle.
 * Takes format-agnostic parameters (no DefaultTableRuntime dependency).
 *
 * <p>Supports {@link TableFormat#ICEBERG}, {@link TableFormat#MIXED_ICEBERG}, and {@link
 * TableFormat#MIXED_HIVE}. MIXED_HIVE uses {@link MixedIcebergOptimizingEvaluator} as the factory
 * is in amoro-format-iceberg; for precise hiveLocation-aware partition evaluation, extend {@link
 * MixedIcebergOptimizingEvaluator} in the mixed-hive module.
 */
public class IcebergOptimizingEvaluatorFactory {

  public static AbstractOptimizingEvaluator create(
      ServerTableIdentifier identifier,
      OptimizingConfig config,
      org.apache.amoro.table.MixedTable table,
      TableSnapshot snapshot,
      int maxPendingPartitions,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime,
      long lastMajorOptimizingTime) {

    if (TableFormat.ICEBERG.equals(table.format())) {
      return new IcebergOptimizerEvaluator(
          identifier,
          config,
          table,
          snapshot,
          maxPendingPartitions,
          lastMinorOptimizingTime,
          lastFullOptimizingTime,
          lastMajorOptimizingTime);
    } else if (TableFormat.MIXED_ICEBERG.equals(table.format())
        || TableFormat.MIXED_HIVE.equals(table.format())) {
      return new MixedIcebergOptimizingEvaluator(
          identifier,
          config,
          table,
          snapshot,
          maxPendingPartitions,
          lastMinorOptimizingTime,
          lastFullOptimizingTime,
          lastMajorOptimizingTime);
    }

    throw new IllegalArgumentException(
        "IcebergOptimizingEvaluatorFactory does not support format: " + table.format());
  }
}

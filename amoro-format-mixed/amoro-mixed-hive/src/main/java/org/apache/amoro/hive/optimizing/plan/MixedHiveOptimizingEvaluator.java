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

package org.apache.amoro.hive.optimizing.plan;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.optimizing.plan.MixedIcebergOptimizingEvaluator;
import org.apache.amoro.optimizing.plan.PartitionEvaluator;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableSnapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.Pair;

import java.util.Map;

public class MixedHiveOptimizingEvaluator extends MixedIcebergOptimizingEvaluator {
  public MixedHiveOptimizingEvaluator(
      ServerTableIdentifier identifier,
      OptimizingConfig config,
      MixedTable table,
      TableSnapshot currentSnapshot,
      int maxPendingPartitions,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime) {
    super(
        identifier,
        config,
        table,
        currentSnapshot,
        maxPendingPartitions,
        lastMinorOptimizingTime,
        lastFullOptimizingTime);
  }

  @Override
  protected PartitionEvaluator buildEvaluator(Pair<Integer, StructLike> partition) {
    String hiveLocation = (((SupportHive) mixedTable).hiveLocation());
    Map<String, String> partitionProperties = partitionProperties(partition);
    return new MixedHivePartitionPlan.MixedHivePartitionEvaluator(
        identifier,
        config,
        partition,
        partitionProperties,
        hiveLocation,
        System.currentTimeMillis(),
        mixedTable.isKeyedTable(),
        lastMinorOptimizingTime,
        lastFullOptimizingTime);
  }
}

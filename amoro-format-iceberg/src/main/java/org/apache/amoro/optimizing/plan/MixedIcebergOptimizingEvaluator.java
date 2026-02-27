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
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableSnapshot;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.Pair;

import java.util.Map;

public class MixedIcebergOptimizingEvaluator extends AbstractOptimizingEvaluator {
  public MixedIcebergOptimizingEvaluator(
      ServerTableIdentifier identifier,
      OptimizingConfig config,
      MixedTable table,
      TableSnapshot currentSnapshot,
      int maxPendingPartitions,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime,
      long lastMajorOptimizingTime) {
    super(
        identifier,
        config,
        table,
        currentSnapshot,
        maxPendingPartitions,
        lastMinorOptimizingTime,
        lastFullOptimizingTime,
        lastMajorOptimizingTime);
  }

  protected Map<String, String> partitionProperties(Pair<Integer, StructLike> partition) {
    return TablePropertyUtil.getPartitionProperties(mixedTable, partition.second());
  }

  @Override
  protected PartitionEvaluator buildEvaluator(Pair<Integer, StructLike> partition) {
    Map<String, String> partitionProperties = partitionProperties(partition);
    return new MixedIcebergPartitionPlan.MixedIcebergPartitionEvaluator(
        identifier,
        config,
        partition,
        partitionProperties,
        System.currentTimeMillis(),
        mixedTable.isKeyedTable(),
        lastMinorOptimizingTime,
        lastFullOptimizingTime,
        lastMajorOptimizingTime);
  }
}

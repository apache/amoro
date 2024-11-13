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
import org.apache.amoro.optimizing.plan.AbstractOptimizingPlanner;
import org.apache.amoro.optimizing.plan.PartitionEvaluator;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableSnapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.util.Pair;

public class MixedHiveOptimizingPlanner extends AbstractOptimizingPlanner {
  private final String hiveLocation;

  public MixedHiveOptimizingPlanner(
      ServerTableIdentifier identifier,
      OptimizingConfig config,
      MixedTable table,
      TableSnapshot snapshot,
      Expression partitionFilter,
      long processId,
      double availableCore,
      long maxInputSizePerThread,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime) {
    super(
        identifier,
        config,
        table,
        snapshot,
        partitionFilter,
        processId,
        availableCore,
        maxInputSizePerThread,
        lastMinorOptimizingTime,
        lastFullOptimizingTime);
    this.hiveLocation = (((SupportHive) mixedTable).hiveLocation());
  }

  @Override
  protected PartitionEvaluator buildEvaluator(Pair<Integer, StructLike> partition) {
    return new MixedHivePartitionPlan(
        identifier,
        mixedTable,
        config,
        partition,
        hiveLocation,
        planTime,
        lastMinorOptimizingTime,
        lastFullOptimizingTime);
  }
}

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

package com.netease.arctic.server.optimizing.plan;

import com.clearspring.analytics.util.Lists;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import com.netease.arctic.server.table.KeyedTableSnapshot;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.TableTypeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OptimizingPlanner extends OptimizingEvaluator {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizingPlanner.class);

  private static final long MAX_INPUT_FILE_SIZE_PER_THREAD = 5L * 1024 * 1024 * 1024;

  private final TableFileScanHelper.PartitionFilter partitionFilter;

  // protected long processId;
  private final double availableCore;
  private final int totalParallelism;
  private final long planTime;
  private final Map<String, OptimizingType> optimizingTypes = Maps.newHashMap();
  private final PartitionPlannerFactory partitionPlannerFactory;
  private Map<String, List<TaskDescriptor>> partitionTaskDescriptors = Maps.newHashMap();;

  public OptimizingPlanner(TableRuntime tableRuntime, ArcticTable table, double availableCore, int totalParallelism) {
    super(tableRuntime, table);
    this.partitionFilter = tableRuntime.getPendingInput() == null ?
        null : tableRuntime.getPendingInput().getPartitions()::contains;
    this.availableCore = availableCore;
    this.totalParallelism = totalParallelism;
    this.planTime = System.currentTimeMillis();
    this.partitionPlannerFactory = new PartitionPlannerFactory(arcticTable, tableRuntime, planTime);
  }

  @Override
  protected PartitionEvaluator buildEvaluator(String partitionPath) {
    return partitionPlannerFactory.buildPartitionPlanner(partitionPath);
  }

  public Map<String, Long> getFromSequence() {
    return partitionPlanMap.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> ((AbstractPartitionPlan) e.getValue()).getFromSequence()));
  }

  public Map<String, Long> getToSequence() {
    return partitionPlanMap.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> ((AbstractPartitionPlan) e.getValue()).getToSequence()));
  }

  @Override
  protected TableFileScanHelper.PartitionFilter getPartitionFilter() {
    return partitionFilter;
  }

  public long getTargetSnapshotId() {
    return currentSnapshot.snapshotId();
  }

  public long getTargetChangeSnapshotId() {
    if (currentSnapshot instanceof KeyedTableSnapshot) {
      return ((KeyedTableSnapshot) currentSnapshot).changeSnapshotId();
    } else {
      return ArcticServiceConstants.INVALID_SNAPSHOT_ID;
    }
  }

  @Override
  public boolean isNecessary() {
    if (!super.isNecessary()) {
      return false;
    }
    return !planTasks().isEmpty();
  }

  public List<TaskDescriptor> planTasks() {
    return planPartitionedTasks().entrySet().stream()
        .flatMap(kv -> kv.getValue().stream())
        .collect(Collectors.toList());
  }

  public Map<String, List<TaskDescriptor>> planPartitionedTasks() {
    if (!this.partitionTaskDescriptors.isEmpty()) {
      return this.partitionTaskDescriptors;
    }
    long startTime = System.nanoTime();

    if (!isInitialized) {
      initEvaluator();
    }
    if (!super.isNecessary()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("{} === skip planning", tableRuntime.getTableIdentifier());
      }
      return cacheAndReturnTasks(Collections.emptyMap());
    }

    List<PartitionEvaluator> evaluators = new ArrayList<>(partitionPlanMap.values());
    evaluators.sort(Comparator.comparing(PartitionEvaluator::getWeight, Collections.reverseOrder()));

    double maxInputSize = MAX_INPUT_FILE_SIZE_PER_THREAD * availableCore;
    List<PartitionEvaluator> inputPartitions = Lists.newArrayList();
    long actualInputSize = 0;
    for (int i = 0; i < evaluators.size() && actualInputSize < maxInputSize; i++) {
      PartitionEvaluator evaluator = evaluators.get(i);
      inputPartitions.add(evaluator);
      if (actualInputSize + evaluator.getCost() < maxInputSize) {
        actualInputSize += evaluator.getCost();
      }
    }

    double avgThreadCost = actualInputSize / availableCore;
    int limitCost = (int) (actualInputSize / avgThreadCost);
    for (PartitionEvaluator evaluator : inputPartitions) {
      List<TaskDescriptor> tasks = ((AbstractPartitionPlan) evaluator)
          .splitTasks(totalParallelism);
      if (!tasks.isEmpty()) {
        optimizingTypes.put(evaluator.getPartition(), evaluator.getOptimizingType());
        partitionTaskDescriptors.put(evaluator.getPartition(), tasks);
      }
    }
    long endTime = System.nanoTime();
    LOG.info("{} finish plan, get tasks: {}, cost {} ns, {} ms", tableRuntime.getTableIdentifier(),
        partitionTaskDescriptors.entrySet().stream()
            .map(kv -> (StringUtils.isBlank(kv.getKey()) ? "null" : kv.getKey()) + " => " + kv.getValue().size())
            .collect(Collectors.joining(", ", "[", "]")),
        endTime - startTime,
        (endTime - startTime) / 1_000_000);
    return cacheAndReturnTasks(partitionTaskDescriptors);
  }

  private Map<String, List<TaskDescriptor>> cacheAndReturnTasks(Map<String, List<TaskDescriptor>> tasks) {
    this.partitionTaskDescriptors = tasks;
    return this.partitionTaskDescriptors;
  }

  public long getPlanTime() {
    return planTime;
  }

  public OptimizingType getOptimizingType() {
    Preconditions.checkArgument(!partitionTaskDescriptors.isEmpty(),
        "Optimizing tasks are empty, optimizing is not started");
    if (optimizingTypes.values().stream()
        .anyMatch(t -> t == OptimizingType.FULL)) {
      return OptimizingType.FULL;
    } else if (optimizingTypes.values().stream()
        .anyMatch(t -> t == OptimizingType.MAJOR)) {
      return OptimizingType.MAJOR;
    } else {
      return OptimizingType.MINOR;
    }
  }

  public OptimizingType getOptTypeByPartition(String partition) {
    return optimizingTypes.get(partition);
  }

  public long getNewProcessId() {
    return Math.max(tableRuntime.getNewestProcessId() + 1, System.currentTimeMillis());
  }

  private static class PartitionPlannerFactory {
    private final ArcticTable arcticTable;
    private final TableRuntime tableRuntime;
    private final String hiveLocation;
    private final long planTime;

    public PartitionPlannerFactory(ArcticTable arcticTable, TableRuntime tableRuntime, long planTime) {
      this.arcticTable = arcticTable;
      this.tableRuntime = tableRuntime;
      this.planTime = planTime;
      if (com.netease.arctic.hive.utils.TableTypeUtil.isHive(arcticTable)) {
        this.hiveLocation = (((SupportHive) arcticTable).hiveLocation());
      } else {
        this.hiveLocation = null;
      }
    }

    public PartitionEvaluator buildPartitionPlanner(String partitionPath) {
      if (TableTypeUtil.isIcebergTableFormat(arcticTable)) {
        return new IcebergPartitionPlan(tableRuntime, arcticTable, partitionPath, planTime);
      } else {
        if (com.netease.arctic.hive.utils.TableTypeUtil.isHive(arcticTable)) {
          return new MixedHivePartitionPlan(tableRuntime, arcticTable, partitionPath, hiveLocation, planTime);
        } else {
          return new MixedIcebergPartitionPlan(tableRuntime, arcticTable, partitionPath, planTime);
        }
      }
    }
  }
}

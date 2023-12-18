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

package com.netease.arctic.server;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.process.TableProcess;
import com.netease.arctic.server.process.optimizing.DefaultOptimizingProcess;
import com.netease.arctic.server.process.optimizing.DefaultOptimizingState;
import com.netease.arctic.server.process.optimizing.OptimizingStage;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptimizingScheduler extends AbstractScheduler<DefaultOptimizingState> {

  private static final Map<String, Comparator<TableRuntime>> OPTIMIZING_POLICIES = ImmutableMap.of(
      ArcticServiceConstants.SCHEDULING_POLICY_QUOTA, new QuotaOccupySorter(),
      ArcticServiceConstants.SCHEDULING_POLICY_BALANCED, new BalancedSorter()
  );

  private final int maxPlanningParallelism;
  private final Map<ServerTableIdentifier, TableRuntime> tableRuntimeMap = new HashMap<>();
  private final Comparator<TableRuntime> tableSorter;

  public OptimizingScheduler(
      ResourceGroup optimizerGroup,
      int maxPlanningParallelism) {
    super(optimizerGroup);
    this.maxPlanningParallelism = maxPlanningParallelism;
    this.tableSorter = OPTIMIZING_POLICIES.get(getPolicyName(optimizerGroup));
  }

  private String getPolicyName(ResourceGroup resourceGroup) {
    String policy = Optional.ofNullable(resourceGroup.getProperties()).map(
        props -> props.getOrDefault(
            ArcticServiceConstants.SCHEDULING_POLICY_PROPERTY_NAME,
            ArcticServiceConstants.SCHEDULING_POLICY_QUOTA))
        .orElse(ArcticServiceConstants.SCHEDULING_POLICY_QUOTA);
    return OPTIMIZING_POLICIES.containsKey(policy) ? policy : ArcticServiceConstants.SCHEDULING_POLICY_QUOTA;
  }

  public void refreshTable(TableRuntime tableRuntime) {
    schedulerLock.lock();
    try {
      if (tableRuntime.isOptimizingEnabled()) {
        tableRuntimeMap.put(tableRuntime.getTableIdentifier(), tableRuntime);
        LOG.info(
            "Bind queue {} success with table {}",
            optimizerGroup.getName(),
            tableRuntime.getTableIdentifier());
      }
    } finally {
      schedulerLock.unlock();
    }
  }

  public void releaseTable(TableRuntime tableRuntime) {
    schedulerLock.lock();
    try {
      tableRuntimeMap.remove(tableRuntime.getTableIdentifier());
      LOG.info(
          "Release queue {} with table {}",
          optimizerGroup.getName(),
          tableRuntime.getTableIdentifier());
    } finally {
      schedulerLock.unlock();
    }
  }

  public boolean containsTable(ServerTableIdentifier identifier) {
    schedulerLock.lock();
    try {
      return tableRuntimeMap.containsKey(identifier);
    } finally {
      schedulerLock.unlock();
    }
  }

  @Override
  public List<TableRuntime> listTables() {
    schedulerLock.lock();
    try {
      return Stream.concat(
              tableRuntimeMap.values().stream(),
              tableProcessQueue.stream().map(TableProcess::getTableRuntime))
          .collect(Collectors.toList());
    } finally {
      schedulerLock.unlock();
    }
  }

  @Override
  protected TableProcess<DefaultOptimizingState> createProcess(TableRuntime tableRuntime, Action action) {
    Preconditions.checkState(action == Action.OPTIMIZING);
    return new DefaultOptimizingProcess(tableRuntime, false);
  }

  @Override
  protected TableProcess<DefaultOptimizingState> recoverProcess(
      TableRuntime tableRuntime, Action action, DefaultOptimizingState state) {
    Preconditions.checkState(action == Action.OPTIMIZING);
    if (state.getStage().isOptimizing()) {
      return new DefaultOptimizingProcess(tableRuntime, true);
    } else {
      return null;
    }
  }

  @Override
  public Pair<TableRuntime, Action> scheduleTable() {
    int planningCount = tableProcessQueue.stream()
        .map(TableProcess::getState)
        .filter(state -> state.getStage().isOptimizing())
        .mapToInt(state -> 1)
        .sum();
    TableRuntime targetTable = planningCount < maxPlanningParallelism ? scheduleInternal() : null;
    return targetTable != null ? Pair.of(scheduleInternal(), Action.OPTIMIZING) : null;
  }

  public TableRuntime scheduleInternal() {
    Set<TableRuntime> skipSet = fillSkipSet();
    return tableRuntimeMap.values().stream()
        .filter(tableRuntime -> !skipSet.contains(tableRuntime))
        .min(tableSorter)
        .orElse(null);
  }

  private Set<TableRuntime> fillSkipSet() {
    long currentTime = System.currentTimeMillis();
    return tableRuntimeMap.values().stream()
        .filter(
            tableRuntime ->
                !isTablePending(tableRuntime)
                    || tableRuntime.getBlockerRuntime().isBlocked(BlockableOperation.OPTIMIZE)
                    || currentTime - tableRuntime.getDefaultOptimizingState().getStartTime()
                    < tableRuntime.getOptimizingConfig().getMinPlanInterval())
        .collect(Collectors.toSet());
  }

  private boolean isTablePending(TableRuntime tableRuntime) {
    DefaultOptimizingState state = tableRuntime.getDefaultOptimizingState();
    return state.getStage() == OptimizingStage.PENDING
        && (state.getTargetSnapshotId() != state.getLastOptimizedSnapshotId()
        || state.getLastOptimizedChangeSnapshotId() != state.getTargetChangeSnapshotId());
  }

  private static class QuotaOccupySorter implements Comparator<TableRuntime> {

    @Override
    public int compare(TableRuntime one, TableRuntime another) {
      return Double.compare(one.getDefaultOptimizingState().getQuotaOccupy(),
          another.getDefaultOptimizingState().getQuotaOccupy());
    }
  }

  private static class BalancedSorter implements Comparator<TableRuntime> {
    @Override
    public int compare(TableRuntime one, TableRuntime another) {
      return Long.compare(
          Math.max(
              one.getDefaultOptimizingState().getLastFullOptimizingTime(),
              Math.max(one.getDefaultOptimizingState().getLastMinorOptimizingTime(),
                  one.getDefaultOptimizingState().getLastMajorOptimizingTime())),
          Math.max(
              another.getDefaultOptimizingState().getLastFullOptimizingTime(),
              Math.max(
                  another.getDefaultOptimizingState().getLastMinorOptimizingTime(),
                  another.getDefaultOptimizingState().getLastMajorOptimizingTime())));
    }
  }
}

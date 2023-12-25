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
import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.ams.api.TableRuntime;
import com.netease.arctic.ams.api.process.OptimizingStage;
import com.netease.arctic.ams.api.process.TableProcess;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.process.DefaultOptimizingProcess;
import com.netease.arctic.server.process.DefaultOptimizingState;
import com.netease.arctic.server.process.ManagedProcess;
import com.netease.arctic.server.table.DefaultTableRuntime;
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

public class OptimizingScheduler extends TaskScheduler<DefaultOptimizingState> {

  private static final Map<String, Comparator<DefaultTableRuntime>> OPTIMIZING_POLICIES =
      ImmutableMap.of(
          ArcticServiceConstants.SCHEDULING_POLICY_QUOTA, new QuotaOccupySorter(),
          ArcticServiceConstants.SCHEDULING_POLICY_BALANCED, new BalancedSorter());

  private final int maxPlanningParallelism;
  private final Map<ServerTableIdentifier, DefaultTableRuntime> tableRuntimeMap = new HashMap<>();
  private final Comparator<DefaultTableRuntime> tableSorter;
  private double allTargetQuota = 0;

  public OptimizingScheduler(ResourceGroup optimizerGroup, int maxPlanningParallelism) {
    super(optimizerGroup);
    this.maxPlanningParallelism = maxPlanningParallelism;
    this.tableSorter = OPTIMIZING_POLICIES.get(getPolicyName(optimizerGroup));
  }

  private String getPolicyName(ResourceGroup resourceGroup) {
    String policy =
        Optional.ofNullable(resourceGroup.getProperties())
            .map(
                props ->
                    props.getOrDefault(
                        ArcticServiceConstants.SCHEDULING_POLICY_PROPERTY_NAME,
                        ArcticServiceConstants.SCHEDULING_POLICY_QUOTA))
            .orElse(ArcticServiceConstants.SCHEDULING_POLICY_QUOTA);
    return OPTIMIZING_POLICIES.containsKey(policy)
        ? policy
        : ArcticServiceConstants.SCHEDULING_POLICY_QUOTA;
  }

  public void refreshTable(DefaultTableRuntime tableRuntime) {
    schedulerLock.lock();
    try {
      if (tableRuntime.getTableConfiguration().getOptimizingConfig().isEnabled()) {
        tableRuntimeMap.put(tableRuntime.getTableIdentifier(), tableRuntime);
        allTargetQuota +=
            tableRuntime.getTableConfiguration().getOptimizingConfig().getTargetQuota();
        LOG.info(
            "Bind queue {} success with table {}",
            optimizerGroup.getName(),
            tableRuntime.getTableIdentifier());
      }
    } finally {
      schedulerLock.unlock();
    }
  }

  public void releaseTable(DefaultTableRuntime tableRuntime) {
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
  protected ManagedProcess<DefaultOptimizingState> createProcess(
      DefaultTableRuntime tableRuntime, Action action) {
    Preconditions.checkState(action == Action.OPTIMIZING);
    return new DefaultOptimizingProcess(tableRuntime, false);
  }

  @Override
  protected ManagedProcess<DefaultOptimizingState> recoverProcess(
      DefaultTableRuntime tableRuntime, Action action, DefaultOptimizingState state) {
    Preconditions.checkState(action == Action.OPTIMIZING);
    if (state.getStage().isOptimizing()) {
      return new DefaultOptimizingProcess(tableRuntime, true);
    } else {
      return null;
    }
  }

  @Override
  public Pair<DefaultTableRuntime, Action> scheduleTable() {
    int planningCount =
        tableProcessQueue.stream()
            .map(TableProcess::getState)
            .filter(state -> state.getStage().isOptimizing())
            .mapToInt(state -> 1)
            .sum();
    DefaultTableRuntime targetTable =
        planningCount < maxPlanningParallelism ? scheduleInternal() : null;
    return targetTable != null ? Pair.of(scheduleInternal(), Action.OPTIMIZING) : null;
  }

  public DefaultTableRuntime scheduleInternal() {
    Set<DefaultTableRuntime> skipSet = fillSkipSet();
    return tableRuntimeMap.values().stream()
        .filter(tableRuntime -> !skipSet.contains(tableRuntime))
        .min(tableSorter)
        .orElse(null);
  }

  @Override
  public void setAvailableQuota(long availableQuota) {
    schedulerLock.lock();
    try {
      tableRuntimeMap
          .values()
          .forEach(
              tableRuntime ->
                  tableRuntime.setTargetQuota(
                      tableRuntime.getTableConfiguration().getOptimizingConfig().getTargetQuota()
                          * availableQuota
                          / allTargetQuota));
    } finally {
      schedulerLock.unlock();
    }
  }

  private Set<DefaultTableRuntime> fillSkipSet() {
    long currentTime = System.currentTimeMillis();
    return tableRuntimeMap.values().stream()
        .filter(
            tableRuntime ->
                !isTablePending(tableRuntime)
                    || tableRuntime.getBlockerRuntime().isBlocked(BlockableOperation.OPTIMIZE)
                    || currentTime - tableRuntime.getDefaultOptimizingState().getStartTime()
                        < tableRuntime
                            .getTableConfiguration()
                            .getOptimizingConfig()
                            .getMinPlanInterval())
        .collect(Collectors.toSet());
  }

  private boolean isTablePending(DefaultTableRuntime tableRuntime) {
    DefaultOptimizingState state = tableRuntime.getDefaultOptimizingState();
    return state.getStage() == OptimizingStage.PENDING
        && (state.getTargetSnapshotId() != state.getLastOptimizedSnapshotId()
            || state.getLastOptimizedChangeSnapshotId() != state.getTargetChangeSnapshotId());
  }

  private static class QuotaOccupySorter implements Comparator<DefaultTableRuntime> {

    @Override
    public int compare(DefaultTableRuntime one, DefaultTableRuntime another) {
      return Double.compare(
          one.getDefaultOptimizingState().getQuotaOccupy(),
          another.getDefaultOptimizingState().getQuotaOccupy());
    }
  }

  private static class BalancedSorter implements Comparator<DefaultTableRuntime> {
    @Override
    public int compare(DefaultTableRuntime one, DefaultTableRuntime another) {
      return Long.compare(
          Math.max(
              one.getDefaultOptimizingState().getLastFullOptimizingTime(),
              Math.max(
                  one.getDefaultOptimizingState().getLastMinorOptimizingTime(),
                  one.getDefaultOptimizingState().getLastMajorOptimizingTime())),
          Math.max(
              another.getDefaultOptimizingState().getLastFullOptimizingTime(),
              Math.max(
                  another.getDefaultOptimizingState().getLastMinorOptimizingTime(),
                  another.getDefaultOptimizingState().getLastMajorOptimizingTime())));
    }
  }
}

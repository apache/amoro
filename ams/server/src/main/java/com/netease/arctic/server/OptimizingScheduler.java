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
import com.netease.arctic.ams.api.config.OptimizingConfig;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptimizingScheduler extends TaskScheduler<DefaultOptimizingState> {

  private static final Map<String, Comparator<Pair<DefaultTableRuntime, Action>>>
      OPTIMIZING_POLICIES =
          ImmutableMap.of(
              ArcticServiceConstants.SCHEDULING_POLICY_QUOTA, new QuotaOccupySorter(),
              ArcticServiceConstants.SCHEDULING_POLICY_WATERMARK, new MajorWatermarkSorter());

  private final int maxPlanningParallelism;
  private final Map<ServerTableIdentifier, DefaultTableRuntime> tableRuntimeMap = new HashMap<>();
  private final Comparator<Pair<DefaultTableRuntime, Action>> tableSorter;
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
  public void setAvailableQuota(long quota) {
    schedulerLock.lock();
    try {
      tableRuntimeMap
          .values()
          .forEach(
              tableRuntime -> {
                OptimizingConfig config =
                    tableRuntime.getTableConfiguration().getOptimizingConfig();
                tableRuntime
                    .getOptimizingQuota()
                    .setQuotaTarget(config.getTargetQuota() * quota / allTargetQuota);
              });
    } finally {
      schedulerLock.unlock();
    }
  }

  @Override
  protected ManagedProcess<DefaultOptimizingState> createProcess(
      DefaultTableRuntime tableRuntime, Action action) {
    Preconditions.checkState(
        action == Action.MINOR_OPTIMIZING || action == Action.MAJOR_OPTIMIZING);
    return new DefaultOptimizingProcess(
        action == Action.MINOR_OPTIMIZING
            ? tableRuntime.getMinorOptimizingState()
            : tableRuntime.getMajorOptimizingState(),
        tableRuntime,
        false);
  }

  @Override
  protected ManagedProcess<DefaultOptimizingState> recoverProcess(
      DefaultTableRuntime tableRuntime, Action action, DefaultOptimizingState state) {
    Preconditions.checkState(
        action == Action.MINOR_OPTIMIZING || action == Action.MAJOR_OPTIMIZING);
    if (state.getStage().isOptimizing()) {
      return new DefaultOptimizingProcess(
          action == Action.MINOR_OPTIMIZING
              ? tableRuntime.getMinorOptimizingState()
              : tableRuntime.getMajorOptimizingState(),
          tableRuntime,
          true);
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
    return planningCount < maxPlanningParallelism ? scheduleInternal() : null;
  }

  private Pair<DefaultTableRuntime, Action> scheduleInternal() {
    return tableRuntimeMap.values().stream()
        .map(tableRuntime -> Pair.of(tableRuntime, determineAction(tableRuntime)))
        .filter(pair -> pair.getRight() != null && isReady(pair.getLeft()))
        .min(tableSorter)
        .orElse(null);
  }

  private Action determineAction(DefaultTableRuntime tableRuntime) {
    if (tableRuntime.needMinorOptimizing()) {
      return Action.MINOR_OPTIMIZING;
    } else if (tableRuntime.needMajorOptimizing()) {
      return Action.MAJOR_OPTIMIZING;
    } else {
      return null;
    }
  }

  private boolean isReady(DefaultTableRuntime tableRuntime) {
    return !tableRuntime.getBlockerRuntime().isBlocked(BlockableOperation.OPTIMIZE)
        && System.currentTimeMillis() - tableRuntime.getLastTriggerOptimizingTime()
            < tableRuntime.getTableConfiguration().getOptimizingConfig().getMinPlanInterval();
  }

  private static class QuotaOccupySorter implements Comparator<Pair<DefaultTableRuntime, Action>> {

    @Override
    public int compare(Pair<DefaultTableRuntime, Action> p1, Pair<DefaultTableRuntime, Action> p2) {
      double quota1 = p1.getLeft().getOptimizingQuota().getQuotaOccupy();
      double quota2 = p2.getLeft().getOptimizingQuota().getQuotaOccupy();
      if (quota1 == quota2) {
        return Long.compare(
            p1.getLeft().getOptimizingQuota().getQuotaRuntime(),
            p2.getLeft().getOptimizingQuota().getQuotaRuntime());
      } else {
        return Double.compare(quota1, quota2);
      }
    }
  }

  private static class MajorWatermarkSorter
      implements Comparator<Pair<DefaultTableRuntime, Action>> {

    private final Comparator<Pair<DefaultTableRuntime, Action>> quotaSorter =
        new QuotaOccupySorter();

    @Override
    public int compare(Pair<DefaultTableRuntime, Action> p1, Pair<DefaultTableRuntime, Action> p2) {
      if (p1.getRight() == p2.getRight()) {
        if (p1.getRight() == Action.MINOR_OPTIMIZING) {
          return Long.compare(
              p1.getLeft().getMinorOptimizingState().getWatermark(),
              p2.getLeft().getMinorOptimizingState().getWatermark());
        } else {
          return quotaSorter.compare(p1, p2);
        }
      } else {
        Pair<DefaultTableRuntime, Action> minorPair =
            p1.getRight() == Action.MINOR_OPTIMIZING ? p1 : p2;
        if (minorPair.getLeft().getOptimizingQuota().getQuotaOccupy() < 1) {
          return minorPair == p1 ? -1 : 1;
        } else {
          return quotaSorter.compare(p1, p2);
        }
      }
    }
  }
}

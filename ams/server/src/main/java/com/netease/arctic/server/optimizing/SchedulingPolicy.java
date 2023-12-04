package com.netease.arctic.server.optimizing;

import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SchedulingPolicy {

  private static final String SCHEDULING_POLICY_PROPERTY_NAME = "scheduling-policy";
  private static final String QUOTA = "quota";
  private static final String BALANCED = "balanced";

  private final Map<ServerTableIdentifier, TableRuntime> tableRuntimeMap = new HashMap<>();
  private Comparator<TableRuntime> tableSorter;
  private final Lock tableLock = new ReentrantLock();

  public SchedulingPolicy(ResourceGroup group) {
    setTableSorterIfNeeded(group);
  }

  public void setTableSorterIfNeeded(ResourceGroup optimizerGroup) {
    String schedulingPolicy =
        Optional.ofNullable(optimizerGroup.getProperties())
            .orElseGet(Maps::newHashMap)
            .getOrDefault(SCHEDULING_POLICY_PROPERTY_NAME, QUOTA);
    if (schedulingPolicy.equalsIgnoreCase(QUOTA)) {
      if (tableSorter == null || !(tableSorter instanceof QuotaOccupySorter)) {
        tableSorter = new QuotaOccupySorter();
      }
    } else if (schedulingPolicy.equalsIgnoreCase(BALANCED)) {
      if (tableSorter == null || !(tableSorter instanceof BalancedSorter)) {
        tableSorter = new BalancedSorter();
      }
    } else {
      throw new IllegalArgumentException("Illegal scheduling policy: " + schedulingPolicy);
    }
  }

  public TableRuntime scheduleTable(Set<ServerTableIdentifier> skipSet) {
    tableLock.lock();
    try {
      fillSkipSet(skipSet);
      return tableRuntimeMap.values().stream()
          .filter(tableRuntime -> !skipSet.contains(tableRuntime.getTableIdentifier()))
          .min(tableSorter)
          .orElse(null);
    } finally {
      tableLock.unlock();
    }
  }

  public TableRuntime getTableRuntime(ServerTableIdentifier tableIdentifier) {
    tableLock.lock();
    try {
      return tableRuntimeMap.get(tableIdentifier);
    } finally {
      tableLock.unlock();
    }
  }

  private void fillSkipSet(Set<ServerTableIdentifier> originalSet) {
    long currentTime = System.currentTimeMillis();
    tableRuntimeMap.values().stream()
        .filter(
            tableRuntime ->
                !isTablePending(tableRuntime)
                    || tableRuntime.isBlocked(BlockableOperation.OPTIMIZE)
                    || currentTime - tableRuntime.getLastPlanTime()
                        < tableRuntime.getOptimizingConfig().getMinPlanInterval())
        .forEach(tableRuntime -> originalSet.add(tableRuntime.getTableIdentifier()));
  }

  private boolean isTablePending(TableRuntime tableRuntime) {
    return tableRuntime.getOptimizingStatus() == OptimizingStatus.PENDING
        && (tableRuntime.getLastOptimizedSnapshotId() != tableRuntime.getCurrentSnapshotId()
            || tableRuntime.getLastOptimizedChangeSnapshotId()
                != tableRuntime.getCurrentChangeSnapshotId());
  }

  public void addTable(TableRuntime tableRuntime) {
    tableLock.lock();
    try {
      tableRuntimeMap.put(tableRuntime.getTableIdentifier(), tableRuntime);
    } finally {
      tableLock.unlock();
    }
  }

  public void removeTable(TableRuntime tableRuntime) {
    tableLock.lock();
    try {
      tableRuntimeMap.remove(tableRuntime.getTableIdentifier());
    } finally {
      tableLock.unlock();
    }
  }

  @VisibleForTesting
  Map<ServerTableIdentifier, TableRuntime> getTableRuntimeMap() {
    return tableRuntimeMap;
  }

  private static class QuotaOccupySorter implements Comparator<TableRuntime> {

    private final Map<TableRuntime, Double> tableWeightMap = Maps.newHashMap();

    @Override
    public int compare(TableRuntime one, TableRuntime another) {
      return Double.compare(
          tableWeightMap.computeIfAbsent(one, TableRuntime::calculateQuotaOccupy),
          tableWeightMap.computeIfAbsent(another, TableRuntime::calculateQuotaOccupy));
    }
  }

  private static class BalancedSorter implements Comparator<TableRuntime> {
    @Override
    public int compare(TableRuntime one, TableRuntime another) {
      return Long.compare(
          Math.max(
              one.getLastFullOptimizingTime(),
              Math.max(one.getLastMinorOptimizingTime(), one.getLastMajorOptimizingTime())),
          Math.max(
              another.getLastFullOptimizingTime(),
              Math.max(
                  another.getLastMinorOptimizingTime(), another.getLastMajorOptimizingTime())));
    }
  }
}

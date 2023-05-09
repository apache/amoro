package com.netease.arctic.server.optimizing;

import com.google.common.collect.Maps;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class SchedulingPolicy {

  private Map<ServerTableIdentifier, TableRuntime> tableRuntimeMap = new HashMap<>();
  private Comparator<TableRuntime> tableSorter = new QuotaOccupySorter();
  private Lock tableLock = new ReentrantLock();

  public List<TableRuntime> scheduleTables() {
    tableLock.lock();
    try {
      return tableRuntimeMap.values().stream()
          .filter(tableRuntime -> tableRuntime.getOptimizingStatus() == OptimizingStatus.PENDING)
          .sorted(tableSorter)
          .collect(Collectors.toList());
    } finally {
      tableLock.unlock();
    }
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

  private static class QuotaOccupySorter implements Comparator<TableRuntime> {

    private final Map<TableRuntime, Double> tableWeightMap = Maps.newHashMap();

    @Override
    public int compare(TableRuntime one, TableRuntime another) {
      return Double.compare(tableWeightMap.computeIfAbsent(one, TableRuntime::calculateQuotaOccupy),
          tableWeightMap.computeIfAbsent(another, TableRuntime::calculateQuotaOccupy));
    }
  }
}

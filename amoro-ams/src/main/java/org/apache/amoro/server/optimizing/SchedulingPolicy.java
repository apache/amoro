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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.optimizing.sorter.QuotaOccupySorter;
import org.apache.amoro.server.optimizing.sorter.SorterFactory;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SchedulingPolicy {

  public static final Logger LOG = LoggerFactory.getLogger(SchedulingPolicy.class);

  private static final String SCHEDULING_POLICY_PROPERTY_NAME = "scheduling-policy";

  private final Map<ServerTableIdentifier, TableRuntime> tableRuntimeMap = new HashMap<>();
  private volatile String policyName;
  private final Lock tableLock = new ReentrantLock();
  private static final Map<String, SorterFactory> sorterFactoryCache = new ConcurrentHashMap<>();

  public SchedulingPolicy(ResourceGroup group) {
    setTableSorterIfNeeded(group);
  }

  public void setTableSorterIfNeeded(ResourceGroup optimizerGroup) {
    tableLock.lock();
    try {
      policyName =
          Optional.ofNullable(optimizerGroup.getProperties())
              .orElseGet(Maps::newHashMap)
              .getOrDefault(SCHEDULING_POLICY_PROPERTY_NAME, QuotaOccupySorter.IDENTIFIER);
    } finally {
      tableLock.unlock();
    }
  }

  static {
    ServiceLoader<SorterFactory> sorterFactories = ServiceLoader.load(SorterFactory.class);
    Iterator<SorterFactory> iterator = sorterFactories.iterator();
    iterator.forEachRemaining(
        sorterFactory -> {
          String identifier = sorterFactory.getIdentifier();
          sorterFactoryCache.put(identifier, sorterFactory);
          LOG.info(
              "Loaded scheduling policy {} and its corresponding sorter instance {}",
              identifier,
              sorterFactory.getClass().getName());
        });
  }

  public String name() {
    return policyName;
  }

  public TableRuntime scheduleTable(Set<ServerTableIdentifier> skipSet) {
    tableLock.lock();
    try {
      fillSkipSet(skipSet);
      return tableRuntimeMap.values().stream()
          .filter(tableRuntime -> !skipSet.contains(tableRuntime.getTableIdentifier()))
          .min(createSorterByPolicy())
          .orElse(null);
    } finally {
      tableLock.unlock();
    }
  }

  private Comparator<TableRuntime> createSorterByPolicy() {
    if (sorterFactoryCache.get(policyName) != null) {
      SorterFactory sorterFactory = sorterFactoryCache.get(policyName);
      LOG.info(
          "Using sorter instance {} corresponding to the scheduling policy {}",
          sorterFactory.getClass().getName(),
          policyName);
      return sorterFactory.createComparator();
    } else {
      throw new IllegalArgumentException("Unsupported scheduling policy: " + policyName);
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
}

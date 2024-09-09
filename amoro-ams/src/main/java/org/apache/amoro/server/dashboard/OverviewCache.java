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

package org.apache.amoro.server.dashboard;

import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_COMMITTING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_EXECUTING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_IDLE_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PENDING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PLANING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_THREADS;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_HEALTH_SCORE;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_FILES;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_FILES_SIZE;

import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricSet;
import org.apache.amoro.server.dashboard.model.OverviewDataSizeItem;
import org.apache.amoro.server.dashboard.model.OverviewResourceUsageItem;
import org.apache.amoro.server.dashboard.model.OverviewTopTableItem;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OverviewCache {

  public static final String STATUS_PENDING = "Pending";
  public static final String STATUS_PLANING = "Planing";
  public static final String STATUS_EXECUTING = "Executing";
  public static final String STATUS_IDLE = "Idle";
  public static final String STATUS_COMMITTING = "Committing";

  private static final Logger log = LoggerFactory.getLogger(OverviewCache.class);
  private static volatile OverviewCache INSTANCE;

  private Map<MetricKey, Metric> registeredMetrics;
  private Map<MetricDefine, List<MetricKey>> metricDefineMap;
  private Map<String, Long> optimizingStatusCountMap = new ConcurrentHashMap<>();
  private ConcurrentLinkedDeque<OverviewResourceUsageItem> resourceUsageHistory =
      new ConcurrentLinkedDeque<>();
  private ConcurrentLinkedDeque<OverviewDataSizeItem> dataSizeHistory =
      new ConcurrentLinkedDeque<>();

  private volatile List<OverviewTopTableItem> allTopTableItem = new ArrayList<>();

  private AtomicInteger totalCatalog = new AtomicInteger();
  private AtomicLong totalDataSize = new AtomicLong();
  private AtomicInteger totalTableCount = new AtomicInteger();
  private AtomicInteger totalCpu = new AtomicInteger();
  private AtomicLong totalMemory = new AtomicLong();
  private int maxRecordCount;

  private OverviewCache() {}

  /** @return Get the singleton object. */
  public static OverviewCache getInstance() {
    if (INSTANCE == null) {
      synchronized (OverviewCache.class) {
        if (INSTANCE == null) {
          INSTANCE = new OverviewCache();
        }
      }
    }
    return INSTANCE;
  }

  public void initialize(int maxRecordCount, MetricSet globalMetricSet) {
    this.maxRecordCount = maxRecordCount;
    this.registeredMetrics = globalMetricSet.getMetrics();
  }

  public List<OverviewTopTableItem> getAllTopTableItem() {
    return ImmutableList.copyOf(allTopTableItem);
  }

  public int getTotalCatalog() {
    return totalCatalog.get();
  }

  public int getTotalTableCount() {
    return totalTableCount.get();
  }

  public long getTotalDataSize() {
    return totalDataSize.get();
  }

  public int getTotalCpu() {
    return totalCpu.get();
  }

  public long getTotalMemory() {
    return totalMemory.get();
  }

  public List<OverviewResourceUsageItem> getResourceUsageHistory(long startTime) {
    return resourceUsageHistory.stream()
        .filter(item -> item.getTs() >= startTime)
        .collect(Collectors.toList());
  }

  public List<OverviewDataSizeItem> getDataSizeHistory(long startTime) {
    return dataSizeHistory.stream()
        .filter(item -> item.getTs() >= startTime)
        .collect(Collectors.toList());
  }

  public Map<String, Long> getOptimizingStatus() {
    return optimizingStatusCountMap;
  }

  public void refresh() {
    long start = System.currentTimeMillis();
    log.info("Updating overview cache");
    try {
      this.metricDefineMap =
          registeredMetrics.keySet().stream()
              .collect(
                  Collectors.groupingBy(
                      MetricKey::getDefine,
                      Collectors.mapping(Function.identity(), Collectors.toList())));

      updateResourceUsage(start);
      updateTableDetail(start);
      updateOptimizingStatus();

    } catch (Exception e) {
      log.error("OverviewRefresher error", e);
    } finally {
      long end = System.currentTimeMillis();
      log.info("Refresher overview cache took {} ms.", end - start);
    }
  }

  private void updateResourceUsage(long ts) {
    int optimizerGroupThreadCount = (int) sumMetricValuesByDefine(OPTIMIZER_GROUP_THREADS);
    long optimizerGroupMemoryBytes =
        sumMetricValuesByDefine(OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED);

    this.totalCpu.set(optimizerGroupThreadCount);
    this.totalMemory.set(optimizerGroupMemoryBytes);
    addAndCheck(
        new OverviewResourceUsageItem(ts, optimizerGroupThreadCount, optimizerGroupMemoryBytes));
  }

  private void updateTableDetail(long ts) {
    Map<String, OverviewTopTableItem> topTableItemMap = Maps.newHashMap();
    Set<String> allCatalogs = Sets.newHashSet();
    long totalTableSize = 0L;

    // table size
    List<MetricKey> metricKeys = metricDefineMap.get(TABLE_SUMMARY_TOTAL_FILES_SIZE);
    for (MetricKey metricKey : metricKeys) {
      String tableName = tableName(metricKey);
      allCatalogs.add(catalog(metricKey));
      OverviewTopTableItem tableItem =
          topTableItemMap.computeIfAbsent(tableName, ignore -> new OverviewTopTableItem(tableName));
      long tableSize = covertValue(registeredMetrics.get(metricKey));
      tableItem.setTableSize(tableSize);
      totalTableSize += tableSize;
    }

    // file count
    metricKeys = metricDefineMap.get(TABLE_SUMMARY_TOTAL_FILES);
    for (MetricKey metricKey : metricKeys) {
      String tableName = tableName(metricKey);
      allCatalogs.add(catalog(metricKey));
      OverviewTopTableItem tableItem =
          topTableItemMap.computeIfAbsent(tableName, ignore -> new OverviewTopTableItem(tableName));
      int fileCount = (int) covertValue(registeredMetrics.get(metricKey));
      tableItem.setFileCount(fileCount);
      tableItem.setAverageFileSize(fileCount == 0 ? 0 : tableItem.getTableSize() / fileCount);
    }

    // health score
    metricKeys = metricDefineMap.get(TABLE_SUMMARY_HEALTH_SCORE);
    for (MetricKey metricKey : metricKeys) {
      String tableName = tableName(metricKey);
      allCatalogs.add(catalog(metricKey));
      OverviewTopTableItem tableItem =
          topTableItemMap.computeIfAbsent(tableName, ignore -> new OverviewTopTableItem(tableName));
      int healthScore = (int) covertValue(registeredMetrics.get(metricKey));
      tableItem.setHealthScore(healthScore);
    }

    this.totalDataSize.set(totalTableSize);
    this.totalCatalog.set(allCatalogs.size());
    this.totalTableCount.set(metricKeys.size());
    this.allTopTableItem = new ArrayList<>(topTableItemMap.values());
    addAndCheck(new OverviewDataSizeItem(ts, totalTableSize));
  }

  private void updateOptimizingStatus() {
    optimizingStatusCountMap.put(
        STATUS_PENDING, sumMetricValuesByDefine(OPTIMIZER_GROUP_PENDING_TABLES));
    optimizingStatusCountMap.put(
        STATUS_PLANING, sumMetricValuesByDefine(OPTIMIZER_GROUP_PLANING_TABLES));
    optimizingStatusCountMap.put(
        STATUS_EXECUTING, sumMetricValuesByDefine(OPTIMIZER_GROUP_EXECUTING_TABLES));
    optimizingStatusCountMap.put(STATUS_IDLE, sumMetricValuesByDefine(OPTIMIZER_GROUP_IDLE_TABLES));
    optimizingStatusCountMap.put(
        STATUS_COMMITTING, sumMetricValuesByDefine(OPTIMIZER_GROUP_COMMITTING_TABLES));
  }

  private void addAndCheck(OverviewDataSizeItem dataSizeItem) {
    dataSizeHistory.add(dataSizeItem);
    checkSize(dataSizeHistory);
  }

  private void addAndCheck(OverviewResourceUsageItem resourceUsageItem) {
    resourceUsageHistory.add(resourceUsageItem);
    checkSize(resourceUsageHistory);
  }

  private <T> void checkSize(Deque<T> deque) {
    if (deque.size() > maxRecordCount) {
      deque.poll();
    }
  }

  private String tableName(MetricKey metricKey) {
    return catalog(metricKey)
        .concat(".")
        .concat(database(metricKey))
        .concat(".")
        .concat(table(metricKey));
  }

  private String catalog(MetricKey metricKey) {
    return metricKey.valueOfTag("catalog");
  }

  private String database(MetricKey metricKey) {
    return metricKey.valueOfTag("database");
  }

  private String table(MetricKey metricKey) {
    return metricKey.valueOfTag("table");
  }

  private long sumMetricValuesByDefine(MetricDefine metricDefine) {
    List<MetricKey> metricKeys = metricDefineMap.get(metricDefine);
    if ((metricKeys == null)) {
      return 0;
    }
    return metricKeys.stream()
        .map(metricKey -> covertValue(registeredMetrics.get(metricKey)))
        .mapToLong(Long::longValue)
        .sum();
  }

  private long covertValue(Metric metric) {
    if (metric instanceof Counter) {
      return ((Counter) metric).getCount();
    } else if (metric instanceof Gauge) {
      return ((Gauge<?>) metric).getValue().longValue();
    } else {
      throw new IllegalStateException(
          "unknown metric implement class:" + metric.getClass().getName());
    }
  }
}

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

import org.apache.amoro.api.metrics.Counter;
import org.apache.amoro.api.metrics.Gauge;
import org.apache.amoro.api.metrics.Metric;
import org.apache.amoro.api.metrics.MetricDefine;
import org.apache.amoro.api.metrics.MetricKey;
import org.apache.amoro.api.metrics.MetricSet;
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

  private static final int MAX_CACHE_SIZE = 5000;

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

  private volatile List<OverviewTopTableItem> top10TablesByTableSize = new ArrayList<>();
  private volatile List<OverviewTopTableItem> top10TablesByFileCount = new ArrayList<>();
  private volatile List<OverviewTopTableItem> top10TablesByHealthScore = new ArrayList<>();

  private AtomicInteger totalCatalog = new AtomicInteger();
  private AtomicInteger totalTableCount = new AtomicInteger();
  private AtomicLong totalDataSize = new AtomicLong();
  private AtomicInteger totalCpu = new AtomicInteger();
  private AtomicLong totalMemory = new AtomicLong();

  public OverviewCache() {}

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

  public void initialize(MetricSet globalMetricSet) {
    this.registeredMetrics = globalMetricSet.getMetrics();
  }

  public List<OverviewTopTableItem> getTop10TablesByTableSize() {
    return ImmutableList.copyOf(top10TablesByTableSize);
  }

  public List<OverviewTopTableItem> getTop10TablesByFileCount() {
    return ImmutableList.copyOf(top10TablesByFileCount);
  }

  public List<OverviewTopTableItem> getTop10TablesByHealthScore() {
    return ImmutableList.copyOf(top10TablesByHealthScore);
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

  public void overviewUpdate() {
    long start = System.currentTimeMillis();
    log.info("Updating overview cache");
    try {
      this.metricDefineMap =
          registeredMetrics.keySet().stream()
              .collect(
                  Collectors.groupingBy(
                      MetricKey::getDefine,
                      Collectors.mapping(Function.identity(), Collectors.toList())));

      // resource usage
      updateResourceUsage(start);
      // optimizing status
      updateOptimizingStatus();

      // TODO:
      // data size
      // health score
      updateTableSummary(start);

    } catch (Exception e) {
      log.error("OverviewUpdater error", e);
    } finally {
      long end = System.currentTimeMillis();
      log.info("Updating overview cache took {} ms.", end - start);
    }
  }

  private void updateTableSummary(long ts) {
    Map<String, OverviewTopTableItem> topTableItemMap = Maps.newHashMap();
    Set<String> catalog = Sets.newHashSet();
    MetricDefine metricDefine = null;
    long totalTableSize = 0L;
    int totalTableCount = 0;
    // table size
    List<MetricKey> metricKeys = metricDefineMap.get(metricDefine);
    for (MetricKey metricKey : metricKeys) {
      String tableName = tableName(metricKey);
      catalog.add(metricKey.valueOfTag("catalog"));
      topTableItemMap.put(tableName, new OverviewTopTableItem());
    }
    // file count

    // health score

    this.totalDataSize.set(totalTableSize);
    this.totalTableCount.set(totalTableCount);
    addAndCheck(new OverviewDataSizeItem(ts, totalTableSize));
  }

  private String tableName(MetricKey metricKey) {
    return metricKey
        .valueOfTag("catalog")
        .concat(".")
        .concat(metricKey.valueOfTag("database"))
        .concat(".")
        .concat(metricKey.valueOfTag("table"));
  }

  private void updateResourceUsage(long ts) {
    int optimizerGroupThreadCount = (int) sumMetricValuesByDefine(OPTIMIZER_GROUP_THREADS);
    long optimizerGroupMemoryInMb =
        byte2Mb(sumMetricValuesByDefine(OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED));

    this.totalCpu.set(optimizerGroupThreadCount);
    this.totalMemory.set(optimizerGroupMemoryInMb);
    addAndCheck(
        new OverviewResourceUsageItem(ts, optimizerGroupThreadCount, optimizerGroupMemoryInMb));
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
    if (deque.size() > MAX_CACHE_SIZE) {
      deque.poll();
    }
  }

  private long byte2Mb(long bytes) {
    return bytes / 1024 / 1024;
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

  private long countMetric(MetricDefine metricDefine) {
    List<MetricKey> metricKeys = metricDefineMap.get(metricDefine);
    if ((metricKeys == null)) {
      return 0;
    }
    return metricKeys.size();
  }

  private long countMetricByTag(MetricDefine metricDefine, String tag) {
    List<MetricKey> metricKeys = metricDefineMap.get(metricDefine);
    if ((metricKeys == null)) {
      return 0;
    }
    return metricKeys.stream()
        .map(metricKey -> metricKey.valueOfTag(tag))
        .collect(Collectors.toSet())
        .size();
  }

  private long sumMetricValuesByDefine(MetricDefine metricDefine) {
    List<MetricKey> metricKeys = metricDefineMap.get(metricDefine);
    if ((metricKeys == null)) {
      return 0;
    }
    return metricKeys.stream()
        .map(metricKey -> covertValue(registeredMetrics.get(metricKey)))
        .mapToLong(Double::longValue)
        .sum();
  }

  private double covertValue(Metric metric) {
    if (metric instanceof Counter) {
      return ((Counter) metric).getCount();
    } else if (metric instanceof Gauge) {
      return ((Gauge<?>) metric).getValue().doubleValue();
    } else {
      throw new IllegalStateException(
          "unknown metric implement class:" + metric.getClass().getName());
    }
  }
}

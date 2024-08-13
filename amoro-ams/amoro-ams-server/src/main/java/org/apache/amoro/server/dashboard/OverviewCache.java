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

import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_EXECUTING_TABLES;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OverviewCache {

  public static final String STATUS_PENDING = "pending";
  public static final String STATUS_PLANING = "planing";
  public static final String STATUS_EXECUTING = "executing";

  private static final Logger log = LoggerFactory.getLogger(OverviewCache.class);

  private Map<MetricKey, Metric> registeredMetrics;
  private Map<MetricDefine, List<MetricKey>> metricDefineMap;
  private Map<String, Long> optimizingStatusCountMap = new ConcurrentHashMap<>();

  private static volatile OverviewCache INSTANCE;
  private AtomicInteger threadCount = new AtomicInteger();
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

  public int getThreadCount() {
    return threadCount.get();
  }

  public long getTotalMemory() {
    return totalMemory.get();
  }

  public void getTableFormat() {}

  public Map<String, Long> getOptimizingStatus() {
    return optimizingStatusCountMap;
  }

  public void getUnhealthTables() {}

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

      // summary
      updateSummary();
      // optimizing status
      updateOptimizingStatus();

      // TODO:
      // format
      // health score
    } catch (Exception e) {
      log.error("OverviewUpdater error", e);
    }
    long end = System.currentTimeMillis();
    log.info("Updating overview cache took {} ms.", end - start);
  }

  private void updateSummary() {
    this.threadCount.set((int) sumMetricValuesByDefine(OPTIMIZER_GROUP_THREADS));
    this.totalMemory.set(sumMetricValuesByDefine(OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED));
  }

  private void updateOptimizingStatus() {
    optimizingStatusCountMap.put(
        STATUS_PENDING, sumMetricValuesByDefine(OPTIMIZER_GROUP_PENDING_TABLES));
    optimizingStatusCountMap.put(
        STATUS_PLANING, sumMetricValuesByDefine(OPTIMIZER_GROUP_PLANING_TABLES));
    optimizingStatusCountMap.put(
        STATUS_EXECUTING, sumMetricValuesByDefine(OPTIMIZER_GROUP_EXECUTING_TABLES));
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

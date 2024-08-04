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

package org.apache.amoro.metrics.overview;

import static org.apache.amoro.api.metrics.MetricDefine.defineGauge;

import io.javalin.http.Context;
import org.apache.amoro.api.metrics.Counter;
import org.apache.amoro.api.metrics.Gauge;
import org.apache.amoro.api.metrics.Metric;
import org.apache.amoro.api.metrics.MetricDefine;
import org.apache.amoro.api.metrics.MetricKey;
import org.apache.amoro.api.metrics.MetricSet;
import org.apache.amoro.metrics.overview.model.OverviewSummary;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OverviewCache {

  public static final String STATUS_PENDING = "pending";
  public static final String STATUS_PLANING = "planing";
  public static final String STATUS_EXECUTING = "executing";

  public static final MetricDefine OPTIMIZER_GROUP_PLANING_TABLES =
      defineGauge("optimizer_group_planing_tables")
          .withDescription("Number of planing tables in optimizer group")
          .withTags("group")
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_PENDING_TABLES =
      defineGauge("optimizer_group_pending_tables")
          .withDescription("Number of pending tables in optimizer group")
          .withTags("group")
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_EXECUTING_TABLES =
      defineGauge("optimizer_group_executing_tables")
          .withDescription("Number of executing tables in optimizer group")
          .withTags("group")
          .build();

  public static final MetricDefine AMS_JVM_MEMORY_HEAP_USED =
      defineGauge("ams_jvm_memory_heap_used")
          .withDescription("The amount of heap memory currently used (in bytes) by the AMS")
          .build();

  public static final MetricDefine AMS_JVM_THREADS_COUNT =
      defineGauge("ams_jvm_threads_count")
          .withDescription("The total number of live threads used by the AMS")
          .build();
  private static final Logger log = LoggerFactory.getLogger(OverviewCache.class);

  private Map<MetricKey, Metric> registeredMetrics;
  private Map<MetricDefine, List<MetricKey>> metricDefineMap;
  private Map<String, Long> optimizingStatusCountMap;
  private OverviewSummary overviewSummary;

  public OverviewCache() {
    optimizingStatusCountMap = Maps.newConcurrentMap();
    overviewSummary = new OverviewSummary();
  }

  public void setMetricSet(MetricSet globalMetricSet) {
    this.registeredMetrics = globalMetricSet.getMetrics();
  }

  public void getSummary(Context ctx) {
    synchronized (overviewSummary) {
      ctx.json(overviewSummary);
    }
  }

  public void getTableFormat(Context ctx) {
    ctx.json(Maps.newHashMap());
  }

  public void getOptimizingStatus(Context ctx) {
    ctx.json(optimizingStatusCountMap.entrySet().stream().collect(Collectors.toList()));
  }

  public void getUnhealthTables(Context ctx) {
    ctx.json(Maps.newHashMap());
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

      // TODO cache overview page data
      // summary
      updateSummary();
      // format
      // optimizing status
      updateOptimizingStatus();

      // health score}
    } catch (Exception e) {
      log.error("OverviewUpdater error", e);
    }
    long end = System.currentTimeMillis();
    log.info("Updating overview cache took {} ms.", end - start);
  }

  private void updateSummary() {
    synchronized (overviewSummary) {
      overviewSummary.setTotalCpu((int) sumMetricValuesByDefine(AMS_JVM_THREADS_COUNT));
      overviewSummary.setTotalMemory(sumMetricValuesByDefine(AMS_JVM_MEMORY_HEAP_USED));
    }
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

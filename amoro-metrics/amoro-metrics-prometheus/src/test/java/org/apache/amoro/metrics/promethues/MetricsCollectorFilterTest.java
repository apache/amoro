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

package org.apache.amoro.metrics.promethues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.prometheus.client.Collector;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricSet;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricsCollectorFilterTest {

  private MetricSet createMetricSet(Map<MetricKey, Metric> metrics) {
    return () -> Collections.unmodifiableMap(metrics);
  }

  private MetricKey registerMetric(
      Map<MetricKey, Metric> metrics, String name, Metric metric, String... tags) {
    MetricDefine.Builder builder;
    if (metric instanceof Counter) {
      builder = MetricDefine.defineCounter(name);
    } else {
      builder = MetricDefine.defineGauge(name);
    }
    if (tags.length > 0) {
      builder.withTags(tags);
    }
    MetricDefine define = builder.build();

    Map<String, String> tagValues = new HashMap<>();
    for (String tag : tags) {
      tagValues.put(tag, "test_value");
    }
    MetricKey key = new MetricKey(define, tagValues);
    metrics.put(key, metric);
    return key;
  }

  @Test
  public void testCollectWithNoFilter() {
    Map<MetricKey, Metric> metrics = new HashMap<>();
    registerMetric(metrics, "table_optimizing_status_in_idle", (Gauge<Long>) () -> 1L);
    registerMetric(metrics, "optimizer_group_pending_tasks", (Gauge<Long>) () -> 5L);
    registerMetric(metrics, "ams_jvm_cpu_load", (Gauge<Double>) () -> 0.5);

    MetricsCollector collector = new MetricsCollector(createMetricSet(metrics));
    List<Collector.MetricFamilySamples> result = collector.collect();

    assertEquals(3, result.size());
  }

  @Test
  public void testCollectWithDisabledCategory() {
    Map<MetricKey, Metric> metrics = new HashMap<>();
    registerMetric(metrics, "table_optimizing_status_in_idle", (Gauge<Long>) () -> 1L);
    registerMetric(metrics, "optimizer_group_pending_tasks", (Gauge<Long>) () -> 5L);
    registerMetric(metrics, "ams_jvm_cpu_load", (Gauge<Double>) () -> 0.5);

    Set<MetricCategory> disabled = EnumSet.of(MetricCategory.SELF_OPTIMIZING);
    MetricsCollector collector = new MetricsCollector(createMetricSet(metrics), disabled);
    List<Collector.MetricFamilySamples> result = collector.collect();

    assertEquals(2, result.size());
    Set<String> names = result.stream().map(s -> s.name).collect(Collectors.toSet());
    assertFalse(names.contains("amoro_table_optimizing_status_in_idle"));
    assertTrue(names.contains("amoro_optimizer_group_pending_tasks"));
    assertTrue(names.contains("amoro_ams_jvm_cpu_load"));
  }

  @Test
  public void testCollectWithMultipleDisabledCategories() {
    Map<MetricKey, Metric> metrics = new HashMap<>();
    registerMetric(metrics, "table_optimizing_status_in_idle", (Gauge<Long>) () -> 1L);
    registerMetric(metrics, "optimizer_group_pending_tasks", (Gauge<Long>) () -> 5L);
    registerMetric(metrics, "ams_jvm_cpu_load", (Gauge<Double>) () -> 0.5);
    registerMetric(metrics, "table_summary_total_files", (Gauge<Long>) () -> 100L);

    Set<MetricCategory> disabled =
        EnumSet.of(MetricCategory.SELF_OPTIMIZING, MetricCategory.TABLE_SUMMARY);
    MetricsCollector collector = new MetricsCollector(createMetricSet(metrics), disabled);
    List<Collector.MetricFamilySamples> result = collector.collect();

    assertEquals(2, result.size());
    Set<String> names = result.stream().map(s -> s.name).collect(Collectors.toSet());
    assertTrue(names.contains("amoro_optimizer_group_pending_tasks"));
    assertTrue(names.contains("amoro_ams_jvm_cpu_load"));
  }

  @Test
  public void testUnknownMetricNotFiltered() {
    Map<MetricKey, Metric> metrics = new HashMap<>();
    registerMetric(metrics, "custom_unknown_metric", (Gauge<Long>) () -> 42L);

    Set<MetricCategory> disabled =
        EnumSet.of(
            MetricCategory.SELF_OPTIMIZING,
            MetricCategory.OPTIMIZER_GROUP,
            MetricCategory.ORPHAN_FILES,
            MetricCategory.AMS_JVM,
            MetricCategory.TABLE_SUMMARY);
    MetricsCollector collector = new MetricsCollector(createMetricSet(metrics), disabled);
    List<Collector.MetricFamilySamples> result = collector.collect();

    assertEquals(1, result.size());
    assertEquals("amoro_custom_unknown_metric", result.get(0).name);
  }

  @Test
  public void testOrphanFilesMultiplePrefixes() {
    Map<MetricKey, Metric> metrics = new HashMap<>();
    Counter counter1 = new Counter();
    Counter counter2 = new Counter();
    registerMetric(metrics, "table_orphan_content_file_cleaning_count", counter1);
    registerMetric(metrics, "table_expected_orphan_metadata_file_cleaning_count", counter2);
    registerMetric(metrics, "ams_jvm_cpu_load", (Gauge<Double>) () -> 0.5);

    Set<MetricCategory> disabled = EnumSet.of(MetricCategory.ORPHAN_FILES);
    MetricsCollector collector = new MetricsCollector(createMetricSet(metrics), disabled);
    List<Collector.MetricFamilySamples> result = collector.collect();

    assertEquals(1, result.size());
    assertEquals("amoro_ams_jvm_cpu_load", result.get(0).name);
  }
}

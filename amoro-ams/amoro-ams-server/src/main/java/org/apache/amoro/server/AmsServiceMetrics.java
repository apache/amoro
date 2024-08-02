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

package org.apache.amoro.server;

import static org.apache.amoro.api.metrics.MetricDefine.defineGauge;

import org.apache.amoro.api.metrics.Metric;
import org.apache.amoro.api.metrics.MetricDefine;
import org.apache.amoro.api.metrics.MetricKey;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AmsServiceMetrics {
  public static final String GARBAGE_COLLECTOR_TAG = "garbage_collector";
  public static final MetricDefine AMS_JVM_CPU_LOAD =
      defineGauge("ams_jvm_cpu_load").withDescription("The recent CPU usage of the AMS").build();
  public static final MetricDefine AMS_JVM_CPU_TIME =
      defineGauge("ams_jvm_cpu_time").withDescription("The CPU time used by the AMS").build();

  public static final MetricDefine AMS_JVM_MEMORY_HEAP_USED =
      defineGauge("ams_jvm_memory_heap_used")
          .withDescription("The amount of heap memory currently used (in bytes) by the AMS")
          .build();

  public static final MetricDefine AMS_JVM_MEMORY_HEAP_COMMITTED =
      defineGauge("ams_jvm_memory_heap_committed")
          .withDescription(
              "The amount of memory in the heap that is committed for the JVM to use (in bytes)")
          .build();

  public static final MetricDefine AMS_JVM_MEMORY_HEAP_MAX =
      defineGauge("ams_jvm_memory_heap_max")
          .withDescription(
              "The maximum amount of memory in the heap (in bytes), It's equal to the value specified through -Xmx")
          .build();

  public static final MetricDefine AMS_JVM_THREADS_COUNT =
      defineGauge("ams_jvm_threads_count")
          .withDescription("The total number of live threads used by the AMS")
          .build();

  public static final MetricDefine AMS_JVM_GARBAGE_COLLECTOR_COUNT =
      defineGauge("ams_jvm_garbage_collector_count")
          .withDescription("The count of the JVM's Garbage Collector")
          .withTags(GARBAGE_COLLECTOR_TAG)
          .build();

  public static final MetricDefine AMS_JVM_GARBAGE_COLLECTOR_TIME =
      defineGauge("ams_jvm_garbage_collector_time")
          .withDescription("The time of the JVM's Garbage Collector")
          .withTags(GARBAGE_COLLECTOR_TAG)
          .build();

  private final MetricRegistry registry;
  private List<MetricKey> registeredMetricKeys = Lists.newArrayList();

  public AmsServiceMetrics(MetricRegistry registry) {
    this.registry = registry;
  }

  public void register() {
    registerHeapMetric();
    registerThreadMetric();
    registerCPuMetric();
    registerGarbageCollectorMetrics();
  }

  public void unregister() {
    registeredMetricKeys.forEach(registry::unregister);
    registeredMetricKeys.clear();
  }

  private void registerHeapMetric() {
    MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    registerMetric(
        registry,
        AMS_JVM_MEMORY_HEAP_USED,
        (org.apache.amoro.api.metrics.Gauge<Long>) () -> heapMemoryUsage.getUsed());

    registerMetric(
        registry,
        AMS_JVM_MEMORY_HEAP_COMMITTED,
        (org.apache.amoro.api.metrics.Gauge<Long>) () -> heapMemoryUsage.getCommitted());

    registerMetric(
        registry,
        AMS_JVM_MEMORY_HEAP_MAX,
        (org.apache.amoro.api.metrics.Gauge<Long>) () -> heapMemoryUsage.getMax());
  }

  private void registerThreadMetric() {
    registerMetric(
        registry,
        AMS_JVM_THREADS_COUNT,
        (org.apache.amoro.api.metrics.Gauge<Integer>)
            () -> ManagementFactory.getThreadMXBean().getThreadCount());
  }

  private void registerCPuMetric() {
    final com.sun.management.OperatingSystemMXBean mxBean =
        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    registerMetric(
        registry,
        AMS_JVM_CPU_LOAD,
        (org.apache.amoro.api.metrics.Gauge<Double>) () -> mxBean.getProcessCpuLoad());
    registerMetric(
        registry,
        AMS_JVM_CPU_TIME,
        (org.apache.amoro.api.metrics.Gauge<Long>) () -> mxBean.getProcessCpuTime());
  }

  private void registerGarbageCollectorMetrics() {
    List<GarbageCollectorMXBean> garbageCollectorMXBeans =
        ManagementFactory.getGarbageCollectorMXBeans();

    for (final GarbageCollectorMXBean garbageCollector : garbageCollectorMXBeans) {
      registerMetric(
          registry,
          AMS_JVM_GARBAGE_COLLECTOR_COUNT,
          ImmutableMap.of(GARBAGE_COLLECTOR_TAG, garbageCollector.getName()),
          (org.apache.amoro.api.metrics.Gauge<Long>) () -> garbageCollector.getCollectionCount());
      registerMetric(
          registry,
          AMS_JVM_GARBAGE_COLLECTOR_TIME,
          ImmutableMap.of(GARBAGE_COLLECTOR_TAG, garbageCollector.getName()),
          (org.apache.amoro.api.metrics.Gauge<Long>) () -> garbageCollector.getCollectionTime());
    }
  }

  private void registerMetric(MetricRegistry registry, MetricDefine define, Metric metric) {
    MetricKey key = registry.register(define, Collections.emptyMap(), metric);
    registeredMetricKeys.add(key);
  }

  private void registerMetric(
      MetricRegistry registry, MetricDefine define, Map<String, String> tags, Metric metric) {
    MetricKey key = registry.register(define, tags, metric);
    registeredMetricKeys.add(key);
  }
}

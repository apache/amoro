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

import static org.apache.amoro.metrics.MetricDefine.defineGauge;

import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
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

  public static final String AMS_ACTIVE_TAG = "active";
  public static final MetricDefine AMS_HA_STATE =
      defineGauge("ams_ha_state")
          .withDescription("The HA state of the AMS")
          .withTags(AMS_ACTIVE_TAG)
          .build();

  private final MetricRegistry registry;
  private List<MetricKey> registeredMetricKeys = Lists.newArrayList();

  private AmoroServiceContainer ams;

  public AmsServiceMetrics(MetricRegistry registry, AmoroServiceContainer ams) {
    this.registry = registry;
    this.ams = ams;
  }

  public void register() {
    registerHeapMetric();
    registerThreadMetric();
    registerCPuMetric();
    registerGarbageCollectorMetrics();
    registerHAStateMetrics();
  }

  public void unregister() {
    registeredMetricKeys.forEach(registry::unregister);
    registeredMetricKeys.clear();
  }

  private void registerHeapMetric() {
    MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    registerMetric(
        registry, AMS_JVM_MEMORY_HEAP_USED, (Gauge<Long>) () -> heapMemoryUsage.getUsed());

    registerMetric(
        registry,
        AMS_JVM_MEMORY_HEAP_COMMITTED,
        (Gauge<Long>) () -> heapMemoryUsage.getCommitted());

    registerMetric(registry, AMS_JVM_MEMORY_HEAP_MAX, (Gauge<Long>) () -> heapMemoryUsage.getMax());
  }

  private void registerThreadMetric() {
    registerMetric(
        registry,
        AMS_JVM_THREADS_COUNT,
        (Gauge<Integer>) () -> ManagementFactory.getThreadMXBean().getThreadCount());
  }

  private void registerCPuMetric() {
    final com.sun.management.OperatingSystemMXBean mxBean =
        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    registerMetric(registry, AMS_JVM_CPU_LOAD, (Gauge<Double>) () -> mxBean.getProcessCpuLoad());
    registerMetric(registry, AMS_JVM_CPU_TIME, (Gauge<Long>) () -> mxBean.getProcessCpuTime());
  }

  private void registerGarbageCollectorMetrics() {
    List<GarbageCollectorMXBean> garbageCollectorMXBeans =
        ManagementFactory.getGarbageCollectorMXBeans();

    for (final GarbageCollectorMXBean garbageCollector : garbageCollectorMXBeans) {
      registerMetric(
          registry,
          AMS_JVM_GARBAGE_COLLECTOR_COUNT,
          ImmutableMap.of(GARBAGE_COLLECTOR_TAG, garbageCollector.getName()),
          (Gauge<Long>) () -> garbageCollector.getCollectionCount());
      registerMetric(
          registry,
          AMS_JVM_GARBAGE_COLLECTOR_TIME,
          ImmutableMap.of(GARBAGE_COLLECTOR_TAG, garbageCollector.getName()),
          (Gauge<Long>) () -> garbageCollector.getCollectionTime());
    }
  }

  private void registerHAStateMetrics() {
    String host = ams.getServiceConfig().get(AmoroManagementConf.SERVER_EXPOSE_HOST);
    registerMetric(
        registry,
        AMS_HA_STATE,
        ImmutableMap.of(AMS_ACTIVE_TAG, host),
        (Gauge<Integer>) () -> ams.getHaState().getCode());
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

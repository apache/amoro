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

import static org.apache.amoro.server.AmsServiceMetrics.AMS_JVM_CPU_LOAD;
import static org.apache.amoro.server.AmsServiceMetrics.AMS_JVM_CPU_TIME;
import static org.apache.amoro.server.AmsServiceMetrics.AMS_JVM_GARBAGE_COLLECTOR_COUNT;
import static org.apache.amoro.server.AmsServiceMetrics.AMS_JVM_GARBAGE_COLLECTOR_TIME;
import static org.apache.amoro.server.AmsServiceMetrics.AMS_JVM_MEMORY_HEAP_COMMITTED;
import static org.apache.amoro.server.AmsServiceMetrics.AMS_JVM_MEMORY_HEAP_MAX;
import static org.apache.amoro.server.AmsServiceMetrics.AMS_JVM_MEMORY_HEAP_USED;
import static org.apache.amoro.server.AmsServiceMetrics.AMS_JVM_THREADS_COUNT;

import org.apache.amoro.api.metrics.Gauge;
import org.apache.amoro.api.metrics.Metric;
import org.apache.amoro.api.metrics.MetricKey;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

public class TestAmsServiceMetrics {
  private static final AmsEnvironment amsEnv = AmsEnvironment.getIntegrationInstances();

  @BeforeAll
  public static void before() throws Exception {
    amsEnv.start();
  }

  @Test
  public void testAmsServiceMetrics() throws Exception {
    MetricRegistry registry = MetricManager.getInstance().getGlobalRegistry();
    Gauge<Long> heapMx =
        (Gauge<Long>)
            registry
                .getMetrics()
                .get(new MetricKey(AMS_JVM_MEMORY_HEAP_MAX, Collections.emptyMap()));
    Assert.assertTrue(heapMx.getValue().longValue() > 0);
    registry.unregister(new MetricKey(AMS_JVM_MEMORY_HEAP_MAX, Collections.emptyMap()));

    Gauge<Long> heapUsed =
        (Gauge<Long>)
            registry
                .getMetrics()
                .get(new MetricKey(AMS_JVM_MEMORY_HEAP_USED, Collections.emptyMap()));
    Assert.assertTrue(heapUsed.getValue().longValue() > 0);
    registry.unregister(new MetricKey(AMS_JVM_MEMORY_HEAP_USED, Collections.emptyMap()));

    Gauge<Long> heapCommitted =
        (Gauge<Long>)
            registry
                .getMetrics()
                .get(new MetricKey(AMS_JVM_MEMORY_HEAP_COMMITTED, Collections.emptyMap()));
    Assert.assertTrue(heapCommitted.getValue().longValue() > 0);
    registry.unregister(new MetricKey(AMS_JVM_MEMORY_HEAP_COMMITTED, Collections.emptyMap()));

    Gauge<Integer> threadsCount =
        (Gauge<Integer>)
            registry.getMetrics().get(new MetricKey(AMS_JVM_THREADS_COUNT, Collections.emptyMap()));
    Assert.assertTrue(threadsCount.getValue().intValue() > 0);
    registry.unregister(new MetricKey(AMS_JVM_THREADS_COUNT, Collections.emptyMap()));

    Gauge<Double> cpuLoad =
        (Gauge<Double>)
            registry.getMetrics().get(new MetricKey(AMS_JVM_CPU_LOAD, Collections.emptyMap()));
    Assert.assertNotNull(cpuLoad);
    registry.unregister(new MetricKey(AMS_JVM_CPU_LOAD, Collections.emptyMap()));

    Gauge<Long> cpuTime =
        (Gauge<Long>)
            registry.getMetrics().get(new MetricKey(AMS_JVM_CPU_TIME, Collections.emptyMap()));
    Assert.assertTrue(cpuTime.getValue().longValue() > 0);
    registry.unregister(new MetricKey(AMS_JVM_CPU_TIME, Collections.emptyMap()));

    Map<MetricKey, Metric> metrics = registry.getMetrics();
    Assert.assertTrue(
        metrics.keySet().stream()
            .anyMatch(
                key ->
                    key.getDefine().getName().equals(AMS_JVM_GARBAGE_COLLECTOR_COUNT.getName())));

    Assert.assertTrue(
        metrics.keySet().stream()
            .anyMatch(
                key -> key.getDefine().getName().equals(AMS_JVM_GARBAGE_COLLECTOR_TIME.getName())));

    amsEnv.stop();
  }
}

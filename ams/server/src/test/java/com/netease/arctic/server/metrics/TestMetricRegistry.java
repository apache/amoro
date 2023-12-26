/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.netease.arctic.ams.api.metrics.Counter;
import com.netease.arctic.ams.api.metrics.Gauge;
import com.netease.arctic.ams.api.metrics.Metric;
import com.netease.arctic.ams.api.metrics.MetricDefine;
import com.netease.arctic.ams.api.metrics.MetricKey;
import com.netease.arctic.ams.api.metrics.MetricRegisterListener;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class TestMetricRegistry {
  private MetricRegistry metricRegistry;

  @Mock private MetricRegisterListener mockListener;

  @BeforeEach
  public void setUp() {
    metricRegistry = new MetricRegistry();
    metricRegistry.addListener(mockListener);
  }

  @Test
  public void testRegisterMetricSuccessfully() {
    MetricDefine define = MetricDefine.defineCounter("test_metric").withTags("tag1").build();

    Map<String, String> tags = Maps.newHashMap();
    tags.put("tag1", "value1");

    Metric counter = new Counter();

    MetricKey key = metricRegistry.register(define, tags, counter);

    // Verify the metric is registered
    assertNotNull(key);
    assertEquals(counter, metricRegistry.getMetrics().get(key));
    assertEquals("test_metric", key.getDefine().getName());
    assertEquals("value1", key.valueOfTag("tag1"));
    // Verify the listener is called
    verify(mockListener, times(1)).onMetricRegistered(eq(key), eq(counter));

    // test different tags values could be registered
    MetricDefine sameDefine = MetricDefine.defineCounter("test_metric").withTags("tag1").build();
    MetricKey key2 =
        metricRegistry.register(sameDefine, ImmutableMap.of("tag1", "value2"), new Counter());
    assertEquals(2, metricRegistry.getMetrics().size());

    // unregister metric
    metricRegistry.unregister(key);
    metricRegistry.unregister(key2);
    verify(mockListener, times(1)).onMetricUnregistered(eq(key));
    assertEquals(0, metricRegistry.getMetrics().size());
    assertEquals(0, metricRegistry.metricDefineCount(define.getName()));
  }

  @Test
  public void testRegisterMetricFailed() {
    MetricDefine define = MetricDefine.defineCounter("test_metric").withTags("tag1").build();

    Map<String, String> tags = Maps.newHashMap();
    tags.put("tag1", "value1");

    Metric counter = new Counter();
    metricRegistry.register(define, tags, counter);

    // test duplicate define could not be registered
    MetricDefine duplicateDefine = MetricDefine.defineGauge("test_metric").withTags("tag1").build();
    Metric metric = (Gauge<Long>) () -> 0L;
    assertThrows(
        IllegalArgumentException.class,
        () -> metricRegistry.register(duplicateDefine, tags, metric));

    MetricDefine duplicateTagDefine =
        MetricDefine.defineCounter("test_metric").withTags("tag3").build();
    Map<String, String> duplicateTags = ImmutableMap.of("tag3", "val3");
    assertThrows(
        IllegalArgumentException.class,
        () -> metricRegistry.register(duplicateTagDefine, duplicateTags, counter));

    // test same tags value could not be registered
    assertThrows(
        IllegalArgumentException.class,
        () -> metricRegistry.register(define, ImmutableMap.of("tag1", "value1"), counter));
  }

  @Test
  public void testRegisterMetricWithInvalidType() {
    MetricDefine define = MetricDefine.defineCounter("test_metric").withTags("tag1").build();

    // Assuming the mockMetric is a GAUGE
    Metric metric = (Gauge<Long>) () -> 0L;

    Map<String, String> tags = ImmutableMap.of("tag1", "value1");

    assertThrows(
        IllegalArgumentException.class, () -> metricRegistry.register(define, tags, metric));
  }
}

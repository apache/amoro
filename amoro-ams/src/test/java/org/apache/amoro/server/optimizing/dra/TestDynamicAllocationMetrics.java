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

package org.apache.amoro.server.optimizing.dra;

import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DynamicAllocationMetrics}: gauge values are read through the injected {@link
 * DynamicAllocationMetrics.Source}, counters accumulate through the inc hooks, and unregister
 * removes every metric of the group.
 */
public class TestDynamicAllocationMetrics {

  private static class FakeSource implements DynamicAllocationMetrics.Source {
    int pendingRemovalOptimizers;
    int effectiveThreads;
    long backlogDurationMs;

    @Override
    public int pendingRemovalOptimizers() {
      return pendingRemovalOptimizers;
    }

    @Override
    public int effectiveThreads() {
      return effectiveThreads;
    }

    @Override
    public long backlogDurationMs() {
      return backlogDurationMs;
    }
  }

  private final MetricRegistry registry = new MetricRegistry();
  private final FakeSource source = new FakeSource();
  private final DynamicAllocationMetrics metrics =
      new DynamicAllocationMetrics("group1", registry, source);

  private Metric metric(MetricDefine define) {
    return registry.getMetrics().get(new MetricKey(define, ImmutableMap.of("group", "group1")));
  }

  @Test
  void gaugesReflectSourceValues() {
    metrics.register();
    source.pendingRemovalOptimizers = 2;
    source.effectiveThreads = 7;
    source.backlogDurationMs = 45_000L;

    Gauge<?> pendingRemoval =
        (Gauge<?>) metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS);
    Gauge<?> effectiveThreads =
        (Gauge<?>) metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_EFFECTIVE_THREADS);
    Gauge<?> backlogDuration =
        (Gauge<?>) metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_BACKLOG_DURATION_MS);
    Assertions.assertEquals(2, ((Number) pendingRemoval.getValue()).intValue());
    Assertions.assertEquals(7, ((Number) effectiveThreads.getValue()).intValue());
    Assertions.assertEquals(45_000L, ((Number) backlogDuration.getValue()).longValue());
  }

  @Test
  void countersStartAtZeroAndAccumulate() {
    metrics.register();
    Counter scaleUp = (Counter) metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL);
    Counter scaleDown = (Counter) metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_DOWN_TOTAL);
    Assertions.assertEquals(0, scaleUp.getCount());
    Assertions.assertEquals(0, scaleDown.getCount());

    metrics.incScaleUp();
    metrics.incScaleUp();
    metrics.incScaleDown();
    Assertions.assertEquals(2, scaleUp.getCount());
    Assertions.assertEquals(1, scaleDown.getCount());
  }

  @Test
  void registerRollsBackPartialRegistrationOnFailure() {
    // Occupy one of the group's keys so register() fails midway through its five metrics.
    MetricKey conflict =
        registry.register(
            DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL,
            ImmutableMap.of("group", "group1"),
            new Counter());
    Assertions.assertThrows(RuntimeException.class, metrics::register);
    Assertions.assertNull(
        metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS),
        "a failed register() must not leave partially registered metrics behind");
    Assertions.assertNull(metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_EFFECTIVE_THREADS));
    Assertions.assertNull(metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_BACKLOG_DURATION_MS));

    registry.unregister(conflict);
    metrics.register();
    Assertions.assertNotNull(metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL));
    Assertions.assertNotNull(
        metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS));
  }

  @Test
  void unregisterRemovesAllGroupMetrics() {
    metrics.register();
    metrics.unregister();
    Assertions.assertNull(
        metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS));
    Assertions.assertNull(metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_EFFECTIVE_THREADS));
    Assertions.assertNull(metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_BACKLOG_DURATION_MS));
    Assertions.assertNull(metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_UP_TOTAL));
    Assertions.assertNull(metric(DynamicAllocationMetrics.OPTIMIZER_GROUP_SCALE_DOWN_TOTAL));
  }
}

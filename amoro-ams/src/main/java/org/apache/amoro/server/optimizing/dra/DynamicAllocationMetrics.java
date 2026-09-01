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

import static org.apache.amoro.metrics.MetricDefine.defineCounter;
import static org.apache.amoro.metrics.MetricDefine.defineGauge;

import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;

import java.util.List;

/**
 * Per-group metrics of dynamic allocation (AIP-5). Their sources — the pending-removal set, the
 * boot-window accounting and the backlog timer — are owned by the scale keeper, so unlike the
 * queue-scoped {@code OptimizerGroupMetrics} these are registered when the keeper starts watching a
 * group and unregistered when it stops; gauge reads go through the injected {@link Source}.
 */
public class DynamicAllocationMetrics {

  static final String GROUP_TAG = "group";

  public static final MetricDefine OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS =
      defineGauge("optimizer_group_pending_removal_optimizers")
          .withDescription("Number of optimizer instances in graceful drain in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_EFFECTIVE_THREADS =
      defineGauge("optimizer_group_effective_threads")
          .withDescription(
              "Number of registered threads plus threads of optimizers pending registration "
                  + "in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_BACKLOG_DURATION_MS =
      defineGauge("optimizer_group_backlog_duration_ms")
          .withDescription(
              "Duration in milliseconds since demand first exceeded capacity in optimizer "
                  + "group, 0 while there is no backlog")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_SCALE_UP_TOTAL =
      defineCounter("optimizer_group_scale_up_total")
          .withDescription(
              "Cumulative count of attempted scale-up actions in optimizer group, one per "
                  + "scale-out round regardless of instance count or request outcome")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_SCALE_DOWN_TOTAL =
      defineCounter("optimizer_group_scale_down_total")
          .withDescription(
              "Cumulative count of scale-down actions in optimizer group, one per drain start")
          .withTags(GROUP_TAG)
          .build();

  /** Read side of the gauges, implemented by the owner of the scaling state. */
  public interface Source {

    /** Number of this group's optimizer instances currently in graceful drain. */
    int pendingRemovalOptimizers();

    /** Registered threads plus threads of optimizers pending registration. */
    int effectiveThreads();

    /** Duration since demand first exceeded capacity, {@code 0} while there is no backlog. */
    long backlogDurationMs();
  }

  private final String groupName;
  private final MetricRegistry registry;
  private final Source source;
  private final Counter scaleUpTotal = new Counter();
  private final Counter scaleDownTotal = new Counter();
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();

  public DynamicAllocationMetrics(String groupName, MetricRegistry registry, Source source) {
    this.groupName = groupName;
    this.registry = registry;
    this.source = source;
  }

  public void register() {
    try {
      registerMetric(
          OPTIMIZER_GROUP_PENDING_REMOVAL_OPTIMIZERS,
          (Gauge<Integer>) source::pendingRemovalOptimizers);
      registerMetric(OPTIMIZER_GROUP_EFFECTIVE_THREADS, (Gauge<Integer>) source::effectiveThreads);
      registerMetric(OPTIMIZER_GROUP_BACKLOG_DURATION_MS, (Gauge<Long>) source::backlogDurationMs);
      registerMetric(OPTIMIZER_GROUP_SCALE_UP_TOTAL, scaleUpTotal);
      registerMetric(OPTIMIZER_GROUP_SCALE_DOWN_TOTAL, scaleDownTotal);
    } catch (Exception e) {
      // Roll back any metrics that were partially registered before the failure so that a retry
      // finds a clean state.
      unregister();
      throw e;
    }
  }

  public void unregister() {
    registeredMetricKeys.forEach(registry::unregister);
    registeredMetricKeys.clear();
  }

  public void incScaleUp() {
    scaleUpTotal.inc();
  }

  public void incScaleDown() {
    scaleDownTotal.inc();
  }

  private void registerMetric(MetricDefine define, Metric metric) {
    registeredMetricKeys.add(
        registry.register(define, ImmutableMap.of(GROUP_TAG, groupName), metric));
  }
}

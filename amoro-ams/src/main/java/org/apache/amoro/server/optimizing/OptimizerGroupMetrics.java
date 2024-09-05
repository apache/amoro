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

package org.apache.amoro.server.optimizing;

import static org.apache.amoro.metrics.MetricDefine.defineGauge;
import static org.apache.amoro.server.optimizing.OptimizingStatus.COMMITTING;
import static org.apache.amoro.server.optimizing.OptimizingStatus.IDLE;
import static org.apache.amoro.server.optimizing.OptimizingStatus.PENDING;
import static org.apache.amoro.server.optimizing.OptimizingStatus.PLANNING;
import static org.apache.amoro.server.optimizing.TaskRuntime.Status.ACKED;
import static org.apache.amoro.server.optimizing.TaskRuntime.Status.PLANNED;
import static org.apache.amoro.server.optimizing.TaskRuntime.Status.SCHEDULED;

import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Metrics manager for an optimizer group. */
public class OptimizerGroupMetrics {

  static final String GROUP_TAG = "group";
  public static final MetricDefine OPTIMIZER_GROUP_PENDING_TASKS =
      defineGauge("optimizer_group_pending_tasks")
          .withDescription("Number of pending tasks in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_EXECUTING_TASKS =
      defineGauge("optimizer_group_executing_tasks")
          .withDescription("Number of executing tasks in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_PLANING_TABLES =
      defineGauge("optimizer_group_planing_tables")
          .withDescription("Number of planing tables in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_PENDING_TABLES =
      defineGauge("optimizer_group_pending_tables")
          .withDescription("Number of pending tables in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_EXECUTING_TABLES =
      defineGauge("optimizer_group_executing_tables")
          .withDescription("Number of executing tables in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_IDLE_TABLES =
      defineGauge("optimizer_group_idle_tables")
          .withDescription("Number of idle tables in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_COMMITTING_TABLES =
      defineGauge("optimizer_group_committing_tables")
          .withDescription("Number of committing tables in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_OPTIMIZER_INSTANCES =
      defineGauge("optimizer_group_optimizer_instances")
          .withDescription("Number of optimizer instances in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED =
      defineGauge("optimizer_group_memory_bytes_allocated")
          .withDescription("Memory bytes allocated in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_THREADS =
      defineGauge("optimizer_group_threads")
          .withDescription("Number of total threads in optimizer group")
          .withTags(GROUP_TAG)
          .build();

  private final String groupName;
  private final MetricRegistry registry;
  private final OptimizingQueue optimizingQueue;
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();
  private final Map<String, OptimizerInstance> optimizerInstances = new ConcurrentHashMap<>();

  public OptimizerGroupMetrics(
      String groupName, MetricRegistry registry, OptimizingQueue optimizingQueue) {
    this.groupName = groupName;
    this.registry = registry;
    this.optimizingQueue = optimizingQueue;
  }

  private void registerMetric(MetricRegistry registry, MetricDefine define, Metric metric) {
    MetricKey key = registry.register(define, ImmutableMap.of(GROUP_TAG, groupName), metric);
    registeredMetricKeys.add(key);
  }

  public void register() {
    registerMetric(
        registry,
        OPTIMIZER_GROUP_PENDING_TASKS,
        (Gauge<Integer>)
            () ->
                optimizingQueue
                    .collectTasks(
                        task ->
                            task.getStatus().equals(PLANNED) || task.getStatus().equals(SCHEDULED))
                    .size());
    registerMetric(
        registry,
        OPTIMIZER_GROUP_EXECUTING_TASKS,
        (Gauge<Integer>)
            () -> optimizingQueue.collectTasks(task -> task.getStatus().equals(ACKED)).size());

    registerMetric(
        registry,
        OPTIMIZER_GROUP_PLANING_TABLES,
        (Gauge<Long>)
            () ->
                optimizingQueue.getSchedulingPolicy().getTableRuntimeMap().values().stream()
                    .filter(t -> t.getOptimizingStatus().equals(PLANNING))
                    .count());
    registerMetric(
        registry,
        OPTIMIZER_GROUP_PENDING_TABLES,
        (Gauge<Long>)
            () ->
                optimizingQueue.getSchedulingPolicy().getTableRuntimeMap().values().stream()
                    .filter(t -> t.getOptimizingStatus().equals(PENDING))
                    .count());
    registerMetric(
        registry,
        OPTIMIZER_GROUP_EXECUTING_TABLES,
        (Gauge<Long>)
            () ->
                optimizingQueue.getSchedulingPolicy().getTableRuntimeMap().values().stream()
                    .filter(t -> t.getOptimizingStatus().isProcessing())
                    .count());
    registerMetric(
        registry,
        OPTIMIZER_GROUP_IDLE_TABLES,
        (Gauge<Long>)
            () ->
                optimizingQueue.getSchedulingPolicy().getTableRuntimeMap().values().stream()
                    .filter(t -> t.getOptimizingStatus().equals(IDLE))
                    .count());
    registerMetric(
        registry,
        OPTIMIZER_GROUP_COMMITTING_TABLES,
        (Gauge<Long>)
            () ->
                optimizingQueue.getSchedulingPolicy().getTableRuntimeMap().values().stream()
                    .filter(t -> t.getOptimizingStatus().equals(COMMITTING))
                    .count());

    registerMetric(
        registry, OPTIMIZER_GROUP_OPTIMIZER_INSTANCES, (Gauge<Integer>) optimizerInstances::size);
    registerMetric(
        registry,
        OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED,
        (Gauge<Long>)
            () ->
                optimizerInstances.values().stream()
                    .mapToLong(OptimizerInstance::getMemoryMb)
                    .map(mb -> mb * 1024 * 1024)
                    .sum());
    registerMetric(
        registry,
        OPTIMIZER_GROUP_THREADS,
        (Gauge<Long>)
            () ->
                optimizerInstances.values().stream()
                    .mapToLong(OptimizerInstance::getThreadCount)
                    .sum());
  }

  public void unregister() {
    registeredMetricKeys.forEach(registry::unregister);
    registeredMetricKeys.clear();
  }

  public void addOptimizer(OptimizerInstance optimizerInstance) {
    optimizerInstances.put(optimizerInstance.getToken(), optimizerInstance);
  }

  public void removeOptimizer(OptimizerInstance optimizerInstance) {
    optimizerInstances.remove(optimizerInstance.getToken());
  }
}

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

package com.netease.arctic.server.optimizing;

import static com.netease.arctic.ams.api.metrics.MetricDefine.defineGauge;
import static com.netease.arctic.server.optimizing.TaskRuntime.Status.ACKED;
import static com.netease.arctic.server.optimizing.TaskRuntime.Status.PLANNED;
import static com.netease.arctic.server.optimizing.TaskRuntime.Status.SCHEDULED;

import com.netease.arctic.ams.api.metrics.Gauge;
import com.netease.arctic.ams.api.metrics.Metric;
import com.netease.arctic.ams.api.metrics.MetricDefine;
import com.netease.arctic.ams.api.metrics.MetricKey;
import com.netease.arctic.server.metrics.MetricRegistry;
import com.netease.arctic.server.resource.OptimizerInstance;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OptimizingGroupMetrics {

  @VisibleForTesting protected static final String GROUP_TAG = "group";
  public static final MetricDefine OPTIMIZER_GROUP_QUEUE_TASKS =
      defineGauge("optimizer_group_queue_tasks")
          .withDescription("Number of queue tasks in optimizing resource group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_EXECUTING_TASKS =
      defineGauge("optimizer_group_executing_tasks")
          .withDescription("Number of executing tasks in optimizing resource group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_OPTIMIZERS =
      defineGauge("optimizer_group_optimizers")
          .withDescription("Number of optimizer instances in optimizing resource group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED =
      defineGauge("optimizer_group_memory_bytes_allocated")
          .withDescription("Memory bytes allocated in optimizing resource group")
          .withTags(GROUP_TAG)
          .build();

  public static final MetricDefine OPTIMIZER_GROUP_THREADS =
      defineGauge("optimizer_group_threads")
          .withDescription("Number of total threads in optimizing resource group")
          .withTags(GROUP_TAG)
          .build();

  private final String groupName;
  @VisibleForTesting private final MetricRegistry registry;
  private final OptimizingQueue optimizingQueue;
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();
  private final Map<String, OptimizerInstance> optimizerInstances = new ConcurrentHashMap<>();

  public OptimizingGroupMetrics(
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
        OPTIMIZER_GROUP_QUEUE_TASKS,
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

    registerMetric(registry, OPTIMIZER_GROUP_OPTIMIZERS, (Gauge<Integer>) optimizerInstances::size);
    registerMetric(
        registry,
        OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED,
        (Gauge<Long>)
            () -> {
              // set totalMemoryMb as long type to avoid arithmetic overflow of sum(int)
              long totalMemoryMb = 0;
              for (OptimizerInstance o : optimizerInstances.values()) {
                totalMemoryMb += (long) o.getMemoryMb() * 1024 * 1024;
              }
              return totalMemoryMb;
            });
    registerMetric(
        registry,
        OPTIMIZER_GROUP_THREADS,
        (Gauge<Long>)
            () -> {
              // set totalThreads as long type to avoid arithmetic overflow of sum(int)
              long totalThreads = 0;
              for (OptimizerInstance o : optimizerInstances.values()) {
                totalThreads += o.getThreadCount();
              }
              return totalThreads;
            });
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

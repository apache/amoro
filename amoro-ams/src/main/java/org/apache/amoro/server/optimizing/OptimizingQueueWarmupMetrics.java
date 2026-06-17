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

import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Global metrics for startup optimizing queue warmup. */
public class OptimizingQueueWarmupMetrics {

  public static final MetricDefine OPTIMIZING_QUEUE_WARMUP_TOTAL =
      defineGauge("optimizing_queue_warmup_total")
          .withDescription("Total number of table runtimes submitted to optimizing queue warmup")
          .build();
  public static final MetricDefine OPTIMIZING_QUEUE_WARMUP_LOADED =
      defineGauge("optimizing_queue_warmup_loaded")
          .withDescription("Number of table runtimes successfully warmed into optimizing queues")
          .build();
  public static final MetricDefine OPTIMIZING_QUEUE_WARMUP_SKIPPED =
      defineGauge("optimizing_queue_warmup_skipped")
          .withDescription("Number of table runtimes skipped during optimizing queue warmup")
          .build();
  public static final MetricDefine OPTIMIZING_QUEUE_WARMUP_FAILED =
      defineGauge("optimizing_queue_warmup_failed")
          .withDescription("Number of table runtimes failed during optimizing queue warmup")
          .build();
  public static final MetricDefine OPTIMIZING_QUEUE_WARMUP_RUNNING =
      defineGauge("optimizing_queue_warmup_running")
          .withDescription("Number of table runtimes currently running optimizing queue warmup")
          .build();
  public static final MetricDefine OPTIMIZING_QUEUE_WARMUP_RETRYING =
      defineGauge("optimizing_queue_warmup_retrying")
          .withDescription("Number of table runtimes retried during optimizing queue warmup")
          .build();
  public static final MetricDefine OPTIMIZING_QUEUE_WARMUP_INITIALIZING =
      defineGauge("optimizing_queue_warmup_initializing")
          .withDescription("Number of table runtimes waiting for optimizing queue warmup")
          .build();

  private final MetricRegistry registry;
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();

  private final AtomicLong total = new AtomicLong();
  private final AtomicLong loaded = new AtomicLong();
  private final AtomicLong skipped = new AtomicLong();
  private final AtomicLong failed = new AtomicLong();
  private final AtomicLong running = new AtomicLong();
  private final AtomicLong retrying = new AtomicLong();
  private final AtomicLong initializing = new AtomicLong();

  public OptimizingQueueWarmupMetrics(MetricRegistry registry) {
    this.registry = registry;
  }

  public void register() {
    registerMetric(registry, OPTIMIZING_QUEUE_WARMUP_TOTAL, (Gauge<Long>) total::get);
    registerMetric(registry, OPTIMIZING_QUEUE_WARMUP_LOADED, (Gauge<Long>) loaded::get);
    registerMetric(registry, OPTIMIZING_QUEUE_WARMUP_SKIPPED, (Gauge<Long>) skipped::get);
    registerMetric(registry, OPTIMIZING_QUEUE_WARMUP_FAILED, (Gauge<Long>) failed::get);
    registerMetric(registry, OPTIMIZING_QUEUE_WARMUP_RUNNING, (Gauge<Long>) running::get);
    registerMetric(registry, OPTIMIZING_QUEUE_WARMUP_RETRYING, (Gauge<Long>) retrying::get);
    registerMetric(registry, OPTIMIZING_QUEUE_WARMUP_INITIALIZING, (Gauge<Long>) initializing::get);
  }

  public void unregister() {
    registeredMetricKeys.forEach(registry::unregister);
    registeredMetricKeys.clear();
  }

  void recordSubmitted(long count) {
    if (count <= 0) {
      return;
    }
    total.addAndGet(count);
    initializing.addAndGet(count);
  }

  void recordStart() {
    initializing.decrementAndGet();
    running.incrementAndGet();
  }

  void recordRetry() {
    retrying.incrementAndGet();
  }

  void recordResult(OptimizingQueue.WarmupResult result) {
    running.decrementAndGet();
    if (result == OptimizingQueue.WarmupResult.WARMED) {
      loaded.incrementAndGet();
    } else if (result == OptimizingQueue.WarmupResult.SKIPPED
        || result == OptimizingQueue.WarmupResult.DUPLICATE) {
      skipped.incrementAndGet();
    } else {
      failed.incrementAndGet();
    }
  }

  private void registerMetric(MetricRegistry registry, MetricDefine define, Metric metric) {
    MetricKey key = registry.register(define, Collections.emptyMap(), metric);
    registeredMetricKeys.add(key);
  }
}

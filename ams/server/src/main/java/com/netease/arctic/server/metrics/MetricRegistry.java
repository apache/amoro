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

import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.metrics.Metric;
import com.netease.arctic.ams.api.metrics.MetricName;
import com.netease.arctic.ams.api.metrics.MetricRegistryListener;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A registry of amoro metric.
 */
public class MetricRegistry implements MetricSet {

  private final ConcurrentMap<MetricName, Metric> metrics = Maps.newConcurrentMap();
  private final List<MetricRegistryListener> listeners = new CopyOnWriteArrayList<>();

  /**
   * Add metric registry listener
   * @param listener Metric registry listener
   */
  public void addListener(MetricRegistryListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Register a new metric
   * @param name metric name
   * @param metric metric
   * @param <T> Subclass of metric
   */
  public <T extends Metric> void register(MetricName name, T metric) {
    Preconditions.checkNotNull(name, "metric name must not be null");
    Preconditions.checkNotNull(metric, "metric must not be null");
    Metric exists = metrics.putIfAbsent(name, metric);
    if (exists != null) {
      throw new IllegalArgumentException("Metric: " + name + " has already been registered");
    }

    callListener(l -> l.onMetricAdded(name, metric));
  }

  /**
   * Remove a metric
   * @param name metric name
   */
  public void unregister(MetricName name) {
    Metric exists = metrics.remove(name);
    if (exists != null) {
      callListener(l -> l.onMetricRemoved(name));
    }
  }

  /**
   * Register a metric set
   * @param metrics metric set
   */
  public void registerAll(MetricSet metrics) {
    metrics.getMetrics().forEach(this::register);
  }

  @Override
  public Map<MetricName, Metric> getMetrics() {
    return Collections.unmodifiableMap(metrics);
  }

  private void callListener(Consumer<MetricRegistryListener> consumer) {
    this.listeners.forEach(consumer);
  }
}

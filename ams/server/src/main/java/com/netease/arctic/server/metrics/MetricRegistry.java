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
import com.netease.arctic.ams.api.metrics.*;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** A registry of amoro metric. */
public class MetricRegistry implements MetricSet {

  private final List<MetricRegisterListener> listeners = new CopyOnWriteArrayList<>();

  private final ConcurrentMap<MetricKey, Metric> registeredMetrics = Maps.newConcurrentMap();
  private final Map<String, MetricDefine> definedMetric = Maps.newConcurrentMap();

  /**
   * Add metric registry listener
   *
   * @param listener Metric registry listener
   */
  public void addListener(MetricRegisterListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Register a metric
   *
   * @param define metric define
   * @param tags values of tag
   * @param metric metric
   */
  public <T extends Metric> MetricKey register(MetricDefine define, List<String> tags, T metric) {
    Preconditions.checkNotNull(metric, "Metric must not be null");
    Preconditions.checkNotNull(define, "Metric define must not be null");
    Preconditions.checkArgument(
        define.getType().isType(metric),
        "Metric type miss-match, required：%s, but found implement:%s ",
        define.getType(),
        metric.getClass().getName());

    MetricDefine exists = definedMetric.computeIfAbsent(define.getName(), n -> define);
    Preconditions.checkArgument(
        exists.equals(define),
        "The metric define with name: %s has been already exists, but the define is different.",
        define.getName(),
        exists);

    MetricKey key = MetricKey.buildRegisteredMetricKey(define, tags);

    definedMetric.computeIfPresent(
        define.getName(),
        (name, existsDefine) -> {
          Preconditions.checkArgument(
              define.equals(existsDefine),
              "Metric define:%s is not equal to existed define:%s",
              define,
              existsDefine);
          Metric existedMetric = registeredMetrics.putIfAbsent(key, metric);
          Preconditions.checkArgument(existedMetric == null, "Metric is already been registered.");
          return existsDefine;
        });

    callListener(l -> l.onMetricRegistered(key, metric));
    return key;
  }

  /**
   * Remove a metric
   *
   * @param key registered metric key
   */
  public void unregister(MetricKey key) {
    Metric exists = registeredMetrics.remove(key);
    if (exists != null) {
      callListener(l -> l.onMetricUnregistered(key));
    }
  }

  /**
   * Register a metric set
   *
   * @param metrics metric set
   */
  public void registerAll(MetricSet metrics) {
    metrics.getMetrics().forEach((k, m) -> this.register(k.getDefine(), k.valueOfTags(), m));
  }

  @Override
  public Map<MetricKey, Metric> getMetrics() {
    return Collections.unmodifiableMap(registeredMetrics);
  }

  private void callListener(Consumer<MetricRegisterListener> consumer) {
    this.listeners.forEach(consumer);
  }
}

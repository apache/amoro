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

package com.netease.arctic.server.metrics;

import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.metrics.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** A registry of amoro metric. */
public class MetricRegistry implements MetricSet {

  private final List<MetricRegisterListener> listeners = new CopyOnWriteArrayList<>();

  private final ConcurrentMap<MetricKey, Metric> registeredMetrics = Maps.newConcurrentMap();
  private final Map<String, Pair<MetricDefine, Integer>> definedMetrics = Maps.newConcurrentMap();

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
  public <T extends Metric> MetricKey register(
      MetricDefine define, Map<String, String> tags, T metric) {
    Preconditions.checkNotNull(metric, "Metric must not be null");
    Preconditions.checkNotNull(define, "Metric define must not be null");
    Preconditions.checkArgument(
        define.getType().isType(metric),
        "Metric type miss-match, required：%s, but found implement:%s ",
        define.getType(),
        metric.getClass().getName());

    Pair<MetricDefine, Integer> exists =
        definedMetrics.computeIfAbsent(define.getName(), name -> Pair.of(define, 0));
    Preconditions.checkArgument(
        exists.getLeft().equals(define),
        "The metric define with name: %s has been already exists, but the define is different.",
        define.getName());

    MetricKey key = new MetricKey(define, tags);

    definedMetrics.computeIfPresent(
        define.getName(),
        (name, existsDefine) -> {
          Preconditions.checkArgument(
              define.equals(existsDefine.getLeft()),
              "Metric define:%s is not equal to existed define:%s",
              define,
              existsDefine.getLeft());
          Metric existedMetric = registeredMetrics.putIfAbsent(key, metric);
          Preconditions.checkArgument(existedMetric == null, "Metric is already been registered.");
          return Pair.of(existsDefine.getLeft(), existsDefine.getRight() + 1);
        });

    callListener(listener -> listener.onMetricRegistered(key, metric));
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
      callListener(listener -> listener.onMetricUnregistered(key));
    }
    definedMetrics.computeIfPresent(
        key.getDefine().getName(),
        (name, pair) -> {
          int count = pair.getRight() - 1;
          if (count <= 0) {
            return null;
          } else {
            return Pair.of(pair.getLeft(), count);
          }
        });
  }

  @VisibleForTesting
  int metricDefineCount(String name) {
    return Optional.ofNullable(definedMetrics.getOrDefault(name, null))
        .map(Pair::getRight)
        .orElseGet(() -> 0);
  }

  @Override
  public Map<MetricKey, Metric> getMetrics() {
    return Collections.unmodifiableMap(registeredMetrics);
  }

  private void callListener(Consumer<MetricRegisterListener> consumer) {
    this.listeners.forEach(consumer);
  }
}

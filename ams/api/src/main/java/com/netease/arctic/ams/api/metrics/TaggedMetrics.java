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

package com.netease.arctic.ams.api.metrics;


import com.codahale.metrics.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * This is a simple data structure that separates tags and metrics, making it easier for reporters to write to popular
 * monitoring systems.
 */
public class TaggedMetrics {
  private final Map<String, Object> tags;
  private final Map<String, Metric> metrics;

  private TaggedMetrics(Map<String, Object> tags, Map<String, Metric> metrics) {
    this.tags = tags;
    this.metrics = metrics;
  }

  public static TaggedMetrics of(Map<String, Object> tags, Map<String, Metric> metrics) {
    return new TaggedMetrics(tags, metrics);
  }

  public static TaggedMetrics from(MetricsContent metricsContent) {
    return of(parseTags(metricsContent), parseMetrics(metricsContent));
  }

  public Map<String, Object> tags() {
    return tags;
  }

  public Map<String, Metric> metrics() {
    return metrics;
  }

  private static Map<String, Object> parseTags(MetricsContent payloadMetrics) {
    List<Method> tagMethods = getAnnotationMethods(payloadMetrics, MetricsAnnotation.Tag.class);
    Map<String, Object> tags = Maps.newHashMap();
    tagMethods.forEach(method -> {
      MetricsAnnotation.Tag tag = method.getAnnotation(MetricsAnnotation.Tag.class);
      try {
        tags.put(tag.name(), method.invoke(payloadMetrics));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    });
    return tags;
  }

  private static Map<String, Metric> parseMetrics(MetricsContent payloadMetrics) {
    List<Method> tagMethods = getAnnotationMethods(payloadMetrics, MetricsAnnotation.Metric.class);
    Map<String, Metric> metrics = Maps.newHashMap();
    tagMethods.forEach(method -> {
      MetricsAnnotation.Metric metric = method.getAnnotation(MetricsAnnotation.Metric.class);
      try {
        if (method.invoke(payloadMetrics) != null) {
          metrics.put(metric.name(), (Metric) method.invoke(payloadMetrics));
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    });
    return metrics;
  }

  private static List<Method> getAnnotationMethods(Object object, Class<? extends Annotation> annotationClass) {
    List<Method> methods = Lists.newArrayList();
    for (Method method : object.getClass().getDeclaredMethods()) {
      if (method.isAnnotationPresent(annotationClass)) {
        method.setAccessible(true);
        methods.add(method);
      }
    }
    return methods;
  }
}

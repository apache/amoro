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

package com.netease.arctic.ams.api.metrics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 *  This is a simple data structure that separates tags and metrics, making it easier for reporters to write to popular
 *  monitoring systems when processing {@link MetricReport}
 */
public class TaggedMetrics {
  private final Map<String, Object> tags;
  private final Map<String, Object> metrics;

  public TaggedMetrics(Map<String, Object> tags, Map<String, Object> metrics) {
    this.tags = tags;
    this.metrics = metrics;
  }

  public static TaggedMetrics of(Map<String, Object> tags, Map<String, Object> metrics) {
    return new TaggedMetrics(tags, metrics);
  }

  public static TaggedMetrics from(MetricReport metricReport) {
    return of(parseTags(metricReport), parseMetrics(metricReport));
  }

  public Map<String, Object> tags() {
    return tags;
  }

  public Map<String, Object> metrics() {
    return metrics;
  }

  private static Map<String, Object> parseTags(Object object) {
    List<Method> tagMethods = getAnnotationMethods(object, TaggedMetrics.Tag.class);
    Map<String, Object> tags = Maps.newHashMap();
    tagMethods.forEach(method -> {
      TaggedMetrics.Tag tag = method.getAnnotation(TaggedMetrics.Tag.class);
      try {
        tags.put(tag.name(), method.invoke(object));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    });
    return tags;
  }

  private static Map<String, Object> parseMetrics(Object object) {
    List<Method> tagMethods = getAnnotationMethods(object, TaggedMetrics.Metric.class);
    Map<String, Object> metrics = Maps.newHashMap();
    tagMethods.forEach(method -> {
      TaggedMetrics.Metric metric = method.getAnnotation(TaggedMetrics.Metric.class);
      try {
        if (method.invoke(object) != null) {
          metrics.put(metric.name(), method.invoke(object));
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

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Tag {
    String name();
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Metric {
    String name();
  }
}

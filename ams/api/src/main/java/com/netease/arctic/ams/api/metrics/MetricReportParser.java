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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link MetricReport}, providing common functionality required by MetricReport
 */
public abstract class MetricReportParser {

  public MetricWithTags toMetricWithTags(MetricReport report) {
    return new MetricWithTags(parseTags(report), parseMetrics(report));
  }

  private Map<String, Object> parseTags(Object object) {
    List<Method> tagMethods = getTagMethods(object);
    Map<String, Object> tags = Maps.newHashMap();
    tagMethods.forEach(method -> {
      MetricWithTags.TagName tagName = method.getAnnotation(MetricWithTags.TagName.class);
      try {
        tags.put(tagName.value(), method.invoke(object));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    });
    return tags;
  }

  private Map<String, Object> parseMetrics(Object object) {
    List<Method> metricMethods = getMetricMethods(object);
    Map<String, Object> tags = Maps.newHashMap();
    metricMethods.forEach(method -> {
      try {
        List<Field> tagFields = getMetricFields(method.invoke(object));
        tagFields.forEach(field -> {
          MetricWithTags.MetricName metricName = field.getAnnotation(MetricWithTags.MetricName.class);
          try {
            tags.put(metricName.value(), field.get(object));
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        });
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    });
    return tags;
  }

  private List<Field> getMetricFields(Object object) {
    return getAnnotationFields(object, MetricWithTags.MetricName.class);
  }

  private List<Method> getTagMethods(Object object) {
    return getAnnotationMethods(object, MetricWithTags.TagName.class);
  }

  private List<Method> getMetricMethods(Object object) {
    return getAnnotationMethods(object, MetricWithTags.Metric.class);
  }

  private List<Field> getAnnotationFields(Object object, Class<? extends Annotation> annotationClass) {
    List<Field> fields = Lists.newArrayList();
    for (Field field : object.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(annotationClass)) {
        field.setAccessible(true);
        fields.add(field);
      }
    }
    return fields;
  }

  private List<Method> getAnnotationMethods(Object object, Class<? extends Annotation> annotationClass) {
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

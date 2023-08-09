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

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public abstract class AbstractMetricReport<T> implements MetricReport<T> {

  public static final String METRICS_NAME = "metrics";

  public JSONObject toJson() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.putAll(parseTags(this));
    jsonObject.put(METRICS_NAME, new JSONObject(parseTags(this.metrics())));
    return jsonObject;
  }

  public MetricWithTags toMetricWithTags() {
    return new MetricWithTags(parseTags(this), parseTags(this.metrics()));
  }

  private Map<String, Object> parseTags(Object object) {
    List<Field> tagFields = getTagFields(object);
    Map<String, Object> tags = Maps.newHashMap();
    tagFields.forEach(field -> {
      TagName tagName = field.getAnnotation(TagName.class);
      try {
        tags.put(tagName.value(), field.get(object));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    });
    return tags;
  }

  private Map<String, Object> parseMetrics(Object object) {
    List<Field> tagFields = getTagFields(object);
    Map<String, Object> tags = Maps.newHashMap();
    tagFields.forEach(field -> {
      MetricName metricName = field.getAnnotation(MetricName.class);
      try {
        tags.put(metricName.value(), field.get(object));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    });
    return tags;
  }

  private List<Field> getTagFields(Object object) {
    return getAnnotationFields(object, TagName.class);
  }

  private List<Field> getMetricFields(Object object) {
    return getAnnotationFields(object, MetricName.class);
  }

  private List<Field> getAnnotationFields(Object object, Class<? extends Annotation> annotationClass) {
    List<Field> fields = Lists.newArrayList();
    for (Field field: object.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(annotationClass)) {
        field.setAccessible(true);
        fields.add(field);
      }
    }
    return fields;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface TagName {
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface MetricName {
    String value();
  }
}

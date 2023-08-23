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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 *  This is a simple data structure that separates tags and metrics, making it easier for reporters to write to popular
 *  monitoring systems when processing {@link MetricReport}
 */
public class MetricWithTags {
  private final Map<String, Object> tags;
  private final Map<String, Object> metrics;

  public MetricWithTags(Map<String, Object> tags, Map<String, Object> metrics) {
    this.tags = tags;
    this.metrics = metrics;
  }

  public static MetricWithTags of(Map<String, Object> tags, Map<String, Object> metrics) {
    return new MetricWithTags(tags, metrics);
  }

  public Map<String, Object> tags() {
    return tags;
  }

  public Map<String, Object> metrics() {
    return metrics;
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface TagName {
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface MetricName {
    String value();
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Metric {
  }
}

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

import com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/** Define a metric */
public class MetricDefine {

  private final String name;
  private final List<String> tags;
  private final MetricType type;
  private final String description;

  MetricDefine(String name, List<String> tags, MetricType type, String description) {
    Preconditions.checkArgument(name != null && !name.trim().isEmpty(), "Metric name is required");
    Preconditions.checkArgument(type != null, "Metric type is required");
    this.name = name;
    if (tags == null) {
      tags = ImmutableList.of();
    }
    this.tags = tags;
    this.type = type;
    this.description = description;
  }

  /**
   * Metric name
   *
   * @return metric name
   */
  public String getName() {
    return name;
  }

  /**
   * Supported tags
   *
   * @return metric tags
   */
  public List<String> getTags() {
    return tags;
  }

  /**
   * Metric type
   *
   * @return metric type
   */
  public MetricType getType() {
    return type;
  }

  /**
   * Metric description
   *
   * @return description
   */
  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MetricDefine that = (MetricDefine) o;
    return this.toString().equals(that.toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.toString());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(name);
    builder.append(",type=").append(type.name());
    builder.append("<");
    Joiner.on(",").appendTo(builder, tags.stream().sorted().iterator());
    builder.append(">");
    return builder.toString();
  }

  static Builder defineCounter(String name) {
    return new Builder(name, MetricType.Counter);
  }

  public static Builder defineGauge(String name) {
    return new Builder(name, MetricType.Gauge);
  }

  public static class Builder {
    private final String name;
    private List<String> tags;
    private final MetricType type;
    private String description;

    Builder(String name, MetricType type) {
      this.name = name;
      this.type = type;
    }

    public Builder withTags(String... tags) {
      this.tags = Lists.newArrayList(tags);
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public MetricDefine build() {
      return new MetricDefine(name, tags, type, description);
    }
  }
}

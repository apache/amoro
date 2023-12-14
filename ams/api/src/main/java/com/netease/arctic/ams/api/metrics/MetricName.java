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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

public class MetricName {

  private final String name;

  private final List<String> tags;
  private final Map<String, String> values;
  private final String explicitMetricName;

  public static MetricName fromExplicitName(String explicitMetricName) {
    int tagStart = explicitMetricName.indexOf('<');
    if (tagStart < 0) {
      // No tags
      return new MetricName(explicitMetricName, Collections.emptyList(), Collections.emptyList());
    } else {
      String name = explicitMetricName.substring(0, tagStart);
      String tagValueString = explicitMetricName.substring(tagStart + 1, explicitMetricName.length() - 1);
      String[] tagValuePairs = tagValueString.split(",");
      List<String> tags = Lists.newArrayList();
      List<String> values = Lists.newArrayList();
      for (String pair : tagValuePairs) {
        String[] parts = pair.split("=");
        tags.add(parts[0]);
        values.add(parts[1]);
      }
      return new MetricName(name, tags, values);
    }
  }


  public MetricName(String name, List<String> tags, List<String> values) {
    if (tags == null) {
      tags = Collections.emptyList();
    }
    if (values == null) {
      values = Collections.emptyList();
    }

    Preconditions.checkArgument(tags.size() == values.size(),
        "Illegal metric name: tags size and values mismatch");
    this.name = name;
    this.tags = tags;
    Map<String,String> valueMap = Collections.emptyMap();

    StringBuilder nameBuilder = new StringBuilder(100);
    nameBuilder.append(this.name);
    if (!tags.isEmpty()) {
      valueMap = Maps.newHashMap();

      Iterator<String> tagIt = tags.iterator();
      Iterator<String> valueIt = values.iterator();
      List<String> tagAndValueList = Lists.newArrayList();
      while (tagIt.hasNext() && valueIt.hasNext()) {
        String tag = tagIt.next();
        String value = valueIt.next();
        tagAndValueList.add(tag + "=" + value);
        valueMap.put(tag, value);
      }

      String tagAndValue = Joiner.on(",").join(tagAndValueList);
      nameBuilder.append("<").append(tagAndValue).append(">");
    }
    this.values = valueMap;
    this.explicitMetricName = nameBuilder.toString();
  }


  public String getName() {
    return name;
  }

  public List<String> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public Map<String, String> getValues() {
    return Collections.unmodifiableMap(values);
  }

  public String getExplicitMetricName() {
    return explicitMetricName;
  }

  @Override
  public String toString() {
    return getExplicitMetricName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MetricName that = (MetricName) o;
    return that.explicitMetricName.equals(this.explicitMetricName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(explicitMetricName);
  }
}

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

package org.apache.amoro.metrics.promethues;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Metric categories for Prometheus exporter filtering. */
public enum MetricCategory {
  SELF_OPTIMIZING("self-optimizing", "table_optimizing_"),
  OPTIMIZER_GROUP("optimizer-group", "optimizer_group_"),
  ORPHAN_FILES("orphan-files", "table_orphan_", "table_expected_orphan_"),
  AMS_JVM("ams-jvm", "ams_jvm_", "ams_ha_"),
  TABLE_SUMMARY("table-summary", "table_summary_");

  private static final String CATEGORY_PROPERTY_PREFIX = "category.";
  private static final String CATEGORY_PROPERTY_SUFFIX = ".enabled";

  private final String configName;
  private final String[] prefixes;

  MetricCategory(String configName, String... prefixes) {
    this.configName = configName;
    this.prefixes = prefixes;
  }

  public String getConfigName() {
    return configName;
  }

  /** Check if a metric name belongs to this category. */
  public boolean matches(String metricName) {
    for (String prefix : prefixes) {
      if (metricName.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Parse properties and return the set of disabled categories. Categories not mentioned in
   * properties are enabled by default.
   */
  public static Set<MetricCategory> parseDisabledCategories(Map<String, String> properties) {
    return Arrays.stream(values())
        .filter(
            category -> {
              String key =
                  CATEGORY_PROPERTY_PREFIX + category.configName + CATEGORY_PROPERTY_SUFFIX;
              String value = properties.get(key);
              return "false".equalsIgnoreCase(value);
            })
        .collect(Collectors.toSet());
  }

  /** Find the category that a metric name belongs to. Returns null if no category matches. */
  public static MetricCategory findCategory(String metricName) {
    for (MetricCategory category : values()) {
      if (category.matches(metricName)) {
        return category;
      }
    }
    return null;
  }
}

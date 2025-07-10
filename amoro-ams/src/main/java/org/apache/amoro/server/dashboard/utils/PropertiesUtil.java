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

package org.apache.amoro.server.dashboard.utils;

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertiesUtil {
  public static void putNotNullProperties(
      Map<String, String> properties, String key, String value) {
    if (value != null) {
      properties.put(key, value);
    }
  }

  public static void removeHiddenProperties(Map<String, String> properties, Set<String> skipKeys) {
    for (String skipKey : skipKeys) {
      properties.remove(skipKey);
    }
  }

  public static Map<String, String> extractTableProperties(Map<String, String> catalogProperties) {
    Map<String, String> result = Maps.newHashMap();
    catalogProperties.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(CatalogMetaProperties.TABLE_PROPERTIES_PREFIX))
        .forEach(
            entry ->
                result.put(
                    entry.getKey().replaceFirst(CatalogMetaProperties.TABLE_PROPERTIES_PREFIX, ""),
                    entry.getValue()));
    return result;
  }

  public static Map<String, String> extractCatalogMetaProperties(
      Map<String, String> catalogProperties) {
    Map<String, String> result = Maps.newHashMap();
    catalogProperties.entrySet().stream()
        .filter(entry -> !entry.getKey().startsWith(CatalogMetaProperties.TABLE_PROPERTIES_PREFIX))
        .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
    return result;
  }

  public static Map<String, String> unionCatalogProperties(
      Map<String, String> tableProperties, Map<String, String> catalogMetaProperties) {
    Map<String, String> result = Maps.newHashMap(catalogMetaProperties);
    tableProperties.forEach(
        (key, value) -> result.put(CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + key, value));
    return result;
  }

  /**
   * Sanitize a map of string properties. This processing includes trimming keys and values,
   * removing entries with empty keys or null values, and resolving key conflicts by keeping the
   * latter value.
   *
   * @param properties the raw input map to sanitize
   * @return a new map with cleaned keys and values
   */
  public static Map<String, String> sanitizeProperties(Map<String, String> properties) {
    if (properties == null || properties.isEmpty()) {
      return Collections.emptyMap();
    }

    return properties.entrySet().stream()
        .map(
            entry -> {
              String key = entry.getKey() == null ? "" : entry.getKey().trim();
              String value = entry.getValue() == null ? null : entry.getValue().trim();
              return new AbstractMap.SimpleEntry<>(key, value);
            })
        .filter(e -> !e.getKey().isEmpty() && e.getValue() != null)
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (v1, v2) -> v2 // use the later value on duplicate keys
                ));
  }
}

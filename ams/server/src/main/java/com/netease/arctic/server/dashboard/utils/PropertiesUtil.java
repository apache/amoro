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

package com.netease.arctic.server.dashboard.utils;

import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

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
}

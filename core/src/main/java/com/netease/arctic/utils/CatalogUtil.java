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

package com.netease.arctic.utils;

import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;
import java.util.stream.Collectors;

public class CatalogUtil {

  /**
   * merge properties of table level in catalog properties to table(properties key start with table.)
   * @param tableProperties properties in table
   * @param catalogProperties properties in catalog
   * @return merged table properties
   */
  public static Map<String, String> mergeCatalogPropertiesToTable(Map<String, String> tableProperties,
                                                                  Map<String, String> catalogProperties) {
    Map<String, String> mergedProperties = catalogProperties.entrySet().stream()
        .filter(e -> e.getKey().startsWith(CatalogMetaProperties.TABLE_PROPERTIES_PREFIX))
        .collect(Collectors.toMap(e ->
            e.getKey().substring(CatalogMetaProperties.TABLE_PROPERTIES_PREFIX.length()), Map.Entry::getValue));

    if (!PropertyUtil.propertyAsBoolean(tableProperties, TableProperties.ENABLE_LOG_STORE,
        TableProperties.ENABLE_LOG_STORE_DEFAULT)) {
      mergedProperties = mergedProperties.entrySet().stream()
          .filter(e -> !e.getKey().startsWith(CatalogMetaProperties.LOG_STORE_PROPERTIES_PREFIX))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    String optimizationEnabled = tableProperties.getOrDefault(TableProperties.ENABLE_OPTIMIZE,
        mergedProperties.getOrDefault(TableProperties.ENABLE_OPTIMIZE, TableProperties.ENABLE_OPTIMIZE_DEFAULT));
    if (!Boolean.parseBoolean(optimizationEnabled)) {
      mergedProperties = mergedProperties.entrySet().stream()
          .filter(e -> !e.getKey().startsWith(CatalogMetaProperties.OPTIMIZE_PROPERTIES_PREFIX))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      // maintain 'optimize.enable' flag as false in table properties
      mergedProperties.put(TableProperties.ENABLE_OPTIMIZE, optimizationEnabled);
    }
    mergedProperties.putAll(tableProperties);

    return mergedProperties;
  }
}

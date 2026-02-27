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

package org.apache.amoro.flink.table;

import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;

import java.util.Map;
import java.util.Properties;

public class OptionsUtil {

  // Prefix for specific properties.
  public static final String PROPERTIES_PREFIX = "properties.";

  public static Properties getKafkaProperties(Map<String, String> tableOptions) {
    final Properties kafkaProperties = new Properties();

    if (hasProperties(tableOptions)) {
      tableOptions.keySet().stream()
          .filter(key -> key.startsWith(PROPERTIES_PREFIX))
          .forEach(
              key -> {
                final String value = tableOptions.get(key);
                final String subKey = key.substring((PROPERTIES_PREFIX).length());
                kafkaProperties.put(subKey, value);
              });
    }
    return kafkaProperties;
  }

  public static Map<String, String> getCatalogProperties(Map<String, String> options) {
    Map<String, String> catalogProperties = Maps.newHashMap();
    options.forEach(
        (key, value) -> {
          if (key.startsWith(PROPERTIES_PREFIX)) {
            catalogProperties.put(key.substring((PROPERTIES_PREFIX).length()), value);
          } else {
            catalogProperties.put(key, value);
          }
        });
    return catalogProperties;
  }

  /** Decides if the table options contains table properties that start with prefix 'properties'. */
  private static boolean hasProperties(Map<String, String> tableOptions) {
    return tableOptions.keySet().stream().anyMatch(k -> k.startsWith(PROPERTIES_PREFIX));
  }
}

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

package com.netease.arctic.flink.util;

import com.netease.arctic.flink.table.descriptors.ArcticValidator;
import com.netease.arctic.table.TableProperties;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;
import java.util.Properties;

import static com.netease.arctic.table.TableProperties.LOG_STORE_ADDRESS;
import static com.netease.arctic.table.TableProperties.LOG_STORE_STORAGE_TYPE_DEFAULT;
import static com.netease.arctic.table.TableProperties.LOG_STORE_STORAGE_TYPE_PULSAR;
import static com.netease.arctic.table.TableProperties.LOG_STORE_TYPE;
import static org.apache.flink.connector.pulsar.common.config.PulsarOptions.PULSAR_SERVICE_URL;

/**
 * PropertyUtil compatible with legacy flink properties
 */
public class CompatibleFlinkPropertyUtil {

  private CompatibleFlinkPropertyUtil() {
  }

  public static boolean propertyAsBoolean(Map<String, String> properties,
                                          String property, boolean defaultValue) {
    return PropertyUtil.propertyAsBoolean(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static boolean propertyAsBoolean(ReadableConfig config, ConfigOption<Boolean> configOption) {
    ConfigOption<Boolean> legacyProperty = getLegacyProperty(configOption);
    if (legacyProperty != null && config.getOptional(legacyProperty).isPresent() &&
        !config.getOptional(configOption).isPresent()) {
      return config.get(legacyProperty);
    } else {
      return config.get(configOption);
    }
  }

  public static double propertyAsDouble(Map<String, String> properties,
                                        String property, double defaultValue) {
    return PropertyUtil.propertyAsDouble(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static int propertyAsInt(Map<String, String> properties,
                                  String property, int defaultValue) {
    return PropertyUtil.propertyAsInt(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static long propertyAsLong(Map<String, String> properties,
                                    String property, long defaultValue) {
    return PropertyUtil.propertyAsLong(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static String propertyAsString(Map<String, String> properties,
                                        String property, String defaultValue) {
    return PropertyUtil.propertyAsString(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  private static String getCompatibleProperty(Map<String, String> properties, String property) {
    String legacyProperty = getLegacyProperty(property);
    if (legacyProperty != null && properties.containsKey(legacyProperty) && !properties.containsKey(property)) {
      return legacyProperty;
    } else {
      return property;
    }
  }

  private static String getLegacyProperty(String property) {
    if (property == null) {
      return null;
    }
    if (ArcticValidator.ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE.key().equals(property)) {
      return ArcticValidator.ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE_LEGACY.key();
    } else if (ArcticValidator.DIM_TABLE_ENABLE.key().equals(property)) {
      return ArcticValidator.DIM_TABLE_ENABLE_LEGACY.key();
    }
    switch (property) {
      case ArcticValidator.ARCTIC_LATENCY_METRIC_ENABLE:
        return ArcticValidator.ARCTIC_LATENCY_METRIC_ENABLE_LEGACY;
      case ArcticValidator.ARCTIC_THROUGHPUT_METRIC_ENABLE:
        return ArcticValidator.ARCTIC_THROUGHPUT_METRIC_ENABLE_LEGACY;
      default:
        return null;
    }
  }

  private static ConfigOption<Boolean> getLegacyProperty(ConfigOption<Boolean> configOption) {
    if (configOption == null) {
      return null;
    }
    if (ArcticValidator.ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE.key().equals(configOption.key())) {
      return ArcticValidator.ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE_LEGACY;
    } else if (ArcticValidator.DIM_TABLE_ENABLE.key().equals(configOption.key())) {
      return ArcticValidator.DIM_TABLE_ENABLE_LEGACY;
    }
    return null;
  }

  /**
   * Get log-store properties from table properties and flink options, whose prefix is
   * {@link TableProperties#LOG_STORE_PROPERTIES_PREFIX}.
   *
   * @param tableOptions including table properties and flink options
   * @return Properties. The keys in it have no {@link TableProperties#LOG_STORE_PROPERTIES_PREFIX}.
   */
  public static Properties getLogStoreProperties(Map<String, String> tableOptions) {
    final Properties properties = new Properties();

    if (hasPrefix(tableOptions, TableProperties.LOG_STORE_PROPERTIES_PREFIX)) {
      tableOptions.keySet().stream()
          .filter(key -> key.startsWith(TableProperties.LOG_STORE_PROPERTIES_PREFIX))
          .forEach(
              key -> {
                final String value = tableOptions.get(key);
                final String subKey = key.substring((TableProperties.LOG_STORE_PROPERTIES_PREFIX).length());
                properties.put(subKey, value);
              });
    }

    // convert the key to support create client in writer
    if (CompatibleFlinkPropertyUtil.propertyAsString(tableOptions, LOG_STORE_TYPE, LOG_STORE_STORAGE_TYPE_DEFAULT)
        .equals(LOG_STORE_STORAGE_TYPE_PULSAR)) {
      properties.put(PULSAR_SERVICE_URL.key(),
          CompatibleFlinkPropertyUtil.propertyAsString(tableOptions, LOG_STORE_ADDRESS, null));
    }
    return properties;
  }

  public static boolean hasPrefix(Map<String, String> tableOptions, String prefix) {
    return tableOptions.keySet().stream().anyMatch(k -> k.startsWith(prefix));
  }
}

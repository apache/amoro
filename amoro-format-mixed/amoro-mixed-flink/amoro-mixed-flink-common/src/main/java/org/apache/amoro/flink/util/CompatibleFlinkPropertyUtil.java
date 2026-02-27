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

package org.apache.amoro.flink.util;

import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.TOPIC;

import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.table.TableProperties;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.iceberg.util.PropertyUtil;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/** PropertyUtil compatible with legacy flink properties */
public class CompatibleFlinkPropertyUtil {

  private CompatibleFlinkPropertyUtil() {}

  public static boolean propertyAsBoolean(
      Map<String, String> properties, String property, boolean defaultValue) {
    return PropertyUtil.propertyAsBoolean(
        properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static boolean propertyAsBoolean(
      ReadableConfig config, ConfigOption<Boolean> configOption) {
    ConfigOption<Boolean> legacyProperty = getLegacyProperty(configOption);
    if (legacyProperty != null
        && config.getOptional(legacyProperty).isPresent()
        && !config.getOptional(configOption).isPresent()) {
      return config.get(legacyProperty);
    } else {
      return config.get(configOption);
    }
  }

  public static double propertyAsDouble(
      Map<String, String> properties, String property, double defaultValue) {
    return PropertyUtil.propertyAsDouble(
        properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static int propertyAsInt(
      Map<String, String> properties, String property, int defaultValue) {
    return PropertyUtil.propertyAsInt(
        properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static long propertyAsLong(
      Map<String, String> properties, String property, long defaultValue) {
    return PropertyUtil.propertyAsLong(
        properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static String propertyAsString(
      Map<String, String> properties, String property, String defaultValue) {
    return PropertyUtil.propertyAsString(
        properties, getCompatibleProperty(properties, property), defaultValue);
  }

  private static String getCompatibleProperty(Map<String, String> properties, String property) {
    String legacyProperty = getLegacyProperty(property);
    if (legacyProperty != null
        && properties.containsKey(legacyProperty)
        && !properties.containsKey(property)) {
      return legacyProperty;
    } else {
      return property;
    }
  }

  private static String getLegacyProperty(String property) {
    if (property == null) {
      return null;
    }
    if (MixedFormatValidator.MIXED_FORMAT_LOG_CONSISTENCY_GUARANTEE_ENABLE.key().equals(property)) {
      return MixedFormatValidator.MIXED_FORMAT_LOG_CONSISTENCY_GUARANTEE_ENABLE_LEGACY.key();
    } else if (MixedFormatValidator.DIM_TABLE_ENABLE.key().equals(property)) {
      return MixedFormatValidator.DIM_TABLE_ENABLE_LEGACY.key();
    }
    switch (property) {
      case MixedFormatValidator.MIXED_FORMAT_LATENCY_METRIC_ENABLE:
        return MixedFormatValidator.MIXED_FORMAT_LATENCY_METRIC_ENABLE_LEGACY;
      case MixedFormatValidator.MIXED_FORMAT_THROUGHPUT_METRIC_ENABLE:
        return MixedFormatValidator.MIXED_FORMAT_THROUGHPUT_METRIC_ENABLE_LEGACY;
      default:
        return null;
    }
  }

  private static ConfigOption<Boolean> getLegacyProperty(ConfigOption<Boolean> configOption) {
    if (configOption == null) {
      return null;
    }
    if (MixedFormatValidator.MIXED_FORMAT_LOG_CONSISTENCY_GUARANTEE_ENABLE
        .key()
        .equals(configOption.key())) {
      return MixedFormatValidator.MIXED_FORMAT_LOG_CONSISTENCY_GUARANTEE_ENABLE_LEGACY;
    } else if (MixedFormatValidator.DIM_TABLE_ENABLE.key().equals(configOption.key())) {
      return MixedFormatValidator.DIM_TABLE_ENABLE_LEGACY;
    }
    return null;
  }

  /**
   * Get log-store properties from table properties and flink options, whose prefix is {@link
   * TableProperties#LOG_STORE_PROPERTIES_PREFIX}.
   *
   * @param tableOptions including table properties and flink options
   * @return Properties. The keys in it have no {@link TableProperties#LOG_STORE_PROPERTIES_PREFIX}.
   */
  public static Properties fetchLogstorePrefixProperties(Map<String, String> tableOptions) {
    final Properties properties = new Properties();

    if (hasPrefix(tableOptions, TableProperties.LOG_STORE_PROPERTIES_PREFIX)) {
      tableOptions.keySet().stream()
          .filter(key -> key.startsWith(TableProperties.LOG_STORE_PROPERTIES_PREFIX))
          .forEach(
              key -> {
                final String value = tableOptions.get(key);
                final String subKey =
                    key.substring((TableProperties.LOG_STORE_PROPERTIES_PREFIX).length());
                properties.put(subKey, value);
              });
    }
    return properties;
  }

  public static boolean hasPrefix(Map<String, String> tableOptions, String prefix) {
    return tableOptions.keySet().stream().anyMatch(k -> k.startsWith(prefix));
  }

  public static List<String> getLogTopic(Map<String, String> tableProperties) {
    Configuration conf = new Configuration();
    conf.setString(TOPIC.key(), tableProperties.get(TableProperties.LOG_STORE_MESSAGE_TOPIC));
    return conf.get(TOPIC);
  }
}

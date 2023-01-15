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

package com.netease.arctic.flink.table.descriptors;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.pulsar.common.config.PulsarOptions;
import org.apache.flink.connector.pulsar.sink.PulsarSinkOptions;
import org.apache.flink.connector.pulsar.sink.config.SinkConfiguration;
import org.apache.flink.connector.pulsar.source.PulsarSourceOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.flink.connector.pulsar.common.config.PulsarOptions.ADMIN_CONFIG_PREFIX;
import static org.apache.flink.connector.pulsar.common.config.PulsarOptions.CLIENT_CONFIG_PREFIX;
import static org.apache.flink.connector.pulsar.common.config.PulsarOptions.PULSAR_SERVICE_URL;
import static org.apache.flink.connector.pulsar.source.PulsarSourceOptions.CONSUMER_CONFIG_PREFIX;
import static org.apache.flink.connector.pulsar.source.PulsarSourceOptions.PULSAR_SUBSCRIPTION_NAME;
import static org.apache.flink.connector.pulsar.source.PulsarSourceOptions.SOURCE_CONFIG_PREFIX;

/**
 * It's used for converting Arctic log-store related properties in {@link com.netease.arctic.table.TableProperties}
 * to Flink official pulsar configuration.
 */
public class PulsarConfigurationConverter {

  private final static Logger LOG = LoggerFactory.getLogger(PulsarConfigurationConverter.class);
  private final static String PULSAR_OPTIONS_PREFIX = "PULSAR_";

  /**
   * @param arcticProperties The key has been trimmed of Arctic prefix
   * @return Properties with Flink Pulsar source keys
   */
  public static Properties toSourceConf(Properties arcticProperties) {
    Properties props = new Properties();

    Set<String> sourceKeys = getPulsarSourceConf();
    arcticProperties.stringPropertyNames().forEach(k -> {
      String toKey;
      Object v = arcticProperties.get(k);
      if (sourceKeys.contains(toKey = CLIENT_CONFIG_PREFIX + k)) {
        props.put(toKey, v);
      } else if (sourceKeys.contains(toKey = ADMIN_CONFIG_PREFIX + k)) {
        props.put(toKey, v);
      } else if (sourceKeys.contains(toKey = CONSUMER_CONFIG_PREFIX + k)) {
        props.put(toKey, v);
      } else if (sourceKeys.contains(toKey = SOURCE_CONFIG_PREFIX + k)) {
        props.put(toKey, v);
      } else {
        props.put(k, v);
      }
    });

    props.putIfAbsent(PULSAR_SUBSCRIPTION_NAME.key(), UUID.randomUUID());
    return props;
  }

  /**
   * @param arcticProperties The key has been trimmed of Arctic prefix
   * @param serviceUrl       e.g. pulsar://localhost:6650
   * @return Properties with Flink Pulsar source keys
   */
  public static SinkConfiguration toSinkConf(Properties arcticProperties, String serviceUrl) {
    Configuration conf = new Configuration();
    conf.setString(PULSAR_SERVICE_URL.key(), serviceUrl);

    Set<String> sourceKeys = getPulsarSinkConf();
    arcticProperties.stringPropertyNames().forEach(k -> {
      String toKey;
      String v = (String) arcticProperties.get(k);
      if (sourceKeys.contains(toKey = CLIENT_CONFIG_PREFIX + k)) {
        conf.setString(toKey, v);
      } else if (sourceKeys.contains(toKey = ADMIN_CONFIG_PREFIX + k)) {
        conf.setString(toKey, v);
      } else if (sourceKeys.contains(toKey = CONSUMER_CONFIG_PREFIX + k)) {
        conf.setString(toKey, v);
      } else if (sourceKeys.contains(toKey = SOURCE_CONFIG_PREFIX + k)) {
        conf.setString(toKey, v);
      } else {
        conf.setString(k, v);
      }
    });
    return new SinkConfiguration(conf);
  }

  public static Set<String> getPulsarSourceConf() {
    Set<String> keys = getPulsarConf(PulsarSourceOptions.class);
    keys.addAll(getPulsarConf(PulsarOptions.class));
    return keys;
  }

  public static Set<String> getPulsarSinkConf() {
    Set<String> keys = getPulsarConf(PulsarSinkOptions.class);
    keys.addAll(getPulsarConf(PulsarOptions.class));
    return keys;
  }

  public static Set<String> getPulsarConf(Class<?> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
        .filter(field -> field.getName().startsWith(PULSAR_OPTIONS_PREFIX))
        .map(field -> {
          try {
            return ((ConfigOption) field.get(clazz)).key();
          } catch (IllegalAccessException e) {
            LOG.warn("can not access field: {}", field.getName());
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

}

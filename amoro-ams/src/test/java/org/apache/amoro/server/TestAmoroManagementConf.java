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

package org.apache.amoro.server;

import static org.apache.amoro.server.AmoroServiceContainer.expandConfigMap;

import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.io.Resources;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.utils.JacksonUtil;
import org.apache.amoro.utils.MemorySize;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

public class TestAmoroManagementConf {
  private static final ConfigOption<Duration>[] TIME_RELATED_CONFIG_OPTIONS =
      new ConfigOption[] {
        AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL,
        AmoroManagementConf.AUTO_CREATE_TAGS_INTERVAL,
        AmoroManagementConf.REFRESH_TABLES_INTERVAL,
        AmoroManagementConf.BLOCKER_TIMEOUT,
        AmoroManagementConf.OPTIMIZER_HB_TIMEOUT,
        AmoroManagementConf.OPTIMIZER_TASK_ACK_TIMEOUT,
        AmoroManagementConf.OPTIMIZER_POLLING_TIMEOUT,
        AmoroManagementConf.TERMINAL_SESSION_TIMEOUT
      };

  private static final Map<String, String> DEFAULT_TIME_UNIT_IN_OLD_VERSIONS =
      ImmutableMap.<String, String>builder()
          .put(AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL.key(), "ms")
          .put(AmoroManagementConf.AUTO_CREATE_TAGS_INTERVAL.key(), "ms")
          .put(AmoroManagementConf.REFRESH_TABLES_INTERVAL.key(), "ms")
          .put(AmoroManagementConf.BLOCKER_TIMEOUT.key(), "ms")
          .put(AmoroManagementConf.OPTIMIZER_HB_TIMEOUT.key(), "ms")
          .put(AmoroManagementConf.OPTIMIZER_TASK_ACK_TIMEOUT.key(), "ms")
          .put(AmoroManagementConf.OPTIMIZER_POLLING_TIMEOUT.key(), "ms")
          .put(AmoroManagementConf.TERMINAL_SESSION_TIMEOUT.key(), "min")
          .build();

  private static final ConfigOption<MemorySize>[] STORAGE_RELATED_CONFIG_OPTIONS =
      new ConfigOption[] {AmoroManagementConf.THRIFT_MAX_MESSAGE_SIZE};

  @Test
  void testParsingDefaultTimeRelatedConfigs() {
    Configurations serviceConfig = new Configurations();
    Configurations expectedConfig =
        Configurations.fromObjectMap(timeRelatedConfigMapInMillisSecondsWithoutTimeUnits);
    assertTimeRelatedConfigs(serviceConfig, expectedConfig);
  }

  @Test
  void testParsingDefaultStorageRelatedConfigs() {
    Configurations serviceConfig = new Configurations();
    Configurations expectedConfig =
        Configurations.fromObjectMap(storageRelatedConfigMapWithoutTimeUnits);
    assertStorageRelatedConfigs(serviceConfig, expectedConfig);
  }

  @Test
  void testParsingAmoroManagementConfWithTimeUnits() throws Exception {
    Configurations serviceConfig = getConfigurationsWithUnits();
    Configurations expectedConfig =
        Configurations.fromObjectMap(timeRelatedConfigMapInMillisSecondsWithoutTimeUnits);
    assertTimeRelatedConfigs(serviceConfig, expectedConfig);
  }

  @Test
  void testParsingAmoroManagementConfWithStorageUnits() throws Exception {
    Configurations serviceConfig = getConfigurationsWithUnits();
    Configurations expectedConfig =
        Configurations.fromObjectMap(storageRelatedConfigMapWithoutTimeUnits);
    assertStorageRelatedConfigs(serviceConfig, expectedConfig);
  }

  @Test
  void testParsingAmoroManagementConfWithoutTimeUnits() throws Exception {
    Configurations serviceConfig = getConfigurationsWithoutUnits();
    Configurations expectedConfig = Configurations.fromObjectMap(timeRelatedConfigMapWithTimeUnits);
    assertTimeRelatedConfigs(serviceConfig, expectedConfig);
  }

  @Test
  void testParsingAmoroManagementConfWithoutStorageUnits() throws Exception {
    Configurations serviceConfig = getConfigurationsWithoutUnits();
    Configurations expectedConfig =
        Configurations.fromObjectMap(storageRelatedConfigMapWithTimeUnits);
    assertStorageRelatedConfigs(serviceConfig, expectedConfig);
  }

  /** Test for conflicts when parsing configuration files in older versions (< 0.8) */
  @Test
  void testConflictsForParsingAmoroManagementConfInOldVersions() throws Exception {
    Configurations serviceConfig = getConfigurationsWithoutUnits();
    Configurations expectedConfig =
        Configurations.fromObjectMap(timeRelatedConfigMapWithExpectedTimeUnitsInOldVersions);

    // Checking the parsed time-related configuration items (should fail).
    // As the default unit for `terminal.session.timeout` has changed from minutes to milliseconds
    // since version 0.8.
    Assertions.assertThrows(
        AssertionError.class, () -> assertTimeRelatedConfigs(serviceConfig, expectedConfig));

    // Updating the time-related items with the expected time units in older versions
    Configurations updatedServiceConfig = updateConfigurationOfOldVersions(serviceConfig);

    // Checking the parsed time-related configuration items after upgrading (should pass).
    assertTimeRelatedConfigs(updatedServiceConfig, expectedConfig);
  }

  private Configurations updateConfigurationOfOldVersions(Configurations serviceConfig) {
    Map<String, String> updatedServiceConfigMap = serviceConfig.toMap();
    for (String key : DEFAULT_TIME_UNIT_IN_OLD_VERSIONS.keySet()) {
      if (updatedServiceConfigMap.containsKey(key)) {
        String value = updatedServiceConfigMap.get(key);
        String newValue = value + DEFAULT_TIME_UNIT_IN_OLD_VERSIONS.get(key);
        updatedServiceConfigMap.put(key, newValue);
      }
    }
    return Configurations.fromMap(updatedServiceConfigMap);
  }

  private Configurations getConfigurationsWithUnits() throws URISyntaxException, IOException {
    URL resource = Resources.getResource("config-with-units.yaml");
    JsonNode yamlConfig =
        JacksonUtil.fromObjects(
            new Yaml().loadAs(Files.newInputStream(Paths.get(resource.toURI())), Map.class));
    Map<String, Object> systemConfig =
        JacksonUtil.getMap(
            yamlConfig,
            AmoroManagementConf.SYSTEM_CONFIG,
            new TypeReference<Map<String, Object>>() {});
    Map<String, Object> expandedConfigurationMap = Maps.newHashMap();
    expandConfigMap(systemConfig, "", expandedConfigurationMap);
    return Configurations.fromObjectMap(expandedConfigurationMap);
  }

  private Configurations getConfigurationsWithoutUnits() throws URISyntaxException, IOException {
    URL resource = Resources.getResource("config-without-units.yaml");
    JsonNode yamlConfig =
        JacksonUtil.fromObjects(
            new Yaml().loadAs(Files.newInputStream(Paths.get(resource.toURI())), Map.class));
    Map<String, Object> systemConfig =
        JacksonUtil.getMap(
            yamlConfig,
            AmoroManagementConf.SYSTEM_CONFIG,
            new TypeReference<Map<String, Object>>() {});
    Map<String, Object> expandedConfigurationMap = Maps.newHashMap();
    expandConfigMap(systemConfig, "", expandedConfigurationMap);
    return Configurations.fromObjectMap(expandedConfigurationMap);
  }

  private void assertTimeRelatedConfigs(
      Configurations serviceConfig, Configurations expectedConfig) {
    for (ConfigOption<Duration> configOption : TIME_RELATED_CONFIG_OPTIONS) {
      Assertions.assertEquals(expectedConfig.get(configOption), serviceConfig.get(configOption));
    }
  }

  private void assertStorageRelatedConfigs(
      Configurations serviceConfig, Configurations expectedConfig) {
    for (ConfigOption<MemorySize> configOption : STORAGE_RELATED_CONFIG_OPTIONS) {
      Assertions.assertEquals(
          expectedConfig.get(configOption),
          serviceConfig.get(AmoroManagementConf.THRIFT_MAX_MESSAGE_SIZE));
    }
  }

  private final Map<String, Object> timeRelatedConfigMapInMillisSecondsWithoutTimeUnits =
      ImmutableMap.<String, Object>builder()
          .put("refresh-external-catalogs.interval", "180000")
          .put("refresh-tables.interval", "60000")
          .put("optimizer.heart-beat-timeout", "60000")
          .put("optimizer.task-ack-timeout", "30000")
          .put("optimizer.polling-timeout", "3000")
          .put("blocker.timeout", "60000")
          .put("auto-create-tags.interval", "60000")
          .put("terminal.session.timeout", "1800000")
          .build();

  private final Map<String, Object> timeRelatedConfigMapWithTimeUnits =
      ImmutableMap.<String, Object>builder()
          .put("refresh-external-catalogs.interval", "5 min")
          .put("refresh-tables.interval", "2 min")
          .put("optimizer.heart-beat-timeout", "2 min")
          .put("optimizer.task-ack-timeout", "60 s")
          .put("optimizer.polling-timeout", "6 s")
          .put("blocker.timeout", "2 min")
          .put("auto-create-tags.interval", "2 min")
          .put("terminal.session.timeout", "30 ms")
          .build();

  private final Map<String, Object> timeRelatedConfigMapWithExpectedTimeUnitsInOldVersions =
      ImmutableMap.<String, Object>builder()
          .put("refresh-external-catalogs.interval", "5 min")
          .put("refresh-tables.interval", "2 min")
          .put("optimizer.heart-beat-timeout", "2 min")
          .put("optimizer.task-ack-timeout", "60 s")
          .put("optimizer.polling-timeout", "6 s")
          .put("blocker.timeout", "2 min")
          .put("auto-create-tags.interval", "2 min")
          .put("terminal.session.timeout", "30 min")
          .build();

  private final Map<String, Object> storageRelatedConfigMapWithoutTimeUnits =
      ImmutableMap.<String, Object>builder()
          .put("thrift-server.max-message-size", "104857600")
          .build();

  private final Map<String, Object> storageRelatedConfigMapWithTimeUnits =
      ImmutableMap.<String, Object>builder()
          .put("thrift-server.max-message-size", "200 mb")
          .build();
}

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

import static org.apache.amoro.server.AmoroServiceContainer.expandConfigMap;

public class TestAmoroManagementConf {
    private static final ConfigOption<Duration>[] TIME_RELATED_CONFIG_OPTIONS = new ConfigOption[] {
            AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL,
            AmoroManagementConf.AUTO_CREATE_TAGS_INTERVAL,
            AmoroManagementConf.REFRESH_TABLES_INTERVAL,
            AmoroManagementConf.BLOCKER_TIMEOUT,
            AmoroManagementConf.OPTIMIZER_HB_TIMEOUT,
            AmoroManagementConf.OPTIMIZER_TASK_ACK_TIMEOUT,
            AmoroManagementConf.OPTIMIZER_POLLING_TIMEOUT,
            AmoroManagementConf.TERMINAL_SESSION_TIMEOUT
    };

    private static final ConfigOption<String>[] STORAGE_RELATED_CONFIG_OPTIONS = new ConfigOption[] {
            AmoroManagementConf.THRIFT_MAX_MESSAGE_SIZE
    };

    @Test
    void testParsingAmoroManagementConfWithTimeUnits() throws Exception {
        Configurations serviceConfig = getConfigurationsWithUnits();
        assertTimeRelatedConfigs(serviceConfig);
    }

    @Test
    void testParsingAmoroManagementConfWithStorageUnits() throws Exception {
        Configurations serviceConfig = getConfigurationsWithUnits();
        assertStorageRelatedConfigs(serviceConfig);
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
    private void assertTimeRelatedConfigs(Configurations serviceConfig) {
        Configurations timeRelatedConfigsWithoutTimeUnit = Configurations.fromObjectMap(timeRelatedConfigMapInMillisSecondsWithoutTimeUnits);
        for (ConfigOption<Duration> configOption : TIME_RELATED_CONFIG_OPTIONS) {
            Assertions.assertEquals(
                    timeRelatedConfigsWithoutTimeUnit.get(configOption),
                    serviceConfig.get(configOption));
        }
    }

    private void assertStorageRelatedConfigs(Configurations serviceConfig) {
        Configurations storageRelatedConfigsWithoutTimeUnit = Configurations.fromObjectMap(storageRelatedConfigMapWithoutTimeUnits);
        for (ConfigOption<String> configOption : STORAGE_RELATED_CONFIG_OPTIONS) {
            Assertions.assertEquals(
                    MemorySize.parse(storageRelatedConfigsWithoutTimeUnit.getString(configOption)),
                    MemorySize.parse(serviceConfig.getString(AmoroManagementConf.THRIFT_MAX_MESSAGE_SIZE)));
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
                    .put("terminal.session", "180000")
                    .build();

    private final Map<String, Object> storageRelatedConfigMapWithoutTimeUnits =
            ImmutableMap.<String, Object>builder()
                    .put("thrift-server.max-message-size", "104857600")
                    .build();
}

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

package org.apache.amoro.server.config;

import static org.apache.amoro.server.AmoroServiceContainer.expandConfigMap;

import org.apache.amoro.config.shade.impl.Base64ConfigShade;
import org.apache.amoro.config.shade.utils.ConfigShadeUtils;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.io.Resources;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.utils.JacksonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;

public class TestConfigShade {
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "password";

  private static final String BASE64_CONFIG_SHADE_IDENTIFIER =
      new Base64ConfigShade().getIdentifier();

  static Stream<Arguments> encryptedValueProvider() {
    return Stream.of(Arguments.of(USERNAME, "YWRtaW4="), Arguments.of(PASSWORD, "cGFzc3dvcmQ="));
  }

  @ParameterizedTest
  @MethodSource("encryptedValueProvider")
  public void testDecryptOption(String rawValue, String expectedEncoded) {
    String actualEncoded = getBase64EncodedText(rawValue);
    Assertions.assertEquals(expectedEncoded, actualEncoded);

    String decrypted =
        ConfigShadeUtils.decryptOption(BASE64_CONFIG_SHADE_IDENTIFIER, actualEncoded);
    Assertions.assertEquals(rawValue, decrypted);
  }

  private String getBase64EncodedText(String plaintext) {
    return Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void testDecryptServiceConfigWithDefaultShade() throws Exception {
    URL resource = Resources.getResource("configs/config-default-shade.yaml");
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
    expandedConfigurationMap = ConfigShadeUtils.decryptConfig(expandedConfigurationMap);

    Assertions.assertEquals(decryptedServiceConfigWithDefaultShade, expandedConfigurationMap);
  }

  @Test
  void testDecryptServiceConfigWithBase64Shade() throws Exception {
    URL resource = Resources.getResource("configs/config-base64-shade.yaml");
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
    expandedConfigurationMap = ConfigShadeUtils.decryptConfig(expandedConfigurationMap);

    Assertions.assertEquals(decryptedServiceConfigWithBase64Shade, expandedConfigurationMap);
  }

  private final Map<String, String> decryptedServiceConfigWithDefaultShade =
      ImmutableMap.<String, String>builder()
          .put("admin-username", "admin")
          .put("admin-password", "admin")
          .put("server-bind-host", "0.0.0.0")
          .put("server-expose-host", "127.0.0.1")
          .put("shade.identifier", "default")
          .put("database.type", "mysql")
          .put("database.jdbc-driver-class", "com.mysql.cj.jdbc.Driver")
          .put(
              "database.url",
              "jdbc:mysql://127.0.0.1:3306/amoro?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&allowPublicKeyRetrieval=true&useSSL=false")
          .put("database.username", "root")
          .put("database.password", "password")
          .build();

  private final Map<String, String> decryptedServiceConfigWithBase64Shade =
      ImmutableMap.<String, String>builder()
          .put("admin-username", "admin")
          .put("admin-password", "admin")
          .put("server-bind-host", "0.0.0.0")
          .put("server-expose-host", "127.0.0.1")
          .put("shade.identifier", "base64")
          .put("shade.sensitive-keywords", "admin-password;database.password")
          .put("database.type", "mysql")
          .put("database.jdbc-driver-class", "com.mysql.cj.jdbc.Driver")
          .put(
              "database.url",
              "jdbc:mysql://127.0.0.1:3306/amoro?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&allowPublicKeyRetrieval=true&useSSL=false")
          .put("database.username", "root")
          .put("database.password", "password")
          .build();
}

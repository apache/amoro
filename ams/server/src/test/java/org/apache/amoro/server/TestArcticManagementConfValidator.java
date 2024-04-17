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

import org.apache.amoro.api.config.Configurations;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TestArcticManagementConfValidator {

  @Test
  public void testBlankServerExposeHost() {
    Configurations configurations = new Configurations();
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));
  }

  @Test
  public void testValidateServerExposeHost() {
    Configurations configurations = new Configurations();
    configurations.setString(ArcticManagementConf.SERVER_EXPOSE_HOST, "0.0.0.0");
    Assert.assertThrows(
        RuntimeException.class, () -> ArcticManagementConfValidator.validateConfig(configurations));
  }

  public static Stream<Arguments> testValidateDBConfig() {
    return Stream.of(Arguments.of("mysql"), Arguments.of("postgres"));
  }

  @ParameterizedTest
  @MethodSource
  public void testValidateDBConfig(String dbType) {
    Configurations configurations = new Configurations();
    configurations.setString(ArcticManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    configurations.setString(ArcticManagementConf.DB_TYPE, dbType);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));

    configurations.setString(ArcticManagementConf.DB_PASSWORD, "123456");
    configurations.setString(ArcticManagementConf.DB_USER_NAME, "");
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));

    configurations.removeConfig(ArcticManagementConf.DB_PASSWORD);
    configurations.setString(ArcticManagementConf.DB_USER_NAME, "root");
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));

    configurations.setString(ArcticManagementConf.DB_PASSWORD, "123456");
    ArcticManagementConfValidator.validateConfig(configurations);
  }

  @Test
  public void testValidateHAConfig() {
    Configurations configurations = new Configurations();
    configurations.setString(ArcticManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    configurations.setBoolean(ArcticManagementConf.HA_ENABLE, true);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));

    configurations.setString(ArcticManagementConf.HA_ZOOKEEPER_ADDRESS, "127.0.0.1:2181");
    ArcticManagementConfValidator.validateConfig(configurations);
  }

  @Test
  public void testValidateTerminalBackend() {
    Configurations configurations = new Configurations();
    configurations.setString(ArcticManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");

    configurations.setString(ArcticManagementConf.TERMINAL_BACKEND, "invalid_terminal_backend");
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));

    configurations.setString(ArcticManagementConf.TERMINAL_BACKEND, "local");
    ArcticManagementConfValidator.validateConfig(configurations);

    configurations.setString(ArcticManagementConf.TERMINAL_BACKEND, "kyuubi");
    ArcticManagementConfValidator.validateConfig(configurations);

    configurations.setString(ArcticManagementConf.TERMINAL_BACKEND, "custom");
    ArcticManagementConfValidator.validateConfig(configurations);
  }

  @Test
  public void testValidateThreadCount() {
    Configurations configurations = new Configurations();
    configurations.setString(ArcticManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");

    configurations.setInteger(ArcticManagementConf.REFRESH_TABLES_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(ArcticManagementConf.REFRESH_TABLES_THREAD_COUNT, 10);
    ArcticManagementConfValidator.validateConfig(configurations);

    configurations.setInteger(ArcticManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(ArcticManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT, 10);
    ArcticManagementConfValidator.validateConfig(configurations);

    configurations.setBoolean(ArcticManagementConf.EXPIRE_SNAPSHOTS_ENABLED, true);
    configurations.setInteger(ArcticManagementConf.EXPIRE_SNAPSHOTS_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(ArcticManagementConf.EXPIRE_SNAPSHOTS_THREAD_COUNT, 10);
    ArcticManagementConfValidator.validateConfig(configurations);

    configurations.setBoolean(ArcticManagementConf.CLEAN_ORPHAN_FILES_ENABLED, true);
    configurations.setInteger(ArcticManagementConf.CLEAN_ORPHAN_FILES_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(ArcticManagementConf.CLEAN_ORPHAN_FILES_THREAD_COUNT, 10);
    ArcticManagementConfValidator.validateConfig(configurations);

    configurations.setBoolean(ArcticManagementConf.SYNC_HIVE_TABLES_ENABLED, true);
    configurations.setInteger(ArcticManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> ArcticManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(ArcticManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT, 10);
    ArcticManagementConfValidator.validateConfig(configurations);
  }
}

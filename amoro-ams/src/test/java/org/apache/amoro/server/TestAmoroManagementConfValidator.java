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

import org.apache.amoro.config.Configurations;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TestAmoroManagementConfValidator {

  @Test
  public void testBlankServerExposeHost() {
    Configurations configurations = new Configurations();
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));
  }

  @Test
  public void testValidateServerExposeHost() {
    Configurations configurations = new Configurations();
    configurations.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "0.0.0.0");
    Assert.assertThrows(
        RuntimeException.class, () -> AmoroManagementConfValidator.validateConfig(configurations));
  }

  public static Stream<Arguments> testValidateDBConfig() {
    return Stream.of(Arguments.of("mysql"), Arguments.of("postgres"));
  }

  @ParameterizedTest
  @MethodSource
  public void testValidateDBConfig(String dbType) {
    Configurations configurations = new Configurations();
    configurations.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    configurations.setString(AmoroManagementConf.DB_TYPE, dbType);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));

    configurations.setString(AmoroManagementConf.DB_PASSWORD, "123456");
    configurations.setString(AmoroManagementConf.DB_USER_NAME, "");
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));

    configurations.removeConfig(AmoroManagementConf.DB_PASSWORD);
    configurations.setString(AmoroManagementConf.DB_USER_NAME, "root");
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));

    configurations.setString(AmoroManagementConf.DB_PASSWORD, "123456");
    AmoroManagementConfValidator.validateConfig(configurations);
  }

  @Test
  public void testValidateHAConfig() {
    Configurations configurations = new Configurations();
    configurations.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    configurations.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));

    configurations.setString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, "127.0.0.1:2181");
    AmoroManagementConfValidator.validateConfig(configurations);
  }

  @Test
  public void testValidateTerminalBackend() {
    Configurations configurations = new Configurations();
    configurations.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");

    configurations.setString(AmoroManagementConf.TERMINAL_BACKEND, "invalid_terminal_backend");
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));

    configurations.setString(AmoroManagementConf.TERMINAL_BACKEND, "local");
    AmoroManagementConfValidator.validateConfig(configurations);

    configurations.setString(AmoroManagementConf.TERMINAL_BACKEND, "kyuubi");
    AmoroManagementConfValidator.validateConfig(configurations);

    configurations.setString(AmoroManagementConf.TERMINAL_BACKEND, "custom");
    AmoroManagementConfValidator.validateConfig(configurations);
  }

  @Test
  public void testValidateThreadCount() {
    Configurations configurations = new Configurations();
    configurations.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");

    configurations.setInteger(AmoroManagementConf.REFRESH_TABLES_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(AmoroManagementConf.REFRESH_TABLES_THREAD_COUNT, 10);
    AmoroManagementConfValidator.validateConfig(configurations);

    configurations.setInteger(AmoroManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(AmoroManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT, 10);
    AmoroManagementConfValidator.validateConfig(configurations);

    configurations.setBoolean(AmoroManagementConf.EXPIRE_SNAPSHOTS_ENABLED, true);
    configurations.setInteger(AmoroManagementConf.EXPIRE_SNAPSHOTS_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(AmoroManagementConf.EXPIRE_SNAPSHOTS_THREAD_COUNT, 10);
    AmoroManagementConfValidator.validateConfig(configurations);

    configurations.setBoolean(AmoroManagementConf.CLEAN_ORPHAN_FILES_ENABLED, true);
    configurations.setInteger(AmoroManagementConf.CLEAN_ORPHAN_FILES_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(AmoroManagementConf.CLEAN_ORPHAN_FILES_THREAD_COUNT, 10);
    AmoroManagementConfValidator.validateConfig(configurations);

    configurations.setBoolean(AmoroManagementConf.SYNC_HIVE_TABLES_ENABLED, true);
    configurations.setInteger(AmoroManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT, -1);
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> AmoroManagementConfValidator.validateConfig(configurations));
    configurations.setInteger(AmoroManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT, 10);
    AmoroManagementConfValidator.validateConfig(configurations);
  }
}

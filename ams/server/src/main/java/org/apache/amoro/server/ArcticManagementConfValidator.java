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

import org.apache.amoro.api.config.ConfigOption;
import org.apache.amoro.api.config.Configurations;
import org.apache.amoro.server.dashboard.utils.AmsUtil;

import java.net.InetAddress;

public class ArcticManagementConfValidator {
  public static void validateConfig(Configurations configurations) {
    // SERVER_EXPOSE_HOST config
    if ("".equals(configurations.getString(ArcticManagementConf.SERVER_EXPOSE_HOST))) {
      throw new IllegalArgumentException(
          "configuration " + ArcticManagementConf.SERVER_EXPOSE_HOST.key() + " must be set");
    }
    InetAddress inetAddress =
        AmsUtil.lookForBindHost(configurations.getString(ArcticManagementConf.SERVER_EXPOSE_HOST));
    configurations.setString(ArcticManagementConf.SERVER_EXPOSE_HOST, inetAddress.getHostAddress());

    // mysql or postgres config
    if (ArcticManagementConf.DB_TYPE_MYSQL.equalsIgnoreCase(
            configurations.getString(ArcticManagementConf.DB_TYPE))
        || ArcticManagementConf.DB_TYPE_POSTGRES.equalsIgnoreCase(
            configurations.getString(ArcticManagementConf.DB_TYPE))) {
      if ("".equals(configurations.getString(ArcticManagementConf.DB_PASSWORD))
          || "".equals(configurations.getString(ArcticManagementConf.DB_USER_NAME))) {
        throw new IllegalArgumentException(
            "username and password must be configured if the database type is mysql or postgres");
      }
    }

    // HA config
    if (configurations.getBoolean(ArcticManagementConf.HA_ENABLE)) {
      if ("".equals(configurations.getString(ArcticManagementConf.HA_ZOOKEEPER_ADDRESS))) {
        throw new IllegalArgumentException(
            ArcticManagementConf.HA_ZOOKEEPER_ADDRESS.key()
                + " must be configured when you enable "
                + "the ams high availability");
      }
    }
    // terminal config
    String terminalBackend =
        configurations.getString(ArcticManagementConf.TERMINAL_BACKEND).toLowerCase();
    if (!ArcticManagementConf.TERMINAL_BACKEND_VALUES.contains(terminalBackend)) {
      throw new IllegalArgumentException(
          String.format(
              "Illegal terminal implement: %s, local, kyuubi, custom is available",
              terminalBackend));
    }

    validateThreadCount(configurations, ArcticManagementConf.REFRESH_TABLES_THREAD_COUNT);
    validateThreadCount(configurations, ArcticManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT);

    if (configurations.getBoolean(ArcticManagementConf.EXPIRE_SNAPSHOTS_ENABLED)) {
      validateThreadCount(configurations, ArcticManagementConf.EXPIRE_SNAPSHOTS_THREAD_COUNT);
    }

    if (configurations.getBoolean(ArcticManagementConf.CLEAN_ORPHAN_FILES_ENABLED)) {
      validateThreadCount(configurations, ArcticManagementConf.CLEAN_ORPHAN_FILES_THREAD_COUNT);
    }
    if (configurations.getBoolean(ArcticManagementConf.SYNC_HIVE_TABLES_ENABLED)) {
      validateThreadCount(configurations, ArcticManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT);
    }
  }

  private static void validateThreadCount(
      Configurations configurations, ConfigOption<Integer> config) {
    int threadCount = configurations.getInteger(config);
    if (threadCount <= 0) {
      throw new IllegalArgumentException(
          String.format(
              "%s(%s) must > 0, actual value = %d",
              config.key(), config.description(), threadCount));
    }
  }
}

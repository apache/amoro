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
import org.apache.amoro.server.dashboard.utils.AmsUtil;

import java.net.InetAddress;

public class AmoroManagementConfValidator {
  public static void validateConfig(Configurations configurations) {
    // SERVER_EXPOSE_HOST config
    if ("".equals(configurations.getString(AmoroManagementConf.SERVER_EXPOSE_HOST))) {
      throw new IllegalArgumentException(
          "configuration " + AmoroManagementConf.SERVER_EXPOSE_HOST.key() + " must be set");
    }
    InetAddress inetAddress =
        AmsUtil.lookForBindHost(configurations.getString(AmoroManagementConf.SERVER_EXPOSE_HOST));
    configurations.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, inetAddress.getHostAddress());

    // mysql or postgres config
    if (AmoroManagementConf.DB_TYPE_MYSQL.equalsIgnoreCase(
            configurations.getString(AmoroManagementConf.DB_TYPE))
        || AmoroManagementConf.DB_TYPE_POSTGRES.equalsIgnoreCase(
            configurations.getString(AmoroManagementConf.DB_TYPE))) {
      if ("".equals(configurations.getString(AmoroManagementConf.DB_PASSWORD))
          || "".equals(configurations.getString(AmoroManagementConf.DB_USER_NAME))) {
        throw new IllegalArgumentException(
            "username and password must be configured if the database type is mysql or postgres");
      }
    }

    // HA config
    if (configurations.getBoolean(AmoroManagementConf.HA_ENABLE)) {
      if ("".equals(configurations.getString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS))) {
        throw new IllegalArgumentException(
            AmoroManagementConf.HA_ZOOKEEPER_ADDRESS.key()
                + " must be configured when you enable "
                + "the ams high availability");
      }
    }
    // terminal config
    String terminalBackend =
        configurations.getString(AmoroManagementConf.TERMINAL_BACKEND).toLowerCase();
    if (!AmoroManagementConf.TERMINAL_BACKEND_VALUES.contains(terminalBackend)) {
      throw new IllegalArgumentException(
          String.format(
              "Illegal terminal implement: %s, local, kyuubi, custom is available",
              terminalBackend));
    }

    validateThreadCount(configurations, AmoroManagementConf.REFRESH_TABLES_THREAD_COUNT);
    validateThreadCount(configurations, AmoroManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT);

    if (configurations.getBoolean(AmoroManagementConf.EXPIRE_SNAPSHOTS_ENABLED)
        || configurations.getBoolean(AmoroManagementConf.CLEAN_ORPHAN_FILES_ENABLED)) {
      validateThreadCount(configurations, AmoroManagementConf.CLEAN_TABLE_DATA_THREAD_COUNT);
    }

    if (configurations.getBoolean(AmoroManagementConf.SYNC_HIVE_TABLES_ENABLED)) {
      validateThreadCount(configurations, AmoroManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT);
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

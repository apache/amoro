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

package org.apache.amoro.server.ha;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

/** Build DATABASE HA configurations against the shared H2 persistence fixture. */
public final class HATestConfigs {
  private HATestConfigs() {}

  /**
   * Build unified DataBase HA configuration for containers/AMS. Use short heartbeat and lease TTL
   * to speed up tests.
   *
   * @param heartbeatMillis Heartbeat interval in milliseconds.
   * @param ttlMillis Lease TTL in milliseconds.
   * @param exposeHost server-expose-host.
   * @param tablePort Table Thrift port.
   * @param optimizingPort Optimizing Thrift port.
   * @param httpPort HTTP port.
   * @return Configurations including H2 URL, ha.enabled=true, ha.type=database, etc.
   */
  public static Configurations buildDataBaseHAConfig(
      String clusterName,
      int heartbeatMillis,
      int ttlMillis,
      String exposeHost,
      int tablePort,
      int optimizingPort,
      int httpPort) {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.DB_TYPE, AmoroManagementConf.DB_TYPE_H2);
    conf.set(AmoroManagementConf.DB_DRIVER_CLASS_NAME, "org.h2.Driver");
    conf.set(AmoroManagementConf.DB_CONNECTION_URL, detectH2UrlFromSqlSessionFactory());

    // HA basic switches.
    conf.set(AmoroManagementConf.HA_ENABLE, true);
    conf.setString(AmoroManagementConf.HA_TYPE, AmoroManagementConf.HA_TYPE_DATABASE);
    conf.setString(
        AmoroManagementConf.HA_HEARTBEAT_INTERVAL.key(), String.valueOf(heartbeatMillis));
    conf.setString(AmoroManagementConf.HA_LEASE_TTL.key(), String.valueOf(ttlMillis));
    conf.setString(AmoroManagementConf.HA_CLUSTER_NAME, clusterName);

    // Exposed host and ports.
    conf.set(AmoroManagementConf.SERVER_EXPOSE_HOST, exposeHost);
    conf.set(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, tablePort);
    conf.set(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, optimizingPort);
    conf.set(AmoroManagementConf.HTTP_SERVER_PORT, httpPort);

    return conf;
  }

  /**
   * Detect the JDBC URL used by H2Persistence from SqlSessionFactoryProvider connection metadata.
   */
  public static String detectH2UrlFromSqlSessionFactory() {
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      try (Connection conn = session.getConnection()) {
        return conn.getMetaData().getURL();
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot read the shared H2 test database URL", e);
    }
  }

  /** Generate a random port in the range 14000-18000 to avoid conflicts. */
  public static int randomEphemeralPort() {
    return new Random().nextInt(4000) + 14000;
  }
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.DataSourceFactory;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.Map;

/**
 * Integration tests for {@link DynamicConfigStore} and {@link DbConfigurationManager}.
 *
 * <p>This test uses an in-memory Derby database together with {@link DataSourceFactory} and {@link
 * SqlSessionFactoryProvider} so that the real {@code dynamic_conf} table is created and accessed
 * via MyBatis.
 */
public class DynamicConfigIntegrationIT {

  private Configurations baseConfig;
  private DataSource dataSource;
  private DynamicConfigStore store;

  @Before
  public void setUp() throws Exception {
    // use in-memory Derby and let DataSourceFactory create all AMS tables
    baseConfig = new Configurations();
    baseConfig.setString(AmoroManagementConf.DB_TYPE, AmoroManagementConf.DB_TYPE_DERBY);
    baseConfig.setString(
        AmoroManagementConf.DB_CONNECTION_URL, "jdbc:derby:memory:dynamic_config_it;create=true");
    baseConfig.setBoolean(AmoroManagementConf.DB_AUTO_CREATE_TABLES, true);

    dataSource = DataSourceFactory.createDataSource(baseConfig);
    SqlSessionFactoryProvider.getInstance().init(dataSource);

    store = new DynamicConfigStore();
  }

  @After
  public void tearDown() throws Exception {
    if (dataSource != null) {
      try (Connection conn = dataSource.getConnection()) {
        // closing the last connection is enough for in-memory Derby
        conn.close();
      }
    }
  }

  /**
   * Verify that {@link DynamicConfigStore} can load AMS and plugin level overrides from the {@code
   * dynamic_conf} table.
   */
  @Test
  public void testDynamicConfigStoreLoadOverrides() throws Exception {
    try (Connection conn = dataSource.getConnection()) {
      // AMS level override
      try (PreparedStatement ps =
          conn.prepareStatement(
              "INSERT INTO dynamic_conf(conf_group, plugin_name, conf_key, conf_value) "
                  + "VALUES(?, ?, ?, ?)")) {
        ps.setString(1, "AMS");
        ps.setString(2, null);
        ps.setString(3, "test.ams.key");
        ps.setString(4, "v1");
        ps.executeUpdate();
      }

      // plugin level override
      try (PreparedStatement ps =
          conn.prepareStatement(
              "INSERT INTO dynamic_conf(conf_group, plugin_name, conf_key, conf_value) "
                  + "VALUES(?, ?, ?, ?)")) {
        ps.setString(1, "PLUGIN_metric-reporters");
        ps.setString(2, "test-metric");
        ps.setString(3, "metric.level");
        ps.setString(4, "INFO");
        ps.executeUpdate();
      }

      conn.commit();
    }

    Map<String, String> serverOverrides = store.loadServerOverrides();
    assertNotNull(serverOverrides);
    assertEquals("v1", serverOverrides.get("test.ams.key"));

    Map<String, String> pluginOverrides =
        store.loadPluginOverrides("metric-reporters", "test-metric");
    assertNotNull(pluginOverrides);
    assertEquals("INFO", pluginOverrides.get("metric.level"));
  }

  /**
   * Verify that {@link DbConfigurationManager} refreshes its internal cache when {@code
   * dynamic_conf} is updated.
   */
  @Test
  public void testDbConfigurationManagerRefreshServerOverrides() throws Exception {
    DbConfigurationManager manager = new DbConfigurationManager(store, Duration.ofMillis(50L));

    try {
      manager.start();

      // at the very beginning there should be no overrides
      Configurations initial = manager.getServerConfigurations();
      assertNotNull(initial);
      assertFalse(initial.containsKey("dynamic.ams.key"));

      // insert an AMS level override directly via JDBC
      try (Connection conn = dataSource.getConnection();
          PreparedStatement ps =
              conn.prepareStatement(
                  "INSERT INTO dynamic_conf(conf_group, plugin_name, conf_key, conf_value) "
                      + "VALUES(?, ?, ?, ?)")) {
        ps.setString(1, "AMS");
        ps.setString(2, null);
        ps.setString(3, "dynamic.ams.key");
        ps.setString(4, "v1");
        ps.executeUpdate();
        conn.commit();
      }

      // wait for the scheduled refresh to pick up the new value
      Thread.sleep(200L);

      Configurations afterInsert = manager.getServerConfigurations();
      assertEquals("v1", afterInsert.toMap().get("dynamic.ams.key"));

      // update the value to v2
      try (Connection conn = dataSource.getConnection();
          PreparedStatement ps =
              conn.prepareStatement(
                  "UPDATE dynamic_conf SET conf_value = ? "
                      + "WHERE conf_group = ? AND plugin_name IS NULL AND conf_key = ?")) {
        ps.setString(1, "v2");
        ps.setString(2, "AMS");
        ps.setString(3, "dynamic.ams.key");
        assertEquals(1, ps.executeUpdate());
        conn.commit();
      }

      // wait for another refresh round
      Thread.sleep(200L);

      Configurations afterUpdate = manager.getServerConfigurations();
      assertEquals("v2", afterUpdate.toMap().get("dynamic.ams.key"));
    } finally {
      manager.stop();
    }
  }
}

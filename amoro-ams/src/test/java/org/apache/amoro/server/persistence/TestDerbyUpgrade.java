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

package org.apache.amoro.server.persistence;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class TestDerbyUpgrade {

  @Test
  public void testAddDatabasePropertiesColumn() throws Exception {
    String databaseName = "upgrade_" + UUID.randomUUID().toString().replace("-", "");
    String connectionUrl = "jdbc:derby:memory:" + databaseName + ";create=true";

    try (Connection connection = DriverManager.getConnection(connectionUrl);
        Statement statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE database_metadata ("
              + "catalog_name VARCHAR(64) NOT NULL, "
              + "db_name VARCHAR(128) NOT NULL, "
              + "table_count INT NOT NULL DEFAULT 0, "
              + "PRIMARY KEY (catalog_name, db_name))");
      statement.execute(
          "INSERT INTO database_metadata(catalog_name, db_name) VALUES ('catalog', 'database')");

      try (InputStream script =
          TestDerbyUpgrade.class.getClassLoader().getResourceAsStream("derby/upgrade.sql")) {
        Assertions.assertNotNull(script);
        new ScriptRunner(connection)
            .runScript(new InputStreamReader(script, StandardCharsets.UTF_8));
      }

      statement.execute(
          "UPDATE database_metadata SET properties = '{\"location\":\"file:/warehouse\"}'"
              + " WHERE catalog_name = 'catalog' AND db_name = 'database'");
      try (ResultSet resultSet =
          statement.executeQuery(
              "SELECT properties FROM database_metadata"
                  + " WHERE catalog_name = 'catalog' AND db_name = 'database'")) {
        Assertions.assertTrue(resultSet.next());
        Assertions.assertEquals("{\"location\":\"file:/warehouse\"}", resultSet.getString(1));
      }
      connection.commit();
    } finally {
      try {
        DriverManager.getConnection("jdbc:derby:memory:" + databaseName + ";drop=true");
        Assertions.fail("Derby did not drop the database");
      } catch (SQLException e) {
        Assertions.assertEquals("08006", e.getSQLState());
      }
    }
  }
}

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

import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Test the catalog manager with SQLite persistence. */
public class SQLiteCatalogManagerTest extends SQLiteAMSManagerTestBase {

  @Test
  public void testSQLiteConnection() {
    try (SqlSession sqlSession = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      Assert.assertNotNull("SqlSession should not be null", sqlSession);

      // Test connection
      try (Connection connection = sqlSession.getConnection()) {
        Assert.assertNotNull("Connection should not be null", connection);

        try (Statement statement = connection.createStatement()) {
          // Check that we can query SQLite metadata
          String query = "SELECT name FROM sqlite_master WHERE type='table'";
          try (ResultSet rs = statement.executeQuery(query)) {
            // Just check that the query executes without error
            int tableCount = 0;
            while (rs.next()) {
              tableCount++;
            }
            Assert.assertTrue("Database should have tables", tableCount > 0);
          }
        }
      }
    } catch (Exception e) {
      Assert.fail("Exception should not be thrown: " + e.getMessage());
    }
  }

  @Test
  public void testSQLiteTableStructure() {
    // This test just verifies that the SQLite database has been properly initialized with the
    // expected tables
    try (SqlSession sqlSession = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      try (Connection connection = sqlSession.getConnection()) {
        try (Statement statement = connection.createStatement()) {
          // Test that we can query catalog_metadata table
          String query = "SELECT COUNT(*) FROM catalog_metadata";
          try (ResultSet rs = statement.executeQuery(query)) {
            Assert.assertTrue("Should be able to query catalog_metadata table", rs.next());
          }

          // Test that we can query table_metadata table
          query = "SELECT COUNT(*) FROM table_metadata";
          try (ResultSet rs = statement.executeQuery(query)) {
            Assert.assertTrue("Should be able to query table_metadata table", rs.next());
          }

          // Test that we can query table_runtime table
          query = "SELECT COUNT(*) FROM table_runtime";
          try (ResultSet rs = statement.executeQuery(query)) {
            Assert.assertTrue("Should be able to query table_runtime table", rs.next());
          }

          // Verify the schema by checking a specific column in a table
          query = "PRAGMA table_info(catalog_metadata)";
          try (ResultSet rs = statement.executeQuery(query)) {
            boolean hasCatalogName = false;
            while (rs.next()) {
              if ("catalog_name".equals(rs.getString("name"))) {
                hasCatalogName = true;
                break;
              }
            }
            Assert.assertTrue("catalog_metadata should have a catalog_name column", hasCatalogName);
          }
        }
      }
    } catch (Exception e) {
      Assert.fail("Exception should not be thrown: " + e.getMessage());
    }
  }
}

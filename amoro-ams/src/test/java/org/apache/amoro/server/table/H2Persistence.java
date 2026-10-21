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

package org.apache.amoro.server.table;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.DataSourceFactory;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSession;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class H2Persistence extends ExternalResource {
  private static final Logger LOG = LoggerFactory.getLogger(H2Persistence.class);

  private static final TemporaryFolder SINGLETON_FOLDER;

  private static final int TRUNCATE_RETRY_NUM = 3;

  static {
    try {
      SINGLETON_FOLDER = new TemporaryFolder();
      SINGLETON_FOLDER.create();
      String h2FilePath = SINGLETON_FOLDER.newFolder("h2").getPath();
      String h2Url = String.format("jdbc:h2:file:%s/ams", h2FilePath);
      Configurations configurations = new Configurations();
      configurations.set(AmoroManagementConf.DB_CONNECTION_URL, h2Url);
      configurations.set(AmoroManagementConf.DB_TYPE, AmoroManagementConf.DB_TYPE_H2);
      configurations.set(AmoroManagementConf.DB_DRIVER_CLASS_NAME, "org.h2.Driver");
      BasicDataSource ds = (BasicDataSource) DataSourceFactory.createDataSource(configurations);
      SqlSessionFactoryProvider.getInstance().init(ds);
      LOG.info("Initialized H2 persistence with url: {}", h2Url);
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      ds.close();
                    } catch (SQLException e) {
                      LOG.warn("Failed to close H2 data source", e);
                    }
                    SINGLETON_FOLDER.delete();
                    LOG.info("Deleted H2 persistence resources.");
                  }));
      truncateAllTables();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void after() {
    truncateAllTables();
  }

  private static void truncateAllTables() {
    try (SqlSession sqlSession = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      try (Connection connection = sqlSession.getConnection()) {
        try (Statement statement = connection.createStatement()) {
          List<String> tableList = Lists.newArrayList();
          try (ResultSet rs =
              connection
                  .getMetaData()
                  .getTables(null, connection.getSchema(), null, new String[] {"TABLE"})) {
            while (rs.next()) {
              tableList.add(rs.getString("TABLE_NAME"));
            }
          }
          for (String table : tableList) {
            int retry = TRUNCATE_RETRY_NUM;
            while (true) {
              try {
                statement.execute(
                    "TRUNCATE TABLE \"" + table.replace("\"", "\"\"") + "\" CONTINUE IDENTITY");
                break;
              } catch (SQLException e) {
                if (retry > 0) {
                  LOG.warn("Failed to truncate {}, retry later.", table, e);
                  retry--;
                } else {
                  throw e;
                }
              }
            }
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Clear table failed", e);
    }
  }
}

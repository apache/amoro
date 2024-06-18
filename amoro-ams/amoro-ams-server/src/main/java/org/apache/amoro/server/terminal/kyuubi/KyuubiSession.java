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

package org.apache.amoro.server.terminal.kyuubi;

import org.apache.amoro.server.terminal.JDBCResultSet;
import org.apache.amoro.server.terminal.TerminalSession;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class KyuubiSession implements TerminalSession {

  final List<String> logs = Lists.newArrayList();
  final Connection connection;

  private volatile String currentCatalog;
  Map<String, String> sessionConf;

  public KyuubiSession(Connection connection, List<String> logs, Map<String, String> sessionConf) {
    this.logs.addAll(logs);
    this.connection = connection;
    this.sessionConf = sessionConf;
  }

  @Override
  public Map<String, String> configs() {
    return this.sessionConf;
  }

  @Override
  public ResultSet executeStatement(String catalog, String statement) {
    if (currentCatalog == null || !currentCatalog.equalsIgnoreCase(catalog)) {
      String useCatalog;
      if (TerminalSession.canUseSparkSessionCatalog(sessionConf, catalog)) {
        logs.add(
            String.format(
                "current catalog is %s, "
                    + "since it's a hive type catalog and can use spark session catalog, "
                    + "switch to spark_catalog before execution",
                currentCatalog));
        useCatalog = "spark_catalog";
      } else {
        logs.add(
            String.format(
                "current catalog is %s, switch to %s before execution", currentCatalog, catalog));
        useCatalog = catalog;
      }
      try {
        connection.setCatalog(useCatalog);
      } catch (SQLException e) {
        throw new RuntimeException(
            String.format("Failed to set catalog %s on the connection", useCatalog), e);
      }
      this.currentCatalog = catalog;
    }
    java.sql.ResultSet rs = null;
    Statement sts = null;
    try {
      sts = connection.createStatement();
      boolean withRs = sts.execute(statement);
      if (withRs) {
        rs = sts.getResultSet();
      }
    } catch (SQLException e) {
      throw new RuntimeException("error when execute sql:" + statement, e);
    }

    return new JDBCResultSet(rs, sts);
  }

  @Override
  public synchronized List<String> logs() {
    List<String> logs = Lists.newArrayList(this.logs);
    this.logs.clear();
    return logs;
  }

  @Override
  public boolean active() {
    try {
      execute("SELECT 1");
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  @Override
  public void release() {
    try {
      this.connection.close();
    } catch (SQLException e) {
      this.logs.add("error when release connection." + e);
    }
  }

  private void execute(String sql) {
    try (Statement sts = connection.createStatement()) {
      sts.execute(sql);
    } catch (SQLException e) {
      throw new RuntimeException("error when execute sql:" + sql, e);
    }
  }
}

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

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.eclipse.jetty.server.session.DatabaseAdaptor;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.JDBCSessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Duration;

public class HttpSessionHandlerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(HttpSessionHandlerFactory.class);

  public static SessionHandler createSessionHandler(
      DataSource dataSource, Configurations configurations) {
    DatabaseAdaptor adaptor = new DatabaseAdaptor();
    adaptor.setDatasource(dataSource);

    JDBCSessionDataStore dataStore = new JDBCSessionDataStore();
    dataStore.setDatabaseAdaptor(adaptor);
    dataStore.setSessionTableSchema(new AmoroSessionTableSchema(dataSource));

    SessionHandler handler = new SessionHandler();
    DefaultSessionCache cache = new DefaultSessionCache(handler);
    cache.setSessionDataStore(dataStore);
    // set session timeout
    Duration sessionTimeout = configurations.get(AmoroManagementConf.HTTP_SERVER_SESSION_TIMEOUT);
    handler.setMaxInactiveInterval((int) sessionTimeout.getSeconds());
    handler.setSessionCache(cache);
    return handler;
  }

  private static class AmoroSessionTableSchema extends JDBCSessionDataStore.SessionTableSchema {
    private final DataSource dataSource;

    public AmoroSessionTableSchema(DataSource dataSource) {
      setTableName("http_session");
      setIdColumn("session_id");
      setContextPathColumn("context_path");
      setVirtualHostColumn("virtual_host");
      setLastNodeColumn("last_node");
      setAccessTimeColumn("access_time");
      setLastAccessTimeColumn("last_access_time");
      setCreateTimeColumn("create_time");
      setCookieTimeColumn("cookie_time");
      setLastSavedTimeColumn("last_save_time");
      setExpiryTimeColumn("expiry_time");
      setMaxIntervalColumn("max_interval");
      setMapColumn("data_store");
      this.dataSource = dataSource;
    }

    @Override
    public void prepareTables() throws SQLException {
      LOG.info("Skip jetty prepare http session tables.");
      try (Connection connection = dataSource.getConnection()) {
        // make the id table
        connection.setAutoCommit(true);
        DatabaseMetaData metaData = connection.getMetaData();
        _dbAdaptor.adaptTo(metaData);
      }
    }
  }
}

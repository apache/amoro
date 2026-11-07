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

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.DatabaseMetadata;
import org.apache.amoro.server.dashboard.model.PlatformFileInfo;
import org.apache.amoro.server.persistence.converter.Object2ByteArrayConvert;
import org.apache.amoro.server.persistence.mapper.BucketAssignMapper;
import org.apache.amoro.server.persistence.mapper.HaLeaseMapper;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.PlatformFileMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.eclipse.jetty.server.session.SessionContext;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.server.session.SessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestH2Persistence {
  @TempDir Path temporaryDirectory;

  private Configurations configurations;
  private BasicDataSource dataSource;
  private SqlSessionFactory sessionFactory;

  @BeforeEach
  public void initialize() throws Exception {
    configurations = new Configurations();
    configurations.set(AmoroManagementConf.DB_CONNECTION_URL, "jdbc:h2:mem:" + UUID.randomUUID());
    openDataSource();
  }

  @AfterEach
  public void close() throws Exception {
    PageHelper.clearPage();
    dataSource.close();
  }

  private void openDataSource() throws Exception {
    dataSource = (BasicDataSource) DataSourceFactory.createDataSource(configurations);
    sessionFactory = SqlSessionFactoryProvider.buildSqlSessionFactory(dataSource);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void testRejectRetiredDatabaseBeforeOpeningConnection(boolean autoCreateTables) {
    Configurations legacy = new Configurations();
    legacy.set(AmoroManagementConf.DB_TYPE, "derby");
    legacy.set(AmoroManagementConf.DB_DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
    Path legacyPath = temporaryDirectory.resolve("legacy");
    legacy.set(AmoroManagementConf.DB_CONNECTION_URL, "jdbc:derby:" + legacyPath + ";create=true");
    legacy.set(AmoroManagementConf.DB_AUTO_CREATE_TABLES, autoCreateTables);
    IllegalArgumentException error =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> DataSourceFactory.createDataSource(legacy));
    Assertions.assertTrue(error.getMessage().contains("derby"));
    Assertions.assertTrue(error.getMessage().contains("h2, mysql, postgres"));
    Assertions.assertFalse(java.nio.file.Files.exists(legacyPath));
  }

  @Test
  public void testDefaultDatabaseAndSchema() throws Exception {
    Configurations defaults = new Configurations();
    Assertions.assertEquals("h2", defaults.getString(AmoroManagementConf.DB_TYPE));
    Assertions.assertEquals(
        "org.h2.Driver", defaults.getString(AmoroManagementConf.DB_DRIVER_CLASS_NAME));
    Assertions.assertEquals(
        "jdbc:h2:file:/tmp/amoro/h2/ams",
        defaults.getString(AmoroManagementConf.DB_CONNECTION_URL));
    Assertions.assertEquals("h2", sessionFactory.getConfiguration().getDatabaseId());
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      for (String table :
          new String[] {
            "CATALOG_METADATA",
            "TABLE_RUNTIME_STATE",
            "OPTIMIZING_PROCESS_STATE",
            "HTTP_SESSION",
            "HA_LEASE",
            "BUCKET_ASSIGNMENTS"
          }) {
        try (ResultSet tables =
            connection
                .getMetaData()
                .getTables(null, connection.getSchema(), table, new String[] {"TABLE"})) {
          Assertions.assertTrue(tables.next(), table);
        }
      }
      Assertions.assertEquals("ROOT", connection.getMetaData().getUserName());
    }
  }

  @Test
  public void testFileDatabaseSurvivesReinitialization() throws Exception {
    dataSource.close();
    configurations.set(
        AmoroManagementConf.DB_CONNECTION_URL, "jdbc:h2:file:" + temporaryDirectory.resolve("ams"));
    openDataSource();
    try (SqlSession session = sessionFactory.openSession(true)) {
      session
          .getMapper(TableMetaMapper.class)
          .insertDatabase("catalog", "database", Collections.singletonMap("owner", "analytics"));
    }
    dataSource.close();
    openDataSource();
    try (SqlSession session = sessionFactory.openSession()) {
      Assertions.assertEquals(
          "analytics",
          session
              .getMapper(TableMetaMapper.class)
              .selectDatabaseMetadata("catalog", "database")
              .getProperties()
              .get("owner"));
    }
  }

  @Test
  public void testDisableAutomaticSchemaCreation() throws Exception {
    dataSource.close();
    configurations.set(AmoroManagementConf.DB_CONNECTION_URL, "jdbc:h2:mem:" + UUID.randomUUID());
    configurations.set(AmoroManagementConf.DB_AUTO_CREATE_TABLES, false);
    openDataSource();
    try (Connection connection = dataSource.getConnection();
        ResultSet tables =
            connection
                .getMetaData()
                .getTables(null, connection.getSchema(), "CATALOG_METADATA", null)) {
      Assertions.assertFalse(tables.next());
    }
  }

  @Test
  public void testConfiguredCredentials() throws Exception {
    dataSource.close();
    configurations.set(
        AmoroManagementConf.DB_CONNECTION_URL,
        "jdbc:h2:file:" + temporaryDirectory.resolve("authenticated"));
    configurations.set(AmoroManagementConf.DB_USER_NAME, "ams_user");
    configurations.set(AmoroManagementConf.DB_PASSWORD, "test-password");
    openDataSource();
    try (Connection connection = dataSource.getConnection()) {
      Assertions.assertEquals("AMS_USER", connection.getMetaData().getUserName());
    }
    dataSource.close();
    configurations.set(AmoroManagementConf.DB_PASSWORD, "wrong-password");
    Assertions.assertThrows(
        IllegalStateException.class, () -> DataSourceFactory.createDataSource(configurations));
  }

  @Test
  public void testGeneratedKeysAndClob() {
    try (SqlSession session = sessionFactory.openSession(true)) {
      TableMetaMapper tables = session.getMapper(TableMetaMapper.class);
      ServerTableIdentifier identifier =
          ServerTableIdentifier.of("catalog", "database", "table", TableFormat.ICEBERG);
      tables.insertTable(identifier);
      Assertions.assertTrue(identifier.getId() > 0);
      Assertions.assertEquals(
          identifier, tables.selectTableIdentifier("catalog", "database", "table"));
      PlatformFileInfo file =
          new PlatformFileInfo("test.txt", String.join("", Collections.nCopies(10000, "payload")));
      PlatformFileMapper files = session.getMapper(PlatformFileMapper.class);
      files.addFile(file);
      Assertions.assertTrue(file.getFileId() > 0);
      Assertions.assertEquals(file.getFileContent(), files.getFileById(file.getFileId()));
    }
  }

  @Test
  public void testRuntimeFilteringAndPagination() {
    try (SqlSession session = sessionFactory.openSession(true)) {
      TableMetaMapper tables = session.getMapper(TableMetaMapper.class);
      TableRuntimeMapper runtimes = session.getMapper(TableRuntimeMapper.class);
      for (int i = 0; i < 4; i++) {
        ServerTableIdentifier identifier =
            ServerTableIdentifier.of(
                "catalog", i == 3 ? "other" : "sales_db", "orders_" + i, TableFormat.ICEBERG);
        tables.insertTable(identifier);
        TableRuntimeMeta runtime = new TableRuntimeMeta();
        runtime.setTableId(identifier.getId());
        runtime.setGroupName("default");
        runtime.setStatusCode(700 + i);
        runtimes.insertRuntime(runtime);
      }
      List<TableRuntimeMeta> matches = runtimes.queryForGroups("default", "sales", "orders", null);
      Assertions.assertEquals(3, matches.size());
      Assertions.assertTrue(matches.get(0).getStatusCodeUpdateTime() > 0);
      Assertions.assertNull(matches.get(0).getBucketId());
      Assertions.assertEquals(700, matches.get(0).getStatusCode());
      Assertions.assertEquals(702, matches.get(2).getStatusCode());
      Assertions.assertTrue(
          runtimes.queryForGroups("default", "missing", "orders", null).isEmpty());
      Assertions.assertTrue(runtimes.queryForGroups("default", "sales", "missing", null).isEmpty());
      try (Page<TableRuntimeMeta> page = PageHelper.startPage(2, 2)) {
        List<TableRuntimeMeta> secondPage =
            runtimes.queryForGroups("default", "sales", "orders", null);
        Assertions.assertEquals(3, page.getTotal());
        Assertions.assertEquals(1, secondPage.size());
        Assertions.assertEquals(702, secondPage.get(0).getStatusCode());
      }
    }
  }

  @Test
  public void testBlobAndNullValues() throws Exception {
    try (SqlSession session = sessionFactory.openSession(true)) {
      OptimizingProcessMapper mapper = session.getMapper(OptimizingProcessMapper.class);
      mapper.insertInternalProcessState(1, 2, 3, 4, Collections.emptyMap(), Collections.emptyMap());
      mapper.updateProcessInputFiles(2, Collections.emptyMap());
      Object2ByteArrayConvert<Map<Integer, Object>> converter = new Object2ByteArrayConvert<>();
      try (Statement statement = session.getConnection().createStatement();
          ResultSet result =
              statement.executeQuery(
                  "SELECT rewrite_input FROM optimizing_process_state WHERE process_id = 2")) {
        Assertions.assertTrue(result.next());
        Assertions.assertEquals(Collections.emptyMap(), converter.getResult(result, 1));
      }
      mapper.updateProcessInputFiles(2, null);
      try (PreparedStatement statement =
          session
              .getConnection()
              .prepareStatement(
                  "SELECT rewrite_input FROM optimizing_process_state WHERE process_id = ?")) {
        statement.setLong(1, 2);
        try (ResultSet result = statement.executeQuery()) {
          Assertions.assertTrue(result.next());
          Assertions.assertNull(converter.getResult(result, "rewrite_input"));
        }
      }
    }
  }

  @Test
  public void testDatabaseRowLockSerializesUpdates() throws Exception {
    try (SqlSession session = sessionFactory.openSession(true)) {
      session
          .getMapper(TableMetaMapper.class)
          .insertDatabase("catalog", "database", Collections.singletonMap("base", "value"));
    }
    CountDownLatch waiting = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (SqlSession first = sessionFactory.openSession(false)) {
      TableMetaMapper mapper = first.getMapper(TableMetaMapper.class);
      DatabaseMetadata metadata = mapper.selectDatabaseMetadataForUpdate("catalog", "database");
      Future<Map<String, String>> update =
          executor.submit(
              () -> {
                try (SqlSession second = sessionFactory.openSession(false)) {
                  TableMetaMapper secondMapper = second.getMapper(TableMetaMapper.class);
                  waiting.countDown();
                  Map<String, String> properties =
                      new HashMap<>(
                          secondMapper
                              .selectDatabaseMetadataForUpdate("catalog", "database")
                              .getProperties());
                  properties.put("second", "2");
                  secondMapper.updateDatabaseProperties("catalog", "database", properties);
                  second.commit();
                  return properties;
                }
              });
      Assertions.assertTrue(waiting.await(5, TimeUnit.SECONDS));
      Assertions.assertThrows(TimeoutException.class, () -> update.get(250, TimeUnit.MILLISECONDS));
      Map<String, String> properties = new HashMap<>(metadata.getProperties());
      properties.put("first", "1");
      mapper.updateDatabaseProperties("catalog", "database", properties);
      first.commit();
      Map<String, String> result = update.get(5, TimeUnit.SECONDS);
      Assertions.assertEquals("1", result.get("first"));
      Assertions.assertEquals("2", result.get("second"));
      try (SqlSession verification = sessionFactory.openSession()) {
        Assertions.assertEquals(
            result,
            verification
                .getMapper(TableMetaMapper.class)
                .selectDatabaseMetadata("catalog", "database")
                .getProperties());
      }
    } finally {
      executor.shutdownNow();
      Assertions.assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }
  }

  @Test
  public void testRollback() {
    try (SqlSession session = sessionFactory.openSession(false)) {
      session
          .getMapper(TableMetaMapper.class)
          .insertDatabase("catalog", "rolled_back", Collections.emptyMap());
      session.rollback();
    }
    try (SqlSession session = sessionFactory.openSession()) {
      Assertions.assertNull(
          session.getMapper(TableMetaMapper.class).selectDatabase("catalog", "rolled_back"));
    }
  }

  @Test
  public void testLeaseAndBucketAssignment() {
    try (SqlSession session = sessionFactory.openSession(true)) {
      HaLeaseMapper leases = session.getMapper(HaLeaseMapper.class);
      leases.insertIfAbsent(
          new HaLeaseMeta("cluster", "AMS", "node", "127.0.0.1", "{}", 0L, 0, 1L));
      Assertions.assertEquals(
          1, leases.acquireLease("cluster", "AMS", "node", "127.0.0.1", "{}", 100L, 10L));
      Assertions.assertEquals(
          0, leases.acquireLease("cluster", "AMS", "other", "127.0.0.2", "{}", 100L, 10L));
      Assertions.assertEquals(1, leases.renewLease("cluster", "AMS", "node", 1, 200L, 20L));
      Assertions.assertEquals(200L, leases.selectLease("cluster", "AMS").getLeaseExpireTs());
      BucketAssignMapper buckets = session.getMapper(BucketAssignMapper.class);
      buckets.insert(new BucketAssignmentMeta("cluster", "node", "{}", "[1,2]", 10L));
      Assertions.assertEquals(1, buckets.update("cluster", "node", "{}", "[3]", 20L));
      Assertions.assertEquals("[3]", buckets.selectByNode("cluster", "node").getAssignmentsJson());
      Assertions.assertEquals(1, buckets.selectAllByCluster("cluster").size());
    }
  }

  @Test
  public void testHttpSessionPersistence() throws Exception {
    SessionHandler handler =
        HttpSessionHandlerFactory.createSessionHandler(dataSource, configurations);
    SessionDataStore store = handler.getSessionCache().getSessionDataStore();
    store.initialize(new SessionContext("h2-test", null));
    store.start();
    try {
      long now = System.currentTimeMillis();
      SessionData data = store.newSessionData("h2-session", now, now, now, 60000);
      data.setLastNode("h2-test");
      data.setAttribute("user", "admin");
      store.store(data.getId(), data);
      Assertions.assertEquals("admin", store.load(data.getId()).getAttribute("user"));
      data.setAttribute("user", "viewer");
      store.store(data.getId(), data);
      Assertions.assertEquals("viewer", store.load(data.getId()).getAttribute("user"));
      Assertions.assertTrue(store.delete(data.getId()));
      Assertions.assertNull(store.load(data.getId()));
    } finally {
      store.stop();
    }
  }
}

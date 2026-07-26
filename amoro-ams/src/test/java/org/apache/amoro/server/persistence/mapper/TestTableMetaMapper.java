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

package org.apache.amoro.server.persistence.mapper;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.DatabaseMetadata;
import org.apache.amoro.server.persistence.DataSourceFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestTableMetaMapper {

  private static BasicDataSource dataSource;
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  public static void initialize() {
    Configurations configurations = new Configurations();
    configurations.set(
        AmoroManagementConf.DB_CONNECTION_URL,
        String.format("jdbc:derby:memory:%s;create=true", UUID.randomUUID()));
    configurations.set(AmoroManagementConf.DB_TYPE, AmoroManagementConf.DB_TYPE_DERBY);
    configurations.set(
        AmoroManagementConf.DB_DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
    dataSource = (BasicDataSource) DataSourceFactory.createDataSource(configurations);

    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("test", transactionFactory, dataSource);
    org.apache.ibatis.session.Configuration configuration =
        new org.apache.ibatis.session.Configuration(environment);
    configuration.setDatabaseId("derby");
    configuration.addMapper(TableMetaMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @AfterAll
  public static void clean() throws SQLException {
    dataSource.close();
  }

  @Test
  public void testDatabaseProperties() {
    Map<String, String> properties = new HashMap<>();
    properties.put("location", "s3://warehouse/test_db");
    properties.put("owner", "analytics");

    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      TableMetaMapper mapper = session.getMapper(TableMetaMapper.class);
      mapper.insertDatabase("test_catalog", "test_db", properties);

      DatabaseMetadata metadata = mapper.selectDatabaseMetadata("test_catalog", "test_db");
      Assertions.assertEquals(properties, metadata.getProperties());

      Map<String, String> updatedProperties = new HashMap<>();
      updatedProperties.put("location", "s3://warehouse/test_db");
      updatedProperties.put("retention-days", "30");
      Assertions.assertEquals(
          Integer.valueOf(1),
          mapper.updateDatabaseProperties("test_catalog", "test_db", updatedProperties));
      Assertions.assertEquals(
          updatedProperties,
          mapper.selectDatabaseMetadata("test_catalog", "test_db").getProperties());
    }
  }

  @Test
  public void testDatabasePropertiesUpdateIsSerialized() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("base", "value");
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      session
          .getMapper(TableMetaMapper.class)
          .insertDatabase("lock_catalog", "lock_db", properties);
    }

    CountDownLatch secondUpdateStarted = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (SqlSession firstSession = sqlSessionFactory.openSession(false)) {
      TableMetaMapper firstMapper = firstSession.getMapper(TableMetaMapper.class);
      DatabaseMetadata firstMetadata =
          firstMapper.selectDatabaseMetadataForUpdate("lock_catalog", "lock_db");

      Future<Map<String, String>> secondUpdate =
          executor.submit(
              () -> {
                try (SqlSession secondSession = sqlSessionFactory.openSession(false)) {
                  TableMetaMapper secondMapper = secondSession.getMapper(TableMetaMapper.class);
                  secondUpdateStarted.countDown();
                  DatabaseMetadata secondMetadata =
                      secondMapper.selectDatabaseMetadataForUpdate("lock_catalog", "lock_db");
                  Map<String, String> secondProperties =
                      new HashMap<>(secondMetadata.getProperties());
                  secondProperties.put("second", "2");
                  secondMapper.updateDatabaseProperties(
                      "lock_catalog", "lock_db", secondProperties);
                  secondSession.commit();
                  return secondProperties;
                }
              });

      Assertions.assertTrue(secondUpdateStarted.await(5, TimeUnit.SECONDS));
      Assertions.assertThrows(
          TimeoutException.class, () -> secondUpdate.get(500, TimeUnit.MILLISECONDS));

      Map<String, String> firstProperties = new HashMap<>(firstMetadata.getProperties());
      firstProperties.put("first", "1");
      firstMapper.updateDatabaseProperties("lock_catalog", "lock_db", firstProperties);
      firstSession.commit();

      Map<String, String> mergedProperties = secondUpdate.get(5, TimeUnit.SECONDS);
      Assertions.assertEquals("1", mergedProperties.get("first"));
      Assertions.assertEquals("2", mergedProperties.get("second"));
    } finally {
      executor.shutdownNow();
      Assertions.assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      Map<String, String> storedProperties =
          session
              .getMapper(TableMetaMapper.class)
              .selectDatabaseMetadata("lock_catalog", "lock_db")
              .getProperties();
      Assertions.assertEquals("value", storedProperties.get("base"));
      Assertions.assertEquals("1", storedProperties.get("first"));
      Assertions.assertEquals("2", storedProperties.get("second"));
    }
  }
}

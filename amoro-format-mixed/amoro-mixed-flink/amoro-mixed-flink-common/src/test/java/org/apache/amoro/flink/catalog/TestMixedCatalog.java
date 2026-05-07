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

package org.apache.amoro.flink.catalog;

import static org.apache.amoro.flink.FlinkSchemaUtil.COMPUTED_COLUMNS;
import static org.apache.amoro.flink.FlinkSchemaUtil.FLINK_PREFIX;
import static org.apache.amoro.flink.FlinkSchemaUtil.WATERMARK;
import static org.apache.flink.table.api.config.TableConfigOptions.TABLE_DYNAMIC_TABLE_OPTIONS_ENABLED;
import static org.apache.flink.table.descriptors.DescriptorProperties.DATA_TYPE;
import static org.apache.flink.table.descriptors.DescriptorProperties.EXPR;
import static org.apache.flink.table.descriptors.DescriptorProperties.NAME;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_ROWTIME;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_STRATEGY_DATA_TYPE;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_STRATEGY_EXPR;

import org.apache.amoro.MockAmoroManagementServer;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestAms;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.flink.MiniClusterResource;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;
import org.apache.flink.util.CollectionUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test cases for mixed catalog factories, including:
 * CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER, CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER,
 * CatalogFactoryOptions.LEGACY_MIXED_IDENTIFIER.
 *
 * <p>This test no longer extends the still-JUnit-4 {@code CatalogTestBase}; the bits of catalog
 * lifecycle that were pulled in from there are inlined so the class can be a clean Jupiter test.
 */
public class TestMixedCatalog {
  private static final Logger LOG = LoggerFactory.getLogger(TestMixedCatalog.class);

  protected static final TestAms TEST_AMS = new TestAms();

  private final BasicCatalogTestHelper testHelper =
      new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG);

  private CatalogMeta catalogMeta;
  private MixedFormatCatalog mixedFormatCatalog;
  private File tempRoot;

  private String catalogName;
  private String catalogFactoryType;

  protected Map<String, String> props;

  private static final String DB = TableTestHelper.TEST_DB_NAME;
  private static final String TABLE = TableTestHelper.TEST_TABLE_NAME;
  private volatile StreamExecutionEnvironment env = null;
  private volatile StreamTableEnvironment tEnv = null;

  @BeforeAll
  public static void startTestAms() throws Exception {
    TEST_AMS.before();
  }

  @AfterAll
  public static void stopTestAms() {
    TEST_AMS.after();
  }

  @AfterEach
  public void afterEach() {
    try {
      if (catalogName != null) {
        sql("DROP TABLE IF EXISTS " + catalogName + "." + DB + "." + TABLE);
        sql("USE CATALOG default_catalog");
        sql("DROP DATABASE IF EXISTS " + catalogName + "." + DB);
        Assertions.assertTrue(
            CollectionUtil.isNullOrEmpty(getMixedFormatCatalog().listDatabases()));
        sql("DROP CATALOG " + catalogName);
      }
    } catch (Throwable t) {
      LOG.warn("Test teardown failed", t);
    }
    if (catalogMeta != null) {
      try {
        getAmsHandler().dropCatalog(catalogMeta.getCatalogName());
      } catch (Exception e) {
        LOG.warn("dropCatalog failed", e);
      }
      catalogMeta = null;
    }
    if (tempRoot != null) {
      try {
        FileUtils.deleteDirectory(tempRoot);
      } catch (Exception e) {
        // ignore
      }
      tempRoot = null;
    }
    mixedFormatCatalog = null;
  }

  private void setUpForParam(String catalogFactoryType) throws Exception {
    this.catalogFactoryType = catalogFactoryType;
    this.catalogName = catalogFactoryType + "_catalog";
    tempRoot = Files.createTempDirectory("test-mixed-catalog").toFile();
    catalogMeta = testHelper.buildCatalogMeta(tempRoot.getPath());
    catalogMeta.putToCatalogProperties(CatalogMetaProperties.AMS_URI, TEST_AMS.getServerUrl());
    getAmsHandler().createCatalog(catalogMeta);
    props = Maps.newHashMap();
    props.put("type", catalogFactoryType);
    props.put(CatalogFactoryOptions.AMS_URI.key(), getCatalogUri());
    sql("CREATE CATALOG " + catalogName + " WITH %s", toWithClause(props));
    sql("USE CATALOG " + catalogName);
    sql("CREATE DATABASE " + catalogName + "." + DB);
  }

  static MockAmoroManagementServer.AmsHandler getAmsHandler() {
    return TEST_AMS.getAmsHandler();
  }

  protected MixedFormatCatalog getMixedFormatCatalog() {
    if (mixedFormatCatalog == null) {
      mixedFormatCatalog = CatalogLoader.load(getCatalogUri());
    }
    return mixedFormatCatalog;
  }

  protected String getCatalogUri() {
    return TEST_AMS.getServerUrl() + "/" + catalogMeta.getCatalogName();
  }

  static Stream<String> parameters() {
    return Stream.of(
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER);
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testMixedCatalog(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    String[] catalogs = getTableEnv().listCatalogs();
    Assertions.assertArrayEquals(
        Arrays.stream(catalogs).sorted().toArray(),
        Stream.of("default_catalog", catalogName).sorted().toArray());
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testDDL(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    sql(
        "CREATE TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " name STRING,"
            + " t TIMESTAMP,"
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(t) ");
    sql("USE  " + catalogName + "." + DB);
    sql("SHOW tables");

    Assertions.assertTrue(
        getMixedFormatCatalog()
            .loadTable(TableIdentifier.of(catalogName, DB, TABLE))
            .isKeyedTable());
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testComputeIndex(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    // if compute column before any physical column, will throw exception.
    Assertions.assertThrows(
        org.apache.flink.table.api.TableException.class,
        () ->
            sql(
                "CREATE TABLE "
                    + catalogName
                    + "."
                    + DB
                    + "."
                    + TABLE
                    + " ("
                    + " id INT,"
                    + " compute_id as id+5 ,"
                    + " proc as PROCTIME() ,"
                    + " name STRING"
                    + ") "));

    // compute column must come after all the physical columns
    sql(
        "CREATE TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " proc as PROCTIME() "
            + ") ");
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testDDLWithVirtualColumn(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    // create mixed-format table with compute columns and watermark under mixed-format catalog
    // org.apache.iceberg.flink.TypeToFlinkType will convert Timestamp to Timestamp(6), so we cast
    // datatype manually
    sql(
        "CREATE TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " name STRING,"
            + " t TIMESTAMP,"
            + " t3 as cast(t as TIMESTAMP(3)),"
            + " compute_id as id+5 ,"
            + " proc as PROCTIME() ,"
            + " watermark FOR t3 AS t3 - INTERVAL '5' SECOND, "
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(t) ");

    Map<String, String> properties =
        getMixedFormatCatalog().loadTable(TableIdentifier.of(catalogName, DB, TABLE)).properties();

    // index for compute columns
    int[] computedIndex = {1, 2, 3};
    Arrays.stream(computedIndex)
        .forEach(
            x -> {
              Assertions.assertTrue(
                  properties.containsKey(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, x, NAME)));
              Assertions.assertTrue(
                  properties.containsKey(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, x, EXPR)));
              Assertions.assertTrue(
                  properties.containsKey(
                      compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, x, DATA_TYPE)));
            });

    Assertions.assertTrue(
        properties.containsKey(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_ROWTIME)));
    Assertions.assertTrue(
        properties.containsKey(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_EXPR)));
    Assertions.assertTrue(
        properties.containsKey(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_DATA_TYPE)));

    List<Row> result = sql("DESC " + catalogName + "." + DB + "." + TABLE + "");
    Assertions.assertEquals(6, result.size());
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testDMLWithVirtualColumn(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    // create mixed-format table with compute columns under mixed-format catalog
    sql(
        "CREATE TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " t TIMESTAMP(6),"
            + " compute_id as id+5 ,"
            + " proc as PROCTIME(), "
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(t) ");

    // insert values into mixed-format table
    insertValue();

    // select from mixed-format table with compute columns under mixed-format catalog
    List<Row> rows =
        sql(
            "SELECT * FROM "
                + catalogName
                + "."
                + DB
                + "."
                + TABLE
                + " /*+ OPTIONS("
                + "'streaming'='false'"
                + ") */");
    checkRows(rows);
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testReadNotMatchColumn(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    // create mixed-format table with compute columns under mixed-format catalog
    sql(
        "CREATE TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " t TIMESTAMP(6),"
            + " proc as PROCTIME(), "
            + " compute_id as id+5 ,"
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(t) ");

    MixedTable amoroTable =
        getMixedFormatCatalog().loadTable(TableIdentifier.of(catalogName, DB, TABLE));
    String beforeExpr =
        amoroTable.properties().get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, 2, EXPR));
    // change property "flink.computed-column.2.expr" from "`id` +5" to "`newId` +5"
    String afterExpr = "`newId` +5";
    amoroTable
        .updateProperties()
        .set(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, 2, EXPR), afterExpr)
        .commit();

    Assertions.assertNotEquals(
        beforeExpr,
        amoroTable.properties().get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, 2, EXPR)));

    // property for expr do not match any columns in amoro, will throw exception.
    Assertions.assertThrows(
        IllegalStateException.class,
        () -> sql("DESC " + catalogName + "." + DB + "." + TABLE + ""));
    amoroTable
        .updateProperties()
        .set(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, 2, EXPR), beforeExpr)
        .commit();

    // can get table normally
    sql("DESC " + catalogName + "." + DB + "." + TABLE + "");
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testDML(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    sql(
        "CREATE TABLE default_catalog.default_database."
            + TABLE
            + " ("
            + " id INT,"
            + " name STRING,"
            + " t TIMESTAMP,"
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(t) "
            + " WITH ("
            + " 'connector' = 'datagen',"
            + " 'fields.id.kind'='sequence',"
            + " 'fields.id.start'='1',"
            + " 'fields.id.end'='1'"
            + ")");
    sql(
        "CREATE TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " name STRING,"
            + " t TIMESTAMP,"
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(t) ");

    sql(
        "INSERT INTO "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " SELECT * FROM default_catalog.default_database."
            + TABLE);
    List<Row> rows =
        sql(
            "SELECT * FROM "
                + catalogName
                + "."
                + DB
                + "."
                + TABLE
                + " /*+ OPTIONS("
                + "'streaming'='false'"
                + ") */");
    Assertions.assertEquals(1, rows.size());

    sql("DROP TABLE default_catalog.default_database." + TABLE);
  }

  private void checkRows(List<Row> rows) {
    Assertions.assertEquals(1, rows.size());
    int id = (int) rows.get(0).getField("id");
    int computeId = (int) rows.get(0).getField("compute_id");
    Assertions.assertEquals(1, id);
    // computeId should be id+5
    Assertions.assertEquals(id + 5, computeId);
    Assertions.assertEquals(4, rows.get(0).getFieldNames(true).size());
  }

  protected List<Row> sql(String query, Object... args) {
    TableResult tableResult = getTableEnv().executeSql(String.format(query, args));
    tableResult
        .getJobClient()
        .ifPresent(
            c -> {
              try {
                c.getJobExecutionResult().get();
              } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
              }
            });
    try (CloseableIterator<Row> iter = tableResult.collect()) {
      List<Row> results = Lists.newArrayList(iter);
      return results;
    } catch (Exception e) {
      LOG.warn("Failed to collect table result", e);
      return null;
    }
  }

  protected StreamTableEnvironment getTableEnv() {
    if (tEnv == null) {
      synchronized (this) {
        if (tEnv == null) {
          this.tEnv =
              StreamTableEnvironment.create(
                  getEnv(), EnvironmentSettings.newInstance().inStreamingMode().build());
          Configuration configuration = tEnv.getConfig().getConfiguration();
          // set low-level key-value options
          configuration.setString(TABLE_DYNAMIC_TABLE_OPTIONS_ENABLED.key(), "true");
        }
      }
    }
    return tEnv;
  }

  protected StreamExecutionEnvironment getEnv() {
    if (env == null) {
      synchronized (this) {
        if (env == null) {
          StateBackend backend =
              new FsStateBackend(
                  "file:///" + System.getProperty("java.io.tmpdir") + "/flink/backend");
          env =
              StreamExecutionEnvironment.getExecutionEnvironment(
                  MiniClusterResource.DISABLE_CLASSLOADER_CHECK_CONFIG);
          env.setParallelism(1);
          env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
          env.getCheckpointConfig().setCheckpointInterval(300);
          env.getCheckpointConfig()
              .enableExternalizedCheckpoints(
                  CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
          env.setStateBackend(backend);
          env.setRestartStrategy(RestartStrategies.noRestart());
        }
      }
    }
    return env;
  }

  public static String toWithClause(Map<String, String> props) {
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    int propCount = 0;
    for (Map.Entry<String, String> entry : props.entrySet()) {
      if (propCount > 0) {
        builder.append(",");
      }
      builder
          .append("'")
          .append(entry.getKey())
          .append("'")
          .append("=")
          .append("'")
          .append(entry.getValue())
          .append("'");
      propCount++;
    }
    builder.append(")");
    return builder.toString();
  }

  private String compoundKey(Object... components) {
    return Stream.of(components).map(Object::toString).collect(Collectors.joining("."));
  }

  private void insertValue() {
    sql(
        "CREATE TABLE default_catalog.default_database."
            + TABLE
            + " ("
            + " id INT,"
            + " t TIMESTAMP,"
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(t) "
            + " WITH ("
            + " 'connector' = 'datagen',"
            + " 'fields.id.kind'='sequence',"
            + " 'fields.id.start'='1',"
            + " 'fields.id.end'='1'"
            + ")");

    sql(
        "INSERT INTO "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " SELECT * FROM default_catalog.default_database."
            + TABLE);

    sql("DROP TABLE default_catalog.default_database." + TABLE);
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testAlterUnKeyTable(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    sql(
        "CREATE TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " name STRING,"
            + " t TIMESTAMP"
            + ") PARTITIONED BY(t) "
            + " WITH ("
            + " 'self-optimizing.enabled' = 'false'"
            + ")");

    sql(
        "ALTER TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " "
            + "SET ( 'write.metadata.delete-after-commit.enabled' = 'false')");
    Map<String, String> unKeyTableProperties =
        getMixedFormatCatalog().loadTable(TableIdentifier.of(catalogName, DB, TABLE)).properties();
    Assertions.assertEquals(
        unKeyTableProperties.get("write.metadata.delete-after-commit.enabled"), "false");
  }

  @ParameterizedTest(name = "catalogFactoryType = {0}")
  @ValueSource(
      strings = {
        CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
        CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER
      })
  public void testAlterKeyTable(String catalogFactoryType) throws Exception {
    setUpForParam(catalogFactoryType);
    sql(
        "CREATE TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " name STRING,"
            + " t TIMESTAMP,"
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(t) ");
    sql(
        "ALTER TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " "
            + "SET ( 'self-optimizing.group' = 'flink')");
    sql(
        "ALTER TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " "
            + "SET ( 'self-optimizing.enabled' = 'true')");

    sql(
        "ALTER TABLE "
            + catalogName
            + "."
            + DB
            + "."
            + TABLE
            + " "
            + "SET ( 'write.upsert.enabled' = 'true')");

    Map<String, String> keyTableProperties =
        getMixedFormatCatalog().loadTable(TableIdentifier.of(catalogName, DB, TABLE)).properties();
    Assertions.assertEquals(keyTableProperties.get("self-optimizing.enabled"), "true");
    Assertions.assertEquals(keyTableProperties.get("self-optimizing.group"), "flink");
    Assertions.assertEquals(keyTableProperties.get("write.upsert.enabled"), "true");
  }
}

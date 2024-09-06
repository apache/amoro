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

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestBase;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.flink.table.DynamicTableFactory;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
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
import org.apache.iceberg.flink.MiniClusterResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test cases for mixed catalog factories, including:
 * CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER, CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER,
 * CatalogFactoryOptions.LEGACY_MIXED_IDENTIFIER
 */
@RunWith(value = Parameterized.class)
public class TestMixedCatalog extends CatalogTestBase {
  private String catalogName;
  private String catalogFactoryType;
  private static final Logger LOG = LoggerFactory.getLogger(TestMixedCatalog.class);

  public TestMixedCatalog(String catalogFactoryType) {
    super(new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG));
    this.catalogFactoryType = catalogFactoryType;
    this.catalogName = catalogFactoryType + "_catalog";
  }

  @Parameterized.Parameters(name = "catalogFactoryType = {0}")
  public static Object[] parameters() {
    return new Object[] {
      CatalogFactoryOptions.MIXED_ICEBERG_IDENTIFIER,
      CatalogFactoryOptions.MIXED_HIVE_IDENTIFIER,
      CatalogFactoryOptions.LEGACY_MIXED_IDENTIFIER
    };
  }

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  protected Map<String, String> props;

  private static final String DB = TableTestHelper.TEST_DB_NAME;
  private static final String TABLE = TableTestHelper.TEST_TABLE_NAME;
  private volatile StreamExecutionEnvironment env = null;
  private volatile StreamTableEnvironment tEnv = null;

  @Before
  public void before() throws Exception {
    props = Maps.newHashMap();
    props.put("type", catalogFactoryType);
    props.put(CatalogFactoryOptions.METASTORE_URL.key(), getCatalogUrl());
    sql("CREATE CATALOG " + catalogName + " WITH %s", toWithClause(props));
    sql("USE CATALOG " + catalogName);
    sql("CREATE DATABASE " + catalogName + "." + DB);
  }

  @After
  public void after() {
    sql("DROP TABLE IF EXISTS " + catalogName + "." + DB + "." + TABLE);
    sql("DROP DATABASE IF EXISTS " + catalogName + "." + DB);
    Assert.assertTrue(CollectionUtil.isNullOrEmpty(getMixedFormatCatalog().listDatabases()));
    sql("USE CATALOG default_catalog");
    sql("DROP CATALOG " + catalogName);
  }

  @Test
  public void testMixedCatalog() {
    String[] catalogs = getTableEnv().listCatalogs();
    Assert.assertArrayEquals(
        Arrays.stream(catalogs).sorted().toArray(),
        Stream.of("default_catalog", catalogName).sorted().toArray());
  }

  @Test
  public void testDDL() {
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

    Assert.assertTrue(
        getMixedFormatCatalog()
            .loadTable(TableIdentifier.of(catalogName, DB, TABLE))
            .isKeyedTable());
  }

  @Test
  public void testComputeIndex() {
    // if compute column before any physical column, will throw exception.
    Assert.assertThrows(
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

  @Test
  public void testDDLWithVirtualColumn() throws IOException {
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
              Assert.assertTrue(
                  properties.containsKey(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, x, NAME)));
              Assert.assertTrue(
                  properties.containsKey(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, x, EXPR)));
              Assert.assertTrue(
                  properties.containsKey(
                      compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, x, DATA_TYPE)));
            });

    Assert.assertTrue(
        properties.containsKey(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_ROWTIME)));
    Assert.assertTrue(
        properties.containsKey(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_EXPR)));
    Assert.assertTrue(
        properties.containsKey(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_DATA_TYPE)));

    List<Row> result = sql("DESC " + catalogName + "." + DB + "." + TABLE + "");
    Assert.assertEquals(6, result.size());
  }

  @Test
  public void testDMLWithVirtualColumn() throws IOException {
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

  @Test
  public void testReadNotMatchColumn() throws IOException {
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

    Assert.assertNotEquals(
        beforeExpr,
        amoroTable.properties().get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, 2, EXPR)));

    // property for expr do not match any columns in amoro, will throw exception.
    Assert.assertThrows(
        IllegalStateException.class,
        () -> sql("DESC " + catalogName + "." + DB + "." + TABLE + ""));
    amoroTable
        .updateProperties()
        .set(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, 2, EXPR), beforeExpr)
        .commit();

    // can get table normally
    sql("DESC " + catalogName + "." + DB + "." + TABLE + "");
  }

  @Test
  public void testDML() throws IOException {
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
    Assert.assertEquals(1, rows.size());

    sql("DROP TABLE default_catalog.default_database." + TABLE);
  }

  @Test
  public void testDefaultCatalogDDLWithVirtualColumn() {
    // this test only for LEGACY_MIXED_IDENTIFIER
    if (catalogFactoryType.equals(CatalogFactoryOptions.LEGACY_MIXED_IDENTIFIER)) {
      // create mixed-format table with only physical columns
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
              + " PRIMARY KEY (id) NOT ENFORCED "
              + ") PARTITIONED BY(t) "
              + " WITH ("
              + " 'connector' = 'mixed-format'"
              + ")");

      // insert values into mixed-format table
      insertValue();

      // create Table with compute columns under default catalog
      props = Maps.newHashMap();
      props.put("connector", DynamicTableFactory.IDENTIFIER);
      props.put(CatalogFactoryOptions.METASTORE_URL.key(), getCatalogUrl());
      props.put(DynamicTableFactory.IDENTIFIER + ".catalog", catalogName);
      props.put(DynamicTableFactory.IDENTIFIER + ".database", DB);
      props.put(DynamicTableFactory.IDENTIFIER + ".table", TABLE);

      sql(
          "CREATE TABLE default_catalog.default_database."
              + TABLE
              + " ("
              + " id INT,"
              + " t TIMESTAMP(6),"
              + " compute_id as id+5 ,"
              + " proc as PROCTIME(), "
              + " PRIMARY KEY (id) NOT ENFORCED "
              + ") PARTITIONED BY(t) "
              + "WITH %s",
          toWithClause(props));

      // select from mixed-format table with compute columns under default catalog
      List<Row> rows =
          sql(
              "SELECT * FROM default_catalog.default_database."
                  + TABLE
                  + " /*+ OPTIONS("
                  + "'streaming'='false'"
                  + ") */");
      checkRows(rows);
    }
  }

  private void checkRows(List<Row> rows) {
    Assert.assertEquals(1, rows.size());
    int id = (int) rows.get(0).getField("id");
    int computeId = (int) rows.get(0).getField("compute_id");
    Assert.assertEquals(1, id);
    // computeId should be id+5
    Assert.assertEquals(id + 5, computeId);
    Assert.assertEquals(4, rows.get(0).getFieldNames(true).size());
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

  @Test
  public void testAlterUnKeyTable() throws Exception {
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
    Assert.assertEquals(
        unKeyTableProperties.get("write.metadata.delete-after-commit.enabled"), "false");
  }

  @Test
  public void testAlterKeyTable() throws Exception {
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
    Assert.assertEquals(keyTableProperties.get("self-optimizing.enabled"), "true");
    Assert.assertEquals(keyTableProperties.get("self-optimizing.group"), "flink");
    Assert.assertEquals(keyTableProperties.get("write.upsert.enabled"), "true");
  }
}

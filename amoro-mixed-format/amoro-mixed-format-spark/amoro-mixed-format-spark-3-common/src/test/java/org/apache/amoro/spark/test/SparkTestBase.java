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

package org.apache.amoro.spark.test;

import org.apache.amoro.AlreadyExistsException;
import org.apache.amoro.Constants;
import org.apache.amoro.TableFormat;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.UnifiedCatalogLoader;
import org.apache.amoro.client.AmsThriftUrl;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.spark.test.utils.TestTableUtil;
import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.execution.QueryExecution;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class SparkTestBase {
  protected static final Logger LOG = LoggerFactory.getLogger(SparkTestBase.class);
  public static final SparkTestContext CONTEXT = new SparkTestContext();
  public static final String SPARK_SESSION_CATALOG = "spark_catalog";

  @BeforeAll
  public static void setupContext() throws Exception {
    CONTEXT.initialize();
  }

  @AfterAll
  public static void tearDownContext() {
    CONTEXT.close();
  }

  private SparkSession spark;
  protected String currentCatalog = SPARK_SESSION_CATALOG;
  protected QueryExecution qe;

  private final String database = "mixed_database";
  private final String table = "mixed_table";
  protected final String sourceTable = "test_source_table";
  protected TestIdentifier source;

  protected Map<String, String> sparkSessionConfig() {
    return ImmutableMap.of(
        "spark.sql.catalog.spark_catalog",
        SparkTestContext.SESSION_CATALOG_IMPL,
        "spark.sql.catalog.spark_catalog.uri",
        CONTEXT.amsCatalogUrl(TableFormat.MIXED_HIVE));
  }

  @AfterEach
  public void tearDownTestSession() {
    spark = null;
  }

  @BeforeEach
  public void before() {
    try {
      LOG.debug("prepare database for table test: {}", database());
      UnifiedCatalog catalog = unifiedCatalog();
      if (!unifiedCatalog().databaseExists(database())) {
        catalog.createDatabase(database());
      }
    } catch (AlreadyExistsException e) {
      // pass
    }
    source = null;
  }

  @AfterEach
  public void after() {
    LOG.debug("clean up table after test: {}.{}.{}", currentCatalog, database(), table());
    UnifiedCatalog catalog = unifiedCatalog();
    try {
      catalog.dropTable(database, table, true);
    } catch (Exception e) {
      // pass
    }
    try {
      CONTEXT.dropHiveTable(database(), table());
    } catch (Exception e) {
      // pass
    }
  }

  public void setCurrentCatalog(String catalog) {
    this.currentCatalog = catalog;
    sql("USE " + this.currentCatalog);
  }

  protected String sparkCatalogToAMSCatalog(String sparkCatalog) {
    String uri = null;
    try {
      uri = spark().conf().get("spark.sql.catalog." + sparkCatalog + ".uri");
    } catch (NoSuchElementException e) {
      uri = spark().conf().get("spark.sql.catalog." + sparkCatalog + ".url");
    }
    AmsThriftUrl catalogUri = AmsThriftUrl.parse(uri, Constants.THRIFT_TABLE_SERVICE_NAME);
    return catalogUri.catalogName();
  }

  protected UnifiedCatalog unifiedCatalog() {
    String amsCatalogName = sparkCatalogToAMSCatalog(currentCatalog);
    return UnifiedCatalogLoader.loadUnifiedCatalog(
        CONTEXT.ams.getServerUrl(), amsCatalogName, Maps.newHashMap());
  }

  protected SparkSession spark() {
    if (this.spark == null) {
      Map<String, String> conf = sparkSessionConfig();
      this.spark = CONTEXT.getSparkSession(conf);
    }
    return spark;
  }

  protected Dataset<Row> sql(String sqlText) {
    long begin = System.currentTimeMillis();
    LOG.info("Execute SQL: {}", sqlText);
    Dataset<Row> ds = spark().sql(sqlText);
    if (ds.columns().length == 0) {
      LOG.info("+----------------+");
      LOG.info("|  Empty Result  |");
      LOG.info("+----------------+");
    } else {
      ds.show();
    }
    qe = ds.queryExecution();
    LOG.info("SQL Execution cost: {} ms", System.currentTimeMillis() - begin);
    return ds;
  }

  protected String database() {
    return this.database;
  }

  protected String table() {
    return this.table;
  }

  protected TestIdentifier target() {
    String amsCatalogName = sparkCatalogToAMSCatalog(currentCatalog);
    return TestIdentifier.ofDataLake(currentCatalog, amsCatalogName, database, table, false);
  }

  protected TestIdentifier source() {
    Preconditions.checkNotNull(source);
    return source;
  }

  protected void createHiveSource(
      List<FieldSchema> cols, List<FieldSchema> partitions, Map<String, String> properties) {
    long currentTimeMillis = System.currentTimeMillis();
    Table source =
        new Table(
            sourceTable,
            database,
            null,
            (int) currentTimeMillis / 1000,
            (int) currentTimeMillis / 1000,
            Integer.MAX_VALUE,
            null,
            partitions,
            new HashMap<>(),
            null,
            null,
            TableType.EXTERNAL_TABLE.toString());
    StorageDescriptor storageDescriptor = new StorageDescriptor();
    storageDescriptor.setInputFormat(HiveTableProperties.PARQUET_INPUT_FORMAT);
    storageDescriptor.setOutputFormat(HiveTableProperties.PARQUET_OUTPUT_FORMAT);
    storageDescriptor.setCols(cols);
    SerDeInfo serDeInfo = new SerDeInfo();
    serDeInfo.setSerializationLib(HiveTableProperties.PARQUET_ROW_FORMAT_SERDE);
    storageDescriptor.setSerdeInfo(serDeInfo);
    source.setSd(storageDescriptor);
    source.setParameters(properties);
    try {
      CONTEXT.getHiveClient().createTable(source);
      this.source = TestIdentifier.ofHiveSource(database, sourceTable);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void createViewSource(Schema schema, List<Record> data) {
    Dataset<Row> ds =
        spark()
            .createDataFrame(
                data.stream().map(TestTableUtil::recordToRow).collect(Collectors.toList()),
                SparkSchemaUtil.convert(schema));

    ds.createOrReplaceTempView(sourceTable);
    this.source = TestIdentifier.ofViewSource(sourceTable);
  }

  protected void createHiveSource(List<FieldSchema> cols, List<FieldSchema> partitions) {
    this.createHiveSource(cols, partitions, ImmutableMap.of());
  }

  protected Table loadHiveTable() {
    TestIdentifier identifier = target();
    return CONTEXT.loadHiveTable(identifier.database, identifier.table);
  }

  protected String provider(TableFormat format) {
    return format.name().toLowerCase();
  }
}

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

package org.apache.amoro.server.dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.hive.HMSMockServer;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.server.catalog.CatalogBuilder;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.model.TableMeta;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.catalog.CatalogFactory;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.options.Options;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.types.DataTypes;
import org.apache.thrift.TException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Verifies table/database visibility rules across catalog formats against a real embedded Hive
 * Metastore.
 */
public class TestTableControllerVisibility {

  private static final HMSMockServer HMS =
      new HMSMockServer(new File("target/hive_warehouse_visibility"));
  private static final TableController CONTROLLER = new TableController(null, null, null, null);

  private static final String DB_WITH_HIVE_TABLES = "db_native";
  private static final String DB_WITH_MIXED_TABLE = "db_mixed";
  private static final String DB_WITH_HIVE_TABLES_ONLY = "db_native_only";
  private static final String DB_EMPTY = "db_empty";
  // The embedded metastore always creates this empty database.
  private static final String HMS_DEFAULT_DB = "default";

  private static final String MIXED_HIVE_TABLE = "mixed_tbl";
  private static final Map<String, ServerCatalog> CATALOGS = new HashMap<>();

  @BeforeAll
  static void startMetastore() {
    HMS.start();
  }

  @AfterAll
  static void stopMetastore() {
    HMS.stop();
  }

  @BeforeEach
  void prepareTables() throws TException {
    createDatabase(DB_WITH_HIVE_TABLES);
    createDatabase(DB_WITH_MIXED_TABLE);
    createDatabase(DB_WITH_HIVE_TABLES_ONLY);
    createDatabase(DB_EMPTY);

    createHiveTable(DB_WITH_HIVE_TABLES, "t1");
    createHiveTable(DB_WITH_HIVE_TABLES, "t2");
    createHiveTable(DB_WITH_HIVE_TABLES_ONLY, "t4");
    createHiveTable(DB_WITH_MIXED_TABLE, "t3");
    createMixedHiveTable(DB_WITH_MIXED_TABLE, MIXED_HIVE_TABLE);
  }

  @AfterEach
  void resetMetastore() throws Exception {
    HMS.reset();
  }

  @Test
  public void testHiveTablesListedWhenHiveSelected() throws TException {
    ServerCatalog catalog = catalog(TableFormat.HIVE);

    List<TableMeta> tables = CONTROLLER.buildTableList(catalog, DB_WITH_HIVE_TABLES, null);

    assertEquals(Arrays.asList("t1", "t2"), tableNames(tables));
    assertTrue(
        tables.stream()
            .allMatch(table -> TableMeta.TableType.HIVE.toString().equals(table.getType())));
  }

  @Test
  public void testMixedHiveSelectedShowsOnlyMixedTables() throws TException {
    ServerCatalog catalog = catalog(TableFormat.MIXED_HIVE);

    assertTrue(CONTROLLER.buildTableList(catalog, DB_WITH_HIVE_TABLES, null).isEmpty());

    List<TableMeta> tables = CONTROLLER.buildTableList(catalog, DB_WITH_MIXED_TABLE, null);
    assertEquals(Collections.singletonList(MIXED_HIVE_TABLE), tableNames(tables));
    assertEquals(TableMeta.TableType.ARCTIC.toString(), tables.get(0).getType());
  }

  @Test
  public void testHiveAndMixedTablesListedWhenBothSelected() throws TException {
    ServerCatalog catalog = catalog(TableFormat.MIXED_HIVE, TableFormat.HIVE);

    List<TableMeta> tables = CONTROLLER.buildTableList(catalog, DB_WITH_MIXED_TABLE, null);

    assertEquals(Arrays.asList(MIXED_HIVE_TABLE, "t3"), tableNames(tables));
    assertEquals(TableMeta.TableType.ARCTIC.toString(), tables.get(0).getType());
    assertEquals(TableMeta.TableType.HIVE.toString(), tables.get(1).getType());
  }

  @Test
  public void testDatabasesDerivedFromSelectedFormats() throws TException {
    ServerCatalog mixedHiveOnly = catalog(TableFormat.MIXED_HIVE);
    ServerCatalog mixedAndHive = catalog(TableFormat.MIXED_HIVE, TableFormat.HIVE);

    // A database is visible only when it holds a table of a selected format.
    assertEquals(
        sorted(Collections.singletonList(DB_WITH_MIXED_TABLE)),
        sorted(CONTROLLER.buildDatabaseList(mixedHiveOnly, null)));
    assertEquals(
        sorted(Arrays.asList(DB_WITH_HIVE_TABLES, DB_WITH_MIXED_TABLE, DB_WITH_HIVE_TABLES_ONLY)),
        sorted(CONTROLLER.buildDatabaseList(mixedAndHive, null)));
  }

  @Test
  public void testEmptyDatabaseIsHidden() throws TException {
    ServerCatalog catalog = catalog(TableFormat.HIVE);

    List<String> databases = CONTROLLER.buildDatabaseList(catalog, null);

    assertFalse(databases.contains(DB_EMPTY));
    assertFalse(databases.contains(HMS_DEFAULT_DB));
  }

  @Test
  public void testTableFilterKeepsOnlyMatchingNativeHiveTables() throws TException {
    // table-filter is a whitelist: only tables matching it are visible.
    ServerCatalog catalog = catalog("db_native.t1", TableFormat.HIVE);

    List<TableMeta> tables = CONTROLLER.buildTableList(catalog, DB_WITH_HIVE_TABLES, null);

    assertEquals(Collections.singletonList("t1"), tableNames(tables));
  }

  @Test
  public void testIcebergListingsDeriveFromRealTables() throws Exception {
    ServerCatalog catalog = hadoopIcebergCatalog();
    try (HadoopCatalog iceberg = hadoopCatalog()) {
      iceberg.createNamespace(Namespace.of("db_hadoop"));
      iceberg.createTable(
          org.apache.iceberg.catalog.TableIdentifier.of("db_hadoop", "iceberg_tbl"),
          icebergTableSchema());
    }

    List<TableMeta> tables = CONTROLLER.buildTableList(catalog, "db_hadoop", null);
    assertEquals(Collections.singletonList("iceberg_tbl"), tableNames(tables));
    assertEquals(TableMeta.TableType.ICEBERG.toString(), tables.get(0).getType());
    assertTrue(CONTROLLER.buildDatabaseList(catalog, null).contains("db_hadoop"));
  }

  @Test
  public void testPaimonSelectedHidesNonPaimonTablesAndDatabases() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogOptions.METASTORE.key(), CatalogMetaProperties.CATALOG_TYPE_HIVE);
    properties.put(
        CatalogOptions.URI.key(), HMS.hiveConf().get(HiveConf.ConfVars.METASTOREURIS.varname));
    properties.put(CatalogOptions.WAREHOUSE.key(), HMS.getWareHouseLocation());
    properties.put(CatalogOptions.FORMAT_TABLE_ENABLED.key(), "false");
    try (Catalog paimon =
        CatalogFactory.createCatalog(
            CatalogContext.create(
                Options.fromMap(properties), new org.apache.hadoop.conf.Configuration()))) {
      paimon.createTable(
          Identifier.create(DB_WITH_MIXED_TABLE, "paimon_tbl"),
          Schema.newBuilder().column("id", DataTypes.INT()).build(),
          false);
    }

    ServerCatalog catalog = catalog(TableFormat.PAIMON);
    List<TableMeta> tables = CONTROLLER.buildTableList(catalog, DB_WITH_MIXED_TABLE, null);

    assertEquals(Collections.singletonList("paimon_tbl"), tableNames(tables));
    assertEquals(TableMeta.TableType.PAIMON.toString(), tables.get(0).getType());
    assertFalse(CONTROLLER.buildDatabaseList(catalog, null).contains(DB_WITH_HIVE_TABLES));
  }

  private static org.apache.iceberg.Schema icebergTableSchema() {
    return new org.apache.iceberg.Schema(
        Collections.singletonList(
            org.apache.iceberg.types.Types.NestedField.optional(
                1, "id", org.apache.iceberg.types.Types.IntegerType.get())));
  }

  private static ServerCatalog catalog(TableFormat... formats) {
    return catalog(null, formats);
  }

  private static ServerCatalog catalog(String tableFilter, TableFormat... formats) {
    String cacheKey = tableFilter + Arrays.toString(formats);
    return CATALOGS.computeIfAbsent(cacheKey, key -> buildCatalog(tableFilter, formats));
  }

  private static ServerCatalog buildCatalog(String tableFilter, TableFormat... formats) {
    Map<String, String> properties = new HashMap<>();
    properties.put(
        CatalogProperties.URI, HMS.hiveConf().get(HiveConf.ConfVars.METASTOREURIS.varname));
    properties.put(CatalogProperties.WAREHOUSE_LOCATION, HMS.getWareHouseLocation());
    if (tableFilter != null) {
      properties.put(CatalogMetaProperties.KEY_TABLE_FILTER, tableFilter);
    }
    CatalogMeta metadata =
        CatalogTestHelpers.buildHiveCatalogMeta(
            "visibility_catalog", properties, HMS.hiveConf(), formats);
    return CatalogBuilder.buildServerCatalog(metadata, new Configurations());
  }

  private static ServerCatalog hadoopIcebergCatalog() {
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogProperties.WAREHOUSE_LOCATION, HMS.getWareHouseLocation());
    CatalogMeta metadata =
        CatalogTestHelpers.buildCatalogMeta(
            "visibility_hadoop",
            CatalogMetaProperties.CATALOG_TYPE_HADOOP,
            properties,
            TableFormat.ICEBERG);
    return CatalogBuilder.buildServerCatalog(metadata, new Configurations());
  }

  private static HadoopCatalog hadoopCatalog() {
    HadoopCatalog hadoopCatalog = new HadoopCatalog();
    hadoopCatalog.setConf(new org.apache.hadoop.conf.Configuration());
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogProperties.WAREHOUSE_LOCATION, HMS.getWareHouseLocation());
    hadoopCatalog.initialize("visibility_hadoop", properties);
    return hadoopCatalog;
  }

  private static void createDatabase(String database) throws TException {
    Database meta = new Database();
    meta.setName(database);
    HMS.getClient().createDatabase(meta);
  }

  private static void createHiveTable(String database, String tableName) throws TException {
    HMS.getClient()
        .createTable(CatalogTestHelpers.hiveTable(database, tableName, Collections.emptyMap()));
  }

  private static void createMixedHiveTable(String database, String tableName) throws TException {
    HMS.getClient()
        .createTable(
            CatalogTestHelpers.hiveTable(
                database,
                tableName,
                Collections.singletonMap(HiveTableProperties.MIXED_TABLE_FLAG, "true")));
  }

  private static List<String> tableNames(List<TableMeta> tables) {
    return tables.stream().map(TableMeta::getName).collect(Collectors.toList());
  }

  private static List<String> sorted(List<String> databases) {
    List<String> sorted = new ArrayList<>(databases);
    Collections.sort(sorted);
    return sorted;
  }
}

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

package com.netease.arctic.server;

import com.netease.arctic.AmsClient;
import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.BasicArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.ArcticTableUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TestInternalMixedCatalogService extends InternalCatalogServiceTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(TestInternalMixedCatalogService.class);

  private ArcticCatalog catalog;
  private final String icebergTable = "iceberg_table";

  private final TableIdentifier tableIdentifier =
      TableIdentifier.of(AmsEnvironment.INTERNAL_ICEBERG_CATALOG, database, table);

  private org.apache.iceberg.catalog.TableIdentifier icebergTableIdentifier =
      org.apache.iceberg.catalog.TableIdentifier.of(database, icebergTable);

  static final List<Record> baseRecords =
      Lists.newArrayList(
          MixedDataTestHelpers.createRecord(1, "777", 0, "2022-01-01T12:00:00"),
          MixedDataTestHelpers.createRecord(2, "888", 0, "2022-01-02T12:00:00"),
          MixedDataTestHelpers.createRecord(3, "999", 0, "2022-01-03T12:00:00"));
  static final List<Record> changeInsert =
      Lists.newArrayList(
          MixedDataTestHelpers.createRecord(4, "777", 0, "2022-01-01T12:00:00"),
          MixedDataTestHelpers.createRecord(5, "888", 0, "2022-01-02T12:00:00"),
          MixedDataTestHelpers.createRecord(6, "999", 0, "2022-01-03T12:00:00"));

  static final List<Record> changeDelete =
      Lists.newArrayList(
          MixedDataTestHelpers.createRecord(3, "999", 0, "2022-01-03T12:00:00"),
          MixedDataTestHelpers.createRecord(4, "777", 0, "2022-01-01T12:00:00"));

  @BeforeEach
  public void setMixedCatalog() {
    catalog = loadMixedIcebergCatalog();
  }

  @Override
  protected String catalogName() {
    return AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG;
  }

  @Nested
  public class TestDatabaseOperation {

    @Test
    public void test() {
      ArcticCatalog catalog = loadMixedIcebergCatalog();
      Assertions.assertTrue(catalog.listDatabases().isEmpty());

      catalog.createDatabase(database);
      Assertions.assertEquals(1, catalog.listDatabases().size());
      Assertions.assertTrue(catalog.listDatabases().contains(database));
      Assertions.assertEquals(1, nsCatalog.listNamespaces(Namespace.of()).size());

      catalog.dropDatabase(database);
      Assertions.assertTrue(catalog.listDatabases().isEmpty());
      Assertions.assertTrue(nsCatalog.listNamespaces().isEmpty());
    }
  }

  @Nested
  public class TestTableOperation {

    @BeforeEach
    public void before() {
      catalog.createDatabase(database);
    }

    @AfterEach
    public void after() {
      LOG.info("Test finished.");
      catalog.dropDatabase(database);
    }

    @ParameterizedTest(name = "CatalogTableOperationTest[withPrimaryKey={0}]")
    @ValueSource(booleans = {true, false})
    public void catalogTableOperationTests(boolean withPrimary) {
      TableBuilder builder =
          catalog.newTableBuilder(tableIdentifier, schema).withPartitionSpec(spec);
      if (withPrimary) {
        builder.withPrimaryKeySpec(keySpec);
      }
      // create mixed-iceberg
      builder.create();

      // assert 1 table in mixed-iceberg catalog
      Assertions.assertEquals(1, catalog.listTables(database).size());
      Assertions.assertTrue(catalog.tableExists(tableIdentifier));

      // assert 1 tables in iceberg catalog
      Assertions.assertEquals(1, nsCatalog.listTables(Namespace.of(database)).size());
      Set<String> tables =
          nsCatalog.listTables(Namespace.of(database)).stream()
              .map(org.apache.iceberg.catalog.TableIdentifier::name)
              .collect(Collectors.toSet());
      Set<String> expects = Sets.newHashSet(table);
      Assertions.assertEquals(expects, tables);

      // assert load table
      ArcticTable mixedIcebergTable = catalog.loadTable(tableIdentifier);
      Assertions.assertEquals(withPrimary, mixedIcebergTable.isKeyedTable());

      // drop table
      catalog.dropTable(tableIdentifier, true);
      Assertions.assertEquals(0, catalog.listTables(database).size());
      Assertions.assertEquals(0, nsCatalog.listTables(Namespace.of(database)).size());
    }
  }

  @Nested
  public class TestTableCommit {


    @BeforeEach
    public void before() {
      catalog.createDatabase(database);
    }

    @AfterEach
    public void after() {
      catalog.dropTable(tableIdentifier, true);
      catalog.dropDatabase(database);
    }

    @ParameterizedTest(name = "TableCommitTest[withPrimaryKey={0}]")
    @ValueSource(booleans = {true, false})
    public void testTableCommit(boolean withPrimary) {
      TableBuilder builder =
          catalog.newTableBuilder(tableIdentifier, schema).withPartitionSpec(spec);
      if (withPrimary) {
        builder.withPrimaryKeySpec(keySpec);
      }
      // create mixed-iceberg
      builder.create();

      // load table
      ArcticTable table = catalog.loadTable(tableIdentifier);

      // write and commit records.
      List<Record> expectedResult = writeTestData(table);

      table.refresh();
      // scan table records.
      List<Record> records =
          MixedDataTestHelpers.readTable(table.asKeyedTable(), Expressions.alwaysTrue());
      Assertions.assertEquals(expectedResult.size(), records.size());
    }
  }

  /**
   * The purpose of these tests is to test the compatibility of historical versions of connectors.
   * Mixed-iceberg tables created by old connectors can be accessed with new connectors,
   * but tables created by new connectors cannot be accessed with old versions.
   */
  @Nested
  public class CompatibilityCatalogTests {

    ArcticCatalog historicalCatalog;

    @BeforeEach
    public void setupTest() {
      ArcticCatalog historicalCatalog = new BasicArcticCatalog();
      CatalogMeta meta = serverCatalog.getMetadata();
      AmsClient client = new PooledAmsClient(ams.getTableServiceUrl());
      historicalCatalog.initialize(client, meta, ImmutableMap.of());

      historicalCatalog.createDatabase(database);
      this.historicalCatalog = historicalCatalog;
    }

    @ParameterizedTest(name = "TestNewCatalogLoadHistoricalTable[withPrimaryKey={0}]")
    @ValueSource(booleans = {false, true})
    public void testNewCatalogLoadHistorical(boolean primaryKey) {
      TableBuilder builder = historicalCatalog.newTableBuilder(tableIdentifier, schema)
          .withPartitionSpec(spec);
      if (primaryKey) {
        builder.withPrimaryKeySpec(keySpec);
      }
      // create historical table
      ArcticTable historicalTable = builder.create();
      // write and commit historical table
      List<Record> expectedResult = writeTestData(historicalTable);

      // load table base on rest catalog
      ArcticTable restTable = catalog.loadTable(tableIdentifier);
      List<Record> loadedRecords = MixedDataTestHelpers.readTable(restTable, Expressions.alwaysTrue());

      // assert rest-based table scan right records.
      Assertions.assertEquals(expectedResult.size(), loadedRecords.size());

    }
  }

  /**
   * write test records to the target table and return the expected records
   */
  private static List<Record> writeTestData(ArcticTable table) {
    long txId = table.isKeyedTable() ? table.asKeyedTable().beginTransaction("") : 1L;
    List<DataFile> files = MixedDataTestHelpers.writeBaseStore(table, txId, baseRecords, false);
    UnkeyedTable baseStore = ArcticTableUtil.baseStore(table);
    AppendFiles appendFiles = baseStore.newAppend();
    files.forEach(appendFiles::appendFile);
    appendFiles.commit();
    if (table.isKeyedTable()) {
      txId = table.asKeyedTable().beginTransaction("");
      List<DataFile> changeInsertFiles =
          MixedDataTestHelpers.writeChangeStore(
              table.asKeyedTable(), txId, ChangeAction.INSERT, changeInsert, false);
      UnkeyedTable changeStore = table.asKeyedTable().changeTable();
      appendFiles = changeStore.newAppend();
      changeInsertFiles.forEach(appendFiles::appendFile);
      appendFiles.commit();

      txId = table.asKeyedTable().beginTransaction("");
      List<DataFile> changeDeleteFiles =
          MixedDataTestHelpers.writeChangeStore(
              table.asKeyedTable(), txId, ChangeAction.DELETE, changeDelete, false);
      appendFiles = changeStore.newAppend();
      changeDeleteFiles.forEach(appendFiles::appendFile);
      appendFiles.commit();
      Map<Integer, Record> results = Maps.newHashMap();
      baseRecords.forEach(r -> results.put(r.get(0, Integer.class), r));
      changeInsert.forEach(r -> results.put(r.get(0, Integer.class), r));
      changeDelete.forEach(r -> results.remove(r.get(0, Integer.class)));
      return Lists.newArrayList(results.values());
    } else {
      return baseRecords;
    }
  }

  private ArcticCatalog loadMixedIcebergCatalog() {
    return CatalogLoader.load(
        ams.getTableServiceUrl() + "/" + AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG);
  }
}

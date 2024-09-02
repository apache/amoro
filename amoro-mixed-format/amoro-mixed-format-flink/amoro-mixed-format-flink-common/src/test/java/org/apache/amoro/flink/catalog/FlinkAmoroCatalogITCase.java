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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.flink.table.AmoroCatalogITCaseBase;
import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.formats.paimon.PaimonHadoopCatalogTestHelper;
import org.apache.amoro.formats.paimon.PaimonHiveCatalogTestHelper;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.hive.TestHMS;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CatalogBaseTable;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.types.Row;
import org.apache.paimon.table.FileStoreTable;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** ITCase for Flink UnifiedCatalog based on AmoroCatalogTestBase */
@RunWith(value = Parameterized.class)
public class FlinkAmoroCatalogITCase extends AmoroCatalogITCaseBase {
  static final TestHMS TEST_HMS = new TestHMS();
  AbstractCatalog flinkCatalog;

  public FlinkAmoroCatalogITCase(AmoroCatalogTestHelper<?> catalogTestHelper) {
    super(catalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {
      PaimonHiveCatalogTestHelper.defaultHelper(), PaimonHadoopCatalogTestHelper.defaultHelper()
    };
  }

  @BeforeClass
  public static void beforeAll() throws Exception {
    TEST_HMS.before();
  }

  @Before
  public void setup() throws Exception {
    createDatabase();
    createTable();
    String catalog = "unified_catalog";
    exec(
        "CREATE CATALOG %s WITH ('type'='unified', 'metastore.url'='%s')",
        catalog, getCatalogUrl());
    exec("USE CATALOG %s", catalog);
    exec("USE %s", TEST_DB_NAME);
    Optional<Catalog> catalogOptional = getTableEnv().getCatalog(catalog);
    assertTrue(catalogOptional.isPresent());
    flinkCatalog = (AbstractCatalog) catalogOptional.get();
    assertEquals(catalog, flinkCatalog.getName());
  }

  @After
  public void teardown() {
    TEST_HMS.after();
    if (flinkCatalog != null) {
      flinkCatalog.close();
    }
  }

  public void createDatabase() {
    try {
      catalogTestHelper.createDatabase(TEST_DB_NAME);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void createTable() {
    try {
      catalogTestHelper.createTable(TEST_DB_NAME, TEST_TABLE_NAME);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testTableExists() throws Exception {
    CatalogBaseTable catalogBaseTable =
        flinkCatalog.getTable(new ObjectPath(TEST_DB_NAME, TEST_TABLE_NAME));
    assertNotNull(catalogBaseTable);
    PaimonTable paimonTable =
        (PaimonTable) catalogTestHelper.amoroCatalog().loadTable(TEST_DB_NAME, TEST_TABLE_NAME);
    FileStoreTable originalPaimonTable = (FileStoreTable) paimonTable.originalTable();
    assertEquals(
        originalPaimonTable.schema().fields().size(),
        catalogBaseTable.getUnresolvedSchema().getColumns().size());
  }

  @Test
  public void testInsertAndQuery() throws Exception {
    exec("INSERT INTO %s SELECT 1, 'Lily', 1234567890", TEST_TABLE_NAME);
    TableResult tableResult =
        exec("select * from %s /*+OPTIONS('monitor-interval'='1s')*/ ", TEST_TABLE_NAME);

    tableResult.await(30, TimeUnit.SECONDS);

    Row actualRow = tableResult.collect().next();
    assertEquals(Row.of(1, "Lily", 1234567890).toString(), actualRow.toString());
  }

  @Test
  public void testSwitchCurrentCatalog() {
    String memCatalog = "mem_catalog";
    exec("create catalog %s with('type'='generic_in_memory')", memCatalog);
    exec(
        "create table %s.`default`.datagen_table(\n"
            + "    a int,\n"
            + "    b varchar"
            + ") with(\n"
            + "    'connector'='datagen',\n"
            + "    'number-of-rows'='1'\n"
            + ")",
        memCatalog);
    TableResult tableResult = exec("select * from mem_catalog.`default`.datagen_table");
    assertNotNull(tableResult.collect().next());
    exec("use catalog %s", memCatalog);
    tableResult = exec("select * from datagen_table");
    assertNotNull(tableResult.collect().next());
  }
}

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

package com.netease.arctic.flink.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.flink.table.CatalogITCaseBase;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.table.TableIdentifier;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CatalogBaseTable;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.exceptions.TableNotExistException;
import org.apache.flink.types.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RunWith(value = Parameterized.class)
public class FlinkUnifiedCatalogITCase extends CatalogITCaseBase {
  static final TestHMS TEST_HMS = new TestHMS();
  AbstractCatalog flinkCatalog;
  TableIdentifier identifier;

  public FlinkUnifiedCatalogITCase(CatalogTestHelper catalogTestHelper) {
    super(catalogTestHelper, new BasicTableTestHelper(true, false));
  }

  @Parameterized.Parameters(name = "catalogTestHelper = {0}")
  public static Object[][] parameters() {
    return new Object[][] {
      {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf())},
      {new HiveCatalogTestHelper(TableFormat.MIXED_ICEBERG, TEST_HMS.getHiveConf())},
      {new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf())}
    };
  }

  @BeforeClass
  public static void beforeAll() throws Exception {
    TEST_HMS.before();
  }

  @Before
  public void setup() throws Exception {
    String catalog = "unified_catalog";
    exec(
        "CREATE CATALOG %s WITH ('type'='unified', 'metastore.url'='%s')",
        catalog, getCatalogUrl());
    exec("USE CATALOG %s", catalog);
    exec("USE %s", tableTestHelper().id().getDatabase());
    Optional<Catalog> catalogOptional = getTableEnv().getCatalog(catalog);
    assertTrue(catalogOptional.isPresent());
    flinkCatalog = (AbstractCatalog) catalogOptional.get();
    assertEquals(catalog, flinkCatalog.getName());
    identifier = tableTestHelper().id();
  }

  @After
  public void teardown() {
    TEST_HMS.after();
    if (flinkCatalog != null) {
      flinkCatalog.close();
    }
  }

  @Test
  public void testTableExists() throws TableNotExistException {
    CatalogBaseTable catalogBaseTable =
        flinkCatalog.getTable(new ObjectPath(identifier.getDatabase(), identifier.getTableName()));
    assertNotNull(catalogBaseTable);
    assertEquals(
        tableTestHelper().tableSchema().columns().size(),
        catalogBaseTable.getUnresolvedSchema().getColumns().size());
  }

  @Test
  public void testInsertAndQuery() throws Exception {
    exec(
        "INSERT INTO %s SELECT 1, 'Lily', 1234567890, TO_TIMESTAMP('2020-01-01 01:02:03')",
        identifier.getTableName());
    TableResult tableResult =
        exec("select * from %s /*+OPTIONS('monitor-interval'='1s')*/ ", identifier.getTableName());

    tableResult.await(30, TimeUnit.SECONDS);

    Row actualRow = tableResult.collect().next();
    assertEquals(
        Row.of(1, "Lily", 1234567890L, "2020-01-01T01:02:03").toString(), actualRow.toString());
  }
}

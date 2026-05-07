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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.flink.table.CatalogITCaseBase;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CatalogBaseTable;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.types.Row;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FlinkUnifiedCatalogITCase extends CatalogITCaseBase {
  static final TestHMS TEST_HMS = new TestHMS();
  AbstractCatalog flinkCatalog;
  TableIdentifier identifier;

  static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf())),
        Arguments.of(new HiveCatalogTestHelper(TableFormat.MIXED_ICEBERG, TEST_HMS.getHiveConf())),
        Arguments.of(new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf())));
  }

  @BeforeAll
  public static void startTestHms() throws Exception {
    TEST_HMS.before();
  }

  @AfterAll
  public static void stopTestHms() {
    TEST_HMS.after();
  }

  @AfterEach
  public void teardown() {
    if (flinkCatalog != null) {
      flinkCatalog.close();
    }
  }

  private void setUpForParam(CatalogTestHelper catalogTestHelper) throws Exception {
    initCatalogITCase(catalogTestHelper, new BasicTableTestHelper(true, false));
    String catalog = "unified_catalog";
    exec("CREATE CATALOG %s WITH ('type'='unified', 'ams.uri'='%s')", catalog, getCatalogUri());
    exec("USE CATALOG %s", catalog);
    exec("USE %s", tableTestHelper().id().getDatabase());
    Optional<Catalog> catalogOptional = getTableEnv().getCatalog(catalog);
    assertTrue(catalogOptional.isPresent());
    flinkCatalog = (AbstractCatalog) catalogOptional.get();
    assertEquals(catalog, flinkCatalog.getName());
    identifier = tableTestHelper().id();
  }

  @ParameterizedTest(name = "catalogTestHelper = {0}")
  @MethodSource("parameters")
  public void testTableExists(CatalogTestHelper catalogTestHelper) throws Exception {
    setUpForParam(catalogTestHelper);
    CatalogBaseTable catalogBaseTable =
        flinkCatalog.getTable(new ObjectPath(identifier.getDatabase(), identifier.getTableName()));
    assertNotNull(catalogBaseTable);
    assertEquals(
        tableTestHelper().tableSchema().columns().size(),
        catalogBaseTable.getUnresolvedSchema().getColumns().size());
  }

  @ParameterizedTest(name = "catalogTestHelper = {0}")
  @MethodSource("parameters")
  public void testInsertAndQuery(CatalogTestHelper catalogTestHelper) throws Exception {
    setUpForParam(catalogTestHelper);
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

  @ParameterizedTest(name = "catalogTestHelper = {0}")
  @MethodSource("parameters")
  public void testSwitchCurrentCatalog(CatalogTestHelper catalogTestHelper) throws Exception {
    setUpForParam(catalogTestHelper);
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

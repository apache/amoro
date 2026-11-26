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

package org.apache.amoro.formats.hive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.hive.HMSMockServer;
import org.apache.amoro.utils.CatalogUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestHiveCatalog {

  private static final String CATALOG_NAME = "hive_catalog";
  private static HMSMockServer hms;
  private static HiveCatalog catalog;

  @BeforeAll
  static void startMetastore() {
    hms = new HMSMockServer(new File("target/hive_warehouse_format_hive"));
    hms.start();

    CatalogMeta metadata =
        CatalogTestHelpers.buildHiveCatalogMeta(
            CATALOG_NAME, new HashMap<>(), hms.hiveConf(), TableFormat.HIVE);
    catalog =
        new HiveCatalog(
            CATALOG_NAME, CatalogUtil.buildMetaStore(metadata), metadata.getCatalogProperties());
  }

  @AfterAll
  static void stopMetastore() {
    hms.stop();
  }

  @Test
  public void testCreateDatabaseAndListHiveTables() throws Exception {
    String database = "db_create";
    catalog.createDatabase(database);

    assertTrue(catalog.databaseExists(database));
    assertTrue(catalog.listDatabases().contains(database));
    assertEquals(Collections.emptyList(), catalog.listTables(database));

    createTable(database, "native_tbl", Collections.emptyMap());
    createTable(database, "iceberg_tbl", Collections.singletonMap("table_type", "ICEBERG"));

    assertEquals(Collections.singletonList("native_tbl"), catalog.listTables(database));
    assertTrue(catalog.tableExists(database, "native_tbl"));
    assertFalse(catalog.tableExists(database, "iceberg_tbl"));

    assertEquals(TableFormat.HIVE, catalog.loadTable(database, "native_tbl").format());
    assertThrows(NoSuchTableException.class, () -> catalog.loadTable(database, "iceberg_tbl"));
  }

  @Test
  public void testDropTableAndDatabase() throws Exception {
    String database = "db_drop";
    catalog.createDatabase(database);
    createTable(database, "t1", Collections.emptyMap());

    assertTrue(catalog.dropTable(database, "t1", false));
    assertFalse(catalog.dropTable(database, "t1", false));

    catalog.dropDatabase(database);
    assertFalse(catalog.databaseExists(database));
  }

  private static void createTable(String database, String tableName, Map<String, String> parameters)
      throws Exception {
    hms.getClient().createTable(CatalogTestHelpers.hiveTable(database, tableName, parameters));
  }
}

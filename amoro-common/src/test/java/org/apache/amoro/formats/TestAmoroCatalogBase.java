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

package org.apache.amoro.formats;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class TestAmoroCatalogBase extends AmoroCatalogTestBase {

  private static final String DB1 = "db1";
  private static final String DB2 = "db2";
  private static final String DB3 = "db3";

  private static final String TABLE = "table";

  protected AmoroCatalogTestHelper<?> catalogTestHelper;

  @BeforeEach
  public void setupCatalogHelper(AmoroCatalogTestHelper<?> helper) throws IOException {
    this.catalogTestHelper = helper;
    String path = temp.getPath();
    catalogTestHelper.initWarehouse(path);
    this.amoroCatalog = catalogTestHelper.amoroCatalog();
    this.originalCatalog = catalogTestHelper.originalCatalog();
  }

  protected abstract void createDatabase(String dbName);

  protected abstract void createTable(
      String dbName, String tableName, Map<String, String> properties);

  protected abstract List<String> listDatabases();

  protected void testListDatabases() {
    createDatabase(DB1);
    createDatabase(DB2);
    createDatabase(DB3);
    HashSet<String> databases = Sets.newHashSet(amoroCatalog.listDatabases());
    Assertions.assertTrue(databases.contains(DB1));
    Assertions.assertTrue(databases.contains(DB2));
    Assertions.assertTrue(databases.contains(DB3));
  }

  protected void testDropDatabases() {
    createDatabase(DB1);
    amoroCatalog.dropDatabase(DB1);

    Assertions.assertFalse(amoroCatalog.listDatabases().contains(DB1));
  }

  protected void testCreateDatabases() {
    amoroCatalog.createDatabase(DB1);
    Assertions.assertTrue(listDatabases().contains(DB1));
  }

  protected void testExistsDatabase() {
    createDatabase(DB1);
    Assertions.assertTrue(amoroCatalog.databaseExists(DB1));
  }

  protected void testExistsTable() {
    createDatabase(DB1);
    createTable(DB1, TABLE, new HashMap<>());
    Assertions.assertTrue(amoroCatalog.tableExists(DB1, TABLE));
  }

  protected void testLoadTable() {
    createDatabase(DB1);
    Map<String, String> properties = new HashMap<>();
    properties.put("key1", "value1");
    createTable(DB1, TABLE, properties);
    AmoroTable<?> amoroTable = amoroCatalog.loadTable(DB1, TABLE);
    Assertions.assertEquals("value1", amoroTable.properties().get("key1"));
    Assertions.assertEquals(
        catalogTestHelper.catalogName() + "." + DB1 + "." + TABLE, amoroTable.name());
    Assertions.assertEquals(
        TableIdentifier.of(catalogTestHelper.catalogName(), DB1, TABLE), amoroTable.id());
  }
}

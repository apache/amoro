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

package com.netease.arctic.formats;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class TestAmoroCatalogBase extends AmoroCatalogTestBase {

  private static final String DB1 = "db1";
  private static final String DB2 = "db2";
  private static final String DB3 = "db3";

  private static final String TABLE = "table";

  public TestAmoroCatalogBase(AmoroCatalogTestHelper<?> catalogTestHelper) {
    super(catalogTestHelper);
  }

  protected abstract void createDatabase(String dbName);

  protected abstract void createTable(
      String dbName, String tableName, Map<String, String> properties);

  protected abstract List<String> listDatabases();

  @Test
  public void testListDatabases() {
    createDatabase(DB1);
    createDatabase(DB2);
    createDatabase(DB3);
    HashSet<String> databases = Sets.newHashSet(amoroCatalog.listDatabases());
    Assert.assertTrue(databases.contains(DB1));
    Assert.assertTrue(databases.contains(DB2));
    Assert.assertTrue(databases.contains(DB3));
  }

  @Test
  public void testDropDatabases() {
    createDatabase(DB1);
    amoroCatalog.dropDatabase(DB1);

    Assert.assertFalse(amoroCatalog.listDatabases().contains(DB1));
  }

  @Test
  public void testCreateDatabases() {
    amoroCatalog.createDatabase(DB1);
    Assert.assertTrue(listDatabases().contains(DB1));
  }

  @Test
  public void testExistsDatabase() {
    createDatabase(DB1);
    Assert.assertTrue(amoroCatalog.exist(DB1));
  }

  @Test
  public void testExistsTable() {
    createDatabase(DB1);
    createTable(DB1, TABLE, new HashMap<>());
    Assert.assertTrue(amoroCatalog.exist(DB1, TABLE));
  }

  @Test
  public void testLoadTable() {
    createDatabase(DB1);
    Map<String, String> properties = new HashMap<>();
    properties.put("key1", "value1");
    createTable(DB1, TABLE, properties);
    AmoroTable<?> amoroTable = amoroCatalog.loadTable(DB1, TABLE);
    Assert.assertEquals(amoroTable.properties().get("key1"), "value1");
    Assert.assertEquals(
        amoroTable.name(), catalogTestHelper.catalogName() + "." + DB1 + "." + TABLE);
    Assert.assertEquals(
        amoroTable.id(), TableIdentifier.of(catalogTestHelper.catalogName(), DB1, TABLE));
  }
}

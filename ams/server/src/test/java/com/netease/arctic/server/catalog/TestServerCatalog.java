/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroCatalog;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.formats.AmoroCatalogTestHelper;
import com.netease.arctic.formats.IcebergHadoopCatalogTestHelper;
import com.netease.arctic.formats.MixedIcebergHadoopCatalogTestHelper;
import com.netease.arctic.formats.PaimonHadoopCatalogTestHelper;
import com.netease.arctic.hive.formats.IcebergHiveCatalogTestHelper;
import com.netease.arctic.hive.formats.MixedIcebergHiveCatalogTestHelper;
import com.netease.arctic.hive.formats.PaimonHiveCatalogTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashMap;

@RunWith(Parameterized.class)
public class TestServerCatalog extends TableCatalogTestBase {

  private final String testDatabaseName = "test_database";

  private final String testTableName = "test_table";

  private final String testTableNameFilter = "test_table_filter";

  public TestServerCatalog(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {
      new PaimonHadoopCatalogTestHelper(
          "test_paimon_catalog",
          new HashMap<String, String>() {
            {
              put(CatalogMetaProperties.KEY_TABLE_FILTER, "test_database.test_table");
            }
          }),
      new PaimonHiveCatalogTestHelper(
          "test_paimon_catalog",
          new HashMap<String, String>() {
            {
              put(CatalogMetaProperties.KEY_TABLE_FILTER, "test_database.test_table");
            }
          }),
      new IcebergHadoopCatalogTestHelper(
          "test_iceberg_catalog",
          new HashMap<String, String>() {
            {
              put(CatalogMetaProperties.KEY_TABLE_FILTER, "test_database.test_table");
            }
          }),
      new IcebergHiveCatalogTestHelper(
          "test_iceberg_catalog",
          new HashMap<String, String>() {
            {
              put(CatalogMetaProperties.KEY_TABLE_FILTER, "test_database.test_table");
            }
          }),
      new MixedIcebergHadoopCatalogTestHelper(
          "test_mixed_catalog",
          new HashMap<String, String>() {
            {
              put(CatalogMetaProperties.KEY_TABLE_FILTER, "test_database.test_table");
            }
          }),
      new MixedIcebergHiveCatalogTestHelper(
          "test_mixed_catalog",
          new HashMap<String, String>() {
            {
              put(CatalogMetaProperties.KEY_TABLE_FILTER, "test_database.test_table");
            }
          })
    };
  }

  @Before
  public void setUp() throws Exception {
    getAmoroCatalog().createDatabase(testDatabaseName);
    getAmoroCatalogTestHelper().createTable(testDatabaseName, testTableName);
    getAmoroCatalogTestHelper().createTable(testDatabaseName, testTableNameFilter);
  }

  @After
  public void cleanFilterTable() {
    AmoroCatalog amoroCatalog = getAmoroCatalog();
    if (amoroCatalog.exist(testDatabaseName, testTableNameFilter)) {
      amoroCatalog.dropTable(testDatabaseName, testTableNameFilter, true);
    }
  }

  @Test
  public void listDatabases() {
    Assert.assertTrue(getServerCatalog().listDatabases().contains(testDatabaseName));
  }

  @Test
  public void dataBaseExists() {
    Assert.assertTrue(getServerCatalog().exist(testDatabaseName));
  }

  @Test
  public void tableExists() {
    Assert.assertTrue(getServerCatalog().exist(testDatabaseName, testTableName));
  }

  @Test
  public void listTables() {
    ServerCatalog serverCatalog = getServerCatalog();
    if (serverCatalog instanceof ExternalCatalog) {
      Assert.assertEquals(1, serverCatalog.listTables(testDatabaseName).size());
      Assert.assertEquals(
          testTableName,
          serverCatalog.listTables(testDatabaseName).get(0).getIdentifier().getTableName());
    } else {
      Assert.assertEquals(2, serverCatalog.listTables(testDatabaseName).size());
    }
  }

  @Test
  public void loadTable() {
    Assert.assertNotNull(getServerCatalog().loadTable(testDatabaseName, testTableName));
  }

  private ServerCatalog getServerCatalog() {
    return tableService().getServerCatalog(getAmoroCatalogTestHelper().catalogName());
  }
}

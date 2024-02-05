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

package com.netease.arctic.server.catalog;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.formats.AmoroCatalogTestHelper;
import com.netease.arctic.formats.IcebergHadoopCatalogTestHelper;
import com.netease.arctic.formats.MixedIcebergHadoopCatalogTestHelper;
import com.netease.arctic.formats.PaimonHadoopCatalogTestHelper;
import com.netease.arctic.hive.formats.IcebergHiveCatalogTestHelper;
import com.netease.arctic.hive.formats.MixedIcebergHiveCatalogTestHelper;
import com.netease.arctic.hive.formats.PaimonHiveCatalogTestHelper;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestServerCatalog extends TableCatalogTestBase {

  private final String testDatabaseName = "test_database";

  private final String testTableName = "test_table";

  public TestServerCatalog(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {
      PaimonHadoopCatalogTestHelper.defaultHelper(),
      PaimonHiveCatalogTestHelper.defaultHelper(),
      IcebergHadoopCatalogTestHelper.defaultHelper(),
      IcebergHiveCatalogTestHelper.defaultHelper(),
      MixedIcebergHadoopCatalogTestHelper.defaultHelper(),
      MixedIcebergHiveCatalogTestHelper.defaultHelper()
    };
  }

  @Before
  public void setUp() throws Exception {
    getAmoroCatalog().createDatabase(testDatabaseName);
    getAmoroCatalogTestHelper().createTable(testDatabaseName, testTableName);
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
    Assert.assertEquals(1, getServerCatalog().listTables(testDatabaseName).size());
    Assert.assertEquals(
        testTableName,
        getServerCatalog().listTables(testDatabaseName).get(0).getIdentifier().getTableName());
  }

  @Test
  public void listTablesWithTableFilter() throws Exception {
    // Table filter only affects ExternalCatalog
    Assume.assumeTrue(getServerCatalog() instanceof ExternalCatalog);
    String dbWithFilter = "db_with_filter";
    String tableWithFilter1 = "test_table1";
    String tableWithFilter2 = "test_table2";
    getAmoroCatalog().createDatabase(dbWithFilter);
    getAmoroCatalogTestHelper().createTable(dbWithFilter, tableWithFilter1);
    getAmoroCatalogTestHelper().createTable(dbWithFilter, tableWithFilter2);
    // without table filter
    Assert.assertEquals(2, getServerCatalog().listTables(dbWithFilter).size());

    CatalogMeta metadata = getServerCatalog().getMetadata();
    metadata
        .getCatalogProperties()
        .put(CatalogMetaProperties.KEY_TABLE_FILTER, dbWithFilter + "." + tableWithFilter1);
    getServerCatalog().updateMetadata(metadata);
    Assert.assertEquals(1, getServerCatalog().listTables(dbWithFilter).size());
    Assert.assertEquals(
        tableWithFilter1,
        getServerCatalog().listTables(dbWithFilter).get(0).getIdentifier().getTableName());

    CatalogMeta metadata2 = getServerCatalog().getMetadata();
    metadata
        .getCatalogProperties()
        .put(CatalogMetaProperties.KEY_TABLE_FILTER, dbWithFilter + "\\." + ".+");
    getServerCatalog().updateMetadata(metadata2);
    Assert.assertEquals(2, getServerCatalog().listTables(dbWithFilter).size());

    CatalogMeta metadata3 = getServerCatalog().getMetadata();
    metadata
        .getCatalogProperties()
        .put(CatalogMetaProperties.KEY_TABLE_FILTER, testDatabaseName + "\\." + ".+");
    getServerCatalog().updateMetadata(metadata3);
    Assert.assertEquals(1, getServerCatalog().listTables(testDatabaseName).size());
    Assert.assertTrue(getServerCatalog().listTables(dbWithFilter).isEmpty());

    CatalogMeta metadata4 = getServerCatalog().getMetadata();
    metadata.getCatalogProperties().remove(CatalogMetaProperties.KEY_TABLE_FILTER);
    getServerCatalog().updateMetadata(metadata4);
  }

  @Test
  public void loadTable() {
    Assert.assertNotNull(getServerCatalog().loadTable(testDatabaseName, testTableName));
  }

  private ServerCatalog getServerCatalog() {
    return tableService().getServerCatalog(getAmoroCatalogTestHelper().catalogName());
  }
}

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

package org.apache.amoro.server.catalog;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.formats.IcebergHadoopCatalogTestHelper;
import org.apache.amoro.formats.MixedIcebergHadoopCatalogTestHelper;
import org.apache.amoro.formats.paimon.PaimonHadoopCatalogTestHelper;
import org.apache.amoro.formats.paimon.PaimonHiveCatalogTestHelper;
import org.apache.amoro.hive.formats.IcebergHiveCatalogTestHelper;
import org.apache.amoro.hive.formats.MixedIcebergHiveCatalogTestHelper;
import org.apache.amoro.properties.CatalogMetaProperties;
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
    Assert.assertTrue(getServerCatalog().databaseExists(testDatabaseName));
  }

  @Test
  public void tableExists() {
    Assert.assertTrue(getServerCatalog().tableExists(testDatabaseName, testTableName));
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
    CATALOG_MANAGER.updateCatalog(metadata);
    Assert.assertEquals(1, getServerCatalog().listTables(dbWithFilter).size());
    Assert.assertEquals(
        tableWithFilter1,
        getServerCatalog().listTables(dbWithFilter).get(0).getIdentifier().getTableName());

    CatalogMeta metadata2 = getServerCatalog().getMetadata();
    metadata
        .getCatalogProperties()
        .put(CatalogMetaProperties.KEY_TABLE_FILTER, dbWithFilter + "\\." + ".+");
    CATALOG_MANAGER.updateCatalog(metadata2);
    Assert.assertEquals(2, getServerCatalog().listTables(dbWithFilter).size());

    CatalogMeta metadata3 = getServerCatalog().getMetadata();
    metadata
        .getCatalogProperties()
        .put(CatalogMetaProperties.KEY_TABLE_FILTER, testDatabaseName + "\\." + ".+");
    CATALOG_MANAGER.updateCatalog(metadata3);
    Assert.assertEquals(1, getServerCatalog().listTables(testDatabaseName).size());
    Assert.assertTrue(getServerCatalog().listTables(dbWithFilter).isEmpty());

    CatalogMeta metadata4 = getServerCatalog().getMetadata();
    metadata.getCatalogProperties().remove(CatalogMetaProperties.KEY_TABLE_FILTER);
    CATALOG_MANAGER.updateCatalog(metadata4);
  }

  @Test
  public void loadTable() {
    Assert.assertNotNull(getServerCatalog().loadTable(testDatabaseName, testTableName));
  }

  private ServerCatalog getServerCatalog() {
    return CATALOG_MANAGER.getServerCatalog(getAmoroCatalogTestHelper().catalogName());
  }
}

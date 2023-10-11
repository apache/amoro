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

import com.netease.arctic.formats.AmoroCatalogTestHelper;
import com.netease.arctic.formats.IcebergHadoopCatalogTestHelper;
import com.netease.arctic.formats.PaimonHadoopCatalogTestHelper;
import com.netease.arctic.hive.formats.IcebergHiveCatalogTestHelper;
import com.netease.arctic.hive.formats.PaimonHiveCatalogTestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ExternalCatalogTest extends TableCatalogTestBase {

  private final String testDatabaseName = "test_database";

  private final String testTableName = "test_table";

  public ExternalCatalogTest(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {
        PaimonHadoopCatalogTestHelper.defaultHelper(),
        PaimonHiveCatalogTestHelper.defaultHelper(),
        IcebergHadoopCatalogTestHelper.defaultHelper(),
        IcebergHiveCatalogTestHelper.defaultHelper()
    };
  }

  @Before
  public void setUp() throws Exception {
    getAmoroCatalog().createDatabase(testDatabaseName);
    getAmoroCatalogTestHelper().createTable(testDatabaseName, testTableName);
  }

  @Test
  public void listDatabases() {
    Assert.assertTrue(getExternalCatalog().listDatabases().contains(testDatabaseName));
  }

  @Test
  public void dataBaseExists() {
    Assert.assertTrue(getExternalCatalog().exist(testDatabaseName));
  }

  @Test
  public void tableExists() {
    Assert.assertTrue(getExternalCatalog().exist(testDatabaseName, testTableName));
  }

  @Test
  public void listTables() {
    Assert.assertEquals(1, getExternalCatalog().listTables(testDatabaseName).size());
    Assert.assertEquals(testTableName, getExternalCatalog().listTables(testDatabaseName).get(0).getTableName());
  }

  @Test
  public void loadTable() {
    Assert.assertNotNull(getExternalCatalog().loadTable(testDatabaseName, testTableName));
  }

  private ServerCatalog getExternalCatalog() {
    return tableService().getServerCatalog(getAmoroCatalogTestHelper().catalogName());
  }
}

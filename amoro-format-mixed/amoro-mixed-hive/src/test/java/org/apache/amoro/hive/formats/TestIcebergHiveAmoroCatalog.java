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

package org.apache.amoro.hive.formats;

import org.apache.amoro.FormatCatalog;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.formats.TestIcebergAmoroCatalog;
import org.apache.amoro.hive.TestHMS;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(Parameterized.class)
public class TestIcebergHiveAmoroCatalog extends TestIcebergAmoroCatalog {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestIcebergHiveAmoroCatalog(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {IcebergHiveCatalogTestHelper.defaultHelper()};
  }

  @Override
  public void setupCatalog() throws IOException {
    catalogTestHelper.initHiveConf(TEST_HMS.getHiveConf());
    super.setupCatalog();
  }

  @Test
  public void testListTablesExcludesHiveViews() throws Exception {
    String database = "view_filter_db";
    String tableName = "iceberg_table";
    String viewName = "hive_view";
    createDatabase(database);
    createTable(database, tableName, new HashMap<>());
    TEST_HMS.createView(database, viewName);

    try {
      List<TableIdentifier> unfilteredTables =
          ((Catalog) originalCatalog).listTables(Namespace.of(database));
      Assert.assertTrue(
          unfilteredTables.stream().map(TableIdentifier::name).anyMatch(viewName::equals));

      List<String> tableNames = ((FormatCatalog) amoroCatalog).listTables(database);
      Assert.assertTrue(tableNames.contains(tableName));
      Assert.assertFalse(tableNames.contains(viewName));
      Assert.assertFalse(amoroCatalog.tableExists(database, viewName));
      Assert.assertThrows(
          NoSuchTableException.class, () -> amoroCatalog.loadTable(database, viewName));
    } finally {
      TEST_HMS.getHiveClient().dropTable(database, viewName, false, true);
    }
  }
}

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

package com.netease.arctic.server.table;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.server.exception.AlreadyExistsException;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static com.netease.arctic.TableTestHelper.TEST_DB_NAME;
import static com.netease.arctic.catalog.CatalogTestHelper.TEST_CATALOG_NAME;

@RunWith(Parameterized.class)
public class TestTableService extends AMSTableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {{new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
                            new BasicTableTestHelper(true, true)},
                           {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
                            new HiveTableTestHelper(true, true)}};
  }

  public TestTableService(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testCreateAndDropTable() {
    tableService().createDatabase(TEST_CATALOG_NAME, TEST_DB_NAME);

    // test create table
    createTable();
    Assert.assertEquals(tableMeta(), tableService().loadTableMetadata(
        tableMeta().getTableIdentifier()).buildTableMeta());

    // test list tables
    List<ServerTableIdentifier> tableIdentifierList = tableService().listTables(TEST_CATALOG_NAME,
        TEST_DB_NAME);
    Assert.assertEquals(1, tableIdentifierList.size());
    Assert.assertEquals(tableMeta().getTableIdentifier(), tableIdentifierList.get(0).getIdentifier());

    // test list table metadata
    List<TableMetadata> tableMetadataList = tableService().listTableMetas();
    Assert.assertEquals(1, tableMetadataList.size());
    Assert.assertEquals(tableMeta(), tableMetadataList.get(0).buildTableMeta());

    tableMetadataList = tableService().listTableMetas(TEST_CATALOG_NAME, TEST_DB_NAME);
    Assert.assertEquals(1, tableMetadataList.size());
    Assert.assertEquals(tableMeta(), tableMetadataList.get(0).buildTableMeta());

    // test table exist
    Assert.assertTrue(tableService().tableExist(tableMeta().getTableIdentifier()));

    // test create duplicate table
    Assert.assertThrows(AlreadyExistsException.class, () -> tableService().createTable(TEST_CATALOG_NAME,
        tableMeta()));

    TableMeta copyMeta = new TableMeta(tableMeta());
    copyMeta.setTableIdentifier(new TableIdentifier("unknown", TEST_DB_NAME,
        TableTestHelper.TEST_TABLE_NAME));
    // test create table with wrong catalog name
    Assert.assertThrows(ObjectNotExistsException.class, () -> tableService().createTable(TEST_CATALOG_NAME,
        copyMeta));

    // test create table in not existed catalog
    Assert.assertThrows(ObjectNotExistsException.class, () -> tableService().createTable("unknown",
        copyMeta));

    if (catalogTestHelper().tableFormat().equals(TableFormat.MIXED_ICEBERG)) {
      copyMeta.setTableIdentifier(new TableIdentifier(TableTestHelper.TEST_CATALOG_NAME, "unknown",
          TableTestHelper.TEST_TABLE_NAME));
      // test create table in not existed database
      Assert.assertThrows(
          ObjectNotExistsException.class,
          () -> tableService().createTable(TEST_CATALOG_NAME, copyMeta));
    }

    // test drop table
    dropTable();
    Assert.assertEquals(0, tableService().listTables().size());
    Assert.assertEquals(0, tableService().listTables(TEST_CATALOG_NAME, TEST_DB_NAME).size());
    Assert.assertEquals(0, tableService().listTableMetas().size());
    Assert.assertEquals(0, tableService().listTableMetas(TEST_CATALOG_NAME, TEST_DB_NAME).size());
    Assert.assertFalse(tableService().tableExist(tableMeta().getTableIdentifier()));

    // test drop not existed table
    Assert.assertThrows(ObjectNotExistsException.class,
        () -> tableService().dropTableMetadata(tableMeta().getTableIdentifier(), true));

    tableService().dropDatabase(TEST_CATALOG_NAME, TEST_DB_NAME);
  }
}

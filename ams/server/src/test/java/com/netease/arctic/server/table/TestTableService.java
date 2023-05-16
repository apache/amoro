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

import com.google.common.collect.Lists;
import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.MixedTables;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.catalog.MixedHiveTables;
import com.netease.arctic.server.exception.AlreadyExistsException;
import com.netease.arctic.server.exception.IllegalMetadataException;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.utils.ConvertStructUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class TestTableService extends TableServiceTestBase {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  private final CatalogTestHelper catalogTestHelper;
  private final TableTestHelper tableTestHelper;
  private String catalogWarehouse;
  private MixedTables mixedTables;
  private CatalogMeta catalogMeta;

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {{new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
                            new BasicTableTestHelper(true, true)},
                           {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
                            new HiveTableTestHelper(true, true)}};
  }

  public TestTableService(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    this.catalogTestHelper = catalogTestHelper;
    this.tableTestHelper = tableTestHelper;
  }

  @Before
  public void initCatalog() throws IOException {
    catalogWarehouse = temp.newFolder().getPath();
    catalogMeta = catalogTestHelper.buildCatalogMeta(catalogWarehouse);
    tableService().createCatalog(catalogMeta);
    switch (catalogTestHelper.tableFormat()) {
      case MIXED_HIVE:
        mixedTables = new MixedHiveTables(catalogMeta);
      case MIXED_ICEBERG:
        mixedTables = new MixedTables(catalogMeta);
    }
  }

  @After
  public void dropCatalog() {
    if (catalogMeta != null) {
      tableService().dropCatalog(catalogMeta.getCatalogName());
    }
  }

  @Test
  public void testCreateAndDropDatabase() {
    final String createDBName = "test_db";
    // test create database
    tableService().createDatabase(catalogMeta.getCatalogName(), createDBName);

    // test create duplicate database
    Assert.assertThrows(AlreadyExistsException.class,
        () -> tableService().createDatabase(catalogMeta.getCatalogName(), createDBName));

    // test list database
    Assert.assertEquals(Lists.newArrayList("test_db"),
        tableService().listDatabases(catalogMeta.getCatalogName()));

    // test drop database
    tableService().dropDatabase(catalogMeta.getCatalogName(), createDBName);
    Assert.assertEquals(Lists.newArrayList(),
        tableService().listDatabases(catalogMeta.getCatalogName()));

    // test drop unknown database
    Assert.assertThrows(ObjectNotExistsException.class,
        () -> tableService().dropDatabase(catalogMeta.getCatalogName(), createDBName));

    // test create database in not existed catalog
    Assert.assertThrows(ObjectNotExistsException.class,
        () -> tableService().createDatabase("unknown", createDBName));

    // test drop database in not existed catalog
    Assert.assertThrows(ObjectNotExistsException.class,
        () -> tableService().dropDatabase("unknown", createDBName));
  }

  @Test
  public void testCreateAndDropTable() {
    tableService().createDatabase(catalogMeta.getCatalogName(), TableTestHelper.TEST_DB_NAME);
    TableMeta tableMeta = buildTable();

    // test create table
    tableService().createTable(catalogMeta.getCatalogName(), tableMeta);
    Assert.assertEquals(tableMeta, tableService().loadTableMetadata(tableMeta.getTableIdentifier()).buildTableMeta());

    // test list tables
    List<ServerTableIdentifier> tableIdentifierList = tableService().listTables(catalogMeta.getCatalogName(),
        TableTestHelper.TEST_DB_NAME);
    Assert.assertEquals(1, tableIdentifierList.size());
    Assert.assertEquals(tableMeta.getTableIdentifier(), tableIdentifierList.get(0).getIdentifier());

    // test create duplicate table
    Assert.assertThrows(AlreadyExistsException.class, () -> tableService().createTable(catalogMeta.getCatalogName(),
        tableMeta));

    tableMeta.setTableIdentifier(new TableIdentifier("unknown", TableTestHelper.TEST_DB_NAME,
        TableTestHelper.TEST_TABLE_NAME));
    // test create table with wrong catalog name
    Assert.assertThrows(ObjectNotExistsException.class, () -> tableService().createTable(catalogMeta.getCatalogName(),
        tableMeta));

    // test create table in not existed catalog
    Assert.assertThrows(ObjectNotExistsException.class, () -> tableService().createTable("unknown",
        tableMeta));

    tableMeta.setTableIdentifier(new TableIdentifier(TableTestHelper.TEST_CATALOG_NAME, "unknown",
        TableTestHelper.TEST_TABLE_NAME));
    // test create table in not existed database
    Assert.assertThrows(ObjectNotExistsException.class, () -> tableService().createTable(catalogMeta.getCatalogName(),
        tableMeta));

    tableMeta.setTableIdentifier(new TableIdentifier(TableTestHelper.TEST_CATALOG_NAME, TableTestHelper.TEST_DB_NAME,
        TableTestHelper.TEST_TABLE_NAME));
    // test drop table
    tableService().dropTableMetadata(tableMeta.getTableIdentifier(), true);
    Assert.assertEquals(0, tableService().listTables().size());
    Assert.assertEquals(0, tableService().listTables(catalogMeta.getCatalogName(),
        tableMeta.getTableIdentifier().getDatabase()).size());
    mixedTables.dropTableByMeta(tableMeta, true);

    // test drop not existed table
    Assert.assertThrows(ObjectNotExistsException.class,
        () -> tableService().dropTableMetadata(tableMeta.getTableIdentifier(), true));

    tableService().dropDatabase(catalogMeta.getCatalogName(), TableTestHelper.TEST_DB_NAME);
  }

  private TableMeta buildTable() {
    ConvertStructUtil.TableMetaBuilder builder =
        new ConvertStructUtil.TableMetaBuilder(TableTestHelper.TEST_TABLE_ID, tableTestHelper.tableSchema());
    String tableLocation = String.format("%s/%s/%s", catalogWarehouse, TableTestHelper.TEST_DB_NAME,
        TableTestHelper.TEST_TABLE_NAME);
    TableMeta tableMeta = builder.withPrimaryKeySpec(tableTestHelper.primaryKeySpec())
        .withProperties(tableTestHelper.tableProperties())
        .withTableLocation(tableLocation)
        .withChangeLocation(tableLocation + "/change")
        .withBaseLocation(tableLocation + "/base")
        .build();
    mixedTables.createTableByMeta(tableMeta, tableTestHelper.tableSchema(),
        tableTestHelper.primaryKeySpec(), tableTestHelper.partitionSpec());
    return tableMeta;
  }
}

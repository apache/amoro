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

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.MixedTables;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.utils.ConvertStructUtil;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

public class AMSTableTestBase extends TableServiceTestBase {
  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  private final CatalogTestHelper catalogTestHelper;
  private final TableTestHelper tableTestHelper;
  private String catalogWarehouse;
  private MixedTables mixedTables;
  private Catalog icebergCatalog;
  private CatalogMeta catalogMeta;
  private TableMeta tableMeta;

  private boolean autoInitTable;
  private ServerTableIdentifier serverTableIdentifier;

  public AMSTableTestBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    this(catalogTestHelper, tableTestHelper, false);
  }

  public AMSTableTestBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper,
      boolean autoInitTable) {
    this.catalogTestHelper = catalogTestHelper;
    this.tableTestHelper = tableTestHelper;
    this.autoInitTable = autoInitTable;
  }

  @Before
  public void init() throws IOException, TException {
    catalogWarehouse = temp.newFolder().getPath();
    catalogMeta = catalogTestHelper.buildCatalogMeta(catalogWarehouse);
    if (TableFormat.ICEBERG.equals(catalogTestHelper.tableFormat())) {
      icebergCatalog = catalogTestHelper.buildIcebergCatalog(catalogMeta);
    } else {
      mixedTables = catalogTestHelper.buildMixedTables(catalogMeta);
      tableMeta = buildTableMeta();
    }
    tableService().createCatalog(catalogMeta);
    Database database = new Database();
    database.setName(TableTestHelper.TEST_DB_NAME);
    TEST_HMS.getHiveClient().createDatabase(database);
    if (autoInitTable) {
      createDatabase();
      createTable();
      serverTableIdentifier = tableService().listTables().get(0);
    }
  }

  @After
  public void dispose() throws TException {
    if (autoInitTable) {
      dropTable();
      dropDatabase();
    }
    if (catalogMeta != null) {
      tableService().dropCatalog(catalogMeta.getCatalogName());
      TEST_HMS.getHiveClient().dropDatabase(TableTestHelper.TEST_DB_NAME, false, true);
    }
  }

  protected TableMeta buildTableMeta() {
    ConvertStructUtil.TableMetaBuilder builder =
        new ConvertStructUtil.TableMetaBuilder(TableTestHelper.TEST_TABLE_ID, tableTestHelper.tableSchema());
    String tableLocation = String.format("%s/%s/%s", catalogWarehouse, TableTestHelper.TEST_DB_NAME,
        TableTestHelper.TEST_TABLE_NAME);
    return builder.withPrimaryKeySpec(tableTestHelper.primaryKeySpec())
        .withProperties(tableTestHelper.tableProperties())
        .withTableLocation(tableLocation)
        .withChangeLocation(tableLocation + "/change")
        .withBaseLocation(tableLocation + "/base")
        .build();
  }

  protected void createDatabase() {
    if (TableFormat.ICEBERG.equals(catalogTestHelper.tableFormat())) {
      ((SupportsNamespaces)icebergCatalog).createNamespace(Namespace.of(TableTestHelper.TEST_DB_NAME));
    } else {
      tableService().createDatabase(TableTestHelper.TEST_CATALOG_NAME, TableTestHelper.TEST_DB_NAME);
    }
  }

  protected void dropDatabase() {
    if (TableFormat.ICEBERG.equals(catalogTestHelper.tableFormat())) {
      ((SupportsNamespaces)icebergCatalog).dropNamespace(Namespace.of(TableTestHelper.TEST_DB_NAME));
    } else {
      tableService().dropDatabase(TableTestHelper.TEST_CATALOG_NAME, TableTestHelper.TEST_DB_NAME);
    }
  }

  protected void createTable() {
    if (TableFormat.ICEBERG.equals(catalogTestHelper.tableFormat())) {
      icebergCatalog.createTable(
          TableIdentifier.of(TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME),
          tableTestHelper.tableSchema(),
          tableTestHelper.partitionSpec(), tableTestHelper.tableProperties());
      tableService().exploreExternalCatalog();
    } else {
      mixedTables.createTableByMeta(tableMeta, tableTestHelper.tableSchema(), tableTestHelper.primaryKeySpec(),
          tableTestHelper.partitionSpec());
      tableService().createTable(catalogMeta.getCatalogName(), tableMeta);
    }
  }

  protected void dropTable() {
    if (TableFormat.ICEBERG.equals(catalogTestHelper.tableFormat())) {
      icebergCatalog.dropTable(TableIdentifier.of(TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME));
      tableService().exploreExternalCatalog();
    } else {
      mixedTables.dropTableByMeta(tableMeta, true);
      tableService().dropTableMetadata(tableMeta.getTableIdentifier(), true);
    }
  }

  protected CatalogTestHelper catalogTestHelper() {
    return catalogTestHelper;
  }

  protected TableTestHelper tableTestHelper() {
    return tableTestHelper;
  }

  protected TableMeta tableMeta() {
    return tableMeta;
  }

  protected ServerTableIdentifier serverTableIdentifier() {
    return serverTableIdentifier;
  }
}

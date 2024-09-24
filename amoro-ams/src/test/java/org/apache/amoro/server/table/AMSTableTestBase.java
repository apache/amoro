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

package org.apache.amoro.server.table;

import org.apache.amoro.CommonUnifiedCatalog;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.MixedTables;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.MixedHiveCatalog;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.amoro.utils.ConvertStructUtil;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.List;

public class AMSTableTestBase extends TableServiceTestBase {
  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  @Rule public TemporaryFolder temp = new TemporaryFolder();
  private final CatalogTestHelper catalogTestHelper;
  private final TableTestHelper tableTestHelper;
  private String catalogWarehouse;
  private MixedTables mixedTables;
  private UnifiedCatalog externalCatalog;
  private CatalogMeta catalogMeta;

  private TableMeta tableMeta;

  private final boolean autoInitTable;
  private ServerTableIdentifier serverTableIdentifier;

  public AMSTableTestBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    this(catalogTestHelper, tableTestHelper, false);
  }

  public AMSTableTestBase(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper, boolean autoInitTable) {
    this.catalogTestHelper = catalogTestHelper;
    this.tableTestHelper = tableTestHelper;
    this.autoInitTable = autoInitTable;
  }

  @Before
  public void init() throws IOException, TException {
    catalogWarehouse = temp.newFolder().getPath();
    catalogMeta = catalogTestHelper.buildCatalogMeta(catalogWarehouse);
    // mixed-hive format only exists in external catalog
    if (catalogTestHelper.isInternalCatalog()) {
      if (TableFormat.MIXED_ICEBERG.equals(catalogTestHelper.tableFormat())) {
        mixedTables = catalogTestHelper.buildMixedTables(catalogMeta);
        tableMeta = buildTableMeta();
      }
    } else {
      externalCatalog = new CommonUnifiedCatalog(() -> catalogMeta, Maps.newHashMap());
      if (TableFormat.MIXED_HIVE.equals(catalogTestHelper.tableFormat())) {
        tableMeta = buildTableMeta();
      }
    }

    tableService().createCatalog(catalogMeta);
    try {
      Database database = new Database();
      database.setName(TableTestHelper.TEST_DB_NAME);
      TEST_HMS.getHiveClient().createDatabase(database);
    } catch (AlreadyExistsException e) {
      // pass
    }
    if (autoInitTable) {
      createDatabase();
      createTable();
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
        new ConvertStructUtil.TableMetaBuilder(
            TableTestHelper.TEST_TABLE_ID, tableTestHelper.tableSchema());
    String tableLocation =
        String.format(
            "%s/%s/%s",
            catalogWarehouse, TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME);
    return builder
        .withPrimaryKeySpec(tableTestHelper.primaryKeySpec())
        .withProperties(tableTestHelper.tableProperties())
        .withTableLocation(tableLocation)
        .withFormat(catalogTestHelper.tableFormat())
        .withChangeLocation(tableLocation + "/change")
        .withBaseLocation(tableLocation + "/base")
        .build();
  }

  protected void createDatabase() {
    if (externalCatalog == null) {
      InternalCatalog catalog =
          tableService().getInternalCatalog(TableTestHelper.TEST_CATALOG_NAME);
      if (!catalog.listDatabases().contains(TableTestHelper.TEST_DB_NAME)) {
        catalog.createDatabase(TableTestHelper.TEST_DB_NAME);
      }
    } else {
      List<String> databases = externalCatalog.listDatabases();
      if (!(databases != null && databases.contains(TableTestHelper.TEST_DB_NAME))) {
        externalCatalog.createDatabase(TableTestHelper.TEST_DB_NAME);
      }
    }
  }

  protected void dropDatabase() {
    if (externalCatalog == null) {
      InternalCatalog catalog =
          tableService().getInternalCatalog(TableTestHelper.TEST_CATALOG_NAME);
      catalog.dropDatabase(TableTestHelper.TEST_DB_NAME);
    } else {
      externalCatalog.dropDatabase(TableTestHelper.TEST_DB_NAME);
    }
  }

  protected void createTable() {
    if (externalCatalog == null) {
      mixedTables.createTableByMeta(
          tableMeta,
          tableTestHelper.tableSchema(),
          tableTestHelper.primaryKeySpec(),
          tableTestHelper.partitionSpec());
      TableMetadata tableMetadata = tableMetadata();
      tableService().createTable(catalogMeta.getCatalogName(), tableMetadata);
    } else {
      if (catalogTestHelper.tableFormat().equals(TableFormat.ICEBERG)) {
        createIcebergTable();
      } else if (catalogTestHelper.tableFormat().equals(TableFormat.MIXED_ICEBERG)) {
        createMixedIcebergTable();
      } else if (catalogTestHelper.tableFormat().equals(TableFormat.MIXED_HIVE)) {
        createMixedHiveTable();
      } else {
        throw new IllegalStateException("un-support format");
      }
      tableService().exploreExternalCatalog();
    }

    serverTableIdentifier = tableService().listManagedTables().get(0);
  }

  private void createMixedHiveTable() {
    // only create mixed hive table here !
    catalogMeta.putToCatalogProperties(
        CatalogMetaProperties.TABLE_FORMATS, TableFormat.MIXED_HIVE.name());
    MixedHiveCatalog catalog =
        (MixedHiveCatalog)
            CatalogLoader.createCatalog(
                catalogMeta.getCatalogName(),
                catalogMeta.getCatalogType(),
                catalogMeta.getCatalogProperties(),
                CatalogUtil.buildMetaStore(catalogMeta));
    catalog
        .newTableBuilder(tableTestHelper.id(), tableTestHelper.tableSchema())
        .withPartitionSpec(tableTestHelper.partitionSpec())
        .withProperties(tableTestHelper.tableProperties())
        .withPrimaryKeySpec(tableTestHelper.primaryKeySpec())
        .create();
  }

  private void createMixedIcebergTable() {
    MixedFormatCatalog catalog =
        CatalogLoader.createCatalog(
            catalogMeta.getCatalogName(),
            catalogMeta.getCatalogType(),
            catalogMeta.getCatalogProperties(),
            CatalogUtil.buildMetaStore(catalogMeta));
    catalog
        .newTableBuilder(tableTestHelper.id(), tableTestHelper.tableSchema())
        .withPartitionSpec(tableTestHelper.partitionSpec())
        .withProperties(tableTestHelper.tableProperties())
        .withPrimaryKeySpec(tableTestHelper.primaryKeySpec())
        .create();
  }

  private void createIcebergTable() {
    org.apache.iceberg.catalog.Catalog catalog = catalogTestHelper.buildIcebergCatalog(catalogMeta);
    catalog.createTable(
        TableIdentifier.of(tableTestHelper.id().getDatabase(), tableTestHelper.id().getTableName()),
        tableTestHelper.tableSchema(),
        tableTestHelper.partitionSpec(),
        tableTestHelper.tableProperties());
  }

  protected void dropTable() {
    if (externalCatalog == null) {
      mixedTables.dropTableByMeta(tableMeta, true);
      tableService().dropTableMetadata(tableMeta.getTableIdentifier(), true);
    } else {
      String database = tableTestHelper.id().getDatabase();
      String table = tableTestHelper.id().getTableName();
      externalCatalog.dropTable(database, table, true);
      tableService().exploreExternalCatalog();
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

  protected CatalogMeta catalogMeta() {
    return catalogMeta;
  }

  protected TableMetadata tableMetadata() {
    return new TableMetadata(
        ServerTableIdentifier.of(tableMeta.getTableIdentifier(), catalogTestHelper.tableFormat()),
        tableMeta,
        catalogMeta);
  }

  protected ServerTableIdentifier serverTableIdentifier() {
    return serverTableIdentifier;
  }

  protected void validateMixedTable(MixedTable mixedTable) {
    Assert.assertEquals(catalogTestHelper().tableFormat(), mixedTable.format());
    Assert.assertEquals(TableTestHelper.TEST_TABLE_ID, mixedTable.id());
    Assert.assertEquals(tableTestHelper().tableSchema().asStruct(), mixedTable.schema().asStruct());
    Assert.assertEquals(tableTestHelper().partitionSpec(), mixedTable.spec());
    Assert.assertEquals(
        tableTestHelper().primaryKeySpec().primaryKeyExisted(), mixedTable.isKeyedTable());
  }

  protected void validateTableRuntime(TableRuntime tableRuntime) {
    Assert.assertEquals(serverTableIdentifier(), tableRuntime.getTableIdentifier());
  }
}

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

import static org.apache.amoro.TableTestHelper.TEST_DB_NAME;
import static org.apache.amoro.TableTestHelper.TEST_TABLE_NAME;
import static org.apache.amoro.catalog.CatalogTestHelper.TEST_CATALOG_NAME;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.CommonUnifiedCatalog;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.catalog.ExternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.iceberg.CatalogUtil;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestSyncTableOfExternalCatalog extends AMSTableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {TestedCatalogs.hadoopCatalog(TableFormat.ICEBERG), new BasicTableTestHelper(true, true)},
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      }
    };
  }

  public TestSyncTableOfExternalCatalog(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  private final Persistency persistency = new Persistency();

  private ExternalCatalog initExternalCatalog() {
    ServerCatalog serverCatalog = catalogManager().getServerCatalog(TEST_CATALOG_NAME);
    return catalogTestHelper().isInternalCatalog() ? null : (ExternalCatalog) serverCatalog;
  }

  /** Test synchronization after creating a table. */
  @Test
  public void testSynchronizationAfterCreateTable() {
    // create table and sync table to Amoro server catalog
    createTable();

    // test list tables
    List<TableRuntimeMeta> tableRuntimeMetaListAfterAddTable = persistency.getTableRuntimeMetas();
    List<TableRuntimeMeta> tableRuntimeMetaListForOptimizerGroupAfterAddTable =
        persistency.getTableRuntimeMetasForOptimizerGroup(defaultResourceGroup().getName());
    Assert.assertEquals(
        tableRuntimeMetaListForOptimizerGroupAfterAddTable.size(),
        tableRuntimeMetaListAfterAddTable.size());

    dropTable();
    dropDatabase();
  }

  /** Test synchronization after dropping a table. */
  @Test
  public void testSynchronizationAfterDropTable() {
    createTable();
    // drop table and sync table to Amoro server catalog
    dropTable();

    // test list tables
    List<TableRuntimeMeta> tableRuntimeMetaListAfterDropTable = persistency.getTableRuntimeMetas();
    List<TableRuntimeMeta> tableRuntimeMetaListForOptimizerGroupAfterDropTable =
        persistency.getTableRuntimeMetasForOptimizerGroup(defaultResourceGroup().getName());
    Assert.assertEquals(
        tableRuntimeMetaListForOptimizerGroupAfterDropTable.size(),
        tableRuntimeMetaListAfterDropTable.size());

    dropDatabase();
  }

  /**
   * Test synchronization in the anomaly scenario where the tableIdentifier is remained in
   * persistence but iceberg table does not exist.
   */
  @Test
  public void testSynchronizationWithLegacyTableIdentifierAndNonExistingIcebergTable() {
    // Simulate the anomaly scenario by only adding the table identifier in the persistent table
    createDatabase();
    ExternalCatalog externalCatalog = initExternalCatalog();

    persistency.addTableIdentifier(
        TEST_CATALOG_NAME, TEST_DB_NAME, TEST_TABLE_NAME, TableFormat.ICEBERG);

    // Verify that the table not exists
    List<TableIDWithFormat> tableIdentifierList =
        catalogManager().getServerCatalog(TEST_CATALOG_NAME).listTables(TEST_DB_NAME);
    Assert.assertEquals(0, tableIdentifierList.size());

    // Verify that the tables do not match exactly in tableIdentifier and tableRuntime
    List<TableRuntimeMeta> tableRuntimeMetaList = persistency.getTableRuntimeMetas();
    List<ServerTableIdentifier> serverTableIdentifierList = tableManager().listManagedTables();
    Assert.assertEquals(0, tableRuntimeMetaList.size());
    Assert.assertEquals(1, serverTableIdentifierList.size());

    // Verify that the synchronization works by firing exploreExternalCatalog
    tableService().exploreExternalCatalog(externalCatalog);

    List<TableRuntimeMeta> tableRuntimeMetaListAfterSync = persistency.getTableRuntimeMetas();
    List<ServerTableIdentifier> serverTableIdentifierListAfterSync =
        tableManager().listManagedTables();
    Assert.assertEquals(0, tableRuntimeMetaListAfterSync.size());
    Assert.assertEquals(0, serverTableIdentifierListAfterSync.size());

    dropDatabase();
  }

  /**
   * Test synchronization in the anomaly scenario where the tableRuntime is remained but iceberg
   * table does not exist.
   *
   * <p>NOTE: exploreExternalCatalog does not work.
   */
  @Test
  public void testSynchronizationWithLegacyTableRuntimeAndNonExistingIcebergTable() {
    ExternalCatalog externalCatalog = initExternalCatalog();
    // Simulate the anomaly scenario by only removing the table runtime in the persistent table
    // after dropping table
    createTable();
    dropTableOnly();
    persistency.deleteTableIdentifier(
        serverTableIdentifier().getIdentifier().buildTableIdentifier());

    // Verify that the table is dropped
    List<TableIDWithFormat> tableIdentifierList =
        catalogManager().getServerCatalog(TEST_CATALOG_NAME).listTables(TEST_DB_NAME);
    Assert.assertEquals(0, tableIdentifierList.size());

    // Verify that the tables do not match exactly in tableIdentifier and tableRuntime
    List<ServerTableIdentifier> serverTableIdentifierList = tableManager().listManagedTables();
    List<TableRuntimeMeta> tableRuntimeMetaListForOptimizerGroup =
        persistency.getTableRuntimeMetasForOptimizerGroup(defaultResourceGroup().getName());
    Assert.assertEquals(0, serverTableIdentifierList.size());
    Assert.assertEquals(1, tableRuntimeMetaListForOptimizerGroup.size());

    // Verify that the synchronization does not work by firing exploreExternalCatalog
    tableService().exploreExternalCatalog(externalCatalog);

    List<ServerTableIdentifier> serverTableIdentifierListAfterSync =
        tableManager().listManagedTables();
    List<TableRuntimeMeta> tableRuntimeMetaListForOptimizerGroupAfterSync =
        persistency.getTableRuntimeMetasForOptimizerGroup(defaultResourceGroup().getName());
    Assert.assertEquals(0, serverTableIdentifierListAfterSync.size());
    Assert.assertEquals(1, tableRuntimeMetaListForOptimizerGroupAfterSync.size());

    // The existed tableRuntime will prevent the table from being added again
    Assert.assertThrows(IndexOutOfBoundsException.class, this::createTable);

    MetricRegistry globalRegistry = MetricManager.getInstance().getGlobalRegistry();
    globalRegistry.getMetrics().keySet().forEach(globalRegistry::unregister);
    persistency.deleteTableRuntime(serverTableIdentifier().getId());
    dropTable();
    dropDatabase();
  }

  /**
   * Test synchronization in the anomaly scenario where the tableIdentifier is remained in
   * persistence and the tableRuntime is remained in memory but iceberg table does not exist.
   */
  @Test
  public void
      testSynchronizationWithLegacyTableIdentifierAndLegacyTableRuntimeAndNonExistingIcebergTable() {
    ExternalCatalog externalCatalog = initExternalCatalog();
    // Simulate the anomaly scenario by only removing the table runtime in the persistent table
    // after dropping table
    createTable();
    dropTableOnly();
    List<TableRuntimeMeta> tableRuntimeMetaListForOptimizerGroup =
        persistency.getTableRuntimeMetasForOptimizerGroup(defaultResourceGroup().getName());
    persistency.deleteTableRuntime(tableRuntimeMetaListForOptimizerGroup.get(0).getTableId());

    // Verify that the table is dropped
    List<TableIDWithFormat> tableIdentifierList =
        catalogManager().getServerCatalog(TEST_CATALOG_NAME).listTables(TEST_DB_NAME);
    Assert.assertEquals(0, tableIdentifierList.size());

    // Verify that the tables do not match exactly in tableIdentifier and tableRuntime
    List<TableRuntimeMeta> tableRuntimeMetaList = persistency.getTableRuntimeMetas();
    List<ServerTableIdentifier> serverTableIdentifierList = tableManager().listManagedTables();
    Assert.assertEquals(1, serverTableIdentifierList.size());
    Assert.assertEquals(0, tableRuntimeMetaList.size());

    // Verify that the synchronization works by firing exploreExternalCatalog
    tableService().exploreExternalCatalog(externalCatalog);

    List<TableRuntimeMeta> tableRuntimeMetaListAfterSync = persistency.getTableRuntimeMetas();
    List<ServerTableIdentifier> serverTableIdentifierListAfterSync =
        tableManager().listManagedTables();
    Assert.assertEquals(0, serverTableIdentifierListAfterSync.size());
    Assert.assertEquals(0, tableRuntimeMetaListAfterSync.size());

    // Verify that recreating the table works
    createTable();
    tableRuntimeMetaListAfterSync = persistency.getTableRuntimeMetas();
    serverTableIdentifierListAfterSync = tableManager().listManagedTables();
    Assert.assertEquals(1, serverTableIdentifierListAfterSync.size());
    Assert.assertEquals(1, tableRuntimeMetaListAfterSync.size());

    dropTable();
    dropDatabase();
  }

  @Mock private ExternalCatalog externalCatalog;

  @Test
  public void exploreExternalCatalog_ListDatabasesException() {
    createTable();
    ServerCatalog originalCatalog = catalogManager().getServerCatalog(TEST_CATALOG_NAME);

    // test list tables
    List<TableRuntimeMeta> tableRuntimeMetaListAfterAddTable = persistency.getTableRuntimeMetas();
    Assert.assertEquals(1, tableRuntimeMetaListAfterAddTable.size());

    MockitoAnnotations.openMocks(this);
    when(externalCatalog.name()).thenReturn(TEST_CATALOG_NAME);
    when(externalCatalog.toString()).thenReturn(originalCatalog.toString());
    when(externalCatalog.getMetadata()).thenReturn(originalCatalog.getMetadata());
    when(externalCatalog.listDatabases()).thenThrow(new RuntimeException("List databases error"));

    // This should throw an exception
    Assert.assertThrows(
        "List databases error",
        RuntimeException.class,
        () -> tableService().exploreExternalCatalog(externalCatalog));

    verify(externalCatalog, times(1)).listDatabases();

    // test tableRuntime not removed
    List<TableRuntimeMeta> tableRuntimeMetaListAfterExploreCatalog =
        persistency.getTableRuntimeMetas();
    Assert.assertEquals(1, tableRuntimeMetaListAfterExploreCatalog.size());

    dropTable();
    dropDatabase();
  }

  @Test
  public void exploreExternalCatalog_ListTablesException() {
    createTable();
    ServerCatalog originalCatalog = catalogManager().getServerCatalog(TEST_CATALOG_NAME);

    // test list tables
    List<TableRuntimeMeta> tableRuntimeMetaListAfterAddTable = persistency.getTableRuntimeMetas();
    Assert.assertEquals(1, tableRuntimeMetaListAfterAddTable.size());

    MockitoAnnotations.openMocks(this);
    when(externalCatalog.name()).thenReturn(TEST_CATALOG_NAME);
    when(externalCatalog.toString()).thenReturn(originalCatalog.toString());
    when(externalCatalog.getMetadata()).thenReturn(originalCatalog.getMetadata());
    when(externalCatalog.listDatabases()).thenReturn(originalCatalog.listDatabases());
    when(externalCatalog.listTables(TEST_DB_NAME))
        .thenThrow(new RuntimeException("List tables in " + TEST_DB_NAME + " error"));

    // This should throw an exception
    Assert.assertThrows(
        "List tables in " + TEST_DB_NAME + " error",
        RuntimeException.class,
        () -> tableService().exploreExternalCatalog(externalCatalog));

    verify(externalCatalog, times(1)).listTables(TEST_DB_NAME);

    // test tableRuntime not removed
    List<TableRuntimeMeta> tableRuntimeMetaListAfterExploreCatalog =
        persistency.getTableRuntimeMetas();
    Assert.assertEquals(1, tableRuntimeMetaListAfterExploreCatalog.size());

    dropTable();
    dropDatabase();
  }

  @Test
  public void exploreMultipleExternalCatalog_ListTablesException() throws TException, IOException {
    createTable();
    ServerCatalog originalCatalog = catalogManager().getServerCatalog(TEST_CATALOG_NAME);

    // test list tables
    List<TableRuntimeMeta> tableRuntimeMetaListAfterAddTable = persistency.getTableRuntimeMetas();
    Assert.assertEquals(1, tableRuntimeMetaListAfterAddTable.size());

    MockitoAnnotations.openMocks(this);
    when(externalCatalog.name()).thenReturn(TEST_CATALOG_NAME);
    when(externalCatalog.toString()).thenReturn(originalCatalog.toString());
    when(externalCatalog.getMetadata()).thenReturn(originalCatalog.getMetadata());
    when(externalCatalog.listDatabases()).thenReturn(originalCatalog.listDatabases());
    when(externalCatalog.listTables(TEST_DB_NAME))
        .thenThrow(new RuntimeException("List tables in " + TEST_DB_NAME + " error"));

    catalogManager().setServerCatalog(externalCatalog);

    // create a new catalog new_catalog with a new db and a new table new_table
    String newCatalogName = "new_catalog";
    String newDbName = "new_db";
    String newTableName = "new_table";
    UnifiedCatalog newCatalog = createNewCatalogTable(newCatalogName, newDbName, newTableName);

    // This should not throw an exception, and log an error message "TableExplorer error when
    // explore table runtimes for catalog:test_catalog". Meanwhile, the newly added catalog can be
    // synchronized normally
    // without being affected.
    tableService().exploreTableRuntimes();

    // test tableRuntime of test_table not removed and tableRuntime of new_table added
    List<TableRuntimeMeta> tableRuntimeMetaListAfterExploreCatalog =
        persistency.getTableRuntimeMetas();
    Assert.assertEquals(2, tableRuntimeMetaListAfterExploreCatalog.size());
    Assert.assertTrue(
        tableRuntimeMetaListAfterExploreCatalog.stream()
            .anyMatch(tableRuntimeMeta -> tableRuntimeMeta.getTableName().equals(TEST_TABLE_NAME)));
    Assert.assertTrue(
        tableRuntimeMetaListAfterExploreCatalog.stream()
            .anyMatch(tableRuntimeMeta -> tableRuntimeMeta.getTableName().equals(newTableName)));

    // test tableRuntime of new_table removed
    disposeNewCatalogTable(newCatalog, newCatalogName, newDbName, newTableName);
    // This should not throw an exception, but log an error message "TableExplorer error when
    // explore table runtimes for catalog:test_catalog". Meanwhile, the removed catalog and
    // tableRuntime of new_table can
    // be synchronized normally
    // without being affected.
    tableService().exploreTableRuntimes();

    List<TableRuntimeMeta> tableRuntimeMetaListAfterDisposeNewCatalog =
        persistency.getTableRuntimeMetas();
    Assert.assertEquals(1, tableRuntimeMetaListAfterDisposeNewCatalog.size());
    Assert.assertTrue(
        tableRuntimeMetaListAfterExploreCatalog.stream()
            .anyMatch(tableRuntimeMeta -> tableRuntimeMeta.getTableName().equals(TEST_TABLE_NAME)));
    Assert.assertFalse(
        tableRuntimeMetaListAfterDisposeNewCatalog.stream()
            .anyMatch(tableRuntimeMeta -> tableRuntimeMeta.getTableName().equals(newTableName)));

    catalogManager().setServerCatalog(originalCatalog);
    dropTable();
    dropDatabase();
  }

  private UnifiedCatalog createNewCatalogTable(String catalogName, String dbName, String tableName)
      throws IOException, TException {
    // create new catalog
    String warehouseDir = temp.newFolder().getPath();
    Map<String, String> properties = Maps.newHashMap();
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, warehouseDir);
    CatalogMeta catalogMeta =
        CatalogTestHelpers.buildCatalogMeta(
            catalogName,
            CatalogMetaProperties.CATALOG_TYPE_HADOOP,
            properties,
            TableFormat.ICEBERG);
    UnifiedCatalog externalCatalog = new CommonUnifiedCatalog(() -> catalogMeta, Maps.newHashMap());
    catalogManager().createCatalog(catalogMeta);

    // create new database
    try {
      Database database = new Database();
      database.setName(dbName);
      TEST_HMS.getHiveClient().createDatabase(database);
    } catch (AlreadyExistsException e) {
      // pass
    }
    List<String> databases = externalCatalog.listDatabases();
    if (!(databases != null && databases.contains(dbName))) {
      externalCatalog.createDatabase(dbName);
    }
    // create new table
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(
        CatalogUtil.ICEBERG_CATALOG_TYPE, CatalogUtil.ICEBERG_CATALOG_TYPE_HADOOP);
    org.apache.iceberg.catalog.Catalog catalog =
        CatalogUtil.buildIcebergCatalog(catalogName, catalogProperties, new Configuration());
    catalog.createTable(
        org.apache.iceberg.catalog.TableIdentifier.of(dbName, tableName),
        tableTestHelper().tableSchema(),
        tableTestHelper().partitionSpec(),
        tableTestHelper().tableProperties());

    return externalCatalog;
  }

  private void disposeNewCatalogTable(
      UnifiedCatalog externalCatalog, String catalogName, String dbName, String tableName)
      throws TException {
    // drop table
    externalCatalog.dropTable(dbName, tableName, true);
    // drop database
    externalCatalog.dropDatabase(dbName);
    TEST_HMS.getHiveClient().dropDatabase(dbName, false, true);
    // drop catalog
    catalogManager().dropCatalog(catalogName);
  }

  private static class Persistency extends PersistentBase {
    public void addTableIdentifier(
        String catalog, String database, String tableName, TableFormat format) {
      ServerTableIdentifier tableIdentifier =
          ServerTableIdentifier.of(catalog, database, tableName, format);
      doAs(TableMetaMapper.class, mapper -> mapper.insertTable(tableIdentifier));
    }

    public void deleteTableIdentifier(TableIdentifier tableIdentifier) {
      doAs(
          TableMetaMapper.class,
          mapper ->
              mapper.deleteTableIdByName(
                  tableIdentifier.getCatalog(),
                  tableIdentifier.getDatabase(),
                  tableIdentifier.getTableName()));
    }

    public void deleteTableRuntime(Long tableId) {
      doAs(TableMetaMapper.class, mapper -> mapper.deleteOptimizingRuntime(tableId));
    }

    public List<TableRuntimeMeta> getTableRuntimeMetasForOptimizerGroup(String optimizerGroup) {
      return getAs(
          TableMetaMapper.class,
          mapper -> mapper.selectTableRuntimesForOptimizerGroup(optimizerGroup, null, null, null));
    }

    public List<TableRuntimeMeta> getTableRuntimeMetas() {
      return getAs(TableMetaMapper.class, TableMetaMapper::selectTableRuntimeMetas);
    }
  }
}

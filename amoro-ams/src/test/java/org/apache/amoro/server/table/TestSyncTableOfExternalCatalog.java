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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.server.catalog.ExternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

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

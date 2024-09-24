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
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.Blocker;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.exception.AlreadyExistsException;
import org.apache.amoro.server.exception.BlockerConflictException;
import org.apache.amoro.server.exception.ObjectNotExistsException;
import org.apache.amoro.table.blocker.RenewableBlocker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestTableService extends AMSTableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        TestedCatalogs.internalCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      }
    };
  }

  public TestTableService(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testCreateAndDropTable() {
    ServerCatalog serverCatalog = tableService().getServerCatalog(TEST_CATALOG_NAME);
    InternalCatalog internalCatalog =
        catalogTestHelper().isInternalCatalog() ? (InternalCatalog) serverCatalog : null;

    if (catalogTestHelper().isInternalCatalog()) {
      assert internalCatalog != null;
      internalCatalog.createDatabase(TEST_DB_NAME);
    }

    // test create table
    createTable();
    if (catalogTestHelper().isInternalCatalog()) {
      assert internalCatalog != null;
      Assert.assertEquals(
          tableMeta(),
          internalCatalog.loadTableMetadata(TEST_DB_NAME, TEST_TABLE_NAME).buildTableMeta());
    }

    // test list tables
    List<TableIDWithFormat> tableIdentifierList =
        tableService().getServerCatalog(TEST_CATALOG_NAME).listTables(TEST_DB_NAME);
    Assert.assertEquals(1, tableIdentifierList.size());
    Assert.assertEquals(
        tableMeta().getTableIdentifier(),
        tableIdentifierList.get(0).getIdentifier().buildTableIdentifier());

    // test list table metadata
    if (catalogTestHelper().isInternalCatalog()) {
      assert internalCatalog != null;
      List<TableMetadata> tableMetadataList =
          internalCatalog.listTables().stream()
              .map(i -> internalCatalog.loadTableMetadata(i.database(), i.table()))
              .collect(Collectors.toList());

      Assert.assertEquals(1, tableMetadataList.size());
      Assert.assertEquals(tableMeta(), tableMetadataList.get(0).buildTableMeta());
      tableMetadataList = internalCatalog.listTableMetadataInDatabase(TEST_DB_NAME);
      Assert.assertEquals(1, tableMetadataList.size());
      Assert.assertEquals(tableMeta(), tableMetadataList.get(0).buildTableMeta());
    }

    // test table exist
    Assert.assertTrue(serverCatalog.tableExists(TEST_DB_NAME, TEST_TABLE_NAME));

    // test create duplicate table
    if (catalogTestHelper().isInternalCatalog()) {
      Assert.assertThrows(
          AlreadyExistsException.class,
          () -> tableService().createTable(TEST_CATALOG_NAME, tableMetadata()));
    }

    // test create table with wrong catalog name
    Assert.assertThrows(
        ObjectNotExistsException.class,
        () -> {
          TableMetadata copyMetadata =
              new TableMetadata(serverTableIdentifier(), tableMeta(), catalogMeta());
          copyMetadata.getTableIdentifier().setCatalog("unknown");
          tableService().createTable("unknown", copyMetadata);
        });

    // test create table in not existed catalog
    Assert.assertThrows(
        ObjectNotExistsException.class,
        () -> {
          TableMetadata copyMetadata =
              new TableMetadata(serverTableIdentifier(), tableMeta(), catalogMeta());
          copyMetadata.getTableIdentifier().setCatalog("unknown");
          tableService().createTable("unknown", copyMetadata);
        });

    if (catalogTestHelper().tableFormat().equals(TableFormat.MIXED_ICEBERG)) {
      // test create table in not existed database
      Assert.assertThrows(
          ObjectNotExistsException.class,
          () -> {
            TableMetadata copyMetadata =
                new TableMetadata(serverTableIdentifier(), tableMeta(), catalogMeta());
            copyMetadata.getTableIdentifier().setDatabase("unknown");
            tableService().createTable("unknown", copyMetadata);
          });
    }

    // test drop table
    dropTable();
    Assert.assertEquals(0, tableService().listManagedTables().size());
    Assert.assertEquals(0, serverCatalog.listTables(TEST_DB_NAME).size());
    Assert.assertEquals(0, serverCatalog.listTables().size());
    if (catalogTestHelper().isInternalCatalog()) {
      assert internalCatalog != null;
      Assert.assertEquals(0, internalCatalog.listTableMetadataInDatabase(TEST_DB_NAME).size());
    }
    Assert.assertFalse(serverCatalog.tableExists(TEST_DB_NAME, TEST_TABLE_NAME));

    // test drop not existed table
    Assert.assertThrows(
        ObjectNotExistsException.class,
        () -> tableService().dropTableMetadata(tableMeta().getTableIdentifier(), true));

    dropDatabase();
  }

  @Test
  public void testBlockAndRelease() {
    createDatabase();
    createTable();
    TableIdentifier tableIdentifier = serverTableIdentifier().getIdentifier();

    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    Blocker block = tableService().block(tableIdentifier, operations, getProperties());
    assertBlocker(block, operations);
    assertBlockerCnt(1);
    assertBlocked(BlockableOperation.OPTIMIZE);
    assertBlocked(BlockableOperation.BATCH_WRITE);

    tableService().releaseBlocker(tableIdentifier, block.getBlockerId());
    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    dropTable();
    dropDatabase();
  }

  @Test
  public void testBlockConflict() {
    createDatabase();
    createTable();
    TableIdentifier tableIdentifier = serverTableIdentifier().getIdentifier();

    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    Blocker block = tableService().block(tableIdentifier, operations, getProperties());

    Assert.assertThrows(
        "should be conflict",
        BlockerConflictException.class,
        () -> tableService().block(tableIdentifier, operations, getProperties()));

    assertBlocker(block, operations);
    assertBlockerCnt(1);
    assertBlocked(BlockableOperation.OPTIMIZE);
    assertBlocked(BlockableOperation.BATCH_WRITE);

    tableService().releaseBlocker(tableIdentifier, block.getBlockerId());
    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    dropTable();
    dropDatabase();
  }

  @Test
  public void testRenewBlocker() throws InterruptedException {
    createDatabase();
    createTable();
    TableIdentifier tableIdentifier = serverTableIdentifier().getIdentifier();

    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    Blocker block = tableService().block(tableIdentifier, operations, getProperties());
    Thread.sleep(1);

    tableService().renewBlocker(tableIdentifier, block.getBlockerId());
    assertBlockerCnt(1);
    assertBlocked(BlockableOperation.OPTIMIZE);
    assertBlocked(BlockableOperation.BATCH_WRITE);

    assertBlocker(block, operations);
    assertBlockerCnt(1);
    assertBlocked(BlockableOperation.OPTIMIZE);
    assertBlocked(BlockableOperation.BATCH_WRITE);
    assertBlockerRenewed(tableService().getBlockers(tableIdentifier).get(0));

    tableService().releaseBlocker(tableIdentifier, block.getBlockerId());
    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    dropTable();
    dropDatabase();
  }

  @Test
  public void testAutoIncrementBlockerId() {
    createDatabase();
    createTable();
    TableIdentifier tableIdentifier = serverTableIdentifier().getIdentifier();

    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    Blocker block = tableService().block(tableIdentifier, operations, getProperties());

    tableService().releaseBlocker(tableIdentifier, block.getBlockerId());

    Blocker block2 = tableService().block(tableIdentifier, operations, getProperties());

    Assert.assertEquals(
        Long.parseLong(block2.getBlockerId()) - Long.parseLong(block.getBlockerId()), 1);

    tableService().releaseBlocker(tableIdentifier, block2.getBlockerId());

    dropTable();
    dropDatabase();
  }

  private void assertBlocker(Blocker block, List<BlockableOperation> operations) {
    Assert.assertEquals(operations.size(), block.getOperations().size());
    operations.forEach(operation -> Assert.assertTrue(block.getOperations().contains(operation)));
    Assert.assertEquals(getProperties().size() + 3, block.getProperties().size());
    getProperties()
        .forEach((key, value) -> Assert.assertEquals(block.getProperties().get(key), value));
    long timeout = AmoroManagementConf.BLOCKER_TIMEOUT.defaultValue();
    Assert.assertEquals(timeout + "", block.getProperties().get(RenewableBlocker.BLOCKER_TIMEOUT));

    Assert.assertEquals(
        timeout,
        Long.parseLong(block.getProperties().get(RenewableBlocker.EXPIRATION_TIME_PROPERTY))
            - Long.parseLong(block.getProperties().get(RenewableBlocker.CREATE_TIME_PROPERTY)));
  }

  private void assertBlockerRenewed(Blocker block) {
    long timeout = AmoroManagementConf.BLOCKER_TIMEOUT.defaultValue();
    long actualTimeout =
        Long.parseLong(block.getProperties().get(RenewableBlocker.EXPIRATION_TIME_PROPERTY))
            - Long.parseLong(block.getProperties().get(RenewableBlocker.CREATE_TIME_PROPERTY));
    Assert.assertTrue("actualTimeout is " + actualTimeout, actualTimeout > timeout);
  }

  private void assertNotBlocked(BlockableOperation operation) {
    Assert.assertFalse(isBlocked(operation));
    Assert.assertFalse(isTableRuntimeBlocked(operation));
  }

  private void assertBlocked(BlockableOperation operation) {
    Assert.assertTrue(isBlocked(operation));
    Assert.assertTrue(isTableRuntimeBlocked(operation));
  }

  private boolean isBlocked(BlockableOperation operation) {
    return tableService().getBlockers(serverTableIdentifier().getIdentifier()).stream()
        .anyMatch(blocker -> blocker.getOperations().contains(operation));
  }

  private boolean isTableRuntimeBlocked(BlockableOperation operation) {
    return tableService().getRuntime(serverTableIdentifier().getId()).isBlocked(operation);
  }

  private void assertBlockerCnt(int i) {
    List<Blocker> blockers;
    blockers = tableService().getBlockers(serverTableIdentifier().getIdentifier());
    Assert.assertEquals(i, blockers.size());
  }

  private Map<String, String> getProperties() {
    Map<String, String> properties = new HashMap<>();
    properties.put("test_key", "test_value");
    properties.put("2", "");
    properties.put("3", null);
    return properties;
  }
}

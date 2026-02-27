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

package org.apache.amoro.catalog;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.mixed.BasicMixedIcebergCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.blocker.TableBlockerManager;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.util.PropertyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestMixedCatalog extends CatalogTestBase {

  public TestMixedCatalog(CatalogTestHelper catalogTestHelper) {
    super(catalogTestHelper);
  }

  @Parameterized.Parameters(name = "testFormat = {0}")
  public static Object[] parameters() {
    return new Object[] {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG)};
  }

  @Before
  public void before() {
    Assert.assertEquals(expectCatalogImpl(), getMixedFormatCatalog().getClass().getName());
  }

  protected String expectCatalogImpl() {
    return BasicMixedIcebergCatalog.class.getName();
  }

  protected void validateCreatedTable(MixedTable table, boolean withKey) throws Exception {
    Assert.assertEquals(getCreateTableSchema().asStruct(), table.schema().asStruct());
    Assert.assertEquals(getCreateTableSpec(), table.spec());
    Assert.assertEquals(TableTestHelper.TEST_TABLE_ID, table.id());
    Assert.assertEquals(withKey, table.isKeyedTable());

    if (table.isKeyedTable()) {
      KeyedTable keyedTable = (KeyedTable) table;
      Assert.assertEquals(BasicTableTestHelper.PRIMARY_KEY_SPEC, keyedTable.primaryKeySpec());
      Assert.assertEquals(
          getCreateTableSchema().asStruct(), keyedTable.baseTable().schema().asStruct());
      Assert.assertEquals(getCreateTableSpec(), keyedTable.baseTable().spec());
      Assert.assertEquals(
          getCreateTableSchema().asStruct(), keyedTable.changeTable().schema().asStruct());
      Assert.assertEquals(getCreateTableSpec(), keyedTable.changeTable().spec());
      assertIcebergTableStore(table.asKeyedTable().baseTable(), true, true);
      assertIcebergTableStore(table.asKeyedTable().changeTable(), false, true);
    } else {
      assertIcebergTableStore(table.asUnkeyedTable(), true, false);
    }
  }

  @Test
  public void testCreateAndDropDatabase() {
    String createDbName = TableTestHelper.TEST_DB_NAME;
    Assert.assertFalse(getMixedFormatCatalog().listDatabases().contains(createDbName));
    getMixedFormatCatalog().createDatabase(createDbName);
    Assert.assertTrue(getMixedFormatCatalog().listDatabases().contains(createDbName));
    getMixedFormatCatalog().dropDatabase(createDbName);
    Assert.assertFalse(getMixedFormatCatalog().listDatabases().contains(createDbName));
  }

  @Test
  public void testCreateDuplicateDatabase() {
    String createDbName = TableTestHelper.TEST_DB_NAME;
    Assert.assertFalse(getMixedFormatCatalog().listDatabases().contains(createDbName));
    getMixedFormatCatalog().createDatabase(createDbName);
    Assert.assertTrue(getMixedFormatCatalog().listDatabases().contains(createDbName));
    Assert.assertThrows(
        AlreadyExistsException.class, () -> getMixedFormatCatalog().createDatabase(createDbName));
    getMixedFormatCatalog().dropDatabase(createDbName);
  }

  @Test
  public void testCreateTableWithCatalogTablePropertiesKeyed() throws Exception {
    getMixedFormatCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    testCreateTableWithCatalogTableProperties(true);
  }

  @Test
  public void testCreateTableWithCatalogTablePropertiesUnKeyed() throws Exception {
    getMixedFormatCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    testCreateTableWithCatalogTableProperties(false);
  }

  private void testCreateTableWithCatalogTableProperties(boolean withKey) throws Exception {
    MixedTable mixedTable = createTestTable(withKey);
    validateCreatedTable(mixedTable, withKey);

    // equal to catalog default value
    mixedTable = getMixedFormatCatalog().loadTable(TableTestHelper.TEST_TABLE_ID);
    validateCreatedTable(mixedTable, withKey);
    Assert.assertTrue(
        PropertyUtil.propertyAsBoolean(
            mixedTable.properties(),
            TableProperties.ENABLE_SELF_OPTIMIZING,
            TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT));
    getMixedFormatCatalog().dropTable(TableTestHelper.TEST_TABLE_ID, true);

    CatalogMeta testCatalogMeta =
        TEST_AMS.getAmsHandler().getCatalog(CatalogTestHelper.TEST_CATALOG_NAME);
    TEST_AMS
        .getAmsHandler()
        .updateMeta(
            testCatalogMeta,
            CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.ENABLE_SELF_OPTIMIZING,
            "false");
    refreshMixedFormatCatalog();
    mixedTable = createTestTable(withKey);
    validateCreatedTable(mixedTable, withKey);
    // equal to catalog set value
    Assert.assertFalse(
        PropertyUtil.propertyAsBoolean(
            mixedTable.properties(),
            TableProperties.ENABLE_SELF_OPTIMIZING,
            TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT));

    mixedTable = getMixedFormatCatalog().loadTable(TableTestHelper.TEST_TABLE_ID);
    validateCreatedTable(mixedTable, withKey);
    Assert.assertFalse(
        PropertyUtil.propertyAsBoolean(
            mixedTable.properties(),
            TableProperties.ENABLE_SELF_OPTIMIZING,
            TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT));
    getMixedFormatCatalog().dropTable(TableTestHelper.TEST_TABLE_ID, true);
  }

  @Test
  public void testCreateTableWithNewCatalogLogPropertiesKeyed() throws Exception {
    getMixedFormatCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    testCreateTableWithNewCatalogLogProperties(true);
  }

  @Test
  public void testCreateTableWithNewCatalogLogPropertiesUnKeyed() throws Exception {
    getMixedFormatCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    testCreateTableWithNewCatalogLogProperties(false);
  }

  protected void testCreateTableWithNewCatalogLogProperties(boolean withKey) throws Exception {
    CatalogMeta testCatalogMeta =
        TEST_AMS.getAmsHandler().getCatalog(CatalogTestHelper.TEST_CATALOG_NAME);
    TEST_AMS
        .getAmsHandler()
        .updateMeta(
            testCatalogMeta,
            CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.LOG_STORE_ADDRESS,
            "1.1.1.1");
    TEST_AMS
        .getAmsHandler()
        .updateMeta(
            testCatalogMeta,
            CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.LOG_STORE_MESSAGE_TOPIC,
            "test-topic");
    TEST_AMS
        .getAmsHandler()
        .updateMeta(
            testCatalogMeta,
            CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.ENABLE_LOG_STORE,
            "true");
    refreshMixedFormatCatalog();
    MixedTable createTable = createTestTable(withKey);
    Assert.assertFalse(
        PropertyUtil.propertyAsBoolean(
            createTable.properties(),
            TableProperties.ENABLE_LOG_STORE,
            TableProperties.ENABLE_LOG_STORE_DEFAULT));
    Assert.assertFalse(createTable.properties().containsKey(TableProperties.LOG_STORE_ADDRESS));
    Assert.assertFalse(
        createTable.properties().containsKey(TableProperties.LOG_STORE_MESSAGE_TOPIC));
    getMixedFormatCatalog().dropTable(TableTestHelper.TEST_TABLE_ID, true);

    refreshMixedFormatCatalog();
    createTable =
        createTestTableBuilder(withKey)
            .withProperty(TableProperties.ENABLE_LOG_STORE, "true")
            .create();

    Assert.assertTrue(
        PropertyUtil.propertyAsBoolean(
            createTable.properties(),
            TableProperties.ENABLE_LOG_STORE,
            TableProperties.ENABLE_LOG_STORE_DEFAULT));
    Assert.assertTrue(createTable.properties().containsKey(TableProperties.LOG_STORE_ADDRESS));
    Assert.assertTrue(
        createTable.properties().containsKey(TableProperties.LOG_STORE_MESSAGE_TOPIC));
    getMixedFormatCatalog().dropTable(TableTestHelper.TEST_TABLE_ID, true);
  }

  @Test
  public void testGetTableBlockerManager() {
    getMixedFormatCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    KeyedTable createTable = createTestTable(true).asKeyedTable();
    TableBlockerManager tableBlockerManager =
        getMixedFormatCatalog().getTableBlockerManager(createTable.id());
    Assert.assertEquals(createTable.id(), tableBlockerManager.tableIdentifier());
    Assert.assertTrue(tableBlockerManager.getBlockers().isEmpty());
  }

  @After
  public void after() {
    getMixedFormatCatalog().dropTable(TableTestHelper.TEST_TABLE_ID, true);
    getMixedFormatCatalog().dropDatabase(TableTestHelper.TEST_DB_NAME);
  }

  protected Schema getCreateTableSchema() {
    return BasicTableTestHelper.TABLE_SCHEMA;
  }

  protected PartitionSpec getCreateTableSpec() {
    return BasicTableTestHelper.SPEC;
  }

  protected void assertIcebergTableStore(
      Table tableStore, boolean isBaseStore, boolean isKeyedTable) {
    Assert.assertEquals(
        2, ((HasTableOperations) tableStore).operations().current().formatVersion());
    Assert.assertNotNull(tableStore.properties().get(TableProperties.TABLE_CREATE_TIME));
    Assert.assertEquals(
        "true",
        tableStore
            .properties()
            .get(org.apache.iceberg.TableProperties.METADATA_DELETE_AFTER_COMMIT_ENABLED));
    Assert.assertEquals(
        String.valueOf(Integer.MAX_VALUE),
        tableStore.properties().get("flink.max-continuous-empty-commits"));

    String expectTableStore =
        isBaseStore
            ? TableProperties.MIXED_FORMAT_TABLE_STORE_BASE
            : TableProperties.MIXED_FORMAT_TABLE_STORE_CHANGE;
    Assert.assertEquals(
        expectTableStore, tableStore.properties().get(TableProperties.MIXED_FORMAT_TABLE_STORE));

    if (isKeyedTable) {
      Assert.assertNotNull(
          tableStore.properties().get(TableProperties.MIXED_FORMAT_PRIMARY_KEY_FIELDS));
      if (isBaseStore) {
        Assert.assertNotNull(
            tableStore.properties().get(TableProperties.MIXED_FORMAT_CHANGE_STORE_IDENTIFIER));
      }
    }
  }

  protected TableBuilder createTestTableBuilder(boolean withKey) {
    TableBuilder builder =
        getMixedFormatCatalog()
            .newTableBuilder(TableTestHelper.TEST_TABLE_ID, getCreateTableSchema())
            .withPartitionSpec(getCreateTableSpec());
    if (withKey) {
      builder.withPrimaryKeySpec(BasicTableTestHelper.PRIMARY_KEY_SPEC);
    }
    return builder;
  }

  protected MixedTable createTestTable(boolean withKey) {
    TableBuilder builder = createTestTableBuilder(withKey);
    return builder.create();
  }
}

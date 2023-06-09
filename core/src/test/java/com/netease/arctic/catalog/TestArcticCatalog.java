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

package com.netease.arctic.catalog;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestArcticCatalog extends CatalogTestBase {

  TableFormat format;

  public TestArcticCatalog(CatalogTestHelper catalogTestHelper, TableFormat format) {
    super(catalogTestHelper);
    this.format = format;
  }

  @Parameterized.Parameters(name = "catalogType = {0}, tableFormat={1}")
  public static Object[][] parameters() {
    return new Object[][] {
        new Object[]{BasicCatalogTestHelper.externalCatalog(), TableFormat.ICEBERG},
        new Object[]{BasicCatalogTestHelper.externalCatalog(), TableFormat.MIXED_ICEBERG},
        new Object[]{BasicCatalogTestHelper.internalCatalog(), TableFormat.MIXED_ICEBERG}
    };
  }


  @Test
  public void testIcebergTable() {
    Assume.assumeTrue(format.equals(TableFormat.ICEBERG));
    Catalog nativeIcebergCatalog = getIcebergCatalog();
    nativeIcebergCatalog.createTable(
        TableIdentifier.of(TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME),
        BasicTableTestHelper.TABLE_SCHEMA);

    ArcticTable table = getCatalog().loadTable(TableTestHelper.TEST_TABLE_ID);
    Assert.assertEquals(TableFormat.ICEBERG, table.format());
    Assert.assertTrue(table.isUnkeyedTable());
    Assert.assertEquals(BasicTableTestHelper.TABLE_SCHEMA.asStruct(), table.schema().asStruct());
  }

  @Test
  public void testCreateAndDropDatabase() {
    String createDbName = TableTestHelper.TEST_DB_NAME;
    Assert.assertFalse(getCatalog().listDatabases().contains(createDbName));
    getCatalog().createDatabase(createDbName);
    Assert.assertTrue(getCatalog().listDatabases().contains(createDbName));
    getCatalog().dropDatabase(createDbName);
    Assert.assertFalse(getCatalog().listDatabases().contains(createDbName));
  }

  @Test
  public void testCreateDuplicateDatabase() {
    String createDbName = TableTestHelper.TEST_DB_NAME;
    Assert.assertFalse(getCatalog().listDatabases().contains(createDbName));
    getCatalog().createDatabase(createDbName);
    Assert.assertTrue(getCatalog().listDatabases().contains(createDbName));
    Assert.assertThrows(
        AlreadyExistsException.class,
        () -> getCatalog().createDatabase(createDbName));
    getCatalog().dropDatabase(createDbName);
  }

  @Test
  public void testCreateTableWithCatalogTableProperties() throws TException {
    CatalogMeta testCatalogMeta = TEST_AMS.getAmsHandler().getCatalog(CatalogTestHelper.TEST_CATALOG_NAME);
    TEST_AMS.getAmsHandler().updateMeta(testCatalogMeta,
        CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.ENABLE_SELF_OPTIMIZING,
        "false");
    getCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    createTestTable();
    ArcticTable createTable = getCatalog().loadTable(TableTestHelper.TEST_TABLE_ID);
    Assert.assertEquals(format, createTable.format());
    Assert.assertFalse(PropertyUtil.propertyAsBoolean(createTable.properties(),
        TableProperties.ENABLE_SELF_OPTIMIZING, TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT));
  }

  @Test
  public void testLoadTableWithNewCatalogProperties() throws TException {
    getCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    createTestTable();
    ArcticTable createTable = getCatalog().loadTable(TableTestHelper.TEST_TABLE_ID);

    Assert.assertTrue(PropertyUtil.propertyAsBoolean(createTable.properties(),
        TableProperties.ENABLE_SELF_OPTIMIZING, TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT));

    CatalogMeta testCatalogMeta = TEST_AMS.getAmsHandler().getCatalog(CatalogTestHelper.TEST_CATALOG_NAME);
    TEST_AMS.getAmsHandler().updateMeta(testCatalogMeta,
        CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.ENABLE_SELF_OPTIMIZING,
        "false");
    getCatalog().refresh();
    ArcticTable loadTable = getCatalog().loadTable(createTable.id());
    Assert.assertEquals(format, createTable.format());
    Assert.assertFalse(PropertyUtil.propertyAsBoolean(loadTable.properties(),
        TableProperties.ENABLE_SELF_OPTIMIZING, TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT));
  }

  @After
  public void after() {
    getCatalog().dropTable(TableTestHelper.TEST_TABLE_ID, true);
    if (getCatalog().listDatabases().contains(TableTestHelper.TEST_DB_NAME)) {
      getCatalog().dropDatabase(TableTestHelper.TEST_DB_NAME);
    }
  }

  protected void createTestTable() {
    getCatalog()
        .newTableBuilder(TableTestHelper.TEST_TABLE_ID, BasicTableTestHelper.TABLE_SCHEMA, format)
        .create();
  }
}

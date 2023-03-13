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

import com.netease.arctic.TableTestHelpers;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

public class IcebergCatalogTest extends CatalogTestBase {

  public IcebergCatalogTest() {
    super(TableFormat.ICEBERG);
  }

  @Test
  public void testLoadIcebergTable() {
    getCatalog().createDatabase(TableTestHelpers.TEST_DB_NAME);
    Catalog nativeIcebergCatalog = getIcebergCatalog();
    nativeIcebergCatalog.createTable(
        TableIdentifier.of(TableTestHelpers.TEST_DB_NAME, TableTestHelpers.TEST_TABLE_NAME),
        TableTestHelpers.TABLE_SCHEMA);
    ArcticTable table = getCatalog().loadTable(TableTestHelpers.TEST_TABLE_ID);
    Assert.assertTrue(table instanceof BasicIcebergCatalog.BasicIcebergTable);
    Assert.assertEquals(true, table.isUnkeyedTable());
    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), table.schema().asStruct());

    getCatalog().dropTable(TableTestHelpers.TEST_TABLE_ID, true);
    getCatalog().dropDatabase(TableTestHelpers.TEST_DB_NAME);
  }

  @Test
  public void testLoadTableWithNewCatalogProperties() throws TException {
    getCatalog().createDatabase(TableTestHelpers.TEST_DB_NAME);
    Catalog nativeIcebergCatalog = getIcebergCatalog();
    nativeIcebergCatalog.createTable(
        TableIdentifier.of(TableTestHelpers.TEST_DB_NAME, TableTestHelpers.TEST_TABLE_NAME),
        TableTestHelpers.TABLE_SCHEMA);
    ArcticTable table = getCatalog().loadTable(TableTestHelpers.TEST_TABLE_ID);

    Assert.assertEquals(true, PropertyUtil.propertyAsBoolean(table.properties(),
        TableProperties.ENABLE_SELF_OPTIMIZING, TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT));
    Assert.assertEquals(false, PropertyUtil.propertyAsBoolean(table.properties(),
        TableProperties.ENABLE_TABLE_TRASH, TableProperties.ENABLE_TABLE_TRASH_DEFAULT));

    CatalogMeta testCatalogMeta = TEST_AMS.getAmsHandler().getCatalog(TEST_CATALOG_NAME);
    TEST_AMS.getAmsHandler().updateMeta(testCatalogMeta,
        CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.ENABLE_SELF_OPTIMIZING,
        "false");
    TEST_AMS.getAmsHandler().updateMeta(testCatalogMeta,
        CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.ENABLE_TABLE_TRASH,
        "true");
    getCatalog().refresh();
    ArcticTable loadTable = getCatalog().loadTable(table.id());
    Assert.assertEquals(false, PropertyUtil.propertyAsBoolean(loadTable.properties(),
        TableProperties.ENABLE_SELF_OPTIMIZING, TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT));
    Assert.assertEquals(true, PropertyUtil.propertyAsBoolean(loadTable.properties(),
        TableProperties.ENABLE_TABLE_TRASH, TableProperties.ENABLE_TABLE_TRASH_DEFAULT));

    getCatalog().dropTable(TableTestHelpers.TEST_TABLE_ID, true);
    getCatalog().dropDatabase(TableTestHelpers.TEST_DB_NAME);
  }
}

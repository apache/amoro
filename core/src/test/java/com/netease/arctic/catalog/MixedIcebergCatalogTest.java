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
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

public class MixedIcebergCatalogTest extends CatalogTestBase {

  public MixedIcebergCatalogTest() {
    super(TableFormat.MIXED_ICEBERG);
  }

  @Test
  public void testCreateUnkeyedTable() {
    getCatalog().createDatabase(TableTestHelpers.TEST_DB_NAME);
    UnkeyedTable createTable = getCatalog()
        .newTableBuilder(TableTestHelpers.TEST_TABLE_ID, TableTestHelpers.TABLE_SCHEMA)
        .withPartitionSpec(TableTestHelpers.SPEC)
        .create()
        .asUnkeyedTable();

    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), createTable.schema().asStruct());
    Assert.assertEquals(TableTestHelpers.SPEC, createTable.spec());
    Assert.assertEquals(TableTestHelpers.TEST_TABLE_ID, createTable.id());

    UnkeyedTable loadTable = getCatalog().loadTable(TableTestHelpers.TEST_TABLE_ID).asUnkeyedTable();
    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), loadTable.schema().asStruct());
    Assert.assertEquals(TableTestHelpers.SPEC, loadTable.spec());
    Assert.assertEquals(TableTestHelpers.TEST_TABLE_ID, loadTable.id());

    getCatalog().dropTable(TableTestHelpers.TEST_TABLE_ID, true);
    getCatalog().dropDatabase(TableTestHelpers.TEST_DB_NAME);
  }

  @Test
  public void testLoadKeyedTable() {
    getCatalog().createDatabase(TableTestHelpers.TEST_DB_NAME);
    KeyedTable createTable = getCatalog()
        .newTableBuilder(TableTestHelpers.TEST_TABLE_ID, TableTestHelpers.TABLE_SCHEMA)
        .withPartitionSpec(TableTestHelpers.SPEC)
        .withPrimaryKeySpec(TableTestHelpers.PRIMARY_KEY_SPEC)
        .create()
        .asKeyedTable();

    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), createTable.schema().asStruct());
    Assert.assertEquals(TableTestHelpers.SPEC, createTable.spec());
    Assert.assertEquals(TableTestHelpers.TEST_TABLE_ID, createTable.id());
    Assert.assertEquals(TableTestHelpers.PRIMARY_KEY_SPEC, createTable.primaryKeySpec());

    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), createTable.baseTable().schema().asStruct());
    Assert.assertEquals(TableTestHelpers.SPEC, createTable.baseTable().spec());

    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), createTable.changeTable().schema().asStruct());
    Assert.assertEquals(TableTestHelpers.SPEC, createTable.changeTable().spec());

    KeyedTable loadTable = getCatalog().loadTable(TableTestHelpers.TEST_TABLE_ID).asKeyedTable();

    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), loadTable.schema().asStruct());
    Assert.assertEquals(TableTestHelpers.SPEC, loadTable.spec());
    Assert.assertEquals(TableTestHelpers.TEST_TABLE_ID, loadTable.id());
    Assert.assertEquals(TableTestHelpers.PRIMARY_KEY_SPEC, loadTable.primaryKeySpec());

    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), loadTable.baseTable().schema().asStruct());
    Assert.assertEquals(TableTestHelpers.SPEC, loadTable.baseTable().spec());

    Assert.assertEquals(TableTestHelpers.TABLE_SCHEMA.asStruct(), loadTable.changeTable().schema().asStruct());
    Assert.assertEquals(TableTestHelpers.SPEC, loadTable.changeTable().spec());

    getCatalog().dropTable(TableTestHelpers.TEST_TABLE_ID, true);
    getCatalog().dropDatabase(TableTestHelpers.TEST_DB_NAME);
  }

  @Test
  public void refreshCatalog() throws TException {
    // CatalogMeta catalog = AMS.handler().getCatalog(TEST_CATALOG_NAME);
    // AMS.handler().updateMeta(catalog, CatalogMetaProperties.KEY_WAREHOUSE, "/test");
    // testCatalog = CatalogLoader.load(AMS.getUrl());
    // testCatalog.refresh();
    // Assert.assertEquals("/test",
    //     AMS.handler().getCatalog(TEST_CATALOG_NAME).
    //         getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE));
  }

}

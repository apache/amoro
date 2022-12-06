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

import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_CATALOG_NAME;
import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_DB_NAME;

public class BaseCatalogTest extends TableTestBase {

  @Test
  public void testCreateAndDropDatabase() {
    Assert.assertEquals(Lists.newArrayList(TEST_DB_NAME), testCatalog.listDatabases());
    testCatalog.createDatabase("create_db");
    Assert.assertEquals(Lists.newArrayList(TEST_DB_NAME, "create_db"), testCatalog.listDatabases());
    testCatalog.dropDatabase("create_db");
    Assert.assertEquals(Lists.newArrayList(TEST_DB_NAME), testCatalog.listDatabases());
  }

  @Test
  public void testLoadUnkeyedTable() {
    UnkeyedTable loadTable = testCatalog.loadTable(TABLE_ID).asUnkeyedTable();
    Assert.assertEquals(TABLE_SCHEMA.asStruct(), loadTable.schema().asStruct());
    Assert.assertEquals(SPEC, loadTable.spec());
    Assert.assertEquals(TABLE_ID, loadTable.id());
  }

  @Test
  public void testLoadKeyedTable() {
    KeyedTable loadTable = testCatalog.loadTable(PK_TABLE_ID).asKeyedTable();
    Assert.assertEquals(TABLE_SCHEMA.asStruct(), loadTable.schema().asStruct());
    Assert.assertEquals(SPEC, loadTable.spec());
    Assert.assertEquals(PK_TABLE_ID, loadTable.id());
    Assert.assertEquals(PRIMARY_KEY_SPEC, loadTable.primaryKeySpec());

    Assert.assertEquals(TABLE_SCHEMA.asStruct(), loadTable.baseTable().schema().asStruct());
    Assert.assertEquals(SPEC, loadTable.baseTable().spec());

    Assert.assertEquals(TABLE_SCHEMA.asStruct(), loadTable.changeTable().schema().asStruct());
    Assert.assertEquals(SPEC, loadTable.changeTable().spec());
  }

  @Test
  public void refreshCatalog() throws TException {
    CatalogMeta catalog = AMS.handler().getCatalog(TEST_CATALOG_NAME);
    AMS.handler().updateMeta(catalog, CatalogMetaProperties.KEY_WAREHOUSE, "/test");
    testCatalog = CatalogLoader.load(AMS.getUrl());
    testCatalog.refresh();
    Assert.assertEquals("/test",
        AMS.handler().getCatalog(TEST_CATALOG_NAME).
            getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE));
  }

}

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

package com.netease.arctic.hive.catalog;

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

public class TestArcticHiveCatalog extends HiveTableTestBase {

  @Test
  public void testCreateAndDropDatabase() {
    Assert.assertEquals(Sets.newHashSet("default", HIVE_DB_NAME), Sets.newHashSet(hiveCatalog.listDatabases()));
    hiveCatalog.createDatabase("create_db");
    Assert.assertEquals(Sets.newHashSet("default", HIVE_DB_NAME, "create_db"),
        Sets.newHashSet(hiveCatalog.listDatabases()));
    hiveCatalog.dropDatabase("create_db");
    Assert.assertEquals(Lists.newArrayList("default", HIVE_DB_NAME), hiveCatalog.listDatabases());
    Assert.assertEquals(Sets.newHashSet("default", HIVE_DB_NAME), Sets.newHashSet(hiveCatalog.listDatabases()));
  }

  @Test
  public void testLoadUnkeyedTable() {
    UnkeyedTable loadTable = hiveCatalog.loadTable(HIVE_TABLE_ID).asUnkeyedTable();
    Assert.assertEquals(HIVE_TABLE_SCHEMA.asStruct(), loadTable.schema().asStruct());
    Assert.assertEquals(HIVE_SPEC, loadTable.spec());
    Assert.assertEquals(HIVE_TABLE_ID, loadTable.id());
  }

  @Test
  public void testLoadKeyedTable() {
    KeyedTable loadTable = hiveCatalog.loadTable(HIVE_PK_TABLE_ID).asKeyedTable();
    Assert.assertEquals(HIVE_TABLE_SCHEMA.asStruct(), loadTable.schema().asStruct());
    Assert.assertEquals(HIVE_SPEC, loadTable.spec());
    Assert.assertEquals(HIVE_PK_TABLE_ID, loadTable.id());
    Assert.assertEquals(PRIMARY_KEY_SPEC, loadTable.primaryKeySpec());

    Assert.assertEquals(HIVE_TABLE_SCHEMA.asStruct(), loadTable.baseTable().schema().asStruct());
    Assert.assertEquals(HIVE_SPEC, loadTable.baseTable().spec());

    Assert.assertEquals(HIVE_TABLE_SCHEMA.asStruct(), loadTable.changeTable().schema().asStruct());
    Assert.assertEquals(HIVE_SPEC, loadTable.changeTable().spec());
  }
}

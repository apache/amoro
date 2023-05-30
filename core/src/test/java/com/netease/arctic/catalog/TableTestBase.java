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

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.ArcticTableUtil;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.junit.After;
import org.junit.Before;

public abstract class TableTestBase extends CatalogTestBase {

  private final TableTestHelper tableTestHelper;
  private ArcticTable arcticTable;

  public TableTestBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper);
    this.tableTestHelper = tableTestHelper;
    if (isKeyedTable()) {
      Preconditions.checkArgument(TableFormat.MIXED_HIVE.equals(catalogTestHelper.tableFormat()) ||
          TableFormat.MIXED_ICEBERG.equals(catalogTestHelper.tableFormat()),
          "Only mixed format table support primary key spec");
    }
  }

  @Before
  public void setupTable() {
    switch (getTestFormat()) {
      case MIXED_HIVE:
      case MIXED_ICEBERG:
        createMixedFormatTable();
        break;
      case ICEBERG:
        createIcebergFormatTable();
        break;
    }
  }

  private void createMixedFormatTable() {
    getCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    TableBuilder tableBuilder = getCatalog().newTableBuilder(
        TableTestHelper.TEST_TABLE_ID,
        tableTestHelper.tableSchema());
    tableBuilder.withProperties(tableTestHelper.tableProperties());
    if (isKeyedTable()) {
      tableBuilder.withPrimaryKeySpec(tableTestHelper.primaryKeySpec());
    }
    if (isPartitionedTable()) {
      tableBuilder.withPartitionSpec(tableTestHelper.partitionSpec());
    }
    arcticTable = tableBuilder.create();
  }

  private void createIcebergFormatTable() {
    getIcebergCatalog().createTable(
        org.apache.iceberg.catalog.TableIdentifier.of(TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME),
        tableTestHelper.tableSchema(),
        tableTestHelper.partitionSpec(),
        tableTestHelper.tableProperties());
    arcticTable = getCatalog().loadTable(TableTestHelper.TEST_TABLE_ID);
  }

  @After
  public void dropTable() {
    getCatalog().dropTable(TableTestHelper.TEST_TABLE_ID, true);
    getCatalog().dropDatabase(TableTestHelper.TEST_DB_NAME);
  }

  protected ArcticTable getArcticTable() {
    return arcticTable;
  }

  protected UnkeyedTable getBaseStore() {
    return ArcticTableUtil.baseStore(arcticTable);
  }

  protected boolean isKeyedTable() {
    return tableTestHelper.primaryKeySpec() != null && tableTestHelper.primaryKeySpec().primaryKeyExisted();
  }

  protected boolean isPartitionedTable() {
    return tableTestHelper.partitionSpec() != null && tableTestHelper.partitionSpec().isPartitioned();
  }

  protected TableTestHelper tableTestHelper() {
    return tableTestHelper;
  }
}

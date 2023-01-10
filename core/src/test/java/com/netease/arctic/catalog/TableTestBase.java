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
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.trace.TableTracerTest;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.junit.After;
import org.junit.Before;

public abstract class TableTestBase extends CatalogTestBase {

  private final boolean keyedTable;

  private final boolean partitionedTable;

  private ArcticTable arcticTable;

  public TableTestBase(TableFormat testFormat, boolean keyedTable, boolean partitionedTable) {
    super(testFormat);
    this.keyedTable = keyedTable;
    this.partitionedTable = partitionedTable;
    if (keyedTable) {
      Preconditions.checkArgument(TableFormat.MIXED_HIVE.equals(testFormat) ||
          TableFormat.MIXED_ICEBERG.equals(testFormat), "Only mixed format table support primary key spec");
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
    getCatalog().createDatabase(TableTestHelpers.TEST_DB_NAME);
    TableBuilder tableBuilder = getCatalog().newTableBuilder(TableTestHelpers.TEST_TABLE_ID,
        TableTestHelpers.TABLE_SCHEMA);
    if (isKeyedTable()) {
      tableBuilder.withPrimaryKeySpec(TableTestHelpers.PRIMARY_KEY_SPEC);
    }
    if (isPartitionedTable()) {
      tableBuilder.withPartitionSpec(TableTestHelpers.SPEC);
    }
    arcticTable = tableBuilder.create();
  }

  private void createIcebergFormatTable() {
    PartitionSpec partitionSpec = PartitionSpec.unpartitioned();
    if (isPartitionedTable()) {
      partitionSpec = TableTestHelpers.SPEC;
    }
    getIcebergCatalog().createTable(
        TableTestHelpers.TEST_TABLE_ICEBERG_ID,
        TableTestHelpers.TABLE_SCHEMA,
        partitionSpec);
    arcticTable = getCatalog().loadTable(TableTestHelpers.TEST_TABLE_ID);
  }

  @After
  public void dropTable() {
    getCatalog().dropTable(TableTestHelpers.TEST_TABLE_ID, true);
    getCatalog().dropDatabase(TableTestHelpers.TEST_DB_NAME);
  }

  protected ArcticTable getArcticTable() {
    return arcticTable;
  }

  protected boolean isKeyedTable() {
    return keyedTable;
  }

  protected boolean isPartitionedTable() {
    return partitionedTable;
  }

}

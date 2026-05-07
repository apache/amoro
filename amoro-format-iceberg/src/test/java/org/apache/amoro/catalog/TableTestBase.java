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

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.amoro.utils.MixedTableUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;

/**
 * Dual-mode base class supporting both JUnit 4 and JUnit 5 subclasses. See {@link CatalogTestBase}
 * for the full dual-mode rationale; the same constraints apply here. Cross-module
 * {@code @RunWith(Parameterized.class)} subclasses keep the legacy two-arg constructor and rely on
 * the {@code @Before} / {@code @After} hooks. In-module JUnit 5 subclasses use the no-arg
 * constructor and explicitly call {@link #setupTable(CatalogTestHelper, TableTestHelper)} from
 * their {@code @ParameterizedTest} method body or {@code @BeforeEach}.
 */
public abstract class TableTestBase extends CatalogTestBase {

  protected TableTestHelper tableTestHelper;
  private MixedTable mixedTable;
  private TableMetaStore tableMetaStore;

  /** JUnit 4 constructor — kept for cross-module {@code @RunWith(Parameterized.class)} callers. */
  public TableTestBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper);
    this.tableTestHelper = tableTestHelper;
  }

  /** JUnit 5 constructor — used by in-module migrated subclasses. */
  public TableTestBase() {
    super();
  }

  /** JUnit 4 lifecycle — fires when subclass uses {@code @RunWith(Parameterized)}. */
  @Before
  public void setupTable() {
    if (tableTestHelper != null) {
      doSetupTable();
    }
  }

  /**
   * JUnit 5 entry point — invoke from {@code @ParameterizedTest} method body or
   * {@code @BeforeEach}.
   */
  protected void setupTable(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper)
      throws IOException {
    setupCatalog(catalogTestHelper);
    this.tableTestHelper = tableTestHelper;
    doSetupTable();
  }

  private void doSetupTable() {
    this.tableMetaStore = CatalogUtil.buildMetaStore(getCatalogMeta());

    getUnifiedCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
    TableFormat format = getTestFormat();
    if (format.in(TableFormat.MIXED_HIVE, TableFormat.MIXED_ICEBERG)) {
      createMixedFormatTable();
    } else if (TableFormat.ICEBERG.equals(format)) {
      createIcebergFormatTable();
    }
  }

  private void createMixedFormatTable() {
    TableBuilder tableBuilder =
        getMixedFormatCatalog()
            .newTableBuilder(TableTestHelper.TEST_TABLE_ID, tableTestHelper.tableSchema());
    tableBuilder.withProperties(tableTestHelper.tableProperties());
    if (isKeyedTable()) {
      tableBuilder.withPrimaryKeySpec(tableTestHelper.primaryKeySpec());
    }
    if (isPartitionedTable()) {
      tableBuilder.withPartitionSpec(tableTestHelper.partitionSpec());
    }
    mixedTable = tableBuilder.create();
  }

  private void createIcebergFormatTable() {
    getIcebergCatalog()
        .createTable(
            org.apache.iceberg.catalog.TableIdentifier.of(
                TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME),
            tableTestHelper.tableSchema(),
            tableTestHelper.partitionSpec(),
            tableTestHelper.tableProperties());
    mixedTable =
        (MixedTable)
            getUnifiedCatalog()
                .loadTable(TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME)
                .originalTable();
  }

  @After
  @AfterEach
  public void dropTable() {
    if (tableTestHelper == null) {
      return;
    }
    getUnifiedCatalog()
        .dropTable(tableTestHelper.id().getDatabase(), tableTestHelper.id().getTableName(), true);
    try {
      getUnifiedCatalog().dropDatabase(TableTestHelper.TEST_DB_NAME);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected MixedTable getMixedTable() {
    return mixedTable;
  }

  protected UnkeyedTable getBaseStore() {
    return MixedTableUtil.baseStore(mixedTable);
  }

  protected TableMetaStore getTableMetaStore() {
    return this.tableMetaStore;
  }

  protected boolean isKeyedTable() {
    return tableTestHelper.primaryKeySpec() != null
        && tableTestHelper.primaryKeySpec().primaryKeyExisted();
  }

  protected boolean isPartitionedTable() {
    return tableTestHelper.partitionSpec() != null
        && tableTestHelper.partitionSpec().isPartitioned();
  }

  protected TableTestHelper tableTestHelper() {
    return tableTestHelper;
  }
}

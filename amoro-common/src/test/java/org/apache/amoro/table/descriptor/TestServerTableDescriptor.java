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

package org.apache.amoro.table.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.AmoroCatalog;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.hive.TestHMS;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

public abstract class TestServerTableDescriptor {

  protected static final String TEST_DB = "test_db";

  protected static final String TEST_TABLE = "test_table";

  @TempDir Path tempDir;

  protected static TestHMS TEST_HMS;

  private final AmoroCatalogTestHelper<?> amoroCatalogTestHelper;
  private AmoroCatalog amoroCatalog;
  private Object originalCatalog;

  public TestServerTableDescriptor(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    this.amoroCatalogTestHelper = amoroCatalogTestHelper;
  }

  @BeforeAll
  static void setupTestHMS() throws Exception {
    TEST_HMS = new TestHMS();
    TEST_HMS.before();
  }

  @AfterAll
  static void teardownTestHMS() {
    if (TEST_HMS != null) {
      TEST_HMS.after();
    }
  }

  @BeforeEach
  void before() throws IOException {
    String path = tempDir.toFile().getAbsolutePath();
    amoroCatalogTestHelper.initWarehouse(path);
    amoroCatalogTestHelper.initHiveConf(TEST_HMS.getHiveConf());
    this.amoroCatalog = amoroCatalogTestHelper.amoroCatalog();
    this.originalCatalog = amoroCatalogTestHelper.originalCatalog();

    getAmoroCatalog().createDatabase(TEST_DB);
    try {
      this.amoroCatalogTestHelper.createTable(TEST_DB, TEST_TABLE);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  void after() throws IOException {
    try {
      this.amoroCatalogTestHelper.amoroCatalog().dropTable(TEST_DB, TEST_TABLE, true);
      this.amoroCatalogTestHelper.amoroCatalog().dropDatabase(TEST_DB);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void tableOperations() throws Exception {
    FormatTableDescriptor tableDescriptor = getTableDescriptor();
    tableDescriptor.withIoExecutor(Executors.newSingleThreadExecutor());

    // add properties
    amoroCatalogTestHelper.setTableProperties(TEST_DB, TEST_TABLE, "k1", "v1");

    // remove properties
    amoroCatalogTestHelper.removeTableProperties(TEST_DB, TEST_TABLE, "k1");

    // add columns
    tableOperationsAddColumns();

    // rename columns
    tableOperationsRenameColumns();

    // change columns type
    tableOperationsChangeColumnType();

    // change columns comment
    tableOperationsChangeColumnComment();

    // change columns nullable
    tableOperationsChangeColumnRequired();

    // change columns default value
    tableOperationsDropColumn();

    AmoroTable<?> table = amoroCatalog.loadTable(TEST_DB, TEST_TABLE);

    List<DDLInfo> tableOperations = tableDescriptor.getTableOperations(table);

    assertEquals(
        "ALTER TABLE test_table SET TBLPROPERTIES ('k1' = 'v1')", tableOperations.get(0).getDdl());

    assertEquals(
        "ALTER TABLE test_table UNSET TBLPROPERTIES ('k1')", tableOperations.get(1).getDdl());

    assertTrue(
        tableOperations
            .get(2)
            .getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table ADD COLUMNS (new_col int)"));

    assertTrue(
        tableOperations
            .get(3)
            .getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table RENAME COLUMN new_col TO renamed_col"));

    assertTrue(
        tableOperations
            .get(4)
            .getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table ALTER COLUMN renamed_col TYPE BIGINT"));

    assertTrue(
        tableOperations
            .get(5)
            .getDdl()
            .equalsIgnoreCase(
                "ALTER TABLE test_table ALTER COLUMN renamed_col COMMENT 'new comment'"));

    assertTrue(
        tableOperations
            .get(6)
            .getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table ALTER COLUMN renamed_col DROP NOT NULL"));

    assertTrue(
        tableOperations
            .get(7)
            .getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table DROP COLUMN renamed_col"));
  }

  protected abstract void tableOperationsAddColumns();

  protected abstract void tableOperationsRenameColumns();

  protected abstract void tableOperationsChangeColumnType();

  protected abstract void tableOperationsChangeColumnComment();

  protected abstract void tableOperationsChangeColumnRequired();

  protected abstract void tableOperationsDropColumn();

  protected abstract FormatTableDescriptor getTableDescriptor();

  protected AmoroCatalog getAmoroCatalog() {
    return amoroCatalog;
  }

  protected Object getOriginalCatalog() {
    return originalCatalog;
  }
}

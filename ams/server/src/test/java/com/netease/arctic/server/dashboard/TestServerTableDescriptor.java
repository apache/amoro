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

package com.netease.arctic.server.dashboard;

import com.netease.arctic.formats.AmoroCatalogTestHelper;
import com.netease.arctic.server.catalog.TableCatalogTestBase;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.utils.Configurations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public abstract class TestServerTableDescriptor extends TableCatalogTestBase {

  protected static final String TEST_DB = "test_db";

  protected static final String TEST_TABLE = "test_table";

  public TestServerTableDescriptor(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Before
  public void before() {
    getAmoroCatalog().createDatabase(TEST_DB);
    try {
      getAmoroCatalogTestHelper().createTable(TEST_DB, TEST_TABLE);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void tableOperations() throws Exception {
    ServerTableDescriptor serverTableDescriptor = new ServerTableDescriptor(tableService(), new Configurations());

    //add properties
    getAmoroCatalogTestHelper().setTableProperties(TEST_DB, TEST_TABLE, "k1", "v1");

    //remove properties
    getAmoroCatalogTestHelper().removeTableProperties(TEST_DB, TEST_TABLE, "k1");

    //add columns
    tableOperationsAddColumns();

    //rename columns
    tableOperationsRenameColumns();

    //change columns type
    tableOperationsChangeColumnType();

    //change columns comment
    tableOperationsChangeColumnComment();

    //change columns nullable
    tableOperationsChangeColumnRequired();

    //change columns default value
    tableOperationsDropColumn();

    List<DDLInfo> tableOperations = serverTableDescriptor.getTableOperations(
        ServerTableIdentifier.of(getAmoroCatalogTestHelper().catalogName(), TEST_DB, TEST_TABLE));

    Assert.assertEquals(
        tableOperations.get(0).getDdl(),
        "ALTER TABLE test_table SET TBLPROPERTIES ('k1' = 'v1')");

    Assert.assertEquals(
        tableOperations.get(1).getDdl(),
        "ALTER TABLE test_table UNSET TBLPROPERTIES ('k1')");

    Assert.assertTrue(
        tableOperations.get(2).getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table ADD COLUMNS (new_col int)"));

    Assert.assertTrue(
        tableOperations.get(3).getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table RENAME COLUMN new_col TO renamed_col"));

    Assert.assertTrue(
        tableOperations.get(4).getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table ALTER COLUMN renamed_col TYPE BIGINT"));

    Assert.assertTrue(
        tableOperations.get(5).getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table ALTER COLUMN renamed_col COMMENT 'new comment'"));

    Assert.assertTrue(
        tableOperations.get(6).getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table ALTER COLUMN renamed_col DROP NOT NULL"));

    Assert.assertTrue(
        tableOperations.get(7).getDdl()
            .equalsIgnoreCase("ALTER TABLE test_table DROP COLUMN renamed_col")
    );
  }

  protected abstract void tableOperationsAddColumns();

  protected abstract void tableOperationsRenameColumns();

  protected abstract void tableOperationsChangeColumnType();

  protected abstract void tableOperationsChangeColumnComment();

  protected abstract void tableOperationsChangeColumnRequired();

  protected abstract void tableOperationsDropColumn();
}
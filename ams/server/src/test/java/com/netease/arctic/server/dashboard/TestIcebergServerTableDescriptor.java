/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.dashboard;

import com.netease.arctic.formats.AmoroCatalogTestHelper;
import com.netease.arctic.formats.IcebergHadoopCatalogTestHelper;
import com.netease.arctic.hive.formats.IcebergHiveCatalogTestHelper;
import org.apache.iceberg.Table;
import org.apache.iceberg.types.Types;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestIcebergServerTableDescriptor extends TestServerTableDescriptor {

  public TestIcebergServerTableDescriptor(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {
        IcebergHadoopCatalogTestHelper.defaultHelper(),
        IcebergHiveCatalogTestHelper.defaultHelper()
    };
  }

  @Override
  protected void tableOperationsAddColumns() {
    getTable().updateSchema()
        .allowIncompatibleChanges()
        .addColumn("new_col", Types.IntegerType.get())
        .commit();
  }

  @Override
  protected void tableOperationsRenameColumns() {
    getTable().updateSchema()
        .renameColumn("new_col", "renamed_col")
        .commit();
  }

  @Override
  protected void tableOperationsChangeColumnType() {
    getTable().updateSchema()
        .updateColumn("renamed_col", Types.LongType.get())
        .commit();
  }

  @Override
  protected void tableOperationsChangeColumnComment() {
    getTable().updateSchema()
        .updateColumn("renamed_col", Types.LongType.get(), "new comment")
        .commit();
  }

  @Override
  protected void tableOperationsChangeColumnRequired() {
    getTable().updateSchema()
        .allowIncompatibleChanges()
        .requireColumn("renamed_col")
        .commit();
  }

  @Override
  protected void tableOperationsDropColumn() {
    getTable().updateSchema()
        .deleteColumn("renamed_col")
        .commit();
  }

  private Table getTable() {
    return (Table) getAmoroCatalog().loadTable(TEST_DB, TEST_TABLE).originalTable();
  }
}

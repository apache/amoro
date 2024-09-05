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

package org.apache.amoro.formats.paimon;

import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.TestServerTableDescriptor;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.types.DataTypes;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestPaimonServerTableDescriptor extends TestServerTableDescriptor {

  public TestPaimonServerTableDescriptor(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {
      PaimonHadoopCatalogTestHelper.defaultHelper(), PaimonHiveCatalogTestHelper.defaultHelper()
    };
  }

  @Override
  protected void tableOperationsAddColumns() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.addColumn("new_col", DataTypes.INT()),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsRenameColumns() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.renameColumn("new_col", "renamed_col"),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsChangeColumnType() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.updateColumnType("renamed_col", DataTypes.BIGINT()),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsChangeColumnComment() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.updateColumnComment("renamed_col", "new comment"),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsChangeColumnRequired() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.updateColumnNullability("renamed_col", false),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void tableOperationsDropColumn() {
    try {
      getCatalog()
          .alterTable(
              Identifier.create(
                  TestServerTableDescriptor.TEST_DB, TestServerTableDescriptor.TEST_TABLE),
              SchemaChange.dropColumn("renamed_col"),
              false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected FormatTableDescriptor getTableDescriptor() {
    return new PaimonTableDescriptor();
  }

  private Catalog getCatalog() {
    return (Catalog) getOriginalCatalog();
  }
}

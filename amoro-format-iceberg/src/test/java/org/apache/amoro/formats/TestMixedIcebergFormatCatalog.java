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

package org.apache.amoro.formats;

import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.table.TableIdentifier;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestMixedIcebergFormatCatalog extends TestAmoroCatalogBase {

  public TestMixedIcebergFormatCatalog(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {MixedIcebergHadoopCatalogTestHelper.defaultHelper()};
  }

  @Override
  protected void createDatabase(String dbName) {
    catalog().createDatabase(dbName);
  }

  @Override
  protected void createTable(String dbName, String tableName, Map<String, String> properties) {
    Schema schema =
        new Schema(
            Types.NestedField.of(1, false, "id", Types.IntegerType.get()),
            Types.NestedField.of(2, false, "data", Types.StringType.get()));
    catalog()
        .newTableBuilder(TableIdentifier.of(catalog().name(), dbName, tableName), schema)
        .withProperties(properties)
        .create();
  }

  @Override
  protected List<String> listDatabases() {
    return catalog().listDatabases();
  }

  private MixedFormatCatalog catalog() {
    return (MixedFormatCatalog) originalCatalog;
  }
}

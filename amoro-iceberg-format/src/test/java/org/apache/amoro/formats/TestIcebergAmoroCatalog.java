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

import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Types;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestIcebergAmoroCatalog extends TestAmoroCatalogBase {

  public TestIcebergAmoroCatalog(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {IcebergHadoopCatalogTestHelper.defaultHelper()};
  }

  @Override
  protected void createDatabase(String dbName) {
    if (catalog() instanceof SupportsNamespaces) {
      Namespace ns = Namespace.of(dbName);
      ((SupportsNamespaces) catalog()).createNamespace(ns);
    }
  }

  @Override
  protected void createTable(String dbName, String tableName, Map<String, String> properties) {
    TableIdentifier identifier = TableIdentifier.of(dbName, tableName);

    Schema schema =
        new Schema(
            Types.NestedField.of(1, false, "id", Types.IntegerType.get()),
            Types.NestedField.of(2, false, "data", Types.StringType.get()));

    catalog()
        .newCreateTableTransaction(identifier, schema, PartitionSpec.unpartitioned(), properties)
        .commitTransaction();
  }

  @Override
  protected List<String> listDatabases() {
    if (catalog() instanceof SupportsNamespaces) {
      return ((SupportsNamespaces) catalog())
          .listNamespaces().stream().map(ns -> ns.level(0)).collect(Collectors.toList());
    }
    return Lists.newArrayList();
  }

  private Catalog catalog() {
    return (Catalog) originalCatalog;
  }
}

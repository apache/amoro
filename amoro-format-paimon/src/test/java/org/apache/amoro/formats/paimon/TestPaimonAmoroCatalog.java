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
import org.apache.amoro.formats.TestAmoroCatalogBase;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.types.DataTypes;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestPaimonAmoroCatalog extends TestAmoroCatalogBase {

  public TestPaimonAmoroCatalog(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {PaimonHadoopCatalogTestHelper.defaultHelper()};
  }

  @Override
  protected void createDatabase(String dbName) {
    try (Catalog catalog = catalog()) {
      catalog.createDatabase(dbName, false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void createTable(String dbName, String tableName, Map<String, String> properties) {
    Schema schema =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .primaryKey("id", "name")
            .options(properties)
            .build();
    try (Catalog catalog = catalog()) {
      catalog.createTable(Identifier.create(dbName, tableName), schema, false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected List<String> listDatabases() {
    try (Catalog catalog = catalog()) {
      return catalog.listDatabases();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Catalog catalog() {
    return (Catalog) originalCatalog;
  }
}

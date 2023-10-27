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

package com.netease.arctic.server;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class TestInternalMixedCatalogService extends InternalCatalogServiceTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(TestInternalMixedCatalogService.class);

  private ArcticCatalog catalog;
  private String icebergTable = "iceberg_table";

  private TableIdentifier tableIdentifier = TableIdentifier.of(
      AmsEnvironment.INTERNAL_ICEBERG_CATALOG, database, table);

  private org.apache.iceberg.catalog.TableIdentifier icebergTableIdentifier =
      org.apache.iceberg.catalog.TableIdentifier.of(database, icebergTable);

  @BeforeEach
  public void before() {
    catalog = loadMixedIcebergCatalog();
  }

  @Nested
  public class TestDatabaseOperation {

    @Test
    public void test() {
      ArcticCatalog catalog = loadMixedIcebergCatalog();
      Assertions.assertTrue(catalog.listDatabases().isEmpty());

      catalog.createDatabase(database);
      Assertions.assertEquals(1, catalog.listDatabases().size());
      Assertions.assertTrue(catalog.listDatabases().contains(database));

      catalog.dropDatabase(database);
      Assertions.assertTrue(catalog.listDatabases().isEmpty());
    }
  }

  @Nested
  public class TestTableOperation {

    @BeforeEach
    public void before() {
      catalog.createDatabase(database);
    }

    @AfterEach
    public void after() {
      catalog.dropDatabase(database);
      try {
        catalog.dropTable(tableIdentifier, true);
      } catch (Exception e) {
        // pass
      }
      try {
        nsCatalog.dropTable(icebergTableIdentifier, true);
      } catch (Exception e) {
        // pass
      }
    }

    @ParameterizedTest(name = "CatalogTableOperationTest[withPrimaryKey={0}]")
    @ValueSource(booleans = {true, false})
    public void catalogTableOperationTests(boolean withPrimary) {
      TableBuilder builder = catalog.newTableBuilder(tableIdentifier, schema)
          .withPartitionSpec(spec);
      if (withPrimary) {
        builder.withPrimaryKeySpec(keySpec);
      }
      // create mixed-iceberg
      builder.create();
      // create iceberg
      nsCatalog.createTable(icebergTableIdentifier, schema);

      // assert 1 table in mixed-iceberg catalog
      Assertions.assertEquals(1, catalog.listTables(database).size());
      Assertions.assertTrue(catalog.tableExists(tableIdentifier));

      // assert 2 tables in iceberg catalog
      Assertions.assertEquals(2, nsCatalog.listTables(Namespace.of(database)).size());
      Set<String> tables =
          nsCatalog.listTables(Namespace.of(database))
              .stream()
              .map(org.apache.iceberg.catalog.TableIdentifier::name)
              .collect(Collectors.toSet());
      Set<String> expects = Sets.newHashSet(table, icebergTable);
      Assertions.assertEquals(expects, tables);

     // assert load table
      ArcticTable mixedIcebergTable = catalog.loadTable(tableIdentifier);
      Assertions.assertEquals(withPrimary, mixedIcebergTable.isKeyedTable());
    }
  }

  private ArcticCatalog loadMixedIcebergCatalog() {
    return CatalogLoader.load(
        ams.getTableServiceUrl() + "/" + AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG);
  }
}

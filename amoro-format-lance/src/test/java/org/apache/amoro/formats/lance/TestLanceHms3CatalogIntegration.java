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

package org.apache.amoro.formats.lance;

import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/** Opt-in smoke test for a real Hive 3 metastore. */
public class TestLanceHms3CatalogIntegration {

  @Test
  public void testRealHmsMetadata() {
    String hmsUri = System.getProperty("lance.hms.uri");
    String catalogName = System.getProperty("lance.hms.catalog");
    String database = System.getProperty("lance.hms.database");
    String table = System.getProperty("lance.hms.table");
    Assumptions.assumeTrue(
        hmsUri != null && catalogName != null && database != null && table != null,
        "Real HMS coordinates were not provided");

    Configuration configuration = new Configuration(false);
    configuration.set("hive.metastore.uris", hmsUri);
    TableMetaStore metaStore = TableMetaStore.builder().withConfiguration(configuration).build();

    try (LanceHms3Catalog catalog =
        new LanceHms3Catalog(catalogName, Collections.emptyMap(), metaStore)) {
      Assertions.assertTrue(catalog.listDatabases().contains(database));
      Assertions.assertTrue(catalog.tableExists(database, table));
      Assertions.assertTrue(catalog.listTables(database).contains(table));
    }
  }
}

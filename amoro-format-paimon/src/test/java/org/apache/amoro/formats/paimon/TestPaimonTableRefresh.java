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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.CachingCatalog;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.options.Options;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;

class TestPaimonTableRefresh {
  @TempDir Path warehouse;

  @Test
  void refreshInvalidatesCachedTableWithoutClosingCatalog() throws Exception {
    try (Catalog original =
            PaimonCatalogFactory.paimonCatalog(
                Collections.singletonMap("warehouse", warehouse.toString()), new Configuration());
        Catalog cached = new CachingCatalog(original, new Options())) {
      original.createDatabase("db", false);
      Identifier id = Identifier.create("db", "tbl");
      original.createTable(id, Schema.newBuilder().column("id", DataTypes.INT()).build(), false);
      PaimonCatalog catalog = new PaimonCatalog(cached, "catalog");
      PaimonTable before = (PaimonTable) catalog.loadTable("db", "tbl");
      original.alterTable(
          id, SchemaChange.setOption("amoro.expire-snapshots.enabled", "true"), false);
      assertNull(before.properties().get("amoro.expire-snapshots.enabled"));
      assertSame(before.originalTable(), catalog.loadTable("db", "tbl").originalTable());
      PaimonTable fresh = before.refresh();
      assertEquals("true", fresh.properties().get("amoro.expire-snapshots.enabled"));
      original.alterTable(id, SchemaChange.setOption("snapshot.num-retained.min", "2"), false);
      assertEquals("2", fresh.refresh().properties().get("snapshot.num-retained.min"));
      assertTrue(catalog.tableExists("db", "tbl"));
      assertThrows(
          IllegalStateException.class,
          () -> new PaimonTable(before.id(), before.originalTable()).refresh());
    }
  }
}

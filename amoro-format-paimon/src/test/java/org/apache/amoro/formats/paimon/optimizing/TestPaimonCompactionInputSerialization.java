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

package org.apache.amoro.formats.paimon.optimizing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.Table;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@DisplayName("PaimonCompactionInput serialization")
public class TestPaimonCompactionInputSerialization {

  private static PaimonTable buildPaimonTable(Path warehouse) throws Exception {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    Catalog catalog = PaimonCatalogFactory.paimonCatalog(props, new Configuration());
    catalog.createDatabase("db1", true);
    Schema schema =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .option("bucket", "-1")
            .build();
    Identifier id = Identifier.create("db1", "t_in");
    catalog.createTable(id, schema, true);
    Table table = catalog.getTable(id);
    return new PaimonTable(TableIdentifier.of("test_catalog", "db1", "t_in"), table);
  }

  @Test
  @DisplayName("Empty Input (no-arg ctor) remains serializable for backward compatibility")
  void testEmptyInputSerialization() {
    PaimonCompactionInput input = new PaimonCompactionInput();
    input.option("k1", "v1");

    ByteBuffer buffer = SerializationUtil.simpleSerialize(input);
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);

    PaimonCompactionInput out = SerializationUtil.simpleDeserialize(bytes);
    assertNotNull(out);
    assertNull(out.getTable());
    assertNull(out.getTaskBytes());
    assertNull(out.getCommitUser());
    assertNull(out.getPartitionPath());
    assertEquals(0, out.getSerializerVersion());
    assertEquals(0L, out.getTargetSnapshotId());
    assertEquals("v1", out.getOptions().get("k1"));
  }

  @Test
  @DisplayName("Fully-populated Input round-trips every field via SerializationUtil")
  void testFullInputRoundTrip(@TempDir Path warehouse) throws Exception {
    PaimonTable table = buildPaimonTable(warehouse);
    byte[] taskBytes = new byte[] {1, 2, 3, 4, 5};
    PaimonCompactionInput input =
        new PaimonCompactionInput(table, taskBytes, 2, "user-uuid-abc", "age=10", 42L);
    input.option("opt-key", "opt-val");

    ByteBuffer buffer = SerializationUtil.simpleSerialize(input);
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);

    PaimonCompactionInput out = SerializationUtil.simpleDeserialize(bytes);
    assertNotNull(out);
    assertNotNull(out.getTable());
    assertEquals(table.id(), out.getTable().id());
    assertEquals(table.originalTable().name(), out.getTable().originalTable().name());
    assertArrayEquals(taskBytes, out.getTaskBytes());
    assertEquals(2, out.getSerializerVersion());
    assertEquals("user-uuid-abc", out.getCommitUser());
    assertEquals("age=10", out.getPartitionPath());
    assertEquals(42L, out.getTargetSnapshotId());
    assertEquals("opt-val", out.getOptions().get("opt-key"));
  }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Risk-front-load probe: verify whether Paimon Table obtained via Catalog.getTable() is
 * Java-serializable. This decides between the preferred design (ship Table object inside
 * PaimonCompactionInput à la Iceberg RewriteFilesInput) and the fallback design (ship catalogType +
 * options + identifier and re-load on Optimizer side).
 */
@DisplayName("C1 risk probe - Paimon FileStoreTable Java serialization")
public class TestPaimonTableSerialization {

  private static Table createAppendOnlyTable(Path warehouse) throws Exception {
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
    Identifier id = Identifier.create("db1", "t_append");
    catalog.createTable(id, schema, true);
    return catalog.getTable(id);
  }

  @Test
  @DisplayName("Paimon Table via ObjectOutputStream round-trip should succeed")
  void testJavaSerializationRoundTrip(@TempDir Path warehouse) throws Exception {
    Table table = createAppendOnlyTable(warehouse);
    assertNotNull(table);

    byte[] bytes;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(table);
      oos.flush();
      bytes = bos.toByteArray();
    }

    Table roundTripped;
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis)) {
      roundTripped = (Table) ois.readObject();
    }

    assertNotNull(roundTripped);
    assertEquals(table.name(), roundTripped.name());
    assertEquals(table.rowType(), roundTripped.rowType());
    assertEquals(table.options(), roundTripped.options());
  }

  @Test
  @DisplayName("PaimonTable wrapper via SerializationUtil round-trip should succeed")
  void testPaimonTableWrapperSerialization(@TempDir Path warehouse) throws Exception {
    Table table = createAppendOnlyTable(warehouse);
    PaimonTable wrapper =
        new PaimonTable(TableIdentifier.of("test_catalog", "db1", "t_append"), table);

    ByteBuffer buffer = SerializationUtil.simpleSerialize(wrapper);
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);

    PaimonTable deserialized = SerializationUtil.simpleDeserialize(bytes);
    assertNotNull(deserialized);
    assertEquals(wrapper.id(), deserialized.id());
    assertEquals(wrapper.originalTable().name(), deserialized.originalTable().name());
    assertEquals(wrapper.originalTable().rowType(), deserialized.originalTable().rowType());
    assertEquals(wrapper.originalTable().options(), deserialized.originalTable().options());
  }
}

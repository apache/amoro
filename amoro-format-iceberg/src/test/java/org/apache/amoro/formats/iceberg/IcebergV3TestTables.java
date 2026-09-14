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

package org.apache.amoro.formats.iceberg;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local tables for testing metadata preservation by unsupported-version guards. */
public final class IcebergV3TestTables {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private IcebergV3TestTables() {}

  public static UnkeyedTable create(Path location, int formatVersion) throws IOException {
    Configuration conf = new Configuration();
    HadoopTables tables = new HadoopTables(conf);
    Schema schema = new Schema(Types.NestedField.required(1, "id", Types.IntegerType.get()));
    Table table =
        tables.create(
            schema,
            PartitionSpec.unpartitioned(),
            Collections.singletonMap("format-version", "2"),
            location.toString());
    IcebergDataTestHelpers.append(table, records(table, 1));
    IcebergDataTestHelpers.append(table, records(table, 2));

    if (formatVersion == 3) {
      return upgradeToV3(table);
    }
    assertEquals(2, formatVersion);
    return wrap(table);
  }

  public static UnkeyedTable wrap(Table table) {
    return IcebergTable.newIcebergTable(
            TableIdentifier.of("test_catalog", "test_database", "test_table"),
            table,
            TableMetaStore.builder().withConfiguration(new Configuration()).build(),
            Collections.emptyMap())
        .originalTable();
  }

  public static UnkeyedTable upgradeToV3(Table table) throws IOException {
    // Commit through an independent handle so existing readers retain their cached V2
    // metadata and Hadoop's normal version-conflict detection remains effective.
    Table external = new HadoopTables(new Configuration()).load(table.location());
    String previousLocation = metadataLocation(external);
    external.updateProperties().set("format-version", "3").commit();
    external.refresh();
    assertFalse(previousLocation.equals(metadataLocation(external)));

    // Complete the synthetic external upgrade with the modern V3 JSON fields. Only
    // this freshly committed temporary fixture is edited, before control returns to
    // the old writer. Existing V2 snapshots remain valid and no row IDs are allocated.
    ObjectNode metadata = (ObjectNode) MAPPER.readTree(metadataBytes(external));
    metadata.put("next-row-id", 0L);
    metadata.remove("row-lineage");
    try (OutputStream output =
        external.io().newOutputFile(metadataLocation(external)).createOrOverwrite()) {
      MAPPER.writeValue(output, metadata);
    }
    UnkeyedTable upgraded = wrap(new HadoopTables(new Configuration()).load(external.location()));
    JsonNode raw = MAPPER.readTree(metadataBytes(upgraded));
    assertEquals(3, raw.get("format-version").asInt());
    assertTrue(raw.has("next-row-id"));
    assertFalse(raw.has("row-lineage"));
    return upgraded;
  }

  public static String metadataLocation(Table table) {
    return ((HasTableOperations) table).operations().current().metadataFileLocation();
  }

  public static byte[] metadataBytes(Table table) throws IOException {
    try (InputStream input = table.io().newInputFile(metadataLocation(table)).newStream()) {
      return input.readAllBytes();
    }
  }

  public static void assertV3MetadataUnchanged(
      Table table, String originalLocation, byte[] originalBytes) throws IOException {
    table.refresh();
    byte[] currentBytes = metadataBytes(table);
    JsonNode currentJson = MAPPER.readTree(currentBytes);
    assertAll(
        () -> assertEquals(originalLocation, metadataLocation(table), "Metadata pointer changed"),
        () -> assertArrayEquals(originalBytes, currentBytes, "Current metadata JSON changed"),
        () -> assertTrue(currentJson.has("next-row-id"), "Commit dropped next-row-id"),
        () -> assertEquals(3, currentJson.get("format-version").asInt()));
  }

  public static List<DataFile> dataFiles(Table table) throws IOException {
    List<DataFile> files = new ArrayList<>();
    try (CloseableIterable<FileScanTask> tasks = table.newScan().planFiles()) {
      for (FileScanTask task : tasks) {
        files.add(task.file().copy());
      }
    }
    return files;
  }

  public static List<Record> records(Table table, int... ids) {
    List<Record> records = new ArrayList<>();
    for (int id : ids) {
      GenericRecord record = GenericRecord.create(table.schema());
      record.setField("id", id);
      records.add(record);
    }
    return records;
  }

  public static List<Integer> readIds(Table table) throws IOException {
    List<Integer> ids = new ArrayList<>();
    try (CloseableIterable<Record> records = IcebergGenerics.read(table).build()) {
      for (Record record : records) {
        ids.add((Integer) record.getField("id"));
      }
    }
    Collections.sort(ids);
    return ids;
  }
}

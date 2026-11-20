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

package org.apache.amoro.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Files;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class TestEventTimeMetrics {
  private static final Schema SCHEMA =
      new Schema(
          Types.NestedField.required(1, "event_time", Types.StringType.get()),
          Types.NestedField.optional(2, "description", Types.StringType.get()),
          Types.NestedField.optional(3, "timestamp", Types.TimestampType.withoutZone()));
  private static final String METRIC_KEY = "write.metadata.metrics.column.event_time";
  @TempDir private Path temp;

  @ParameterizedTest
  @ValueSource(strings = {"PARQUET", "AVRO", "ORC"})
  void testStringTimestampBoundsAreNotTruncated(String format) throws Exception {
    Map<String, String> source =
        Collections.singletonMap(TableProperties.TABLE_EVENT_TIME_FIELD, "event_time");
    BasicUnkeyedTable table = table(source, Collections.emptyMap());
    String timestamp = "2026-10-16 12:34:56";
    GenericRecord row = GenericRecord.create(SCHEMA);
    row.setField("event_time", timestamp);
    row.setField("description", "a description longer than sixteen characters");
    FileAppender<Record> appender =
        new GenericAppenderFactory(SCHEMA, PartitionSpec.unpartitioned())
            .setAll(table.properties())
            .newAppender(
                Files.localOutput(temp.resolve("events." + format.toLowerCase()).toFile()),
                FileFormat.valueOf(format));
    try (FileAppender<Record> output = appender) {
      output.add(row);
    }
    // Avro does not collect column bounds, so only its configuration is asserted.
    assertEquals("full", table.properties().get(METRIC_KEY));
    if (!format.equals("AVRO")) {
      assertEquals(
          timestamp,
          Conversions.fromByteBuffer(
                  Types.StringType.get(), appender.metrics().upperBounds().get(1))
              .toString());
    }
    assertFalse(source.containsKey(METRIC_KEY));
    assertFalse(table.properties().containsKey("write.metadata.metrics.column.description"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"none", "counts", "truncate(32)", "full"})
  void testExplicitColumnModeIsPreserved(String mode) {
    Map<String, String> properties = new HashMap<>();
    properties.put(TableProperties.TABLE_EVENT_TIME_FIELD, "event_time");
    properties.put(METRIC_KEY, mode);
    assertEquals(mode, table(properties, Collections.emptyMap()).properties().get(METRIC_KEY));
  }

  @Test
  void testCatalogDefaultEventTimeIsCovered() {
    BasicUnkeyedTable table =
        table(
            Collections.emptyMap(),
            Collections.singletonMap(
                "table." + TableProperties.TABLE_EVENT_TIME_FIELD, "event_time"));
    assertEquals("full", table.properties().get(METRIC_KEY));
  }

  @ParameterizedTest
  @ValueSource(strings = {"timestamp", "missing", "_ingest_time"})
  void testNonStringOrMissingEventFieldDoesNotChangeMetrics(String field) {
    Map<String, String> properties =
        Collections.singletonMap(TableProperties.TABLE_EVENT_TIME_FIELD, field);
    assertEquals(properties, table(properties, null).properties());
  }

  @Test
  void testNoConfiguredEventFieldDoesNotChangeProperties() {
    assertEquals(Collections.emptyMap(), table(Collections.emptyMap(), null).properties());
  }

  private BasicUnkeyedTable table(
      Map<String, String> properties, Map<String, String> catalogProperties) {
    Table raw = mock(Table.class);
    when(raw.properties()).thenReturn(properties);
    when(raw.schema()).thenReturn(SCHEMA);
    return new BasicUnkeyedTable(
        TableIdentifier.of("catalog", "database", "table"),
        raw,
        mock(AuthenticatedFileIO.class),
        catalogProperties);
  }
}

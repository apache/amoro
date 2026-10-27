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

package org.apache.amoro.server.optimizing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.amoro.spark.reader.SparkParquetReaders;
import org.apache.iceberg.Files;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.AdaptHiveGenericParquetReaders;
import org.apache.iceberg.data.parquet.AdaptHiveGenericParquetWriter;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.catalyst.InternalRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Reads Parquet through the shared mixed-format readers on the Iceberg version selected by AMS. The
 * shared modules are compiled against the connector Iceberg version, so this catches reader API
 * calls that no longer link against the maintenance Iceberg version.
 */
public class TestMaintenanceIcebergCompatibility {

  private static final Schema SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.optional(2, "data", Types.StringType.get()));
  private static final List<Integer> IDS = Arrays.asList(1, 2, 3);

  @TempDir Path temp;
  private OutputFile file;

  @BeforeEach
  void writeParquet() throws IOException {
    file = Files.localOutput(temp.resolve("data.parquet").toFile());
    try (FileAppender<Record> appender =
        AdaptHiveParquet.write(file)
            .schema(SCHEMA)
            .createWriterFunc(AdaptHiveGenericParquetWriter::buildWriter)
            .build()) {
      for (int id : IDS) {
        Record record = GenericRecord.create(SCHEMA);
        record.set(0, id);
        record.set(1, "v" + id);
        appender.add(record);
      }
    }
  }

  @Test
  void testHiveGenericReader() throws IOException {
    List<Integer> ids = new ArrayList<>();
    try (CloseableIterable<Record> records =
        AdaptHiveParquet.read(file.toInputFile())
            .project(SCHEMA)
            .createReaderFunc(
                fileSchema ->
                    AdaptHiveGenericParquetReaders.buildReader(
                        SCHEMA, fileSchema, Collections.emptyMap()))
            .build()) {
      records.forEach(record -> ids.add((Integer) record.get(0)));
    }
    assertEquals(IDS, ids);
  }

  @Test
  void testSparkReader() throws IOException {
    List<Integer> ids = new ArrayList<>();
    try (CloseableIterable<InternalRow> rows =
        Parquet.read(file.toInputFile())
            .project(SCHEMA)
            .createReaderFunc(
                fileSchema ->
                    SparkParquetReaders.buildReader(SCHEMA, fileSchema, Collections.emptyMap()))
            .build()) {
      rows.forEach(row -> ids.add(row.getInt(0)));
    }
    assertEquals(IDS, ids);
  }
}

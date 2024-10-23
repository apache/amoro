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

package org.apache.amoro.spark.writer;

import org.apache.amoro.TableFormat;
import org.apache.amoro.hive.io.HiveDataTestHelpers;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterators;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.spark.io.TaskWriters;
import org.apache.amoro.spark.reader.SparkParquetReaders;
import org.apache.amoro.spark.test.MixedTableTestBase;
import org.apache.amoro.spark.test.utils.RecordGenerator;
import org.apache.amoro.spark.test.utils.TestTableUtil;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Files;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.spark.data.SparkOrcReader;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.connector.write.DataWriter;
import org.apache.spark.sql.connector.write.LogicalWriteInfoImpl;
import org.apache.spark.sql.connector.write.Write;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;
import org.apache.spark.unsafe.types.UTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestMixedWriter extends MixedTableTestBase {

  static final Schema SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "data", Types.StringType.get()),
          Types.NestedField.required(3, "pt", Types.StringType.get()));

  static final PrimaryKeySpec ID_PRIMARY_KEY_SPEC =
      PrimaryKeySpec.builderFor(SCHEMA).addColumn("id").build();

  static final PartitionSpec PT_SPEC = PartitionSpec.builderFor(SCHEMA).identity("pt").build();

  public static Stream<Arguments> testWrite() {
    return Stream.of(
        Arguments.of(
            MIXED_HIVE, WriteMode.APPEND, SCHEMA, ID_PRIMARY_KEY_SPEC, PT_SPEC, FileFormat.PARQUET),
        Arguments.of(
            MIXED_HIVE, WriteMode.APPEND, SCHEMA, NO_PRIMARY_KEY, PT_SPEC, FileFormat.PARQUET),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.APPEND,
            SCHEMA,
            ID_PRIMARY_KEY_SPEC,
            UNPARTITIONED,
            FileFormat.PARQUET),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.APPEND,
            SCHEMA,
            NO_PRIMARY_KEY,
            UNPARTITIONED,
            FileFormat.PARQUET),
        Arguments.of(
            MIXED_HIVE, WriteMode.APPEND, SCHEMA, ID_PRIMARY_KEY_SPEC, PT_SPEC, FileFormat.ORC),
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, SCHEMA, NO_PRIMARY_KEY, PT_SPEC, FileFormat.ORC),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.APPEND,
            SCHEMA,
            ID_PRIMARY_KEY_SPEC,
            UNPARTITIONED,
            FileFormat.ORC),
        Arguments.of(
            MIXED_HIVE, WriteMode.APPEND, SCHEMA, NO_PRIMARY_KEY, UNPARTITIONED, FileFormat.ORC),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.OVERWRITE_DYNAMIC,
            SCHEMA,
            ID_PRIMARY_KEY_SPEC,
            PT_SPEC,
            FileFormat.PARQUET),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.OVERWRITE_DYNAMIC,
            SCHEMA,
            NO_PRIMARY_KEY,
            PT_SPEC,
            FileFormat.PARQUET),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.OVERWRITE_DYNAMIC,
            SCHEMA,
            ID_PRIMARY_KEY_SPEC,
            UNPARTITIONED,
            FileFormat.PARQUET),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.OVERWRITE_DYNAMIC,
            SCHEMA,
            NO_PRIMARY_KEY,
            UNPARTITIONED,
            FileFormat.PARQUET),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.OVERWRITE_DYNAMIC,
            SCHEMA,
            ID_PRIMARY_KEY_SPEC,
            PT_SPEC,
            FileFormat.ORC),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.OVERWRITE_DYNAMIC,
            SCHEMA,
            NO_PRIMARY_KEY,
            PT_SPEC,
            FileFormat.ORC),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.OVERWRITE_DYNAMIC,
            SCHEMA,
            ID_PRIMARY_KEY_SPEC,
            UNPARTITIONED,
            FileFormat.ORC),
        Arguments.of(
            MIXED_HIVE,
            WriteMode.OVERWRITE_DYNAMIC,
            SCHEMA,
            NO_PRIMARY_KEY,
            UNPARTITIONED,
            FileFormat.ORC));
  }

  @DisplayName("Test write mix_hive Table")
  @ParameterizedTest
  @MethodSource
  public void testWrite(
      TableFormat format,
      WriteMode writeMode,
      Schema schema,
      PrimaryKeySpec keySpec,
      PartitionSpec ptSpec,
      FileFormat fileFormat)
      throws IOException {
    MixedTable table =
        createTarget(
            schema,
            tableBuilder ->
                tableBuilder
                    .withPrimaryKeySpec(keySpec)
                    .withProperty(TableProperties.CHANGE_FILE_FORMAT, fileFormat.name())
                    .withProperty(TableProperties.BASE_FILE_FORMAT, fileFormat.name())
                    .withProperty(TableProperties.DEFAULT_FILE_FORMAT, fileFormat.name())
                    .withPartitionSpec(ptSpec));
    Map<String, String> map = new HashMap<>();
    map.put(WriteMode.WRITE_MODE_KEY, writeMode.mode);
    testWriteData(table, map);
  }

  private void testWriteData(MixedTable table, Map<String, String> map) throws IOException {
    StructType structType = SparkSchemaUtil.convert(table.schema());
    LogicalWriteInfoImpl info =
        new LogicalWriteInfoImpl("queryId", structType, new CaseInsensitiveStringMap(map));
    MixedFormatSparkWriteBuilder builder = new MixedFormatSparkWriteBuilder(table, info, catalog());
    Write write = builder.build();
    DataWriter<InternalRow> writer =
        write.toBatch().createBatchWriterFactory(null).createWriter(0, 0);
    // create record
    InternalRow record = geneRowData();
    List<InternalRow> records = Collections.singletonList(record);
    writer.write(record);
    WriteTaskCommit commit = (WriteTaskCommit) writer.commit();
    DataFile[] files = commit.files();
    CloseableIterable<InternalRow> concat =
        CloseableIterable.concat(
            Arrays.stream(files)
                .map(
                    s -> {
                      switch (s.format()) {
                        case PARQUET:
                          return readParquet(table.schema(), s.path().toString());
                        case ORC:
                          return readOrc(table.schema(), s.path().toString());
                        default:
                          throw new UnsupportedOperationException(
                              "Cannot read unknown format: " + s.format());
                      }
                    })
                .collect(Collectors.toList()));
    Set<InternalRow> result = new HashSet<>();
    Iterators.addAll(result, concat.iterator());
    Assertions.assertEquals(result, new HashSet<>(records));
  }

  private CloseableIterable<InternalRow> readParquet(Schema schema, String path) {
    AdaptHiveParquet.ReadBuilder builder =
        AdaptHiveParquet.read(Files.localInput(path))
            .project(schema)
            .createReaderFunc(
                fileSchema -> SparkParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
            .caseSensitive(false);

    return builder.build();
  }

  private CloseableIterable<InternalRow> readOrc(Schema schema, String path) {
    ORC.ReadBuilder builder =
        ORC.read(Files.localInput(path))
            .project(schema)
            .createReaderFunc(fileSchema -> new SparkOrcReader(schema, fileSchema, new HashMap<>()))
            .caseSensitive(false);

    return builder.build();
  }

  private InternalRow geneRowData() {
    return new GenericInternalRow(
        new Object[] {1, UTF8String.fromString("aaa"), UTF8String.fromString("AAA")});
  }

  public static Stream<Arguments> testConsistentWrite() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_HIVE, true), Arguments.of(TableFormat.MIXED_HIVE, false));
  }

  @ParameterizedTest
  @MethodSource
  public void testConsistentWrite(TableFormat format, boolean enableConsistentWrite) {
    MixedTable table =
        createTarget(
            SCHEMA,
            builder ->
                builder.withProperty(
                    HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED, enableConsistentWrite + ""));
    StructType dsSchema = SparkSchemaUtil.convert(SCHEMA);
    List<Record> records = RecordGenerator.buildFor(SCHEMA).build().records(10);
    try (TaskWriter<InternalRow> writer =
        TaskWriters.of(table)
            .withOrderedWriter(false)
            .withDataSourceSchema(dsSchema)
            .newBaseWriter(true)) {
      records.stream()
          .map(r -> TestTableUtil.recordToInternalRow(SCHEMA, r))
          .forEach(
              i -> {
                try {
                  writer.write(i);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
      WriteResult result = writer.complete();
      DataFile[] dataFiles = result.dataFiles();
      HiveDataTestHelpers.assertWriteConsistentFilesName(
          (SupportHive) table, Lists.newArrayList(dataFiles));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

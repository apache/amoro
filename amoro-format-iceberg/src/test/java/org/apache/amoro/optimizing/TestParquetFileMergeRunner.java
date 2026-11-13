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

package org.apache.amoro.optimizing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.Files;
import org.apache.iceberg.MetricsConfig;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.encryption.EncryptedFiles;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.mapping.MappedField;
import org.apache.iceberg.mapping.MappedFields;
import org.apache.iceberg.mapping.NameMapping;
import org.apache.iceberg.parquet.ParquetIOBridge;
import org.apache.iceberg.parquet.ParquetSchemaUtil;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Types;
import org.apache.parquet.column.ParquetProperties.WriterVersion;
import org.apache.parquet.column.values.bloomfilter.BloomFilter;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.format.BloomFilterHeader;
import org.apache.parquet.format.Util;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ColumnChunkMetaData;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.internal.column.columnindex.ColumnIndex;
import org.apache.parquet.internal.column.columnindex.OffsetIndex;
import org.apache.parquet.io.SeekableInputStream;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class TestParquetFileMergeRunner {
  private static final Schema SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.optional(2, "payload", Types.StringType.get()),
          Types.NestedField.optional(
              3,
              "details",
              Types.StructType.of(
                  Types.NestedField.optional(4, "score", Types.IntegerType.get()))));
  private static final MessageType PARQUET_SCHEMA = ParquetSchemaUtil.convert(SCHEMA, "table");
  private static final int ROWS_PER_FILE = 1000;

  @TempDir private java.nio.file.Path temp;

  @Test
  void testEncryptedOutputIsRejectedBeforeCreatingFile() throws IOException {
    java.nio.file.Path output = temp.resolve("encrypted-merge.parquet");
    try (HadoopFileIO io = new HadoopFileIO(new Configuration());
        ParquetFileMergeRunner runner =
            new ParquetFileMergeRunner(
                io,
                PARQUET_SCHEMA,
                Collections.emptyMap(),
                PartitionSpec.unpartitioned(),
                null,
                4096,
                MetricsConfig.fromProperties(Collections.emptyMap()))) {
      IOException failure =
          assertThrows(
              IOException.class,
              () ->
                  runner.start(
                      EncryptedFiles.encryptedOutput(
                          Files.localOutput(output.toFile()), new byte[] {1, 2, 3})));
      assertTrue(failure.getMessage().contains("encrypted output"));
      assertFalse(java.nio.file.Files.exists(output));
    }
    assertFalse(java.nio.file.Files.exists(output));
  }

  @Test
  void testNameMappingPreservesMetricsForFilesWithoutFieldIds() throws IOException {
    MessageType schemaWithoutIds =
        MessageTypeParser.parseMessageType(
            "message table { required int32 id; optional binary payload (UTF8); "
                + "optional group details { optional int32 score; } }");
    java.nio.file.Path input =
        writeFile("without-field-ids.parquet", 0, WriterVersion.PARQUET_1_0, schemaWithoutIds);
    NameMapping mapping =
        NameMapping.of(
            MappedField.of(101, Arrays.asList("id", "renamed_id")),
            MappedField.of(202, "payload"),
            MappedField.of(303, "details", MappedFields.of(MappedField.of(404, "score"))));
    java.nio.file.Path output = temp.resolve("mapped-merge.parquet");
    DataFile result;
    try (HadoopFileIO io = new HadoopFileIO(new Configuration());
        ParquetFileMergeRunner runner =
            new ParquetFileMergeRunner(
                io,
                schemaWithoutIds,
                Collections.emptyMap(),
                PartitionSpec.unpartitioned(),
                null,
                4096,
                MetricsConfig.fromProperties(Collections.emptyMap()),
                mapping)) {
      runner.start(EncryptedFiles.plainAsEncryptedOutput(Files.localOutput(output.toFile())));
      runner.appendFile(Files.localInput(input.toFile()));
      result = runner.result();
    }
    assertEquals(readRows(input), readRows(output));
    assertEquals((long) ROWS_PER_FILE, result.valueCounts().get(101));
    assertEquals(0L, result.nullValueCounts().get(101));
    assertEquals(200L, result.nullValueCounts().get(404));
    assertEquals(
        0,
        Conversions.<Integer>fromByteBuffer(Types.IntegerType.get(), result.lowerBounds().get(101))
            .intValue());
    assertEquals(
        ROWS_PER_FILE - 1,
        Conversions.<Integer>fromByteBuffer(Types.IntegerType.get(), result.upperBounds().get(101))
            .intValue());
    assertEquals(
        2,
        Conversions.<Integer>fromByteBuffer(Types.IntegerType.get(), result.lowerBounds().get(404))
            .intValue());
    assertEquals(
        2 * (ROWS_PER_FILE - 1),
        Conversions.<Integer>fromByteBuffer(Types.IntegerType.get(), result.upperBounds().get(404))
            .intValue());
    assertFalse(result.valueCounts().containsKey(1));
    assertFalse(result.valueCounts().containsKey(4));
  }

  @Test
  void testUnreadableBloomFilterFailsCopyAndCleansOutput() throws IOException {
    java.nio.file.Path input = writeFile("invalid-bloom.parquet", 0, WriterVersion.PARQUET_1_0);
    long bloomOffset;
    try (ParquetFileReader reader = open(input)) {
      bloomOffset = reader.getRowGroups().get(0).getColumns().get(0).getBloomFilterOffset();
      assertTrue(bloomOffset >= 0);
    }
    BloomFilterHeader header;
    try (SeekableInputStream stream =
        ParquetIOBridge.file(Files.localInput(input.toFile())).newStream()) {
      stream.seek(bloomOffset);
      header = Util.readBloomFilterHeader(stream);
    }
    // parquet-java returns null for a bloom filter with an invalid bitset size.
    header.setNumBytes(0);
    ByteArrayOutputStream invalidHeader = new ByteArrayOutputStream();
    Util.writeBloomFilterHeader(header, invalidHeader);
    try (RandomAccessFile file = new RandomAccessFile(input.toFile(), "rw")) {
      file.seek(bloomOffset);
      file.write(invalidHeader.toByteArray());
    }

    java.nio.file.Path output = temp.resolve("failed-merge.parquet");
    try (HadoopFileIO io = new HadoopFileIO(new Configuration());
        ParquetFileMergeRunner runner =
            new ParquetFileMergeRunner(
                io,
                PARQUET_SCHEMA,
                Collections.emptyMap(),
                PartitionSpec.unpartitioned(),
                null,
                4096,
                MetricsConfig.fromProperties(Collections.emptyMap()))) {
      runner.start(EncryptedFiles.plainAsEncryptedOutput(Files.localOutput(output.toFile())));
      IOException failure =
          assertThrows(
              IOException.class, () -> runner.appendFile(Files.localInput(input.toFile())));
      assertTrue(failure.getMessage().contains("Cannot preserve parquet bloom filter"));
    }
    assertFalse(java.nio.file.Files.exists(output));
  }

  @ParameterizedTest
  @CsvSource({
    "PARQUET_1_0,ALL,ALL",
    "PARQUET_2_0,ALL,ALL",
    "PARQUET_1_0,ALL,NONE",
    "PARQUET_1_0,NONE,ALL",
    "PARQUET_1_0,NONE,NONE",
    "PARQUET_1_0,PAGE_INDEX,PAGE_INDEX",
    "PARQUET_1_0,BLOOM_FILTER,BLOOM_FILTER",
    "PARQUET_1_0,COLUMN_INDEX,NONE",
    "PARQUET_1_0,NONE,COLUMN_INDEX",
    "PARQUET_1_0,OFFSET_INDEX,NONE",
    "PARQUET_1_0,NONE,OFFSET_INDEX"
  })
  void testCopyPreservesIndexesAndRows(
      WriterVersion version, MetadataMode firstMode, MetadataMode secondMode) throws IOException {
    java.nio.file.Path first = withMetadata(writeFile("first.parquet", 0, version), firstMode);
    java.nio.file.Path second =
        withMetadata(writeFile("second.parquet", ROWS_PER_FILE, version), secondMode);
    List<java.nio.file.Path> inputs = Arrays.asList(first, second);
    List<String> expectedRows = new ArrayList<>();
    for (java.nio.file.Path input : inputs) {
      expectedRows.addAll(readRows(input));
    }

    java.nio.file.Path output = temp.resolve("merged.parquet");
    DataFile result;
    try (HadoopFileIO io = new HadoopFileIO(new Configuration());
        ParquetFileMergeRunner runner =
            new ParquetFileMergeRunner(
                io,
                PARQUET_SCHEMA,
                Collections.emptyMap(),
                PartitionSpec.unpartitioned(),
                null,
                4096,
                MetricsConfig.fromProperties(Collections.emptyMap()))) {
      runner.start(EncryptedFiles.plainAsEncryptedOutput(Files.localOutput(output.toFile())));
      for (java.nio.file.Path input : inputs) {
        InputFile source = spy(Files.localInput(input.toFile()));
        runner.appendFile(source);
        MetadataMode mode = input.equals(first) ? firstMode : secondMode;
        // The index-free path must reuse its footer reader without opening a second data stream.
        verify(source, times(mode == MetadataMode.NONE ? 1 : 2)).newStream();
      }
      result = runner.result();
    }

    // Read every field, including optional nested data, to detect duplication or corruption.
    assertEquals(expectedRows, readRows(output));
    assertEquals(2L * ROWS_PER_FILE, result.recordCount());
    assertEquals(java.nio.file.Files.size(output), result.fileSizeInBytes());

    try (ParquetFileReader merged = open(output)) {
      int outputGroup = 0;
      for (java.nio.file.Path input : inputs) {
        try (ParquetFileReader source = open(input)) {
          assertTrue(source.getRowGroups().size() > 1, "Fixture must contain multiple row groups");
          for (BlockMetaData group : source.getRowGroups()) {
            BlockMetaData copied = merged.getRowGroups().get(outputGroup++);
            assertEquals(group.getRowCount(), copied.getRowCount());
            for (int column = 0; column < group.getColumns().size(); column++) {
              ColumnChunkMetaData before = group.getColumns().get(column);
              ColumnChunkMetaData after = copied.getColumns().get(column);
              assertEquals(before.getPath(), after.getPath());
              assertEquals(before.getCodec(), after.getCodec());
              MetadataMode mode = input.equals(first) ? firstMode : secondMode;
              assertEquals(mode.columnIndex, before.getColumnIndexReference() != null);
              assertEquals(mode.offsetIndex, before.getOffsetIndexReference() != null);
              assertPageIndexes(source, merged, before, after);
              BloomFilter sourceBloomFilter = source.readBloomFilter(before);
              assertEquals(mode.bloomFilter && column == 0, sourceBloomFilter != null);
              assertBloomFilter(sourceBloomFilter, merged.readBloomFilter(after));
              if (input.equals(second)) {
                assertNotEquals(before.getStartingPos(), after.getStartingPos());
              }
            }
          }
        }
      }
      assertEquals(outputGroup, merged.getRowGroups().size());
      assertEquals(outputGroup, result.splitOffsets().size());
    }
  }

  private java.nio.file.Path writeFile(String name, int firstId, WriterVersion version)
      throws IOException {
    return writeFile(name, firstId, version, PARQUET_SCHEMA);
  }

  private java.nio.file.Path writeFile(
      String name, int firstId, WriterVersion version, MessageType schema) throws IOException {
    java.nio.file.Path path = temp.resolve(name);
    SimpleGroupFactory factory = new SimpleGroupFactory(schema);
    try (ParquetWriter<Group> writer =
        ExampleParquetWriter.builder(ParquetIOBridge.file(Files.localOutput(path.toFile())))
            .withType(schema)
            .withCompressionCodec(CompressionCodecName.ZSTD)
            .withWriterVersion(version)
            .withRowGroupSize(4096)
            .withPageSize(256)
            .withPageRowCountLimit(25)
            .withMinRowCountForPageSizeCheck(1)
            .withMaxRowCountForPageSizeCheck(1)
            .withBloomFilterEnabled("id", true)
            .withBloomFilterNDV("id", ROWS_PER_FILE)
            .build()) {
      for (int i = firstId; i < firstId + ROWS_PER_FILE; i++) {
        Group row = factory.newGroup().append("id", i);
        if (i % 7 != 0) {
          row.append("payload", "payload-" + i);
        }
        if (i % 5 != 0) {
          row.addGroup("details").append("score", i * 2);
        }
        writer.write(row);
      }
    }
    return path;
  }

  private java.nio.file.Path withMetadata(java.nio.file.Path input, MetadataMode mode)
      throws IOException {
    if (mode == MetadataMode.ALL) {
      return input;
    }
    java.nio.file.Path output = temp.resolve("filtered-" + input.getFileName());
    try (ParquetFileReader reader = open(input);
        SeekableInputStream stream =
            ParquetIOBridge.file(Files.localInput(input.toFile())).newStream();
        ParquetFileWriter writer =
            new ParquetFileWriter(
                ParquetIOBridge.file(Files.localOutput(output.toFile())),
                PARQUET_SCHEMA,
                ParquetFileWriter.Mode.CREATE,
                4096,
                0)) {
      writer.start();
      for (BlockMetaData group : reader.getRowGroups()) {
        writer.startBlock(group.getRowCount());
        for (int column = 0; column < group.getColumns().size(); column++) {
          ColumnChunkMetaData chunk = group.getColumns().get(column);
          writer.appendColumnChunk(
              PARQUET_SCHEMA.getColumns().get(column),
              stream,
              chunk,
              mode.bloomFilter ? reader.readBloomFilter(chunk) : null,
              mode.columnIndex ? reader.readColumnIndex(chunk) : null,
              mode.offsetIndex ? reader.readOffsetIndex(chunk) : null);
        }
        writer.endBlock();
      }
      writer.end(Collections.emptyMap());
    }
    return output;
  }

  private enum MetadataMode {
    NONE(false, false, false),
    PAGE_INDEX(true, true, false),
    COLUMN_INDEX(true, false, false),
    OFFSET_INDEX(false, true, false),
    BLOOM_FILTER(false, false, true),
    ALL(true, true, true);

    private final boolean columnIndex;
    private final boolean offsetIndex;
    private final boolean bloomFilter;

    MetadataMode(boolean columnIndex, boolean offsetIndex, boolean bloomFilter) {
      this.columnIndex = columnIndex;
      this.offsetIndex = offsetIndex;
      this.bloomFilter = bloomFilter;
    }
  }

  private static void assertPageIndexes(
      ParquetFileReader source,
      ParquetFileReader target,
      ColumnChunkMetaData before,
      ColumnChunkMetaData after)
      throws IOException {
    ColumnIndex sourceIndex = source.readColumnIndex(before);
    ColumnIndex targetIndex = target.readColumnIndex(after);
    OffsetIndex sourceOffsets = source.readOffsetIndex(before);
    OffsetIndex targetOffsets = target.readOffsetIndex(after);
    if (sourceIndex == null) {
      assertNull(targetIndex);
    } else {
      assertNotNull(targetIndex);
      assertEquals(sourceIndex.getBoundaryOrder(), targetIndex.getBoundaryOrder());
      assertEquals(sourceIndex.getNullPages(), targetIndex.getNullPages());
      assertEquals(sourceIndex.getNullCounts(), targetIndex.getNullCounts());
      assertEquals(sourceIndex.getMinValues(), targetIndex.getMinValues());
      assertEquals(sourceIndex.getMaxValues(), targetIndex.getMaxValues());
    }
    if (sourceOffsets == null) {
      assertNull(targetOffsets);
      return;
    }
    assertNotNull(targetOffsets);
    assertTrue(sourceOffsets.getPageCount() > 1, "Fixture must contain multiple pages per chunk");
    assertEquals(sourceOffsets.getPageCount(), targetOffsets.getPageCount());
    long displacement = after.getStartingPos() - before.getStartingPos();
    for (int page = 0; page < sourceOffsets.getPageCount(); page++) {
      assertEquals(sourceOffsets.getOffset(page) + displacement, targetOffsets.getOffset(page));
      assertEquals(
          sourceOffsets.getCompressedPageSize(page), targetOffsets.getCompressedPageSize(page));
      assertEquals(sourceOffsets.getFirstRowIndex(page), targetOffsets.getFirstRowIndex(page));
    }
  }

  private static void assertBloomFilter(BloomFilter before, BloomFilter after) throws IOException {
    if (before == null) {
      assertNull(after);
      return;
    }
    assertNotNull(after);
    ByteArrayOutputStream original = new ByteArrayOutputStream();
    ByteArrayOutputStream copied = new ByteArrayOutputStream();
    before.writeTo(original);
    after.writeTo(copied);
    assertArrayEquals(original.toByteArray(), copied.toByteArray());
  }

  private static List<String> readRows(java.nio.file.Path input) throws IOException {
    List<String> rows = new ArrayList<>();
    try (ParquetReader<Group> reader =
        ParquetReader.builder(new GroupReadSupport(), new Path(input.toUri())).build()) {
      Group row;
      while ((row = reader.read()) != null) {
        rows.add(row.toString());
      }
    }
    return rows;
  }

  private static ParquetFileReader open(java.nio.file.Path input) throws IOException {
    return ParquetFileReader.open(ParquetIOBridge.file(Files.localInput(input.toFile())));
  }
}

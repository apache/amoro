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

package org.apache.amoro.io;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.io.reader.CombinedDeleteFilter;
import org.apache.amoro.io.reader.DeleteCache;
import org.apache.amoro.io.reader.GenericCombinedIcebergDataReader;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.TestHelpers;
import org.apache.iceberg.data.FileHelpers;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestIcebergCombinedReader extends TableTestBase {

  private FileFormat fileFormat;

  private RewriteFilesInput scanTask;

  private RewriteFilesInput dataScanTask;

  private RewriteFilesInput filterEqDeleteScanTask;

  public static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(true, FileFormat.PARQUET, true),
        Arguments.of(false, FileFormat.PARQUET, true),
        Arguments.of(true, FileFormat.AVRO, true),
        Arguments.of(false, FileFormat.AVRO, true),
        Arguments.of(true, FileFormat.ORC, true),
        Arguments.of(false, FileFormat.ORC, true),
        Arguments.of(true, FileFormat.PARQUET, false),
        Arguments.of(false, FileFormat.PARQUET, false),
        Arguments.of(true, FileFormat.AVRO, false),
        Arguments.of(false, FileFormat.AVRO, false),
        Arguments.of(true, FileFormat.ORC, false),
        Arguments.of(false, FileFormat.ORC, false));
  }

  private static Map<String, String> buildTableProperties(FileFormat fileFormat) {
    Map<String, String> tableProperties = Maps.newHashMapWithExpectedSize(3);
    tableProperties.put(TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(TableProperties.DEFAULT_FILE_FORMAT, fileFormat.name());
    tableProperties.put(TableProperties.DELETE_DEFAULT_FILE_FORMAT, fileFormat.name());
    return tableProperties;
  }

  private void prepare(boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    setupTable(
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(false, partitionedTable, buildTableProperties(fileFormat)));
    this.fileFormat = fileFormat;
    if (deleteCacheEnabled) {
      System.setProperty(DeleteCache.DELETE_CACHE_ENABLED, "true");
    } else {
      System.setProperty(DeleteCache.DELETE_CACHE_ENABLED, "false");
    }
    initDataAndReader();
  }

  private StructLike getPartitionData() {
    if (isPartitionedTable()) {
      return TestHelpers.Row.of(0);
    } else {
      return TestHelpers.Row.of();
    }
  }

  private void initDataAndReader() throws IOException {
    StructLike partitionData = getPartitionData();
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(getMixedTable().asUnkeyedTable(), 0, 1)
            .format(fileFormat)
            .build();
    DataFile dataFile =
        FileHelpers.writeDataFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            Arrays.asList(
                MixedDataTestHelpers.createRecord(1, "john", 0, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(2, "lily", 1, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(3, "sam", 2, "1970-01-01T08:00:00")));

    Schema idSchema = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1));
    GenericRecord idRecord = GenericRecord.create(idSchema);
    DeleteFile eqDeleteFile =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            Collections.singletonList(idRecord.copy("id", 1)),
            idSchema);

    List<Pair<CharSequence, Long>> deletes = Lists.newArrayList();
    deletes.add(Pair.of(dataFile.path(), 1L));
    DeleteFile posDeleteFile =
        FileHelpers.writeDeleteFile(
                getMixedTable().asUnkeyedTable(),
                outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
                partitionData,
                deletes)
            .first();

    List<Record> records = new ArrayList<>();
    IntStream.range(2, 100).forEach(id -> records.add(idRecord.copy("id", id)));
    DeleteFile eqDeleteFile1 =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            records,
            idSchema);
    DeleteFile eqDeleteFile2 =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            records,
            idSchema);

    scanTask =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DeleteFile[] {
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile, 2L),
              MixedDataTestHelpers.wrapIcebergDeleteFile(posDeleteFile, 3L)
            },
            new DeleteFile[] {},
            getMixedTable());
    dataScanTask =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DeleteFile[] {},
            new DeleteFile[] {},
            getMixedTable());
    filterEqDeleteScanTask =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DeleteFile[] {},
            new DeleteFile[] {
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile1, 2L),
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile2, 3L)
            },
            getMixedTable());
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}, deleteCacheEnabled = {2}")
  @MethodSource("parameters")
  public void readAllData(
      boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    prepare(partitionedTable, fileFormat, deleteCacheEnabled);
    GenericCombinedIcebergDataReader dataReader =
        new GenericCombinedIcebergDataReader(
            getMixedTable().io(),
            getMixedTable().schema(),
            getMixedTable().spec(),
            getMixedTable().asUnkeyedTable().encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            scanTask,
            "");
    try (CloseableIterable<Record> records = dataReader.readData()) {
      Assertions.assertEquals(1, Iterables.size(records));
      Record record = Iterables.getFirst(records, null);
      Assertions.assertEquals(record.get(0), 3);
    }
    dataReader.close();
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}, deleteCacheEnabled = {2}")
  @MethodSource("parameters")
  public void readAllDataNegate(
      boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    prepare(partitionedTable, fileFormat, deleteCacheEnabled);
    GenericCombinedIcebergDataReader dataReader =
        new GenericCombinedIcebergDataReader(
            getMixedTable().io(),
            getMixedTable().schema(),
            getMixedTable().spec(),
            getMixedTable().asUnkeyedTable().encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            scanTask,
            "");
    try (CloseableIterable<Record> records = dataReader.readDeletedData()) {
      Assertions.assertEquals(2, Iterables.size(records));
      Record first = Iterables.getFirst(records, null);
      Assertions.assertEquals(first.get(1), 0L);
      Record last = Iterables.getLast(records);
      Assertions.assertEquals(last.get(1), 1L);
    }
    dataReader.close();
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}, deleteCacheEnabled = {2}")
  @MethodSource("parameters")
  public void readOnlyData(
      boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    prepare(partitionedTable, fileFormat, deleteCacheEnabled);
    GenericCombinedIcebergDataReader dataReader =
        new GenericCombinedIcebergDataReader(
            getMixedTable().io(),
            getMixedTable().schema(),
            getMixedTable().spec(),
            getMixedTable().asUnkeyedTable().encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            dataScanTask,
            "");
    try (CloseableIterable<Record> records = dataReader.readData()) {
      Assertions.assertEquals(3, Iterables.size(records));
    }
    dataReader.close();
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}, deleteCacheEnabled = {2}")
  @MethodSource("parameters")
  public void readOnlyDataNegate(
      boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    prepare(partitionedTable, fileFormat, deleteCacheEnabled);
    GenericCombinedIcebergDataReader dataReader =
        new GenericCombinedIcebergDataReader(
            getMixedTable().io(),
            getMixedTable().schema(),
            getMixedTable().spec(),
            getMixedTable().asUnkeyedTable().encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            dataScanTask,
            "");
    try (CloseableIterable<Record> records = dataReader.readDeletedData()) {
      Assertions.assertEquals(0, Iterables.size(records));
    }
    dataReader.close();
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}, deleteCacheEnabled = {2}")
  @MethodSource("parameters")
  public void readDataEnableFilterEqDelete(
      boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    prepare(partitionedTable, fileFormat, deleteCacheEnabled);
    CombinedDeleteFilter.FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT = 100L;
    GenericCombinedIcebergDataReader dataReader =
        new GenericCombinedIcebergDataReader(
            getMixedTable().io(),
            getMixedTable().schema(),
            getMixedTable().spec(),
            getMixedTable().asUnkeyedTable().encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            filterEqDeleteScanTask,
            "");

    Assertions.assertTrue(dataReader.getDeleteFilter().isFilterEqDelete());

    try (CloseableIterable<Record> records = dataReader.readData()) {
      Assertions.assertEquals(1, Iterables.size(records));
    }

    try (CloseableIterable<Record> records = dataReader.readDeletedData()) {
      Assertions.assertEquals(2, Iterables.size(records));
    }
    dataReader.close();
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}, deleteCacheEnabled = {2}")
  @MethodSource("parameters")
  public void readDataDropAEqField(
      boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    prepare(partitionedTable, fileFormat, deleteCacheEnabled);
    CombinedDeleteFilter.FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT = 100L;
    StructLike partitionData = getPartitionData();
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(getMixedTable().asUnkeyedTable(), 0, 1)
            .format(fileFormat)
            .build();
    DataFile dataFile =
        FileHelpers.writeDataFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            Arrays.asList(
                MixedDataTestHelpers.createRecord(1, "john", 0, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(2, "lily", 1, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(3, "sam", 2, "1970-01-01T08:00:00")));

    Schema idSchema1 = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1, 2));
    GenericRecord idRecord = GenericRecord.create(idSchema1);
    List<Record> records = new ArrayList<>();
    IntStream.range(2, 100).forEach(id -> records.add(idRecord.copy("id", id, "name", "john")));
    DeleteFile eqDeleteFile1 =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            records,
            idSchema1);

    // Assuming that drop an identifier field
    Schema idSchema2 = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1));
    GenericRecord idRecord2 = GenericRecord.create(idSchema2);
    List<Record> records2 = new ArrayList<>();
    IntStream.range(2, 100).forEach(id -> records2.add(idRecord2.copy("id", id)));
    DeleteFile eqDeleteFile2 =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            records2,
            idSchema2);

    RewriteFilesInput task2 =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 3L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 3L)},
            new DeleteFile[] {},
            new DeleteFile[] {
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile1, 4L),
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile2, 5L)
            },
            getMixedTable());

    GenericCombinedIcebergDataReader dataReader =
        new GenericCombinedIcebergDataReader(
            getMixedTable().io(),
            getMixedTable().schema(),
            getMixedTable().spec(),
            getMixedTable().asUnkeyedTable().encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            task2,
            "");
    try (CloseableIterable<Record> readRecords = dataReader.readData()) {
      Assertions.assertEquals(1, Iterables.size(readRecords));
    }

    try (CloseableIterable<Record> readRecords = dataReader.readDeletedData()) {
      Assertions.assertEquals(2, Iterables.size(readRecords));
    }

    dataReader.close();
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}, deleteCacheEnabled = {2}")
  @MethodSource("parameters")
  public void readDataReplaceAEqField(
      boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    prepare(partitionedTable, fileFormat, deleteCacheEnabled);
    StructLike partitionData = getPartitionData();
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(getMixedTable().asUnkeyedTable(), 0, 1)
            .format(fileFormat)
            .build();
    DataFile dataFile =
        FileHelpers.writeDataFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            Arrays.asList(
                MixedDataTestHelpers.createRecord(1, "john", 0, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(2, "lily", 1, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(3, "sam", 2, "1970-01-01T08:00:00")));

    Schema idSchema1 = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1));
    GenericRecord idRecord1 = GenericRecord.create(idSchema1);
    List<Record> records1 = new ArrayList<>();
    IntStream.range(2, 100).forEach(id -> records1.add(idRecord1.copy("id", id)));
    DeleteFile eqDeleteFile1 =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            records1,
            idSchema1);

    // Write records and identifier field is `name` instead
    Schema idSchema2 = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(2));
    GenericRecord idRecord2 = GenericRecord.create(idSchema2);
    List<Record> records2 = new ArrayList<>();
    records2.add(idRecord2.copy("name", "john"));
    DeleteFile eqDeleteFile2 =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            records2,
            idSchema2);
    RewriteFilesInput task =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 3L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 3L)},
            new DeleteFile[] {},
            new DeleteFile[] {
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile1, 4L),
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile2, 5L)
            },
            getMixedTable());
    GenericCombinedIcebergDataReader dataReader =
        new GenericCombinedIcebergDataReader(
            getMixedTable().io(),
            getMixedTable().schema(),
            getMixedTable().spec(),
            getMixedTable().asUnkeyedTable().encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            task,
            "");
    try (CloseableIterable<Record> readRecords = dataReader.readData()) {
      Assertions.assertEquals(0, Iterables.size(readRecords));
    }
    try (CloseableIterable<Record> readRecords = dataReader.readDeletedData()) {
      Assertions.assertEquals(3, Iterables.size(readRecords));
    }

    dataReader.close();
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}, deleteCacheEnabled = {2}")
  @MethodSource("parameters")
  public void readReadAddAEqField(
      boolean partitionedTable, FileFormat fileFormat, boolean deleteCacheEnabled)
      throws IOException {
    prepare(partitionedTable, fileFormat, deleteCacheEnabled);
    StructLike partitionData = getPartitionData();
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(getMixedTable().asUnkeyedTable(), 0, 1)
            .format(fileFormat)
            .build();
    DataFile dataFile =
        FileHelpers.writeDataFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            Arrays.asList(
                MixedDataTestHelpers.createRecord(1, "john", 0, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(2, "lily", 1, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(3, "sam", 2, "1970-01-01T08:00:00")));

    Schema idSchema1 = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1));
    GenericRecord idRecord1 = GenericRecord.create(idSchema1);
    List<Record> records1 = new ArrayList<>();
    IntStream.range(2, 100).forEach(id -> records1.add(idRecord1.copy("id", id)));
    DeleteFile eqDeleteFile1 =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            records1,
            idSchema1);

    // Write delete records and add a new field `name`
    Schema idSchema2 = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1, 2));
    GenericRecord idRecord2 = GenericRecord.create(idSchema2);
    List<Record> records2 = new ArrayList<>();
    IntStream.range(1, 100).forEach(id -> records2.add(idRecord2.copy("id", id, "name", "john")));
    DeleteFile eqDeleteFile2 =
        FileHelpers.writeDeleteFile(
            getMixedTable().asUnkeyedTable(),
            outputFileFactory.newOutputFile(partitionData).encryptingOutputFile(),
            partitionData,
            records2,
            idSchema2);
    RewriteFilesInput task =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 3L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 3L)},
            new DeleteFile[] {},
            new DeleteFile[] {
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile1, 4L),
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile2, 5L)
            },
            getMixedTable());

    GenericCombinedIcebergDataReader dataReader =
        new GenericCombinedIcebergDataReader(
            getMixedTable().io(),
            getMixedTable().schema(),
            getMixedTable().spec(),
            getMixedTable().asUnkeyedTable().encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            task,
            "");

    try (CloseableIterable<Record> readRecords = dataReader.readData()) {
      Assertions.assertEquals(0, Iterables.size(readRecords));
    }
    try (CloseableIterable<Record> readRecords = dataReader.readDeletedData()) {
      Assertions.assertEquals(3, Iterables.size(readRecords));
    }

    dataReader.close();
  }
}

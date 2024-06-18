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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class TestIcebergCombinedReader extends TableTestBase {

  private final FileFormat fileFormat;

  private RewriteFilesInput scanTask;

  private RewriteFilesInput dataScanTask;

  private RewriteFilesInput filterEqDeleteScanTask;

  public TestIcebergCombinedReader(boolean partitionedTable, FileFormat fileFormat) {
    super(
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(false, partitionedTable, buildTableProperties(fileFormat)));
    this.fileFormat = fileFormat;
  }

  @Parameterized.Parameters(name = "partitionedTable = {0}, fileFormat = {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {true, FileFormat.PARQUET}, {false, FileFormat.PARQUET},
      {true, FileFormat.AVRO}, {false, FileFormat.AVRO},
      {true, FileFormat.ORC}, {false, FileFormat.ORC}
    };
  }

  private static Map<String, String> buildTableProperties(FileFormat fileFormat) {
    Map<String, String> tableProperties = Maps.newHashMapWithExpectedSize(3);
    tableProperties.put(TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(TableProperties.DEFAULT_FILE_FORMAT, fileFormat.name());
    tableProperties.put(TableProperties.DELETE_DEFAULT_FILE_FORMAT, fileFormat.name());
    return tableProperties;
  }

  private StructLike getPartitionData() {
    if (isPartitionedTable()) {
      return TestHelpers.Row.of(0);
    } else {
      return TestHelpers.Row.of();
    }
  }

  @Before
  public void initDataAndReader() throws IOException {
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

  @Test
  public void readAllData() throws IOException {
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
            scanTask);
    try (CloseableIterable<Record> records = dataReader.readData()) {
      Assert.assertEquals(1, Iterables.size(records));
      Record record = Iterables.getFirst(records, null);
      Assert.assertEquals(record.get(0), 3);
    }
    dataReader.close();
  }

  @Test
  public void readAllDataNegate() throws IOException {
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
            scanTask);
    try (CloseableIterable<Record> records = dataReader.readDeletedData()) {
      Assert.assertEquals(2, Iterables.size(records));
      Record first = Iterables.getFirst(records, null);
      Assert.assertEquals(first.get(1), 0L);
      Record last = Iterables.getLast(records);
      Assert.assertEquals(last.get(1), 1L);
    }
    dataReader.close();
  }

  @Test
  public void readOnlyData() throws IOException {
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
            dataScanTask);
    try (CloseableIterable<Record> records = dataReader.readData()) {
      Assert.assertEquals(3, Iterables.size(records));
    }
    dataReader.close();
  }

  @Test
  public void readOnlyDataNegate() throws IOException {
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
            dataScanTask);
    try (CloseableIterable<Record> records = dataReader.readDeletedData()) {
      Assert.assertEquals(0, Iterables.size(records));
    }
    dataReader.close();
  }

  @Test
  public void readDataEnableFilterEqDelete() throws IOException {
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
            filterEqDeleteScanTask);

    Assert.assertTrue(dataReader.getDeleteFilter().isFilterEqDelete());

    try (CloseableIterable<Record> records = dataReader.readData()) {
      Assert.assertEquals(1, Iterables.size(records));
    }

    try (CloseableIterable<Record> records = dataReader.readDeletedData()) {
      Assert.assertEquals(2, Iterables.size(records));
    }
    dataReader.close();
  }
}

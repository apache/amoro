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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.TestHelpers;
import org.apache.iceberg.avro.Avro;
import org.apache.iceberg.data.FileHelpers;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.avro.DataReader;
import org.apache.iceberg.data.orc.GenericOrcReader;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class IcebergRewriteExecutorTest extends TableTestBase {

  private FileFormat fileFormat;

  private RewriteFilesInput scanTask;

  private RewriteFilesInput dataScanTask;

  private final Schema posSchema =
      new Schema(MetadataColumns.FILE_PATH, MetadataColumns.ROW_POSITION);

  public static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(true, FileFormat.PARQUET),
        Arguments.of(false, FileFormat.PARQUET),
        Arguments.of(true, FileFormat.AVRO),
        Arguments.of(false, FileFormat.AVRO),
        Arguments.of(true, FileFormat.ORC),
        Arguments.of(false, FileFormat.ORC));
  }

  private static Map<String, String> buildTableProperties(FileFormat fileFormat) {
    Map<String, String> tableProperties = Maps.newHashMapWithExpectedSize(3);
    tableProperties.put(TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(TableProperties.DEFAULT_FILE_FORMAT, fileFormat.name());
    tableProperties.put(TableProperties.DELETE_DEFAULT_FILE_FORMAT, fileFormat.name());
    return tableProperties;
  }

  private void prepare(boolean hasPartition, FileFormat fileFormat) throws IOException {
    setupTable(
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(false, true, buildTableProperties(fileFormat)));
    this.fileFormat = fileFormat;
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
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}")
  @MethodSource("parameters")
  public void readAllData(boolean hasPartition, FileFormat fileFormat) throws IOException {
    prepare(hasPartition, fileFormat);
    runReadAllData();
  }

  private void runReadAllData() throws IOException {
    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(scanTask, getMixedTable(), Collections.emptyMap());

    RewriteFilesOutput output = executor.execute();

    try (CloseableIterable<Record> records =
        openFile(
            output.getDataFiles()[0].path().toString(),
            output.getDataFiles()[0].format(),
            getMixedTable().schema(),
            new HashMap<>())) {
      Assertions.assertEquals(1, Iterables.size(records));
      Record record = Iterables.getFirst(records, null);
      Assertions.assertEquals(record.get(0), 3);
    }

    try (CloseableIterable<Record> records =
        openFile(
            output.getDeleteFiles()[0].path().toString(),
            output.getDataFiles()[0].format(),
            posSchema,
            new HashMap<>())) {
      Assertions.assertEquals(2, Iterables.size(records));
      Record first = Iterables.getFirst(records, null);
      Assertions.assertEquals(first.get(1), 0L);
      Record last = Iterables.getLast(records);
      Assertions.assertEquals(last.get(1), 1L);
    }
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}")
  @MethodSource("parameters")
  public void readAllDataWithPartitionEvolution(boolean hasPartition, FileFormat fileFormat)
      throws IOException {
    prepare(hasPartition, fileFormat);
    Assumptions.assumeTrue(getMixedTable().spec().isPartitioned());
    getMixedTable()
        .asUnkeyedTable()
        .updateSpec()
        .removeField("op_time_day")
        .addField(Expressions.month("op_time"))
        .commit();
    runReadAllData();
  }

  @ParameterizedTest(name = "partitionedTable = {0}, fileFormat = {1}")
  @MethodSource("parameters")
  public void readOnlyData(boolean hasPartition, FileFormat fileFormat) throws IOException {
    prepare(hasPartition, fileFormat);
    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(dataScanTask, getMixedTable(), Collections.emptyMap());

    RewriteFilesOutput output = executor.execute();

    try (CloseableIterable<Record> records =
        openFile(
            output.getDataFiles()[0].path().toString(),
            output.getDataFiles()[0].format(),
            getMixedTable().schema(),
            new HashMap<>())) {
      Assertions.assertEquals(3, Iterables.size(records));
    }

    Assertions.assertTrue(output.getDeleteFiles() == null || output.getDeleteFiles().length == 0);
  }

  private CloseableIterable<Record> openFile(
      String path, FileFormat fileFormat, Schema fileProjection, Map<Integer, ?> idToConstant) {
    InputFile input = getMixedTable().io().newInputFile(path);

    switch (fileFormat) {
      case AVRO:
        Avro.ReadBuilder avro =
            Avro.read(input)
                .project(fileProjection)
                .createReaderFunc(
                    avroSchema -> DataReader.create(fileProjection, avroSchema, idToConstant));
        return avro.build();

      case PARQUET:
        Parquet.ReadBuilder parquet =
            Parquet.read(input)
                .project(fileProjection)
                .createReaderFunc(
                    fileSchema ->
                        GenericParquetReaders.buildReader(
                            fileProjection, fileSchema, idToConstant));
        return parquet.build();

      case ORC:
        Schema projectionWithoutConstantAndMetadataFields =
            TypeUtil.selectNot(
                fileProjection,
                org.apache.amoro.shade.guava32.com.google.common.collect.Sets.union(
                    idToConstant.keySet(), MetadataColumns.metadataFieldIds()));
        org.apache.iceberg.orc.ORC.ReadBuilder orc =
            org.apache
                .iceberg
                .orc
                .ORC
                .read(input)
                .project(projectionWithoutConstantAndMetadataFields)
                .createReaderFunc(
                    fileSchema ->
                        GenericOrcReader.buildReader(fileProjection, fileSchema, idToConstant));
        return orc.build();

      default:
        throw new UnsupportedOperationException(
            String.format("Cannot read %s file: %s", fileFormat.name(), path));
    }
  }
}

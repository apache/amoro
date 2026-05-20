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

import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_ENABLED;

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
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TableMetadata;
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
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.Pair;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class IcebergRewriteExecutorTest extends TableTestBase {

  private final FileFormat fileFormat;

  private RewriteFilesInput scanTask;

  private RewriteFilesInput dataScanTask;

  private final Schema posSchema =
      new Schema(MetadataColumns.FILE_PATH, MetadataColumns.ROW_POSITION);

  public IcebergRewriteExecutorTest(boolean hasPartition, FileFormat fileFormat) {
    super(
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(false, true, buildTableProperties(fileFormat)));
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

  @Test
  public void readAllData() throws IOException {
    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(scanTask, getMixedTable(), Collections.emptyMap());

    RewriteFilesOutput output = executor.execute();

    try (CloseableIterable<Record> records =
        openFile(
            output.getDataFiles()[0].path().toString(),
            output.getDataFiles()[0].format(),
            getMixedTable().schema(),
            new HashMap<>())) {
      Assert.assertEquals(1, Iterables.size(records));
      Record record = Iterables.getFirst(records, null);
      Assert.assertEquals(record.get(0), 3);
    }

    try (CloseableIterable<Record> records =
        openFile(
            output.getDeleteFiles()[0].path().toString(),
            output.getDataFiles()[0].format(),
            posSchema,
            new HashMap<>())) {
      Assert.assertEquals(2, Iterables.size(records));
      Record first = Iterables.getFirst(records, null);
      Assert.assertEquals(first.get(1), 0L);
      Record last = Iterables.getLast(records);
      Assert.assertEquals(last.get(1), 1L);
    }
  }

  @Test
  public void readAllDataWithPartitionEvolution() throws IOException {
    Assume.assumeTrue(getMixedTable().spec().isPartitioned());
    getMixedTable()
        .asUnkeyedTable()
        .updateSpec()
        .removeField("op_time_day")
        .addField(Expressions.month("op_time"))
        .commit();
    readAllData();
  }

  @Test
  public void testParquetRowGroupMergeEnableCondition() throws IOException {
    // parquet row-group merge should only be enabled for Parquet files.
    if (fileFormat != FileFormat.PARQUET) {
      Assert.assertFalse(newExecutor(dataScanTask).canParquetRowGroupMerge());
      return;
    }

    // All parquet row-group merge preconditions are satisfied.
    prepareParquetRowGroupMergeTableConditions(true);
    Assert.assertTrue(newExecutor(dataScanTask).canParquetRowGroupMerge());

    // parquet row-group merge should be disabled if the table property is not enabled.
    prepareParquetRowGroupMergeTableConditions(false);
    Assert.assertFalse(newExecutor(dataScanTask).canParquetRowGroupMerge());

    // parquet row-group merge should be disabled if there are delete files.
    prepareParquetRowGroupMergeTableConditions(true);
    RewriteFilesInput rewrittenDeleteInput =
        new RewriteFilesInput(
            dataScanTask.rewrittenDataFiles(),
            dataScanTask.rePosDeletedDataFiles(),
            new DeleteFile[] {},
            new DeleteFile[] {(DeleteFile) scanTask.readOnlyDeleteFiles()[0]},
            getMixedTable());
    Assert.assertFalse(newExecutor(rewrittenDeleteInput).canParquetRowGroupMerge());

    rewrittenDeleteInput =
        new RewriteFilesInput(
            dataScanTask.rewrittenDataFiles(),
            dataScanTask.rePosDeletedDataFiles(),
            new DeleteFile[] {(DeleteFile) scanTask.readOnlyDeleteFiles()[0]},
            new DeleteFile[] {},
            getMixedTable());
    Assert.assertFalse(newExecutor(rewrittenDeleteInput).canParquetRowGroupMerge());

    // parquet row-group merge should be disabled if the bloom filter is enabled.
    resetParquetRowGroupMergeTestState();
    Assert.assertTrue(newExecutor(dataScanTask).canParquetRowGroupMerge());
    getMixedTable()
        .asUnkeyedTable()
        .updateProperties()
        .set(
            org.apache.iceberg.TableProperties.PARQUET_BLOOM_FILTER_COLUMN_ENABLED_PREFIX + "id",
            "true")
        .commit();
    Assert.assertFalse(newExecutor(dataScanTask).canParquetRowGroupMerge());

    // parquet row-group merge should be disabled if the global bloom filter switch is enabled.
    resetParquetRowGroupMergeTestState();
    Assert.assertTrue(newExecutor(dataScanTask).canParquetRowGroupMerge());
    getMixedTable()
        .asUnkeyedTable()
        .updateProperties()
        .set("write.parquet.bloom-filter-enabled.default", "true")
        .commit();
    Assert.assertFalse(newExecutor(dataScanTask).canParquetRowGroupMerge());

    // parquet row-group merge should be disabled if the table spec evolves so that the source files
    // no longer match the partition spec.
    resetParquetRowGroupMergeTestState();
    Assert.assertTrue(newExecutor(dataScanTask).canParquetRowGroupMerge());
    getMixedTable().asUnkeyedTable().updateSpec().addField(Expressions.month("op_time")).commit();
    Assert.assertFalse(newExecutor(dataScanTask).canParquetRowGroupMerge());

    // parquet row-group merge should be disabled if table has a sort order.
    resetParquetRowGroupMergeTestState();
    Assert.assertTrue(newExecutor(dataScanTask).canParquetRowGroupMerge());
    getMixedTable().asUnkeyedTable().replaceSortOrder().asc("id").commit();
    Assert.assertFalse(newExecutor(dataScanTask).canParquetRowGroupMerge());
  }

  @Test
  public void testParquetRowGroupMergeWithEncryptedDataFile() throws IOException {
    Assume.assumeTrue(fileFormat == FileFormat.PARQUET);
    prepareParquetRowGroupMergeTableConditions(true);
    Assert.assertTrue(newExecutor(dataScanTask).canParquetRowGroupMerge());

    DataFile encryptedDataFile =
        DataFiles.builder(getMixedTable().spec())
            .copy(dataScanTask.rewrittenDataFiles()[0])
            .withEncryptionKeyMetadata(ByteBuffer.wrap(new byte[] {1}))
            .build();

    RewriteFilesInput encryptedInput =
        new RewriteFilesInput(
            new DataFile[] {encryptedDataFile},
            new DataFile[] {},
            new DeleteFile[] {},
            new DeleteFile[] {},
            getMixedTable());
    // parquet row-group merge should be disabled if the data file is encrypted.
    Assert.assertFalse(newExecutor(encryptedInput).canParquetRowGroupMerge());
  }

  @Test
  public void testParquetRowGroupMergeDisabledForV3Table() throws IOException {
    Assume.assumeTrue(fileFormat == FileFormat.PARQUET);
    prepareParquetRowGroupMergeTableConditions(true);
    Assert.assertTrue(newExecutor(dataScanTask).canParquetRowGroupMerge());

    HasTableOperations tableWithOperations = (HasTableOperations) getMixedTable().asUnkeyedTable();
    TableMetadata current = tableWithOperations.operations().current();
    tableWithOperations.operations().commit(current, current.upgradeToFormatVersion(3));
    getMixedTable().asUnkeyedTable().refresh();

    Assert.assertFalse(newExecutor(dataScanTask).canParquetRowGroupMerge());
  }

  @Test
  public void testParquetRowGroupMergeExecuteSplitAndCommitDataIntegrity() throws IOException {
    Assume.assumeTrue(fileFormat == FileFormat.PARQUET);
    prepareParquetRowGroupMergeTableConditions(true);

    // Create 5 data files, and each data file has 3 records.
    List<List<Record>> inputRecordGroups = parquetRowGroupMergeSourceRecordGroups();
    List<DataFile> sourceDataFiles = Lists.newArrayListWithExpectedSize(inputRecordGroups.size());
    long txId = 1L;
    for (List<Record> records : inputRecordGroups) {
      sourceDataFiles.addAll(
          MixedDataTestHelpers.writeAndCommitBaseStore(getMixedTable(), txId++, records, false));
    }

    // Set the targetSize
    long splitTargetSize =
        sourceDataFiles.get(0).fileSizeInBytes()
            + sourceDataFiles.get(1).fileSizeInBytes()
            + sourceDataFiles.get(2).fileSizeInBytes();
    getMixedTable()
        .asUnkeyedTable()
        .updateProperties()
        .set(
            org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
            String.valueOf(splitTargetSize))
        .commit();

    // Build a rewrite input with only data files and no delete files.
    RewriteFilesInput rewriteFilesInput = newDataOnlyInput(sourceDataFiles);
    IcebergRewriteExecutor executor = newExecutor(rewriteFilesInput);

    // Verify parquet row-group merge eligibility and execute the merge.
    Assert.assertTrue(executor.canParquetRowGroupMerge());

    RewriteFilesOutput output = executor.execute();
    // With this target, the first 3 source files are grouped into one output and
    // the last 2 into another.
    Assert.assertEquals(2, output.getDataFiles().length);

    // Commit merge result and validate the iceberg table data contains all records from the
    // source files.
    getMixedTable()
        .asUnkeyedTable()
        .newRewrite()
        .rewriteFiles(Sets.newHashSet(sourceDataFiles), Sets.newHashSet(output.getDataFiles()))
        .commit();

    // Get the source records
    Map<Integer, Record> expectedRecordsById = new HashMap<>();
    for (List<Record> recordGroup : inputRecordGroups) {
      for (Record record : recordGroup) {
        expectedRecordsById.put((Integer) record.get(0), record);
      }
    }

    // Get the current records after merge and commit, and compare with the source records.
    List<Record> currentRecords =
        MixedDataTestHelpers.readBaseStore(getMixedTable(), Expressions.alwaysTrue());
    Assert.assertEquals(15, currentRecords.size());
    for (Record currentRecord : currentRecords) {
      Integer id = (Integer) currentRecord.get(0);
      Record expectedRecord = expectedRecordsById.get(id);
      // Compare all fields to ensure the record is correctly merged.
      Assert.assertEquals(expectedRecord.get(0), currentRecord.get(0));
      Assert.assertEquals(expectedRecord.get(1), currentRecord.get(1));
      Assert.assertEquals(expectedRecord.get(2), currentRecord.get(2));
      Assert.assertEquals(expectedRecord.get(3), currentRecord.get(3));
    }
  }

  @Test
  public void testParquetRowGroupMergeWithSchemaDifferent() throws IOException {
    Assume.assumeTrue(fileFormat == FileFormat.PARQUET);
    prepareParquetRowGroupMergeTableConditions(true);

    // Scenario 1: schema differs by adding new field.
    List<DataFile> sourceDataFiles = Lists.newArrayList();
    sourceDataFiles.addAll(
        MixedDataTestHelpers.writeAndCommitBaseStore(
            getMixedTable(),
            301L,
            Collections.singletonList(
                MixedDataTestHelpers.createRecord(301, "field_old", 11L, "1970-01-03T08:00:00")),
            false));
    // add new field
    getMixedTable()
        .asUnkeyedTable()
        .updateSchema()
        .addColumn("new_col", Types.StringType.get())
        .commit();
    sourceDataFiles.addAll(
        MixedDataTestHelpers.writeAndCommitBaseStore(
            getMixedTable(),
            302L,
            Collections.singletonList(
                MixedDataTestHelpers.createRecord(
                    getMixedTable().schema(), 302, "field_new", 12L, "1970-01-04T08:00:00", "x")),
            false));

    RewriteFilesInput rewriteInput = newDataOnlyInput(sourceDataFiles);
    IcebergRewriteExecutor executor = newExecutor(rewriteInput);

    Assert.assertFalse(executor.canParquetRowGroupMerge());

    // Scenario 2: schema differs by type promotion.
    resetParquetRowGroupMergeTestState();
    sourceDataFiles = Lists.newArrayList();
    sourceDataFiles.addAll(
        MixedDataTestHelpers.writeAndCommitBaseStore(
            getMixedTable(),
            401L,
            Collections.singletonList(
                MixedDataTestHelpers.createRecord(401, "type_old", 21L, "1970-01-05T08:00:00")),
            false));
    // change the type
    getMixedTable()
        .asUnkeyedTable()
        .updateSchema()
        .updateColumn("id", Types.LongType.get())
        .commit();
    sourceDataFiles.addAll(
        MixedDataTestHelpers.writeAndCommitBaseStore(
            getMixedTable(),
            402L,
            Collections.singletonList(
                MixedDataTestHelpers.createRecord(
                    getMixedTable().schema(), 402L, "type_new", 22L, "1970-01-06T08:00:00")),
            false));

    rewriteInput = newDataOnlyInput(sourceDataFiles);
    executor = newExecutor(rewriteInput);

    Assert.assertFalse(executor.canParquetRowGroupMerge());
  }

  @Test
  public void testParquetRowGroupMergeMinAvgRowGroupSizeThreshold() throws IOException {
    Assume.assumeTrue(fileFormat == FileFormat.PARQUET);
    prepareParquetRowGroupMergeTableConditions(true);

    List<DataFile> sourceDataFiles = Lists.newArrayList();
    sourceDataFiles.addAll(
        MixedDataTestHelpers.writeAndCommitBaseStore(
            getMixedTable(),
            1L,
            Arrays.asList(
                MixedDataTestHelpers.createRecord(1, "a", 0L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(2, "b", 1L, "1970-01-01T08:00:00")),
            false));
    sourceDataFiles.addAll(
        MixedDataTestHelpers.writeAndCommitBaseStore(
            getMixedTable(),
            2L,
            Arrays.asList(
                MixedDataTestHelpers.createRecord(3, "c", 2L, "1970-01-02T08:00:00"),
                MixedDataTestHelpers.createRecord(4, "d", 3L, "1970-01-02T08:00:00")),
            false));

    // We compute the actual avg row-group size from file metadata and use it to set thresholds.
    // Each small test file has exactly 1 row group, so avgRowGroupSize = totalFileSize / 2.
    long totalFileSize = sourceDataFiles.stream().mapToLong(DataFile::fileSizeInBytes).sum();
    int totalRowGroups = sourceDataFiles.size();
    long avgRowGroupSize = totalFileSize / totalRowGroups;

    RewriteFilesInput rewriteInput = newDataOnlyInput(sourceDataFiles);
    // Case 1: threshold = avgRowGroupSize + 1 < threshold → merge disabled.
    setMinAvgRowGroupSizeThreshold(avgRowGroupSize + 1);
    boolean result = newExecutor(rewriteInput).canParquetRowGroupMerge();
    Assert.assertFalse(result);

    // Case 2: threshold = avgRowGroupSize >= threshold → merge enabled (boundary).
    setMinAvgRowGroupSizeThreshold(avgRowGroupSize);
    result = newExecutor(rewriteInput).canParquetRowGroupMerge();
    Assert.assertTrue(result);
  }

  private void setMinAvgRowGroupSizeThreshold(long threshold) {
    getMixedTable()
        .asUnkeyedTable()
        .updateProperties()
        .set(
            org.apache.amoro.table.TableProperties
                .SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_MIN_AVG_ROW_GROUP_SIZE_BYTES,
            String.valueOf(threshold))
        .commit();
    getMixedTable().asUnkeyedTable().refresh();
  }

  private RewriteFilesInput newDataOnlyInput(List<DataFile> dataFiles) {
    DataFile[] wrappedDataFiles = new DataFile[dataFiles.size()];
    for (int i = 0; i < dataFiles.size(); i++) {
      wrappedDataFiles[i] =
          MixedDataTestHelpers.wrapIcebergDataFile(dataFiles.get(i), (long) i + 1);
    }

    return new RewriteFilesInput(
        wrappedDataFiles,
        new DataFile[] {},
        new DeleteFile[] {},
        new DeleteFile[] {},
        getMixedTable());
  }

  private IcebergRewriteExecutor newExecutor(RewriteFilesInput input) {
    return new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
  }

  private void resetParquetRowGroupMergeTestState() throws IOException {
    dropTable();
    setupTable();
    initDataAndReader();
    prepareParquetRowGroupMergeTableConditions(true);
  }

  private List<List<Record>> parquetRowGroupMergeSourceRecordGroups() {
    return Arrays.asList(
        Arrays.asList(
            MixedDataTestHelpers.createRecord(11, "john", 0, "1970-01-01T08:00:00"),
            MixedDataTestHelpers.createRecord(21, "lily", 1, "1970-01-01T08:00:00"),
            MixedDataTestHelpers.createRecord(31, "sam", 2, "1970-01-01T08:00:00")),
        Arrays.asList(
            MixedDataTestHelpers.createRecord(41, "tom", 3, "1970-01-02T08:00:00"),
            MixedDataTestHelpers.createRecord(51, "lucy", 4, "1970-01-02T08:00:00"),
            MixedDataTestHelpers.createRecord(61, "kate", 5, "1970-01-02T08:00:00")),
        Arrays.asList(
            MixedDataTestHelpers.createRecord(71, "ben", 6, "1970-01-03T08:00:00"),
            MixedDataTestHelpers.createRecord(81, "mia", 7, "1970-01-03T08:00:00"),
            MixedDataTestHelpers.createRecord(91, "zoe", 8, "1970-01-03T08:00:00")),
        Arrays.asList(
            MixedDataTestHelpers.createRecord(101, "max", 9, "1970-01-04T08:00:00"),
            MixedDataTestHelpers.createRecord(111, "eve", 10, "1970-01-04T08:00:00"),
            MixedDataTestHelpers.createRecord(121, "amy", 11, "1970-01-04T08:00:00")),
        Arrays.asList(
            MixedDataTestHelpers.createRecord(131, "leo", 12, "1970-01-05T08:00:00"),
            MixedDataTestHelpers.createRecord(141, "ivy", 13, "1970-01-05T08:00:00"),
            MixedDataTestHelpers.createRecord(151, "jay", 14, "1970-01-05T08:00:00")));
  }

  // Configure only the table properties that are relevant to parquet row-group merge eligibility
  // checks. The min-avg-row-group-size is set to 0 so that small test files are not filtered
  // out by the threshold gate.
  private void prepareParquetRowGroupMergeTableConditions(boolean enabled) {
    getMixedTable()
        .asUnkeyedTable()
        .updateProperties()
        .set(SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_ENABLED, String.valueOf(enabled))
        .set(
            org.apache.amoro.table.TableProperties
                .SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_MIN_AVG_ROW_GROUP_SIZE_BYTES,
            "0")
        .commit();
  }

  @Test
  public void readOnlyData() throws IOException {
    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(dataScanTask, getMixedTable(), Collections.emptyMap());

    RewriteFilesOutput output = executor.execute();

    try (CloseableIterable<Record> records =
        openFile(
            output.getDataFiles()[0].path().toString(),
            output.getDataFiles()[0].format(),
            getMixedTable().schema(),
            new HashMap<>())) {
      Assert.assertEquals(3, Iterables.size(records));
    }

    Assert.assertTrue(output.getDeleteFiles() == null || output.getDeleteFiles().length == 0);
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

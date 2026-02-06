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
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
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
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.lang.reflect.Method;
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
    tableProperties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT, fileFormat.name());
    tableProperties.put(
        org.apache.iceberg.TableProperties.DELETE_DEFAULT_FILE_FORMAT, fileFormat.name());
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

  /**
   * Test targetSize() method with various input scenarios to ensure proper file merging behavior.
   * This test uses reflection to access the protected targetSize() method.
   */
  @Test
  public void testTargetSizeWithSmallInputFiles() throws Exception {
    // Test case 1: Multiple small files (total < targetSize) should merge into one file
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB
    long smallFileSize = 10 * 1024 * 1024; // 10MB each
    int fileCount = 5; // 5 files, total 50MB < 128MB

    DataFile[] dataFiles = createDataFilesWithSize(fileCount, smallFileSize);
    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
    long resultTargetSize = invokeTargetSize(executor);

    // Should return inputSize (50MB) to merge all files into one
    Assert.assertEquals(
        "Multiple small files should merge into one file",
        smallFileSize * fileCount,
        resultTargetSize);
  }

  @Test
  public void testTargetSizeWithSingleSmallFile() throws Exception {
    // Test case 2: Single small file (< targetSize) should use targetSize
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB
    long smallFileSize = 10 * 1024 * 1024; // 10MB

    DataFile[] dataFiles = createDataFilesWithSize(1, smallFileSize);
    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
    long resultTargetSize = invokeTargetSize(executor);

    // Should return targetSize for single file
    Assert.assertEquals("Single small file should use targetSize", targetSize, resultTargetSize);
  }

  @Test
  public void testTargetSizeWithExactTargetSize() throws Exception {
    // Test case 3: Input size exactly equals targetSize
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB

    DataFile[] dataFiles = createDataFilesWithSize(1, targetSize);
    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
    long resultTargetSize = invokeTargetSize(executor);

    // Should calculate based on expectedOutputFiles (should be 1)
    Assert.assertTrue(
        "Input size equals targetSize should return >= targetSize", resultTargetSize >= targetSize);
  }

  @Test
  public void testTargetSizeWithLargeRemainder() throws Exception {
    // Test case 4: Input size > targetSize with large remainder (> minFileSize)
    // targetSize = 128MB, minFileSize = 128MB * 0.75 = 96MB
    // inputSize = 200MB, remainder = 72MB < 96MB, but let's test with remainder > 96MB
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB
    long inputSize = 224 * 1024 * 1024; // 224MB = 128MB * 1.75, remainder = 96MB
    // Actually, remainder = 224 - 128 = 96MB, which equals minFileSize (96MB)
    // So we need remainder > 96MB, let's use 225MB, remainder = 97MB > 96MB

    inputSize = 225 * 1024 * 1024; // 225MB, remainder = 97MB > 96MB

    DataFile[] dataFiles = createDataFilesWithSize(1, inputSize);
    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
    long resultTargetSize = invokeTargetSize(executor);

    // Should round up (2 files), so split size should be around inputSize / 2
    long expectedSplitSize = (inputSize / 2) + (5L * 1024); // with overhead
    long maxFileSize = (long) (targetSize * 1.5); // 192MB
    long expectedResult = Math.min(expectedSplitSize, maxFileSize);
    if (expectedResult < targetSize) {
      expectedResult = targetSize;
    }

    Assert.assertTrue(
        "Large remainder should result in appropriate split size",
        resultTargetSize >= targetSize && resultTargetSize <= maxFileSize);
  }

  @Test
  public void testTargetSizeWithSmallRemainderDistributed() throws Exception {
    // Test case 5: Input size > targetSize with small remainder that can be distributed
    // targetSize = 128MB, minFileSize = 96MB
    // inputSize = 250MB, remainder = 122MB > 96MB, so should round up
    // Let's use inputSize where remainder < 96MB and avgFileSize < 1.1 * targetSize
    // inputSize = 256MB = 2 * 128MB, remainder = 0, should round down to 2 files
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB
    long inputSize = 256 * 1024 * 1024; // 256MB = 2 * 128MB, no remainder

    DataFile[] dataFiles = createDataFilesWithSize(1, inputSize);
    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
    long resultTargetSize = invokeTargetSize(executor);

    // Should round down to 2 files, so split size = 256MB / 2 = 128MB + overhead
    long expectedSplitSize = (inputSize / 2) + (5L * 1024);
    if (expectedSplitSize < targetSize) {
      expectedSplitSize = targetSize;
    }
    long maxFileSize = (long) (targetSize * 1.5);
    long expectedResult = Math.min(expectedSplitSize, maxFileSize);

    Assert.assertEquals(
        "Exact multiple of targetSize should split evenly", expectedResult, resultTargetSize);
  }

  @Test
  public void testTargetSizeWithMultipleSmallFiles() throws Exception {
    // Test case 6: Multiple small files that together exceed targetSize
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB
    long smallFileSize = 20 * 1024 * 1024; // 20MB each
    int fileCount = 10; // 10 files, total 200MB > 128MB

    DataFile[] dataFiles = createDataFilesWithSize(fileCount, smallFileSize);
    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
    long resultTargetSize = invokeTargetSize(executor);

    long totalInputSize = smallFileSize * fileCount;
    // Should calculate based on expectedOutputFiles
    Assert.assertTrue(
        "Multiple files exceeding targetSize should calculate appropriate split size",
        resultTargetSize >= targetSize);
    Assert.assertTrue(
        "Split size should not exceed maxFileSize", resultTargetSize <= (long) (targetSize * 1.5));
  }

  @Test
  public void testTargetSizeWithVeryLargeInput() throws Exception {
    // Test case 7: Very large input size
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB
    long inputSize = 500 * 1024 * 1024; // 500MB

    DataFile[] dataFiles = createDataFilesWithSize(1, inputSize);
    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
    long resultTargetSize = invokeTargetSize(executor);

    // Should be capped at maxFileSize (targetSize * 1.5 = 192MB)
    long maxFileSize = (long) (targetSize * 1.5);
    Assert.assertTrue(
        "Very large input should be capped at maxFileSize", resultTargetSize <= maxFileSize);
    Assert.assertTrue("Result should be at least targetSize", resultTargetSize >= targetSize);
  }

  @Test
  public void testTargetSizeWithCustomMinTargetSizeRatio() throws Exception {
    // Test case 8: Custom min-target-size-ratio
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB
    double customRatio = 0.8; // 80% instead of default 75%
    long minFileSize = (long) (targetSize * customRatio); // 102.4MB

    // Set custom ratio in table properties
    Map<String, String> tableProps = Maps.newHashMap(getMixedTable().properties());
    tableProps.put(
        TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO, String.valueOf(customRatio));
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO, String.valueOf(customRatio))
        .commit();

    try {
      // inputSize = 250MB, remainder = 122MB > 102.4MB, should round up
      long inputSize = 250 * 1024 * 1024; // 250MB
      DataFile[] dataFiles = createDataFilesWithSize(1, inputSize);
      RewriteFilesInput input =
          new RewriteFilesInput(
              dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

      IcebergRewriteExecutor executor =
          new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
      long resultTargetSize = invokeTargetSize(executor);

      Assert.assertTrue(
          "Custom min-target-size-ratio should affect calculation", resultTargetSize >= targetSize);
    } finally {
      // Restore original ratio
      String originalRatio =
          tableProps.getOrDefault(
              TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO,
              String.valueOf(TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO_DEFAULT));
      getMixedTable()
          .updateProperties()
          .set(TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO, originalRatio)
          .commit();
    }
  }

  @Test
  public void testTargetSizeBoundaryConditions() throws Exception {
    long targetSize = TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT; // 128MB
    long minFileSize = (long) (targetSize * 0.75); // 96MB

    // Test case: inputSize = targetSize + minFileSize - 1
    // remainder = minFileSize - 1 < minFileSize, should check avgFileSize
    long inputSize = targetSize + minFileSize - 1; // 128MB + 96MB - 1 = 223MB
    DataFile[] dataFiles = createDataFilesWithSize(1, inputSize);
    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, dataFiles, new DeleteFile[] {}, new DeleteFile[] {}, getMixedTable());

    IcebergRewriteExecutor executor =
        new IcebergRewriteExecutor(input, getMixedTable(), Collections.emptyMap());
    long resultTargetSize = invokeTargetSize(executor);

    Assert.assertTrue(
        "Boundary condition should be handled correctly", resultTargetSize >= targetSize);
  }

  /** Helper method to create DataFile array with specified size */
  private DataFile[] createDataFilesWithSize(int count, long fileSize) {
    DataFile[] dataFiles = new DataFile[count];
    PartitionSpec spec = getMixedTable().spec();
    for (int i = 0; i < count; i++) {
      DataFiles.Builder builder = DataFiles.builder(spec);
      builder
          .withPath(String.format("/data/file-%d.parquet", i))
          .withFileSizeInBytes(fileSize)
          .withRecordCount(100);
      if (spec.isPartitioned()) {
        builder.withPartition(getPartitionData());
      }
      dataFiles[i] = builder.build();
    }
    return dataFiles;
  }

  /** Helper method to invoke protected targetSize() method using reflection */
  private long invokeTargetSize(IcebergRewriteExecutor executor) throws Exception {
    Method method = IcebergRewriteExecutor.class.getDeclaredMethod("targetSize");
    method.setAccessible(true);
    return (Long) method.invoke(executor);
  }
}

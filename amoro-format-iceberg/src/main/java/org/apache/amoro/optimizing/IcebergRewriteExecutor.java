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
import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_ENABLED_DEFAULT;
import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_MIN_AVG_ROW_GROUP_SIZE_BYTES;
import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_MIN_AVG_ROW_GROUP_SIZE_DEFAULT;

import org.apache.amoro.io.reader.GenericCombinedIcebergDataReader;
import org.apache.amoro.io.writer.GenericIcebergPartitionedFanoutWriter;
import org.apache.amoro.io.writer.IcebergFanoutPosDeleteWriter;
import org.apache.amoro.table.MixedTable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.MetricsConfig;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.DeleteWriteResult;
import org.apache.iceberg.io.FileWriter;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.UnpartitionedWriter;
import org.apache.iceberg.parquet.ParquetIOBridge;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.schema.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** OptimizingExecutor for iceberg format. */
public class IcebergRewriteExecutor extends AbstractRewriteFilesExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(IcebergRewriteExecutor.class);
  private static final String PARQUET_BLOOM_FILTER_ENABLED_PREFIX =
      "write.parquet.bloom-filter-enabled";
  private FileSchemaContext fileSchemaContext;

  private static class FileSchemaContext {
    private final MessageType parquetSchema;
    private final Map<String, String> keyValueMetadata;
    private final List<InputFile> inputFiles;
    private final List<Long> fileSizes;
    private final long totalRowGroupCount;

    private FileSchemaContext(
        MessageType parquetSchema,
        Map<String, String> keyValueMetadata,
        List<InputFile> inputFiles,
        List<Long> fileSizes,
        long totalRowGroupCount) {
      this.parquetSchema = parquetSchema;
      this.keyValueMetadata = keyValueMetadata;
      this.inputFiles = inputFiles;
      this.fileSizes = fileSizes;
      this.totalRowGroupCount = totalRowGroupCount;
    }
  }

  public IcebergRewriteExecutor(
      RewriteFilesInput input, MixedTable table, Map<String, String> properties) {
    super(input, table, properties);
  }

  @Override
  protected OptimizingDataReader dataReader() {
    String processId = TaskProperties.getProcessId(properties);
    return new GenericCombinedIcebergDataReader(
        io,
        table.schema(),
        table.spec(),
        table.asUnkeyedTable().encryption(),
        table.properties().get(TableProperties.DEFAULT_NAME_MAPPING),
        false,
        IdentityPartitionConverters::convertConstant,
        false,
        structLikeCollections,
        input,
        processId);
  }

  @Override
  protected FileWriter<PositionDelete<Record>, DeleteWriteResult> posWriter() {
    return new IcebergFanoutPosDeleteWriter<>(
        fullMetricAppenderFactory(fileSpec()),
        deleteFileFormat(),
        partition(),
        table.io(),
        table.asUnkeyedTable().encryption(),
        UUID.randomUUID().toString());
  }

  @Override
  protected TaskWriter<Record> dataWriter() {
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table.asUnkeyedTable(), table.spec().specId(), 0).build();

    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(table.schema(), table.spec());
    appenderFactory.setAll(table.properties());

    if (table.spec().isUnpartitioned()) {
      return new UnpartitionedWriter<>(
          table.spec(), dataFileFormat(), appenderFactory, outputFileFactory, io, targetSize());
    } else {
      return new GenericIcebergPartitionedFanoutWriter(
          table.schema(),
          table.spec(),
          dataFileFormat(),
          appenderFactory,
          outputFileFactory,
          io,
          targetSize());
    }
  }

  @Override
  protected long targetSize() {
    long targetSize = super.targetSize();
    long inputSize =
        Arrays.stream(input.rewrittenDataFiles()).mapToLong(DataFile::fileSizeInBytes).sum();
    // When the input files’ total size is below targetSize, remove the output file size limit to
    // avoid outputting multiple files.
    // For more details, please refer to: https://github.com/apache/amoro/issues/3645
    return inputSize < targetSize ? Long.MAX_VALUE : targetSize;
  }

  private PartitionSpec fileSpec() {
    return table.asUnkeyedTable().specs().get(input.allFiles()[0].specId());
  }

  @Override
  protected List<DataFile> rewriterDataFiles() throws Exception {
    if (!canParquetRowGroupMerge()) {
      return super.rewriterDataFiles();
    }

    try {
      LOG.debug(
          "Starting parquet row-group merge for {} files.", input.rewrittenDataFiles().length);
      List<DataFile> dataFiles = parquetRowGroupMergeFiles();
      LOG.debug("Parquet row-group merge completed successfully");

      return dataFiles;
    } catch (Exception e) {
      LOG.warn("Parquet row-group merge failed, falling back to row-based rewrite", e);
      return super.rewriterDataFiles();
    }
  }

  protected boolean canParquetRowGroupMerge() {
    fileSchemaContext = null;
    return isParquetRowGroupMergeEnabled()
        && isTableVersionAllowed()
        && isTableUnsorted()
        && isParquetFormat()
        && hasNoEncryptedDataFiles()
        && hasNoReadOnlyDeleteFiles()
        && hasNoRewrittenDeleteFiles()
        && hasNoBloomFilter()
        && allFilesHaveCurrentSpecId()
        && canPrepareParquetSchemaContext();
  }

  private boolean isTableVersionAllowed() {
    if (!(table.asUnkeyedTable() instanceof HasTableOperations)) {
      return checkCondition(false, "table operations are unavailable");
    }

    HasTableOperations tableWithOperations = (HasTableOperations) table.asUnkeyedTable();
    int formatVersion = tableWithOperations.operations().current().formatVersion();

    // Keep parquet row-group merge scoped to V2 tables for now. Iceberg V3 row-lineage
    // semantics (_row_id, _last_updated_sequence_number) are still unsettled here,
    // so V3 tables fall back to row-based rewrite.
    return checkCondition(formatVersion < 3, "table format version is " + formatVersion);
  }

  private boolean isTableUnsorted() {
    return checkCondition(!table.asUnkeyedTable().sortOrder().isSorted(), "table has sort order");
  }

  private boolean hasNoEncryptedDataFiles() {
    for (DataFile file : input.rewrittenDataFiles()) {
      if (!checkCondition(
          file.keyMetadata() == null, "input file is encrypted: " + file.location())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check whether all input Parquet file schemas are consistent; if so, build and return
   * FileSchemaContext, otherwise return null.
   */
  private FileSchemaContext checkSchemaAndBuildContext() {
    MessageType firstSchema = null;
    Map<String, String> firstMetadata = null;
    List<InputFile> inputFiles = new ArrayList<>(input.rewrittenDataFiles().length);
    List<Long> fileSizes = new ArrayList<>(input.rewrittenDataFiles().length);
    long totalRowGroupCount = 0;

    for (DataFile file : input.rewrittenDataFiles()) {
      InputFile inputFile = io.newInputFile(file.location(), file.fileSizeInBytes());
      inputFiles.add(inputFile);
      fileSizes.add(file.fileSizeInBytes());

      // TODO: remove ParquetIOBridge once Iceberg exposes a public ParquetIO.file() helper.
      try (ParquetFileReader reader = ParquetFileReader.open(ParquetIOBridge.file(inputFile))) {
        totalRowGroupCount += reader.getRowGroups().size();
        // Get parquet schema from file metadata.
        MessageType currentSchema = reader.getFileMetaData().getSchema();
        if (firstSchema == null) {
          firstSchema = currentSchema;
          firstMetadata = new HashMap<>(reader.getFileMetaData().getKeyValueMetaData());
          continue;
        }

        if (!firstSchema.equals(currentSchema)) {
          throw new IllegalStateException(
              "The input parquet files have inconsistent schemas and cannot be merged.");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Failed to read parquet file: " + file.location(), e);
      }
    }

    if (firstSchema == null) {
      throw new IllegalStateException(
          "No valid input parquet files are available for parquet row-group merge.");
    }

    return new FileSchemaContext(
        firstSchema, firstMetadata, inputFiles, fileSizes, totalRowGroupCount);
  }

  protected List<DataFile> parquetRowGroupMergeFiles() throws Exception {
    List<DataFile> outputFiles = new ArrayList<>();
    OutputFileFactory outputFileFactory = newRowGroupMergeOutputFileFactory();
    if (fileSchemaContext == null) {
      throw new IllegalStateException(
          "Parquet row-group merge context is not prepared. Call canParquetRowGroupMerge() first.");
    }

    long maxOutputSize = targetSize();
    long currentOutputSize = 0L;
    ParquetFileMergeRunner parquetFileMergeRunner = null;

    try {
      for (int i = 0; i < fileSchemaContext.inputFiles.size(); i++) {
        InputFile inputFile = fileSchemaContext.inputFiles.get(i);
        long fileSize = fileSchemaContext.fileSizes.get(i);
        // Roll to a new output file when appending the next input would exceed target size.
        if (currentOutputSize > 0 && currentOutputSize + fileSize > maxOutputSize) {
          outputFiles.add(parquetFileMergeRunner.result());
          parquetFileMergeRunner = null;
        }
        if (parquetFileMergeRunner == null) {
          parquetFileMergeRunner =
              new ParquetFileMergeRunner(
                  io,
                  fileSchemaContext.parquetSchema,
                  fileSchemaContext.keyValueMetadata,
                  table.spec(),
                  partition(),
                  parquetRowGroupSize(),
                  metricsConfig());
          EncryptedOutputFile outputFile = newRowGroupMergeOutputFile(outputFileFactory);
          parquetFileMergeRunner.start(outputFile);
          currentOutputSize = 0L;
        }

        parquetFileMergeRunner.appendFile(inputFile);
        currentOutputSize += fileSize;
      }

      if (parquetFileMergeRunner != null) {
        outputFiles.add(parquetFileMergeRunner.result());
        parquetFileMergeRunner = null;
      }

      return outputFiles;
    } catch (Exception e) {
      // Clean up already finalized rollover outputs to avoid leaving orphan files before fallback.
      cleanupMergedOutputFiles(outputFiles);
      throw e;
    } finally {
      fileSchemaContext = null;
      if (parquetFileMergeRunner != null) {
        parquetFileMergeRunner.close();
      }
    }
  }

  private boolean canPrepareParquetSchemaContext() {
    try {
      fileSchemaContext = checkSchemaAndBuildContext();
      if (fileSchemaContext.totalRowGroupCount > 0) {
        long totalInputSize = fileSchemaContext.fileSizes.stream().mapToLong(Long::longValue).sum();
        long avgRowGroupSize = totalInputSize / fileSchemaContext.totalRowGroupCount;
        long minAvgRowGroupSize =
            PropertyUtil.propertyAsLong(
                table.properties(),
                SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_MIN_AVG_ROW_GROUP_SIZE_BYTES,
                SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_MIN_AVG_ROW_GROUP_SIZE_DEFAULT);

        if (avgRowGroupSize < minAvgRowGroupSize) {
          LOG.debug(
              "Skip parquet row-group merge: avg row-group size {} bytes ({} input files, {} row groups) "
                  + "is below threshold {} bytes ({}), falling back to row-level rewrite",
              avgRowGroupSize,
              fileSchemaContext.inputFiles.size(),
              fileSchemaContext.totalRowGroupCount,
              minAvgRowGroupSize,
              SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_MIN_AVG_ROW_GROUP_SIZE_BYTES);
          return false;
        }
      }
      return true;
    } catch (IllegalStateException e) {
      return checkCondition(false, e.getMessage());
    }
  }

  private void cleanupMergedOutputFiles(List<DataFile> outputFiles) {
    for (DataFile outputFile : outputFiles) {
      try {
        io.deleteFile(outputFile.location());
      } catch (RuntimeException e) {
        LOG.warn(
            "Failed to delete merged parquet output during cleanup: {}", outputFile.location(), e);
      }
    }
  }

  private boolean isParquetRowGroupMergeEnabled() {
    return checkCondition(
        PropertyUtil.propertyAsBoolean(
            table.properties(),
            SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_ENABLED,
            SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_ENABLED_DEFAULT),
        SELF_OPTIMIZING_REWRITE_USE_PARQUET_ROW_GROUP_MERGE_ENABLED + " is not enabled");
  }

  /**
   * check if all files are in parquet format, and the table default file format is parquet. Only
   * when both conditions are satisfied, we can do parquet row-group merge.
   *
   * @return result of the check
   */
  private boolean isParquetFormat() {
    // 1、table default file format is parquet
    if (!checkCondition(
        dataFileFormat() == FileFormat.PARQUET, "table default file format is not PARQUET")) {
      return false;
    }

    // 2、all files are in parquet format
    for (DataFile file : input.rewrittenDataFiles()) {
      if (!checkCondition(
          file.format() == FileFormat.PARQUET,
          "input file format is not PARQUET: " + file.format())) {
        return false;
      }
    }

    return true;
  }

  private boolean hasNoReadOnlyDeleteFiles() {
    return checkCondition(
        ArrayUtils.isEmpty(input.readOnlyDeleteFiles()), "has read-only delete files");
  }

  private boolean hasNoRewrittenDeleteFiles() {
    return checkCondition(
        ArrayUtils.isEmpty(input.rewrittenDeleteFiles()), "has rewritten delete files");
  }

  private boolean hasNoBloomFilter() {
    // Check if bloom filter is enabled by looking for properties
    // with the prefix 'write.parquet.bloom-filter-enabled' and value 'true'
    // such as 'write.parquet.bloom-filter-enabled.default' or
    // 'write.parquet.bloom-filter-enabled.column.'.
    for (Map.Entry<String, String> entry : table.properties().entrySet()) {
      if (entry.getKey().startsWith(PARQUET_BLOOM_FILTER_ENABLED_PREFIX)
          && Boolean.parseBoolean(entry.getValue())) {
        return checkCondition(false, "table has bloom filter");
      }
    }

    return true;
  }

  private long parquetRowGroupSize() {
    return PropertyUtil.propertyAsLong(
        table.properties(),
        TableProperties.PARQUET_ROW_GROUP_SIZE_BYTES,
        TableProperties.PARQUET_ROW_GROUP_SIZE_BYTES_DEFAULT);
  }

  private boolean allFilesHaveCurrentSpecId() {
    int currentSpecId = table.spec().specId();
    for (DataFile file : input.rewrittenDataFiles()) {
      if (!checkCondition(
          file.specId() == currentSpecId,
          "file spec id does not match current spec id: file ="
              + file.specId()
              + ", current ="
              + currentSpecId)) {
        return false;
      }
    }
    return true;
  }

  private boolean checkCondition(boolean condition, String message) {
    if (!condition) {
      LOG.debug("Skip parquet row-group merge due to {}", message);
    }
    return condition;
  }

  private MetricsConfig metricsConfig() {
    return table.isKeyedTable()
        ? MetricsConfig.forTable(table.asKeyedTable().baseTable())
        : MetricsConfig.forTable(table.asUnkeyedTable());
  }

  private OutputFileFactory newRowGroupMergeOutputFileFactory() {
    return OutputFileFactory.builderFor(table.asUnkeyedTable(), table.spec().specId(), 0)
        .format(FileFormat.PARQUET)
        .ioSupplier(() -> io)
        .build();
  }

  private EncryptedOutputFile newRowGroupMergeOutputFile(OutputFileFactory outputFileFactory) {
    return table.spec().isUnpartitioned()
        ? outputFileFactory.newOutputFile()
        : outputFileFactory.newOutputFile(table.spec(), partition());
  }
}

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

import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.MetricsConfig;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.parquet.ParquetIOBridge;
import org.apache.iceberg.parquet.ParquetUtil;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This component merges one or more parquet files into a single output parquet file without
 * row-level decode/encode, then builds an Iceberg {@link org.apache.iceberg.DataFile} for commit.
 *
 * <p>Lifecycle:
 *
 * <ol>
 *   <li>{@link #start(org.apache.iceberg.encryption.EncryptedOutputFile)} to initialize writer
 *   <li>{@link #appendFile(org.apache.iceberg.io.InputFile)} for each source file
 *   <li>{@link #result()} to finalize and build output {@link org.apache.iceberg.DataFile}
 *   <li>{@link #close()} for safe cleanup (also supports failure cleanup)
 * </ol>
 *
 * <p>Failure behavior:
 *
 * <ul>
 *   <li>If writer is not successful, {@link #close()} attempts to delete output file
 *   <li>If writer close fails, the exception is propagated
 * </ul>
 */
public final class ParquetFileMergeRunner implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(ParquetFileMergeRunner.class);

  private final MessageType parquetSchema;
  private final Map<String, String> keyValueMetadata;
  private final PartitionSpec spec;
  private final StructLike partition;
  private final MetricsConfig metricsConfig;
  private final FileIO io;

  private ParquetFileWriter writer;
  private EncryptedOutputFile outputFile;
  private boolean success;
  private final long rowGroupSize;

  public ParquetFileMergeRunner(
      FileIO io,
      MessageType parquetSchema,
      Map<String, String> keyValueMetadata,
      PartitionSpec spec,
      StructLike partition,
      long rowGroupSize,
      MetricsConfig metricsConfig) {
    this.io = io;
    this.parquetSchema = parquetSchema;
    this.keyValueMetadata =
        keyValueMetadata == null ? new HashMap<>() : new HashMap<>(keyValueMetadata);
    this.spec = spec;
    this.partition = partition;
    this.rowGroupSize = rowGroupSize;
    this.metricsConfig = metricsConfig;
  }

  /**
   * Initializes the ParquetFileWriter for row-group merge.
   *
   * @param output encrypted output file destination
   * @throws java.io.IOException if writer creation or writer start fails
   */
  public void start(EncryptedOutputFile output) throws IOException {
    this.outputFile = output;
    writer =
        new ParquetFileWriter(
            ParquetIOBridge.file(output.encryptingOutputFile()),
            parquetSchema,
            ParquetFileWriter.Mode.CREATE,
            rowGroupSize,
            ParquetWriter.MAX_PADDING_SIZE_DEFAULT,
            null,
            ParquetProperties.builder().build());
    writer.start();
  }

  public void appendFile(InputFile inputFile) throws IOException {
    if (writer == null) {
      throw new IllegalStateException("ParquetFileWriter not initialized. Call start() first.");
    }

    org.apache.parquet.io.InputFile file = ParquetIOBridge.file(inputFile);
    writer.appendFile(file);
  }

  public DataFile result() throws IOException {
    if (writer == null) {
      throw new IllegalStateException("ParquetFileWriter not initialized. Call start() first.");
    }
    if (success) {
      throw new IllegalStateException("result() already called.");
    }

    writer.end(keyValueMetadata);
    ParquetMetadata footer = writer.getFooter();
    writer.close();
    writer = null;

    Metrics metrics = ParquetUtil.footerMetrics(footer, Stream.empty(), metricsConfig);
    List<Long> splitOffsets =
        footer.getBlocks().stream().map(BlockMetaData::getStartingPos).collect(Collectors.toList());
    long recordCount = footer.getBlocks().stream().mapToLong(BlockMetaData::getRowCount).sum();
    long fileSizeInBytes =
        io.newInputFile(outputFile.encryptingOutputFile().location()).getLength();

    DataFile result =
        DataFiles.builder(spec)
            .withEncryptedOutputFile(outputFile)
            .withFormat(FileFormat.PARQUET)
            .withFileSizeInBytes(fileSizeInBytes)
            .withMetrics(metrics)
            .withPartition(partition)
            .withSplitOffsets(splitOffsets)
            .withRecordCount(recordCount)
            .build();
    success = true;
    return result;
  }

  /**
   * Closes writer and performs failure cleanup.
   *
   * <p>If writer did not succeed, this method attempts to delete the output file to avoid leaving
   * partial artifacts. Cleanup deletion failures are logged but not thrown.
   *
   * @throws java.io.IOException when closing writer fails
   */
  @Override
  public void close() throws IOException {
    IOException closeFailure = null;
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        closeFailure = e;
      } finally {
        writer = null;
      }
    }

    // Clean up output file if writer did not succeed.
    if (!success && outputFile != null) {
      String location = outputFile.encryptingOutputFile().location();
      try {
        io.deleteFile(location);
      } catch (RuntimeException e) {
        LOG.warn("Failed to delete output file during cleanup: {}", location, e);
      }
    }

    if (closeFailure != null) {
      throw closeFailure;
    }
  }
}

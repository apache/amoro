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

import org.apache.amoro.io.reader.GenericCombinedIcebergDataReader;
import org.apache.amoro.io.writer.GenericIcebergPartitionedFanoutWriter;
import org.apache.amoro.io.writer.IcebergFanoutPosDeleteWriter;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.io.DeleteWriteResult;
import org.apache.iceberg.io.FileWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.UnpartitionedWriter;
import org.apache.iceberg.relocated.com.google.common.math.LongMath;
import org.apache.iceberg.util.PropertyUtil;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/** OptimizingExecutor for iceberg format. */
public class IcebergRewriteExecutor extends AbstractRewriteFilesExecutor {
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
        table.properties().get(org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING),
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
    int inputFileCount = input.rewrittenDataFiles().length;

    // If input size is less than target size, merge all files into one output file
    if (inputSize < targetSize) {
      // When there are multiple input files, we should merge them into one file
      // to achieve the goal of small file consolidation
      if (inputFileCount > 1) {
        return inputSize;
      }
      // For single file case, use targetSize to avoid creating unnecessarily large file
      return targetSize;
    }

    // Calculate expected number of output files based on input size
    // This logic is inspired by Spark/Iceberg's SizeBasedFileRewritePlanner
    int expectedOutputFiles = expectedOutputFiles(inputSize, targetSize);

    // Calculate the split size: inputSize / expectedOutputFiles
    // Add a small overhead to account for compression and serialization variations
    long splitOverhead = 5L * 1024; // 5KB overhead
    long estimatedSplitSize = (inputSize / expectedOutputFiles) + splitOverhead;

    // Ensure the split size is at least targetSize to avoid creating too many small files
    if (estimatedSplitSize < targetSize) {
      return targetSize;
    }

    // Cap the split size at a reasonable maximum (targetSize * 1.5) to avoid creating
    // excessively large files due to compression variations
    long maxFileSize = (long) (targetSize * 1.5);
    return Math.min(estimatedSplitSize, maxFileSize);
  }

  /**
   * Determines the preferable number of output files when rewriting a particular file group.
   *
   * <p>This method decides whether to round up or round down based on what the estimated average
   * file size will be if the remainder is distributed amongst other files. If the new average file
   * size is no more than 10% greater than the target file size, then this method will round down
   * when determining the number of output files. Otherwise, the remainder will be written into a
   * separate file.
   *
   * <p>This logic is inspired by Spark/Iceberg's SizeBasedFileRewritePlanner.expectedOutputFiles
   *
   * @param inputSize total input size for a file group
   * @param targetSize target file size
   * @return the number of files this rewriter should create
   */
  private int expectedOutputFiles(long inputSize, long targetSize) {
    if (inputSize < targetSize) {
      return 1;
    }

    // Get min file size ratio (default 0.75) to determine if remainder is large enough
    double minFileSizeRatio =
        PropertyUtil.propertyAsDouble(
            table.properties(),
            TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO,
            TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO_DEFAULT);
    long minFileSize = (long) (targetSize * minFileSizeRatio);

    long numFilesWithRemainder = LongMath.divide(inputSize, targetSize, RoundingMode.CEILING);
    long numFilesWithoutRemainder = LongMath.divide(inputSize, targetSize, RoundingMode.FLOOR);
    long remainder = LongMath.mod(inputSize, targetSize);
    long avgFileSizeWithoutRemainder = inputSize / numFilesWithoutRemainder;

    if (remainder > minFileSize) {
      // The remainder file is of a valid size for this rewrite so keep it
      return (int) numFilesWithRemainder;
    } else if (avgFileSizeWithoutRemainder < (long) (1.1 * targetSize)) {
      // If the remainder is distributed amongst other files,
      // the average file size will be no more than 10% bigger than the target file size
      // so round down and distribute remainder amongst other files
      return (int) numFilesWithoutRemainder;
    } else {
      // Keep the remainder file as it is not OK to distribute it amongst other files
      return (int) numFilesWithRemainder;
    }
  }

  private PartitionSpec fileSpec() {
    return table.asUnkeyedTable().specs().get(input.allFiles()[0].specId());
  }
}

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

package org.apache.amoro.formats.hudi;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.hudi.common.model.FileSlice;
import org.apache.hudi.common.model.HoodieLogFile;
import org.apache.hudi.common.table.view.SyncableFileSystemView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/** Common utils for hudi table. */
public class HudiTableUtil {

  public static String convertAvroSchemaToFieldType(Schema schema) {
    switch (schema.getType()) {
      case ENUM:
      case STRING:
        // convert Avro's Utf8/CharSequence to String
        return "STRING";
      case ARRAY:
        return "ARRAY<" + convertAvroSchemaToFieldType(schema.getElementType()) + ">";
      case MAP:
        return "MAP<STRING, " + convertAvroSchemaToFieldType(schema.getValueType()) + ">";
      case UNION:
        final Schema actualSchema;
        if (schema.getTypes().size() == 2
            && schema.getTypes().get(0).getType() == Schema.Type.NULL) {
          actualSchema = schema.getTypes().get(1);
        } else if (schema.getTypes().size() == 2
            && schema.getTypes().get(1).getType() == Schema.Type.NULL) {
          actualSchema = schema.getTypes().get(0);
        } else if (schema.getTypes().size() == 1) {
          actualSchema = schema.getTypes().get(0);
        } else {
          return "";
        }

        return convertAvroSchemaToFieldType(actualSchema);
      case FIXED:
        // logical decimal type
        if (schema.getLogicalType() instanceof LogicalTypes.Decimal) {
          final LogicalTypes.Decimal decimalType = (LogicalTypes.Decimal) schema.getLogicalType();
          return "DECIMAL(" + decimalType.getPrecision() + ", " + decimalType.getScale() + ")";
        }
        // convert fixed size binary data to primitive byte arrays
        return "VARBINARY(" + schema.getFixedSize() + ")";
      case BYTES:
        // logical decimal type
        if (schema.getLogicalType() instanceof LogicalTypes.Decimal) {
          final LogicalTypes.Decimal decimalType = (LogicalTypes.Decimal) schema.getLogicalType();
          return "DECIMAL(" + decimalType.getPrecision() + ", " + decimalType + ")";
        }
        return "BYTES";
      case INT:
        // logical date and time type
        final org.apache.avro.LogicalType logicalType = schema.getLogicalType();
        if (logicalType == LogicalTypes.date()) {
          return "DATE";
        } else if (logicalType == LogicalTypes.timeMillis()) {
          return "TIME";
        }
        return "INT";
      case LONG:
        // logical timestamp type
        if (schema.getLogicalType() == LogicalTypes.timestampMillis()) {
          return "TIMESTAMP";
        } else if (schema.getLogicalType() == LogicalTypes.localTimestampMillis()) {
          return "TIMESTAMP_WITH_LOCAL_TIME_ZONE";
        } else if (schema.getLogicalType() == LogicalTypes.timestampMicros()) {
          return "TIMESTAMP";
        } else if (schema.getLogicalType() == LogicalTypes.localTimestampMicros()) {
          return "TIMESTAMP_WITH_LOCAL_TIME_ZONE";
        } else if (schema.getLogicalType() == LogicalTypes.timeMillis()) {
          return "TIME";
        } else if (schema.getLogicalType() == LogicalTypes.timeMicros()) {
          return "TIME";
        }
        return "BIGINT";
      case FLOAT:
        return "FLOAT";
      case DOUBLE:
        return "DOUBLE";
      case BOOLEAN:
        return "BOOLEAN";
      case NULL:
        return "NULL";
      default:
        throw new IllegalArgumentException("Unsupported Avro type '" + schema.getType() + "'.");
    }
  }

  public static class HoodiePartitionMetric {
    private long totalBaseFileSizeInBytes;
    private long totalLogFileSizeInBytes;
    private long baseFileCount;
    private long logFileCount;

    public HoodiePartitionMetric(
        int baseFileCount,
        int logFileCount,
        long totalBaseFileSizeInBytes,
        long totalLogFileSizeInBytes) {
      this.baseFileCount = baseFileCount;
      this.logFileCount = logFileCount;
      this.totalBaseFileSizeInBytes = totalBaseFileSizeInBytes;
      this.totalLogFileSizeInBytes = totalLogFileSizeInBytes;
    }

    public long getTotalBaseFileSizeInBytes() {
      return totalBaseFileSizeInBytes;
    }

    public long getTotalLogFileSizeInBytes() {
      return totalLogFileSizeInBytes;
    }

    public long getBaseFileCount() {
      return baseFileCount;
    }

    public long getLogFileCount() {
      return logFileCount;
    }
  }

  public static Map<String, HoodiePartitionMetric> statisticPartitionsMetric(
      List<String> partitionPaths,
      SyncableFileSystemView fileSystemView,
      ExecutorService ioExecutors) {
    Map<String, CompletableFuture<HoodiePartitionMetric>> futures = new HashMap<>();
    for (String path : partitionPaths) {
      CompletableFuture<HoodiePartitionMetric> f =
          CompletableFuture.supplyAsync(
              () -> getPartitionMetric(path, fileSystemView), ioExecutors);
      futures.put(path, f);
    }

    Map<String, HoodiePartitionMetric> results = new HashMap<>();
    for (String path : partitionPaths) {
      CompletableFuture<HoodiePartitionMetric> future = futures.get(path);
      try {
        HoodiePartitionMetric metric = future.get();
        results.put(path, metric);
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException("Error when statistic partition: " + path + " metrics", e);
      }
    }
    return results;
  }

  private static HoodiePartitionMetric getPartitionMetric(
      String partitionPath, SyncableFileSystemView fsView) {
    List<FileSlice> latestSlices =
        fsView.getLatestFileSlices(partitionPath).collect(Collectors.toList());
    // Total size of the metadata and count of base/log files
    long totalBaseFileSizeInBytes = 0;
    long totalLogFileSizeInBytes = 0;
    int baseFileCount = 0;
    int logFileCount = 0;

    for (FileSlice slice : latestSlices) {
      if (slice.getBaseFile().isPresent()) {
        totalBaseFileSizeInBytes += slice.getBaseFile().get().getFileStatus().getLen();
        ++baseFileCount;
      }
      Iterator<HoodieLogFile> it = slice.getLogFiles().iterator();
      while (it.hasNext()) {
        totalLogFileSizeInBytes += it.next().getFileSize();
        ++logFileCount;
      }
    }

    return new HoodiePartitionMetric(
        baseFileCount, logFileCount, totalBaseFileSizeInBytes, totalLogFileSizeInBytes);
  }
}

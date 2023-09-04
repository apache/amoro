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

package com.netease.arctic.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.GenericBlobMetadata;
import org.apache.iceberg.GenericStatisticsFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.puffin.Blob;
import org.apache.iceberg.puffin.BlobMetadata;
import org.apache.iceberg.puffin.FileMetadata;
import org.apache.iceberg.puffin.Puffin;
import org.apache.iceberg.puffin.PuffinReader;
import org.apache.iceberg.puffin.PuffinWriter;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.StructLikeMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PuffinUtil {
  public static final String BLOB_TYPE_OPTIMIZED_SEQUENCE = "optimized-sequence";
  public static final String BLOB_TYPE_BASE_OPTIMIZED_TIME = "base-optimized-time";

  public static Writer writer(UnkeyedTable table, long snapshotId, long sequenceNumber) {
    return new Writer(table, snapshotId, sequenceNumber);
  }

  public static Reader reader(UnkeyedTable table) {
    return new Reader(table);
  }

  public static Reader reader(KeyedTable keyedTable) {
    return new Reader(keyedTable.baseTable());
  }

  public static class Writer {
    private final UnkeyedTable table;
    private final long snapshotId;
    private final long sequenceNumber;
    private StructLikeMap<Long> optimizedSequence;
    private StructLikeMap<Long> baseOptimizedTime;
    private boolean overwrite = false;

    private Writer(UnkeyedTable table, long snapshotId, long sequenceNumber) {
      this.table = table;
      this.snapshotId = snapshotId;
      this.sequenceNumber = sequenceNumber;
    }

    public Writer addOptimizedSequence(StructLikeMap<Long> optimizedSequence) {
      this.optimizedSequence = optimizedSequence;
      return this;
    }

    public Writer addBaseOptimizedTime(StructLikeMap<Long> baseOptimizedTime) {
      this.baseOptimizedTime = baseOptimizedTime;
      return this;
    }
    
    public Writer overwrite() {
      this.overwrite = true;
      return this;
    }

    public StatisticsFile write() {
      if (optimizedSequence == null && baseOptimizedTime == null) {
        throw new IllegalArgumentException("No statistics to write");
      }
      OutputFile outputFile = table.io().newOutputFile(table.location() + "/metadata/" + snapshotId + ".puffin");
      if (outputFile.toInputFile().exists()) {
        if (overwrite) {
          // overwrite the old puffin file for retry
          table.io().deleteFile(outputFile);
        } else {
          throw new IllegalStateException("Puffin file already exists: " + outputFile.location());
        }
      }
      List<BlobMetadata> blobMetadata;
      long fileSize;
      long footerSize;
      try (PuffinWriter puffinWriter = Puffin.write(outputFile).build()) {
        if (optimizedSequence != null) {
          puffinWriter.add(
              new Blob(BLOB_TYPE_OPTIMIZED_SEQUENCE, Collections.emptyList(), snapshotId, sequenceNumber,
                  ByteBuffer.wrap(encodePartitionValues(table.spec(), optimizedSequence).getBytes())));
        }
        if (baseOptimizedTime != null) {
          puffinWriter.add(
              new Blob(BLOB_TYPE_BASE_OPTIMIZED_TIME, Collections.emptyList(), snapshotId, sequenceNumber,
                  ByteBuffer.wrap(encodePartitionValues(table.spec(), baseOptimizedTime).getBytes())));
        }
        puffinWriter.finish();
        blobMetadata = puffinWriter.writtenBlobsMetadata();
        fileSize = puffinWriter.fileSize();
        footerSize = puffinWriter.footerSize();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      List<org.apache.iceberg.BlobMetadata> collect =
          blobMetadata.stream().map(GenericBlobMetadata::from).collect(Collectors.toList());
      return new GenericStatisticsFile(
          snapshotId,
          outputFile.location(),
          fileSize,
          footerSize,
          collect);
    }
  }

  public static class Reader {
    private final UnkeyedTable table;
    private Long snapshotId;

    private Reader(UnkeyedTable table) {
      this.table = table;
    }
    
    public Reader useSnapshotId(long snapshotId) {
      this.snapshotId = snapshotId;
      return this;
    }

    public StructLikeMap<Long> readOptimizedSequence() {
      return readWithCompatibility(BLOB_TYPE_OPTIMIZED_SEQUENCE);
    }

    public StructLikeMap<Long> readBaseOptimizedTime() {
      return readWithCompatibility(BLOB_TYPE_BASE_OPTIMIZED_TIME);
    }

    private StructLikeMap<Long> readWithCompatibility(String type) {
      StructLikeMap<Long> result = read(type);
      if (result != null) {
        return result;
      }
      switch (type) {
        case BLOB_TYPE_OPTIMIZED_SEQUENCE:
          return TablePropertyUtil.getPartitionLongProperties(table, TableProperties.PARTITION_OPTIMIZED_SEQUENCE);
        case BLOB_TYPE_BASE_OPTIMIZED_TIME:
          return TablePropertyUtil.getPartitionLongProperties(table, TableProperties.PARTITION_BASE_OPTIMIZED_TIME);
        default:
          throw new IllegalArgumentException("Unknown blob type: " + type);
      }
    }

    private StructLikeMap<Long> read(String type) {
      StatisticsFile statisticsFile = findStatisticsFile(type);
      if (statisticsFile == null) {
        return null;
      }
      try (PuffinReader puffin = Puffin.read(table.io().newInputFile(statisticsFile.path())).build()) {
        FileMetadata fileMetadata = puffin.fileMetadata();
        BlobMetadata blobMetadata =
            fileMetadata.blobs().stream().filter(b -> type.equals(b.type())).findFirst().orElseThrow(
                () -> new IllegalStateException("No blob of type " + type + " found in file " + statisticsFile.path()));
        ByteBuffer result = puffin.readAll(Collections.singletonList(blobMetadata)).iterator().next().second();
        return decodePartitionValues(table.spec(), new String(result.array()));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    private StatisticsFile findStatisticsFile(String type) {
      long snapshotId;
      if (this.snapshotId == null) {
        Snapshot currentSnapshot = table.currentSnapshot();
        if (currentSnapshot == null) {
          return null;
        }
        snapshotId = currentSnapshot.snapshotId();
      } else {
        snapshotId = this.snapshotId;
      }
      List<StatisticsFile> statisticsFiles = table.statisticsFiles();
      if (statisticsFiles.isEmpty()) {
        return null;
      }
      Map<Long, List<StatisticsFile>> statisticsFilesBySnapshotId =
          statisticsFiles.stream().collect(Collectors.groupingBy(StatisticsFile::snapshotId));
      while (true) {
        List<StatisticsFile> statisticsFile = statisticsFilesBySnapshotId.get(snapshotId);
        if (statisticsFile != null) {
          Preconditions.checkArgument(statisticsFile.size() == 1,
              "Multiple statistics files found for snapshot %s", snapshotId);
          StatisticsFile file = statisticsFile.get(0);
          List<org.apache.iceberg.BlobMetadata> blobs = file.blobMetadata();
          if (blobs != null) {
            if (blobs.stream().anyMatch(b -> type.equals(b.type()))) {
              return file;
            }
          }
        }
        // seek parent snapshot
        Snapshot snapshot = table.snapshot(snapshotId);
        if (snapshot == null) {
          return null;
        } else {
          snapshotId = snapshot.parentId();
        }
      }
    }
  }

  public static StatisticsFile copyToSnapshot(StatisticsFile statisticsFile, long snapshotId) {
    return new GenericStatisticsFile(
        snapshotId,
        statisticsFile.path(),
        statisticsFile.fileSizeInBytes(),
        statisticsFile.fileFooterSizeInBytes(),
        statisticsFile.blobMetadata());
  }

  private static <T> String encodePartitionValues(PartitionSpec spec, StructLikeMap<T> partitionValues) {
    Map<String, T> stringKeyMap = Maps.newHashMap();
    for (StructLike pd : partitionValues.keySet()) {
      String pathLike = spec.partitionToPath(pd);
      stringKeyMap.put(pathLike, partitionValues.get(pd));
    }
    String value;
    try {
      value = new ObjectMapper().writeValueAsString(stringKeyMap);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
    return value;
  }

  private static StructLikeMap<Long> decodePartitionValues(PartitionSpec spec, String value) {
    try {
      StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
      TypeReference<Map<String, Long>> typeReference = new TypeReference<Map<String, Long>>() {
      };
      Map<String, Long> map = new ObjectMapper().readValue(value, typeReference);
      for (String key : map.keySet()) {
        if (spec.isUnpartitioned()) {
          results.put(TablePropertyUtil.EMPTY_STRUCT, map.get(key));
        } else {
          StructLike partitionData = ArcticDataFiles.data(spec, key);
          results.put(partitionData, map.get(key));
        }
      }
      return results;
    } catch (JsonProcessingException e) {
      throw new UnsupportedOperationException("Failed to decode partition max txId ", e);
    }
  }
}

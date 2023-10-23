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
import org.apache.iceberg.GenericBlobMetadata;
import org.apache.iceberg.GenericStatisticsFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
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
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Util class for write and read optimized sequences/time from Iceberg Puffin files.
 * Puffin are a kind of file format for Iceberg statistics file {@link StatisticsFile}.
 */
public class PuffinUtil {

  public static Writer writer(Table table, long snapshotId, long sequenceNumber) {
    return new Writer(table, snapshotId, sequenceNumber);
  }

  public static Reader reader(Table table) {
    return new Reader(table);
  }

  public static class Writer {
    private final long snapshotId;
    private final long sequenceNumber;
    private final OutputFile outputFile;
    private final PuffinWriter puffinWriter;
    private boolean closed = false;

    private Writer(Table table, long snapshotId, long sequenceNumber) {
      this.snapshotId = snapshotId;
      this.sequenceNumber = sequenceNumber;
      this.outputFile = table.io()
          .newOutputFile(table.location() + "/data/" + snapshotId + "-" + UUID.randomUUID() + ".puffin");
      this.puffinWriter = Puffin.write(outputFile).build();
    }

    public Writer addBlob(String type, ByteBuffer blobData) {
      checkNotClosed();
      puffinWriter.add(new Blob(type, Collections.emptyList(), snapshotId, sequenceNumber, blobData));
      return this;
    }

    public <T> Writer add(String type, T data, DataSerializer<T> serializer) {
      checkNotClosed();
      return addBlob(type, serializer.serialize(data));
    }

    public StatisticsFile complete() {
      checkNotClosed();
      try {
        puffinWriter.finish();
        List<BlobMetadata> blobMetadata = puffinWriter.writtenBlobsMetadata();
        long fileSize = puffinWriter.fileSize();
        long footerSize = puffinWriter.footerSize();
        List<org.apache.iceberg.BlobMetadata> collect =
            blobMetadata.stream().map(GenericBlobMetadata::from).collect(Collectors.toList());
        return new GenericStatisticsFile(
            snapshotId,
            outputFile.location(),
            fileSize,
            footerSize,
            collect);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } finally {
        close();
      }
    }

    private void checkNotClosed() {
      Preconditions.checkState(!closed, "Cannot operate on a closed writer");
    }

    private void close() {
      if (!closed) {
        closed = true;
        try {
          puffinWriter.close();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    }
  }

  public static class Reader {
    private final Table table;

    private Reader(Table table) {
      this.table = table;
    }

    public <T> T read(StatisticsFile statisticsFile, String type, DataSerializer<T> deserializer) {
      ByteBuffer blobData = read(statisticsFile, type);
      if (blobData == null) {
        return null;
      }
      return deserializer.deserialize(blobData);
    }

    public ByteBuffer read(StatisticsFile statisticsFile, String type) {
      try (PuffinReader puffin = Puffin.read(table.io().newInputFile(statisticsFile.path())).build()) {
        FileMetadata fileMetadata = puffin.fileMetadata();
        BlobMetadata blobMetadata =
            fileMetadata.blobs().stream().filter(b -> type.equals(b.type())).findFirst().orElseThrow(
                () -> new IllegalStateException("No blob of type " + type + " found in file " + statisticsFile.path()));
        return puffin.readAll(Collections.singletonList(blobMetadata)).iterator().next().second();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  /**
   * Copy a statistic file with a new snapshot id, and it points to the same file in the file system as the original
   * file.
   *
   * @param statisticsFile - original statistic file
   * @param snapshotId     - new snapshot id
   * @return a new copied statistic file
   */
  public static StatisticsFile copyToSnapshot(StatisticsFile statisticsFile, long snapshotId) {
    return new GenericStatisticsFile(
        snapshotId,
        statisticsFile.path(),
        statisticsFile.fileSizeInBytes(),
        statisticsFile.fileFooterSizeInBytes(),
        statisticsFile.blobMetadata());
  }

  public static List<StatisticsFile> findLatestValidStatisticsFiles(Table table,
                                                                    long currentSnapshotId,
                                                                    Predicate<StatisticsFile> validator) {
    List<StatisticsFile> statisticsFiles = table.statisticsFiles();
    if (statisticsFiles.isEmpty()) {
      return null;
    }
    Map<Long, List<StatisticsFile>> statisticsFilesBySnapshotId =
        statisticsFiles.stream().collect(Collectors.groupingBy(StatisticsFile::snapshotId));
    long snapshotId = currentSnapshotId;
    while (true) {
      List<StatisticsFile> statisticsFileList = statisticsFilesBySnapshotId.get(snapshotId);
      if (statisticsFileList != null) {
        List<StatisticsFile> result = statisticsFileList.stream().filter(validator).collect(Collectors.toList());
        if (!result.isEmpty()) {
          return result;
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

  public static Predicate<StatisticsFile> containsBlobOfType(String type) {
    return statisticsFile -> statisticsFile.blobMetadata().stream().anyMatch(b -> type.equals(b.type()));
  }
  
  public static PartitionDataSerializer createPartitionDataSerializer(PartitionSpec spec) {
    return new PartitionDataSerializer(spec);
  }

  public interface DataSerializer<T> {
    ByteBuffer serialize(T data);

    T deserialize(ByteBuffer buffer);
  }

  public static class PartitionDataSerializer implements DataSerializer<StructLikeMap<Long>> {

    private final PartitionSpec spec;

    public PartitionDataSerializer(PartitionSpec spec) {
      this.spec = spec;
    }

    @Override
    public ByteBuffer serialize(StructLikeMap<Long> data) {
      Map<String, Long> stringKeyMap = Maps.newHashMap();
      for (StructLike pd : data.keySet()) {
        String pathLike = spec.partitionToPath(pd);
        stringKeyMap.put(pathLike, data.get(pd));
      }
      String value;
      try {
        value = new ObjectMapper().writeValueAsString(stringKeyMap);
      } catch (JsonProcessingException e) {
        throw new UncheckedIOException(e);
      }
      return ByteBuffer.wrap(value.getBytes());
    }

    @Override
    public StructLikeMap<Long> deserialize(ByteBuffer buffer) {
      try {
        StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
        TypeReference<Map<String, Long>> typeReference = new TypeReference<Map<String, Long>>() {
        };
        Map<String, Long> map = new ObjectMapper().readValue(new String(buffer.array()), typeReference);
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
}

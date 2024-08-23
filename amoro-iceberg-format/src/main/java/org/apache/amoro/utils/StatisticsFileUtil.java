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

package org.apache.amoro.utils;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.type.MapLikeType;
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
import org.apache.iceberg.util.Pair;
import org.apache.iceberg.util.StructLikeMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Util class for write and read Iceberg statistics file {@link StatisticsFile}. */
public class StatisticsFileUtil {

  public static WriterBuilder writerBuilder(Table table) {
    return new WriterBuilder(table);
  }

  public static class WriterBuilder {
    private final Table table;
    private Long snapshotId;

    private WriterBuilder(Table table) {
      this.table = table;
    }

    public WriterBuilder withSnapshotId(long snapshotId) {
      this.snapshotId = snapshotId;
      return this;
    }

    public Writer build() {
      Snapshot snapshot;
      if (snapshotId == null) {
        snapshot = table.currentSnapshot();
      } else {
        snapshot = table.snapshot(snapshotId);
      }
      Preconditions.checkArgument(snapshot != null, "Cannot find snapshot with id %s", snapshotId);
      return new Writer(table, snapshot.snapshotId(), snapshot.sequenceNumber());
    }
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
      this.outputFile =
          table
              .io()
              .newOutputFile(
                  table.location()
                      + "/data/puffin/"
                      + snapshotId
                      + "-"
                      + UUID.randomUUID()
                      + ".puffin");
      this.puffinWriter = Puffin.write(outputFile).build();
    }

    public Writer add(Blob blob) {
      checkNotClosed();
      puffinWriter.add(blob);
      return this;
    }

    public Writer add(String type, ByteBuffer blobData) {
      add(new Blob(type, Collections.emptyList(), snapshotId, sequenceNumber, blobData));
      return this;
    }

    public <T> Writer add(String type, T data, DataSerializer<T> serializer) {
      return add(type, serializer.serialize(data));
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
            snapshotId, outputFile.location(), fileSize, footerSize, collect);
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

    public <T> List<T> read(
        StatisticsFile statisticsFile, String type, DataSerializer<T> deserializer) {
      return read(statisticsFile, type).stream()
          .map(deserializer::deserialize)
          .collect(Collectors.toList());
    }

    public List<ByteBuffer> read(StatisticsFile statisticsFile, String type) {
      try (PuffinReader puffin =
          Puffin.read(table.io().newInputFile(statisticsFile.path())).build()) {
        FileMetadata fileMetadata = puffin.fileMetadata();
        List<BlobMetadata> blobs =
            fileMetadata.blobs().stream()
                .filter(b -> type.equals(b.type()))
                .collect(Collectors.toList());
        return Lists.newArrayList(Iterables.transform(puffin.readAll(blobs), Pair::second));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  /**
   * Copy a statistic file with a new snapshot id, and it points to the same file in the file system
   * as the original file.
   *
   * @param statisticsFile - original statistic file
   * @param snapshotId - new snapshot id
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

  /**
   * Get the statistics files that belong to the given snapshot.
   *
   * @param table - Iceberg Table
   * @param snapshotId - the snapshot id
   * @param type - the type of the blob type
   * @return the list of statistics files
   */
  public static List<StatisticsFile> getStatisticsFiles(Table table, long snapshotId, String type) {
    List<StatisticsFile> statisticsFiles = table.statisticsFiles();
    if (statisticsFiles.isEmpty()) {
      return Collections.emptyList();
    }
    return statisticsFiles.stream()
        .filter(s -> s.blobMetadata().stream().anyMatch(b -> type.equals(b.type())))
        .collect(Collectors.groupingBy(StatisticsFile::snapshotId))
        .get(snapshotId);
  }

  public static <T> PartitionDataSerializer<T> createPartitionDataSerializer(
      PartitionSpec spec, Class<T> valueClassType) {
    return new PartitionDataSerializer<>(spec, valueClassType);
  }

  /**
   * A serializer to serialize and deserialize data between ByteBuffer and Type T.
   *
   * @param <T> - the class type of the data
   */
  public interface DataSerializer<T> {
    ByteBuffer serialize(T data);

    T deserialize(ByteBuffer buffer);
  }

  public static class PartitionDataSerializer<T> implements DataSerializer<StructLikeMap<T>> {

    private final PartitionSpec spec;
    private final Class<T> valueClassType;

    public PartitionDataSerializer(PartitionSpec spec, Class<T> valueClassType) {
      this.spec = spec;
      this.valueClassType = valueClassType;
    }

    @Override
    public ByteBuffer serialize(StructLikeMap<T> data) {
      Map<String, T> stringKeyMap = Maps.newHashMap();
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
    public StructLikeMap<T> deserialize(ByteBuffer buffer) {
      try {
        StructLikeMap<T> results = StructLikeMap.create(spec.partitionType());
        ObjectMapper objectMapper = new ObjectMapper();
        MapLikeType mapLikeType =
            objectMapper
                .getTypeFactory()
                .constructMapLikeType(Map.class, String.class, valueClassType);

        Map<String, T> map = objectMapper.readValue(new String(buffer.array()), mapLikeType);
        for (String key : map.keySet()) {
          if (spec.isUnpartitioned()) {
            results.put(TablePropertyUtil.EMPTY_STRUCT, map.get(key));
          } else {
            StructLike partitionData = MixedDataFiles.data(spec, key);
            results.put(partitionData, map.get(key));
          }
        }
        return results;
      } catch (JsonProcessingException e) {
        throw new UnsupportedOperationException("Failed to decode partition data ", e);
      }
    }
  }
}

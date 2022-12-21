/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.utils.map;

import org.apache.commons.lang.Validate;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.openjdk.jol.info.GraphLayout;

import java.util.Map;
import java.util.Optional;

public class SimpleSpillableMap<T, K> implements SimpleMap<T, K> {

  private static final int RECORDS_TO_SKIP_FOR_ESTIMATING = 200;
  private final long maxInMemorySizeInBytes;
  private final String mapIdentifier;
  private Map<T, K> memoryMap;
  private Optional<SimpleSpilledMap<T, K>> diskBasedMap = Optional.empty();
  private Long currentInMemoryMapSize;
  private long estimatedPayloadSize = 0;
  private int putCount = 0;

  private final Serializer<T> keySerializer;
  private final Serializer<K> valueSerializer;

  protected SimpleSpillableMap(Long maxInMemorySizeInBytes, String mapIdentifier) {
    this(maxInMemorySizeInBytes, mapIdentifier, JavaSerializer.INSTANT, JavaSerializer.INSTANT);
  }

  protected SimpleSpillableMap(Long maxInMemorySizeInBytes, String mapIdentifier,
      Serializer<T> keySerializer, Serializer<K> valueSerializer) {
    Validate.isTrue(mapIdentifier != null, "Map identifier can not be null");
    this.memoryMap = Maps.newHashMap();
    this.maxInMemorySizeInBytes = maxInMemorySizeInBytes;
    this.currentInMemoryMapSize = 0L;
    this.mapIdentifier = mapIdentifier;
    this.keySerializer = keySerializer;
    this.valueSerializer = valueSerializer;
  }

  /**
   * Number of bytes spilled to disk.
   */
  public long getSizeOfFileOnDiskInBytes() {
    return diskBasedMap.map(SimpleSpilledMap::sizeOfFileOnDiskInBytes).orElse(0L);
  }

  /**
   * Number of entries in InMemoryMap.
   */
  public int getMemoryMapSize() {
    return memoryMap.size();
  }

  /**
   * Approximate memory footprint of the in-memory map.
   */
  public long getMemoryMapSpaceSize() {
    return currentInMemoryMapSize;
  }

  public boolean containsKey(T key) {
    return memoryMap.containsKey(key) ||
            diskBasedMap.map(diskMap -> diskMap.containsKey(key)).orElse(false);
  }

  public K get(T key) {
    return Optional.ofNullable(memoryMap.get(key))
            .orElse(diskBasedMap.map(diskMap -> diskMap.get(key)).orElse(null));
  }

  public void put(T key, K value) {
    if (estimatedPayloadSize == 0) {
      this.estimatedPayloadSize = estimateSize(key) + estimateSize(value);
    } else if (++putCount % RECORDS_TO_SKIP_FOR_ESTIMATING == 0) {
      this.estimatedPayloadSize = (long) (this.estimatedPayloadSize * 0.9 +
              (estimateSize(key) + estimateSize(value)) * 0.1);
      this.currentInMemoryMapSize = this.memoryMap.size() * this.estimatedPayloadSize;
    }

    if (this.currentInMemoryMapSize < maxInMemorySizeInBytes) {
      if (memoryMap.put(key, value) == null) {
        currentInMemoryMapSize += this.estimatedPayloadSize;
      }
    } else {
      if (!diskBasedMap.isPresent()) {
        diskBasedMap = Optional.of(new SimpleSpilledMap<>(keySerializer, valueSerializer));
      }
      diskBasedMap.get().put(key, value);
    }
  }

  public void delete(T key) {
    if (memoryMap.containsKey(key)) {
      currentInMemoryMapSize -= estimatedPayloadSize;
      memoryMap.remove(key);
    } else {
      diskBasedMap.ifPresent(map -> map.delete(key));
    }
  }

  public void close() {
    memoryMap = null;
    diskBasedMap.ifPresent(SimpleSpilledMap::close);
    currentInMemoryMapSize = 0L;
  }

  private long estimateSize(Object obj) {
    return obj == null ? 0 : GraphLayout.parseInstance(obj).totalSize();
  }

  protected class SimpleSpilledMap<T, K>
          implements SimpleMap<T, K> {

    private final RocksDBBackend<T, K> rocksDB;

    public SimpleSpilledMap(Serializer<T> keySerializer, Serializer<K> valueSerializer) {
      rocksDB = RocksDBBackend.getOrCreateInstance(keySerializer, valueSerializer);
      rocksDB.addColumnFamily(mapIdentifier);
    }

    public boolean containsKey(T key) {
      return rocksDB.get(mapIdentifier, key) != null;
    }

    public K get(T key) {
      return rocksDB.get(mapIdentifier, key);
    }

    public void put(T key, K value) {
      rocksDB.put(mapIdentifier, key, value);
    }

    public void delete(T key) {
      rocksDB.delete(mapIdentifier, key);
    }

    public void close() {
      rocksDB.dropColumnFamily(mapIdentifier);
    }

    public long sizeOfFileOnDiskInBytes() {
      return rocksDB.getTotalBytesWritten();
    }
  }
}

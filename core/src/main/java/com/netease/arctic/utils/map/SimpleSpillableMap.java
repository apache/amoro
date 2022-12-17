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

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import org.apache.commons.lang.Validate;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class SimpleSpillableMap<T extends Serializable, K extends Serializable> implements SimpleMap<T, K> {

  private static final int NUMBER_OF_RECORDS_TO_ESTIMATE_PAYLOAD_SIZE = 100;
  private final long maxInMemorySizeInBytes;
  private Map<T, K> inMemoryMap;
  private Optional<SimpleSpilledMap<T, K>> diskBasedMap = Optional.empty();
  private Long currentInMemoryMapSize;
  private long estimatedPayloadSize = 0;
  private String mapIdentifier;
  private int putCount = 0;

  protected SimpleSpillableMap(Long maxInMemorySizeInBytes, String mapIdentifier) {
    Validate.isTrue(mapIdentifier != null, "Map identifier can not be null");
    this.inMemoryMap = Maps.newHashMap();
    this.maxInMemorySizeInBytes = maxInMemorySizeInBytes;
    this.currentInMemoryMapSize = 0L;
    this.mapIdentifier = mapIdentifier;
  }

  /**
   * Number of bytes spilled to disk.
   */
  public long getSizeOfFileOnDiskInBytes() {
    return diskBasedMap.map(diskMap -> diskMap.sizeOfFileOnDiskInBytes()).orElse(0L);
  }

  /**
   * Number of entries in InMemoryMap.
   */
  public int getMemoryMapSize() {
    return inMemoryMap.size();
  }

  /**
   * Approximate memory footprint of the in-memory map.
   */
  public long getMemoryMapSpaceSize() {
    return currentInMemoryMapSize;
  }

  public boolean containsKey(Object key) {
    return inMemoryMap.containsKey(key) ||
            diskBasedMap.map(diskMap -> diskMap.containsKey(key)).orElse(false);
  }

  public K get(T key) {
    return Optional.ofNullable(inMemoryMap.get(key))
            .orElse(diskBasedMap.map(diskMap -> diskMap.get(key)).orElse(null));
  }

  public void put(T key, K value) {
    if (estimatedPayloadSize == 0) {
      this.estimatedPayloadSize = estimateSize(key) + estimateSize(value);
    } else if (++putCount % NUMBER_OF_RECORDS_TO_ESTIMATE_PAYLOAD_SIZE == 0) {
      this.estimatedPayloadSize = (long) (this.estimatedPayloadSize * 0.9 +
              (estimateSize(key) + estimateSize(value)) * 0.1);
      this.currentInMemoryMapSize = this.inMemoryMap.size() * this.estimatedPayloadSize;
    }

    if (this.currentInMemoryMapSize < maxInMemorySizeInBytes) {
      if (inMemoryMap.put(key, value) == null) {
        currentInMemoryMapSize += this.estimatedPayloadSize;
      }
    } else {
      if (!diskBasedMap.isPresent()) {
        diskBasedMap = Optional.of(new SimpleSpilledMap<>());
      }
      diskBasedMap.get().put(key, value);
    }
  }

  public void delete(T key) {
    if (inMemoryMap.containsKey(key)) {
      currentInMemoryMapSize -= estimatedPayloadSize;
      inMemoryMap.remove(key);
    } else {
      diskBasedMap.ifPresent(map -> map.delete(key));
    }
  }

  public void close() {
    inMemoryMap = null;
    diskBasedMap.ifPresent(diskMap -> diskMap.close());
    currentInMemoryMapSize = 0L;
  }

  private long estimateSize(Object obj) {
    return ObjectSizeCalculator.getObjectSize(obj);
  }

  protected class SimpleSpilledMap<T extends Serializable, K extends Serializable>
          implements SimpleMap<T, K> {

    private RocksDBBackend rocksDB = RocksDBBackend.getOrCreateInstance();

    public SimpleSpilledMap() {
      rocksDB.addColumnFamily(mapIdentifier);
    }

    public boolean containsKey(Object key) {
      return rocksDB.get(mapIdentifier, (T) key) != null;
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

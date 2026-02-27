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

package org.apache.amoro.flink.lookup;

import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.utils.map.RocksDBBackend;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.shaded.guava31.com.google.common.cache.Cache;
import org.apache.flink.table.data.RowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that stores the secondary index in the cache. For {@link SecondaryIndexTable}.
 *
 * <p>Support update the secondary index in the cache.
 */
public class RocksDBSetSpilledState extends RocksDBCacheState<Set<ByteArrayWrapper>> {
  private static final Logger LOG = LoggerFactory.getLogger(RocksDBSetSpilledState.class);
  protected ThreadLocal<BinaryRowDataSerializerWrapper> joinKeySerializerThreadLocal =
      new ThreadLocal<>();
  private final BinaryRowDataSerializerWrapper joinKeySerializer;
  /** Multi-threads would put and delete the joinKeys and Set<ByteArrayWrapper> in the rocksdb. */
  private final Object rocksDBLock = new Object();

  private final Map<ByteArrayWrapper, Set<ByteArrayWrapper>> tmpInitializationMap =
      new ConcurrentHashMap<>();

  public RocksDBSetSpilledState(
      RocksDBBackend rocksDB,
      String columnFamilyName,
      BinaryRowDataSerializerWrapper joinKeySerializer,
      BinaryRowDataSerializerWrapper uniqueKeySerialization,
      BinaryRowDataSerializerWrapper valueSerializer,
      MetricGroup metricGroup,
      LookupOptions lookupOptions) {
    super(
        rocksDB,
        columnFamilyName,
        uniqueKeySerialization,
        valueSerializer,
        metricGroup,
        lookupOptions,
        true);
    this.joinKeySerializer = joinKeySerializer;
  }

  public void asyncWrite(RowData joinKey, byte[] uniqueKeyBytes) throws IOException {
    byte[] joinKeyBytes = serializeKey(joinKey);
    LookupRecord.OpType opType = convertToOpType(joinKey.getRowKind());
    putIntoQueue(LookupRecord.of(opType, joinKeyBytes, uniqueKeyBytes));
  }

  @Override
  public byte[] serializeKey(RowData key) throws IOException {
    if (joinKeySerializerThreadLocal.get() == null) {
      joinKeySerializerThreadLocal.set(joinKeySerializer.clone());
    }
    return serializeKey(joinKeySerializerThreadLocal.get(), key);
  }

  /**
   * Serialize join key to bytes and put the join key bytes and unique key bytes in the cache.
   *
   * @param joinKey the join key
   * @param uniqueKeyBytes the unique key bytes
   * @throws IOException if serialize the RowData variable failed.
   */
  public void put(RowData joinKey, byte[] uniqueKeyBytes) throws IOException {
    byte[] joinKeyBytes = serializeKey(joinKey);
    putSecondaryCache(joinKeyBytes, uniqueKeyBytes);
  }

  /**
   * Delete the secondary index in the cache.
   *
   * @param joinKey the join key
   * @param uniqueKeyBytes the unique key bytes
   * @throws IOException if serialize the RowData variable failed.
   */
  public void delete(RowData joinKey, byte[] uniqueKeyBytes) throws IOException {
    final byte[] joinKeyBytes = serializeKey(joinKey);
    deleteSecondaryCache(joinKeyBytes, uniqueKeyBytes);
  }

  /**
   * Retrieve the elements of the key.
   *
   * <p>Fetch the Collection from guava cache, if not present, fetch the result from RocksDB. if
   * present, just return the result.
   *
   * @return not null, but may be empty.
   */
  public Collection<ByteArrayWrapper> get(RowData key) throws IOException {
    final byte[] joinKeyBytes = serializeKey(key);
    ByteArrayWrapper joinKeyWrap = wrap(joinKeyBytes);
    Set<ByteArrayWrapper> result = guavaCache.getIfPresent(joinKeyWrap);
    if (result == null) {
      byte[] uniqueKeysDeserialized = rocksDB.get(columnFamilyHandle, joinKeyBytes);
      if (uniqueKeysDeserialized != null) {
        result = ByteArraySetSerializer.deserialize(uniqueKeysDeserialized);
      }

      if (CollectionUtils.isNotEmpty(result)) {
        guavaCache.put(joinKeyWrap, result);
        return result;
      }
      return Collections.emptyList();
    }
    return result;
  }

  @Override
  public void putCacheValue(
      Cache<ByteArrayWrapper, Set<ByteArrayWrapper>> cache,
      ByteArrayWrapper keyWrap,
      ByteArrayWrapper valueWrap) {
    if (initialized()) {
      byte[] joinKeyBytes = keyWrap.bytes;
      synchronized (rocksDBLock) {
        byte[] uniqueKeysDeserialized = rocksDB.get(columnFamilyHandle, joinKeyBytes);
        if (uniqueKeysDeserialized != null) {
          Set<ByteArrayWrapper> set = ByteArraySetSerializer.deserialize(uniqueKeysDeserialized);
          if (!set.contains(valueWrap)) {
            set.add(valueWrap);
            uniqueKeysDeserialized = ByteArraySetSerializer.serialize(set);
            rocksDB.put(columnFamilyHandle, joinKeyBytes, uniqueKeysDeserialized);
          }
        } else {
          Set<ByteArrayWrapper> set = new HashSet<>();
          set.add(valueWrap);
          uniqueKeysDeserialized = ByteArraySetSerializer.serialize(set);
          rocksDB.put(columnFamilyHandle, joinKeyBytes, uniqueKeysDeserialized);
        }
      }
      return;
    }
    tmpInitializationMap.compute(
        keyWrap,
        (keyWrapper, oldSet) -> {
          if (oldSet == null) {
            oldSet = Sets.newHashSet();
          }
          oldSet.add(valueWrap);
          return oldSet;
        });
  }

  @Override
  public void removeValue(
      Cache<ByteArrayWrapper, Set<ByteArrayWrapper>> cache,
      ByteArrayWrapper keyWrap,
      ByteArrayWrapper valueWrap) {
    if (initialized()) {
      byte[] joinKeyBytes = keyWrap.bytes;
      synchronized (rocksDBLock) {
        byte[] uniqueKeysDeserialized = rocksDB.get(columnFamilyHandle, joinKeyBytes);
        if (uniqueKeysDeserialized == null) {
          return;
        }
        Set<ByteArrayWrapper> set = ByteArraySetSerializer.deserialize(uniqueKeysDeserialized);
        if (set.contains(valueWrap)) {
          set.remove(valueWrap);
          if (!set.isEmpty()) {
            uniqueKeysDeserialized = ByteArraySetSerializer.serialize(set);
            rocksDB.put(columnFamilyHandle, joinKeyBytes, uniqueKeysDeserialized);
          }
        }
      }
      return;
    }
    tmpInitializationMap.compute(
        keyWrap,
        (keyWrapper, oldSet) -> {
          if (oldSet == null) {
            return null;
          }
          oldSet.remove(valueWrap);
          if (oldSet.isEmpty()) {
            return null;
          }
          return oldSet;
        });
  }

  public void bulkIntoRocksDB() {
    LOG.info("Total size={} in the tmp map, try to bulk into rocksdb", tmpInitializationMap.size());
    int[] count = {0};
    long start = System.currentTimeMillis();

    tmpInitializationMap.forEach(
        (byteArrayWrapper, set) -> {
          rocksDB.put(
              columnFamilyHandle, byteArrayWrapper.bytes, ByteArraySetSerializer.serialize(set));
          set = null;
          count[0] = count[0] + 1;
          if (count[0] % 100000 == 0) {
            LOG.info("Ingested {} into rocksdb.", count[0]);
          }
        });
    tmpInitializationMap.clear();

    LOG.info("Ingested {} completely, cost:{} ms.", count, System.currentTimeMillis() - start);
  }
}

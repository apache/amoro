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

import org.apache.amoro.utils.map.RocksDBBackend;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/** A class used to store the state of a lookup record. For {@link UniqueIndexTable}. */
public class RocksDBRecordState extends RocksDBCacheState<byte[]> {
  private static final Logger LOG = LoggerFactory.getLogger(RocksDBRecordState.class);

  public RocksDBRecordState(
      RocksDBBackend rocksDB,
      String columnFamilyName,
      BinaryRowDataSerializerWrapper keySerializer,
      BinaryRowDataSerializerWrapper valueSerializer,
      MetricGroup metricGroup,
      LookupOptions lookupOptions) {
    super(
        rocksDB,
        columnFamilyName,
        keySerializer,
        valueSerializer,
        metricGroup,
        lookupOptions,
        false);
  }

  /**
   * Writes a key-value pair to the sst file.
   *
   * @param key The key of the pair.
   * @param value The value of the pair.
   */
  public void asyncWrite(RowData key, RowData value) throws IOException {
    byte[] keyBytes = serializeKey(key);
    asyncWrite(key.getRowKind(), keyBytes, value);
  }

  public void asyncWrite(RowKind rowKind, byte[] keyBytes, RowData value) throws IOException {
    byte[] valueBytes = serializeValue(value);
    LookupRecord.OpType opType = convertToOpType(rowKind);
    putIntoQueue(LookupRecord.of(opType, keyBytes, valueBytes));
  }

  /**
   * Retrieve the RowData from guava cache firstly, if value is null, fetch the value from the
   * rocksDB.
   *
   * @param key try to find the record via this key.
   * @throws IOException if serialize the RowData variable <code>key</code> failed.
   */
  public Optional<RowData> get(RowData key) throws IOException {
    byte[] keyBytes = serializeKey(key);
    return get(keyBytes);
  }

  public Optional<RowData> get(byte[] keyBytes) throws IOException {
    ByteArrayWrapper key = wrap(keyBytes);
    byte[] recordBytes = guavaCache.getIfPresent(key);
    if (recordBytes == null) {
      recordBytes = rocksDB.get(columnFamilyHandle, key.bytes);
      if (recordBytes != null) {
        guavaCache.put(key, recordBytes);
      }
    }
    return Optional.ofNullable(deserializeValue(recordBytes));
  }

  /**
   * Putting the serialized RowData key and value into the rocksDB and cache.
   *
   * @throws IOException if serialize the RowData variable key and value failed.
   */
  public void put(RowData key, RowData value) throws IOException {
    byte[] keyBytes = serializeKey(key);
    put(keyBytes, value);
  }

  public void put(byte[] keyBytes, RowData value) throws IOException {
    Preconditions.checkNotNull(value);

    byte[] valueBytes = serializeValue(value);
    rocksDB.put(columnFamilyHandle, keyBytes, valueBytes);

    // Speed up the initialization process of Lookup Join Function
    ByteArrayWrapper key = wrap(keyBytes);
    if (guavaCache.getIfPresent(wrap(keyBytes)) != null) {
      guavaCache.put(key, valueBytes);
    }
  }

  /**
   * Deleting the record in the rocksDB and cache if it exists.
   *
   * @throws IOException if serialize the RowData variable <code>key</code> failed.
   */
  public void delete(RowData key) throws IOException {
    byte[] keyBytes = serializeKey(key);
    delete(keyBytes);
  }

  public void delete(byte[] keyBytes) {
    if (contain(wrap(keyBytes))) {
      rocksDB.delete(columnFamilyName, keyBytes);
      guavaCache.invalidate(wrap(keyBytes));
    }
  }

  private boolean contain(ByteArrayWrapper byteArrayWrapper) {
    byte[] recordBytes = guavaCache.getIfPresent(byteArrayWrapper);
    if (recordBytes == null) {
      recordBytes = rocksDB.get(columnFamilyName, byteArrayWrapper.bytes);
    }
    return recordBytes != null;
  }

  private byte[] serializeValue(RowData value) throws IOException {
    return valueSerializer().serialize(value);
  }

  private RowData deserializeValue(byte[] recordBytes) throws IOException {
    return valueSerializer().deserialize(recordBytes);
  }

  private BinaryRowDataSerializerWrapper valueSerializer() {
    if (valueSerializerThreadLocal.get() == null) {
      valueSerializerThreadLocal.set(valueSerializer.clone());
    }
    return valueSerializerThreadLocal.get();
  }
}

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

import org.apache.amoro.AmoroIOException;
import org.apache.amoro.utils.map.RocksDBBackend;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.shaded.guava31.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava31.com.google.common.cache.CacheBuilder;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.FlinkRuntimeException;
import org.apache.flink.util.Preconditions;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.MutableColumnFamilyOptions;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is an abstract state backed by RocksDB and Guava cache for storing and retrieving key-value
 * pairs of byte arrays.
 *
 * @param <V> the type of the cache's values, which are not permitted to be null
 */
public abstract class RocksDBCacheState<V> {
  private static final Logger LOG = LoggerFactory.getLogger(RocksDBCacheState.class);
  protected RocksDBBackend rocksDB;
  protected final boolean secondaryIndexMemoryMapEnabled;

  protected Cache<ByteArrayWrapper, V> guavaCache;

  protected final String columnFamilyName;
  protected final ColumnFamilyHandle columnFamilyHandle;
  protected ThreadLocal<BinaryRowDataSerializerWrapper> keySerializerThreadLocal =
      new ThreadLocal<>();

  protected ThreadLocal<BinaryRowDataSerializerWrapper> valueSerializerThreadLocal =
      new ThreadLocal<>();

  protected final BinaryRowDataSerializerWrapper keySerializer;

  protected final BinaryRowDataSerializerWrapper valueSerializer;
  private ExecutorService writeRocksDBService;
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private final AtomicBoolean closed = new AtomicBoolean(false);
  protected Queue<LookupRecord> lookupRecordsQueue;

  private final int writeRocksDBThreadNum;
  private List<Future<?>> writeRocksDBThreadFutures;
  private final AtomicReference<Throwable> writingThreadException = new AtomicReference<>();
  protected final MetricGroup metricGroup;
  private final LookupOptions lookupOptions;

  public RocksDBCacheState(
      RocksDBBackend rocksDB,
      String columnFamilyName,
      BinaryRowDataSerializerWrapper keySerializer,
      BinaryRowDataSerializerWrapper valueSerializer,
      MetricGroup metricGroup,
      LookupOptions lookupOptions,
      boolean secondaryIndexMemoryMapEnabled) {
    this.rocksDB = rocksDB;
    this.columnFamilyName = columnFamilyName;
    this.keySerializer = keySerializer;
    this.valueSerializer = valueSerializer;
    this.columnFamilyHandle = rocksDB.getColumnFamilyHandle(columnFamilyName);
    this.writeRocksDBThreadNum = lookupOptions.writeRecordThreadNum();
    this.secondaryIndexMemoryMapEnabled = secondaryIndexMemoryMapEnabled;
    this.metricGroup = metricGroup;
    this.lookupOptions = lookupOptions;
  }

  public void open() {
    writeRocksDBService = Executors.newFixedThreadPool(writeRocksDBThreadNum);

    if (secondaryIndexMemoryMapEnabled) {
      CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
      if (lookupOptions.isTTLAfterWriteValidated()) {
        cacheBuilder.expireAfterWrite(lookupOptions.ttlAfterWrite());
      }
    }
    guavaCache = CacheBuilder.newBuilder().maximumSize(lookupOptions.lruMaximumSize()).build();

    addGauge(columnFamilyName + "_queue_size", () -> lookupRecordsQueue.size());

    lookupRecordsQueue = new ConcurrentLinkedQueue<>();
    writeRocksDBThreadFutures =
        IntStream.range(0, writeRocksDBThreadNum)
            .mapToObj(
                value ->
                    writeRocksDBService.submit(
                        new WriteRocksDBTask(
                            String.format(
                                "writing-rocksDB-cf_%s-thread-%d", columnFamilyName, value),
                            secondaryIndexMemoryMapEnabled)))
            .collect(Collectors.toList());
  }

  @VisibleForTesting
  public byte[] serializeKey(RowData key) throws IOException {
    if (keySerializerThreadLocal.get() == null) {
      keySerializerThreadLocal.set(keySerializer.clone());
    }
    return serializeKey(keySerializerThreadLocal.get(), key);
  }

  @VisibleForTesting
  public byte[] serializeKey(BinaryRowDataSerializerWrapper keySerializer, RowData key)
      throws IOException {
    // key has a different RowKind would serialize different byte[], so unify the RowKind as INSERT.
    byte[] result;
    if (key.getRowKind() != RowKind.INSERT) {
      RowKind rowKind = key.getRowKind();
      key.setRowKind(RowKind.INSERT);
      result = keySerializer.serialize(key);
      key.setRowKind(rowKind);
      return result;
    }
    key.setRowKind(RowKind.INSERT);
    return keySerializer.serialize(key);
  }

  protected ByteArrayWrapper wrap(byte[] bytes) {
    return new ByteArrayWrapper(bytes, bytes.length);
  }

  protected void putIntoQueue(LookupRecord lookupRecord) {
    Preconditions.checkNotNull(lookupRecord);
    lookupRecordsQueue.add(lookupRecord);
  }

  /** Waiting for the writing threads completed. */
  public void waitWriteRocksDBDone() {
    long every5SecondsPrint = Long.MIN_VALUE;

    while (true) {
      if (lookupRecordsQueue.isEmpty()) {
        initialized.set(true);
        break;
      } else if (every5SecondsPrint < System.currentTimeMillis()) {
        LOG.info("Currently rocksDB queue size is {}.", lookupRecordsQueue.size());
        every5SecondsPrint = System.currentTimeMillis() + 5000;
      }
    }
    // Wait for all threads to finish
    for (Future<?> future : writeRocksDBThreadFutures) {
      try {
        // wait for the task to complete, with a timeout of 5 seconds
        future.get(5, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
        // task took too long, interrupt the thread and terminate the task
        future.cancel(true);
      } catch (InterruptedException | ExecutionException e) {
        // handle other exceptions
        throw new FlinkRuntimeException(e);
      }
    }
  }

  public boolean initialized() {
    return initialized.get();
  }

  protected LookupRecord.OpType convertToOpType(RowKind rowKind) {
    switch (rowKind) {
      case INSERT:
      case UPDATE_AFTER:
        return LookupRecord.OpType.PUT_BYTES;
      case DELETE:
      case UPDATE_BEFORE:
        return LookupRecord.OpType.DELETE_BYTES;
      default:
        throw new IllegalArgumentException(String.format("Not support this rowKind %s", rowKind));
    }
  }

  /**
   * Closes the RocksDB instance and cleans up the Guava cache.
   *
   * <p>Additionally, it shuts down the write-service and clears the RocksDB record queue if they
   * exist.
   */
  public void close() {
    rocksDB.close();
    guavaCache.cleanUp();
    if (writeRocksDBService != null) {
      writeRocksDBService.shutdown();
      writeRocksDBService = null;
    }
    closed.set(true);
    if (lookupRecordsQueue != null) {
      lookupRecordsQueue.clear();
      lookupRecordsQueue = null;
    }
  }

  public void initializationCompleted() {
    try {
      rocksDB.getDB().enableAutoCompaction(Collections.singletonList(columnFamilyHandle));
      MutableColumnFamilyOptions mutableColumnFamilyOptions =
          MutableColumnFamilyOptions.builder().setDisableAutoCompactions(false).build();
      rocksDB.setOptions(columnFamilyHandle, mutableColumnFamilyOptions);
    } catch (RocksDBException e) {
      throw new AmoroIOException(e);
    }

    LOG.info("set db options[disable_auto_compactions={}]", false);
  }

  public void addGauge(String metricName, Gauge<Object> gauge) {
    metricGroup.gauge(metricName, gauge);
  }

  protected void checkConcurrentFailed() {
    if (writingThreadException.get() != null) {
      LOG.error("Check concurrent writing threads.", writingThreadException.get());
      throw new FlinkRuntimeException(writingThreadException.get());
    }
  }

  /**
   * This task is running during the initialization phase to write data{@link LookupRecord} to
   * RocksDB.
   *
   * <p>During the initialization phase, the Merge-on-Read approach is used to retrieve data, which
   * will only return INSERT data. When there are multiple entries with the same primary key, only
   * one entry will be returned.
   *
   * <p>During the initialization phase, the incremental pull approach is also used to retrieve data
   * that include four {@link RowKind} rowKinds, -D, +I, -U, and +U.
   */
  class WriteRocksDBTask implements Runnable {

    private final String name;
    private final boolean secondaryIndexMemoryMapEnabled;

    public WriteRocksDBTask(String name, boolean secondaryIndexMemoryMapEnabled) {
      this.name = name;
      this.secondaryIndexMemoryMapEnabled = secondaryIndexMemoryMapEnabled;
    }

    @Override
    public void run() {
      LOG.info("{} starting.", name);
      try {
        while (!closed.get() && !initialized.get()) {
          LookupRecord record = lookupRecordsQueue.poll();
          if (record != null) {
            switch (record.opType()) {
              case PUT_BYTES:
                put(record);
                break;
              case DELETE_BYTES:
                delete(record);
                break;
              default:
                throw new IllegalArgumentException(
                    String.format("Not support this OpType %s", record.opType()));
            }
          }
        }
      } catch (Throwable e) {
        LOG.error("writing failed:", e);
        writingThreadException.set(e);
      }
      LOG.info("{} stopping.", name);
    }

    private void delete(LookupRecord record) {
      if (secondaryIndexMemoryMapEnabled) {
        deleteSecondaryCache(record.keyBytes(), record.valueBytes());
      } else {
        rocksDB.delete(columnFamilyName, record.keyBytes());
        // manually clear the record
        record = null;
      }
    }

    private void put(LookupRecord record) {
      if (secondaryIndexMemoryMapEnabled) {
        putSecondaryCache(record.keyBytes(), record.valueBytes());
      } else {
        rocksDB.put(columnFamilyHandle, record.keyBytes(), record.valueBytes());
        // manually clear the record
        record = null;
      }
    }
  }

  void putSecondaryCache(byte[] key, byte[] value) {
    ByteArrayWrapper keyWrap = wrap(key);
    ByteArrayWrapper valueWrap = wrap(value);
    putCacheValue(guavaCache, keyWrap, valueWrap);
  }

  void deleteSecondaryCache(byte[] key, byte[] value) {
    ByteArrayWrapper keyWrap = wrap(key);
    ByteArrayWrapper valueWrap = wrap(value);
    removeValue(guavaCache, keyWrap, valueWrap);
  }

  void putCacheValue(
      Cache<ByteArrayWrapper, V> cache, ByteArrayWrapper keyWrap, ByteArrayWrapper valueWrap) {}

  void removeValue(
      Cache<ByteArrayWrapper, V> cache, ByteArrayWrapper keyWrap, ByteArrayWrapper valueWrap) {}
}

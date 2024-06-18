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

package org.apache.amoro.utils.map;

import org.apache.amoro.AmoroIOException;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.utils.LocalFileUtil;
import org.apache.amoro.utils.SerializationUtil;
import org.rocksdb.AbstractImmutableNativeReference;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.MutableColumnFamilyOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Statistics;
import org.rocksdb.TtlDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RocksDBBackend {
  private static final Logger LOG = LoggerFactory.getLogger(RocksDBBackend.class);
  private static final String BACKEND_BASE_DIR = System.getProperty("java.io.tmpdir");
  private static final ThreadLocal<RocksDBBackend> instance = new ThreadLocal<>();

  public static RocksDBBackend getOrCreateInstance() {
    Preconditions.checkNotNull(BACKEND_BASE_DIR, "The default rocksdb path is null.");
    return getOrCreateInstance(BACKEND_BASE_DIR);
  }

  public static RocksDBBackend getOrCreateInstance(@Nullable String backendBaseDir) {
    if (backendBaseDir == null) {
      Preconditions.checkNotNull(BACKEND_BASE_DIR, "The default rocksdb path is null.");
      return getOrCreateInstance(BACKEND_BASE_DIR);
    }
    return createIfAbsent(backendBaseDir, null);
  }

  public static RocksDBBackend getOrCreateInstance(
      @Nullable String backendBaseDir, @Nullable Integer ttlSeconds) {
    if (backendBaseDir == null) {
      Preconditions.checkNotNull(BACKEND_BASE_DIR, "The default rocksdb path is null.");
      return getOrCreateInstance(BACKEND_BASE_DIR, ttlSeconds);
    }
    return createIfAbsent(backendBaseDir, ttlSeconds);
  }

  private final Map<String, ColumnFamilyHandle> handleMap = new HashMap<>();
  private final Map<String, ColumnFamilyDescriptor> descriptorMap = new HashMap<>();
  private RocksDB rocksDB;
  private boolean closed = false;
  private final String rocksDBBasePath;
  private long totalBytesWritten;

  private static RocksDBBackend createIfAbsent(
      @Nullable String backendBaseDir, @Nullable Integer ttlSeconds) {
    RocksDBBackend backend = instance.get();
    if (backend == null) {
      backend = new RocksDBBackend(backendBaseDir, ttlSeconds);
      instance.set(backend);
    }
    if (backend.closed) {
      backend = new RocksDBBackend(backendBaseDir, ttlSeconds);
      instance.set(backend);
    }
    return backend;
  }

  private RocksDBBackend(@Nullable String backendBaseDir, @Nullable Integer ttlSeconds) {
    this.rocksDBBasePath =
        backendBaseDir == null
            ? UUID.randomUUID().toString()
            : String.format("%s/%s", backendBaseDir, UUID.randomUUID());
    totalBytesWritten = 0L;
    setup(ttlSeconds);
  }

  /** Initialized Rocks DB instance. */
  private void setup(@Nullable Integer ttlSeconds) {
    try {
      LOG.info("DELETING RocksDB instance persisted at {}", rocksDBBasePath);
      LocalFileUtil.deleteDirectory(new File(rocksDBBasePath));

      final DBOptions dbOptions =
          new DBOptions()
              .setCreateIfMissing(true)
              .setCreateMissingColumnFamilies(true)
              .setWalDir(rocksDBBasePath)
              .setStatsDumpPeriodSec(300)
              .setStatistics(new Statistics());
      dbOptions.setLogger(
          new org.rocksdb.Logger(dbOptions) {
            @Override
            protected void log(InfoLogLevel infoLogLevel, String logMsg) {
              LOG.debug("From Rocks DB : {}", logMsg);
            }
          });
      List<ColumnFamilyDescriptor> managedColumnFamilies;
      List<ColumnFamilyHandle> managedHandles = new ArrayList<>();
      LocalFileUtil.mkdir(new File(rocksDBBasePath));

      if (ttlSeconds != null && ttlSeconds > 0) {
        Options ttlDBOptions = new Options(dbOptions, new ColumnFamilyOptions());
        rocksDB = TtlDB.open(ttlDBOptions, rocksDBBasePath, ttlSeconds, false);
        managedColumnFamilies = new ArrayList<>();
      } else {
        managedColumnFamilies = loadManagedColumnFamilies(dbOptions);
        rocksDB = RocksDB.open(dbOptions, rocksDBBasePath, managedColumnFamilies, managedHandles);
      }

      Preconditions.checkArgument(
          managedHandles.size() == managedColumnFamilies.size(),
          "Unexpected number of handles are returned");
      for (int index = 0; index < managedHandles.size(); index++) {
        ColumnFamilyHandle handle = managedHandles.get(index);
        ColumnFamilyDescriptor descriptor = managedColumnFamilies.get(index);
        String familyNameFromHandle = new String(handle.getName());
        String familyNameFromDescriptor = new String(descriptor.getName());

        Preconditions.checkArgument(
            familyNameFromDescriptor.equals(familyNameFromHandle),
            "Family Handles not in order with descriptors");
        handleMap.put(familyNameFromHandle, handle);
        descriptorMap.put(familyNameFromDescriptor, descriptor);
      }
      addShutDownHook();
    } catch (RocksDBException | IOException re) {
      LOG.error("Got exception opening Rocks DB instance ", re);
      if (rocksDB != null) {
        close();
      }
      throw new AmoroIOException(re);
    }
  }

  private void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::close));
  }

  /** Helper to load managed column family descriptors. */
  private List<ColumnFamilyDescriptor> loadManagedColumnFamilies(DBOptions dbOptions)
      throws RocksDBException {
    final List<ColumnFamilyDescriptor> managedColumnFamilies = new ArrayList<>();
    final Options options = new Options(dbOptions, new ColumnFamilyOptions());
    List<byte[]> existing = RocksDB.listColumnFamilies(options, rocksDBBasePath);

    if (existing.isEmpty()) {
      LOG.info("No column family found. Loading default");
      managedColumnFamilies.add(getColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY));
    } else {
      LOG.info(
          "Loading column families :"
              + existing.stream().map(String::new).collect(Collectors.toList()));
      managedColumnFamilies.addAll(
          existing.stream().map(this::getColumnFamilyDescriptor).collect(Collectors.toList()));
    }
    return managedColumnFamilies;
  }

  private ColumnFamilyDescriptor getColumnFamilyDescriptor(byte[] columnFamilyName) {
    return new ColumnFamilyDescriptor(columnFamilyName, new ColumnFamilyOptions());
  }

  private ColumnFamilyDescriptor getColumnFamilyDescriptor(
      byte[] columnFamilyName, ColumnFamilyOptions columnFamilyOptions) {
    return new ColumnFamilyDescriptor(columnFamilyName, columnFamilyOptions);
  }

  /**
   * Perform single PUT on a column-family.
   *
   * @param columnFamilyName Column family name
   * @param key Key
   * @param value Payload
   */
  @VisibleForTesting
  public <K extends Serializable, T extends Serializable> void put(
      String columnFamilyName, K key, T value) {
    try {
      Preconditions.checkArgument(
          key != null && value != null, "values or keys in rocksdb can not be null!");
      byte[] payload = serializePayload(value);
      rocksDB.put(handleMap.get(columnFamilyName), SerializationUtil.kryoSerialize(key), payload);
    } catch (Exception e) {
      throw new AmoroIOException(e);
    }
  }

  /**
   * Perform single PUT on a column-family.
   *
   * @param columnFamilyName Column family name
   * @param key Key
   * @param value Payload
   */
  public void put(String columnFamilyName, byte[] key, byte[] value) {
    try {
      Preconditions.checkArgument(
          key != null && value != null, "values or keys in rocksdb can not be null!");
      ColumnFamilyHandle cfHandler = handleMap.get(columnFamilyName);
      Preconditions.checkArgument(
          cfHandler != null, "column family " + columnFamilyName + " does not exists in rocksdb");
      rocksDB.put(cfHandler, key, payload(value));
    } catch (Exception e) {
      throw new AmoroIOException(e);
    }
  }

  public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) {
    try {
      Preconditions.checkArgument(
          key != null && value != null, "values or keys in rocksdb can not be null!");
      Preconditions.checkArgument(
          columnFamilyHandle != null, "Column family handler couldn't be null.");
      rocksDB.put(columnFamilyHandle, key, payload(value));
    } catch (Exception e) {
      throw new AmoroIOException(e);
    }
  }

  /**
   * Perform a single Delete operation.
   *
   * @param columnFamilyName Column Family name
   * @param key Key to be deleted
   */
  @VisibleForTesting
  public <K extends Serializable> void delete(String columnFamilyName, K key) {
    try {
      Preconditions.checkArgument(key != null, "keys in rocksdb can not be null!");
      rocksDB.delete(handleMap.get(columnFamilyName), SerializationUtil.kryoSerialize(key));
    } catch (Exception e) {
      throw new AmoroIOException(e);
    }
  }

  /**
   * Perform a single Delete operation.
   *
   * @param columnFamilyName Column Family name
   * @param key Key to be deleted
   */
  public void delete(String columnFamilyName, byte[] key) {
    try {
      Preconditions.checkArgument(key != null, "keys in rocksdb can not be null!");
      rocksDB.delete(handleMap.get(columnFamilyName), key);
    } catch (Exception e) {
      throw new AmoroIOException(e);
    }
  }

  /**
   * Retrieve a value for a given key in a column family.
   *
   * @param columnFamilyName Column Family Name
   * @param key Key to be retrieved
   */
  @VisibleForTesting
  public <K extends Serializable, T extends Serializable> T get(String columnFamilyName, K key) {
    Preconditions.checkArgument(!closed);
    try {
      Preconditions.checkArgument(key != null, "keys in rocksdb can not be null!");
      byte[] val =
          rocksDB.get(handleMap.get(columnFamilyName), SerializationUtil.kryoSerialize(key));
      return val == null ? null : SerializationUtil.kryoDeserialize(val);
    } catch (Exception e) {
      throw new AmoroIOException(e);
    }
  }

  /**
   * Retrieve a value for a given key in a column family.
   *
   * @param columnFamilyName Column Family Name
   * @param key Key to be retrieved
   */
  public byte[] get(String columnFamilyName, byte[] key) {
    Preconditions.checkArgument(!closed);
    try {
      Preconditions.checkArgument(key != null, "keys in rocksdb can not be null!");
      return rocksDB.get(handleMap.get(columnFamilyName), key);
    } catch (Exception e) {
      throw new AmoroIOException(e);
    }
  }

  public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
    Preconditions.checkArgument(!closed);
    try {
      Preconditions.checkArgument(key != null, "keys in rocksdb can not be null!");
      Preconditions.checkNotNull(columnFamilyHandle, "Column Family Handle couldn't be null!");
      return rocksDB.get(columnFamilyHandle, key);
    } catch (Exception e) {
      throw new AmoroIOException(e);
    }
  }

  public RocksDB getDB() {
    return rocksDB;
  }

  /**
   * Return Iterator of key-value pairs from RocksIterator.
   *
   * @param columnFamilyName Column Family Name
   */
  @VisibleForTesting
  public <T extends Serializable> Iterator<T> valuesForTest(String columnFamilyName) {
    return new ValueIteratorForTest<>(rocksDB.newIterator(handleMap.get(columnFamilyName)));
  }

  /**
   * Return Iterator of key-value pairs from RocksIterator.
   *
   * @param columnFamilyName Column Family Name
   */
  public Iterator<byte[]> values(String columnFamilyName) {
    return new ValueIterator(rocksDB.newIterator(handleMap.get(columnFamilyName)));
  }

  /**
   * Return Iterator of key-value pairs from RocksIterator.
   *
   * @param columnFamilyName Column Family Name
   * @param seekOffset byte array describing a key or a key prefix to seek for
   */
  public Iterator<byte[]> values(String columnFamilyName, byte[] seekOffset) {
    return new ValueIterator(rocksDB.newIterator(handleMap.get(columnFamilyName)), seekOffset);
  }

  /**
   * Add a new column family to store.
   *
   * @param columnFamilyName Column family name
   */
  public void addColumnFamily(String columnFamilyName) {
    addColumnFamily(columnFamilyName, new ColumnFamilyOptions());
  }

  public void addColumnFamily(String columnFamilyName, ColumnFamilyOptions columnFamilyOptions) {
    Preconditions.checkArgument(!closed);

    descriptorMap.computeIfAbsent(
        columnFamilyName,
        colFamilyName -> {
          try {
            ColumnFamilyDescriptor descriptor =
                getColumnFamilyDescriptor(colFamilyName.getBytes(), columnFamilyOptions);
            ColumnFamilyHandle handle = rocksDB.createColumnFamily(descriptor);
            handleMap.put(colFamilyName, handle);
            return descriptor;
          } catch (RocksDBException e) {
            throw new AmoroIOException(e);
          }
        });
  }

  /**
   * Note : Does not delete from underlying DB. Just closes the handle.
   *
   * @param columnFamilyName Column Family Name
   */
  public void dropColumnFamily(String columnFamilyName) {
    Preconditions.checkArgument(!closed);

    descriptorMap.computeIfPresent(
        columnFamilyName,
        (colFamilyName, descriptor) -> {
          ColumnFamilyHandle handle = handleMap.get(colFamilyName);
          try {
            rocksDB.dropColumnFamily(handle);
            handle.close();
          } catch (RocksDBException e) {
            throw new AmoroIOException(e);
          }
          handleMap.remove(columnFamilyName);
          return null;
        });
  }

  public List<ColumnFamilyDescriptor> listColumnFamilies() {
    return new ArrayList<>(descriptorMap.values());
  }

  public ColumnFamilyHandle getColumnFamilyHandle(String columnFamilyName) {
    return handleMap.get(columnFamilyName);
  }

  /** Close the DAO object. */
  public void close() {
    if (!closed) {
      closed = true;
      handleMap.values().forEach(AbstractImmutableNativeReference::close);
      handleMap.clear();
      descriptorMap.clear();
      rocksDB.close();
      try {
        LocalFileUtil.deleteDirectory(new File(rocksDBBasePath));
      } catch (IOException e) {
        throw new AmoroIOException(e.getMessage(), e);
      }
    }
  }

  public String getRocksDBBasePath() {
    return rocksDBBasePath;
  }

  public long getTotalBytesWritten() {
    return totalBytesWritten;
  }

  private byte[] serializePayload(Object value) throws IOException {
    byte[] payload = SerializationUtil.kryoSerialize(value);
    totalBytesWritten += payload.length;
    return payload;
  }

  private byte[] payload(byte[] value) {
    totalBytesWritten += value.length;
    return value;
  }

  public void setOptions(
      ColumnFamilyHandle columnFamilyHandle,
      MutableColumnFamilyOptions mutableColumnFamilyOptions) {
    Preconditions.checkNotNull(columnFamilyHandle);
    Preconditions.checkNotNull(mutableColumnFamilyOptions);
    try {
      rocksDB.setOptions(columnFamilyHandle, mutableColumnFamilyOptions);
    } catch (RocksDBException e) {
      throw new AmoroIOException(e);
    }
  }

  /** {@link Iterator} wrapper for RocksDb Iterator {@link RocksIterator}. */
  private static class ValueIteratorForTest<R> implements Iterator<R> {

    private final RocksIterator iterator;

    public ValueIteratorForTest(final RocksIterator iterator) {
      this.iterator = iterator;
      iterator.seekToFirst();
    }

    @Override
    public boolean hasNext() {
      return iterator.isValid();
    }

    @Override
    public R next() {
      if (!hasNext()) {
        throw new IllegalStateException("next() called on rocksDB with no more valid entries");
      }
      R val = SerializationUtil.kryoDeserialize(iterator.value());
      iterator.next();
      return val;
    }
  }

  /** {@link Iterator} wrapper for RocksDb Iterator {@link RocksIterator}. */
  public static class ValueIterator implements Iterator<byte[]>, AutoCloseable {

    private final RocksIterator iterator;

    public ValueIterator(final RocksIterator iterator) {
      this.iterator = iterator;
      iterator.seekToFirst();
    }

    public ValueIterator(final RocksIterator iterator, byte[] seekOffset) {
      this.iterator = iterator;
      iterator.seek(seekOffset);
    }

    @Override
    public boolean hasNext() {
      return iterator.isValid();
    }

    @Override
    public byte[] next() {
      if (!hasNext()) {
        throw new IllegalStateException("next() called on rocksDB with no more valid entries");
      }
      byte[] val = iterator.value();
      iterator.next();
      return val;
    }

    public byte[] key() {
      return iterator.key();
    }

    @Override
    public void close() throws Exception {
      if (iterator != null) {
        iterator.close();
      }
    }
  }
}

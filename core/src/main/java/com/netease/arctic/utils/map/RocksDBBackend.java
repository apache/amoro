package com.netease.arctic.utils.map;

import com.netease.arctic.ArcticIOException;
import com.netease.arctic.utils.LocalFileUtils;
import org.apache.commons.lang.Validate;
import org.rocksdb.AbstractImmutableNativeReference;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final String BACKEND_BASE_DIR = System.getenv("user.dir");
  private static final ThreadLocal<RocksDBBackend> instance = new ThreadLocal<>().withInitial(() -> create());

  public static RocksDBBackend getOrCreateInstance() {
    RocksDBBackend backend = instance.get();
    if (backend.closed) {
      backend = create();
      instance.set(backend);
    }
    return backend;
  }

  private Map<String, ColumnFamilyHandle> managedHandlesMap = new HashMap<>();
  private Map<String, ColumnFamilyDescriptor> managedDescriptorMap = new HashMap<>();
  private RocksDB rocksDB;
  private boolean closed = false;
  private final String rocksDBBasePath;
  private long totalBytesWritten;

  private static RocksDBBackend create() {
    return new RocksDBBackend();
  }

  private RocksDBBackend() {
    this.rocksDBBasePath =
            String.format("%s/%s", BACKEND_BASE_DIR,  UUID.randomUUID().toString());
    totalBytesWritten = 0L;
    setup();
  }

  /**
   * Initialized Rocks DB instance.
   */
  private void setup() {
    try {
      LOG.info("DELETING RocksDB instance persisted at " + rocksDBBasePath);
      LocalFileUtils.deleteDirectory(new File(rocksDBBasePath));
      // If already present, loads the existing column-family handles

      final DBOptions dbOptions = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true)
              .setWalDir(rocksDBBasePath).setStatsDumpPeriodSec(300).setStatistics(new Statistics());
      dbOptions.setLogger(new org.rocksdb.Logger(dbOptions) {
        @Override
        protected void log(InfoLogLevel infoLogLevel, String logMsg) {
          LOG.info("From Rocks DB : " + logMsg);
        }
      });
      final List<ColumnFamilyDescriptor> managedColumnFamilies = loadManagedColumnFamilies(dbOptions);
      final List<ColumnFamilyHandle> managedHandles = new ArrayList<>();
      LocalFileUtils.mkdir(new File(rocksDBBasePath));
      rocksDB = RocksDB.open(dbOptions, rocksDBBasePath, managedColumnFamilies, managedHandles);

      Validate.isTrue(managedHandles.size() == managedColumnFamilies.size(),
              "Unexpected number of handles are returned");
      for (int index = 0; index < managedHandles.size(); index++) {
        ColumnFamilyHandle handle = managedHandles.get(index);
        ColumnFamilyDescriptor descriptor = managedColumnFamilies.get(index);
        String familyNameFromHandle = new String(handle.getName());
        String familyNameFromDescriptor = new String(descriptor.getName());

        Validate.isTrue(familyNameFromDescriptor.equals(familyNameFromHandle),
                "Family Handles not in order with descriptors");
        managedHandlesMap.put(familyNameFromHandle, handle);
        managedDescriptorMap.put(familyNameFromDescriptor, descriptor);
      }
      addShutDownHook();
    } catch (RocksDBException | IOException re) {
      LOG.error("Got exception opening Rocks DB instance ", re);
      if (rocksDB != null)
        close();
      throw new ArcticIOException(re);
    }
  }

  private void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::close));
  }

  /**
   * Helper to load managed column family descriptors.
   */
  private List<ColumnFamilyDescriptor> loadManagedColumnFamilies(DBOptions dbOptions) throws RocksDBException {
    final List<ColumnFamilyDescriptor> managedColumnFamilies = new ArrayList<>();
    final Options options = new Options(dbOptions, new ColumnFamilyOptions());
    List<byte[]> existing = RocksDB.listColumnFamilies(options, rocksDBBasePath);

    if (existing.isEmpty()) {
      LOG.info("No column family found. Loading default");
      managedColumnFamilies.add(getColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY));
    } else {
      LOG.info("Loading column families :" + existing.stream().map(String::new).collect(Collectors.toList()));
      managedColumnFamilies
              .addAll(existing.stream().map(this::getColumnFamilyDescriptor).collect(Collectors.toList()));
    }
    return managedColumnFamilies;
  }

  private ColumnFamilyDescriptor getColumnFamilyDescriptor(byte[] columnFamilyName) {
    return new ColumnFamilyDescriptor(columnFamilyName, new ColumnFamilyOptions());
  }

  /**
   * Perform single PUT on a column-family.
   *
   * @param columnFamilyName Column family name
   * @param key Key
   * @param value Payload
   * @param <T> Type of Payload
   */
  public <T extends Serializable> void put(String columnFamilyName, String key, T value) {
    try {
      Validate.isTrue(key != null && value != null,
              "values or keys in rocksdb can not be null!");
      ColumnFamilyHandle cfHandler = managedHandlesMap.get(columnFamilyName);
      Validate.isTrue(cfHandler != null, "column family "
              + columnFamilyName + " does not exists in rocksdb");
      rocksDB.put(cfHandler, key.getBytes(), serializePayload(value));
    } catch (Exception e) {
      throw new ArcticIOException(e);
    }
  }

  /**
   * Perform single PUT on a column-family.
   *
   * @param columnFamilyName Column family name
   * @param key Key
   * @param value Payload
   * @param <T> Type of Payload
   */
  public <K extends Serializable, T extends Serializable> void put(String columnFamilyName, K key, T value) {
    try {
      Validate.isTrue(key != null && value != null,
              "values or keys in rocksdb can not be null!");
      byte[] payload = serializePayload(value);
      rocksDB.put(managedHandlesMap.get(columnFamilyName), SerializationUtils.serialize(key), payload);
    } catch (Exception e) {
      throw new ArcticIOException(e);
    }
  }

  /**
   * Perform a single Delete operation.
   *
   * @param columnFamilyName Column Family name
   * @param key Key to be deleted
   */
  public void delete(String columnFamilyName, String key) {
    try {
      Validate.isTrue(key != null, "keys in rocksdb can not be null!");
      rocksDB.delete(managedHandlesMap.get(columnFamilyName), key.getBytes());
    } catch (RocksDBException e) {
      throw new ArcticIOException(e);
    }
  }

  /**
   * Perform a single Delete operation.
   *
   * @param columnFamilyName Column Family name
   * @param key Key to be deleted
   */
  public <K extends Serializable> void delete(String columnFamilyName, K key) {
    try {
      Validate.isTrue(key != null, "keys in rocksdb can not be null!");
      rocksDB.delete(managedHandlesMap.get(columnFamilyName), SerializationUtils.serialize(key));
    } catch (Exception e) {
      throw new ArcticIOException(e);
    }
  }

  /**
   * Retrieve a value for a given key in a column family.
   *
   * @param columnFamilyName Column Family Name
   * @param key Key to be retrieved
   * @param <T> Type of object stored.
   */
  public <T extends Serializable> T get(String columnFamilyName, String key) {
    Validate.isTrue(!closed);
    try {
      Validate.isTrue(key != null, "keys in rocksdb can not be null!");
      ColumnFamilyHandle cfHandler = managedHandlesMap.get(columnFamilyName);
      Validate.isTrue(cfHandler != null, "column family "
              + columnFamilyName + " does not exists in rocksdb");
      byte[] val = rocksDB.get(managedHandlesMap.get(columnFamilyName), key.getBytes());
      return val == null ? null : SerializationUtils.deserialize(val);
    } catch (RocksDBException e) {
      throw new ArcticIOException(e);
    }
  }

  /**
   * Retrieve a value for a given key in a column family.
   *
   * @param columnFamilyName Column Family Name
   * @param key Key to be retrieved
   * @param <T> Type of object stored.
   */
  public <K extends Serializable, T extends Serializable> T get(String columnFamilyName, K key) {
    Validate.isTrue(!closed);
    try {
      Validate.isTrue(key != null, "keys in rocksdb can not be null!");
      byte[] val = rocksDB.get(managedHandlesMap.get(columnFamilyName), SerializationUtils.serialize(key));
      return val == null ? null : SerializationUtils.deserialize(val);
    } catch (Exception e) {
      throw new ArcticIOException(e);
    }
  }

  /**
   * Return Iterator of key-value pairs from RocksIterator.
   *
   * @param columnFamilyName Column Family Name
   * @param <T>              Type of value stored
   */
  public <T extends Serializable> Iterator<T> iterator(String columnFamilyName) {
    return new IteratorWrapper<>(rocksDB.newIterator(managedHandlesMap.get(columnFamilyName)));
  }

  /**
   * Add a new column family to store.
   *
   * @param columnFamilyName Column family name
   */
  public void addColumnFamily(String columnFamilyName) {
    Validate.isTrue(!closed);

    managedDescriptorMap.computeIfAbsent(columnFamilyName, colFamilyName -> {
      try {
        ColumnFamilyDescriptor descriptor = getColumnFamilyDescriptor(colFamilyName.getBytes());
        ColumnFamilyHandle handle = rocksDB.createColumnFamily(descriptor);
        managedHandlesMap.put(colFamilyName, handle);
        return descriptor;
      } catch (RocksDBException e) {
        throw new ArcticIOException(e);
      }
    });
  }

  /**
   * Note : Does not delete from underlying DB. Just closes the handle.
   *
   * @param columnFamilyName Column Family Name
   */
  public void dropColumnFamily(String columnFamilyName) {
    Validate.isTrue(!closed);

    managedDescriptorMap.computeIfPresent(columnFamilyName, (colFamilyName, descriptor) -> {
      ColumnFamilyHandle handle = managedHandlesMap.get(colFamilyName);
      try {
        rocksDB.dropColumnFamily(handle);
        handle.close();
      } catch (RocksDBException e) {
        throw new ArcticIOException(e);
      }
      managedHandlesMap.remove(columnFamilyName);
      return null;
    });
  }

  public List<ColumnFamilyDescriptor> listColumnFamilies() {
    return new ArrayList<>(managedDescriptorMap.values());
  }

  /**
   * Close the DAO object.
   */
  public void close() {
    if (!closed) {
      closed = true;
      managedHandlesMap.values().forEach(AbstractImmutableNativeReference::close);
      managedHandlesMap.clear();
      managedDescriptorMap.clear();
      rocksDB.close();
      try {
        LocalFileUtils.deleteDirectory(new File(rocksDBBasePath));
      } catch (IOException e) {
        throw new ArcticIOException(e.getMessage(), e);
      }
    }
  }

  public String getRocksDBBasePath() {
    return rocksDBBasePath;
  }

  public long getTotalBytesWritten() {
    return totalBytesWritten;
  }

  private <T extends Serializable> byte[] serializePayload(T value) throws IOException {
    byte[] payload = SerializationUtils.serialize(value);
    totalBytesWritten += payload.length;
    return payload;
  }

  /**
   * {@link Iterator} wrapper for RocksDb Iterator {@link RocksIterator}.
   */
  private static class IteratorWrapper<R> implements Iterator<R> {

    private final RocksIterator iterator;

    public IteratorWrapper(final RocksIterator iterator) {
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
      R val = SerializationUtils.deserialize(iterator.value());
      iterator.next();
      return val;
    }
  }
}

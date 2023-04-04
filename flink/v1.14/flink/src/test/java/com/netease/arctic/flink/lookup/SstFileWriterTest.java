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

package com.netease.arctic.flink.lookup;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.ComparatorOptions;
import org.rocksdb.EnvOptions;
import org.rocksdb.IngestExternalFileOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.Slice;
import org.rocksdb.SstFileWriter;
import org.rocksdb.StringAppendOperator;
import org.rocksdb.util.BytewiseComparator;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SstFileWriterTest {
  private static final String SST_FILE_NAME = "test.sst";
  private static final String DB_DIRECTORY_NAME = "test_db";

  @Rule
  public TemporaryFolder parentFolder = new TemporaryFolder();

  enum OpType {PUT, PUT_BYTES, PUT_DIRECT, MERGE, MERGE_BYTES, DELETE, DELETE_BYTES}

  static class KeyValueWithOp {
    KeyValueWithOp(String key, String value, OpType opType) {
      this.key = key;
      this.value = value;
      this.opType = opType;
    }

    String getKey() {
      return key;
    }

    String getValue() {
      return value;
    }

    OpType getOpType() {
      return opType;
    }

    private final String key;
    private final String value;
    private final OpType opType;
  }

  private File newSstFile(final List<KeyValueWithOp> keyValues,
                          boolean useJavaBytewiseComparator) throws IOException, RocksDBException {
    final EnvOptions envOptions = new EnvOptions();
    final StringAppendOperator stringAppendOperator = new StringAppendOperator();
    final Options options = new Options().setMergeOperator(stringAppendOperator);
    SstFileWriter sstFileWriter = null;
    ComparatorOptions comparatorOptions = null;
    BytewiseComparator comparator = null;
    if (useJavaBytewiseComparator) {
      comparatorOptions = new ComparatorOptions();
      comparator = new BytewiseComparator(comparatorOptions);
      options.setComparator(comparator);
      sstFileWriter = new SstFileWriter(envOptions, options);
    } else {
      sstFileWriter = new SstFileWriter(envOptions, options);
    }

    final File sstFile = parentFolder.newFile(SST_FILE_NAME);
    try {
      sstFileWriter.open(sstFile.getAbsolutePath());
//      assertThat(sstFileWriter.fileSize()).isEqualTo(0);
      for (KeyValueWithOp keyValue : keyValues) {
        Slice keySlice = new Slice(keyValue.getKey());
        Slice valueSlice = new Slice(keyValue.getValue());
        byte[] keyBytes = keyValue.getKey().getBytes();
        byte[] valueBytes = keyValue.getValue().getBytes();
        ByteBuffer keyDirect = ByteBuffer.allocateDirect(keyBytes.length);
        keyDirect.put(keyBytes);
        keyDirect.flip();
        ByteBuffer valueDirect = ByteBuffer.allocateDirect(valueBytes.length);
        valueDirect.put(valueBytes);
        valueDirect.flip();
        switch (keyValue.getOpType()) {
          case PUT:
            sstFileWriter.put(keySlice, valueSlice);
            break;
          case PUT_BYTES:
            sstFileWriter.put(keyBytes, valueBytes);
            break;
//          case PUT_DIRECT:
//            sstFileWriter.put(keyDirect, valueDirect);
//            assertThat(keyDirect.position()).isEqualTo(keyBytes.length);
//            assertThat(keyDirect.limit()).isEqualTo(keyBytes.length);
//            assertThat(valueDirect.position()).isEqualTo(valueBytes.length);
//            assertThat(valueDirect.limit()).isEqualTo(valueBytes.length);
//          break;
          case MERGE:
            sstFileWriter.merge(keySlice, valueSlice);
            break;
          case MERGE_BYTES:
            sstFileWriter.merge(keyBytes, valueBytes);
            break;
          case DELETE:
            sstFileWriter.delete(keySlice);
            break;
          case DELETE_BYTES:
            sstFileWriter.delete(keyBytes);
            break;
          default:
            fail("Unsupported op type");
        }
        keySlice.close();
        valueSlice.close();
      }
      sstFileWriter.finish();
//      assertThat(sstFileWriter.fileSize()).isGreaterThan(100);
    } finally {
      assertThat(sstFileWriter).isNotNull();
      sstFileWriter.close();
      options.close();
      envOptions.close();
      if (comparatorOptions != null) {
        comparatorOptions.close();
      }
      if (comparator != null) {
        comparator.close();
      }
    }
    return sstFile;
  }

  @Test
  public void generateSstFileWithJavaComparator()
      throws RocksDBException, IOException {
    final List<KeyValueWithOp> keyValues = new ArrayList<>();
    keyValues.add(new KeyValueWithOp("key1", "value1", OpType.PUT));
    keyValues.add(new KeyValueWithOp("key2", "value2", OpType.PUT));
    keyValues.add(new KeyValueWithOp("key3", "value3", OpType.MERGE));
    keyValues.add(new KeyValueWithOp("key4", "value4", OpType.MERGE));
    keyValues.add(new KeyValueWithOp("key5", "", OpType.DELETE));

    newSstFile(keyValues, true);
  }

  @Test
  public void generateSstFileWithNativeComparator()
      throws RocksDBException, IOException {
    final List<KeyValueWithOp> keyValues = new ArrayList<>();
    keyValues.add(new KeyValueWithOp("key1", "value1", OpType.PUT));
    keyValues.add(new KeyValueWithOp("key2", "value2", OpType.PUT));
    keyValues.add(new KeyValueWithOp("key3", "value3", OpType.MERGE));
    keyValues.add(new KeyValueWithOp("key4", "value4", OpType.MERGE));
    keyValues.add(new KeyValueWithOp("key5", "", OpType.DELETE));

    newSstFile(keyValues, false);
  }

  @Test
  public void ingestSstFile() throws RocksDBException, IOException {
    final List<KeyValueWithOp> keyValues = new ArrayList<>();
    keyValues.add(new KeyValueWithOp("key1", "value1", OpType.PUT));
    keyValues.add(new KeyValueWithOp("key2", "value2", OpType.PUT_DIRECT));
    keyValues.add(new KeyValueWithOp("key3", "value3", OpType.PUT_BYTES));
    keyValues.add(new KeyValueWithOp("key4", "value4", OpType.MERGE));
    keyValues.add(new KeyValueWithOp("key5", "value5", OpType.MERGE_BYTES));
    keyValues.add(new KeyValueWithOp("key6", "", OpType.DELETE));
    keyValues.add(new KeyValueWithOp("key7", "", OpType.DELETE));


    final File sstFile = newSstFile(keyValues, false);
    final File dbFolder = parentFolder.newFolder(DB_DIRECTORY_NAME);
    try (final StringAppendOperator stringAppendOperator =
             new StringAppendOperator();
         final Options options = new Options()
             .setCreateIfMissing(true)
             .setMergeOperator(stringAppendOperator);
         final RocksDB db = RocksDB.open(options, dbFolder.getAbsolutePath());
         final IngestExternalFileOptions ingestExternalFileOptions =
             new IngestExternalFileOptions()) {
      db.ingestExternalFile(Arrays.asList(sstFile.getAbsolutePath()),
          ingestExternalFileOptions);

      assertThat(db.get("key1".getBytes())).isEqualTo("value1".getBytes());
      assertThat(db.get("key2".getBytes())).isEqualTo("value2".getBytes());
      assertThat(db.get("key3".getBytes())).isEqualTo("value3".getBytes());
      assertThat(db.get("key4".getBytes())).isEqualTo("value4".getBytes());
      assertThat(db.get("key5".getBytes())).isEqualTo("value5".getBytes());
      assertThat(db.get("key6".getBytes())).isEqualTo(null);
      assertThat(db.get("key7".getBytes())).isEqualTo(null);
    }
  }

  @Test
  public void ingestSstFile_cf() throws RocksDBException, IOException {
    final List<KeyValueWithOp> keyValues = new ArrayList<>();
    keyValues.add(new KeyValueWithOp("key1", "value1", OpType.PUT));
    keyValues.add(new KeyValueWithOp("key2", "value2", OpType.PUT));
    keyValues.add(new KeyValueWithOp("key3", "value3", OpType.MERGE));
    keyValues.add(new KeyValueWithOp("key4", "", OpType.DELETE));

    final File sstFile = newSstFile(keyValues, false);
    final File dbFolder = parentFolder.newFolder(DB_DIRECTORY_NAME);
    try (final StringAppendOperator stringAppendOperator =
             new StringAppendOperator();
         final Options options = new Options()
             .setCreateIfMissing(true)
             .setCreateMissingColumnFamilies(true)
             .setMergeOperator(stringAppendOperator);
         final RocksDB db = RocksDB.open(options, dbFolder.getAbsolutePath());
         final IngestExternalFileOptions ingestExternalFileOptions =
             new IngestExternalFileOptions()) {

      try (final ColumnFamilyOptions cf_opts = new ColumnFamilyOptions()
          .setMergeOperator(stringAppendOperator);
           final ColumnFamilyHandle cf_handle = db.createColumnFamily(
               new ColumnFamilyDescriptor("new_cf".getBytes(), cf_opts))) {

        db.ingestExternalFile(cf_handle,
            Arrays.asList(sstFile.getAbsolutePath()),
            ingestExternalFileOptions);

        assertThat(db.get(cf_handle,
            "key1".getBytes())).isEqualTo("value1".getBytes());
        assertThat(db.get(cf_handle,
            "key2".getBytes())).isEqualTo("value2".getBytes());
        assertThat(db.get(cf_handle,
            "key3".getBytes())).isEqualTo("value3".getBytes());
        assertThat(db.get(cf_handle,
            "key4".getBytes())).isEqualTo(null);
      }
    }
  }
}
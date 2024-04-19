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
import org.apache.flink.util.Preconditions;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowDataStateFactory {
  private static final Logger LOG = LoggerFactory.getLogger(RowDataStateFactory.class);

  private final String dbPath;
  private RocksDBBackend db;
  private final MetricGroup metricGroup;

  public RowDataStateFactory(String dbPath, MetricGroup metricGroup) {
    Preconditions.checkNotNull(metricGroup);
    this.dbPath = dbPath;
    this.metricGroup = metricGroup;
  }

  public RocksDBRecordState createRecordState(
      String columnFamilyName,
      BinaryRowDataSerializerWrapper keySerializer,
      BinaryRowDataSerializerWrapper valueSerializer,
      LookupOptions lookupOptions) {
    db = createDB(lookupOptions, columnFamilyName);

    return new RocksDBRecordState(
        db, columnFamilyName, keySerializer, valueSerializer, metricGroup, lookupOptions);
  }

  public RocksDBSetSpilledState createSetState(
      String columnFamilyName,
      BinaryRowDataSerializerWrapper keySerialization,
      BinaryRowDataSerializerWrapper elementSerialization,
      BinaryRowDataSerializerWrapper valueSerializer,
      LookupOptions lookupOptions) {
    db = createDB(lookupOptions, columnFamilyName);

    return new RocksDBSetSpilledState(
        db,
        columnFamilyName,
        keySerialization,
        elementSerialization,
        valueSerializer,
        metricGroup,
        lookupOptions);
  }

  RocksDBBackend createDB(final LookupOptions lookupOptions, final String columnFamilyName) {
    if (lookupOptions.isTTLAfterWriteValidated()) {
      db =
          RocksDBBackend.getOrCreateInstance(
              dbPath, (int) lookupOptions.ttlAfterWrite().getSeconds());
    } else {
      db = RocksDBBackend.getOrCreateInstance(dbPath);
    }
    ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions();
    configColumnFamilyOption(columnFamilyOptions, lookupOptions);
    db.addColumnFamily(columnFamilyName, columnFamilyOptions);
    return db;
  }

  private void configColumnFamilyOption(
      ColumnFamilyOptions columnFamilyOptions, LookupOptions lookupOptions) {
    columnFamilyOptions.setDisableAutoCompactions(true);

    BlockBasedTableConfig blockBasedTableConfig = new BlockBasedTableConfig();
    blockBasedTableConfig.setBlockCache(
        new LRUCache(lookupOptions.blockCacheCapacity(), lookupOptions.numShardBits()));
    columnFamilyOptions.setTableFormatConfig(blockBasedTableConfig);

    LOG.info("set db options[disable_auto_compactions={}]", true);
    LOG.info("{}", lookupOptions);
  }
}

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

import com.netease.arctic.utils.map.RocksDBBackend;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.util.Preconditions;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateFactory {
  private static final Logger LOG = LoggerFactory.getLogger(StateFactory.class);

  private final RocksDBBackend db;
  private final MetricGroup metricGroup;

  public StateFactory(String dbPath, MetricGroup metricGroup) {
    Preconditions.checkNotNull(metricGroup);

    this.db = RocksDBBackend.getOrCreateInstance(dbPath);
    this.metricGroup = metricGroup;
  }

  public RocksDBRecordState createRecordState(
      String columnFamilyName,
      BinaryRowDataSerializerWrapper keySerializer,
      BinaryRowDataSerializerWrapper valueSerializer,
      LookupOptions lookupOptions) {
    ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions();
    configColumnFamilyOption(columnFamilyOptions);

    db.addColumnFamily(columnFamilyName, columnFamilyOptions);
    return
        new RocksDBRecordState(
            db,
            columnFamilyName,
            keySerializer,
            valueSerializer,
            metricGroup,
            lookupOptions);
  }

  public RocksDBSetMemoryState createSetState(
      String columnFamilyName,
      BinaryRowDataSerializerWrapper keySerialization,
      BinaryRowDataSerializerWrapper elementSerialization,
      BinaryRowDataSerializerWrapper valueSerializer,
      LookupOptions lookupOptions) {
    ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions();
    configColumnFamilyOption(columnFamilyOptions);
    columnFamilyOptions.setCompressionType(CompressionType.NO_COMPRESSION);

    db.addColumnFamily(columnFamilyName, columnFamilyOptions);

    return new RocksDBSetMemoryState(
        db,
        columnFamilyName,
        keySerialization,
        elementSerialization,
        valueSerializer,
        metricGroup,
        lookupOptions);
  }


  private void configColumnFamilyOption(ColumnFamilyOptions columnFamilyOptions) {
    columnFamilyOptions.setDisableAutoCompactions(true);
    LOG.info("set db options[disable_auto_compactions={}]", true);
  }
}

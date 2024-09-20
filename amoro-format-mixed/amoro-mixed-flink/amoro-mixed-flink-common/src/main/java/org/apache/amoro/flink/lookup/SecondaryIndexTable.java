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

import static org.apache.amoro.flink.lookup.LookupMetrics.SECONDARY_CACHE_SIZE;

import org.apache.flink.table.data.RowData;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Use secondary index to lookup. Working for the situation where the join keys don't match the
 * mixed-format table's primary keys.
 *
 * <p>Example: <code>
 * SELECT * FROM t1 JOIN t2 for system_time as of t1.pt as dim ON t1.user_name = dim.user_name
 * </code>
 *
 * <p>t2 as an mixed-format table with primary keys: user_name, city_name.
 */
public class SecondaryIndexTable extends UniqueIndexTable {
  private static final Logger LOG = LoggerFactory.getLogger(SecondaryIndexTable.class);
  private static final long serialVersionUID = 8707586070315884365L;
  private final int[] secondaryKeyIndexMapping;
  private final RocksDBSetSpilledState setState;

  private final LookupOptions lookupOptions;

  public SecondaryIndexTable(
      RowDataStateFactory rowDataStateFactory,
      List<String> primaryKeys,
      List<String> joinKeys,
      Schema projectSchema,
      LookupOptions lookupOptions,
      Predicate<RowData> rowDataPredicate) {
    super(rowDataStateFactory, primaryKeys, projectSchema, lookupOptions, rowDataPredicate);

    this.setState =
        rowDataStateFactory.createSetState(
            "secondaryIndex",
            createKeySerializer(projectSchema, joinKeys),
            createKeySerializer(projectSchema, primaryKeys),
            createValueSerializer(projectSchema),
            lookupOptions);

    List<String> fields =
        projectSchema.asStruct().fields().stream()
            .map(Types.NestedField::name)
            .collect(Collectors.toList());
    secondaryKeyIndexMapping = joinKeys.stream().mapToInt(fields::indexOf).toArray();
    this.lookupOptions = lookupOptions;
  }

  @Override
  public void open() {
    super.open();
    setState.open();
    setState.addGauge(SECONDARY_CACHE_SIZE, () -> setState.guavaCache.size());
  }

  @Override
  public List<RowData> get(RowData key) throws IOException {
    Collection<ByteArrayWrapper> uniqueKeys = setState.get(key);
    if (!uniqueKeys.isEmpty()) {
      List<RowData> result = new ArrayList<>(uniqueKeys.size());
      for (ByteArrayWrapper uniqueKey : uniqueKeys) {
        recordState.get(uniqueKey.bytes).ifPresent(result::add);
      }
      return result;
    }
    return Collections.emptyList();
  }

  @Override
  public void upsert(Iterator<RowData> dataStream) throws IOException {
    while (dataStream.hasNext()) {
      RowData value = dataStream.next();
      if (filter(value)) {
        continue;
      }
      RowData uniqueKey = new KeyRowData(uniqueKeyIndexMapping, value);
      RowData joinKey = new KeyRowData(secondaryKeyIndexMapping, value);
      byte[] uniqueKeyBytes = recordState.serializeKey(uniqueKey);

      if (value.getRowKind() == RowKind.INSERT || value.getRowKind() == RowKind.UPDATE_AFTER) {
        recordState.put(uniqueKeyBytes, value);
        setState.put(joinKey, uniqueKeyBytes);
      } else {
        recordState.delete(uniqueKeyBytes);
        setState.delete(joinKey, uniqueKeyBytes);
      }
    }
    cleanUp();
  }

  @Override
  public void initialize(Iterator<RowData> dataStream) throws IOException {
    while (dataStream.hasNext()) {
      RowData value = dataStream.next();
      if (filter(value)) {
        continue;
      }
      RowData uniqueKey = new KeyRowData(uniqueKeyIndexMapping, value);
      RowData joinKey = new KeyRowData(secondaryKeyIndexMapping, value);
      byte[] uniqueKeyBytes = recordState.serializeKey(uniqueKey);

      recordState.asyncWrite(value.getRowKind(), uniqueKeyBytes, value);
      setState.asyncWrite(joinKey, uniqueKeyBytes);
    }
    recordState.checkConcurrentFailed();
    setState.checkConcurrentFailed();
  }

  @Override
  public boolean initialized() {
    return recordState.initialized() && setState.initialized();
  }

  @Override
  public void cleanUp() {
    if (lookupOptions.isTTLAfterWriteValidated()) {
      setState.guavaCache.cleanUp();
    }
  }

  @Override
  public void waitInitializationCompleted() {
    super.waitInitializationCompleted();
    LOG.info("Waiting for Set State initialization");
    setState.waitWriteRocksDBDone();
    LOG.info("Queue is empty row, try to bulk tmp map into rocksdb");
    setState.bulkIntoRocksDB();
    LOG.info("The concurrent threads have finished writing data into the Set State.");
    setState.initializationCompleted();
  }

  @Override
  public void close() {
    super.close();
    recordState.close();
  }
}

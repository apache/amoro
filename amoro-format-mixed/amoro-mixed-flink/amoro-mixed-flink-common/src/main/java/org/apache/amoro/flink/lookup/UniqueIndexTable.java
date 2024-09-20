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

import static org.apache.amoro.flink.lookup.LookupMetrics.UNIQUE_CACHE_SIZE;

import org.apache.amoro.utils.SchemaUtil;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Use a unique index to lookup. Working for the situation where the join keys include the
 * mixed-format table's primary keys.
 */
public class UniqueIndexTable implements KVTable<RowData> {
  private static final Logger LOG = LoggerFactory.getLogger(UniqueIndexTable.class);
  private static final long serialVersionUID = -6537777722200330050L;
  protected final RocksDBRecordState recordState;

  protected int[] uniqueKeyIndexMapping;
  protected final Predicate<RowData> rowDataPredicate;

  public UniqueIndexTable(
      RowDataStateFactory rowDataStateFactory,
      List<String> primaryKeys,
      Schema projectSchema,
      LookupOptions lookupOptions,
      Predicate<RowData> rowDataPredicate) {

    recordState =
        rowDataStateFactory.createRecordState(
            "uniqueIndex",
            createKeySerializer(projectSchema, primaryKeys),
            createValueSerializer(projectSchema),
            lookupOptions);
    List<String> fields =
        projectSchema.asStruct().fields().stream()
            .map(Types.NestedField::name)
            .collect(Collectors.toList());
    this.uniqueKeyIndexMapping = primaryKeys.stream().mapToInt(fields::indexOf).toArray();
    this.rowDataPredicate = rowDataPredicate;
  }

  @Override
  public void open() {
    recordState.open();
    recordState.addGauge(UNIQUE_CACHE_SIZE, () -> recordState.guavaCache.size());
  }

  @Override
  public List<RowData> get(RowData key) throws IOException {
    Optional<RowData> record = recordState.get(key);
    return record.map(Collections::singletonList).orElse(Collections.emptyList());
  }

  @Override
  public void upsert(Iterator<RowData> dataStream) throws IOException {
    while (dataStream.hasNext()) {
      RowData value = dataStream.next();
      if (filter(value)) {
        continue;
      }
      RowData key = new KeyRowData(uniqueKeyIndexMapping, value);

      if (value.getRowKind() == RowKind.INSERT || value.getRowKind() == RowKind.UPDATE_AFTER) {
        recordState.put(key, value);
      } else {
        recordState.delete(key);
      }
    }
  }

  @Override
  public void initialize(Iterator<RowData> dataStream) throws IOException {
    while (dataStream.hasNext()) {
      RowData value = dataStream.next();
      if (filter(value)) {
        continue;
      }

      RowData key = new KeyRowData(uniqueKeyIndexMapping, value);
      recordState.asyncWrite(key, value);
    }
    recordState.checkConcurrentFailed();
  }

  @Override
  public boolean filter(RowData value) {
    return predicate(value);
  }

  protected boolean predicate(RowData value) {
    return Optional.ofNullable(rowDataPredicate)
        .map(predicate -> !predicate.test(value))
        .orElse(false);
  }

  @Override
  public boolean initialized() {
    return recordState.initialized();
  }

  @Override
  public void waitInitializationCompleted() {
    LOG.info("Waiting for Record State initialization");
    recordState.waitWriteRocksDBDone();
    LOG.info("The concurrent threads have finished writing data into the Record State.");
    recordState.initializationCompleted();
  }

  protected BinaryRowDataSerializerWrapper createKeySerializer(
      Schema mixedTableSchema, List<String> keys) {
    Schema keySchema = SchemaUtil.selectInOrder(mixedTableSchema, keys);
    return new BinaryRowDataSerializerWrapper(keySchema);
  }

  protected BinaryRowDataSerializerWrapper createValueSerializer(Schema projectSchema) {
    return new BinaryRowDataSerializerWrapper(projectSchema);
  }

  @Override
  public void close() {
    recordState.close();
  }
}

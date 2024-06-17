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

package org.apache.amoro;

import static org.apache.amoro.table.TableProperties.BASE_FILE_FORMAT;
import static org.apache.amoro.table.TableProperties.CHANGE_FILE_FORMAT;
import static org.apache.amoro.table.TableProperties.DEFAULT_FILE_FORMAT;
import static org.apache.amoro.table.TableProperties.DEFAULT_FILE_FORMAT_DEFAULT;

import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.types.Types;

import java.util.List;
import java.util.Map;

public class BasicTableTestHelper implements TableTestHelper {

  public static final Schema TABLE_SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "name", Types.StringType.get()),
          Types.NestedField.required(3, "ts", Types.LongType.get()),
          Types.NestedField.required(4, "op_time", Types.TimestampType.withoutZone()));

  public static final PartitionSpec SPEC =
      PartitionSpec.builderFor(TABLE_SCHEMA).day("op_time").build();

  public static final PrimaryKeySpec PRIMARY_KEY_SPEC =
      PrimaryKeySpec.builderFor(TABLE_SCHEMA).addColumn("id").build();

  private final Schema tableSchema;
  private final PrimaryKeySpec primaryKeySpec;
  private final PartitionSpec partitionSpec;
  private final Map<String, String> tableProperties;

  public BasicTableTestHelper(
      Schema tableSchema,
      PrimaryKeySpec primaryKeySpec,
      PartitionSpec partitionSpec,
      Map<String, String> tableProperties) {
    this.tableSchema = tableSchema;
    this.partitionSpec = partitionSpec;
    this.primaryKeySpec = primaryKeySpec;
    this.tableProperties = Maps.newHashMap();
    if (tableProperties != null) {
      this.tableProperties.putAll(tableProperties);
    }
    this.tableProperties.put(TableProperties.FORMAT_VERSION, "2");
  }

  public BasicTableTestHelper(
      boolean hasPrimaryKey, boolean hasPartition, Map<String, String> tableProperties) {
    this(
        TABLE_SCHEMA,
        hasPrimaryKey ? PRIMARY_KEY_SPEC : PrimaryKeySpec.noPrimaryKey(),
        hasPartition ? SPEC : PartitionSpec.unpartitioned(),
        tableProperties);
  }

  public BasicTableTestHelper(boolean hasPrimaryKey, boolean hasPartition, String fileFormat) {
    this(hasPrimaryKey, hasPartition, buildTableFormat(fileFormat));
  }

  public BasicTableTestHelper(boolean hasPrimaryKey, boolean hasPartition) {
    this(hasPrimaryKey, hasPartition, DEFAULT_FILE_FORMAT_DEFAULT);
  }

  public BasicTableTestHelper(boolean hasPrimaryKey, PartitionSpec partitionSpec) {
    this(
        TABLE_SCHEMA,
        hasPrimaryKey ? PRIMARY_KEY_SPEC : PrimaryKeySpec.noPrimaryKey(),
        partitionSpec,
        buildTableFormat(DEFAULT_FILE_FORMAT_DEFAULT));
  }

  public BasicTableTestHelper(
      Schema tableSchema, boolean hasPrimaryKey, PartitionSpec partitionSpec) {
    this(
        tableSchema,
        hasPrimaryKey ? PRIMARY_KEY_SPEC : PrimaryKeySpec.noPrimaryKey(),
        partitionSpec,
        buildTableFormat(DEFAULT_FILE_FORMAT_DEFAULT));
  }

  @Override
  public Schema tableSchema() {
    return tableSchema;
  }

  @Override
  public PartitionSpec partitionSpec() {
    return partitionSpec;
  }

  @Override
  public PrimaryKeySpec primaryKeySpec() {
    return primaryKeySpec;
  }

  @Override
  public Map<String, String> tableProperties() {
    return tableProperties;
  }

  @Override
  public Record generateTestRecord(int id, String name, long ts, String opTime) {
    return MixedDataTestHelpers.createRecord(TABLE_SCHEMA, id, name, ts, opTime);
  }

  @Override
  public List<DataFile> writeChangeStore(
      KeyedTable keyedTable,
      Long txId,
      ChangeAction action,
      List<Record> records,
      boolean orderedWrite) {
    return MixedDataTestHelpers.writeChangeStore(keyedTable, txId, action, records, orderedWrite);
  }

  @Override
  public List<DataFile> writeBaseStore(
      MixedTable table, long txId, List<Record> records, boolean orderedWrite) {
    return MixedDataTestHelpers.writeBaseStore(table, txId, records, orderedWrite);
  }

  @Override
  public List<Record> readKeyedTable(
      KeyedTable keyedTable,
      Expression expression,
      Schema projectSchema,
      boolean useDiskMap,
      boolean readDeletedData) {
    return MixedDataTestHelpers.readKeyedTable(
        keyedTable, expression, projectSchema, useDiskMap, readDeletedData);
  }

  @Override
  public List<Record> readChangeStore(
      KeyedTable keyedTable, Expression expression, Schema projectSchema, boolean useDiskMap) {
    return MixedDataTestHelpers.readChangeStore(keyedTable, expression, projectSchema, useDiskMap);
  }

  @Override
  public List<Record> readBaseStore(
      MixedTable table, Expression expression, Schema projectSchema, boolean useDiskMap) {
    return MixedDataTestHelpers.readBaseStore(table, expression, projectSchema, useDiskMap);
  }

  @Override
  public String toString() {
    return String.format(
        "hasPrimaryKey = %b, hasPartitionSpec = %b",
        primaryKeySpec.primaryKeyExisted(), partitionSpec.isPartitioned());
  }

  protected static Map<String, String> buildTableFormat(String fileFormat) {
    Map<String, String> tableProperties = Maps.newHashMapWithExpectedSize(3);
    tableProperties.put(BASE_FILE_FORMAT, fileFormat);
    tableProperties.put(CHANGE_FILE_FORMAT, fileFormat);
    tableProperties.put(DEFAULT_FILE_FORMAT, fileFormat);
    return tableProperties;
  }
}

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

package com.netease.arctic.flink.write;

import com.google.common.collect.Maps;
import com.netease.arctic.flink.util.DataUtil;
import com.netease.arctic.hive.catalog.HiveTableTestBase;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDateTime;

/**
 * This is a mix_hive table writer test.
 */
@RunWith(value = Parameterized.class)
public class FlinkHiveTaskWriterBuilderTest extends HiveTableTestBase implements FlinkTaskWriterBaseTest {

  public static final Schema HIVE_TABLE_SCHEMA = new Schema(
      Types.NestedField.required(1, COLUMN_NAME_ID, Types.IntegerType.get()),
      Types.NestedField.required(2, COLUMN_NAME_OP_TIME, Types.TimestampType.withoutZone()),
      Types.NestedField.optional(3, COLUMN_NAME_OP_TIME_WITH_ZONE, Types.TimestampType.withZone()),
      Types.NestedField.optional(4, COLUMN_NAME_D, Types.DecimalType.of(10, 0)),
      Types.NestedField.required(5, COLUMN_NAME_NAME, Types.StringType.get())
  );

  public static final PrimaryKeySpec PRIMARY_KEY_SPEC = PrimaryKeySpec.builderFor(HIVE_TABLE_SCHEMA)
      .addColumn(COLUMN_NAME_ID).build();

  public FlinkHiveTaskWriterBuilderTest(
      boolean keyedTable,
      boolean partitionedTable) {
    super(
        HIVE_TABLE_SCHEMA,
        keyedTable ?
            PRIMARY_KEY_SPEC :
            PrimaryKeySpec.noPrimaryKey(),
        partitionedTable ?
            PartitionSpec.builderFor(HIVE_TABLE_SCHEMA).identity(COLUMN_NAME_NAME).build() :
            PartitionSpec.unpartitioned(),
        Maps.newHashMap());
  }

  @Parameterized.Parameters(name = "keyedTable = {0}, partitionedTable = {1}")
  public static Object[][] parameters() {
    return new Object[][]{
        {true, true},
        {true, false},
        {false, true},
        {false, false}};
  }

  @Test
  public void testPartialWriteToArctic() {
    TableSchema flinkPartialSchema = TableSchema.builder()
        .field(COLUMN_NAME_ID, DataTypes.INT())
        .field(COLUMN_NAME_OP_TIME, DataTypes.TIMESTAMP())
        .field(COLUMN_NAME_NAME, DataTypes.STRING())
        .build();
    RowData expected = DataUtil.toRowData(1000004, LocalDateTime.parse("2022-06-18T10:10:11.0"), "a");
    testWriteAndReadArcticTable(getArcticTable(), flinkPartialSchema, expected);
  }

  @Test
  public void testWriteOutOfOrderFieldsFromArctic() {
    TableSchema flinkTableSchemaOutOfOrderFields = TableSchema.builder()
        .field(COLUMN_NAME_D, DataTypes.STRING())
        .field(COLUMN_NAME_ID, DataTypes.INT())
        .field(COLUMN_NAME_OP_TIME, DataTypes.TIMESTAMP())
        .field(COLUMN_NAME_NAME, DataTypes.STRING())
        .build();
    RowData expected = DataUtil.toRowData("dd", 1000004, LocalDateTime.parse("2022-06-18T10:10:11.0"), "a");
    testWriteAndReadArcticTable(getArcticTable(), flinkTableSchemaOutOfOrderFields, expected);
  }

  @Override
  public String getMetastoreUrl() {
    return getCatalogUrl();
  }

  @Override
  public String getCatalogName() {
    return getCatalog().name();
  }
}
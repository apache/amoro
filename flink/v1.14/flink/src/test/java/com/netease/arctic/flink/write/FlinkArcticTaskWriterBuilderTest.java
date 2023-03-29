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

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.flink.util.DataUtil;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.data.RowData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDateTime;

import static com.netease.arctic.TableTestHelpers.COLUMN_NAME_ID;
import static com.netease.arctic.TableTestHelpers.COLUMN_NAME_NAME;
import static com.netease.arctic.TableTestHelpers.COLUMN_NAME_OP_TIME;
import static com.netease.arctic.TableTestHelpers.COLUMN_NAME_TS;

/**
 * This is a mix_iceberg writer test.
 */
@RunWith(value = Parameterized.class)
public class FlinkArcticTaskWriterBuilderTest extends TableTestBase implements FlinkTaskWriterBaseTest {

  public FlinkArcticTaskWriterBuilderTest(
      boolean keyedTable,
      boolean partitionedTable) {
    super(TableFormat.MIXED_ICEBERG, keyedTable, partitionedTable);
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
  public void testWriteToArcticWithPartialSchema() {
    TableSchema flinkPartialSchema = TableSchema.builder()
        .field(COLUMN_NAME_ID, DataTypes.INT())
        .field(COLUMN_NAME_NAME, DataTypes.STRING())
        .field(COLUMN_NAME_OP_TIME, DataTypes.TIMESTAMP())
        .build();
    RowData expected = DataUtil.toRowData(1000004, "a", LocalDateTime.parse("2022-06-18T10:10:11.0"));
    testWriteAndReadArcticTable(getArcticTable(), flinkPartialSchema, expected);
  }

  /**
   * The order of flink table schema fields is different from that of arctic table schema fields.
   */
  @Test
  public void testWriteToArcticWithOutOfOrderFieldsAndPK() {
    TableSchema flinkTableSchemaOutOfOrderFields = TableSchema.builder()
        .field(COLUMN_NAME_TS, DataTypes.BIGINT())
        .field(COLUMN_NAME_ID, DataTypes.INT())
        .field(COLUMN_NAME_NAME, DataTypes.STRING())
        .field(COLUMN_NAME_OP_TIME, DataTypes.TIMESTAMP())
        .build();
    RowData expected = DataUtil.toRowData(System.currentTimeMillis(), 1000004, "a", LocalDateTime.parse("2022-06-18T10:10:11.0"));
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
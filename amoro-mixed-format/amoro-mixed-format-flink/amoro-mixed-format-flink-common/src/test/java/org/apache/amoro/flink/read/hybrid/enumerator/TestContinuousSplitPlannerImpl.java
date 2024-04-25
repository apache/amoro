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

package org.apache.amoro.flink.read.hybrid.enumerator;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.flink.FlinkTestBase;
import org.apache.amoro.table.KeyedTable;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.io.TaskWriter;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class TestContinuousSplitPlannerImpl extends FlinkTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(TestContinuousSplitPlannerImpl.class);
  protected static final RowType ROW_TYPE = FlinkSchemaUtil.convert(TABLE_SCHEMA);
  protected KeyedTable testKeyedTable;

  protected static final LocalDateTime LDT =
      LocalDateTime.of(LocalDate.of(2022, 1, 1), LocalTime.of(0, 0, 0, 0));

  public TestContinuousSplitPlannerImpl(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  @Before
  public void init() throws IOException {
    testKeyedTable = getMixedTable().asKeyedTable();
    // write base
    {
      TaskWriter<RowData> taskWriter = createTaskWriter(true);
      List<RowData> baseData =
          new ArrayList<RowData>() {
            {
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      1,
                      StringData.fromString("john"),
                      LDT.toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT)));
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      2,
                      StringData.fromString("lily"),
                      LDT.toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT)));
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      3,
                      StringData.fromString("jake"),
                      LDT.plusDays(1).toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT.plusDays(1))));
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      4,
                      StringData.fromString("sam"),
                      LDT.plusDays(1).toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT.plusDays(1))));
            }
          };
      for (RowData record : baseData) {
        taskWriter.write(record);
      }
      commit(testKeyedTable, taskWriter.complete(), true);
    }

    // write change insert
    {
      TaskWriter<RowData> taskWriter = createTaskWriter(false);
      List<RowData> insert =
          new ArrayList<RowData>() {
            {
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      5,
                      StringData.fromString("mary"),
                      LDT.toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT)));
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      6,
                      StringData.fromString("mack"),
                      LDT.toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT)));
            }
          };
      for (RowData record : insert) {
        taskWriter.write(record);
      }
      commit(testKeyedTable, taskWriter.complete(), true);
    }

    // write change delete
    {
      TaskWriter<RowData> taskWriter = createTaskWriter(false);
      List<RowData> update =
          new ArrayList<RowData>() {
            {
              add(
                  GenericRowData.ofKind(
                      RowKind.DELETE,
                      5,
                      StringData.fromString("mary"),
                      LDT.toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT)));
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      5,
                      StringData.fromString("lind"),
                      LDT.toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT)));
            }
          };

      for (RowData record : update) {
        taskWriter.write(record);
      }
      commit(testKeyedTable, taskWriter.complete(), false);
    }
  }

  protected TaskWriter<RowData> createTaskWriter(boolean base) {
    return createKeyedTaskWriter(testKeyedTable, ROW_TYPE, base);
  }

  protected TaskWriter<RowData> createTaskWriter(KeyedTable keyedTable, boolean base) {
    return createKeyedTaskWriter(keyedTable, ROW_TYPE, base);
  }
}

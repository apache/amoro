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

package org.apache.amoro.server.refresh.events;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.io.writer.RecordWithAction;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class TestDefaultRefreshEvent extends AMSTableTestBase {
  public static final Schema TABLE_SCHEMA =
      new Schema(
          Lists.newArrayList(
              Types.NestedField.required(1, "id", Types.IntegerType.get()),
              Types.NestedField.required(2, "name", Types.StringType.get()),
              Types.NestedField.required(3, "ts", Types.LongType.get()),
              Types.NestedField.required(4, "op_time", Types.TimestampType.withoutZone())),
          Sets.newHashSet(1, 2, 3, 4));
  public static final PartitionSpec SPEC =
      PartitionSpec.builderFor(TABLE_SCHEMA).day("op_time").build();
  protected final boolean isIcebergTable;
  private static final DefaultRefreshEvent defaultRefreshEvent = new DefaultRefreshEvent();
  private static final Logger logger = LoggerFactory.getLogger(DefaultRefreshEvent.class);

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(TABLE_SCHEMA, true, SPEC)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(TABLE_SCHEMA, true, SPEC)
      },
    };
  }

  public TestDefaultRefreshEvent(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
    isIcebergTable = catalogTestHelper.tableFormat() == TableFormat.ICEBERG;
  }

  private void initTableWithFiles() {
    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    if (isIcebergTable) {
      try {
        write(mixedTable.asUnkeyedTable(), initRecords(1, "aaa", 0, 1, ChangeAction.INSERT));
        write(mixedTable.asUnkeyedTable(), initRecords(2, "bbb", 0, 1, ChangeAction.INSERT));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      DefaultTableRuntime runtime =
          (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
      runtime.refresh(tableService().loadTable(serverTableIdentifier()));
    }
  }

  private List<RecordWithAction> initRecords(
      int id, String name, long ts, int day, ChangeAction action) {
    ImmutableList.Builder<RecordWithAction> builder = ImmutableList.builder();
    builder.add(
        new RecordWithAction(
            MixedDataTestHelpers.createRecord(
                id, name, ts, String.format("2022-01-%02dT12:00:00", day)),
            action));

    return builder.build();
  }

  private void write(UnkeyedTable table, List<RecordWithAction> list) throws IOException {
    WriteResult result = IcebergDataTestHelpers.delta(table, list);

    RowDelta rowDelta = table.newRowDelta();
    Arrays.stream(result.dataFiles()).forEach(rowDelta::addRows);
    Arrays.stream(result.deleteFiles()).forEach(rowDelta::addDeletes);
    rowDelta.commit();
  }

  @Before
  public void prepare() {
    createDatabase();
    createTable();
    initTableWithFiles();
  }

  @After
  public void clear() {
    dropTable();
    dropDatabase();
  }

  @Test
  public void test_getIdentifier() {
    Assert.assertEquals("default", defaultRefreshEvent.getIdentifier());
  }

  @Test
  public void test_OptimizingNecessary() {
    Assume.assumeTrue("Skip non-Iceberg tests", isIcebergTable);

    // test tryEvaluatingPendingInput()
    DefaultTableRuntime runtime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    Assert.assertEquals(0, runtime.getPendingInput().getTotalFileCount());
    defaultRefreshEvent.tryEvaluatingPendingInput(
        runtime,
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable(),
        1,
        logger);
    Assert.assertTrue(runtime.getPendingInput().getTotalFileCount() > 0);
  }
}

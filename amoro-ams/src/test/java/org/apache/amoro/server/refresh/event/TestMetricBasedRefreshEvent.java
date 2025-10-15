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

package org.apache.amoro.server.refresh.event;

import static org.apache.amoro.table.TablePartitionDetailProperties.BASE_FILE_COUNT;
import static org.apache.amoro.table.TablePartitionDetailProperties.BASE_FILE_COUNT_DEFAULT;
import static org.apache.amoro.table.TablePartitionDetailProperties.EQ_DELETE_FILE_COUNT;
import static org.apache.amoro.table.TablePartitionDetailProperties.EQ_DELETE_FILE_COUNT_DEFAULT;
import static org.apache.amoro.table.TablePartitionDetailProperties.FILE_SIZE_SQUARED_ERROR_SUM;
import static org.apache.amoro.table.TablePartitionDetailProperties.FILE_SIZE_SQUARED_ERROR_SUM_DEFAULT;
import static org.apache.amoro.table.TablePartitionDetailProperties.POS_DELETE_FILE_COUNT;
import static org.apache.amoro.table.TablePartitionDetailProperties.POS_DELETE_FILE_COUNT_DEFAULT;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.io.writer.RecordWithAction;
import org.apache.amoro.optimizing.TableRuntimeOptimizingState;
import org.apache.amoro.server.dashboard.MixedAndIcebergTableDescriptor;
import org.apache.amoro.server.scheduler.inline.TableRuntimeRefreshExecutor;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TablePartitionDetailProperties;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.table.descriptor.PartitionBaseInfo;
import org.apache.amoro.utils.MemorySize;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.ThreadPools;
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
import java.util.concurrent.ExecutorService;

@RunWith(Parameterized.class)
public class TestMetricBasedRefreshEvent extends AMSTableTestBase {
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
  private static final long INTERVAL = 60000L; // 1 minute
  private static final int MAX_PENDING_PARTITIONS = 1;
  private static TableRuntimeRefreshExecutor executor;
  protected final boolean isIcebergTable;
  private static final Logger logger = LoggerFactory.getLogger(TestMetricBasedRefreshEvent.class);

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

  public TestMetricBasedRefreshEvent(
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

  public void addChangeStoreData() throws IOException {
    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    write(mixedTable.asUnkeyedTable(), initRecords(1, "aaa", 0, 1, ChangeAction.DELETE));
    write(mixedTable.asUnkeyedTable(), initRecords(2, "ccc", 0, 1, ChangeAction.UPDATE_AFTER));
    DefaultTableRuntime runtime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
  }

  @Before
  public void prepare() {
    createDatabase();
    createTable();
    initTableWithFiles();
    executor = new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS);
  }

  @After
  public void clear() {
    dropTable();
    dropDatabase();
  }

  @Test
  public void test_DefaultRefreshEvent_OptimizingNecessary() {
    Assume.assumeTrue("Skip non-Iceberg tests", isIcebergTable);

    // test tryEvaluatingPendingInput()
    DefaultTableRuntime runtime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    Assert.assertEquals(0, runtime.getPendingInput().getTotalFileCount());

    executor.execute(runtime);
    Assert.assertTrue(runtime.getPendingInput().getTotalFileCount() > 0);
  }

  private static final StateKey<TableRuntimeOptimizingState> OPTIMIZING_STATE_KEY =
      StateKey.stateKey("optimizing_state")
          .jsonType(TableRuntimeOptimizingState.class)
          .defaultValue(new TableRuntimeOptimizingState());

  @Test
  public void test_MetricBasedRefreshEvent_isEvaluatingPendingInputNecessary() {
    TableRuntime runtime = tableService().getRuntime(serverTableIdentifier().getId());

    if (!isIcebergTable) {
      Assert.assertTrue(
          MetricBasedRefreshEvent.isEvaluatingPendingInputNecessary(
              (DefaultTableRuntime) runtime,
              (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable()));
    } else {
      // Temporarily set self-optimizing.minor.trigger.interval to -1 to prevent triggering due to
      // reaching the minor optimization interval.
      ((DefaultTableRuntime) runtime)
          .store()
          .getTableConfig()
          .put(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "-1");

      // Test for event-based trigger enabled and evaluating pendingInput not necessary.
      ((DefaultTableRuntime) runtime)
          .store()
          .getTableConfig()
          .put(TableProperties.SELF_OPTIMIZING_AVERAGE_FILE_SIZE_TOLERANCE, "1000b");
      OptimizingConfig config = ((DefaultTableRuntime) runtime).getOptimizingConfig();
      Assert.assertTrue(config.isEventBasedTriggerEnabled());
      Assert.assertFalse(
          MetricBasedRefreshEvent.isEvaluatingPendingInputNecessary(
              (DefaultTableRuntime) runtime,
              (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable()));

      // Test for event-based trigger enabled and evaluating pendingInput necessary.
      ((DefaultTableRuntime) runtime)
          .store()
          .getTableConfig()
          .put(
              TableProperties.SELF_OPTIMIZING_AVERAGE_FILE_SIZE_TOLERANCE,
              "120mb"); // used to enable metric-based refresh event
      config = ((DefaultTableRuntime) runtime).getOptimizingConfig();
      Assert.assertTrue(config.isEventBasedTriggerEnabled());
      Assert.assertTrue(
          MetricBasedRefreshEvent.isEvaluatingPendingInputNecessary(
              (DefaultTableRuntime) runtime,
              (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable()));

      // Test for event-based trigger disabled.
      ((DefaultTableRuntime) runtime)
          .store()
          .getTableConfig()
          .put(
              TableProperties.SELF_OPTIMIZING_AVERAGE_FILE_SIZE_TOLERANCE,
              "128mb"); // used to enable metric-based refresh event
      config = ((DefaultTableRuntime) runtime).getOptimizingConfig();
      Assert.assertFalse(config.isEventBasedTriggerEnabled());
      Assert.assertTrue(
          MetricBasedRefreshEvent.isEvaluatingPendingInputNecessary(
              (DefaultTableRuntime) runtime,
              (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable()));

      // Test for event-based trigger enabled and evaluating pendingInput necessary because the
      // fallback operation (minor interval has been reached).
      ((DefaultTableRuntime) runtime)
          .store()
          .getTableConfig()
          .put(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "0");
      ((DefaultTableRuntime) runtime)
          .store()
          .getTableConfig()
          .put(TableProperties.SELF_OPTIMIZING_AVERAGE_FILE_SIZE_TOLERANCE, "1000b");
      config = ((DefaultTableRuntime) runtime).getOptimizingConfig();
      Assert.assertTrue(config.isEventBasedTriggerEnabled());
      Assert.assertTrue(
          MetricBasedRefreshEvent.isEvaluatingPendingInputNecessary(
              (DefaultTableRuntime) runtime,
              (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable()));
    }
  }

  @Test
  public void test_setAverageFileSizeTolerance() {
    Assume.assumeTrue("Skip non-Iceberg tests", isIcebergTable);
    TableRuntime runtime = tableService().getRuntime(serverTableIdentifier().getId());
    OptimizingConfig config = ((DefaultTableRuntime) runtime).getOptimizingConfig();

    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> config.setAverageFileSizeTolerance(new MemorySize(-1)));

    config.setAverageFileSizeTolerance(new MemorySize(0));
    Assert.assertEquals(0, config.getAverageFileSizeTolerance().getBytes());

    config.setAverageFileSizeTolerance(MemorySize.ofMebiBytes(50));
    Assert.assertEquals(50, config.getAverageFileSizeTolerance().getMebiBytes());

    config.setAverageFileSizeTolerance(MemorySize.ofMebiBytes(128));
    Assert.assertEquals(128, config.getAverageFileSizeTolerance().getMebiBytes());

    config.setAverageFileSizeTolerance(MemorySize.ofMebiBytes(200));
    Assert.assertEquals(128, config.getAverageFileSizeTolerance().getMebiBytes());
  }

  @Test
  public void test_getTablePartitionsWithDetailProperties() throws IOException {
    Assume.assumeTrue("Skip non-Iceberg tests", isIcebergTable);
    addChangeStoreData();
    // test getTablePartitionsWithDetailProperties
    ExecutorService executorService = ThreadPools.getWorkerPool();
    MixedAndIcebergTableDescriptor formatTableDescriptor = new MixedAndIcebergTableDescriptor();
    formatTableDescriptor.withIoExecutor(executorService);
    TableRuntime runtime = tableService().getRuntime(serverTableIdentifier().getId());
    OptimizingConfig config = ((DefaultTableRuntime) runtime).getOptimizingConfig();

    long minTargetSize = config.getTargetSize();
    List<PartitionBaseInfo> partitionBaseInfos =
        formatTableDescriptor.getTablePartitionsWithDetailProperties(
            (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable(),
            minTargetSize);
    Assert.assertEquals(1, partitionBaseInfos.size());
    Assert.assertEquals(
        7.2056237103088384E16,
        partitionBaseInfos
            .get(0)
            .getPropertyOrDefault(
                FILE_SIZE_SQUARED_ERROR_SUM, FILE_SIZE_SQUARED_ERROR_SUM_DEFAULT));
    Assert.assertEquals(
        3L,
        partitionBaseInfos.get(0).getPropertyOrDefault(BASE_FILE_COUNT, BASE_FILE_COUNT_DEFAULT));
    Assert.assertEquals(
        1L,
        partitionBaseInfos
            .get(0)
            .getPropertyOrDefault(EQ_DELETE_FILE_COUNT, EQ_DELETE_FILE_COUNT_DEFAULT));
    Assert.assertEquals(
        0L,
        partitionBaseInfos
            .get(0)
            .getPropertyOrDefault(POS_DELETE_FILE_COUNT, POS_DELETE_FILE_COUNT_DEFAULT));

    // test filterOutPartitionToBeOptimized
    Assert.assertEquals(
        1,
        MetricBasedRefreshEvent.filterOutPartitionToBeOptimized(
                (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable(),
                partitionBaseInfos,
                100000000)
            .size());
    Assert.assertEquals(
        0,
        MetricBasedRefreshEvent.filterOutPartitionToBeOptimized(
                (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable(),
                partitionBaseInfos,
                300000000)
            .size());
  }

  @Test
  public void testAccept_EmptyPartition_UpdateStatsCorrectly() {
    MetricBasedRefreshEvent.MseStats mseStats = new MetricBasedRefreshEvent.MseStats();
    PartitionBaseInfo p = new PartitionBaseInfo();

    mseStats.accept(p);

    Assert.assertEquals(Double.NaN, mseStats.mseTotalMin, 1e-6);
    Assert.assertEquals(Double.NaN, mseStats.mseTotalMax, 1e-6);
    Assert.assertEquals(0L, mseStats.totalFileCount);
  }

  @Test
  public void testAccept_SinglePartition_UpdateStatsCorrectly() {
    MetricBasedRefreshEvent.MseStats mseStats = new MetricBasedRefreshEvent.MseStats();
    PartitionBaseInfo p = new PartitionBaseInfo();
    p.setFileCount(1L);
    p.setProperty(TablePartitionDetailProperties.FILE_SIZE_SQUARED_ERROR_SUM, 9.0);

    mseStats.accept(p);

    Assert.assertEquals(9.0, mseStats.mseTotalMin, 1e-6);
    Assert.assertEquals(9.0, mseStats.mseTotalMax, 1e-6);
    Assert.assertEquals(1L, mseStats.totalFileCount);
  }

  @Test
  public void testAccept_MultiplePartitions_UpdateStatsCorrectly() {
    MetricBasedRefreshEvent.MseStats mseStats = new MetricBasedRefreshEvent.MseStats();
    PartitionBaseInfo p1 = new PartitionBaseInfo();
    p1.setFileCount(1L);
    p1.setProperty(TablePartitionDetailProperties.FILE_SIZE_SQUARED_ERROR_SUM, 9.0);

    PartitionBaseInfo p2 = new PartitionBaseInfo();
    p2.setFileCount(3L);
    p2.setProperty(TablePartitionDetailProperties.FILE_SIZE_SQUARED_ERROR_SUM, 18.0);

    mseStats.accept(p1);
    mseStats.accept(p2);

    Assert.assertEquals(6.0, mseStats.mseTotalMin, 1e-6);
    Assert.assertEquals(9.0, mseStats.mseTotalMax, 1e-6);
    Assert.assertEquals(4L, mseStats.totalFileCount);
  }

  @Test
  public void testCombine_TwoMseStats_CombinedStatsCorrectly() {
    MetricBasedRefreshEvent.MseStats mseStats = new MetricBasedRefreshEvent.MseStats();

    MetricBasedRefreshEvent.MseStats other = new MetricBasedRefreshEvent.MseStats();
    other.mseTotalMin = 4.5;
    other.mseTotalMax = 9.0;
    other.totalFileCount = 3L;

    mseStats.mseTotalMin = 2.0;
    mseStats.mseTotalMax = 8.0;
    mseStats.totalFileCount = 2L;

    mseStats.combine(other);

    Assert.assertEquals(2.0, mseStats.mseTotalMin, 1e-6);
    Assert.assertEquals(9.0, mseStats.mseTotalMax, 1e-6);
    Assert.assertEquals(5L, mseStats.totalFileCount, 1e-6);
  }
}

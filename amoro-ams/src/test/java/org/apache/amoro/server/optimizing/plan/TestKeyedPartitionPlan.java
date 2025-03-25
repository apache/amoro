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

package org.apache.amoro.server.optimizing.plan;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.optimizing.MixedIcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.OptimizingInputProperties;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.plan.AbstractPartitionPlan;
import org.apache.amoro.optimizing.plan.MixedIcebergPartitionPlan;
import org.apache.amoro.optimizing.scan.KeyedTableFileScanHelper;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestKeyedPartitionPlan extends MixedTablePlanTestBase {

  public TestKeyedPartitionPlan(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  @Test
  public void testFragmentFiles() {
    updateBaseHashBucket(1);
    testFragmentFilesBase();
  }

  @Test
  public void testSegmentFiles() {
    testSegmentFilesBase();
  }

  @Test
  public void testOnlyOneFragmentFile() {
    updateBaseHashBucket(1);
    testOnlyOneFragmentFileBase();
  }

  @Test
  public void testWithDeleteFiles() {
    testWithDeleteFilesBase();
  }

  @Test
  public void testOnlyOneChangeFiles() {
    updateChangeHashBucket(1);
    closeFullOptimizingInterval();
    // write fragment file
    List<Record> newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    List<DataFile> dataFiles =
        OptimizingTestHelpers.appendChange(
            getMixedTable(),
            tableTestHelper()
                .writeChangeStore(
                    getMixedTable(), transactionId, ChangeAction.INSERT, newRecords, false));

    List<RewriteStageTask> taskDescriptors = planWithCurrentFiles();

    Assert.assertEquals(1, taskDescriptors.size());

    assertTask(
        taskDescriptors.get(0),
        dataFiles,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList());
  }

  @Test
  public void testChangeFilesWithDelete() {
    updateChangeHashBucket(1);
    closeFullOptimizingInterval();
    List<Record> newRecords;
    long transactionId;
    List<DataFile> dataFiles = Lists.newArrayList();
    // write fragment file
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendChange(
            getMixedTable(),
            tableTestHelper()
                .writeChangeStore(
                    getMixedTable(), transactionId, ChangeAction.INSERT, newRecords, false)));
    Snapshot fromSnapshot = getMixedTable().changeTable().currentSnapshot();

    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    List<DataFile> deleteFiles =
        OptimizingTestHelpers.appendChange(
            getMixedTable(),
            tableTestHelper()
                .writeChangeStore(
                    getMixedTable(), transactionId, ChangeAction.DELETE, newRecords, false));

    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendChange(
            getMixedTable(),
            tableTestHelper()
                .writeChangeStore(
                    getMixedTable(), transactionId, ChangeAction.INSERT, newRecords, false)));
    Snapshot toSnapshot = getMixedTable().changeTable().currentSnapshot();

    AbstractPartitionPlan plan = buildPlanWithCurrentFiles();
    Assert.assertEquals(fromSnapshot.sequenceNumber(), (long) plan.getFromSequence());
    Assert.assertEquals(toSnapshot.sequenceNumber(), (long) plan.getToSequence());

    List<RewriteStageTask> taskDescriptors = plan.splitTasks(0);

    Assert.assertEquals(1, taskDescriptors.size());

    assertTask(
        taskDescriptors.get(0),
        dataFiles,
        Collections.emptyList(),
        Collections.emptyList(),
        deleteFiles);
  }

  @Test
  public void testMinorOptimizingWithBaseDelay() {
    updateChangeHashBucket(4);
    closeFullOptimizingInterval();
    closeMinorOptimizingInterval();
    List<Record> newRecords;
    long transactionId;
    List<DataFile> dataFiles = Lists.newArrayList();
    // write fragment file
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendChange(
            getMixedTable(),
            tableTestHelper()
                .writeChangeStore(
                    getMixedTable(), transactionId, ChangeAction.INSERT, newRecords, false)));
    StructLike partition = dataFiles.get(0).partition();

    // not trigger optimize
    Assert.assertEquals(0, planWithCurrentFiles().size());

    // update base delay
    updateTableProperty(TableProperties.BASE_REFRESH_INTERVAL, 1 + "");
    Assert.assertEquals(4, planWithCurrentFiles().size());
    updatePartitionProperty(
        partition,
        TableProperties.PARTITION_BASE_OPTIMIZED_TIME,
        (System.currentTimeMillis() - 10) + "");
    Assert.assertEquals(4, planWithCurrentFiles().size());
    updatePartitionProperty(
        partition,
        TableProperties.PARTITION_BASE_OPTIMIZED_TIME,
        (System.currentTimeMillis() + 1000000) + "");
    Assert.assertEquals(0, planWithCurrentFiles().size());
  }

  @Override
  protected KeyedTable getMixedTable() {
    return super.getMixedTable().asKeyedTable();
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    return new MixedIcebergPartitionPlan(
        getTableRuntime().getTableIdentifier(),
        getMixedTable(),
        getTableRuntime().getOptimizingState().getOptimizingConfig(),
        getPartition(),
        System.currentTimeMillis(),
        getTableRuntime().getOptimizingState().getLastMinorOptimizingTime(),
        getTableRuntime().getOptimizingState().getLastFullOptimizingTime());
  }

  @Override
  protected TableFileScanHelper getTableFileScanHelper() {
    return new KeyedTableFileScanHelper(
        getMixedTable(), OptimizingTestHelpers.getCurrentKeyedTableSnapshot(getMixedTable()));
  }

  @Override
  protected Map<String, String> buildTaskProperties() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(
        OptimizingInputProperties.TASK_EXECUTOR_FACTORY_IMPL,
        MixedIcebergRewriteExecutorFactory.class.getName());
    return properties;
  }
}

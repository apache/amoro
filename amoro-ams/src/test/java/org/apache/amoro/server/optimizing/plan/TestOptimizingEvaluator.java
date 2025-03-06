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
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.optimizing.plan.AbstractPartitionPlan;
import org.apache.amoro.optimizing.scan.KeyedTableFileScanHelper;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.optimizing.scan.UnkeyedTableFileScanHelper;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.TableSnapshot;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestOptimizingEvaluator extends MixedTablePlanTestBase {

  public TestOptimizingEvaluator(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Override
  protected Map<String, String> buildTaskProperties() {
    return Collections.emptyMap();
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(false, true)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      }
    };
  }

  @Test
  public void testEmpty() {
    AbstractOptimizingEvaluator optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertFalse(optimizingEvaluator.isNecessary());
    AbstractOptimizingEvaluator.PendingInput pendingInput =
        optimizingEvaluator.getOptimizingPendingInput();
    assertEmptyInput(pendingInput);
  }

  @Test
  public void testFragmentFiles() {
    closeFullOptimizingInterval();
    updateBaseHashBucket(1);
    List<DataFile> dataFiles = Lists.newArrayList();
    List<Record> newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    AbstractOptimizingEvaluator optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertFalse(optimizingEvaluator.isNecessary());
    AbstractOptimizingEvaluator.PendingInput pendingInput =
        optimizingEvaluator.getOptimizingPendingInput();
    assertEmptyInput(pendingInput);

    // add more files
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 5, 8, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertTrue(optimizingEvaluator.isNecessary());
    pendingInput = optimizingEvaluator.getOptimizingPendingInput();

    assertInput(pendingInput, FileInfo.buildFileInfo(dataFiles));
  }

  @Test
  public void testFragmentFilesWithPartitionFilterTimeStamp() {
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FILTER, "op_time >= '2022-01-01T12:00:00'")
        .commit();
    testFragmentFilesWithPartitionFilterDo(true);

    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FILTER, "op_time > '2022-01-01T12:00:00'")
        .commit();
    testFragmentFilesWithPartitionFilterDo(false);
  }

  @Test
  public void testFragmentFilesWithPartitionFilterInteger() {
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FILTER, "id > 0")
        .commit();
    testFragmentFilesWithPartitionFilterDo(true);

    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FILTER, "id > 8")
        .commit();
    testFragmentFilesWithPartitionFilterDo(false);
  }

  @Test
  public void testFragmentFilesWithPartitionFilterString() {
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FILTER, "name > '0'")
        .commit();
    testFragmentFilesWithPartitionFilterDo(true);

    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FILTER, "name > '8'")
        .commit();
    testFragmentFilesWithPartitionFilterDo(false);
  }

  private void testFragmentFilesWithPartitionFilterDo(boolean isNecessary) {
    closeFullOptimizingInterval();
    updateBaseHashBucket(1);
    List<DataFile> dataFiles = Lists.newArrayList();
    List<Record> newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    // add more files
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 5, 8, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    AbstractOptimizingEvaluator optimizingEvaluator = buildOptimizingEvaluator();
    if (isNecessary) {
      Assert.assertTrue(optimizingEvaluator.isNecessary());
      AbstractOptimizingEvaluator.PendingInput pendingInput =
          optimizingEvaluator.getOptimizingPendingInput();
      assertInput(pendingInput, FileInfo.buildFileInfo(dataFiles));
    } else {
      Assert.assertFalse(optimizingEvaluator.isNecessary());
    }
  }

  protected AbstractOptimizingEvaluator buildOptimizingEvaluator() {
    TableSnapshot snapshot = IcebergTableUtil.getSnapshot(getMixedTable(), tableRuntime);
    return IcebergTableUtil.createOptimizingEvaluator(tableRuntime, getMixedTable(), snapshot, 100);
  }

  protected void assertEmptyInput(AbstractOptimizingEvaluator.PendingInput input) {
    Assert.assertEquals(input.getPartitions().size(), 0);
    Assert.assertEquals(input.getDataFileCount(), 0);
    Assert.assertEquals(input.getDataFileSize(), 0);
    Assert.assertEquals(input.getEqualityDeleteBytes(), 0);
    Assert.assertEquals(input.getEqualityDeleteFileCount(), 0);
    Assert.assertEquals(input.getPositionalDeleteBytes(), 0);
    Assert.assertEquals(input.getPositionalDeleteFileCount(), 0);
  }

  protected void assertInput(AbstractOptimizingEvaluator.PendingInput input, FileInfo fileInfo) {
    Assert.assertEquals(input.getPartitions(), fileInfo.getPartitions());
    Assert.assertEquals(input.getDataFileCount(), fileInfo.getDataFileCount());
    Assert.assertEquals(input.getDataFileSize(), fileInfo.getDataFileSize());
    Assert.assertEquals(input.getEqualityDeleteBytes(), fileInfo.getEqualityDeleteBytes());
    Assert.assertEquals(input.getEqualityDeleteFileCount(), fileInfo.getEqualityDeleteFileCount());
    Assert.assertEquals(input.getPositionalDeleteBytes(), fileInfo.getPositionalDeleteBytes());
    Assert.assertEquals(
        input.getPositionalDeleteFileCount(), fileInfo.getPositionalDeleteFileCount());
  }

  private static class FileInfo {
    private final Map<Integer, Set<StructLike>> partitions = Maps.newHashMap();
    private int dataFileCount = 0;
    private long dataFileSize = 0;
    private final int equalityDeleteFileCount = 0;
    private final int positionalDeleteFileCount = 0;
    private final long positionalDeleteBytes = 0L;
    private final long equalityDeleteBytes = 0L;

    public static FileInfo buildFileInfo(List<DataFile> dataFiles) {
      FileInfo fileInfo = new FileInfo();
      for (DataFile dataFile : dataFiles) {
        fileInfo.dataFileCount++;
        fileInfo.dataFileSize += dataFile.fileSizeInBytes();
        fileInfo
            .partitions
            .computeIfAbsent(dataFile.specId(), ignore -> Sets.newHashSet())
            .add(dataFile.partition());
      }
      return fileInfo;
    }

    public Map<Integer, Set<StructLike>> getPartitions() {
      return partitions;
    }

    public int getDataFileCount() {
      return dataFileCount;
    }

    public long getDataFileSize() {
      return dataFileSize;
    }

    public int getEqualityDeleteFileCount() {
      return equalityDeleteFileCount;
    }

    public int getPositionalDeleteFileCount() {
      return positionalDeleteFileCount;
    }

    public long getPositionalDeleteBytes() {
      return positionalDeleteBytes;
    }

    public long getEqualityDeleteBytes() {
      return equalityDeleteBytes;
    }
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    return null;
  }

  @Override
  protected TableFileScanHelper getTableFileScanHelper() {
    if (getMixedTable().isKeyedTable()) {
      return new KeyedTableFileScanHelper(
          getMixedTable().asKeyedTable(),
          OptimizingTestHelpers.getCurrentKeyedTableSnapshot(getMixedTable().asKeyedTable()));
    } else {
      return new UnkeyedTableFileScanHelper(
          getMixedTable().asUnkeyedTable(),
          OptimizingTestHelpers.getCurrentTableSnapshot(getMixedTable()).snapshotId());
    }
  }
}

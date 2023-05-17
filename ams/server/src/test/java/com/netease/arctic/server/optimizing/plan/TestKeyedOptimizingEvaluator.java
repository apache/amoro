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

package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.server.optimizing.OptimizingTestHelpers;
import com.netease.arctic.server.optimizing.scan.KeyedTableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestKeyedOptimizingEvaluator extends TableTestBase {

  public TestKeyedOptimizingEvaluator(CatalogTestHelper catalogTestHelper,
                                      TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false)}};
  }

  @Test
  public void testEmpty() {
    OptimizingEvaluator optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertFalse(optimizingEvaluator.isNecessary());
    OptimizingEvaluator.PendingInput pendingInput = optimizingEvaluator.getPendingInput();
    assertEmptyInput(pendingInput);
  }

  @Test
  public void testFragmentFiles() {
    closeFullOptimizing();
    updateChangeHashBucket(1);
    List<Record> newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = getArcticTable().beginTransaction("");
    OptimizingTestHelpers.appendChange(getArcticTable(),
        tableTestHelper().writeChangeStore(getArcticTable(), transactionId, ChangeAction.INSERT,
            newRecords, false));

    OptimizingEvaluator optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertFalse(optimizingEvaluator.isNecessary());
    OptimizingEvaluator.PendingInput pendingInput = optimizingEvaluator.getPendingInput();
    assertEmptyInput(pendingInput);

    // add more files
    newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 5, 8, "2022-01-01T12:00:00");
    transactionId = getArcticTable().beginTransaction("");
    OptimizingTestHelpers.appendChange(getArcticTable(),
        tableTestHelper().writeChangeStore(getArcticTable(), transactionId, ChangeAction.INSERT,
            newRecords, false));

    optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertTrue(optimizingEvaluator.isNecessary());
    pendingInput = optimizingEvaluator.getPendingInput();

    KeyedTableFileScanHelper scan = new KeyedTableFileScanHelper(getArcticTable(),
        OptimizingTestHelpers.getCurrentKeyedTableSnapshot(getArcticTable()));
    List<DataFile> dataFiles =
        scan.scan().stream().map(TableFileScanHelper.FileScanResult::file).collect(Collectors.toList());

    assertInput(pendingInput, FileInfo.buildFileInfo(getArcticTable().spec(), dataFiles));
  }

  @Override
  protected KeyedTable getArcticTable() {
    return super.getArcticTable().asKeyedTable();
  }

  protected OptimizingEvaluator buildOptimizingEvaluator() {
    return new OptimizingEvaluator(buildTableRuntime(), getArcticTable(),
        OptimizingTestHelpers.getCurrentKeyedTableSnapshot(getArcticTable()));
  }

  protected TableRuntime buildTableRuntime() {
    return new TableRuntime(getArcticTable());
  }


  protected void assertEmptyInput(OptimizingEvaluator.PendingInput input) {
    Assert.assertEquals(input.getPartitions().size(), 0);
    Assert.assertEquals(input.getDataFileCount(), 0);
    Assert.assertEquals(input.getDataFileSize(), 0);
    Assert.assertEquals(input.getEqualityDeleteBytes(), 0);
    Assert.assertEquals(input.getEqualityDeleteFileCount(), 0);
    Assert.assertEquals(input.getPositionalDeleteBytes(), 0);
    Assert.assertEquals(input.getPositionalDeleteFileCount(), 0);
  }

  protected void assertInput(OptimizingEvaluator.PendingInput input, FileInfo fileInfo) {
    Assert.assertEquals(input.getPartitions(), fileInfo.getPartitions());
    Assert.assertEquals(input.getDataFileCount(), fileInfo.getDataFileCount());
    Assert.assertEquals(input.getDataFileSize(), fileInfo.getDataFileSize());
    Assert.assertEquals(input.getEqualityDeleteBytes(), fileInfo.getEqualityDeleteBytes());
    Assert.assertEquals(input.getEqualityDeleteFileCount(), fileInfo.getEqualityDeleteFileCount());
    Assert.assertEquals(input.getPositionalDeleteBytes(), fileInfo.getPositionalDeleteBytes());
    Assert.assertEquals(input.getPositionalDeleteFileCount(), fileInfo.getPositionalDeleteFileCount());
  }

  private static class FileInfo {
    private Set<String> partitions = Sets.newHashSet();
    private int dataFileCount = 0;
    private long dataFileSize = 0;
    private int equalityDeleteFileCount = 0;
    private int positionalDeleteFileCount = 0;
    private long positionalDeleteBytes = 0L;
    private long equalityDeleteBytes = 0L;

    public static FileInfo buildFileInfo(PartitionSpec spec, List<DataFile> dataFiles) {
      FileInfo fileInfo = new FileInfo();
      for (DataFile dataFile : dataFiles) {
        fileInfo.dataFileCount++;
        fileInfo.dataFileSize += dataFile.fileSizeInBytes();
        fileInfo.partitions.add(spec.partitionToPath(dataFile.partition()));
      }
      return fileInfo;
    }

    public Set<String> getPartitions() {
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

  protected void updateChangeHashBucket(int bucket) {
    getArcticTable().updateProperties().set(TableProperties.CHANGE_FILE_INDEX_HASH_BUCKET, bucket + "").commit();
  }

  protected void updateBaseHashBucket(int bucket) {
    getArcticTable().updateProperties().set(TableProperties.BASE_FILE_INDEX_HASH_BUCKET, bucket + "").commit();
  }

  protected void closeFullOptimizing() {
    getArcticTable().updateProperties().set(TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "-1").commit();
  }

}

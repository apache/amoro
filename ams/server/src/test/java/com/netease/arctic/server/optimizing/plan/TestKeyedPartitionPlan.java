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
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.optimizing.OptimizingTestHelpers;
import com.netease.arctic.server.optimizing.scan.KeyedTableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestKeyedPartitionPlan extends MixedTablePlanTestBase {

  public TestKeyedPartitionPlan(CatalogTestHelper catalogTestHelper,
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
  public void testFragmentFiles() {
    List<TaskDescriptor> taskDescriptors = testFragmentFilesBase();
    Assert.assertEquals(4, taskDescriptors.size());
    Map<DataTreeNode, List<TableFileScanHelper.FileScanResult>> baseFiles = scanBaseFilesGroupByNode();
    Assert.assertEquals(4, baseFiles.size());
    for (Map.Entry<DataTreeNode, List<TableFileScanHelper.FileScanResult>> entry : baseFiles.entrySet()) {
      DataTreeNode key = entry.getKey();
      int taskId = 0;
      if (key.getIndex() == 0) {
        taskId = 0;
      } else if (key.getIndex() == 2) {
        taskId = 1;
      } else if (key.getIndex() == 1) {
        taskId = 2;
      } else if (key.getIndex() == 3) {
        taskId = 3;
      } else {
        Assert.fail("unexpected tree node " + key);
      }
      List<IcebergDataFile> files = entry.getValue().stream()
          .map(TableFileScanHelper.FileScanResult::file)
          .collect(Collectors.toList());
      TaskDescriptor actual = taskDescriptors.get(taskId);
      RewriteFilesInput rewriteFilesInput = new RewriteFilesInput(files.toArray(new IcebergDataFile[0]),
          Collections.emptySet().toArray(new IcebergDataFile[0]),
          Collections.emptySet().toArray(new IcebergContentFile[0]),
          Collections.emptySet().toArray(new IcebergContentFile[0]), getArcticTable());

      Map<String, String> properties = buildProperties();
      TaskDescriptor expect = new TaskDescriptor(getPartition(), rewriteFilesInput, properties);
      assertTask(expect, actual);
    }
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
  public void testOnlyOneChangeFiles() {
    updateChangeHashBucket(1);
    closeFullOptimizing();
    // write fragment file
    List<Record> newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    OptimizingTestHelpers.appendChange(getArcticTable(),
        tableTestHelper().writeChangeStore(getArcticTable(), transactionId, ChangeAction.INSERT,
            newRecords, false));

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();

    Assert.assertEquals(1, taskDescriptors.size());
    List<TableFileScanHelper.FileScanResult> actualFiles = scanFiles();
    Assert.assertEquals(1, actualFiles.size());
    List<IcebergDataFile> files = actualFiles.stream()
        .map(TableFileScanHelper.FileScanResult::file)
        .collect(Collectors.toList());
    TaskDescriptor actual = taskDescriptors.get(0);
    RewriteFilesInput rewriteFilesInput = new RewriteFilesInput(files.toArray(new IcebergDataFile[0]),
        Collections.emptySet().toArray(new IcebergDataFile[0]),
        Collections.emptySet().toArray(new IcebergContentFile[0]),
        Collections.emptySet().toArray(new IcebergContentFile[0]), getArcticTable());

    Map<String, String> properties = buildProperties();
    TaskDescriptor expect = new TaskDescriptor(getPartition(), rewriteFilesInput, properties);
    assertTask(expect, actual);
  }

  @Override
  protected KeyedTable getArcticTable() {
    return super.getArcticTable().asKeyedTable();
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    return new KeyedTablePartitionPlan(getTableRuntime(), getArcticTable(), getPartition(),
        System.currentTimeMillis());
  }

  protected void updateChangeHashBucket(int bucket) {
    getArcticTable().updateProperties().set(TableProperties.CHANGE_FILE_INDEX_HASH_BUCKET, bucket + "").commit();
  }

  protected void updateBaseHashBucket(int bucket) {
    getArcticTable().updateProperties().set(TableProperties.BASE_FILE_INDEX_HASH_BUCKET, bucket + "").commit();
  }

  @Override
  protected TableFileScanHelper getTableFileScanHelper() {
    return new KeyedTableFileScanHelper(getArcticTable(),
        OptimizingTestHelpers.getCurrentKeyedTableSnapshot(getArcticTable()));
  }
}

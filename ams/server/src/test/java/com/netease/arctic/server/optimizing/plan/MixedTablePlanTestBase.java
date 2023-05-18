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

import com.google.common.collect.Maps;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.optimizing.MixFormatRewriteExecutorFactory;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.server.optimizing.OptimizingTestHelpers;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.PropertyUtil;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class MixedTablePlanTestBase extends TableTestBase {

  public MixedTablePlanTestBase(CatalogTestHelper catalogTestHelper,
                                TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  public List<TaskDescriptor> testFragmentFilesBase() {
    closeFullOptimizing();
    // write fragment file
    List<Record> newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false));

    // write fragment file
    newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 5, 8, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false));

    return planWithCurrentFiles();
  }

  public void testOnlyOneFragmentFileBase() {
    closeFullOptimizing();
    // write fragment file
    List<Record> newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false));

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();

    Assert.assertTrue(taskDescriptors.isEmpty());
  }

  public void testSegmentFilesBase() {
    closeFullOptimizing();
    List<Record> newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 40, "2022-01-01T12:00:00");
    List<DataFile> dataFiles = Lists.newArrayList();
    long transactionId;

    // write data files
    transactionId = beginTransaction();
    dataFiles.addAll(OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false)));

    // write data files
    newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 41, 80, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    dataFiles.addAll(OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false)));

    setFragmentRatio(dataFiles);
    assertSegmentFiles(dataFiles);

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();

    Assert.assertTrue(taskDescriptors.isEmpty());

    // plan with delete files
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          DataTestHelpers.writeBaseStorePosDelete(getArcticTable(), transactionId, dataFile,
              Collections.singletonList(0L)));
    }
    OptimizingTestHelpers.appendBasePosDelete(getArcticTable(), posDeleteFiles);

    taskDescriptors = planWithCurrentFiles();

    Assert.assertTrue(taskDescriptors.isEmpty());
  }

  protected List<TaskDescriptor> planWithCurrentFiles() {
    TableFileScanHelper tableFileScanHelper = getTableFileScanHelper();
    AbstractPartitionPlan partitionPlan = getAndCheckPartitionPlan();
    List<TableFileScanHelper.FileScanResult> scan = tableFileScanHelper.scan();
    for (TableFileScanHelper.FileScanResult fileScanResult : scan) {
      partitionPlan.addFile(fileScanResult.file(), fileScanResult.deleteFiles());
    }
    return partitionPlan.splitTasks(0);
  }

  private void setFragmentRatio(List<DataFile> dataFiles) {
    Long minFileSizeBytes = dataFiles.stream().map(ContentFile::fileSizeInBytes).min(Long::compareTo)
        .orElseThrow(() -> new IllegalStateException("dataFiles can't not be empty"));
    long targetFileSizeBytes =
        PropertyUtil.propertyAsLong(getArcticTable().properties(), TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
    long ratio = targetFileSizeBytes / minFileSizeBytes + 1;
    getArcticTable().updateProperties().set(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO, ratio + "").commit();
  }

  protected void closeFullOptimizing() {
    getArcticTable().updateProperties().set(TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "-1").commit();
  }

  private void assertSegmentFiles(List<DataFile> files) {
    long maxFragmentFileSizeBytes = getMaxFragmentFileSizeBytes();
    for (DataFile file : files) {
      Assert.assertTrue(file.fileSizeInBytes() > maxFragmentFileSizeBytes);
    }
  }

  private void assertFragmentFiles(List<DataFile> files) {
    long maxFragmentFileSizeBytes = getMaxFragmentFileSizeBytes();
    for (DataFile file : files) {
      Assert.assertTrue(file.fileSizeInBytes() <= maxFragmentFileSizeBytes);
    }
  }

  private long getMaxFragmentFileSizeBytes() {
    long targetFileSizeBytes =
        PropertyUtil.propertyAsLong(getArcticTable().properties(), TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
    long ratio =
        PropertyUtil.propertyAsLong(getArcticTable().properties(), TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT);
    return targetFileSizeBytes / ratio;
  }

  protected Map<String, String> buildProperties() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(OptimizingInputProperties.TASK_EXECUTOR_FACTORY_IMPL,
        MixFormatRewriteExecutorFactory.class.getName());
    return properties;
  }

  protected Map<DataTreeNode, List<TableFileScanHelper.FileScanResult>> scanBaseFilesGroupByNode() {
    TableFileScanHelper tableFileScanHelper = getTableFileScanHelper();
    List<TableFileScanHelper.FileScanResult> scan = tableFileScanHelper.scan();
    return scan.stream().collect(Collectors.groupingBy(f -> {
      PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) f.file().internalFile();
      return primaryKeyedFile.node();
    }));
  }

  protected List<TableFileScanHelper.FileScanResult> scanFiles() {
    TableFileScanHelper tableFileScanHelper = getTableFileScanHelper();
    return tableFileScanHelper.scan();
  }

  protected void assertTask(TaskDescriptor expect, TaskDescriptor actual) {
    Assert.assertEquals(expect.getPartition(), actual.getPartition());
    assertFiles(expect.getInput().rewrittenDeleteFiles(), actual.getInput().rewrittenDeleteFiles());
    assertFiles(expect.getInput().rewrittenDataFiles(), actual.getInput().rewrittenDataFiles());
    assertFiles(expect.getInput().readOnlyDeleteFiles(), actual.getInput().readOnlyDeleteFiles());
    assertFiles(expect.getInput().rePosDeletedDataFiles(), actual.getInput().rePosDeletedDataFiles());
    assertTaskProperties(expect.properties(), actual.properties());
  }

  protected void assertTaskProperties(Map<String, String> expect, Map<String, String> actual) {
    Assert.assertEquals(expect, actual);
  }

  private void assertFiles(IcebergContentFile<?>[] expect, IcebergContentFile<?>[] actual) {
    if (expect == null) {
      Assert.assertNull(actual);
      return;
    }
    Assert.assertEquals(expect.length, actual.length);
    Set<String> expectFilesPath =
        Arrays.stream(expect).map(ContentFile::path).map(CharSequence::toString).collect(Collectors.toSet());
    Set<String> actualFilesPath =
        Arrays.stream(actual).map(ContentFile::path).map(CharSequence::toString).collect(Collectors.toSet());
    Assert.assertEquals(expectFilesPath, actualFilesPath);
  }

  protected abstract AbstractPartitionPlan getPartitionPlan();

  protected AbstractPartitionPlan getAndCheckPartitionPlan() {
    AbstractPartitionPlan plan = getPartitionPlan();
    Assert.assertEquals(getPartition(), plan.getPartition());
    return plan;
  }

  protected abstract TableFileScanHelper getTableFileScanHelper();

  protected String getPartition() {
    return isPartitionedTable() ? "op_time_day=2022-01-01" : "";
  }

  protected long beginTransaction() {
    if (isKeyedTable()) {
      return getArcticTable().asKeyedTable().beginTransaction("");
    } else {
      return 0;
    }
  }

  protected TableRuntime buildTableRuntime() {
    return new TableRuntime(getArcticTable());
  }
}

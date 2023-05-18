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
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.optimizing.OptimizingConfig;
import com.netease.arctic.server.optimizing.OptimizingTestHelpers;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.PropertyUtil;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class MixedTablePlanTestBase extends TableTestBase {
  
  protected TableRuntime tableRuntime;

  public MixedTablePlanTestBase(CatalogTestHelper catalogTestHelper,
                                TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Before
  public void mock() {
    tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.loadTable()).thenReturn(getArcticTable());
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(getArcticTable().id())));
    Mockito.when(tableRuntime.getOptimizingConfig()).thenAnswer(f -> getConfig());
  }

  public void testFragmentFilesBase() {
    closeFullOptimizing();
    List<DataFile> fragmentFiles = Lists.newArrayList();
    // write fragment file
    List<Record> newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    fragmentFiles.addAll(OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false)));

    // write fragment file
    newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 5, 8, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    fragmentFiles.addAll(OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false)));

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());
    assertTask(taskDescriptors.get(0), fragmentFiles, Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList());
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

  public void testWithDeleteFilesBase() {
    closeFullOptimizing();
    updateBaseHashBucket(1);
    
    List<Record> newRecords;
    long transactionId;
    List<DataFile> rePosSegmentFiles = Lists.newArrayList();
    List<DataFile> rewrittenSegmentFiles = Lists.newArrayList();
    List<DataFile> fragmentFiles = Lists.newArrayList();
    List<DeleteFile> readOnlyDeleteFiles = Lists.newArrayList();
    List<DeleteFile> rewrittenDeleteFiles = Lists.newArrayList();

    // 1.write segment files
    newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 40, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    rePosSegmentFiles.addAll(OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false)));

    // 2.write pos-delete
    rewrittenDeleteFiles.addAll(appendPosDelete(transactionId, rePosSegmentFiles, 0));
    rewrittenDeleteFiles.addAll(appendPosDelete(transactionId, rePosSegmentFiles, 1));

    // 3.write segment files
    newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 41, 80, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    rewrittenSegmentFiles.addAll(OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false)));

    // 4.write pos-delete (radio >= 0.5)
    rewrittenDeleteFiles.addAll(appendPosDelete(transactionId, rewrittenSegmentFiles, 0, 19));

    // 5.write fragment files
    newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 81, 84, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    fragmentFiles.addAll(OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false)));

    // 6.write fragment files
    newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 85, 88, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    fragmentFiles.addAll(OptimizingTestHelpers.appendBase(getArcticTable(),
        tableTestHelper().writeBaseStore(getArcticTable(), transactionId, newRecords, false)));


    // 7. write pos-delete
    rewrittenDeleteFiles.addAll(appendPosDelete(transactionId, fragmentFiles, 0, 0));

    List<DataFile> segmentFiles = Lists.newArrayList();
    segmentFiles.addAll(rePosSegmentFiles);
    segmentFiles.addAll(rewrittenSegmentFiles);
    
    setFragmentRatio(segmentFiles);
    assertSegmentFiles(segmentFiles);
    assertFragmentFiles(fragmentFiles);

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());

    List<DataFile> rewrittenDataFiles = Lists.newArrayList();
    rewrittenDataFiles.addAll(fragmentFiles);
    rewrittenDataFiles.addAll(rewrittenSegmentFiles);
    assertTask(taskDescriptors.get(0), rewrittenDataFiles, rePosSegmentFiles, readOnlyDeleteFiles,
        rewrittenDeleteFiles);
  }

  private List<DeleteFile> appendPosDelete(long transactionId, List<DataFile> dataFiles, int pos) {
    return appendPosDelete(transactionId, dataFiles, pos, pos);
  }

  private List<DeleteFile> appendPosDelete(long transactionId, List<DataFile> dataFiles, int fromPos, int toPos) {
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    List<Long> pos = Lists.newArrayList();
    for (long i = fromPos; i <= toPos; i++) {
      pos.add(i);
    }
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          DataTestHelpers.writeBaseStorePosDelete(getArcticTable(), transactionId, dataFile, pos));
    }
    return OptimizingTestHelpers.appendBasePosDelete(getArcticTable(), posDeleteFiles);
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

  protected void assertTask(TaskDescriptor actual, List<DataFile> rewrittenDataFiles,
                            List<DataFile> rePosDeletedDataFiles, List<? extends ContentFile<?>> readOnlyDeleteFiles,
                            List<? extends ContentFile<?>> rewrittenDeleteFiles) {
    Assert.assertEquals(actual.getPartition(), getPartition());
    assertFiles(rewrittenDeleteFiles, actual.getInput().rewrittenDeleteFiles());
    assertFiles(rewrittenDataFiles, actual.getInput().rewrittenDataFiles());
    assertFiles(readOnlyDeleteFiles, actual.getInput().readOnlyDeleteFiles());
    assertFiles(rePosDeletedDataFiles, actual.getInput().rePosDeletedDataFiles());
    assertTaskProperties(buildProperties(), actual.properties());
  }

  protected void assertTaskProperties(Map<String, String> expect, Map<String, String> actual) {
    Assert.assertEquals(expect, actual);
  }

  private void assertFiles(List<? extends ContentFile<?>> expect, IcebergContentFile<?>[] actual) {
    if (expect == null) {
      Assert.assertNull(actual);
      return;
    }
    Assert.assertEquals(expect.size(), actual.length);
    Set<String> expectFilesPath =
        Arrays.stream(actual).map(ContentFile::path).map(CharSequence::toString).collect(Collectors.toSet());
    Set<String> actualFilesPath =
        expect.stream().map(ContentFile::path).map(CharSequence::toString).collect(Collectors.toSet());
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

  protected TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  private OptimizingConfig getConfig() {
    return OptimizingConfig.parseOptimizingConfig(getArcticTable().properties());
  }

  protected void updateChangeHashBucket(int bucket) {
    getArcticTable().updateProperties().set(TableProperties.CHANGE_FILE_INDEX_HASH_BUCKET, bucket + "").commit();
  }

  protected void updateBaseHashBucket(int bucket) {
    getArcticTable().updateProperties().set(TableProperties.BASE_FILE_INDEX_HASH_BUCKET, bucket + "").commit();
  }
}

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

import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.config.OptimizingConfig;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.hive.optimizing.MixFormatRewriteExecutorFactory;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.optimizing.OptimizingInputProperties;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.server.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.PartitionData;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.Pair;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class MixedTablePlanTestBase extends TableTestBase {

  protected TableRuntime tableRuntime;

  public MixedTablePlanTestBase(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Before
  public void mock() {
    tableRuntime = Mockito.mock(TableRuntime.class);
    ServerTableIdentifier id =
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(getMixedTable().id()), getTestFormat());
    id.setId(0L);
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(id);
    Mockito.when(tableRuntime.getOptimizingConfig()).thenAnswer(f -> getConfig());
    Mockito.when(tableRuntime.getCurrentSnapshotId()).thenAnswer(f -> getCurrentSnapshotId());
    Mockito.when(tableRuntime.getCurrentChangeSnapshotId())
        .thenAnswer(f -> getCurrentChangeSnapshotId());
  }

  private long getCurrentSnapshotId() {
    if (getMixedTable().isKeyedTable()) {
      return IcebergTableUtil.getSnapshotId(getMixedTable().asKeyedTable().baseTable(), false);
    } else {
      return IcebergTableUtil.getSnapshotId(getMixedTable().asUnkeyedTable(), false);
    }
  }

  private long getCurrentChangeSnapshotId() {
    if (getMixedTable().isKeyedTable()) {
      return IcebergTableUtil.getSnapshotId(getMixedTable().asKeyedTable().changeTable(), false);
    } else {
      return AmoroServiceConstants.INVALID_SNAPSHOT_ID;
    }
  }

  public void testFragmentFilesBase() {
    closeFullOptimizingInterval();
    List<DataFile> fragmentFiles = Lists.newArrayList();
    // write fragment file
    List<Record> newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    fragmentFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    // write fragment file
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 5, 8, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    fragmentFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());
    assertTask(
        taskDescriptors.get(0),
        fragmentFiles,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList());
  }

  public void testOnlyOneFragmentFileBase() {
    closeFullOptimizingInterval();
    // write fragment file
    List<Record> newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = beginTransaction();
    OptimizingTestHelpers.appendBase(
        getMixedTable(),
        tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false));

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();

    Assert.assertTrue(taskDescriptors.isEmpty());
  }

  public void testSegmentFilesBase() {
    // 1.Step1
    closeFullOptimizingInterval();
    updateBaseHashBucket(1);
    List<Record> newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 40, "2022-01-01T12:00:00");
    List<DataFile> dataFiles = Lists.newArrayList();
    long transactionId;

    // write data files
    transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    // write data files
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 41, 80, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    setFragmentRatio(dataFiles);
    assertSegmentFiles(dataFiles);

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();

    Assert.assertTrue(taskDescriptors.isEmpty());

    // 2.Step2
    // plan with delete files
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          MixedDataTestHelpers.writeBaseStorePosDelete(
              getMixedTable(), transactionId, dataFile, Collections.singletonList(0L)));
    }
    List<DeleteFile> deleteFiles =
        OptimizingTestHelpers.appendBasePosDelete(getMixedTable(), posDeleteFiles);

    taskDescriptors = planWithCurrentFiles();

    Assert.assertTrue(taskDescriptors.isEmpty());

    // 3.Step3
    openFullOptimizing();
    taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());
    assertTask(
        taskDescriptors.get(0),
        dataFiles,
        Collections.emptyList(),
        Collections.emptyList(),
        deleteFiles);
  }

  public void testWithDeleteFilesBase() {
    closeFullOptimizingInterval();
    updateBaseHashBucket(1);

    List<Record> newRecords;
    long transactionId;
    List<DataFile> rePosSegmentFiles = Lists.newArrayList();
    List<DataFile> rewrittenSegmentFiles = Lists.newArrayList();
    List<DataFile> fragmentFiles = Lists.newArrayList();
    List<DeleteFile> readOnlyDeleteFiles = Lists.newArrayList();
    List<DeleteFile> rewrittenDeleteFiles = Lists.newArrayList();

    // 1.write 1 segment file A
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 1000, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    rePosSegmentFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    // 2.write 2 pos-delete (radio < 0.1) for the complete segment file A
    rewrittenDeleteFiles.addAll(appendPosDelete(transactionId, rePosSegmentFiles, 0));
    rewrittenDeleteFiles.addAll(appendPosDelete(transactionId, rePosSegmentFiles, 1));

    // 3.write 1 segment file B
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1001, 2000, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    rewrittenSegmentFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    // 4.write 1 pos-delete (radio > 0.1) for the complete segment file B
    rewrittenDeleteFiles.addAll(
        appendPosDelete(transactionId, rewrittenSegmentFiles, 0, newRecords.size() / 10 + 1));

    // 5.write 1 fragment file C
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 2001, 2004, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    fragmentFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    // 6.write 1 fragment file D
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 2005, 2008, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    fragmentFiles.addAll(
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false)));

    // 7. write 2 pos-delete for fragment file C/D
    rewrittenDeleteFiles.addAll(appendPosDelete(transactionId, fragmentFiles, 0));

    List<DataFile> segmentFiles = Lists.newArrayList();
    segmentFiles.addAll(rePosSegmentFiles);
    segmentFiles.addAll(rewrittenSegmentFiles);

    setTargetSize(segmentFiles, true);
    setFragmentRatio(segmentFiles);
    assertSegmentFiles(segmentFiles);
    assertFragmentFiles(fragmentFiles);

    List<TaskDescriptor> taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());

    List<DataFile> rewrittenDataFiles = Lists.newArrayList();
    rewrittenDataFiles.addAll(fragmentFiles);
    rewrittenDataFiles.addAll(rewrittenSegmentFiles);
    assertTask(
        taskDescriptors.get(0),
        rewrittenDataFiles,
        rePosSegmentFiles,
        readOnlyDeleteFiles,
        rewrittenDeleteFiles);

    // test full optimizing
    openFullOptimizing();
    taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());
    rewrittenDataFiles = Lists.newArrayList();
    rewrittenDataFiles.addAll(fragmentFiles);
    rewrittenDataFiles.addAll(segmentFiles);

    List<DeleteFile> deleteFiles = Lists.newArrayList();
    deleteFiles.addAll(readOnlyDeleteFiles);
    deleteFiles.addAll(rewrittenDeleteFiles);

    assertTask(
        taskDescriptors.get(0),
        rewrittenDataFiles,
        Collections.emptyList(),
        Collections.emptyList(),
        deleteFiles);
  }

  private List<DeleteFile> appendPosDelete(long transactionId, List<DataFile> dataFiles, int pos) {
    return appendPosDelete(transactionId, dataFiles, pos, pos);
  }

  private List<DeleteFile> appendPosDelete(
      long transactionId, List<DataFile> dataFiles, int fromPos, int toPos) {
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    List<Long> pos = Lists.newArrayList();
    for (long i = fromPos; i <= toPos; i++) {
      pos.add(i);
    }
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          MixedDataTestHelpers.writeBaseStorePosDelete(
              getMixedTable(), transactionId, dataFile, pos));
    }
    return OptimizingTestHelpers.appendBasePosDelete(getMixedTable(), posDeleteFiles);
  }

  protected List<TaskDescriptor> planWithCurrentFiles() {
    AbstractPartitionPlan partitionPlan = buildPlanWithCurrentFiles();
    if (partitionPlan.isNecessary()) {
      return partitionPlan.splitTasks(0);
    } else {
      return Collections.emptyList();
    }
  }

  protected AbstractPartitionPlan buildPlanWithCurrentFiles() {
    TableFileScanHelper tableFileScanHelper = getTableFileScanHelper();
    AbstractPartitionPlan partitionPlan = getAndCheckPartitionPlan();
    try (CloseableIterable<TableFileScanHelper.FileScanResult> results =
        tableFileScanHelper.scan()) {
      for (TableFileScanHelper.FileScanResult fileScanResult : results) {
        partitionPlan.addFile(fileScanResult.file(), fileScanResult.deleteFiles());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return partitionPlan;
  }

  private void setTargetSize(List<DataFile> dataFiles, boolean isCompleteSegment) {
    Long maxFileSizeBytes =
        dataFiles.stream()
            .map(ContentFile::fileSizeInBytes)
            .max(Long::compareTo)
            .orElseThrow(() -> new IllegalStateException("dataFiles can't not be empty"));
    long targetFileSizeBytes =
        isCompleteSegment ? maxFileSizeBytes + 1 : (maxFileSizeBytes * 2 + 1);
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_TARGET_SIZE, targetFileSizeBytes + "")
        .commit();
  }

  private void setFragmentRatio(List<DataFile> dataFiles) {
    Long minFileSizeBytes =
        dataFiles.stream()
            .map(ContentFile::fileSizeInBytes)
            .min(Long::compareTo)
            .orElseThrow(() -> new IllegalStateException("dataFiles can't not be empty"));
    long targetFileSizeBytes =
        PropertyUtil.propertyAsLong(
            getMixedTable().properties(),
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
    long ratio = targetFileSizeBytes / minFileSizeBytes + 1;
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO, ratio + "")
        .commit();
  }

  protected void closeFullOptimizingInterval() {
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "-1")
        .commit();
  }

  private StructLikeMap<Map<String, String>> partitionProperty() {
    if (getMixedTable().isKeyedTable()) {
      return getMixedTable().asKeyedTable().baseTable().partitionProperty();
    } else {
      return getMixedTable().asUnkeyedTable().partitionProperty();
    }
  }

  protected void closeMinorOptimizingInterval() {
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "-1")
        .commit();
  }

  private void resetTargetSize() {
    updateTableProperty(
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT + "");
  }

  protected void updateTableProperty(String key, String value) {
    getMixedTable().updateProperties().set(key, value).commit();
  }

  protected void updatePartitionProperty(StructLike partition, String key, String value) {
    UnkeyedTable table;
    if (getMixedTable().isKeyedTable()) {
      table = getMixedTable().asKeyedTable().baseTable();
    } else {
      table = getMixedTable().asUnkeyedTable();
    }
    Transaction transaction = table.newTransaction();
    table.updatePartitionProperties(transaction).set(partition, key, value).commit();
    transaction.commitTransaction();
  }

  protected void openFullOptimizing() {
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "3600")
        .commit();
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
        PropertyUtil.propertyAsLong(
            getMixedTable().properties(),
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
    long ratio =
        PropertyUtil.propertyAsLong(
            getMixedTable().properties(),
            TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT);
    return targetFileSizeBytes / ratio;
  }

  protected Map<String, String> buildProperties() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(
        OptimizingInputProperties.TASK_EXECUTOR_FACTORY_IMPL,
        MixFormatRewriteExecutorFactory.class.getName());
    return properties;
  }

  protected Map<DataTreeNode, List<TableFileScanHelper.FileScanResult>> scanBaseFilesGroupByNode() {
    TableFileScanHelper tableFileScanHelper = getTableFileScanHelper();
    Map<DataTreeNode, List<TableFileScanHelper.FileScanResult>> resultMap = Maps.newHashMap();
    try (CloseableIterable<TableFileScanHelper.FileScanResult> results =
        tableFileScanHelper.scan()) {
      for (TableFileScanHelper.FileScanResult result : results) {
        PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) result.file();
        resultMap.putIfAbsent(primaryKeyedFile.node(), Lists.newArrayList());
        resultMap.get(primaryKeyedFile.node()).add(result);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return resultMap;
  }

  protected List<TableFileScanHelper.FileScanResult> scanFiles() {
    TableFileScanHelper tableFileScanHelper = getTableFileScanHelper();
    try (CloseableIterable<TableFileScanHelper.FileScanResult> results =
        tableFileScanHelper.scan()) {
      return Lists.newArrayList(results.iterator());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected void assertTask(
      TaskDescriptor actual,
      List<DataFile> rewrittenDataFiles,
      List<DataFile> rePosDeletedDataFiles,
      List<? extends ContentFile<?>> readOnlyDeleteFiles,
      List<? extends ContentFile<?>> rewrittenDeleteFiles) {
    Assert.assertEquals(actual.getPartition(), getPartitionPath());
    assertFiles(rewrittenDeleteFiles, actual.getInput().rewrittenDeleteFiles());
    assertFiles(rewrittenDataFiles, actual.getInput().rewrittenDataFiles());
    assertFiles(readOnlyDeleteFiles, actual.getInput().readOnlyDeleteFiles());
    assertFiles(rePosDeletedDataFiles, actual.getInput().rePosDeletedDataFiles());
    assertTaskProperties(buildProperties(), actual.properties());
  }

  protected void assertTaskProperties(Map<String, String> expect, Map<String, String> actual) {
    Assert.assertEquals(expect, actual);
  }

  private void assertFiles(List<? extends ContentFile<?>> expect, ContentFile<?>[] actual) {
    if (expect == null) {
      Assert.assertNull(actual);
      return;
    }
    Assert.assertEquals(expect.size(), actual.length);
    Set<String> expectFilesPath =
        Arrays.stream(actual)
            .map(ContentFile::path)
            .map(CharSequence::toString)
            .collect(Collectors.toSet());
    Set<String> actualFilesPath =
        expect.stream()
            .map(ContentFile::path)
            .map(CharSequence::toString)
            .collect(Collectors.toSet());
    Assert.assertEquals(expectFilesPath, actualFilesPath);
  }

  protected abstract AbstractPartitionPlan getPartitionPlan();

  protected AbstractPartitionPlan getAndCheckPartitionPlan() {
    AbstractPartitionPlan plan = getPartitionPlan();
    Assert.assertEquals(getPartition(), plan.getPartition());
    return plan;
  }

  protected abstract TableFileScanHelper getTableFileScanHelper();

  protected Pair<Integer, StructLike> getPartition() {
    return isPartitionedTable()
        ? Pair.of(
            getMixedTable().spec().specId(),
            MixedDataFiles.data(getMixedTable().spec(), "op_time_day=2022-01-01"))
        : Pair.of(getMixedTable().spec().specId(), new PartitionData(Types.StructType.of()));
  }

  protected String getPartitionPath() {
    return isPartitionedTable() ? "op_time_day=2022-01-01" : "";
  }

  protected long beginTransaction() {
    if (isKeyedTable()) {
      return getMixedTable().asKeyedTable().beginTransaction("");
    } else {
      return 0;
    }
  }

  protected TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  private OptimizingConfig getConfig() {
    return OptimizingConfig.parse(getMixedTable().properties());
  }

  protected void updateChangeHashBucket(int bucket) {
    getMixedTable()
        .updateProperties()
        .set(TableProperties.CHANGE_FILE_INDEX_HASH_BUCKET, bucket + "")
        .commit();
  }

  protected void updateBaseHashBucket(int bucket) {
    getMixedTable()
        .updateProperties()
        .set(TableProperties.BASE_FILE_INDEX_HASH_BUCKET, bucket + "")
        .commit();
  }
}

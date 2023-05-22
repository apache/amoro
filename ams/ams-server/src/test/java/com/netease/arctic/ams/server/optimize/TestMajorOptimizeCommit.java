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

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.api.TreeNode;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.util.DataFileInfoUtils;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({
    JDBCSqlSessionFactoryProvider.class
})
@PowerMockIgnore({"org.apache.logging.log4j.*", "javax.management.*", "org.apache.http.conn.ssl.*",
    "com.amazonaws.http.conn.ssl.*",
    "javax.net.ssl.*", "org.apache.hadoop.*", "javax.*", "com.sun.org.apache.*", "org.apache.xerces.*"})
public class TestMajorOptimizeCommit extends TestBaseOptimizeBase {
  @Before
  public void mock() {
    mockStatic(JDBCSqlSessionFactoryProvider.class);
    when(JDBCSqlSessionFactoryProvider.get()).thenReturn(null);
  }

  @Test
  public void testMajorOptimizeCommit() throws Exception {
    List<DataFile> baseDataFiles = insertTableBaseDataFiles(testKeyedTable, 1L);
    baseDataFilesInfo.addAll(baseDataFiles.stream()
        .map(dataFile ->
            DataFileInfoUtils.convertToDatafileInfo(dataFile, System.currentTimeMillis(), testKeyedTable))
        .collect(Collectors.toList()));

    Set<DataTreeNode> targetNodes = baseDataFilesInfo.stream()
        .map(dataFileInfo -> DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex())).collect(Collectors.toSet());
    List<DeleteFile> deleteFiles = insertBasePosDeleteFiles(testKeyedTable, 2L, baseDataFiles, targetNodes);
    posDeleteFilesInfo.addAll(deleteFiles.stream()
        .map(deleteFile -> DataFileInfoUtils.convertToDatafileInfo(deleteFile, System.currentTimeMillis(), testKeyedTable.asKeyedTable()))
        .collect(Collectors.toList()));

    Set<String> oldDataFilesPath = new HashSet<>();
    Set<String> oldDeleteFilesPath = new HashSet<>();
    testKeyedTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          oldDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> oldDeleteFilesPath.add((String) deleteFile.path()));
        });

    testKeyedTable.updateProperties().
        set(TableProperties.FULL_OPTIMIZE_TRIGGER_DELETE_FILE_SIZE_BYTES, "0").commit();
    TableOptimizeRuntime tableOptimizeRuntime = new TableOptimizeRuntime(testKeyedTable.id());
    MajorOptimizePlan majorOptimizePlan = new MajorOptimizePlan(testKeyedTable,
        tableOptimizeRuntime, baseDataFilesInfo, posDeleteFilesInfo,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = majorOptimizePlan.plan();

    Map<TreeNode, List<DataFile>> resultFiles = generateTargetFiles(testKeyedTable);
    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      List<DataFile> targetFiles = resultFiles.get(task.getSourceNodes().get(0));
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      optimizeRuntime.setNewFileCnt(targetFiles == null ? 0 : targetFiles.size());
      if (targetFiles != null) {
        optimizeRuntime.setNewFileSize(targetFiles.get(0).fileSizeInBytes());
        optimizeRuntime.setTargetFiles(targetFiles.stream().map(SerializationUtil::toByteBuffer).collect(Collectors.toList()));
      }
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    BaseOptimizeCommit optimizeCommit = new BaseOptimizeCommit(testKeyedTable, partitionTasks);
    optimizeCommit.commit(testKeyedTable.baseTable().currentSnapshot().snapshotId());

    Set<String> newDataFilesPath = new HashSet<>();
    Set<String> newDeleteFilesPath = new HashSet<>();
    testKeyedTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          newDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> newDeleteFilesPath.add((String) deleteFile.path()));
        });
    Assert.assertNotEquals(oldDataFilesPath, newDataFilesPath);
    Assert.assertNotEquals(oldDeleteFilesPath, newDeleteFilesPath);
  }

  @Test
  @Ignore
  public void testEmptyTargetFilesMajorOptimizeCommit() throws Exception {
    List<DataFile> baseDataFiles = insertTableBaseDataFiles(testKeyedTable, 1L);
    baseDataFilesInfo.addAll(baseDataFiles.stream()
        .map(dataFile ->
            DataFileInfoUtils.convertToDatafileInfo(dataFile, System.currentTimeMillis(), testKeyedTable))
        .collect(Collectors.toList()));

    Set<DataTreeNode> targetNodes = baseDataFilesInfo.stream()
        .map(dataFileInfo -> DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex())).collect(Collectors.toSet());
    List<DeleteFile> deleteFiles = insertBasePosDeleteFiles(testKeyedTable, 2L, baseDataFiles, targetNodes);
    posDeleteFilesInfo.addAll(deleteFiles.stream()
        .map(deleteFile -> DataFileInfoUtils.convertToDatafileInfo(deleteFile, System.currentTimeMillis(), testKeyedTable.asKeyedTable()))
        .collect(Collectors.toList()));

    Set<String> oldDataFilesPath = new HashSet<>();
    Set<String> oldDeleteFilesPath = new HashSet<>();
    testKeyedTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          oldDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> oldDeleteFilesPath.add((String) deleteFile.path()));
        });

    testKeyedTable.updateProperties().
        set(TableProperties.FULL_OPTIMIZE_TRIGGER_DELETE_FILE_SIZE_BYTES, "0").commit();
    TableOptimizeRuntime tableOptimizeRuntime = new TableOptimizeRuntime(testKeyedTable.id());
    MajorOptimizePlan majorOptimizePlan = new MajorOptimizePlan(testKeyedTable,
        tableOptimizeRuntime, baseDataFilesInfo, posDeleteFilesInfo,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = majorOptimizePlan.plan();

    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      optimizeRuntime.setNewFileCnt(0);
      optimizeRuntime.setNewFileSize(0);
      optimizeRuntime.setTargetFiles(new ArrayList<>());
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    BaseOptimizeCommit optimizeCommit = new BaseOptimizeCommit(testKeyedTable, partitionTasks);
    optimizeCommit.commit(testKeyedTable.baseTable().currentSnapshot().snapshotId());

    Set<String> newDataFilesPath = new HashSet<>();
    Set<String> newDeleteFilesPath = new HashSet<>();
    testKeyedTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          newDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> newDeleteFilesPath.add((String) deleteFile.path()));
        });
    Assert.assertEquals(0, newDataFilesPath.size());
    Assert.assertEquals(0, newDeleteFilesPath.size());
    Assert.assertNotEquals(oldDataFilesPath, newDataFilesPath);
    Assert.assertNotEquals(oldDeleteFilesPath, newDeleteFilesPath);
  }

  @Test
  public void testMajorOptimizeRepeatCommit() throws Exception {
    List<DataFile> baseDataFiles = insertTableBaseDataFiles(testKeyedTable, 1L);
    baseDataFilesInfo.addAll(baseDataFiles.stream()
        .map(dataFile ->
            DataFileInfoUtils.convertToDatafileInfo(dataFile, System.currentTimeMillis(), testKeyedTable))
        .collect(Collectors.toList()));

    Set<DataTreeNode> targetNodes = baseDataFilesInfo.stream()
        .map(dataFileInfo -> DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex())).collect(Collectors.toSet());

    List<DeleteFile> deleteFiles = insertBasePosDeleteFiles(testKeyedTable, 2L, baseDataFiles, targetNodes);
    posDeleteFilesInfo.addAll(deleteFiles.stream()
        .map(deleteFile -> DataFileInfoUtils.convertToDatafileInfo(deleteFile, System.currentTimeMillis(),
            testKeyedTable.asKeyedTable()))
        .collect(Collectors.toList()));

    Set<String> oldDataFilesPath = new HashSet<>();
    Set<String> oldDeleteFilesPath = new HashSet<>();
    testKeyedTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          oldDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> oldDeleteFilesPath.add((String) deleteFile.path()));
        });

    testKeyedTable.updateProperties().
        set(TableProperties.FULL_OPTIMIZE_TRIGGER_DELETE_FILE_SIZE_BYTES, "0").commit();
    TableOptimizeRuntime tableOptimizeRuntime = new TableOptimizeRuntime(testKeyedTable.id());
    MajorOptimizePlan majorOptimizePlan = new MajorOptimizePlan(testKeyedTable,
        tableOptimizeRuntime, baseDataFilesInfo, posDeleteFilesInfo,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = majorOptimizePlan.plan();

    Map<TreeNode, List<DataFile>> resultFiles = generateTargetFiles(testKeyedTable);
    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      List<DataFile> targetFiles = resultFiles.get(task.getSourceNodes().get(0));
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      optimizeRuntime.setNewFileCnt(targetFiles == null ? 0 : targetFiles.size());
      if (targetFiles != null) {
        optimizeRuntime.setNewFileSize(targetFiles.get(0).fileSizeInBytes());
        optimizeRuntime.setTargetFiles(targetFiles.stream().map(SerializationUtil::toByteBuffer).collect(Collectors.toList()));
      }
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    BaseOptimizeCommit optimizeCommit = new BaseOptimizeCommit(testKeyedTable, partitionTasks);
    long baseSnapshotId = testKeyedTable.baseTable().currentSnapshot().snapshotId();
    optimizeCommit.commit(baseSnapshotId);

    Set<String> newDataFilesPath = new HashSet<>();
    Set<String> newDeleteFilesPath = new HashSet<>();
    testKeyedTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          newDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> newDeleteFilesPath.add((String) deleteFile.path()));
        });
    Assert.assertNotEquals(oldDataFilesPath, newDataFilesPath);
    Assert.assertNotEquals(oldDeleteFilesPath, newDeleteFilesPath);

    optimizeCommit.commit(baseSnapshotId);
    Assert.assertTrue(testKeyedTable.io().exists(newDataFilesPath.iterator().next()));
  }

  @Test
  public void testMajorOptimizeConflictCommit() throws Exception {
    List<DataFile> baseDataFiles = insertTableBaseDataFiles(testKeyedTable, 1L);
    baseDataFilesInfo.addAll(baseDataFiles.stream()
        .map(dataFile ->
            DataFileInfoUtils.convertToDatafileInfo(dataFile, System.currentTimeMillis(), testKeyedTable))
        .collect(Collectors.toList()));

    Set<DataTreeNode> targetNodes = baseDataFilesInfo.stream()
        .map(dataFileInfo -> DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex())).collect(Collectors.toSet());

    List<DeleteFile> deleteFiles = insertBasePosDeleteFiles(testKeyedTable, 2L, baseDataFiles, targetNodes);
    posDeleteFilesInfo.addAll(deleteFiles.stream()
        .map(deleteFile -> DataFileInfoUtils.convertToDatafileInfo(deleteFile, System.currentTimeMillis(),
            testKeyedTable.asKeyedTable()))
        .collect(Collectors.toList()));

    testKeyedTable.updateProperties().
        set(TableProperties.FULL_OPTIMIZE_TRIGGER_DELETE_FILE_SIZE_BYTES, "0").commit();
    TableOptimizeRuntime tableOptimizeRuntime = new TableOptimizeRuntime(testKeyedTable.id());
    MajorOptimizePlan majorOptimizePlan = new MajorOptimizePlan(testKeyedTable,
        tableOptimizeRuntime, baseDataFilesInfo, posDeleteFilesInfo,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = majorOptimizePlan.plan();

    Map<TreeNode, List<DataFile>> resultFiles = generateTargetFiles(testKeyedTable);
    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      List<DataFile> targetFiles = resultFiles.get(task.getSourceNodes().get(0));
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      optimizeRuntime.setNewFileCnt(targetFiles == null ? 0 : targetFiles.size());
      if (targetFiles != null) {
        optimizeRuntime.setNewFileSize(targetFiles.get(0).fileSizeInBytes());
        optimizeRuntime.setTargetFiles(targetFiles.stream().map(SerializationUtil::toByteBuffer).collect(Collectors.toList()));
      }
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    testKeyedTable.asKeyedTable().baseTable().newDelete().deleteFile(baseDataFiles.get(0)).commit();
    BaseOptimizeCommit optimizeCommit = new BaseOptimizeCommit(testKeyedTable, partitionTasks);
    long baseSnapshotId = testKeyedTable.baseTable().currentSnapshot().snapshotId();
    optimizeCommit.commit(baseSnapshotId);

    for (List<DataFile> value : resultFiles.values()) {
      for (DataFile dataFile : value) {
        Assert.assertFalse(testKeyedTable.io().exists(dataFile.path().toString()));
      }
    }
  }

  private Map<TreeNode, List<DataFile>> generateTargetFiles(ArcticTable arcticTable) throws Exception {
    List<DataFile> dataFiles = insertOptimizeTargetDataFiles(arcticTable, OptimizeType.Major, 3);
    return dataFiles.stream().collect(Collectors.groupingBy(dataFile ->  {
      DefaultKeyedFile keyedFile = new DefaultKeyedFile(dataFile);
      return keyedFile.node().toAmsTreeNode();
    }));
  }
}

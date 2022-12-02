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

import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.TreeNode;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.util.DataFileInfoUtils;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.utils.FileUtil;
import com.netease.arctic.utils.SerializationUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.Pair;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({
    JDBCSqlSessionFactoryProvider.class
})
@PowerMockIgnore({"org.apache.logging.log4j.*", "javax.management.*", "org.apache.http.conn.ssl.*",
    "com.amazonaws.http.conn.ssl.*",
    "javax.net.ssl.*", "org.apache.hadoop.*", "javax.*", "com.sun.org.apache.*", "org.apache.xerces.*"})
public class TestMinorOptimizeCommit extends TestMinorOptimizePlan {

  @Before
  public void mock() {
    mockStatic(JDBCSqlSessionFactoryProvider.class);
    when(JDBCSqlSessionFactoryProvider.get()).thenReturn(null);
  }

  @Test
  public void testMinorOptimizeCommit() throws Exception {
    Pair<Snapshot, List<DataFile>> insertBaseResult = insertTableBaseDataFiles(testKeyedTable, 1L);
    List<DataFile> baseDataFiles = insertBaseResult.second();
    baseDataFilesInfo.addAll(baseDataFiles.stream()
        .map(dataFile ->
            DataFileInfoUtils.convertToDatafileInfo(dataFile, insertBaseResult.first(), testKeyedTable))
        .collect(Collectors.toList()));

    Set<DataTreeNode> targetNodes = baseDataFilesInfo.stream()
        .map(dataFileInfo -> DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex())).collect(Collectors.toSet());
    Pair<Snapshot, List<DeleteFile>> deleteResult =
        insertBasePosDeleteFiles(testKeyedTable, 2L, baseDataFiles, targetNodes);
    List<DeleteFile> deleteFiles = deleteResult.second();
    posDeleteFilesInfo.addAll(deleteFiles.stream()
        .map(deleteFile -> DataFileInfoUtils.convertToDatafileInfo(deleteFile, deleteResult.first(),
            testKeyedTable.asKeyedTable()))
        .collect(Collectors.toList()));
    insertChangeDeleteFiles(testKeyedTable, 3);
    List<DataFile> dataFiles = insertChangeDataFiles(testKeyedTable,4);

    Set<String> oldDataFilesPath = new HashSet<>();
    Set<String> oldDeleteFilesPath = new HashSet<>();
    testKeyedTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          oldDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> oldDeleteFilesPath.add((String) deleteFile.path()));
        });

    List<DataFileInfo> changeTableFilesInfo = new ArrayList<>(changeInsertFilesInfo);
    changeTableFilesInfo.addAll(changeDeleteFilesInfo);
    TableOptimizeRuntime tableOptimizeRuntime =  new TableOptimizeRuntime(testKeyedTable.id());
    MinorOptimizePlan minorOptimizePlan = new MinorOptimizePlan(testKeyedTable,
       tableOptimizeRuntime, baseDataFilesInfo, changeTableFilesInfo, posDeleteFilesInfo,
        new HashMap<>(), 1, System.currentTimeMillis(), snapshotId -> true);
    List<BaseOptimizeTask> tasks = minorOptimizePlan.plan();

    List<List<DeleteFile>> resultFiles = new ArrayList<>(generateTargetFiles(dataFiles).values());
    Set<StructLike> partitionData = new HashSet<>();
    for (List<DeleteFile> resultFile : resultFiles) {
      partitionData.addAll(resultFile.stream().map(ContentFile::partition).collect(Collectors.toList()));
    }
    AtomicInteger i = new AtomicInteger();
    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      List<DeleteFile> targetFiles = resultFiles.get(i.getAndIncrement());
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      if (targetFiles != null) {
        optimizeRuntime.setNewFileSize(targetFiles.get(0).fileSizeInBytes());
        optimizeRuntime.setTargetFiles(targetFiles.stream().map(SerializationUtil::toByteBuffer).collect(Collectors.toList()));
      }
      List<ByteBuffer> finalTargetFiles = optimizeRuntime.getTargetFiles();
      finalTargetFiles.addAll(task.getInsertFiles());
      optimizeRuntime.setTargetFiles(finalTargetFiles);
      optimizeRuntime.setNewFileCnt(finalTargetFiles.size());
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

    StructLikeMap<Long> maxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testKeyedTable);
    for (StructLike partitionDatum : partitionData) {
      Assert.assertEquals(testKeyedTable.changeTable().currentSnapshot().sequenceNumber(),
          (long) maxTxId.get(partitionDatum));
    }
    Assert.assertNotEquals(oldDataFilesPath, newDataFilesPath);
    Assert.assertNotEquals(oldDeleteFilesPath, newDeleteFilesPath);
  }

  @Test
  public void testNoPartitionTableMinorOptimizeCommit() throws Exception {
    Pair<Snapshot, List<DataFile>> insertBaseResult = insertTableBaseDataFiles(testNoPartitionTable, 1L);
    List<DataFile> baseDataFiles = insertBaseResult.second();
    baseDataFilesInfo.addAll(baseDataFiles.stream()
        .map(dataFile ->
            DataFileInfoUtils.convertToDatafileInfo(dataFile, insertBaseResult.first(), testNoPartitionTable))
        .collect(Collectors.toList()));

    Set<DataTreeNode> targetNodes = baseDataFilesInfo.stream()
        .map(dataFileInfo -> DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex())).collect(Collectors.toSet());
    Pair<Snapshot, List<DeleteFile>> deleteResult =
        insertBasePosDeleteFiles(testNoPartitionTable, 2L, baseDataFiles, targetNodes);
    List<DeleteFile> deleteFiles = deleteResult.second();
    posDeleteFilesInfo.addAll(deleteFiles.stream()
        .map(deleteFile ->
            DataFileInfoUtils.convertToDatafileInfo(deleteFile, deleteResult.first(), testNoPartitionTable.asKeyedTable()))
        .collect(Collectors.toList()));
    insertChangeDeleteFiles(testNoPartitionTable, 3);
    List<DataFile> dataFiles = insertChangeDataFiles(testNoPartitionTable, 4);

    Set<String> oldDataFilesPath = new HashSet<>();
    Set<String> oldDeleteFilesPath = new HashSet<>();
    testNoPartitionTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          oldDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> oldDeleteFilesPath.add((String) deleteFile.path()));
        });

    List<DataFileInfo> changeTableFilesInfo = new ArrayList<>(changeInsertFilesInfo);
    changeTableFilesInfo.addAll(changeDeleteFilesInfo);
    TableOptimizeRuntime tableOptimizeRuntime =  new TableOptimizeRuntime(testNoPartitionTable.id());
    MinorOptimizePlan minorOptimizePlan = new MinorOptimizePlan(testNoPartitionTable,
        tableOptimizeRuntime, baseDataFilesInfo, changeTableFilesInfo, posDeleteFilesInfo,
        new HashMap<>(), 1, System.currentTimeMillis(), snapshotId -> true);
    List<BaseOptimizeTask> tasks = minorOptimizePlan.plan();

    List<List<DeleteFile>> resultFiles = new ArrayList<>(generateTargetFiles(dataFiles).values());
    AtomicInteger i = new AtomicInteger();
    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      List<DeleteFile> targetFiles = resultFiles.get(i.getAndIncrement());
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      if (targetFiles != null) {
        optimizeRuntime.setNewFileSize(targetFiles.get(0).fileSizeInBytes());
        optimizeRuntime.setTargetFiles(targetFiles.stream().map(SerializationUtil::toByteBuffer).collect(Collectors.toList()));
      }
      List<ByteBuffer> finalTargetFiles = optimizeRuntime.getTargetFiles();
      finalTargetFiles.addAll(task.getInsertFiles());
      optimizeRuntime.setTargetFiles(finalTargetFiles);
      optimizeRuntime.setNewFileCnt(finalTargetFiles.size());
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    BaseOptimizeCommit optimizeCommit = new BaseOptimizeCommit(testNoPartitionTable, partitionTasks);
    optimizeCommit.commit(testNoPartitionTable.baseTable().currentSnapshot().snapshotId());

    Set<String> newDataFilesPath = new HashSet<>();
    Set<String> newDeleteFilesPath = new HashSet<>();
    testNoPartitionTable.baseTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          newDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> newDeleteFilesPath.add((String) deleteFile.path()));
        });

    Snapshot snapshot = testNoPartitionTable.changeTable().currentSnapshot();
    StructLikeMap<Long> maxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testNoPartitionTable);
    Assert.assertEquals(snapshot.sequenceNumber(), (long) maxTxId.get(TablePropertyUtil.EMPTY_STRUCT));
    Assert.assertNotEquals(oldDataFilesPath, newDataFilesPath);
    Assert.assertNotEquals(oldDeleteFilesPath, newDeleteFilesPath);
  }

  @Test
  public void testOnlyEqDeleteInChangeCommit() throws Exception {
    List<DataFile> changeEqDeletes = insertChangeDeleteFiles(testKeyedTable, 3);

    List<DataFileInfo> changeTableFilesInfo = new ArrayList<>(changeInsertFilesInfo);
    changeTableFilesInfo.addAll(changeDeleteFilesInfo);
    TableOptimizeRuntime tableOptimizeRuntime =  new TableOptimizeRuntime(testKeyedTable.id());
    MinorOptimizePlan minorOptimizePlan = new MinorOptimizePlan(testKeyedTable,
        tableOptimizeRuntime, baseDataFilesInfo, changeTableFilesInfo, posDeleteFilesInfo,
        new HashMap<>(), 1, System.currentTimeMillis(), snapshotId -> true);
    List<BaseOptimizeTask> tasks = minorOptimizePlan.plan();

    Set<StructLike> partitionData = changeEqDeletes.stream().map(ContentFile::partition).collect(Collectors.toSet());

    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      optimizeRuntime.setNewFileSize(0);
      optimizeRuntime.setTargetFiles(new ArrayList<>());
      List<ByteBuffer> finalTargetFiles = optimizeRuntime.getTargetFiles();
      finalTargetFiles.addAll(task.getInsertFiles());
      optimizeRuntime.setTargetFiles(finalTargetFiles);
      optimizeRuntime.setNewFileCnt(finalTargetFiles.size());
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    StructLikeMap<Long> oldMaxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testKeyedTable);
    for (StructLike partitionDatum : partitionData) {
      Assert.assertNull(oldMaxTxId.get(partitionDatum));
    }

    BaseOptimizeCommit optimizeCommit = new BaseOptimizeCommit(testKeyedTable, partitionTasks);
    optimizeCommit.commit(TableOptimizeRuntime.INVALID_SNAPSHOT_ID);

    StructLikeMap<Long> newMaxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testKeyedTable);
    for (StructLike partitionDatum : partitionData) {
      Assert.assertEquals(testKeyedTable.changeTable().currentSnapshot().sequenceNumber(),
          (long) newMaxTxId.get(partitionDatum));
    }
  }

  @Test
  public void testNoPartitionOnlyEqDeleteInChangeCommit() throws Exception {
    insertChangeDeleteFiles(testNoPartitionTable, 3);

    List<DataFileInfo> changeTableFilesInfo = new ArrayList<>(changeInsertFilesInfo);
    changeTableFilesInfo.addAll(changeDeleteFilesInfo);
    TableOptimizeRuntime tableOptimizeRuntime =  new TableOptimizeRuntime(testNoPartitionTable.id());
    MinorOptimizePlan minorOptimizePlan = new MinorOptimizePlan(testNoPartitionTable,
        tableOptimizeRuntime, baseDataFilesInfo, changeTableFilesInfo, posDeleteFilesInfo,
        new HashMap<>(), 1, System.currentTimeMillis(), snapshotId -> true);
    List<BaseOptimizeTask> tasks = minorOptimizePlan.plan();

    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      optimizeRuntime.setNewFileSize(0);
      optimizeRuntime.setTargetFiles(new ArrayList<>());
      List<ByteBuffer> finalTargetFiles = optimizeRuntime.getTargetFiles();
      finalTargetFiles.addAll(task.getInsertFiles());
      optimizeRuntime.setTargetFiles(finalTargetFiles);
      optimizeRuntime.setNewFileCnt(finalTargetFiles.size());
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    StructLikeMap<Long> oldMaxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testNoPartitionTable);
    Assert.assertNull(oldMaxTxId.get(TablePropertyUtil.EMPTY_STRUCT));

    BaseOptimizeCommit optimizeCommit = new BaseOptimizeCommit(testNoPartitionTable, partitionTasks);
    optimizeCommit.commit(TableOptimizeRuntime.INVALID_SNAPSHOT_ID);

    Snapshot snapshot = testNoPartitionTable.changeTable().currentSnapshot();
    StructLikeMap<Long> newMaxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testNoPartitionTable);
    Assert.assertEquals(snapshot.sequenceNumber(), (long) newMaxTxId.get(TablePropertyUtil.EMPTY_STRUCT));
  }

  private Map<TreeNode, List<DeleteFile>> generateTargetFiles(List<DataFile> dataFiles) throws Exception {
    List<DeleteFile> deleteFiles = insertOptimizeTargetDeleteFiles(testKeyedTable, dataFiles, 5);
    return deleteFiles.stream().collect(Collectors.groupingBy(deleteFile ->  {
      DataTreeNode dataTreeNode = FileUtil.parseFileNodeFromFileName(deleteFile.path().toString());
      return new TreeNode(dataTreeNode.mask(), dataTreeNode.index());
    }));
  }
}

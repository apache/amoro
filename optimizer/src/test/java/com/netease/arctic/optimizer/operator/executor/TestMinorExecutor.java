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

package com.netease.arctic.optimizer.operator.executor;

import com.google.common.collect.Iterables;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.io.writer.GenericChangeTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.util.ContentFileUtil;
import com.netease.arctic.optimizer.util.DataFileInfoUtils;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.WriteResult;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TestMinorExecutor extends TestBaseExecutor {
  protected List<DataFileInfo> changeInsertFilesInfo = new ArrayList<>();
  protected List<DataFileInfo> changeDeleteFilesInfo = new ArrayList<>();

  @Test
  public void testMinorExecutor() throws Exception {
    insertBasePosDeleteFiles(2);
    insertChangeDeleteFiles(3);
    insertChangeDataFiles(4);

    NodeTask nodeTask = constructNodeTask();
    String[] arg = new String[0];
    OptimizerConfig optimizerConfig = new OptimizerConfig(arg);
    optimizerConfig.setOptimizerId("UnitTest");
    MinorExecutor minorExecutor = new MinorExecutor(nodeTask, testKeyedTable, System.currentTimeMillis(), optimizerConfig);
    OptimizeTaskResult<DeleteFile> result = minorExecutor.execute();
    Assert.assertEquals(Iterables.size(result.getTargetFiles()), 4);
  }

  private NodeTask constructNodeTask() {
    NodeTask nodeTask = new NodeTask();
    nodeTask.setSourceNodes(baseDataFilesInfo.stream()
        .map(dataFileInfo -> DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex()))
        .collect(Collectors.toSet()));
    nodeTask.setTableIdentifier(testKeyedTable.id());
    nodeTask.setTaskId(new OptimizeTaskId(OptimizeType.Major, UUID.randomUUID().toString()));
    nodeTask.setAttemptId(Math.abs(ThreadLocalRandom.current().nextInt()));
    nodeTask.setPartition(FILE_A.partition());

    for (DataFileInfo fileInfo : baseDataFilesInfo) {
      nodeTask.addFile(
          ContentFileUtil.buildContentFile(fileInfo, testKeyedTable.baseTable().spec(), testKeyedTable.io()),
          DataFileType.BASE_FILE);
    }
    for (DataFileInfo fileInfo : posDeleteFilesInfo) {
      nodeTask.addFile(
          ContentFileUtil.buildContentFile(fileInfo, testKeyedTable.baseTable().spec(), testKeyedTable.io()),
          DataFileType.POS_DELETE_FILE);
    }
    for (DataFileInfo fileInfo : changeInsertFilesInfo) {
      nodeTask.addFile(
          ContentFileUtil.buildContentFile(fileInfo, testKeyedTable.baseTable().spec(), testKeyedTable.io()),
          DataFileType.INSERT_FILE);
    }
    for (DataFileInfo fileInfo : changeDeleteFilesInfo) {
      nodeTask.addFile(
          ContentFileUtil.buildContentFile(fileInfo, testKeyedTable.baseTable().spec(), testKeyedTable.io()),
          DataFileType.EQ_DELETE_FILE);
    }

    return nodeTask;
  }

  protected void insertChangeDeleteFiles(long transactionId) throws IOException {
    GenericChangeTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable)
        .withChangeAction(ChangeAction.DELETE)
        .withTransactionId(transactionId).buildChangeWriter();

    List<DataFile> changeDeleteFiles = new ArrayList<>();
    // delete 1000 records in 2 partitions(2022-1-1\2022-1-2)
    int length = 100;
    for (int i = 1; i < length * 10; i = i + length) {
      for (Record record : baseRecords(i, length)) {
        writer.write(record);
      }
      WriteResult result = writer.complete();
      changeDeleteFiles.addAll(Arrays.asList(result.dataFiles()));
    }
    AppendFiles baseAppend = testKeyedTable.changeTable().newAppend();
    changeDeleteFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();
    long commitTime = System.currentTimeMillis();

    changeDeleteFilesInfo = changeDeleteFiles.stream()
        .map(deleteFile -> DataFileInfoUtils.convertToDatafileInfo(deleteFile, commitTime, testKeyedTable))
        .collect(Collectors.toList());
  }

  protected void insertChangeDataFiles(long transactionId) throws IOException {
    GenericChangeTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable)
        .withChangeAction(ChangeAction.INSERT)
        .withTransactionId(transactionId).buildChangeWriter();

    List<DataFile> changeInsertFiles = new ArrayList<>();
    // write 1000 records to 2 partitions(2022-1-1\2022-1-2)
    int length = 100;
    for (int i = 1; i < length * 10; i = i + length) {
      for (Record record : baseRecords(i, length)) {
        writer.write(record);
      }
      WriteResult result = writer.complete();
      changeInsertFiles.addAll(Arrays.asList(result.dataFiles()));
    }
    AppendFiles baseAppend = testKeyedTable.changeTable().newAppend();
    changeInsertFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();
    long commitTime = System.currentTimeMillis();

    changeInsertFilesInfo = changeInsertFiles.stream()
        .map(dataFile -> DataFileInfoUtils.convertToDatafileInfo(dataFile, commitTime, testKeyedTable))
        .collect(Collectors.toList());
  }
}

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

import com.netease.arctic.ams.api.JobId;
import com.netease.arctic.ams.api.JobType;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeTaskStat;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.io.reader.BaseIcebergPosDeleteReader;
import com.netease.arctic.io.reader.GenericArcticDataReader;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.scan.BaseArcticFileScanTask;
import com.netease.arctic.scan.KeyedTableScanTask;
import com.netease.arctic.scan.NodeFileScanTask;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MinorExecutor extends BaseExecutor<DeleteFile> {
  private static final Logger LOG = LoggerFactory.getLogger(MinorExecutor.class);

  private final NodeTask task;
  private final ArcticTable table;
  private final long startTime;
  private final OptimizerConfig config;

  public MinorExecutor(NodeTask nodeTask, ArcticTable table, long startTime, OptimizerConfig config) {
    this.task = nodeTask;
    this.table = table;
    this.startTime = startTime;
    this.config = config;
  }

  @Override
  public OptimizeTaskResult<DeleteFile> execute() throws Exception {
    List<DeleteFile> targetFiles = new ArrayList<>();
    LOG.info("start process minor optimize task: {}", task);

    Map<DataTreeNode, List<DataFile>> dataFileMap = groupDataFilesByNode(task.dataFiles());
    Map<DataTreeNode, List<DeleteFile>> deleteFileMap = groupDeleteFilesByNode(task.posDeleteFiles());
    KeyedTable keyedTable = table.asKeyedTable();

    long insertCount = 0;
    Schema requiredSchema = new Schema(MetadataColumns.FILE_PATH, MetadataColumns.ROW_POSITION);
    Types.StructType recordStruct = requiredSchema.asStruct();
    for (Map.Entry<DataTreeNode, List<DataFile>> nodeFileEntry : dataFileMap.entrySet()) {
      DataTreeNode treeNode = nodeFileEntry.getKey();
      List<DataFile> dataFiles = nodeFileEntry.getValue();
      dataFiles.addAll(task.deleteFiles());
      List<DeleteFile> posDeleteList = deleteFileMap.get(treeNode);
      CloseableIterator<Record> iterator =
          openTask(dataFiles, posDeleteList, requiredSchema, task.getSourceNodes());

      SortedPosDeleteWriter<Record> posDeleteWriter = GenericTaskWriters.builderFor(keyedTable)
          .withTransactionId(getMaxTransactionId(dataFiles))
          .withTaskId(task.getAttemptId())
          .buildBasePosDeleteWriter(treeNode.mask(), treeNode.index(), task.getPartition());
      while (iterator.hasNext()) {
        Record record = iterator.next();
        String filePath = (String) record.get(recordStruct.fields()
            .indexOf(recordStruct.field(MetadataColumns.FILE_PATH.name())));
        Long rowPosition = (Long) record.get(recordStruct.fields()
            .indexOf(recordStruct.field(MetadataColumns.ROW_POSITION.name())));
        posDeleteWriter.delete(filePath, rowPosition);
        insertCount++;
        if (insertCount == 1 || insertCount == 100000) {
          LOG.info("task {} insert records number {} and data sampling path:{}, pos:{}",
              task.getTaskId(), insertCount, "", 0);
        }
      }

      // rewrite pos-delete content
      if (CollectionUtils.isNotEmpty(posDeleteList)) {
        BaseIcebergPosDeleteReader posDeleteReader = new BaseIcebergPosDeleteReader(table.io(), posDeleteList);
        CloseableIterable<Record> posDeleteIterable = posDeleteReader.readDeletes();
        CloseableIterator<Record> posDeleteIterator = table.io().doAs(posDeleteIterable::iterator);
        while (posDeleteIterator.hasNext()) {
          Record record = posDeleteIterator.next();
          String filePath = posDeleteReader.readPath(record);
          Long rowPosition = posDeleteReader.readPos(record);
          posDeleteWriter.delete(filePath, rowPosition);
        }
      }

      targetFiles.addAll(posDeleteWriter.complete());
    }
    LOG.info("task {} insert records number {}", task.getTaskId(), insertCount);

    long totalFileSize = 0;
    List<ByteBuffer> deleteFileBytesList = new ArrayList<>();
    for (DeleteFile deleteFile : targetFiles) {
      totalFileSize += deleteFile.fileSizeInBytes();
      deleteFileBytesList.add(SerializationUtil.toByteBuffer(deleteFile));
    }

    OptimizeTaskStat optimizeTaskStat = new OptimizeTaskStat();
    BeanUtils.copyProperties(optimizeTaskStat, task);
    JobId jobId = new JobId();
    jobId.setId(config.getOptimizerId());
    jobId.setType(JobType.Optimize);
    optimizeTaskStat.setJobId(jobId);

    optimizeTaskStat.setStatus(OptimizeStatus.Prepared);
    optimizeTaskStat.setAttemptId(task.getAttemptId() + "");
    optimizeTaskStat.setCostTime(System.currentTimeMillis() - startTime);

    optimizeTaskStat.setNewFileSize(totalFileSize);
    optimizeTaskStat.setReportTime(System.currentTimeMillis());
    optimizeTaskStat.setFiles(deleteFileBytesList);
    optimizeTaskStat.setTableIdentifier(task.getTableIdentifier().buildTableIdentifier());
    optimizeTaskStat.setTaskId(task.getTaskId());

    OptimizeTaskResult<DeleteFile> result = new OptimizeTaskResult<>();
    result.setTargetFiles(targetFiles);
    result.setOptimizeTaskStat(optimizeTaskStat);

    return result;
  }

  @Override
  public void close() {

  }

  private CloseableIterator<Record> openTask(List<DataFile> dataFiles, List<DeleteFile> posDeleteList,
                                             Schema requiredSchema, Set<DataTreeNode> sourceNodes) {
    if (CollectionUtils.isEmpty(dataFiles)) {
      return CloseableIterator.empty();
    }

    List<ArcticFileScanTask> fileScanTasks = dataFiles.stream()
        .map(file -> new BaseArcticFileScanTask(new DefaultKeyedFile(file), posDeleteList, table.spec()))
        .collect(Collectors.toList());

    PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.noPrimaryKey();
    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      primaryKeySpec = keyedTable.primaryKeySpec();
    }
    GenericArcticDataReader arcticDataReader =
        new GenericArcticDataReader(table.io(), table.schema(), requiredSchema,
            primaryKeySpec, null, false, IdentityPartitionConverters::convertConstant, sourceNodes, false);

    KeyedTableScanTask keyedTableScanTask = new NodeFileScanTask(fileScanTasks);
    return arcticDataReader.readDeletedData(keyedTableScanTask);
  }
}
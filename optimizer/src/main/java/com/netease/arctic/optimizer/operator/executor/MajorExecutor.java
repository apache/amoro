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
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.io.reader.GenericArcticDataReader;
import com.netease.arctic.io.writer.GenericBaseTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.util.SerializationUtil;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.scan.BaseArcticFileScanTask;
import com.netease.arctic.scan.KeyedTableScanTask;
import com.netease.arctic.scan.NodeFileScanTask;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableProperties;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MajorExecutor extends BaseExecutor<DataFile> {
  private static final Logger LOG = LoggerFactory.getLogger(MajorExecutor.class);

  private final NodeTask task;
  private final ArcticTable table;
  private final long startTime;
  private final OptimizerConfig config;

  public MajorExecutor(NodeTask nodeTask, ArcticTable table, long startTime, OptimizerConfig config) {
    this.task = nodeTask;
    this.table = table;
    this.startTime = startTime;
    this.config = config;
  }

  @Override
  public OptimizeTaskResult<DataFile> execute() throws Exception {
    Iterable<DataFile> targetFiles;
    LOG.info("start process major optimize task: {}", task);

    Map<DataTreeNode, List<DeleteFile>> deleteFileMap = groupDeleteFilesByNode(task.posDeleteFiles());
    List<DataFile> dataFiles = task.dataFiles();
    dataFiles.addAll(task.deleteFiles());
    CloseableIterator<Record> recordIterator =
        openTask(dataFiles, deleteFileMap, table.schema(), task.getSourceNodes());
    if (table.isUnkeyedTable()) {
      targetFiles = optimizeUnKeyedTable(recordIterator);
    } else {
      targetFiles = optimizeKeyedTable(recordIterator);
    }

    long totalFileSize = 0;
    List<ByteBuffer> baseFileBytesList = new ArrayList<>();
    for (DataFile baseFile : targetFiles) {
      totalFileSize += baseFile.fileSizeInBytes();
      baseFileBytesList.add(SerializationUtil.toByteBuffer(baseFile));
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
    optimizeTaskStat.setFiles(baseFileBytesList);
    optimizeTaskStat.setTableIdentifier(task.getTableIdentifier().buildTableIdentifier());
    optimizeTaskStat.setTaskId(task.getTaskId());

    OptimizeTaskResult<DataFile> result = new OptimizeTaskResult<>();
    result.setTargetFiles(targetFiles);
    result.setOptimizeTaskStat(optimizeTaskStat);

    return result;
  }

  @Override
  public void close() {
  }

  private Iterable<DataFile> optimizeKeyedTable(CloseableIterator<Record> recordIterator) throws Exception {
    KeyedTable keyedTable = table.asKeyedTable();

    GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(keyedTable)
        .withTransactionId(getMaxTransactionId(task.dataFiles()))
        .withTaskId(task.getAttemptId())
        .buildBaseWriter();
    long insertCount = 0;
    while (recordIterator.hasNext()) {
      Record baseRecord = recordIterator.next();
      writer.write(baseRecord);
      insertCount++;
      if (insertCount == 1 || insertCount == 100000) {
        LOG.info("task {} insert records number {} and data sampling {}",
            task.getTaskId(), insertCount, baseRecord);
      }
    }

    LOG.info("task {} insert records number {}", task.getTaskId(), insertCount);

    return Arrays.asList(writer.complete().dataFiles());
  }

  private Iterable<DataFile> optimizeUnKeyedTable(CloseableIterator<Record> recordIterator) {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(table.schema(), table.spec());
    FileFormat fileFormat = FileFormat.valueOf((table.properties().getOrDefault(TableProperties.BASE_FILE_FORMAT,
        TableProperties.BASE_FILE_FORMAT_DEFAULT).toUpperCase(Locale.ENGLISH)));
    OutputFileFactory outputFileFactory = OutputFileFactory
        .builderFor(table.asUnkeyedTable(), 0, 0).format(fileFormat).build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(task.getPartition());
    DataFile targetFile = table.io().doAs(() -> {
      DataWriter<Record> writer = appenderFactory
          .newDataWriter(outputFile, FileFormat.PARQUET, task.getPartition());

      long insertCount = 0;
      while (recordIterator.hasNext()) {
        Record baseRecord = recordIterator.next();
        writer.add(baseRecord);
        insertCount++;
        if (insertCount == 1 || insertCount == 100000) {
          LOG.info("task {} insert records number {} and data sampling {}",
              task.getTaskId(), insertCount, baseRecord);
        }
      }

      LOG.info("task {} insert records number {}", task.getTaskId(), insertCount);

      writer.close();
      return writer.toDataFile();
    });

    return Collections.singletonList(targetFile);
  }

  private CloseableIterator<Record> openTask(List<DataFile> dataFiles,
                                             Map<DataTreeNode, List<DeleteFile>> deleteFileMap,
                                             Schema requiredSchema, Set<DataTreeNode> sourceNodes) {
    if (CollectionUtils.isEmpty(dataFiles)) {
      return CloseableIterator.empty();
    }

    PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.noPrimaryKey();
    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      primaryKeySpec = keyedTable.primaryKeySpec();
    }

    GenericArcticDataReader arcticDataReader =
        new GenericArcticDataReader(table.io(), table.schema(), requiredSchema, primaryKeySpec,
            null, false, IdentityPartitionConverters::convertConstant, sourceNodes, false);

    List<ArcticFileScanTask> fileScanTasks = dataFiles.stream()
        .map(file -> {
          DefaultKeyedFile defaultKeyedFile = new DefaultKeyedFile(file);
          if (defaultKeyedFile.type() == DataFileType.EQ_DELETE_FILE) {
            return new BaseArcticFileScanTask(defaultKeyedFile, null, table.spec());
          } else {
            return new BaseArcticFileScanTask(defaultKeyedFile,
                deleteFileMap.get(defaultKeyedFile.node()), table.spec());
          }
        })
        .collect(Collectors.toList());

    KeyedTableScanTask keyedTableScanTask = new NodeFileScanTask(fileScanTasks);
    LOG.info("start read data : {}", table.id());
    return arcticDataReader.readData(keyedTableScanTask);
  }
}

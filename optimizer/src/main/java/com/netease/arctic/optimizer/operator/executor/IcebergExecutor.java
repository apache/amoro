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
import com.netease.arctic.io.reader.GenericIcebergDataReader;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT;
import static org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT_DEFAULT;

public class IcebergExecutor extends BaseExecutor<DataFile> {
  private static final Logger LOG = LoggerFactory.getLogger(IcebergExecutor.class);

  private final NodeTask task;
  private final ArcticTable table;
  private final long startTime;
  private final OptimizerConfig config;

  public IcebergExecutor(NodeTask nodeTask,
                         ArcticTable table,
                         long startTime,
                         OptimizerConfig config) {
    this.task = nodeTask;
    this.table = table;
    this.startTime = startTime;
    this.config = config;
  }

  @Override
  public OptimizeTaskResult<DataFile> execute() throws Exception {
    Iterable<DataFile> targetFiles;
    LOG.info("start process native optimize task: {}", task);

    List<FileScanTask> fileScanTasks = null;

    targetFiles = table.io().doAs(() -> {
      CloseableIterator<Record> recordIterator = openTask(fileScanTasks, table.schema());
      return optimizeTable(recordIterator);
    });

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

  private Iterable<DataFile> optimizeTable(CloseableIterator<Record> recordIterator) throws Exception {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(table.schema(), table.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table.asUnkeyedTable(), table.spec().specId(), task.getAttemptId())
        .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(task.getPartition());

    String formatAsString = table.properties().getOrDefault(DEFAULT_FILE_FORMAT, DEFAULT_FILE_FORMAT_DEFAULT);
    long targetSizeByBytes = PropertyUtil.propertyAsLong(table.properties(),
        TableProperties.WRITE_TARGET_FILE_SIZE_BYTES, TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    List<DataFile> result = new ArrayList<>();
    DataWriter<Record> writer = appenderFactory
        .newDataWriter(outputFile, FileFormat.valueOf(formatAsString.toUpperCase()), task.getPartition());

    long insertCount = 0;
    while (recordIterator.hasNext()) {
      if (writer.length() >= targetSizeByBytes) {
        writer.close();
        result.add(writer.toDataFile());
        EncryptedOutputFile newOutputFile = outputFileFactory.newOutputFile(task.getPartition());
        writer = appenderFactory
            .newDataWriter(newOutputFile, FileFormat.valueOf(formatAsString.toUpperCase()), task.getPartition());
      }
      Record baseRecord = recordIterator.next();

      writer.write(baseRecord);
      insertCount++;
      if (insertCount == 1 || insertCount == 100000) {
        LOG.info("task {} insert records number {} and data sampling {}",
            task.getTaskId(), insertCount, baseRecord);
      }
    }

    LOG.info("task {} insert records number {}", task.getTaskId(), insertCount);

    writer.close();
    result.add(writer.toDataFile());

    return result;
  }

  private CloseableIterator<Record> openTask(List<FileScanTask> fileScanTasks, Schema requiredSchema) {
    if (CollectionUtils.isEmpty(fileScanTasks)) {
      return CloseableIterator.empty();
    }

    GenericIcebergDataReader icebergDataReader =
        new GenericIcebergDataReader(table.io(), table.schema(), requiredSchema,
            table.properties().get(TableProperties.DEFAULT_NAME_MAPPING),
            false, IdentityPartitionConverters::convertConstant, false);
    CloseableIterable<Record> dataIterable = CloseableIterable.concat(CloseableIterable.transform(
        CloseableIterable.withNoopClose(fileScanTasks),
        icebergDataReader::readData));

    return dataIterable.iterator();
  }
}

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
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.FileUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public abstract class BaseExecutor<F extends ContentFile<F>> implements Executor<F> {

  protected static final int SAMPLE_DATA_INTERVAL = 100000;

  protected final NodeTask task;
  protected final ArcticTable table;
  protected final long startTime;
  protected final OptimizerConfig config;

  public BaseExecutor(NodeTask task, ArcticTable table, long startTime, OptimizerConfig config) {
    this.task = task;
    this.table = table;
    this.startTime = startTime;
    this.config = config;
  }

  protected Map<DataTreeNode, List<DataFile>> groupDataFilesByNode(List<DataFile> dataFiles) {
    return new HashMap<>(dataFiles.stream().collect(Collectors.groupingBy(dataFile ->
        FileUtil.parseFileNodeFromFileName(dataFile.path().toString()))));
  }

  protected Map<DataTreeNode, List<DeleteFile>> groupDeleteFilesByNode(List<DeleteFile> deleteFiles) {
    return new HashMap<>(deleteFiles.stream().collect(Collectors.groupingBy(deleteFile ->
        FileUtil.parseFileNodeFromFileName(deleteFile.path().toString()))));
  }

  protected long getMaxTransactionId(List<DataFile> dataFiles) {
    OptionalLong maxTransactionId = dataFiles.stream()
        .mapToLong(file -> FileUtil.parseFileTidFromFileName(file.path().toString())).max();
    if (maxTransactionId.isPresent()) {
      return maxTransactionId.getAsLong();
    }

    return 0;
  }

  protected <F extends ContentFile<F>> OptimizeTaskResult<F> buildOptimizeResult(Iterable<F> targetFiles)
      throws InvocationTargetException, IllegalAccessException {
    long totalFileSize = 0;
    List<ByteBuffer> baseFileBytesList = new ArrayList<>();
    for (F targetFile : targetFiles) {
      totalFileSize += targetFile.fileSizeInBytes();
      baseFileBytesList.add(SerializationUtil.toByteBuffer(targetFile));
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

    OptimizeTaskResult<F> result = new OptimizeTaskResult<>();
    result.setTargetFiles(targetFiles);
    result.setOptimizeTaskStat(optimizeTaskStat);
    return result;
  }
}

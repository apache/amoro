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

import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.exception.TimeoutException;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.FileUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public abstract class BaseExecutor<F extends ContentFile<F>> implements Executor<F> {
  private static final Logger LOG = LoggerFactory.getLogger(BaseExecutor.class);

  protected final NodeTask task;
  protected final ArcticTable table;
  protected final OptimizerConfig config;
  protected final long startTime;
  protected double factor = 0.9;

  public BaseExecutor(NodeTask nodeTask,
                      ArcticTable table,
                      long startTime,
                      OptimizerConfig config) {
    this.startTime = startTime;
    this.task = nodeTask;
    this.table = table;
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

  protected void executeTimeout(Closeable writer) throws Exception {
    long maxExecuteTime = task.getMaxExecuteTime() != null ?
        task.getMaxExecuteTime() : TableProperties.OPTIMIZE_EXECUTE_TIMEOUT_DEFAULT;
    if (System.currentTimeMillis() - startTime > maxExecuteTime * factor) {
      writer.close();
      LOG.info("table {} execute task {} timeout, max execute time is {}",
          table.id(), task.getTaskId(), task.getMaxExecuteTime());
      throw new TimeoutException("optimizer execute timeout");
    }
  }
}

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

package com.netease.arctic.optimizer.operator;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.netease.arctic.ams.api.ErrorMessage;
import com.netease.arctic.ams.api.JobId;
import com.netease.arctic.ams.api.JobType;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeTask;
import com.netease.arctic.ams.api.OptimizeTaskStat;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.TaskWrapper;
import com.netease.arctic.optimizer.operator.executor.Executor;
import com.netease.arctic.optimizer.operator.executor.ExecutorFactory;
import com.netease.arctic.optimizer.operator.executor.NodeTask;
import com.netease.arctic.optimizer.operator.executor.OptimizeTaskResult;
import com.netease.arctic.optimizer.operator.executor.TableIdentificationInfo;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Execute task.
 */
public class BaseTaskExecutor implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(BaseTaskExecutor.class);

  private static final LoadingCache<TableIdentificationInfo, ArcticTable> ARCTIC_TABLE_CACHE = Caffeine.newBuilder()
      .expireAfterWrite(15, TimeUnit.MINUTES)
      .maximumSize(100)
      .build(BaseTaskExecutor::buildArcticTable);

  private final OptimizerConfig config;

  private final ExecuteListener listener;

  public interface ExecuteListener {
    default void onTaskStart(Iterable<ContentFile<?>> inputFiles) {
    }

    default void onTaskFinish(Iterable<ContentFile<?>> outputFiles) {
    }

    default void onTaskFailed(Throwable t) {
    }
  }

  public BaseTaskExecutor(OptimizerConfig config) {
    this(config, null);
  }

  public BaseTaskExecutor(OptimizerConfig config,
                          ExecuteListener listener) {
    this.config = config;
    this.listener = listener;
  }

  /**
   * Execute task.
   *
   * @param sourceTask -
   * @return task execute result
   */
  public OptimizeTaskStat execute(TaskWrapper sourceTask) {
    long startTime = System.currentTimeMillis();
    NodeTask task;
    ArcticTable table;
    LOG.info("start execute {}", sourceTask.getTask().getTaskId());
    try {
      task = constructTask(sourceTask.getTask(), sourceTask.getAttemptId());
    } catch (Throwable t) {
      LOG.error("failed to build task {}", sourceTask.getTask(), t);
      throw new IllegalArgumentException(t);
    }
    onTaskStart(task.files());
    try {
      String amsUrl = config.getAmsUrl();
      table = getArcticTable(new TableIdentificationInfo(amsUrl, task.getTableIdentifier()));
      setPartition(table, task);
    } catch (Exception e) {
      LOG.error("failed to set partition info {}", task.getTaskId(), e);
      onTaskFailed(e);
      return constructFailedResult(task, e);
    }
    Executor<?> optimize = ExecutorFactory.constructOptimize(task, table, startTime, config);
    try {
      OptimizeTaskResult<?> result = optimize.execute();
      onTaskFinish(result.getTargetFiles());
      return result.getOptimizeTaskStat();
    } catch (Throwable t) {
      LOG.error("failed to execute task {}", task.getTaskId(), t);
      onTaskFailed(t);
      return constructFailedResult(task, t);
    } finally {
      optimize.close();
    }
  }

  private void onTaskStart(Iterable<ContentFile<?>> inputFiles) {
    if (listener != null) {
      listener.onTaskStart(inputFiles);
    }
  }

  private void onTaskFinish(Iterable<? extends ContentFile<?>> outputFiles) {
    List<ContentFile<?>> targetFiles = new ArrayList<>();
    for (ContentFile<?> outputFile : outputFiles) {
      targetFiles.add(outputFile);
    }
    if (listener != null) {
      listener.onTaskFinish(targetFiles);
    }
  }

  private void onTaskFailed(Throwable t) {
    if (listener != null) {
      listener.onTaskFailed(t);
    }
  }

  private static ArcticTable getArcticTable(TableIdentificationInfo tableIdentificationInfo) {
    Preconditions.checkNotNull(tableIdentificationInfo);
    return ARCTIC_TABLE_CACHE.get(tableIdentificationInfo);
  }

  private static ArcticTable buildArcticTable(TableIdentificationInfo tableIdentifierInfo) {
    LOG.info("loading a new table : {}", tableIdentifierInfo);
    try {
      ArcticTable arcticTable = buildTable(tableIdentifierInfo);
      LOG.info("loaded a new table : {}", tableIdentifierInfo);
      return arcticTable;
    } catch (Exception e) {
      LOG.error("failed to load arctic table " + tableIdentifierInfo + ", retry", e);
      return buildTable(tableIdentifierInfo);
    }
  }

  private static ArcticTable buildTable(TableIdentificationInfo tableIdentifierInfo) {
    String amsUrl = tableIdentifierInfo.getAmsUrl();
    amsUrl = amsUrl.trim();
    if (!amsUrl.endsWith("/")) {
      amsUrl = amsUrl + "/";
    }
    ArcticCatalog arcticCatalog = CatalogLoader.load(amsUrl + tableIdentifierInfo.getTableIdentifier().getCatalog());
    return arcticCatalog.loadTable(tableIdentifierInfo.getTableIdentifier());
  }

  private void setPartition(ArcticTable arcticTable, NodeTask nodeTask) {
    // partition
    if (nodeTask.files().size() == 0) {
      LOG.warn("task: {} no files to optimize.", nodeTask.getTaskId());
    } else {
      nodeTask.setPartition(nodeTask.files().get(0).partition());
    }
  }

  private OptimizeTaskStat constructFailedResult(NodeTask task, Throwable t) {
    try {
      OptimizeTaskStat optimizeTaskStat = new OptimizeTaskStat();
      // TODO refactor
      BeanUtils.copyProperties(optimizeTaskStat, task);
      JobId jobId = new JobId();
      jobId.setId(config.getOptimizerId());
      jobId.setType(JobType.Optimize);
      optimizeTaskStat.setJobId(jobId);
      optimizeTaskStat.setStatus(OptimizeStatus.Failed);
      optimizeTaskStat.setTableIdentifier(task.getTableIdentifier().buildTableIdentifier());
      optimizeTaskStat.setAttemptId(task.getAttemptId() + "");
      optimizeTaskStat.setTaskId(task.getTaskId());

      optimizeTaskStat.setErrorMessage(new ErrorMessage(System.currentTimeMillis(), buildErrorMessage(t, 3)));
      return optimizeTaskStat;
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

  private String buildErrorMessage(Throwable t, int deep) {
    StringBuilder message = new StringBuilder();
    Throwable error = t;
    int i = 0;
    while (i++ < deep && error != null) {
      if (i > 1) {
        message.append(". caused by ");
      }
      message.append(error.getMessage());
      error = error.getCause();
    }
    return message.toString();
  }

  private NodeTask constructTask(OptimizeTask task, int attemptId) {
    NodeTask nodeTask = new NodeTask();
    if (CollectionUtils.isNotEmpty(task.getSourceNodes())) {
      nodeTask.setSourceNodes(
          task.getSourceNodes().stream().map(BaseTaskExecutor::toTreeNode).collect(Collectors.toSet()));
    }
    nodeTask.setTableIdentifier(toTableIdentifier(task.getTableIdentifier()));
    nodeTask.setTaskId(task.getTaskId());
    nodeTask.setAttemptId(attemptId);

    for (ByteBuffer file : task.getBaseFiles()) {
      nodeTask.addFile(SerializationUtil.toInternalTableFile(file), DataFileType.BASE_FILE);
    }
    for (ByteBuffer file : task.getInsertFiles()) {
      nodeTask.addFile(SerializationUtil.toInternalTableFile(file), DataFileType.INSERT_FILE);
    }
    for (ByteBuffer file : task.getDeleteFiles()) {
      nodeTask.addFile(SerializationUtil.toInternalTableFile(file), DataFileType.EQ_DELETE_FILE);
    }
    for (ByteBuffer file : task.getPosDeleteFiles()) {
      nodeTask.addFile(SerializationUtil.toInternalTableFile(file), DataFileType.POS_DELETE_FILE);
    }

    Map<String, String> properties = task.getProperties();
    if (properties != null) {
      String allFileCnt = properties.get("all-file-cnt");
      int fileCnt = nodeTask.baseFiles().size() + nodeTask.insertFiles().size() + nodeTask.deleteFiles().size();
      if (allFileCnt != null && Integer.parseInt(allFileCnt) != fileCnt) {
        LOG.error("{} check file cnt error, expected {}, actual {}, {}, value = {}", task.getTaskId(), allFileCnt,
            fileCnt, nodeTask, task);
        throw new IllegalStateException("check file cnt error");
      }
    }

    return nodeTask;
  }

  private static DataTreeNode toTreeNode(com.netease.arctic.ams.api.TreeNode treeNode) {
    if (treeNode == null) {
      return null;
    }
    return DataTreeNode.of(treeNode.getMask(), treeNode.getIndex());
  }

  public static com.netease.arctic.table.TableIdentifier toTableIdentifier(
      TableIdentifier tableIdentifier) {
    if (tableIdentifier == null) {
      return null;
    }
    return com.netease.arctic.table.TableIdentifier.of(tableIdentifier.getCatalog(),
        tableIdentifier.getDatabase(), tableIdentifier.getTableName());
  }
}

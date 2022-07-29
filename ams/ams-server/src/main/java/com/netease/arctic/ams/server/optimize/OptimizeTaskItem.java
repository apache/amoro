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

import com.netease.arctic.ams.api.ErrorMessage;
import com.netease.arctic.ams.api.JobId;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.server.mapper.InternalTableFilesMapper;
import com.netease.arctic.ams.server.mapper.OptimizeTaskRuntimesMapper;
import com.netease.arctic.ams.server.mapper.OptimizeTasksMapper;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.TableTaskHistory;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OptimizeTaskItem extends IJDBCService {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizeTaskItem.class);
  private static final long MAX_EXECUTE_TIME = 7200_000;// 2 hour
  // interval between failed and retry = (1 + retry) * RETRY_INTERVAL
  private static final long RETRY_INTERVAL = 60000; // 60s

  private final BaseOptimizeTask optimizeTask;
  private final BaseOptimizeTaskRuntime optimizeRuntime;
  private final ReentrantLock lock = new ReentrantLock();

  public OptimizeTaskItem(BaseOptimizeTask optimizeTask,
                          BaseOptimizeTaskRuntime optimizeRuntime) {
    this.optimizeTask = optimizeTask;
    this.optimizeRuntime = optimizeRuntime;
  }

  public BaseOptimizeTask getOptimizeTask() {
    return optimizeTask;
  }

  public BaseOptimizeTaskRuntime getOptimizeRuntime() {
    return optimizeRuntime;
  }

  public TableIdentifier getTableIdentifier() {
    return new TableIdentifier(optimizeTask.getTableIdentifier());
  }

  public OptimizeStatus getOptimizeStatus() {
    return optimizeRuntime.getStatus();
  }

  public void onPending() {
    lock.lock();
    try {
      if (optimizeRuntime.getStatus() == OptimizeStatus.Failed) {
        optimizeRuntime.setRetry(optimizeRuntime.getRetry() + 1);
      }
      optimizeRuntime.setPendingTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Pending);
      persistTaskRuntime(false);
    } finally {
      lock.unlock();
    }
  }

  public TableTaskHistory onExecuting(JobId jobId, String attemptId) {
    lock.lock();
    try {
      long currentTime = System.currentTimeMillis();
      optimizeRuntime.setAttemptId(attemptId);
      optimizeRuntime.setExecuteTime(currentTime);
      optimizeRuntime.setJobId(jobId);
      optimizeRuntime.setStatus(OptimizeStatus.Executing);
      persistTaskRuntime(false);
      return constructTableTaskHistory(currentTime);
    } catch (Throwable t) {
      onFailed(new ErrorMessage(System.currentTimeMillis(),
          "internal error, failed to set task status to Executing, set to Failed"), 0);
      throw t;
    } finally {
      lock.unlock();
    }
  }

  public void onCommitted(long commitTime) {
    lock.lock();
    try {
      optimizeRuntime.setCommitTime(commitTime);
      optimizeRuntime.setStatus(OptimizeStatus.Committed);
      // after commit if will be delete, there is no need to update
      persistTaskRuntime(false);
    } finally {
      lock.unlock();
    }
  }

  public void onFailed(ErrorMessage errorMessage, long costTime) {
    long reportTime = System.currentTimeMillis();
    lock.lock();
    try {
      optimizeRuntime.setErrorMessage(errorMessage);
      optimizeRuntime.setStatus(OptimizeStatus.Failed);
      optimizeRuntime.setReportTime(reportTime);
      optimizeRuntime.setAttemptId(null);
      optimizeRuntime.setCostTime(costTime);
      persistTaskRuntime(false);
    } finally {
      lock.unlock();
    }
  }

  public void onPrepared(long preparedTime, List<ByteBuffer> targetFiles, long newFileSize, long costTime) {
    long reportTime = System.currentTimeMillis();
    lock.lock();
    try {
      if (optimizeRuntime.getExecuteTime() == BaseOptimizeTaskRuntime.INVALID_TIME) {
        optimizeRuntime.setExecuteTime(preparedTime);
      }
      optimizeRuntime.setPreparedTime(preparedTime);
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(reportTime);
      optimizeRuntime.setNewFileCnt(targetFiles == null ? 0 : targetFiles.size());
      optimizeRuntime.setNewFileSize(newFileSize);
      optimizeRuntime.setTargetFiles(targetFiles);
      optimizeRuntime.setCostTime(costTime);
      persistTaskRuntime(true);
    } finally {
      lock.unlock();
    }
  }

  public boolean canExecute(Supplier<Integer> maxRetry) {
    if (getOptimizeStatus() == OptimizeStatus.Init) {
      return true;
    } else if (getOptimizeStatus() == OptimizeStatus.Failed) {
      return getOptimizeRuntime().getRetry() <= maxRetry.get() && System.currentTimeMillis() >
          getOptimizeRuntime().getFailTime() + RETRY_INTERVAL;
    }
    return false;
  }

  public boolean executeTimeout() {
    if (getOptimizeStatus() == OptimizeStatus.Executing) {
      return System.currentTimeMillis() - optimizeRuntime.getExecuteTime() > MAX_EXECUTE_TIME;
    }
    return false;
  }

  public boolean canCommit() {
    return getOptimizeStatus() == OptimizeStatus.Prepared;
  }

  public OptimizeTaskId getTaskId() {
    return optimizeTask.getTaskId();
  }

  @Override
  public String toString() {
    return "OptimizeTaskItem{" +
        "optimizeTask=" + optimizeTask +
        ", optimizeRuntime=" + optimizeRuntime +
        '}';
  }

  public void clearFiles() {
    this.optimizeTask.setDeleteFiles(Collections.emptyList());
    this.optimizeTask.setInsertFiles(Collections.emptyList());
    this.optimizeTask.setBaseFiles(Collections.emptyList());
    this.optimizeTask.setPosDeleteFiles(Collections.emptyList());
  }

  public void setFiles() {
    List<ByteBuffer> insertFiles = selectOptimizeTaskFiles(DataFileType.INSERT_FILE, 0)
        .stream().map(SerializationUtil::byteArrayToByteBuffer).collect(Collectors.toList());
    List<ByteBuffer> deleteFiles = selectOptimizeTaskFiles(DataFileType.EQ_DELETE_FILE, 0)
        .stream().map(SerializationUtil::byteArrayToByteBuffer).collect(Collectors.toList());
    List<ByteBuffer> baseFiles = selectOptimizeTaskFiles(DataFileType.BASE_FILE, 0)
        .stream().map(SerializationUtil::byteArrayToByteBuffer).collect(Collectors.toList());
    List<ByteBuffer> posDeleteFiles = selectOptimizeTaskFiles(DataFileType.POS_DELETE_FILE, 0)
        .stream().map(SerializationUtil::byteArrayToByteBuffer).collect(Collectors.toList());
    optimizeTask.setInsertFiles(insertFiles);
    optimizeTask.setDeleteFiles(deleteFiles);
    optimizeTask.setBaseFiles(baseFiles);
    optimizeTask.setPosDeleteFiles(posDeleteFiles);
    // for ams restart, files is not loaded from sysdb, reload here
    List<byte[]> targetFiles =
        selectOptimizeTaskFiles(DataFileType.BASE_FILE, 1);
    targetFiles.addAll(
        selectOptimizeTaskFiles(DataFileType.POS_DELETE_FILE, 1));
    optimizeRuntime.setTargetFiles(targetFiles.stream()
        .map(SerializationUtil::byteArrayToByteBuffer).collect(Collectors.toList()));
  }

  private List<byte[]> selectOptimizeTaskFiles(DataFileType fileType, int isTarget) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      InternalTableFilesMapper internalTableFilesMapper =
          getMapper(sqlSession, InternalTableFilesMapper.class);

      return internalTableFilesMapper.selectOptimizeTaskFiles(getTaskId(), fileType, isTarget);
    }
  }

  private void persistTaskRuntime(boolean updateTargetFiles) {
    try (SqlSession sqlSession = getSqlSession(false)) {
      OptimizeTaskRuntimesMapper optimizeTaskRuntimesMapper =
          getMapper(sqlSession, OptimizeTaskRuntimesMapper.class);
      InternalTableFilesMapper internalTableFilesMapper =
          getMapper(sqlSession, InternalTableFilesMapper.class);

      optimizeTaskRuntimesMapper.updateOptimizeTaskRuntime(optimizeRuntime);
      if (updateTargetFiles) {
        try {
          internalTableFilesMapper.deleteOptimizeTaskTargetFile(optimizeTask.getTaskId());
          optimizeRuntime.getTargetFiles().forEach(file -> {
            ContentFile<?> contentFile = SerializationUtil.toInternalTableFile(file);
            if (contentFile instanceof DataFile) {
              internalTableFilesMapper.insertOptimizeTaskFile(optimizeTask.getTaskId(),
                  DataFileType.BASE_FILE, 1, SerializationUtil.byteBufferToByteArray(file));
            } else {
              internalTableFilesMapper.insertOptimizeTaskFile(optimizeTask.getTaskId(),
                  DataFileType.POS_DELETE_FILE, 1, SerializationUtil.byteBufferToByteArray(file));
            }
          });
        } catch (Exception e) {
          LOG.error("Update the internal table files failed.", e);
          sqlSession.rollback(true);
          throw e;
        }
      }
      sqlSession.commit(true);
    }
  }

  private TableTaskHistory constructTableTaskHistory(long currentTime) {
    TableTaskHistory tableTaskHistory = new TableTaskHistory();
    tableTaskHistory.setTableIdentifier(new TableIdentifier(optimizeTask.getTableIdentifier()));
    tableTaskHistory.setTaskGroupId(optimizeTask.getTaskGroup());
    tableTaskHistory.setTaskHistoryId(optimizeTask.getTaskHistoryId());
    tableTaskHistory.setStartTime(currentTime);
    tableTaskHistory.setQueueId(optimizeTask.getQueueId());

    return tableTaskHistory;
  }

  public void persistOptimizeTask() {
    try (SqlSession sqlSession = getSqlSession(false)) {

      OptimizeTasksMapper optimizeTasksMapper =
          getMapper(sqlSession, OptimizeTasksMapper.class);
      InternalTableFilesMapper internalTableFilesMapper =
          getMapper(sqlSession, InternalTableFilesMapper.class);

      BaseOptimizeTask optimizeTask = getOptimizeTask();
      OptimizeTaskId optimizeTaskId = optimizeTask.getTaskId();
      try {
        optimizeTasksMapper.insertOptimizeTask(optimizeTask, getOptimizeRuntime());
        optimizeTask.getInsertFiles()
            .forEach(f -> internalTableFilesMapper
                .insertOptimizeTaskFile(optimizeTaskId,
                    DataFileType.INSERT_FILE,
                    0,
                    SerializationUtil.byteBufferToByteArray(f)));
        optimizeTask.getDeleteFiles()
            .forEach(f -> internalTableFilesMapper
                .insertOptimizeTaskFile(optimizeTaskId,
                    DataFileType.EQ_DELETE_FILE,
                    0,
                    SerializationUtil.byteBufferToByteArray(f)));
        optimizeTask.getBaseFiles()
            .forEach(f -> internalTableFilesMapper
                .insertOptimizeTaskFile(optimizeTaskId,
                    DataFileType.BASE_FILE,
                    0,
                    SerializationUtil.byteBufferToByteArray(f)));
        optimizeTask.getPosDeleteFiles()
            .forEach(f -> internalTableFilesMapper
                .insertOptimizeTaskFile(optimizeTaskId,
                    DataFileType.POS_DELETE_FILE,
                    0,
                    SerializationUtil.byteBufferToByteArray(f)));

        sqlSession.commit(true);
      } catch (Exception e) {
        LOG.warn("failed to insert optimize task in meta store, ignore. " + optimizeTask, e);
        sqlSession.rollback(true);
        throw e;
      }
    }
  }

  public void clearOptimizeTask() {
    OptimizeTaskId taskId = getTaskId();
    try (SqlSession sqlSession = getSqlSession(false)) {
      OptimizeTasksMapper optimizeTasksMapper =
          getMapper(sqlSession, OptimizeTasksMapper.class);
      InternalTableFilesMapper internalTableFilesMapper =
          getMapper(sqlSession, InternalTableFilesMapper.class);
      
      try {
        optimizeTasksMapper.deleteOptimizeTask(taskId.getTraceId());
        internalTableFilesMapper.deleteOptimizeTaskFile(taskId);

        sqlSession.commit(true);
      } catch (Exception e) {
        LOG.warn("failed to clean optimize task in meta store, ignore. " + taskId, e);
        sqlSession.rollback(true);
      }
    }
  }
}

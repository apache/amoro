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

import com.google.common.base.Preconditions;
import com.netease.arctic.ams.api.ErrorMessage;
import com.netease.arctic.ams.api.JobId;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.server.mapper.InternalTableFilesMapper;
import com.netease.arctic.ams.server.mapper.OptimizeTaskRuntimesMapper;
import com.netease.arctic.ams.server.mapper.OptimizeTasksMapper;
import com.netease.arctic.ams.server.mapper.TaskHistoryMapper;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.TableTaskHistory;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.FileContent;
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
  // interval between failed and retry = (1 + retry) * RETRY_INTERVAL
  private static final long RETRY_INTERVAL = 60000; // 60s

  private final BaseOptimizeTask optimizeTask;
  private volatile BaseOptimizeTaskRuntime optimizeRuntime;
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

  public void setOptimizeRuntime(BaseOptimizeTaskRuntime optimizeRuntime) {
    this.optimizeRuntime = optimizeRuntime;
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
      Preconditions.checkArgument(optimizeRuntime.getStatus() != OptimizeStatus.Prepared,
          "task prepared, can't on pending");
      BaseOptimizeTaskRuntime newRuntime = optimizeRuntime.clone();
      if (newRuntime.getStatus() == OptimizeStatus.Failed) {
        newRuntime.setRetry(newRuntime.getRetry() + 1);
      }
      newRuntime.setPendingTime(System.currentTimeMillis());
      newRuntime.setStatus(OptimizeStatus.Pending);
      persistTaskRuntime(newRuntime, false);
      optimizeRuntime = newRuntime;
    } finally {
      lock.unlock();
    }
  }

  public TableTaskHistory onExecuting(JobId jobId, String attemptId) {
    lock.lock();
    try {
      Preconditions.checkArgument(optimizeRuntime.getStatus() != OptimizeStatus.Prepared,
          "task prepared, can't on executing");
      BaseOptimizeTaskRuntime newRuntime = optimizeRuntime.clone();
      long currentTime = System.currentTimeMillis();
      newRuntime.setAttemptId(attemptId);
      newRuntime.setExecuteTime(currentTime);
      newRuntime.setJobId(jobId);
      newRuntime.setStatus(OptimizeStatus.Executing);
      newRuntime.setPreparedTime(BaseOptimizeTaskRuntime.INVALID_TIME);
      newRuntime.setCostTime(0);
      newRuntime.setErrorMessage(null);
      persistTaskRuntime(newRuntime, false);
      optimizeRuntime = newRuntime;
      return constructNewTableTaskHistory(currentTime);
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
      BaseOptimizeTaskRuntime newRuntime = optimizeRuntime.clone();
      newRuntime.setCommitTime(commitTime);
      newRuntime.setStatus(OptimizeStatus.Committed);
      // after commit, task will be deleted, there is no need to update
      persistTaskRuntime(newRuntime, false);
      optimizeRuntime = newRuntime;
    } finally {
      lock.unlock();
    }
  }

  public void onFailed(ErrorMessage errorMessage, long costTime) {
    long reportTime = System.currentTimeMillis();
    lock.lock();
    try {
      Preconditions.checkArgument(optimizeRuntime.getStatus() != OptimizeStatus.Prepared,
          "task prepared, can't on failed");
      BaseOptimizeTaskRuntime newRuntime = optimizeRuntime.clone();
      newRuntime.setErrorMessage(errorMessage);
      newRuntime.setStatus(OptimizeStatus.Failed);
      newRuntime.setPreparedTime(BaseOptimizeTaskRuntime.INVALID_TIME);
      newRuntime.setReportTime(reportTime);
      newRuntime.setAttemptId(null);
      newRuntime.setCostTime(costTime);
      persistTaskRuntime(newRuntime, false);
      optimizeRuntime = newRuntime;

      updateTableTaskHistory();
    } finally {
      lock.unlock();
    }
  }

  public void onPrepared(long preparedTime, List<ByteBuffer> targetFiles, long newFileSize, long costTime) {
    long reportTime = System.currentTimeMillis();
    lock.lock();
    try {
      Preconditions.checkArgument(optimizeRuntime.getStatus() != OptimizeStatus.Prepared,
          "task prepared, can't on prepared");
      BaseOptimizeTaskRuntime newRuntime = optimizeRuntime.clone();
      if (newRuntime.getExecuteTime() == BaseOptimizeTaskRuntime.INVALID_TIME) {
        newRuntime.setExecuteTime(preparedTime);
      }
      newRuntime.setPreparedTime(preparedTime);
      newRuntime.setStatus(OptimizeStatus.Prepared);
      newRuntime.setReportTime(reportTime);
      newRuntime.setNewFileCnt(targetFiles == null ? 0 : targetFiles.size());
      newRuntime.setNewFileSize(newFileSize);
      newRuntime.setTargetFiles(targetFiles);
      newRuntime.setCostTime(costTime);
      persistTaskRuntime(newRuntime, true);
      optimizeRuntime = newRuntime;

      updateTableTaskHistory();
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

  public boolean executeTimeout(Supplier<Long> maxExecuteTime) {
    if (getOptimizeStatus() == OptimizeStatus.Executing) {
      return System.currentTimeMillis() - optimizeRuntime.getExecuteTime() > maxExecuteTime.get();
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

  private void persistTaskRuntime(BaseOptimizeTaskRuntime newRuntime, boolean updateTargetFiles) {
    try (SqlSession sqlSession = getSqlSession(false)) {
      OptimizeTaskRuntimesMapper optimizeTaskRuntimesMapper =
          getMapper(sqlSession, OptimizeTaskRuntimesMapper.class);
      InternalTableFilesMapper internalTableFilesMapper =
          getMapper(sqlSession, InternalTableFilesMapper.class);

      optimizeTaskRuntimesMapper.updateOptimizeTaskRuntime(newRuntime);
      if (updateTargetFiles) {
        try {
          internalTableFilesMapper.deleteOptimizeTaskTargetFile(optimizeTask.getTaskId());
          newRuntime.getTargetFiles().forEach(file -> {
            ContentFile<?> contentFile = SerializationUtil.toInternalTableFile(file);
            if (contentFile.content() == FileContent.DATA) {
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

  public void persistTargetFiles() {
    try (SqlSession sqlSession = getSqlSession(false)) {
      InternalTableFilesMapper internalTableFilesMapper =
          getMapper(sqlSession, InternalTableFilesMapper.class);

      try {
        internalTableFilesMapper.deleteOptimizeTaskTargetFile(optimizeTask.getTaskId());
        optimizeRuntime.getTargetFiles().forEach(file -> {
          ContentFile<?> contentFile = SerializationUtil.toInternalTableFile(file);
          if (contentFile.content() == FileContent.DATA) {
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

      sqlSession.commit(true);
    }
  }

  private TableTaskHistory constructNewTableTaskHistory(long currentTime) {
    TableTaskHistory tableTaskHistory = new TableTaskHistory();
    tableTaskHistory.setTableIdentifier(new TableIdentifier(optimizeTask.getTableIdentifier()));
    tableTaskHistory.setTaskPlanGroup(optimizeTask.getTaskPlanGroup());
    tableTaskHistory.setTaskTraceId(optimizeTask.getTaskId().getTraceId());
    tableTaskHistory.setRetry(optimizeRuntime.getRetry());
    tableTaskHistory.setStartTime(currentTime);
    tableTaskHistory.setQueueId(optimizeTask.getQueueId());

    return tableTaskHistory;
  }

  private void updateTableTaskHistory() {
    TableTaskHistory tableTaskHistory = new TableTaskHistory();
    tableTaskHistory.setTableIdentifier(new TableIdentifier(optimizeTask.getTableIdentifier()));
    tableTaskHistory.setTaskPlanGroup(optimizeTask.getTaskPlanGroup());
    tableTaskHistory.setTaskTraceId(optimizeTask.getTaskId().getTraceId());
    tableTaskHistory.setRetry(optimizeRuntime.getRetry());
    tableTaskHistory.setQueueId(optimizeTask.getQueueId());

    tableTaskHistory.setStartTime(optimizeRuntime.getExecuteTime());
    tableTaskHistory.setEndTime(optimizeRuntime.getReportTime());
    tableTaskHistory.setCostTime(optimizeRuntime.getCostTime());

    try (SqlSession sqlSession = getSqlSession(true)) {
      TaskHistoryMapper taskHistoryMapper = getMapper(sqlSession, TaskHistoryMapper.class);
      try {
        taskHistoryMapper.updateTaskHistory(tableTaskHistory);
      } catch (Exception e) {
        LOG.error("failed to update task history, tableId is {}, traceId is {}, retry times is {}",
            optimizeTask.getTableIdentifier(),
            optimizeTask.getTaskId().getTraceId(),
            optimizeRuntime.getRetry());
      }
    }
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

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

package com.netease.arctic.server.optimizing;

import com.google.common.collect.Sets;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.dashboard.utils.OptimizingUtil;
import com.netease.arctic.server.exception.DuplicateRuntimeException;
import com.netease.arctic.server.exception.IllegalTaskStateException;
import com.netease.arctic.server.exception.OptimizingClosedException;
import com.netease.arctic.server.optimizing.plan.TaskDescriptor;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.TaskFilesPersistence;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.utils.SerializationUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TaskRuntime extends StatedPersistentBase {

  private String partition;
  private OptimizingTaskId taskId;
  private Status status = Status.PLANNED;
  private TaskStatusMachine statusMachine;
  private int retry = 0;
  private long startTime = ArcticServiceConstants.INVALID_TIME;
  private long endTime = ArcticServiceConstants.INVALID_TIME;
  private long costTime = 0;
  private OptimizingQueue.OptimizingThread optimizingThread;
  private String failReason;
  private TaskOwner owner;
  private RewriteFilesInput input;
  private RewriteFilesOutput output;
  private ByteBuffer outputBytes;
  private MetricsSummary summary;
  private long tableId;
  private Map<String, String> properties;

  private TaskRuntime() {
  }

  public TaskRuntime(
      OptimizingTaskId taskId,
      TaskDescriptor taskDescriptor,
      Map<String, String> properties) {
    this.taskId = taskId;
    this.partition = taskDescriptor.getPartition();
    this.input = taskDescriptor.getInput();
    this.statusMachine = new TaskStatusMachine();
    this.summary = new MetricsSummary(input);
    this.tableId = taskDescriptor.getTableId();
    this.properties = properties;
  }

  public TaskRuntime claimOwnership(TaskOwner owner) {
    this.owner = owner;
    return this;
  }

  protected void setInput(RewriteFilesInput input) {
    if (input == null) {
      throw new IllegalStateException("Optimizing input is null, id:" + taskId);
    }
    this.input = input;
  }

  public RewriteFilesInput getInput() {
    return input;
  }

  public RewriteFilesOutput getOutput() {
    if (output == null && outputBytes != null) {
      return SerializationUtil.simpleDeserialize(outputBytes);
    }
    return output;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public long getProcessId() {
    return taskId.getProcessId();
  }

  public OptimizingQueue.OptimizingThread getOptimizingThread() {
    return optimizingThread;
  }

  public OptimizingTask getOptimizingTask() {
    OptimizingTask optimizingTask = new OptimizingTask(taskId);
    optimizingTask.setTaskInput(SerializationUtil.simpleSerialize(input));
    optimizingTask.setProperties(properties);
    return optimizingTask;
  }

  public long getStartTime() {
    return startTime;
  }

  public OptimizingTaskId getTaskId() {
    return taskId;
  }

  public Status getStatus() {
    return status;
  }

  public int getRetry() {
    return retry;
  }

  public MetricsSummary getMetricsSummary() {
    return summary;
  }

  public String getPartition() {
    return partition;
  }

  public String getFailReason() {
    return failReason;
  }

  public long getCostTime() {
    if (endTime != ArcticServiceConstants.INVALID_TIME) {
      long elapse = System.currentTimeMillis() - startTime;
      return Math.max(0, elapse) + costTime;
    }
    return costTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public long getQuotaTime(long calculatingStartTime, long calculatingEndTime) {
    if (startTime == ArcticServiceConstants.INVALID_TIME) {
      return 0;
    }
    calculatingStartTime = Math.min(startTime, calculatingEndTime);
    calculatingEndTime = costTime == ArcticServiceConstants.INVALID_TIME ? calculatingEndTime : costTime + startTime;
    long lastingTime = calculatingEndTime - calculatingStartTime;
    return Math.max(0, lastingTime);
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void addRetryCount() {
    retry++;
  }

  public MetricsSummary getSummary() {
    return summary;
  }

  public void setSummary(MetricsSummary summary) {
    this.summary = summary;
  }

  public long getTableId() {
    return tableId;
  }

  public void complete(OptimizingQueue.OptimizingThread thread, OptimizingTaskResult result) {
    validThread(thread);
    if (result.getErrorMessage() != null) {
      fail(result.getErrorMessage());
    } else {
      finish(TaskFilesPersistence.loadTaskOutput(result.getTaskOutput()));
    }
    owner.acceptResult(this);
    optimizingThread = null;
  }

  /**
   * Mix-Hive table need move file to hive location in Commit stage. so need to update output.
   *
   * @param filesOutput
   */
  public void updateOutput(RewriteFilesOutput filesOutput) {
    if (this.output == null) {
      throw new IllegalStateException("Old output must not be null");
    }
    statusMachine.accept(Status.SUCCESS);
    summary.setNewFileCnt(OptimizingUtil.getFileCount(filesOutput));
    summary.setNewFileSize(OptimizingUtil.getFileSize(filesOutput));
    output = filesOutput;
    this.outputBytes = SerializationUtil.simpleSerialize(filesOutput);
    persistTaskRuntime(this);
  }

  private void finish(RewriteFilesOutput filesOutput) {
    statusMachine.accept(Status.SUCCESS);
    summary.setNewFileCnt(OptimizingUtil.getFileCount(filesOutput));
    summary.setNewFileSize(OptimizingUtil.getFileSize(filesOutput));
    endTime = System.currentTimeMillis();
    costTime += endTime - startTime;
    output = filesOutput;
    this.outputBytes = SerializationUtil.simpleSerialize(filesOutput);
    persistTaskRuntime(this);
  }

  void fail(String errorMessage) {
    statusMachine.accept(Status.FAILED);
    failReason = errorMessage;
    endTime = System.currentTimeMillis();
    costTime += endTime - startTime;
    persistTaskRuntime(this);
  }

  void reset() {
    statusMachine.accept(Status.PLANNED);
    doAs(OptimizingMapper.class, mapper -> mapper.updateTaskStatus(this, Status.PLANNED));
  }

  void schedule(OptimizingQueue.OptimizingThread thread) {
    statusMachine.accept(Status.SCHEDULED);
    optimizingThread = thread;
    startTime = System.currentTimeMillis();
    persistTaskRuntime(this);
  }

  void ack(OptimizingQueue.OptimizingThread thread) {
    validThread(thread);
    statusMachine.accept(Status.ACKED);
    startTime = System.currentTimeMillis();
    endTime = ArcticServiceConstants.INVALID_TIME;
    persistTaskRuntime(this);
  }

  void tryCanceling() {
    if (statusMachine.tryAccepting(Status.CANCELED)) {
      costTime = System.currentTimeMillis() - startTime;
      persistTaskRuntime(this);
    }
  }

  private void validThread(OptimizingQueue.OptimizingThread thread) {
    if (!thread.equals(this.optimizingThread)) {
      throw new DuplicateRuntimeException("Task already acked by optimizer thread + " + thread);
    }
  }

  private void persistTaskRuntime(TaskRuntime taskRuntime) {
    doAs(OptimizingMapper.class, mapper -> mapper.updateTaskRuntime(taskRuntime));
  }

  public TaskQuota getCurrentQuota() {
    if (startTime == ArcticServiceConstants.INVALID_TIME || endTime == ArcticServiceConstants.INVALID_TIME) {
      throw new IllegalStateException("start time or end time is not correctly set");
    }
    return new TaskQuota(this);
  }

  public boolean isSuspending(long determineTime) {
    return status == TaskRuntime.Status.SCHEDULED &&
        determineTime - startTime > ArcticServiceConstants.MAX_SCHEDULING_TIME;
  }

  private static final Map<Status, Set<Status>> nextStatusMap = new HashMap<>();

  static {
    nextStatusMap.put(
        Status.PLANNED,
        Sets.newHashSet(
            Status.PLANNED,
            Status.SCHEDULED,
            Status.CANCELED));
    nextStatusMap.put(
        Status.SCHEDULED,
        Sets.newHashSet(
            Status.PLANNED,
            Status.SCHEDULED,
            Status.ACKED,
            Status.CANCELED));
    nextStatusMap.put(
        Status.ACKED,
        Sets.newHashSet(
            Status.PLANNED,
            Status.ACKED,
            Status.SUCCESS,
            Status.FAILED,
            Status.CANCELED));
    nextStatusMap.put(
        Status.FAILED,
        Sets.newHashSet(
            Status.PLANNED,
            Status.FAILED,
            Status.CANCELED));
    nextStatusMap.put(
        Status.SUCCESS,
        Sets.newHashSet(Status.SUCCESS));
    nextStatusMap.put(
        Status.CANCELED,
        Sets.newHashSet(Status.CANCELED));
  }

  private class TaskStatusMachine {

    private Set<Status> next;

    private TaskStatusMachine() {
      this.next = nextStatusMap.get(status);
    }

    public void accept(Status targetStatus) {
      if (owner.isClosed()) {
        throw new OptimizingClosedException(taskId.getProcessId());
      }
      if (!next.contains(targetStatus)) {
        throw new IllegalTaskStateException(taskId, status, targetStatus);
      }
      status = targetStatus;
      next = nextStatusMap.get(status);
    }

    public synchronized boolean tryAccepting(Status targetStatus) {
      if (owner.isClosed() || !next.contains(targetStatus)) {
        return false;
      }
      status = targetStatus;
      next = nextStatusMap.get(status);
      return true;
    }
  }

  public enum Status {
    PLANNED,
    SCHEDULED,
    ACKED,
    FAILED,
    SUCCESS,
    CANCELED
  }

  public static class TaskQuota {

    private long processId;
    private int taskId;
    private int retryNum;
    private long startTime;
    private long endTime;
    private String failReason;
    private long tableId;

    public TaskQuota() {

    }

    public TaskQuota(TaskRuntime task) {
      this.startTime = task.getStartTime();
      this.endTime = task.getEndTime();
      this.processId = task.getTaskId().getProcessId();
      this.taskId = task.getTaskId().getTaskId();
      this.tableId = task.getTableId();
    }

    public long getStartTime() {
      return startTime;
    }

    public long getProcessId() {
      return processId;
    }

    public int getTaskId() {
      return taskId;
    }


    public int getRetryNum() {
      return retryNum;
    }

    public long getEndTime() {
      return endTime;
    }

    public String getFailReason() {
      return failReason;
    }

    public long getTableId() {
      return tableId;
    }

    public long getQuotaTime(long calculatingStartTime) {
      long lastingTime = endTime - Math.max(startTime, calculatingStartTime);
      return Math.max(0, lastingTime);
    }

    public boolean checkExpired(long validTime) {
      return endTime <= validTime;
    }
  }

  public interface TaskOwner {
    void acceptResult(TaskRuntime taskRuntime);

    boolean isClosed();
  }
}

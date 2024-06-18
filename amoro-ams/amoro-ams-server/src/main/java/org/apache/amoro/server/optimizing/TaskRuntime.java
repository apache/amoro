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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.api.StateField;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.dashboard.utils.OptimizingUtil;
import org.apache.amoro.server.exception.IllegalTaskStateException;
import org.apache.amoro.server.exception.OptimizingClosedException;
import org.apache.amoro.server.exception.TaskRuntimeException;
import org.apache.amoro.server.optimizing.plan.TaskDescriptor;
import org.apache.amoro.server.persistence.StatedPersistentBase;
import org.apache.amoro.server.persistence.TaskFilesPersistence;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableSet;
import org.apache.amoro.utils.SerializationUtil;

import java.util.Map;
import java.util.Set;

public class TaskRuntime extends StatedPersistentBase {
  private long tableId;
  private String partition;
  private OptimizingTaskId taskId;
  @StateField private Status status = Status.PLANNED;
  private final TaskStatusMachine statusMachine = new TaskStatusMachine();
  @StateField private int runTimes = 0;
  @StateField private long startTime = AmoroServiceConstants.INVALID_TIME;
  @StateField private long endTime = AmoroServiceConstants.INVALID_TIME;
  @StateField private long costTime = AmoroServiceConstants.INVALID_TIME;
  @StateField private String token;
  @StateField private int threadId = -1;
  @StateField private String failReason;
  private TaskOwner owner;
  private RewriteFilesInput input;
  @StateField private RewriteFilesOutput output;
  @StateField private MetricsSummary summary;
  private Map<String, String> properties;

  private TaskRuntime() {}

  public TaskRuntime(
      OptimizingTaskId taskId, TaskDescriptor taskDescriptor, Map<String, String> properties) {
    this.taskId = taskId;
    this.partition = taskDescriptor.getPartition();
    this.input = taskDescriptor.getInput();
    this.summary = new MetricsSummary(input);
    this.tableId = taskDescriptor.getTableId();
    this.properties = properties;
  }

  public void complete(OptimizerThread thread, OptimizingTaskResult result) {
    invokeConsistency(
        () -> {
          validThread(thread);
          if (result.getErrorMessage() != null) {
            statusMachine.accept(Status.FAILED);
            failReason = result.getErrorMessage();
            endTime = System.currentTimeMillis();
            costTime += endTime - startTime;
          } else {
            statusMachine.accept(Status.SUCCESS);
            RewriteFilesOutput filesOutput =
                TaskFilesPersistence.loadTaskOutput(result.getTaskOutput());
            summary.setNewDataFileCnt(OptimizingUtil.getFileCount(filesOutput.getDataFiles()));
            summary.setNewDataSize(OptimizingUtil.getFileSize(filesOutput.getDataFiles()));
            summary.setNewDataRecordCnt(OptimizingUtil.getRecordCnt(filesOutput.getDataFiles()));
            summary.setNewDeleteFileCnt(OptimizingUtil.getFileCount(filesOutput.getDeleteFiles()));
            summary.setNewDeleteSize(OptimizingUtil.getFileSize(filesOutput.getDeleteFiles()));
            summary.setNewDeleteRecordCnt(
                OptimizingUtil.getRecordCnt(filesOutput.getDeleteFiles()));
            summary.setNewFileSize(summary.getNewDataSize() + summary.getNewDeleteSize());
            summary.setNewFileCnt(summary.getNewDataFileCnt() + summary.getNewDeleteFileCnt());
            endTime = System.currentTimeMillis();
            costTime += endTime - startTime;
            output = filesOutput;
          }
          runTimes += 1;
          persistTaskRuntime(this);
          owner.acceptResult(this);
          token = null;
          threadId = -1;
        });
    owner.releaseResourcesIfNecessary();
  }

  void reset() {
    invokeConsistency(
        () -> {
          statusMachine.accept(Status.PLANNED);
          startTime = AmoroServiceConstants.INVALID_TIME;
          endTime = AmoroServiceConstants.INVALID_TIME;
          token = null;
          threadId = -1;
          failReason = null;
          output = null;
          summary = new MetricsSummary(input);
          // The cost time should not be reset since it is the total cost time of all runs.
          persistTaskRuntime(this);
        });
  }

  public void schedule(OptimizerThread thread) {
    invokeConsistency(
        () -> {
          statusMachine.accept(Status.SCHEDULED);
          token = thread.getToken();
          threadId = thread.getThreadId();
          startTime = System.currentTimeMillis();
          persistTaskRuntime(this);
        });
  }

  public void ack(OptimizerThread thread) {
    invokeConsistency(
        () -> {
          validThread(thread);
          statusMachine.accept(Status.ACKED);
          persistTaskRuntime(this);
        });
  }

  void tryCanceling() {
    invokeConsistency(
        () -> {
          if (statusMachine.tryAccepting(Status.CANCELED)) {
            endTime = System.currentTimeMillis();
            if (startTime != AmoroServiceConstants.INVALID_TIME) {
              costTime += endTime - startTime;
            }
            persistTaskRuntime(this);
          }
        });
  }

  public TaskRuntime claimOwnership(TaskOwner owner) {
    this.owner = owner;
    return this;
  }

  public boolean finished() {
    return this.status == Status.SUCCESS
        || this.status == Status.FAILED
        || this.status == Status.CANCELED;
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

  public String getResourceDesc() {
    return token + ":" + threadId;
  }

  public String getToken() {
    return token;
  }

  public int getThreadId() {
    return threadId;
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

  public int getRunTimes() {
    return runTimes;
  }

  public int getRetry() {
    return runTimes - 1;
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
    return costTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public long getQuotaTime(long calculatingStartTime, long calculatingEndTime) {
    if (startTime == AmoroServiceConstants.INVALID_TIME) {
      return 0;
    }
    calculatingStartTime = Math.max(startTime, calculatingStartTime);
    calculatingEndTime =
        costTime == AmoroServiceConstants.INVALID_TIME ? calculatingEndTime : costTime + startTime;
    long lastingTime = calculatingEndTime - calculatingStartTime;
    return Math.max(0, lastingTime);
  }

  public void setStatus(Status status) {
    this.status = status;
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableId", tableId)
        .add("partition", partition)
        .add("taskId", taskId.getTaskId())
        .add("status", status)
        .add("runTimes", runTimes)
        .add("startTime", startTime)
        .add("endTime", endTime)
        .add("costTime", costTime)
        .add("resourceThread", getResourceDesc())
        .add("failReason", failReason)
        .add("summary", summary)
        .add("properties", properties)
        .toString();
  }

  private void validThread(OptimizerThread thread) {
    if (token == null) {
      throw new TaskRuntimeException("Task has been reset or not yet scheduled, taskId:%s", taskId);
    }
    if (!thread.getToken().equals(getToken()) || thread.getThreadId() != threadId) {
      throw new TaskRuntimeException(
          "The optimizer thread does not match, the thread in the task is OptimizerThread(token=%s, threadId=%s), and the thread in the request is OptimizerThread(token=%s, threadId=%s).",
          getToken(), threadId, thread.getToken(), thread.getThreadId());
    }
  }

  private void persistTaskRuntime(TaskRuntime taskRuntime) {
    doAs(OptimizingMapper.class, mapper -> mapper.updateTaskRuntime(taskRuntime));
  }

  public TaskQuota getCurrentQuota() {
    if (startTime == AmoroServiceConstants.INVALID_TIME
        || endTime == AmoroServiceConstants.INVALID_TIME) {
      throw new IllegalStateException("start time or end time is not correctly set");
    }
    return new TaskQuota(this);
  }

  private static final Map<Status, Set<Status>> nextStatusMap =
      ImmutableMap.<Status, Set<Status>>builder()
          .put(Status.PLANNED, ImmutableSet.of(Status.PLANNED, Status.SCHEDULED, Status.CANCELED))
          .put(
              Status.SCHEDULED,
              ImmutableSet.of(Status.PLANNED, Status.SCHEDULED, Status.ACKED, Status.CANCELED))
          .put(
              Status.ACKED,
              ImmutableSet.of(
                  Status.PLANNED, Status.ACKED, Status.SUCCESS, Status.FAILED, Status.CANCELED))
          .put(Status.FAILED, ImmutableSet.of(Status.PLANNED, Status.FAILED))
          .put(Status.SUCCESS, ImmutableSet.of(Status.SUCCESS))
          .put(Status.CANCELED, ImmutableSet.of(Status.CANCELED))
          .build();

  private class TaskStatusMachine {

    public void accept(Status targetStatus) {
      if (owner.isClosed()) {
        throw new OptimizingClosedException(taskId.getProcessId());
      }
      if (!getNext().contains(targetStatus)) {
        throw new IllegalTaskStateException(taskId, status, targetStatus);
      }
      status = targetStatus;
    }

    private Set<Status> getNext() {
      return nextStatusMap.get(status);
    }

    public synchronized boolean tryAccepting(Status targetStatus) {
      if (!getNext().contains(targetStatus)) {
        return false;
      }
      status = targetStatus;
      return true;
    }
  }

  public enum Status {
    PLANNED,
    SCHEDULED,
    ACKED,
    FAILED,
    SUCCESS,
    CANCELED // If Optimizing process failed, all tasks will be CANCELED except for SUCCESS tasks
  }

  public static class TaskQuota {

    private long processId;
    private int taskId;
    private int retryNum;
    private long startTime;
    private long endTime;
    private String failReason;
    private long tableId;

    public TaskQuota() {}

    public TaskQuota(TaskRuntime task) {
      this.startTime = task.getStartTime();
      this.endTime = task.getEndTime();
      this.processId = task.getTaskId().getProcessId();
      this.taskId = task.getTaskId().getTaskId();
      this.tableId = task.getTableId();
      this.retryNum = task.getRetry();
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

    void releaseResourcesIfNecessary();

    boolean isClosed();
  }
}

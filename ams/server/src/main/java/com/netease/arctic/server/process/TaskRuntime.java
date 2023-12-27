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

package com.netease.arctic.server.process;

import com.google.common.collect.Sets;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.StateField;
import com.netease.arctic.ams.api.exception.DuplicateRuntimeException;
import com.netease.arctic.ams.api.exception.IllegalTaskStateException;
import com.netease.arctic.ams.api.process.SimpleFuture;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.resource.OptimizerThread;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TaskRuntime<I, O> extends StatedPersistentBase {

  @StateField private Status status = Status.PLANNED;
  private final TaskStatusMachine statusMachine = new TaskStatusMachine();
  @StateField private int runTimes = 0;
  @StateField private long startTime = ArcticServiceConstants.INVALID_TIME;
  @StateField private long endTime = ArcticServiceConstants.INVALID_TIME;
  @StateField private long costTime = 0;
  @StateField private String token;
  @StateField private int threadId = -1;
  @StateField private String failReason;
  @StateField private String summary;
  private OptimizingTaskId taskId;
  private I input;
  private O output;
  private Map<String, String> properties;
  private TaskSummary<I, O> summaryBuilder;

  private volatile SimpleFuture completedFutuer = new SimpleFuture();

  private TaskRuntime() {}

  TaskRuntime(
      OptimizingTaskId taskId,
      I input,
      Map<String, String> properties,
      TaskSummary<I, O> summaryBuilder) {
    this.taskId = taskId;
    this.input = input;
    this.properties = properties;
    this.summaryBuilder = summaryBuilder;
    this.summary = summaryBuilder.buildSummary(input, null);
  }

  public void whenCompleted(Runnable action) {
    completedFutuer.whenCompleted(action);
  }

  public SimpleFuture getCompletedFuture() {
    return completedFutuer;
  }

  public void complete(OptimizerThread thread, OptimizingTaskResult result) {
    invokeConsistency(
        () -> {
          validThread(thread);
          if (result.getErrorMessage() != null) {
            saveFailReason(result.getErrorMessage());
          } else {
            saveSuccess(result.getTaskOutput());
          }
          token = null;
          threadId = -1;
          completedFutuer.complete();
        });
  }

  private void saveFailReason(String errorMessage) {
    statusMachine.accept(Status.FAILED);
    failReason = errorMessage;
    endTime = System.currentTimeMillis();
    costTime += endTime - startTime;
    runTimes += 1;
    persistTaskRuntime();
  }

  private void saveSuccess(byte[] outputBytes) {
    statusMachine.accept(Status.SUCCESS);
    endTime = System.currentTimeMillis();
    costTime += endTime - startTime;
    output = SerializationUtil.simpleDeserialize(outputBytes);
    summary = summaryBuilder.buildSummary(input, output);
    runTimes += 1;
    persistTaskRuntime();
  }

  public void reset() {
    invokeConsistency(
        () -> {
          statusMachine.accept(Status.PLANNED);
          token = null;
          threadId = -1;
          doAs(OptimizingMapper.class, mapper -> mapper.updateTaskStatus(this, Status.PLANNED));
        });
    SimpleFuture preFuture = completedFutuer;
    completedFutuer = new SimpleFuture();
    preFuture.complete();
  }

  public void schedule(OptimizerThread thread) {
    invokeConsistency(
        () -> {
          statusMachine.accept(Status.SCHEDULED);
          token = thread.getToken();
          threadId = thread.getThreadId();
          startTime = System.currentTimeMillis();
          persistTaskRuntime();
        });
  }

  public void ack(OptimizerThread thread) {
    invokeConsistency(
        () -> {
          validThread(thread);
          statusMachine.accept(Status.ACKED);
          startTime = System.currentTimeMillis();
          endTime = ArcticServiceConstants.INVALID_TIME;
          persistTaskRuntime();
        });
  }

  public void tryCanceling() {
    invokeConsistency(
        () -> {
          if (statusMachine.tryAccepting(Status.CANCELED)) {
            costTime = System.currentTimeMillis() - startTime;
            persistTaskRuntime();
          }
        });
  }

  public boolean finished() {
    return this.status == Status.SUCCESS
        || this.status == Status.FAILED
        || this.status == Status.CANCELED;
  }

  public OptimizingTask formatTask() {
    OptimizingTask optimizingTask = new OptimizingTask(taskId);
    optimizingTask.setTaskInput(SerializationUtil.simpleSerialize(getInput()));
    optimizingTask.setProperties(getProperties());
    return optimizingTask;
  }

  public void setInput(I input) {
    Preconditions.checkNotNull(input);
    this.input = input;
  }

  public I getInput() {
    return input;
  }

  public O getOutput() {
    return output;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public long getProcessId() {
    return getTaskId().getProcessId();
  }

  public String getResourceDesc() {
    return token + ":" + threadId;
  }

  public String getToken() {
    return token;
  }

  private void persistTaskRuntime() {
    doAs(OptimizingMapper.class, mapper -> mapper.updateTaskRuntime(this));
  }

  public long getQuotaRuntime() {
    return status == Status.SCHEDULED
        ? costTime + System.currentTimeMillis() - startTime
        : costTime;
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

  public String getFailReason() {
    return failReason;
  }

  public String getSummary() {
    return summary;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("taskId", getTaskId())
        .add("status", status)
        .add("runTimes", runTimes)
        .add("startTime", startTime)
        .add("endTime", endTime)
        .add("costTime", costTime)
        .add("resourceThread", getResourceDesc())
        .add("failReason", failReason)
        .add("properties", properties)
        .toString();
  }

  private void validThread(OptimizerThread thread) {
    if (token == null) {
      throw new IllegalStateException("Task not scheduled yet, taskId:" + getTaskId());
    }
    if (!thread.getToken().equals(getToken()) || thread.getThreadId() != threadId) {
      throw new DuplicateRuntimeException("Task already acked by optimizer thread + " + thread);
    }
  }

  private static final Map<Status, Set<Status>> nextStatusMap = new HashMap<>();

  static {
    nextStatusMap.put(
        Status.PLANNED, Sets.newHashSet(Status.PLANNED, Status.SCHEDULED, Status.CANCELED));
    nextStatusMap.put(
        Status.SCHEDULED,
        Sets.newHashSet(Status.PLANNED, Status.SCHEDULED, Status.ACKED, Status.CANCELED));
    nextStatusMap.put(
        Status.ACKED,
        Sets.newHashSet(
            Status.PLANNED, Status.ACKED, Status.SUCCESS, Status.FAILED, Status.CANCELED));
    nextStatusMap.put(Status.FAILED, Sets.newHashSet(Status.PLANNED, Status.FAILED));
    nextStatusMap.put(Status.SUCCESS, Sets.newHashSet(Status.SUCCESS));
    nextStatusMap.put(Status.CANCELED, Sets.newHashSet(Status.CANCELED));
  }

  private class TaskStatusMachine {

    public void accept(Status targetStatus) {
      if (!getNext().contains(targetStatus)) {
        throw new IllegalTaskStateException(taskId, status.name(), targetStatus.name());
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
}

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

package com.netease.arctic.server.exception;

import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.server.optimizing.TaskRuntime;

public class IllegalTaskStateException extends ArcticRuntimeException {

  private final TaskRuntime.Status preStatus;
  private final TaskRuntime.Status targetStatus;
  private final OptimizingTaskId taskId;

  public IllegalTaskStateException(
      OptimizingTaskId taskId, TaskRuntime.Status preStatus, TaskRuntime.Status targetStatus) {
    super(
        String.format("Illegal Task of %s status from %s to %s", taskId, preStatus, targetStatus));
    this.taskId = taskId;
    this.preStatus = preStatus;
    this.targetStatus = targetStatus;
  }

  public TaskRuntime.Status getPreStatus() {
    return preStatus;
  }

  public TaskRuntime.Status getTargetStatus() {
    return targetStatus;
  }

  public OptimizingTaskId getTaskId() {
    return taskId;
  }
}

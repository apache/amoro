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

package com.netease.arctic.ams.server.model;

public enum TableTaskStatus {
  COMPLETED,
  STOPPED,
  CREATED,
  RUNNING,
  STOPPING,
  STARTING,
  STOP_FAILED,
  START_FAILED,
  FAILED;

  public static TableTaskStatus convertSubmitterJobStatus(String status) {
    switch (status) {
      case "PENDING":
      case "STARTING":
        return TableTaskStatus.STARTING;
      case "START_FAILED":
        return TableTaskStatus.START_FAILED;
      case "RUNNING":
        return TableTaskStatus.RUNNING;
      case "STOPPING":
        return TableTaskStatus.STOPPING;
      case "UNKNOWN":
      case "FAILED":
        return TableTaskStatus.FAILED;
      case "STOPPED":
        return TableTaskStatus.STOPPED;
      case "FINISHED":
        return TableTaskStatus.COMPLETED;
      default:
        throw new IllegalArgumentException("illegal flink job status: " + status);
    }
  }
}

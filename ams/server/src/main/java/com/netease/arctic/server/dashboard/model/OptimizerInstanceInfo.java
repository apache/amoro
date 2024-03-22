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

package com.netease.arctic.server.dashboard.model;

import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;

import java.util.Objects;

public class OptimizerInstanceInfo {

  private String token;

  private long startTime;

  private long touchTime;

  private String jobId;

  private String groupName;

  private int coreNumber;

  private int memory;

  private String jobStatus;

  private String container;

  public static OptimizerInstanceInfo.Builder builder() {
    return new OptimizerInstanceInfo.Builder();
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getTouchTime() {
    return touchTime;
  }

  public void setTouchTime(long touchTime) {
    this.touchTime = touchTime;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public int getCoreNumber() {
    return coreNumber;
  }

  public void setCoreNumber(int coreNumber) {
    this.coreNumber = coreNumber;
  }

  public int getMemory() {
    return memory;
  }

  public void setMemory(int memory) {
    this.memory = memory;
  }

  public String getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(String jobStatus) {
    this.jobStatus = jobStatus;
  }

  public String getContainer() {
    return container;
  }

  public void setContainer(String container) {
    this.container = container;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OptimizerInstanceInfo that = (OptimizerInstanceInfo) o;
    return Objects.equals(token, that.token)
        && Objects.equals(startTime, that.startTime)
        && Objects.equals(touchTime, that.touchTime)
        && Objects.equals(jobId, that.jobId)
        && Objects.equals(groupName, that.groupName)
        && Objects.equals(coreNumber, that.coreNumber)
        && Objects.equals(memory, that.memory)
        && Objects.equals(jobStatus, that.jobStatus)
        && Objects.equals(container, that.container);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        token, startTime, touchTime, jobId, groupName, coreNumber, memory, jobStatus, container);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("token", token)
        .add("startTime", startTime)
        .add("touchTime", touchTime)
        .add("jobId", jobId)
        .add("groupName", groupName)
        .add("coreNumber", coreNumber)
        .add("memory", memory)
        .add("jobStatus", jobStatus)
        .add("container", container)
        .toString();
  }

  public static class Builder {
    private String token;

    private long startTime;

    private long touchTime;

    private String jobId;

    private String groupName;

    private int coreNumber;

    private int memory;

    private String jobStatus;

    private String container;

    public Builder token(String token) {
      this.token = token;
      return this;
    }

    public Builder startTime(long startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder touchTime(long touchTime) {
      this.touchTime = touchTime;
      return this;
    }

    public Builder jobId(String jobId) {
      this.jobId = jobId;
      return this;
    }

    public Builder groupName(String groupName) {
      this.groupName = groupName;
      return this;
    }

    public Builder coreNumber(int coreNumber) {
      this.coreNumber = coreNumber;
      return this;
    }

    public Builder memory(int memory) {
      this.memory = memory;
      return this;
    }

    public Builder jobStatus(String jobStatus) {
      this.jobStatus = jobStatus;
      return this;
    }

    public Builder container(String container) {
      this.container = container;
      return this;
    }

    public OptimizerInstanceInfo build() {
      OptimizerInstanceInfo instanceInfo = new OptimizerInstanceInfo();
      instanceInfo.setToken(token);
      instanceInfo.setStartTime(startTime);
      instanceInfo.setTouchTime(touchTime);
      instanceInfo.setJobId(jobId);
      instanceInfo.setGroupName(groupName);
      instanceInfo.setCoreNumber(coreNumber);
      instanceInfo.setMemory(memory);
      instanceInfo.setJobStatus(jobStatus);
      instanceInfo.setContainer(container);
      return instanceInfo;
    }
  }
}

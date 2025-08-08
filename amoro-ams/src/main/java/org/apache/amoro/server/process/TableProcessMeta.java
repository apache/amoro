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

package org.apache.amoro.server.process;

import org.apache.amoro.process.ProcessStatus;

import java.util.Map;

public class TableProcessMeta {
  private long processId;
  private long tableId;
  private ProcessStatus status;
  private String processType;
  private String processStage;
  private String executionEngine;
  private long createTime;
  private long finishTime;
  private String failMessage;
  private Map<String, String> summary;

  public long getProcessId() {
    return processId;
  }

  public void setProcessId(long processId) {
    this.processId = processId;
  }

  public long getTableId() {
    return tableId;
  }

  public void setTableId(long tableId) {
    this.tableId = tableId;
  }

  public ProcessStatus getStatus() {
    return status;
  }

  public void setStatus(ProcessStatus status) {
    this.status = status;
  }

  public String getProcessType() {
    return processType;
  }

  public void setProcessType(String processType) {
    this.processType = processType;
  }

  public String getProcessStage() {
    return processStage;
  }

  public void setProcessStage(String processStage) {
    this.processStage = processStage;
  }

  public String getExecutionEngine() {
    return executionEngine;
  }

  public void setExecutionEngine(String executionEngine) {
    this.executionEngine = executionEngine;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public String getFailMessage() {
    return failMessage;
  }

  public void setFailMessage(String failMessage) {
    this.failMessage = failMessage;
  }

  public long getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(long finishTime) {
    this.finishTime = finishTime;
  }

  public Map<String, String> getSummary() {
    return summary;
  }

  public void setSummary(Map<String, String> summary) {
    this.summary = summary;
  }
}

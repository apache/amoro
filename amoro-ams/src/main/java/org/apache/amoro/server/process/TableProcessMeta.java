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

import org.apache.amoro.Action;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.process.TableProcessStore;

import java.util.HashMap;
import java.util.Map;

public class TableProcessMeta {
  private long processId;
  private long tableId;
  private volatile String externalProcessIdentifier;
  private ProcessStatus status;
  private Action action;
  private String processStage;
  private String executionEngine;
  private int retryNumber;
  private long createTime;
  private long finishTime;
  private String failMessage;
  private Map<String, String> processParameters;
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

  /**
   * Get the action of this process.
   *
   * @return action
   */
  public Action getAction() {
    return action;
  }

  /**
   * Set the action of this process.
   *
   * @param action action
   */
  public void setAction(Action action) {
    this.action = action;
  }

  /**
   * Get process type (action name) for backward compatibility.
   *
   * @return process type name
   * @deprecated Use {@link #getAction()} instead
   */
  @Deprecated
  public String getProcessType() {
    return action != null ? action.getName() : null;
  }

  /**
   * Set process type (action name) for backward compatibility.
   *
   * @param processType process type name
   * @deprecated Use {@link #setAction(Action)} instead
   */
  @Deprecated
  public void setProcessType(String processType) {
    // This method is kept for backward compatibility but should not be used
    // Action should be set directly via setAction()
    if (processType != null && action == null) {
      // Try to find action by name from registry
      org.apache.amoro.server.persistence.converter.Action2StringConverter.registerCustomAction(
          new Action(
              new org.apache.amoro.TableFormat[] {
                org.apache.amoro.TableFormat.ICEBERG,
                org.apache.amoro.TableFormat.MIXED_ICEBERG,
                org.apache.amoro.TableFormat.MIXED_HIVE
              },
              0,
              processType));
    }
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

  public String getExternalProcessIdentifier() {
    return externalProcessIdentifier;
  }

  public void setExternalProcessIdentifier(String externalProcessIdentifier) {
    this.externalProcessIdentifier = externalProcessIdentifier;
  }

  public int getRetryNumber() {
    return retryNumber;
  }

  public void setRetryNumber(int retryNumber) {
    this.retryNumber = retryNumber;
  }

  public Map<String, String> getProcessParameters() {
    return processParameters;
  }

  public void setProcessParameters(Map<String, String> processParameters) {
    this.processParameters = processParameters;
  }

  public TableProcessMeta copy() {
    TableProcessMeta meta = new TableProcessMeta();

    meta.setProcessId(this.processId);
    meta.setTableId(this.tableId);
    meta.setRetryNumber(this.retryNumber);
    meta.setCreateTime(this.createTime);
    meta.setFinishTime(this.finishTime);

    meta.setExternalProcessIdentifier(this.externalProcessIdentifier);
    meta.setAction(this.action);
    meta.setProcessStage(this.processStage);
    meta.setExecutionEngine(this.executionEngine);
    meta.setFailMessage(this.failMessage);

    meta.setStatus(this.status);

    if (this.processParameters != null) {
      meta.setProcessParameters(new HashMap<>(this.processParameters));
    }
    if (this.summary != null) {
      meta.setSummary(new HashMap<>(this.summary));
    }

    return meta;
  }

  public static TableProcessMeta fromTableProcessStore(TableProcessStore tableProcessStore) {
    TableProcessMeta tableProcessMeta = new TableProcessMeta();
    tableProcessMeta.setProcessId(tableProcessStore.getProcessId());
    tableProcessMeta.setTableId(tableProcessStore.getTableId());
    tableProcessMeta.setExternalProcessIdentifier(tableProcessStore.getExternalProcessIdentifier());
    tableProcessMeta.setStatus(tableProcessStore.getStatus());
    tableProcessMeta.setAction(tableProcessStore.getAction());
    tableProcessMeta.setProcessStage(tableProcessStore.getProcessStage());
    tableProcessMeta.setExecutionEngine(tableProcessStore.getExecutionEngine());
    tableProcessMeta.setRetryNumber(tableProcessStore.getRetryNumber());
    tableProcessMeta.setCreateTime(tableProcessStore.getCreateTime());
    tableProcessMeta.setFinishTime(tableProcessStore.getFinishTime());
    tableProcessMeta.setFailMessage(tableProcessStore.getFailMessage());
    tableProcessMeta.setProcessParameters(tableProcessStore.getProcessParameters());
    tableProcessMeta.setSummary(tableProcessStore.getSummary());
    return tableProcessMeta;
  }

  @Deprecated
  public static TableProcessMeta fromTableProcessState(TableProcessState tableProcessState) {
    TableProcessMeta tableProcessMeta = new TableProcessMeta();
    tableProcessMeta.setProcessId(tableProcessState.getId());
    tableProcessMeta.setTableId(tableProcessState.getTableIdentifier().getId());
    tableProcessMeta.setExternalProcessIdentifier(tableProcessState.getExternalProcessIdentifier());
    tableProcessMeta.setStatus(tableProcessState.getStatus());
    tableProcessMeta.setAction(tableProcessState.getAction());
    tableProcessMeta.setProcessStage(tableProcessState.getStage().getDesc());
    tableProcessMeta.setExecutionEngine(tableProcessState.getExecutionEngine());
    tableProcessMeta.setRetryNumber(tableProcessState.getRetryNumber());
    tableProcessMeta.setCreateTime(tableProcessState.getStartTime());
    tableProcessMeta.setFinishTime(tableProcessState.getEndTime());
    tableProcessMeta.setFailMessage(tableProcessState.getFailedReason());
    tableProcessMeta.setProcessParameters(tableProcessState.getProcessParameters());
    tableProcessMeta.setSummary(tableProcessState.getSummary());
    return tableProcessMeta;
  }

  /**
   * Create a TableProcessMeta with Action.
   *
   * @param processId process id
   * @param tableId table id
   * @param action action
   * @param executionEngine execution engine
   * @param processParameters process parameters
   * @return TableProcessMeta instance
   */
  public static TableProcessMeta of(
      long processId,
      long tableId,
      Action action,
      String executionEngine,
      Map<String, String> processParameters) {
    TableProcessMeta tableProcessMeta = new TableProcessMeta();
    tableProcessMeta.setProcessId(processId);
    tableProcessMeta.setTableId(tableId);
    tableProcessMeta.setExternalProcessIdentifier("");
    tableProcessMeta.setStatus(ProcessStatus.UNKNOWN);
    tableProcessMeta.setAction(action);
    tableProcessMeta.setProcessStage(ProcessStatus.UNKNOWN.name());
    tableProcessMeta.setExecutionEngine(executionEngine);
    tableProcessMeta.setRetryNumber(0);
    tableProcessMeta.setCreateTime(System.currentTimeMillis());
    tableProcessMeta.setFinishTime(0);
    tableProcessMeta.setFailMessage("");
    tableProcessMeta.setProcessParameters(processParameters);
    tableProcessMeta.setSummary(new HashMap<>());
    return tableProcessMeta;
  }

  /**
   * Create a TableProcessMeta with action name (for backward compatibility).
   *
   * @param processId process id
   * @param tableId table id
   * @param actionName action name
   * @param executionEngine execution engine
   * @param processParameters process parameters
   * @return TableProcessMeta instance
   * @deprecated Use {@link #of(long, long, Action, String, Map)} instead
   */
  @Deprecated
  public static TableProcessMeta of(
      long processId,
      long tableId,
      String actionName,
      String executionEngine,
      Map<String, String> processParameters) {
    // Try to find action from registry
    Action action =
        org.apache.amoro.server.persistence.converter.Action2StringConverter.getActionByName(
            actionName);
    if (action == null) {
      // Create a temporary action if not found
      action =
          new Action(
              new org.apache.amoro.TableFormat[] {
                org.apache.amoro.TableFormat.ICEBERG,
                org.apache.amoro.TableFormat.MIXED_ICEBERG,
                org.apache.amoro.TableFormat.MIXED_HIVE
              },
              0,
              actionName);
    }
    return of(processId, tableId, action, executionEngine, processParameters);
  }
}

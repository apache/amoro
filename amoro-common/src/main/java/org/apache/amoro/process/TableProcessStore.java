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

package org.apache.amoro.process;

import org.apache.amoro.Action;

import java.util.Map;

/** Store interface that persists and exposes table process metadata and transitions. */
public interface TableProcessStore {

  /** Get process id. */
  long getProcessId();

  /** Get table id. */
  long getTableId();

  /** Get external process identifier. */
  String getExternalProcessIdentifier();

  /** Get current status. */
  ProcessStatus getStatus();

  /** Get process type (action name). */
  String getProcessType();

  /** Get process stage. */
  String getProcessStage();

  /** Get execution engine name. */
  String getExecutionEngine();

  /** Get retry number. */
  int getRetryNumber();

  /** Get create time in milliseconds. */
  long getCreateTime();

  /** Get finish time in milliseconds. */
  long getFinishTime();

  /** Get fail message. */
  String getFailMessage();

  /** Get process parameters. */
  Map<String, String> getProcessParameters();

  /** Get summary map. */
  Map<String, String> getSummary();

  /** Get {@link Action} of this process. */
  Action getAction();

  /** Dispose the store and clear persisted state. */
  void dispose();

  /**
   * Try to transit the process state with given parameters and persist changes.
   *
   * @param newStatus target status
   * @param processEvent process event
   * @param externalProcessIdentifier external identifier
   * @param reason reason message
   * @param processParameters parameters
   * @param summary summary
   * @return true if transition succeeds
   */
  boolean tryTransitState(
      ProcessStatus newStatus,
      ProcessEvent processEvent,
      String externalProcessIdentifier,
      String reason,
      Map<String, String> processParameters,
      Map<String, String> summary);

  /**
   * Begin an operation block to update process meta.
   *
   * @return operation builder
   */
  TableProcessOperation begin();

  /** Operation builder to update fields and commit atomically. */
  interface TableProcessOperation {

    /**
     * Set external process identifier.
     *
     * @param externalProcessIdentifier identifier
     * @return this operation
     */
    TableProcessOperation updateExternalProcessIdentifier(String externalProcessIdentifier);

    /**
     * Set process status.
     *
     * @param status status
     * @return this operation
     */
    TableProcessOperation updateTableProcessStatus(ProcessStatus status);

    /**
     * Set fail message.
     *
     * @param failMessage message
     * @return this operation
     */
    TableProcessOperation updateTableProcessFailMessage(String failMessage);

    /**
     * Set create time.
     *
     * @param createTime milliseconds
     * @return this operation
     */
    TableProcessOperation updateCreateTime(long createTime);

    /**
     * Set finish time.
     *
     * @param finishTime milliseconds
     * @return this operation
     */
    TableProcessOperation updateFinishTime(long finishTime);

    /**
     * Set process parameters.
     *
     * @param processParameters parameters
     * @return this operation
     */
    TableProcessOperation updateProcessParameters(Map<String, String> processParameters);

    /**
     * Set summary.
     *
     * @param summary summary map
     * @return this operation
     */
    TableProcessOperation updateSummary(Map<String, String> summary);

    /**
     * Set retry number.
     *
     * @param retryNumber retry count
     * @return this operation
     */
    TableProcessOperation updateRetryNumber(int retryNumber);

    /** Commit the staged operations. */
    void commit();
  }
}

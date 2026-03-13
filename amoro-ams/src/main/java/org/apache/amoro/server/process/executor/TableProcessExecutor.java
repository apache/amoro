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

package org.apache.amoro.server.process.executor;

import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.ProcessEvent;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.shade.guava32.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable executor that submits and tracks a {@link TableProcess} on a given {@link
 * ExecuteEngine}. It polls status until terminal and triggers state transitions and callbacks.
 */
public class TableProcessExecutor extends PersistentBase implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(TableProcessExecutor.class);

  private static final long DEFAULT_POLL_INTERVAL_MS = 5000L;
  public ExecuteEngine executeEngine;
  protected TableProcess tableProcess;
  private final TableProcessStore store;
  private Runnable finishedCallback;

  /**
   * Construct an executor for a table process.
   *
   * @param tableProcess table process
   * @param executeEngine execute engine
   */
  public TableProcessExecutor(
      TableProcess tableProcess, TableProcessStore store, ExecuteEngine executeEngine) {
    this.tableProcess = tableProcess;
    this.executeEngine = executeEngine;
    this.store = store;
  }

  /** Submit or recover the process to engine, poll status and update store. */
  @Override
  public void run() {
    String externalProcessIdentifier = null;
    ProcessStatus status;
    String message = "";

    if (isTableProcessCanceling(store.getStatus())) {
      LOG.info(
          "Table process {} with identifier {} may have been in canceling, exit submit process.",
          store.getProcessId(),
          externalProcessIdentifier);
      return;
    }

    try {
      if (store.getStatus() == ProcessStatus.UNKNOWN
          || store.getStatus() == ProcessStatus.PENDING
          || Strings.isNullOrEmpty(store.getExternalProcessIdentifier())) {
        externalProcessIdentifier = executeEngine.submitTableProcess(tableProcess);
        LOG.info(
            "Submit table process {} to engine {} success, external process identifier is {}",
            store.getProcessId(),
            executeEngine.engineType(),
            externalProcessIdentifier);
      } else {
        externalProcessIdentifier = store.getExternalProcessIdentifier();
      }

      validateIdentifier(externalProcessIdentifier);

      status = executeEngine.getStatus(externalProcessIdentifier);
      store.tryTransitState(
          status,
          ProcessEvent.SUBMIT_REQUESTED,
          externalProcessIdentifier,
          "Complete Submitted.",
          tableProcess.getProcessParameters(),
          tableProcess.getSummary());

      while (isTableProcessExecuting(status)) {
        if (isTableProcessCanceling(store.getStatus())) {
          LOG.info(
              "Table process {} with identifier {} may have been in canceling, exit submit process.",
              store.getProcessId(),
              externalProcessIdentifier);
          return;
        }
        try {
          Thread.sleep(DEFAULT_POLL_INTERVAL_MS);
        } catch (InterruptedException e) {
          throw e;
        }
        status = executeEngine.getStatus(externalProcessIdentifier);
      }
    } catch (Throwable t) {
      if (t instanceof InterruptedException) {
        LOG.info(
            "Table process {} with identifier {} may have been interrupted by process service disposing, exit submit process.",
            store.getProcessId(),
            externalProcessIdentifier);
        return;
      } else {
        status = ProcessStatus.FAILED;
        message = t.getMessage();
      }
    }

    LOG.info("The process {} is finished with status {}", tableProcess, status);
    if (status == ProcessStatus.KILLED) {
      store.tryTransitState(
          status,
          ProcessEvent.KILL_REQUESTED,
          store.getExternalProcessIdentifier(),
          "Gracefully Killed.",
          tableProcess.getProcessParameters(),
          tableProcess.getSummary());
    } else if (status == ProcessStatus.CANCELED) {
      store.tryTransitState(
          status,
          ProcessEvent.CANCEL_REQUESTED,
          store.getExternalProcessIdentifier(),
          "Gracefully Cancelled.",
          tableProcess.getProcessParameters(),
          tableProcess.getSummary());
    } else if (status == ProcessStatus.CLOSED) {
      store.tryTransitState(
          status,
          ProcessEvent.KILL_REQUESTED,
          store.getExternalProcessIdentifier(),
          "Gracefully Closed.",
          tableProcess.getProcessParameters(),
          tableProcess.getSummary());
    } else if (status == ProcessStatus.FAILED) {
      store.tryTransitState(
          status,
          ProcessEvent.COMPLETE_FAILED,
          store.getExternalProcessIdentifier(),
          message,
          tableProcess.getProcessParameters(),
          tableProcess.getSummary());
    } else if (status == ProcessStatus.SUCCESS) {
      store.tryTransitState(
          status,
          ProcessEvent.COMPLETE_SUCCESS,
          store.getExternalProcessIdentifier(),
          "Complete Success",
          tableProcess.getProcessParameters(),
          tableProcess.getSummary());
    } else {
      LOG.warn("Un expected terminal status: {} for process: {}.", status, store.getProcessId());
    }

    if (finishedCallback != null) {
      finishedCallback.run();
    }
  }

  /**
   * Register a callback to be invoked when process finished.
   *
   * @param runnable callback runnable
   */
  public void onProcessFinished(Runnable runnable) {
    this.finishedCallback = runnable;
  }

  /**
   * Validate the external process identifier.
   *
   * @param externalProcessIdentifier identifier from engine
   * @throws IllegalStateException if identifier is null or empty
   */
  private void validateIdentifier(String externalProcessIdentifier) {
    if (Strings.isNullOrEmpty(externalProcessIdentifier)) {
      LOG.warn(
          "The process {} is un-accessible with null or empty external process identifier.",
          tableProcess);
      throw new IllegalStateException(
          String.format(
              "The process %s is submitted or recovered from a illegal external process identifier.",
              tableProcess));
    }
  }

  /**
   * Whether a process is in canceling/canceled status.
   *
   * @param status process status
   * @return true if canceling/canceled
   */
  private boolean isTableProcessCanceling(ProcessStatus status) {
    return (status == ProcessStatus.CANCELING || status == ProcessStatus.CANCELED);
  }

  /**
   * Whether a process is in executing status.
   *
   * @param status process status
   * @return true if running/submitted
   */
  private boolean isTableProcessExecuting(ProcessStatus status) {
    return (status == ProcessStatus.RUNNING || status == ProcessStatus.SUBMITTED);
  }
}

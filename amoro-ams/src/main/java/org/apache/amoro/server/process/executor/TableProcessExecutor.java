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

import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.shade.guava32.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableProcessExecutor extends PersistentBase implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(TableProcessExecutor.class);

  private static final long DEFAULT_POLL_INTERVAL_MS = 5000L;
  public ExecuteEngine executeEngine;
  protected TableProcess tableProcess;
  private Runnable finishedCallback;

  public TableProcessExecutor(TableProcess tableProcess, ExecuteEngine executeEngine) {
    this.tableProcess = tableProcess;
    this.executeEngine = executeEngine;
  }

  @Override
  public void run() {
    String externalProcessIdentifier = null;
    ProcessStatus status;
    String message = "";

    if (isTableProcessCanceling(tableProcess.getStatus())) {
      LOG.info(
          "Table process {} with identifier {} may have been in canceling, exit submit process.",
          tableProcess.getId(),
          externalProcessIdentifier);
      return;
    }

    try {
      if (tableProcess.getStatus() == ProcessStatus.PENDING
          || Strings.isNullOrEmpty(tableProcess.getExternalProcessIdentifier())) {
        externalProcessIdentifier = executeEngine.submitTableProcess(tableProcess);
        LOG.info(
            "Submit table process {} to engine {} success, external process identifier is {}",
            tableProcess.getId(),
            executeEngine.engineType(),
            externalProcessIdentifier);
      } else {
        externalProcessIdentifier = tableProcess.getExternalProcessIdentifier();
      }

      validateIdentifier(externalProcessIdentifier);

      status = executeEngine.getStatus(externalProcessIdentifier);
      tableProcess.updateExternalProcessIdentifier(status, externalProcessIdentifier);

      while (isTableProcessExecuting(status)) {
        if (isTableProcessCanceling(tableProcess.getStatus())) {
          LOG.info(
              "Table process {} with identifier {} may have been in canceling, exit submit process.",
              tableProcess.getId(),
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
            tableProcess.getId(),
            externalProcessIdentifier);
        return;
      } else {
        status = ProcessStatus.FAILED;
        message = t.getMessage();
      }
    }

    LOG.info("The process {} is finished with status {}", tableProcess, status);
    tableProcess.updateTableProcessStatus(status, message);
    if (finishedCallback != null) {
      finishedCallback.run();
    }
  }

  public void onProcessFinished(Runnable runnable) {
    this.finishedCallback = runnable;
  }

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

  private boolean isTableProcessCanceling(ProcessStatus status) {
    return (status == ProcessStatus.CANCELING || status == ProcessStatus.CANCELED);
  }

  private boolean isTableProcessExecuting(ProcessStatus status) {
    return (status == ProcessStatus.RUNNING || status == ProcessStatus.SUBMITTED);
  }
}

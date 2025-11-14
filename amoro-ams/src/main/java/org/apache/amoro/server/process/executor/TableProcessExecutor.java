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

import org.apache.amoro.Action;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.server.persistence.PersistentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableProcessExecutor extends PersistentBase implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(TableProcessExecutor.class);

  public ExecuteEngine executeEngine;
  public Action action;
  protected TableProcess<TableProcessState> tableProcess;
  private Runnable finishedCallback;

  public TableProcessExecutor(
      TableProcess<TableProcessState> tableProcess, ExecuteEngine executeEngine) {
    this.tableProcess = tableProcess;
    this.executeEngine = executeEngine;
  }

  @Override
  public void run() {
    String externalProcessIdentifier = null;
    if (tableProcess.getStatus() == ProcessStatus.PENDING) {
      externalProcessIdentifier = executeEngine.submitTableProcess(tableProcess);
      LOG.info(
          "Submit table process {} to engine {} success, external process identifier is {}",
          tableProcess.getId(),
          executeEngine.engineType(),
          externalProcessIdentifier);
    } else {
      externalProcessIdentifier = tableProcess.getExternalProcessIdentifier();
    }

    ProcessStatus status = executeEngine.getStatus(externalProcessIdentifier);
    while (status != ProcessStatus.RUNNING) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      status = executeEngine.getStatus(externalProcessIdentifier);
    }
    LOG.info("The process {} is finished with status {}", tableProcess, status);
    persistentTableProcess(status);
    if (finishedCallback != null) {
      finishedCallback.run();
    }
  }

  public void onProcessFinished(Runnable runnable) {
    this.finishedCallback = runnable;
  }

  private void persistentTableProcess(ProcessStatus status) {
    // TODO: persist table process status to db.
    // TODO: move this method to TableProcess to trigger complete future.
  }
}

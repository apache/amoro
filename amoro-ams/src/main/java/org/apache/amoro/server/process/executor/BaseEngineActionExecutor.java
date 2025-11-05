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
import org.apache.amoro.server.process.context.ProcessServiceContext;
import org.apache.amoro.server.process.resource.RunningInstanceManager;

public abstract class BaseEngineActionExecutor implements Runnable {

  public EngineType engineType;
  public Action action;
  protected TableProcess tableProcess;

  public RunningInstanceManager runningInstanceManager;

  public BaseEngineActionExecutor(TableProcess tableProcess) {
    this.tableProcess = tableProcess;
  }

  @Override
  public void run() {
    ProcessServiceContext.getRunningInstanceManager().addInstance(tableProcess);
    try {
      execute();
    } finally {
      ProcessServiceContext.getRunningInstanceManager().removeInstance(tableProcess);
    }
  }

  protected void execute() {
    initialize();
    validCheck();
    doSubmit();
    ProcessStatus status = waitForJob();
    completeProcess(status, false);
  }

  protected abstract void initialize();

  protected abstract void validCheck();

  protected abstract void doSubmit();

  protected abstract ProcessStatus waitForJob();

  protected abstract void completeProcess(ProcessStatus status, boolean checkInstant);

  @Override
  public String toString() {
    return this.getClass().getName() + ", tableProcess: " + tableProcess.getId();
  }
}

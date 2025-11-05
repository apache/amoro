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

package org.apache.amoro.server.process.service;

import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.process.executor.BaseEngineActionExecutor;
import org.apache.amoro.server.process.executor.ExecuteEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SubmitService extends PersistentBase implements BaseService {

  private static final Logger LOG = LoggerFactory.getLogger(SubmitService.class);

  private ScheduledExecutorService service;
  private ExecutorService executionService;
  private ExecuteEngine executeEngine;
  private List<BaseSubmitFilter> filters = new ArrayList<>();

  public SubmitService(ExecutorService executionService) {
    this.executionService = executionService;
  }

  @Override
  public void init() {
    this.service = Executors.newSingleThreadScheduledExecutor();
  }

  @Override
  public void startService() {
    LOG.info("Start service: " + this.getClass().getName());
    service.scheduleAtFixedRate(new ScheduleRunnable(), 30, 60, TimeUnit.SECONDS);
    LOG.info("Finish start service: " + this.getClass().getName());
  }

  @Override
  public void stop() {
    LOG.info("Stop service: " + this.getClass().getName());
    if (service != null && !service.isShutdown()) {
      service.shutdown();
    }
    LOG.info("Finish stop service: " + this.getClass().getName());
  }

  private class ScheduleRunnable implements Runnable {

    @Override
    public void run() {
      List<TableProcess> tableProcesses = fetchReadyTask();
      for (TableProcess tableProcess : tableProcesses) {
        boolean success = submitTask(tableProcess);
      }
    }

    public List<TableProcess> fetchReadyTask() {
      return null;
    }

    public boolean submitTask(TableProcess tableProcesses) {
      for (BaseSubmitFilter filter : filters) {
        if (filter.runFilter(tableProcesses)) {
          return false;
        }
      }
      BaseEngineActionExecutor actionExecutor = executeEngine.acceptTableProcess(tableProcesses);
      executionService.submitTask(actionExecutor);
      return true;
    }
  }

  private abstract static class BaseSubmitFilter {

    protected abstract boolean runFilter(TableProcess process);
  }
}

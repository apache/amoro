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
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.ProcessStateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RetryService extends PersistentBase implements BaseService {

  private static final Logger LOG = LoggerFactory.getLogger(RetryService.class);

  private ScheduledExecutorService service;

  @Override
  public void init() {
    LOG.info("Init service: " + this.getClass().getName());
    this.service = Executors.newSingleThreadScheduledExecutor();
    LOG.info("Finish init service: " + this.getClass().getName());
  }

  @Override
  public void startService() {
    LOG.info("Start service: " + this.getClass().getName());
    service.scheduleAtFixedRate(new RetryRunnable(), 30, 300, TimeUnit.SECONDS);
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

  private class RetryRunnable implements Runnable {

    @Override
    public void run() {
      List<TableProcess> tableProcesses = fetchFailedTask();
      for (TableProcess tableProcess : tableProcesses) {
        if (checkRecoverable(tableProcess)) {
          submitFailedTask(tableProcess);
        }
      }
    }

    public List<TableProcess> fetchFailedTask() {
      return null;
    }

    public boolean checkRecoverable(TableProcess tableProcesses) {
      return false;
    }

    public boolean submitFailedTask(TableProcess tableProcesses) {
      TableProcessState tableProcessState = tableProcesses.getState();
      tableProcessState.setSubmitted();
      doAs(ProcessStateMapper.class, mapper -> mapper.createProcessState(tableProcessState));
      return true;
    }
  }
}

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

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionCoordinatorScheduler extends PeriodicTableScheduler {

  public static final Logger LOG = LoggerFactory.getLogger(ActionCoordinatorScheduler.class);
  public static final int PROCESS_MAX_RETRY_NUMBER = 3;

  private final ActionCoordinator coordinator;
  private final ProcessService processService;

  public ActionCoordinatorScheduler(
      ActionCoordinator coordinator, TableService tableService, ProcessService processService) {
    super(coordinator.action(), tableService, coordinator.parallelism());
    this.coordinator = coordinator;
    this.processService = processService;
  }

  public ActionCoordinator getCoordinator() {
    return coordinator;
  }

  @Override
  protected boolean formatSupported(TableFormat format) {
    return coordinator.formatSupported(format);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return coordinator.getNextExecutingTime(tableRuntime);
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return coordinator.enabled(tableRuntime);
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    if (hasAliveTableProcess(tableRuntime)) {
      TableProcess process =
          processService
              .getTableProcessTracker()
              .getTableProcessInstance(tableRuntime.getTableIdentifier());
      LOG.warn(
          "Detect table process: {} with status: {} exists for table runtime: {}, skip schedule {} action this time.",
          process.getId(),
          process.getStatus(),
          tableRuntime.getTableIdentifier(),
          getAction());
    } else {
      TableProcess process = coordinator.createTableProcess(tableRuntime);
      processService.register(tableRuntime, process);
    }
  }

  protected void recover(TableRuntime tableRuntime, TableProcessMeta processMeta) {
    if (hasAliveTableProcess(tableRuntime)) {
      TableProcess process =
          processService
              .getTableProcessTracker()
              .getTableProcessInstance(tableRuntime.getTableIdentifier());
      LOG.warn(
          "Detect table process: {} with status: {} exists for table runtime: {}, skip recover {} action this time.",
          process.getId(),
          process.getStatus(),
          tableRuntime.getTableIdentifier(),
          getAction());
    } else {
      TableProcess process = coordinator.recoverTableProcess(tableRuntime, processMeta);
      processService.recover(tableRuntime, process);
    }
  }

  protected void retry(TableRuntime tableRuntime, TableProcess process) {
    TableProcess existProcess =
        processService
            .getTableProcessTracker()
            .getTableProcessInstance(tableRuntime.getTableIdentifier());
    if (existProcess != null && existProcess.getId() == process.getId()) {
      process = coordinator.retryTableProcess(process);
      processService.retry(process);
    } else {
      LOG.warn(
          "Detect no table process exists or exist table process id is different from retry process for table runtime: {}, skip retry {} action this time.",
          tableRuntime.getTableIdentifier(),
          getAction());
    }
  }

  protected void cancel(TableRuntime tableRuntime, TableProcess process) {
    process = coordinator.cancelTableProcess(tableRuntime, process);
    processService.cancel(process);
  }

  protected boolean hasAliveTableProcess(TableRuntime tableRuntime) {
    TableProcess process =
        processService
            .getTableProcessTracker()
            .getTableProcessInstance(tableRuntime.getTableIdentifier());
    if (process != null
        && (process.getStatus() == ProcessStatus.RUNNING
            || process.getStatus() == ProcessStatus.SUBMITTED
            || process.getStatus() == ProcessStatus.PENDING)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected long getExecutorDelay() {
    return coordinator.getExecutorDelay();
  }

  @Override
  public void handleTableRemoved(TableRuntime tableRuntime) {
    TableProcess process =
        processService
            .getTableProcessTracker()
            .getTableProcessInstance(tableRuntime.getTableIdentifier());
    if (process != null) {
      cancel(tableRuntime, process);
    }
  }
}

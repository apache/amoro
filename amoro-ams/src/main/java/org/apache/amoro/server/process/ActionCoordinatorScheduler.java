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
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.TableService;

public class ActionCoordinatorScheduler extends PeriodicTableScheduler {

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
    TableProcess<TableProcessState> process = coordinator.createTableProcess(tableRuntime);
    processService.register(process);
  }

  @Override
  protected long getExecutorDelay() {
    return coordinator.getExecutorDelay();
  }
}

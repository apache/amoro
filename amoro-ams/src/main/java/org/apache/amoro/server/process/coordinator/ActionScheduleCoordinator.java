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

package org.apache.amoro.server.process.coordinator;

import org.apache.amoro.Action;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.SupportsProcessPlugins;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.AmoroProcess;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.resource.ExternalResourceContainer;
import org.apache.amoro.resource.ResourceManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.scheduler.PeriodicExternalScheduler;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;

public class ActionScheduleCoordinator extends PeriodicExternalScheduler {

  public ActionScheduleCoordinator(
      ResourceManager resourceManager,
      ExternalResourceContainer resourceContainer,
      Action action,
      TableService tableService,
      int poolSize) {
    super(resourceManager, resourceContainer, action, tableService, poolSize);
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    Preconditions.checkArgument(tableRuntime instanceof SupportsProcessPlugins);
    SupportsProcessPlugins runtimeSupportProcessPlugin = (SupportsProcessPlugins) tableRuntime;
    // Trigger a table process and check conflicts by table runtime
    // Update process state after process completed, the callback must be register first
    AmoroProcess<? extends TableProcessState> process =
        runtimeSupportProcessPlugin.trigger(getAction());
    persistTableProcess(process);

    // Trace the table process by async framework so that process can be called back when completed
    trace(process);
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    // DO nothing by default
  }

  @Override
  public void handleTableRemoved(TableRuntime tableRuntime) {
    // DO nothing, handling would be canceled when calling executeTable
  }

  @Override
  public void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {}

  @Override
  public void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  protected void trace(AmoroProcess<? extends TableProcessState> process) {}

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return 0;
  }

  @Override
  protected long getExecutorDelay() {
    return 0;
  }
}

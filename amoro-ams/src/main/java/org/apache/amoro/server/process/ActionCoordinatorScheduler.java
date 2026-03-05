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
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Periodic scheduler that delegates scheduling decisions to an {@link ActionCoordinator}. It
 * creates, recovers and retries table processes via {@link ProcessService}.
 */
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

  /**
   * Get the bound coordinator.
   *
   * @return coordinator
   */
  public ActionCoordinator getCoordinator() {
    return coordinator;
  }

  /**
   * Whether the given table format is supported.
   *
   * @param format table format
   * @return true if supported
   */
  @Override
  protected boolean formatSupported(TableFormat format) {
    return coordinator.formatSupported(format);
  }

  /**
   * Compute next executing time for a table runtime.
   *
   * @param tableRuntime table runtime
   * @return next executing timestamp in milliseconds
   */
  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return coordinator.getNextExecutingTime(tableRuntime);
  }

  /**
   * Whether the given table runtime is enabled for scheduling.
   *
   * @param tableRuntime table runtime
   * @return true if enabled
   */
  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return coordinator.enabled(tableRuntime);
  }

  /**
   * Create and register a new table process for the given table runtime.
   *
   * @param tableRuntime table runtime
   */
  @Override
  protected void execute(TableRuntime tableRuntime) {
    Optional<TableProcess> process = coordinator.trigger(tableRuntime);
    process.ifPresent(p -> processService.register(tableRuntime, p));
  }

  /**
   * Recover and register a table process from store.
   *
   * @param tableRuntime table runtime
   * @param processStore process store
   */
  protected void recover(TableRuntime tableRuntime, TableProcessStore processStore) {
    TableProcess process = coordinator.recoverTableProcess(tableRuntime, processStore);
    processService.recover(tableRuntime, process);
  }

  /**
   * Get executor delay from coordinator.
   *
   * @return delay in milliseconds
   */
  @Override
  protected long getExecutorDelay() {
    return coordinator.getExecutorDelay();
  }
}

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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.table.blocker.TableBlocker;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Periodic scheduler that delegates scheduling decisions to an {@link ActionCoordinator}. It
 * creates, recovers and retries table processes via {@link ProcessService}. Includes Blocker
 * checking for OPTIMIZE operations.
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

  /** Get the bound coordinator. */
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

  /**
   * Create and register a new table process for the given table runtime. Checks for Blocker
   * conflicts before triggering.
   */
  @Override
  protected void execute(TableRuntime tableRuntime) {
    // Check for Blocker conflicts (Step 6.6 from the refactoring plan)
    if (isBlocked(tableRuntime)) {
      LOG.debug(
          "Table {} is blocked for optimize, skip scheduling", tableRuntime.getTableIdentifier());
      return;
    }
    Optional<TableProcess> process = coordinator.trigger(tableRuntime);
    process.ifPresent(p -> processService.register(tableRuntime, p));
  }

  /** Recover and register a table process from store. */
  protected TableProcess recover(TableRuntime tableRuntime, TableProcessStore processStore) {
    return coordinator.recoverTableProcess(tableRuntime, processStore);
  }

  @Override
  protected long getExecutorDelay() {
    return coordinator.getExecutorDelay();
  }

  /**
   * Check if the table is blocked for the OPTIMIZE operation. Migrated from
   * OptimizingQueue.skipBlockedTables().
   */
  private boolean isBlocked(TableRuntime tableRuntime) {
    try (SqlSession session =
        SqlSessionFactoryProvider.getInstance()
            .get()
            .openSession(TransactionIsolationLevel.READ_COMMITTED)) {
      List<TableBlocker> blockers =
          session.getMapper(TableBlockerMapper.class).selectAllBlockers(System.currentTimeMillis());
      ServerTableIdentifier identifier = tableRuntime.getTableIdentifier();
      return blockers.stream()
          .anyMatch(
              blocker ->
                  TableBlocker.conflict(BlockableOperation.OPTIMIZE, blocker)
                      && blocker.getCatalog().equals(identifier.getCatalog())
                      && blocker.getDatabase().equals(identifier.getDatabase())
                      && blocker.getTableName().equals(identifier.getTableName()));
    } catch (Exception e) {
      LOG.warn(
          "Failed to check blockers for table {}, proceeding",
          tableRuntime.getTableIdentifier(),
          e);
      return false;
    }
  }
}

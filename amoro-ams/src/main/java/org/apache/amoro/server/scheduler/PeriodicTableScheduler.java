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

package org.apache.amoro.server.scheduler;

import org.apache.amoro.Action;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.amoro.server.utils.SnowflakeIdGenerator;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class PeriodicTableScheduler extends RuntimeHandlerChain {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private static final long START_DELAY = 10 * 1000L;
  private static final String CLEANUP_EXECUTION_ENGINE = "AMORO";
  private static final String CLEANUP_PROCESS_STAGE = "CLEANUP";
  private static final String EXTERNAL_PROCESS_IDENTIFIER = "";
  private static final SnowflakeIdGenerator ID_GENERATOR = new SnowflakeIdGenerator();

  private final PersistenceHelper persistenceHelper = new PersistenceHelper();

  protected final Set<ServerTableIdentifier> scheduledTables =
      Collections.synchronizedSet(new HashSet<>());
  private final Action action;
  private final ScheduledExecutorService executor;
  private final TableService tableService;

  protected PeriodicTableScheduler(Action action, TableService tableService, int poolSize) {
    this.action = action;
    this.tableService = tableService;
    this.executor =
        Executors.newScheduledThreadPool(
            poolSize,
            new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("async-" + getThreadName() + "-%d")
                .build());
  }

  protected PeriodicTableScheduler(TableService tableService, int poolSize) {
    this.action = IcebergActions.SYSTEM;
    this.tableService = tableService;
    this.executor =
        Executors.newScheduledThreadPool(
            poolSize,
            new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("async-" + getThreadName() + "-%d")
                .build());
  }

  @Override
  protected void initHandler(List<TableRuntime> tableRuntimeList) {
    tableRuntimeList.stream()
        .filter(this::enabled)
        .forEach(
            tableRuntime -> {
              if (scheduledTables.add(tableRuntime.getTableIdentifier())) {
                scheduleTableExecution(
                    tableRuntime, calculateExecutionDelay(tableRuntime, getCleanupOperation()));
              }
            });

    logger.info("Table executor {} initialized", getClass().getSimpleName());
  }

  private long calculateExecutionDelay(
      TableRuntime tableRuntime, CleanupOperation cleanupOperation) {
    // If the table needs to be executed immediately, schedule it to run after a short delay.
    if (shouldExecuteTask(tableRuntime, cleanupOperation)) {
      return getStartDelay();
    }

    // If the table does not need to be executed immediately, schedule it for the next execution
    // time.
    // Adding getStartDelay() helps distribute the execution time of multiple tables,
    // reducing the probability of simultaneous execution and system load spikes.
    return getNextExecutingTime(tableRuntime) + getStartDelay();
  }

  /**
   * Schedule a table for execution with the specified delay.
   *
   * @param tableRuntime The table runtime to schedule
   * @param delay The delay in milliseconds before execution
   */
  private void scheduleTableExecution(TableRuntime tableRuntime, long delay) {
    executor.schedule(() -> executeTask(tableRuntime), delay, TimeUnit.MILLISECONDS);
    logger.debug(
        "Scheduled execution for table {} with delay {} ms",
        tableRuntime.getTableIdentifier(),
        delay);
  }

  private void executeTask(TableRuntime tableRuntime) {
    TableProcessMeta cleanupProcessMeta = null;
    CleanupOperation cleanupOperation = null;
    Exception executionError = null;
    long cleanupEndTime = 0L;

    try {
      if (isExecutable(tableRuntime)) {
        cleanupOperation = getCleanupOperation();
        // create and persist cleanup process info
        cleanupProcessMeta = createCleanupProcessInfo(tableRuntime, cleanupOperation);

        execute(tableRuntime);

        // Different tables take different amounts of time to execute the end of execute(),
        // so you need to perform the update operation separately for each table.
        cleanupEndTime = System.currentTimeMillis();
        persistUpdatingCleanupTime(tableRuntime, cleanupEndTime);
      }
    } catch (Exception e) {
      logger.error("exception when schedule for table: {}", tableRuntime.getTableIdentifier(), e);
      executionError = e;
    } finally {
      // persist cleanup result info.
      persistCleanupResult(
          tableRuntime, cleanupOperation, cleanupProcessMeta, cleanupEndTime, executionError);
      scheduledTables.remove(tableRuntime.getTableIdentifier());
      scheduleIfNecessary(tableRuntime, getNextExecutingTime(tableRuntime));
    }
  }

  protected final void scheduleIfNecessary(TableRuntime tableRuntime, long millisecondsTime) {
    if (isExecutable(tableRuntime)) {
      if (scheduledTables.add(tableRuntime.getTableIdentifier())) {
        executor.schedule(() -> executeTask(tableRuntime), millisecondsTime, TimeUnit.MILLISECONDS);
      }
    }
  }

  protected abstract long getNextExecutingTime(TableRuntime tableRuntime);

  protected abstract boolean enabled(TableRuntime tableRuntime);

  protected abstract void execute(TableRuntime tableRuntime);

  protected boolean shouldExecute(Long lastCleanupEndTime) {
    return true;
  }

  private void persistUpdatingCleanupTime(TableRuntime tableRuntime, long currentTime) {
    CleanupOperation cleanupOperation = getCleanupOperation();
    if (shouldSkipOperation(tableRuntime, cleanupOperation)) {
      return;
    }

    try {
      ((DefaultTableRuntime) tableRuntime).updateLastCleanTime(cleanupOperation, currentTime);

      logger.debug(
          "Update lastCleanTime for table {} with cleanup operation {}",
          tableRuntime.getTableIdentifier().getTableName(),
          cleanupOperation);
    } catch (Exception e) {
      logger.error(
          "Failed to update lastCleanTime for table {}",
          tableRuntime.getTableIdentifier().getTableName(),
          e);
    }
  }

  @VisibleForTesting
  public TableProcessMeta createCleanupProcessInfo(
      TableRuntime tableRuntime, CleanupOperation cleanupOperation) {
    if (shouldSkipOperation(tableRuntime, cleanupOperation)) {
      return null;
    }

    TableProcessMeta cleanupProcessMeta = buildCleanupProcessMeta(tableRuntime, cleanupOperation);
    persistenceHelper.beginAndPersistCleanupProcess(cleanupProcessMeta);
    logger.debug(
        "Successfully persist cleanup process [processId={}, tableId={}, processType={}]",
        cleanupProcessMeta.getProcessId(),
        cleanupProcessMeta.getTableId(),
        cleanupProcessMeta.getProcessType());

    return cleanupProcessMeta;
  }

  private TableProcessMeta buildCleanupProcessMeta(
      TableRuntime tableRuntime, CleanupOperation cleanupOperation) {
    TableProcessMeta cleanupProcessMeta = new TableProcessMeta();

    cleanupProcessMeta.setTableId(tableRuntime.getTableIdentifier().getId());
    cleanupProcessMeta.setProcessId(ID_GENERATOR.generateId());
    cleanupProcessMeta.setExternalProcessIdentifier(EXTERNAL_PROCESS_IDENTIFIER);
    cleanupProcessMeta.setStatus(ProcessStatus.RUNNING);
    cleanupProcessMeta.setProcessType(cleanupOperation.name());
    cleanupProcessMeta.setProcessStage(CLEANUP_PROCESS_STAGE);
    cleanupProcessMeta.setExecutionEngine(CLEANUP_EXECUTION_ENGINE);
    cleanupProcessMeta.setRetryNumber(0);
    cleanupProcessMeta.setFinishTime(0);
    cleanupProcessMeta.setFailMessage("");
    cleanupProcessMeta.setCreateTime(System.currentTimeMillis());
    cleanupProcessMeta.setProcessParameters(new HashMap<>());
    cleanupProcessMeta.setSummary(new HashMap<>());

    return cleanupProcessMeta;
  }

  @VisibleForTesting
  public void persistCleanupResult(
      TableRuntime tableRuntime,
      CleanupOperation cleanupOperation,
      TableProcessMeta cleanupProcessMeta,
      long cleanupEndTime,
      Exception executionError) {

    if (cleanupOperation == null
        || cleanupProcessMeta == null
        || shouldSkipOperation(tableRuntime, cleanupOperation)) {
      return;
    }

    cleanupProcessMeta.setFinishTime(cleanupEndTime);
    if (executionError != null) {
      cleanupProcessMeta.setStatus(ProcessStatus.FAILED);
      cleanupProcessMeta.setFailMessage(executionError.getMessage());
    } else {
      cleanupProcessMeta.setStatus(ProcessStatus.SUCCESS);
    }

    try {
      persistenceHelper.updateAndPersistCleanupProcess(cleanupProcessMeta);
    } catch (PersistenceException e) {
      logger.error(
          "Failed to persist cleanup process result [processId={}, tableId={}, processType={}]",
          cleanupProcessMeta.getProcessId(),
          cleanupProcessMeta.getTableId(),
          cleanupProcessMeta.getProcessType(),
          e);
    }

    logger.debug(
        "Successfully updated lastCleanTime and cleanupProcess for table {} with processId={}, cleanup operation {}",
        tableRuntime.getTableIdentifier().getTableName(),
        cleanupProcessMeta.getProcessId(),
        cleanupOperation);
  }

  private static class PersistenceHelper extends PersistentBase {

    public PersistenceHelper() {}

    private void beginAndPersistCleanupProcess(TableProcessMeta meta) {
      doAs(
          TableProcessMapper.class,
          mapper ->
              mapper.insertProcess(
                  meta.getTableId(),
                  meta.getProcessId(),
                  meta.getExternalProcessIdentifier(),
                  meta.getStatus(),
                  meta.getProcessType(),
                  meta.getProcessStage(),
                  meta.getExecutionEngine(),
                  meta.getRetryNumber(),
                  meta.getCreateTime(),
                  meta.getProcessParameters(),
                  meta.getSummary()));
    }

    private void updateAndPersistCleanupProcess(TableProcessMeta meta) {
      doAs(
          TableProcessMapper.class,
          mapper ->
              mapper.updateProcess(
                  meta.getTableId(),
                  meta.getProcessId(),
                  meta.getExternalProcessIdentifier(),
                  meta.getStatus(),
                  meta.getProcessStage(),
                  meta.getRetryNumber(),
                  meta.getFinishTime(),
                  meta.getFailMessage(),
                  meta.getProcessParameters(),
                  meta.getSummary()));
    }
  }

  /**
   * Get cleanup operation. Default is NONE, subclasses should override this method to provide
   * specific operation.
   *
   * @return cleanup operation
   */
  protected CleanupOperation getCleanupOperation() {
    return CleanupOperation.NONE;
  }

  protected boolean shouldExecuteTask(
      TableRuntime tableRuntime, CleanupOperation cleanupOperation) {
    if (shouldSkipOperation(tableRuntime, cleanupOperation)) {
      return true;
    }

    long lastCleanupEndTime =
        ((DefaultTableRuntime) tableRuntime).getLastCleanTime(cleanupOperation);

    // If it's zero, execute the task
    if (lastCleanupEndTime == 0L) {
      logger.debug(
          "LastCleanupTime for table {} with operation {} is not exist, executing task",
          tableRuntime.getTableIdentifier().getTableName(),
          cleanupOperation);
      return true;
    }

    // After ams restarts, certain cleanup operations can only be re-executed
    // if sufficient time has elapsed since the last cleanup.
    boolean result = shouldExecute(lastCleanupEndTime);
    logger.debug(
        result
            ? "Should execute task for table {} with {}"
            : "Not enough time has passed since last cleanup for table {} with {}, delaying execution",
        tableRuntime.getTableIdentifier().getTableName(),
        cleanupOperation);

    return result;
  }

  /**
   * Check if the operation should be skipped based on common conditions.
   *
   * @param tableRuntime the table runtime to check
   * @param cleanupOperation the cleanup operation to perform
   * @return true if the operation should be skipped, false otherwise
   */
  private boolean shouldSkipOperation(
      TableRuntime tableRuntime, CleanupOperation cleanupOperation) {
    if (cleanupOperation == CleanupOperation.NONE) {
      logger.debug(
          "No cleanup operation specified, skipping cleanup time check for table {}",
          tableRuntime.getTableIdentifier().getTableName());
      return true;
    }

    if (!(tableRuntime instanceof DefaultTableRuntime)) {
      logger.debug(
          "Table runtime is not DefaultTableRuntime, skipping cleanup time check for table {}",
          tableRuntime.getTableIdentifier().getTableName());
      return true;
    }

    return false;
  }

  protected String getThreadName() {
    return String.join("-", StringUtils.splitByCharacterTypeCamelCase(getClass().getSimpleName()))
        .toLowerCase(Locale.ROOT);
  }

  private boolean isExecutable(TableRuntime tableRuntime) {
    return tableService.contains(tableRuntime.getTableIdentifier().getId())
        && enabled(tableRuntime);
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
  protected void doDispose() {

    gracefulShutdown();
    logger.info("dispose thread pool for threads {}", getThreadName());
  }

  public void gracefulShutdown() {
    if (executor == null || executor.isShutdown()) {
      return;
    }

    try {
      // Stop accepting new tasks.
      executor.shutdown();

      // Wait for the current task to complete, with a maximum waiting time of 30 seconds.
      if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        // If the timeout occurs, try to cancel the task that is currently being executed.
        executor.shutdownNow();

        // Wait again for the task response to be cancelled.
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
          logger.error("The thread pool failed to close properly.");
        }
      }
    } catch (InterruptedException e) {
      // Re-cancel the interrupt status of the current thread.
      Thread.currentThread().interrupt();
      executor.shutdownNow();
    }
  }

  protected abstract long getExecutorDelay();

  protected long getStartDelay() {
    return START_DELAY + getExecutorDelay();
  }

  protected AmoroTable<?> loadTable(TableRuntime tableRuntime) {
    return tableService.loadTable(tableRuntime.getTableIdentifier());
  }

  public Action getAction() {
    return action;
  }
}

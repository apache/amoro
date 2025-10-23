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
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.amoro.server.table.cleanup.CleanupProcessPersistence;
import org.apache.amoro.server.table.cleanup.TableCleanupProcessMeta;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class PeriodicTableScheduler extends RuntimeHandlerChain {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private static final long START_DELAY = 10 * 1000L;
  private static final int BATCH_SIZE = 1000;

  protected final Set<ServerTableIdentifier> scheduledTables =
      Collections.synchronizedSet(new HashSet<>());
  private final Action action;
  private final ScheduledExecutorService executor;
  private final TableService tableService;

  private final Map<Long, Long> tableIdWithLastCleanTimeCache =
      Collections.synchronizedMap(Maps.newHashMap());
  private final CleanupProcessPersistence persistence;

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
    this.persistence = new CleanupProcessPersistence();
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
    this.persistence = new CleanupProcessPersistence();
  }

  @Override
  protected void initHandler(List<TableRuntime> tableRuntimeList) {
    // 1、Filter enabled tables
    List<TableRuntime> enabledTables =
        tableRuntimeList.stream().filter(this::enabled).collect(Collectors.toList());
    if (enabledTables.isEmpty()) {
      logger.info(
          "No enabled tables found for executor {}, skipping initialization",
          getClass().getSimpleName());
      return;
    }

    // 2、For specific cleanup executors, query existing cleanup records at startup
    CleanupOperation cleanupOperation = getCleanupOperation();
    if (cleanupOperation != CleanupOperation.NONE) {
      try {
        batchQueryExistingCleanupInfo(enabledTables, cleanupOperation);
      } catch (Exception e) {
        logger.warn("Failed to query table cleanup info for operation: {}", cleanupOperation, e);
      }
    }

    enabledTables.forEach(
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
    try {
      if (isExecutable(tableRuntime)) {
        execute(tableRuntime);

        // Different tables take different amounts of time to execute the end of execute(),
        // so you need to perform the save or update lastCleanEndTime operation separately for each
        // table.
        persistUpdatingCleanupTime(tableRuntime);
      }
    } finally {
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

  private void persistUpdatingCleanupTime(TableRuntime tableRuntime) {
    CleanupOperation cleanupOperation = getCleanupOperation();
    if (cleanupOperation == CleanupOperation.NONE) {
      logger.debug(
          "No cleanup operation found for table {}, skipping cleanup time update",
          tableRuntime.getTableIdentifier().getTableName());
      return;
    }

    try {
      long currentTime = System.currentTimeMillis();
      TableCleanupProcessMeta meta =
          new TableCleanupProcessMeta(
              currentTime,
              tableRuntime.getTableIdentifier().getId(),
              tableRuntime.getTableIdentifier().getCatalog(),
              tableRuntime.getTableIdentifier().getDatabase(),
              tableRuntime.getTableIdentifier().getTableName(),
              cleanupOperation,
              currentTime);
      persistence.upsertLastCleanupEndTimeByTableIdAndCleanupOperation(meta);

      logger.debug(
          "Upserted lastCleanupEndTime for table {} with cleanupOperation {}",
          tableRuntime.getTableIdentifier().getTableName(),
          cleanupOperation);
    } catch (Exception e) {
      logger.error(
          "Failed to upsert cleanup end time for table {}",
          tableRuntime.getTableIdentifier().getTableName(),
          e);
    }
  }

  private void persistDeletingCleanupRecords(TableRuntime tableRuntime) {
    try {
      persistence.deleteTableCleanupProcesses(tableRuntime.getTableIdentifier().getId());

      logger.debug(
          "Deleted cleanup records for table {}", tableRuntime.getTableIdentifier().getTableName());
    } catch (Exception e) {
      logger.error(
          "Failed to delete cleanup records for table {}",
          tableRuntime.getTableIdentifier().getTableName(),
          e);
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
    if (cleanupOperation == CleanupOperation.NONE) {
      logger.debug(
          "No cleanup operation specified, executing task for table {}",
          tableRuntime.getTableIdentifier().getTableName());
      return true;
    }

    // If there's no previous cleanup time, or it's zero, execute the task
    Long lastCleanupEndTime =
        tableIdWithLastCleanTimeCache.remove(tableRuntime.getTableIdentifier().getId());
    if (lastCleanupEndTime == null || lastCleanupEndTime == 0L) {
      logger.debug(
          "No previous cleanup time found for table {}, executing task",
          tableRuntime.getTableIdentifier().getTableName());
      return true;
    }

    // After ams restarts, certain cleanup operations can only be re-executed
    // if sufficient time has elapsed since the last cleanup.
    boolean result = shouldExecute(lastCleanupEndTime);
    logger.debug(
        result
            ? "Should execute task for table {}"
            : "Not enough time has passed since last cleanup for table {}, delaying execution",
        tableRuntime.getTableIdentifier().getTableName());

    return result;
  }

  /** Batch query cleanup records from table table_cleanup_process */
  private void batchQueryExistingCleanupInfo(
      List<TableRuntime> tableRuntimes, CleanupOperation cleanupOperation) {
    List<Long> tableIds =
        tableRuntimes.stream()
            .map(tableRuntime -> tableRuntime.getTableIdentifier().getId())
            .collect(Collectors.toList());
    if (tableIds.isEmpty()) {
      return;
    }

    // 1、Find existing cleanup records for the given table IDs
    Map<Long, Long> existingTableIdWithCleanTime =
        findExistingCleanupRecords(tableIds, cleanupOperation);

    // 2、Cache the tableId with the last cleanup time to determine later
    // whether the table requires cleanup operations.
    tableIdWithLastCleanTimeCache.putAll(existingTableIdWithCleanTime);
  }

  /** Find existing cleanup records for the given table IDs */
  private Map<Long, Long> findExistingCleanupRecords(
      List<Long> tableIds, CleanupOperation cleanupOperation) {
    Map<Long, Long> existingTableIdWithCleanTime = Maps.newHashMap();

    try {
      // Batch query
      processInBatches(
          tableIds,
          BATCH_SIZE,
          (batchTableIds, batchIndex, totalBatches) -> {
            Map<Long, Long> batchResult =
                persistence.selectTableCleanupProcess(batchTableIds, cleanupOperation);
            existingTableIdWithCleanTime.putAll(batchResult);

            logger.debug(
                "Queried batch {}/{} found {} existing records",
                batchIndex + 1,
                totalBatches,
                batchResult.size());
          });

      logger.debug(
          "Found {} existing cleanup process records out of {} tables",
          existingTableIdWithCleanTime.size(),
          tableIds.size());
    } catch (Exception e) {
      logger.warn(
          "Failed to batch check existing cleanup process records for {} tables",
          tableIds.size(),
          e);
    }

    return existingTableIdWithCleanTime;
  }

  interface BatchProcessor<T> {
    void process(List<T> batch, int batchIndex, int totalBatches) throws Exception;
  }

  /**
   * Process items in batches to avoid memory overflow and improve performance
   *
   * @param <T> the type of items to process
   * @param items the list of items to process
   * @param batchSize the maximum number of items to process in each batch
   * @param processor the processor to handle each batch
   */
  private <T> void processInBatches(List<T> items, int batchSize, BatchProcessor<T> processor)
      throws Exception {
    final int size = items.size();
    if (size <= batchSize) {
      // If the number of items does not exceed the batch size, process directly
      processor.process(items, 0, 1);
    } else {
      // If the number of items exceeds the batch size, process in batches
      int totalBatches = (size + batchSize - 1) / batchSize;
      logger.debug("Splitting {} items into batches of size {}", size, batchSize);

      for (int i = 0; i < size; i += batchSize) {
        int endIndex = Math.min(i + batchSize, size);
        List<T> batch = items.subList(i, endIndex);
        processor.process(batch, i / batchSize, totalBatches);
      }
    }
  }

  protected String getThreadName() {
    return String.join("-", StringUtils.splitByCharacterTypeCamelCase(getClass().getSimpleName()))
        .toLowerCase(Locale.ROOT);
  }

  protected boolean isExecutable(TableRuntime tableRuntime) {
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
    // when the table is removed, we need to delete the corresponding cleanup records
    // in table table_cleanup_process
    persistDeletingCleanupRecords(tableRuntime);
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

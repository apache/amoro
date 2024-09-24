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

package org.apache.amoro.server.table.executor;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseTableExecutor extends RuntimeHandlerChain {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private static final long START_DELAY = 10 * 1000L;

  private final ScheduledExecutorService executor;
  private final TableManager tableManager;
  private final Set<ServerTableIdentifier> scheduledTables =
      Collections.synchronizedSet(new HashSet<>());

  protected BaseTableExecutor(TableManager tableManager, int poolSize) {
    this.tableManager = tableManager;
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
                executor.schedule(
                    () -> executeTask(tableRuntime), getStartDelay(), TimeUnit.MILLISECONDS);
              }
            });

    logger.info("Table executor {} initialized", getClass().getSimpleName());
  }

  private void executeTask(TableRuntime tableRuntime) {
    try {
      if (isExecutable(tableRuntime)) {
        execute(tableRuntime);
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

  protected String getThreadName() {
    return String.join("-", StringUtils.splitByCharacterTypeCamelCase(getClass().getSimpleName()))
        .toLowerCase(Locale.ROOT);
  }

  private boolean isExecutable(TableRuntime tableRuntime) {
    return tableManager.contains(tableRuntime.getTableIdentifier().getId())
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
    executor.shutdownNow();
    logger.info("dispose thread pool for threads {}", getThreadName());
  }

  protected long getStartDelay() {
    return START_DELAY;
  }

  protected AmoroTable<?> loadTable(TableRuntime tableRuntime) {
    return tableManager.loadTable(tableRuntime.getTableIdentifier());
  }
}

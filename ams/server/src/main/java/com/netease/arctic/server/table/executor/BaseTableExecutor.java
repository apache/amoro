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

package com.netease.arctic.server.table.executor;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.table.RuntimeHandlerChain;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class BaseTableExecutor extends RuntimeHandlerChain {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private static final long START_DELAY = 10 * 1000L;

  private final ScheduledExecutorService executor;
  private final TableManager tableManager;
  private final ConcurrentHashMap<ServerTableIdentifier, ScheduledFuture<?>> scheduledTasks =
      new ConcurrentHashMap<>();

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
  protected void initHandler(List<TableRuntimeMeta> tableRuntimeMetaList) {
    tableRuntimeMetaList.stream()
        .map(tableRuntimeMeta -> tableRuntimeMeta.getTableRuntime())
        .filter(tableRuntime -> enabled(tableRuntime))
        .forEach(
            tableRuntime -> {
              ScheduledFuture<?> scheduledFuture =
                  executor.scheduleWithFixedDelay(
                      () -> executeTask(tableRuntime),
                      getStartDelay(),
                      getNextExecutingTime(tableRuntime),
                      TimeUnit.MILLISECONDS);
              scheduledTasks.put(tableRuntime.getTableIdentifier(), scheduledFuture);
            });
    logger.info("Table executor {} initialized", getClass().getSimpleName());
  }

  private void executeTask(TableRuntime tableRuntime) {
    if (isExecutable(tableRuntime)) {
      execute(tableRuntime);
    }
  }

  protected final void scheduleIfNecessary(TableRuntime tableRuntime, long millisecondsTime) {
    if (isExecutable(tableRuntime)) {
      if (scheduledTasks.containsKey(tableRuntime.getTableIdentifier())) {
        scheduledTasks.remove(tableRuntime.getTableIdentifier()).cancel(true);
      }
      ScheduledFuture<?> scheduledFuture =
          executor.scheduleWithFixedDelay(
              () -> executeTask(tableRuntime),
              millisecondsTime,
              getNextExecutingTime(tableRuntime),
              TimeUnit.MILLISECONDS);
      scheduledTasks.put(tableRuntime.getTableIdentifier(), scheduledFuture);
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
    return tableManager.contains(tableRuntime.getTableIdentifier()) && enabled(tableRuntime);
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

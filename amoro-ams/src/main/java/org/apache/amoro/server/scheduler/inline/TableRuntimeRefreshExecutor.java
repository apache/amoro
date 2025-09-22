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

package org.apache.amoro.server.scheduler.inline;

import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.refresh.RefreshEvent;
import org.apache.amoro.server.refresh.events.DefaultRefreshEvent;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/** Executor that refreshes table runtimes and evaluates optimizing status periodically. */
public class TableRuntimeRefreshExecutor extends PeriodicTableScheduler {
  private static final Logger logger = LoggerFactory.getLogger(TableRuntimeRefreshExecutor.class);
  // 1 minutes
  private final long interval;
  private final int maxPendingPartitions;
  private static final Map<String, RefreshEvent> REFRESH_EVENTS = new HashMap<>();
  private static final RefreshEvent DEFAULT_REFRESH_EVENT = new DefaultRefreshEvent();

  static {
    ServiceLoader<RefreshEvent> serviceLoader = ServiceLoader.load(RefreshEvent.class);
    Iterator<RefreshEvent> it = serviceLoader.iterator();
    it.forEachRemaining(
        refreshEvent -> {
          REFRESH_EVENTS.put(refreshEvent.getIdentifier(), refreshEvent);
          logger.info(
              "Load refresh event spi [{}] from {}",
              refreshEvent.getIdentifier(),
              refreshEvent.getClass());
        });
  }

  public TableRuntimeRefreshExecutor(
      TableService tableService, int poolSize, long interval, int maxPendingPartitions) {
    super(tableService, poolSize);
    this.interval = interval;
    this.maxPendingPartitions = maxPendingPartitions;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime instanceof DefaultTableRuntime;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;

    RefreshEvent refreshEvent =
        REFRESH_EVENTS.getOrDefault(
            defaultTableRuntime.getOptimizingConfig().getRefreshEventIdentifier(),
            DEFAULT_REFRESH_EVENT);
    return refreshEvent.getNextExecutingTime(tableRuntime, interval);
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
    // After disabling self-optimizing, close the currently running optimizing process.
    if (originalConfig.getOptimizingConfig().isEnabled()
        && !tableRuntime.getTableConfiguration().getOptimizingConfig().isEnabled()) {
      OptimizingProcess optimizingProcess = defaultTableRuntime.getOptimizingProcess();
      if (optimizingProcess != null && optimizingProcess.getStatus() == ProcessStatus.RUNNING) {
        optimizingProcess.close(false);
      }
    }
  }

  @Override
  protected long getExecutorDelay() {
    return 0;
  }

  @Override
  public void execute(TableRuntime tableRuntime) {
    try {
      Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
      DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;

      RefreshEvent refreshEvent =
          REFRESH_EVENTS.getOrDefault(
              defaultTableRuntime.getOptimizingConfig().getRefreshEventIdentifier(),
              DEFAULT_REFRESH_EVENT);
      logger.info(
          "Enable `self-optimizing.refresh-event.identifier`=`{}`. Use refresh event {} for table {}",
          defaultTableRuntime.getOptimizingConfig().getRefreshEventIdentifier(),
          refreshEvent.getIdentifier(),
          tableRuntime.getTableIdentifier());
      refreshEvent.execute(tableRuntime, tableService, maxPendingPartitions);
    } catch (Throwable throwable) {
      logger.error("Refreshing table {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
  }
}

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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.maintainer.TableMaintainer;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/** Service for expiring tables periodically. */
public class TableDataCleaningExecutor extends PeriodicTableScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(TableDataCleaningExecutor.class);

  private final long interval;

  public TableDataCleaningExecutor(TableService tableService, int poolSize, Duration interval) {
    super(tableService, poolSize);
    this.interval = interval.toMillis();
  }

  @Override
  protected long getNextExecutingTime(DefaultTableRuntime tableRuntime) {
    return interval;
  }

  @Override
  protected boolean enabled(DefaultTableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().isExpireSnapshotEnabled()
        || tableRuntime.getTableConfiguration().isCleanOrphanEnabled()
        || tableRuntime.getTableConfiguration().isDeleteDanglingDeleteFilesEnabled()
        || tableRuntime.getTableConfiguration().getExpiringDataConfig().isEnabled();
  }

  @Override
  public void handleConfigChanged(
      DefaultTableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  public void execute(DefaultTableRuntime tableRuntime) {
    try {
      LOG.info(
          "{} start cleaning expire data、expire snapshot、orphan and dangling delete files",
          tableRuntime.getTableIdentifier());
      AmoroTable<?> amoroTable = loadTable(tableRuntime);
      TableMaintainer tableMaintainer = TableMaintainer.ofTable(amoroTable);
      boolean hasCleaned = false;

      // When cleaning orphan File is executed, it internally executes a table refresh operation
      tableMaintainer.cleanOrphanFiles(tableRuntime);
      // If cleaning orphan File is executed, We need to mark the clean operation as having been
      // executed
      if (tableMaintainer.cleanDanglingDeleteFiles(tableRuntime)) {
        hasCleaned = true;
      }

      // If expiring data is enabled and hasCleaned is true, We need to refresh the table
      // to avoid errors caused by subsequent cleaning operations referencing deleted files.
      if (tableRuntime.getTableConfiguration().getExpiringDataConfig().isEnabled()) {
        if (hasCleaned) {
          tableMaintainer.refreshTable();
        }

        if (tableMaintainer.expireData(tableRuntime)) {
          hasCleaned = true;
        }
      }

      if (tableRuntime.getTableConfiguration().isExpireSnapshotEnabled()) {
        if (hasCleaned) {
          tableMaintainer.refreshTable();
        }
        tableMaintainer.expireSnapshots(tableRuntime);
      }
    } catch (Throwable t) {
      LOG.error(
          "{} failed to clean expire data、expire snapshot、orphan and dangling delete files",
          tableRuntime.getTableIdentifier(),
          t);
    }
  }
}

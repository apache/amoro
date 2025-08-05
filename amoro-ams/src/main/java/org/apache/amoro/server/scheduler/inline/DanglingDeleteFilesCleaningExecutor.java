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
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.maintainer.TableMaintainer;
import org.apache.amoro.server.optimizing.maintainer.TableMaintainers;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/** Clean table dangling delete files */
public class DanglingDeleteFilesCleaningExecutor extends PeriodicTableScheduler {

  private static final Logger LOG =
      LoggerFactory.getLogger(DanglingDeleteFilesCleaningExecutor.class);

  private static final long INTERVAL = 24 * 60 * 60 * 1000L;

  protected DanglingDeleteFilesCleaningExecutor(TableService tableService, int poolSize) {
    super(tableService, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime instanceof DefaultTableRuntime
        && tableRuntime.getTableConfiguration().isDeleteDanglingDeleteFilesEnabled();
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  protected long getExecutorDelay() {
    return ThreadLocalRandom.current().nextLong(INTERVAL);
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      LOG.info("{} start cleaning dangling delete files", tableRuntime.getTableIdentifier());
      AmoroTable<?> amoroTable = loadTable(tableRuntime);
      TableMaintainer tableMaintainer = TableMaintainers.create(amoroTable, tableRuntime);
      tableMaintainer.cleanDanglingDeleteFiles();
    } catch (Throwable t) {
      LOG.error("{} failed to clean dangling delete file", tableRuntime.getTableIdentifier(), t);
    }
  }
}

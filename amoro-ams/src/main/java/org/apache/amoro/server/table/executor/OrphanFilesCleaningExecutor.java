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
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.maintainer.TableMaintainer;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class OrphanFilesCleaningExecutor extends BaseTableExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(OrphanFilesCleaningExecutor.class);
  private final Duration interval;

  public OrphanFilesCleaningExecutor(TableManager tableRuntimes, int poolSize, Duration interval) {
    super(tableRuntimes, poolSize);
    this.interval = interval;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return interval.toMillis();
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().isCleanOrphanEnabled();
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  public void execute(TableRuntime tableRuntime) {
    try {
      LOG.info("{} start cleaning orphan files", tableRuntime.getTableIdentifier());
      AmoroTable<?> amoroTable = loadTable(tableRuntime);
      TableMaintainer tableMaintainer = TableMaintainer.ofTable(amoroTable);
      tableMaintainer.cleanOrphanFiles(tableRuntime);
    } catch (Throwable t) {
      LOG.error("{} failed to clean orphan file", tableRuntime.getTableIdentifier(), t);
    }
  }
}

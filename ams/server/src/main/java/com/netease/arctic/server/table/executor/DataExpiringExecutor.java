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
import com.netease.arctic.server.optimizing.maintainer.TableMaintainer;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class DataExpiringExecutor extends BaseTableExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DataExpiringExecutor.class);

  private final Duration interval;

  protected DataExpiringExecutor(TableManager tableManager, int poolSize, Duration interval) {
    super(tableManager, poolSize);
    this.interval = interval;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return interval.toMillis();
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().getExpiringDataConfig().isEnabled();
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      AmoroTable<?> amoroTable = loadTable(tableRuntime);
      TableMaintainer tableMaintainer = TableMaintainer.ofTable(amoroTable);
      tableMaintainer.expireData(tableRuntime);
    } catch (Throwable t) {
      LOG.error("unexpected expire error of table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }
}

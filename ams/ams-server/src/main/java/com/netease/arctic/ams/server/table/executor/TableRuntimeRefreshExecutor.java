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

package com.netease.arctic.ams.server.table.executor;

import com.netease.arctic.ams.server.table.TableRuntime;
import com.netease.arctic.ams.server.table.TableRuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for expiring tables periodically.
 */
public class TableRuntimeRefreshExecutor extends BaseTableExecutor {

  // 1 minutes
  private static final long MAX_INTERVAL = 60 * 1000L;

  public TableRuntimeRefreshExecutor(TableRuntimeManager tableRuntimes, int poolSize) {
    super(tableRuntimes, poolSize);
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.isOptimizingEnabled();
  }

  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return Math.min(tableRuntime.getOptimizingConfig().getMinorLeastInterval() * 4 / 5,
            MAX_INTERVAL);
  }

  @Override
  public void execute(TableRuntime tableRuntime) {
    try {
      tableRuntime.refresh();
    } catch (Throwable throwable) {
      logger.error("Refreshing table {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
  }
}

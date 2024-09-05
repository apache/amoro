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
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableRuntime;

import java.util.Optional;

public class OptimizingCommitExecutor extends BaseTableExecutor {

  private static final long INTERVAL = 60 * 1000L; // 1min

  public OptimizingCommitExecutor(TableManager tableRuntimes, int poolSize) {
    super(tableRuntimes, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getOptimizingStatus() == OptimizingStatus.COMMITTING;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    Optional.ofNullable(tableRuntime.getOptimizingProcess())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "OptimizingProcess is null while committing:" + tableRuntime))
        .commit();
  }

  @Override
  public void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  public void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {}

  protected long getStartDelay() {
    return 0;
  }
}

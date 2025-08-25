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
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;

import java.util.Optional;

public class OptimizingCommitExecutor extends PeriodicTableScheduler {

  private static final long INTERVAL = 60 * 1000L; // 1min

  public OptimizingCommitExecutor(TableService tableService, int poolSize) {
    super(tableService, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return Optional.of(tableRuntime)
        .filter(t -> t instanceof DefaultTableRuntime)
        .map(t -> (DefaultTableRuntime) t)
        .map(t -> t.getOptimizingStatus() == OptimizingStatus.COMMITTING)
        .orElse(false);
  }

  @Override
  protected long getExecutorDelay() {
    return 0;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    Optional.of(tableRuntime)
        .filter(t -> t instanceof DefaultTableRuntime)
        .map(t -> (DefaultTableRuntime) t)
        .map(DefaultTableRuntime::getOptimizingProcess)
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

  protected long getStartDelay() {
    return 0;
  }
}

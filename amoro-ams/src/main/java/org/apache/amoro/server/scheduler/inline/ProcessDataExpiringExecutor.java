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
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.utils.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class ProcessDataExpiringExecutor extends PeriodicTableScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessDataExpiringExecutor.class);

  private final Persistency persistency = new Persistency();
  private final long optimizingKeepTimeMs;
  private final long processKeepTimeMs;
  private final long expireIntervalMs;

  public ProcessDataExpiringExecutor(
      TableService tableService,
      Duration optimizingKeepTime,
      Duration expireInterval,
      Duration processKeepTime) {
    super(tableService, 1);
    this.optimizingKeepTimeMs = optimizingKeepTime.toMillis();
    this.processKeepTimeMs = processKeepTime.toMillis();
    this.expireIntervalMs = expireInterval.toMillis();
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return expireIntervalMs;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return true;
  }

  @Override
  protected long getExecutorDelay() {
    return 0;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      persistency.doExpiring(tableRuntime);
    } catch (Throwable throwable) {
      LOG.error(
          "Expiring table runtimes of {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
  }

  private class Persistency extends PersistentBase {
    public void doExpiring(TableRuntime tableRuntime) {
      long tableId = tableRuntime.getTableIdentifier().getId();
      long now = System.currentTimeMillis();

      // 1. Expire optimizing runtime data (optimizingKeepTimeMs, e.g. 30d)
      long optimizingMinId = SnowflakeIdGenerator.getMinSnowflakeId(now - optimizingKeepTimeMs);
      doAsTransaction(
          () ->
              doAs(
                  TableProcessMapper.class,
                  mapper -> mapper.deleteBefore(tableId, optimizingMinId)),
          () ->
              doAs(
                  OptimizingProcessMapper.class,
                  mapper -> mapper.deleteProcessStateBefore(tableId, optimizingMinId)),
          () ->
              doAs(
                  OptimizingProcessMapper.class,
                  mapper -> mapper.deleteTaskRuntimesBefore(tableId, optimizingMinId)),
          () ->
              doAs(
                  OptimizingProcessMapper.class,
                  mapper -> mapper.deleteOptimizingQuotaBefore(tableId, optimizingMinId)));

      // 2. Expire process history terminal records (processKeepTimeMs, e.g. 7d)
      //    Only deletes terminal records in the window between processKeepTime and keepTime,
      //    since records older than keepTime are already removed by step 1.
      if (processKeepTimeMs < optimizingKeepTimeMs) {
        long processMinId = SnowflakeIdGenerator.getMinSnowflakeId(now - processKeepTimeMs);
        doAs(
            TableProcessMapper.class,
            mapper -> mapper.deleteExpiredProcesses(tableId, processMinId));
      }
    }
  }
}

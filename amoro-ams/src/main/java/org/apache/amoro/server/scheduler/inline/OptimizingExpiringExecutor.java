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

import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizingExpiringExecutor extends PeriodicTableScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizingExpiringExecutor.class);

  private final Persistency persistency = new Persistency();
  private final long keepTime;
  private final long interval;

  public OptimizingExpiringExecutor(TableService tableService, int keepDays, int intervalHours) {
    super(tableService, 1);
    this.keepTime = keepDays * 24 * 60 * 60 * 1000L;
    this.interval = intervalHours * 60 * 60 * 1000L;
  }

  @Override
  protected long getNextExecutingTime(DefaultTableRuntime tableRuntime) {
    return interval;
  }

  @Override
  protected boolean enabled(DefaultTableRuntime tableRuntime) {
    return true;
  }

  @Override
  protected void execute(DefaultTableRuntime tableRuntime) {
    try {
      persistency.doExpiring(tableRuntime);
    } catch (Throwable throwable) {
      LOG.error(
          "Expiring table runtimes of {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
  }

  private class Persistency extends PersistentBase {
    public void doExpiring(DefaultTableRuntime tableRuntime) {
      long expireTime = System.currentTimeMillis() - keepTime;
      doAsTransaction(
          () ->
              doAs(
                  OptimizingMapper.class,
                  mapper ->
                      mapper.deleteOptimizingProcessBefore(
                          tableRuntime.getTableIdentifier().getId(), expireTime)),
          () ->
              doAs(
                  OptimizingMapper.class,
                  mapper ->
                      mapper.deleteTaskRuntimesBefore(
                          tableRuntime.getTableIdentifier().getId(), expireTime)),
          () ->
              doAs(
                  OptimizingMapper.class,
                  mapper ->
                      mapper.deleteOptimizingQuotaBefore(
                          tableRuntime.getTableIdentifier().getId(), expireTime)));
    }
  }
}

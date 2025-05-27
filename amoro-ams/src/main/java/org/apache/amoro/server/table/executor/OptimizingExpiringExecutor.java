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

import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizingExpiringExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizingExpiringExecutor.class);

  private final Persistency persistency = new Persistency();
  private final long keepTime;
  private final long interval;

  public OptimizingExpiringExecutor(int keepDays, int intervalHours) {
    this.keepTime = keepDays * 24 * 60 * 60 * 1000L;
    this.interval = intervalHours * 60 * 60 * 1000L;
  }

  public long getInterval() {
    return interval;
  }

  public long getKeepTime() {
    return keepTime;
  }

  public void execute() {
    try {
      persistency.doExpiring();
    } catch (Throwable throwable) {
      LOG.error("Expiring  failed.", throwable);
    }
  }

  private class Persistency extends PersistentBase {
    public void doExpiring() {
      long expireTime = System.currentTimeMillis() - keepTime;
      doAsTransaction(
          () ->
              doAs(
                  OptimizingMapper.class,
                  mapper -> mapper.deleteOptimizingProcessBefore(expireTime)),
          () -> doAs(OptimizingMapper.class, mapper -> mapper.deleteTaskRuntimesBefore(expireTime)),
          () ->
              doAs(
                  OptimizingMapper.class,
                  mapper -> mapper.deleteOptimizingQuotaBefore(expireTime)));
    }
  }
}

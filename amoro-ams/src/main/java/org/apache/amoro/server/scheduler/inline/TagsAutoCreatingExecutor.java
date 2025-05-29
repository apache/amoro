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
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.maintainer.TableMaintainer;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for automatically creating tags for table periodically. */
public class TagsAutoCreatingExecutor extends PeriodicTableScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(TagsAutoCreatingExecutor.class);

  private final long interval;

  protected TagsAutoCreatingExecutor(TableService tableService, int poolSize, long interval) {
    super(tableService, poolSize);
    this.interval = interval;
  }

  @Override
  protected long getNextExecutingTime(DefaultTableRuntime tableRuntime) {
    return interval;
  }

  @Override
  protected boolean enabled(DefaultTableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().getTagConfiguration().isAutoCreateTag()
        && tableRuntime.getFormat() == TableFormat.ICEBERG;
  }

  @Override
  protected void execute(DefaultTableRuntime tableRuntime) {
    try {
      AmoroTable<?> amoroTable = loadTable(tableRuntime);
      TableMaintainer tableMaintainer = TableMaintainer.ofTable(amoroTable);
      tableMaintainer.autoCreateTags(tableRuntime);
    } catch (Throwable t) {
      LOG.error("Failed to create tags on {}", tableRuntime.getTableIdentifier(), t);
    }
  }

  @Override
  public void handleConfigChanged(
      DefaultTableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }
}

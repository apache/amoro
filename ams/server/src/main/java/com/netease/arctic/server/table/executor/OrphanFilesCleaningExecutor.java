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
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.netease.arctic.server.optimizing.maintainer.TableMaintainer.createMaintainer;

public class OrphanFilesCleaningExecutor extends BaseTableExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(OrphanFilesCleaningExecutor.class);
  // same as org.apache.iceberg.flink.sink.IcebergFilesCommitter#FLINK_JOB_ID

  private static final long INTERVAL = 24 * 60 * 60 * 1000L;

  public OrphanFilesCleaningExecutor(TableManager tableRuntimes, int poolSize) {
    super(tableRuntimes, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
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
      LOG.info("{} clean orphan files", tableRuntime.getTableIdentifier());
      AmoroTable<?> amoroTable = loadTable(tableRuntime);

      boolean needOrphanClean = CompatiblePropertyUtil.propertyAsBoolean(
          amoroTable.properties(),
          TableProperties.ENABLE_ORPHAN_CLEAN,
          TableProperties.ENABLE_ORPHAN_CLEAN_DEFAULT);

      if (!needOrphanClean) {
        return;
      }

      long keepTime = CompatiblePropertyUtil.propertyAsLong(
          amoroTable.properties(),
          TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME,
          TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT) * 60 * 1000;

      LOG.info("{} clean orphan files, keepTime={}", tableRuntime.getTableIdentifier(), keepTime);

      TableMaintainer tableMaintainer = createMaintainer(amoroTable);

      // clear data files
      tableMaintainer.cleanContentFiles(System.currentTimeMillis() - keepTime);

      //refresh
      tableMaintainer = createMaintainer(loadTable(tableRuntime));

      // clear metadata files
      tableMaintainer.cleanMetadata(System.currentTimeMillis() - keepTime);

      boolean needCleanDanglingDeleteFiles = CompatiblePropertyUtil.propertyAsBoolean(
          amoroTable.properties(),
          TableProperties.ENABLE_DANGLING_DELETE_FILES_CLEAN,
          TableProperties.ENABLE_DANGLING_DELETE_FILES_CLEAN_DEFAULT);

      if (!needCleanDanglingDeleteFiles) {
        return;
      }
      // clear dangling delete files
      tableMaintainer.cleanDanglingDeleteFiles();
    } catch (Throwable t) {
      LOG.error("{} orphan file clean unexpected error", tableRuntime.getTableIdentifier(), t);
    }
  }
}


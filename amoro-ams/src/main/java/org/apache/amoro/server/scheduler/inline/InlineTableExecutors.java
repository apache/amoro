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

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.table.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InlineTableExecutors {

  private static final InlineTableExecutors instance = new InlineTableExecutors();
  private static final Logger LOG = LoggerFactory.getLogger(InlineTableExecutors.class);
  private TableDataCleaningExecutor tableDataCleaningExecutor;
  private TableRuntimeRefreshExecutor tableRefreshingExecutor;
  private BlockerExpiringExecutor blockerExpiringExecutor;
  private OptimizingCommitExecutor optimizingCommitExecutor;
  private OptimizingExpiringExecutor optimizingExpiringExecutor;
  private HiveCommitSyncExecutor hiveCommitSyncExecutor;
  private TagsAutoCreatingExecutor tagsAutoCreatingExecutor;

  public static InlineTableExecutors getInstance() {
    return instance;
  }

  public void setup(TableService tableService, Configurations conf) {
    if (conf.getBoolean(AmoroManagementConf.EXPIRE_SNAPSHOTS_ENABLED)
        || conf.getBoolean(AmoroManagementConf.DATA_EXPIRATION_ENABLED)
        || conf.getBoolean(AmoroManagementConf.CLEAN_ORPHAN_FILES_ENABLED)
        || conf.getBoolean(AmoroManagementConf.CLEAN_DANGLING_DELETE_FILES_ENABLED)) {
      LOG.info(
          "Clean Orphan Files Enabled: {}, Clean Dangling Delete Files Enabled: {}, Data Expiration Enabled: {}, Snapshot Expiration Enabled: {}",
          conf.getBoolean(AmoroManagementConf.CLEAN_ORPHAN_FILES_ENABLED),
          conf.getBoolean(AmoroManagementConf.CLEAN_DANGLING_DELETE_FILES_ENABLED),
          conf.getBoolean(AmoroManagementConf.DATA_EXPIRATION_ENABLED),
          conf.getBoolean(AmoroManagementConf.EXPIRE_SNAPSHOTS_ENABLED));
      this.tableDataCleaningExecutor =
          new TableDataCleaningExecutor(
              tableService,
              conf.getInteger(AmoroManagementConf.CLEAN_TABLE_DATA_THREAD_COUNT),
              conf.get(AmoroManagementConf.CLEAN_TABLE_DATA_INTERVAL));
    }
    this.optimizingCommitExecutor =
        new OptimizingCommitExecutor(
            tableService, conf.getInteger(AmoroManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT));
    this.optimizingExpiringExecutor =
        new OptimizingExpiringExecutor(
            tableService,
            conf.getInteger(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_KEEP_DAYS),
            conf.getInteger(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_EXPIRE_INTERVAL_HOURS));
    this.blockerExpiringExecutor = new BlockerExpiringExecutor(tableService);
    if (conf.getBoolean(AmoroManagementConf.SYNC_HIVE_TABLES_ENABLED)) {
      this.hiveCommitSyncExecutor =
          new HiveCommitSyncExecutor(
              tableService, conf.getInteger(AmoroManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT));
    }
    this.tableRefreshingExecutor =
        new TableRuntimeRefreshExecutor(
            tableService,
            conf.getInteger(AmoroManagementConf.REFRESH_TABLES_THREAD_COUNT),
            conf.get(AmoroManagementConf.REFRESH_TABLES_INTERVAL).toMillis(),
            conf.getInteger(AmoroManagementConf.REFRESH_MAX_PENDING_PARTITIONS));
    if (conf.getBoolean(AmoroManagementConf.AUTO_CREATE_TAGS_ENABLED)) {
      this.tagsAutoCreatingExecutor =
          new TagsAutoCreatingExecutor(
              tableService,
              conf.getInteger(AmoroManagementConf.AUTO_CREATE_TAGS_THREAD_COUNT),
              conf.get(AmoroManagementConf.AUTO_CREATE_TAGS_INTERVAL).toMillis());
    }
  }

  public TableDataCleaningExecutor getTableDataCleaningExecutor() {
    return tableDataCleaningExecutor;
  }

  public TableRuntimeRefreshExecutor getTableRefreshingExecutor() {
    return tableRefreshingExecutor;
  }

  public BlockerExpiringExecutor getBlockerExpiringExecutor() {
    return blockerExpiringExecutor;
  }

  public OptimizingCommitExecutor getOptimizingCommitExecutor() {
    return optimizingCommitExecutor;
  }

  public OptimizingExpiringExecutor getOptimizingExpiringExecutor() {
    return optimizingExpiringExecutor;
  }

  public HiveCommitSyncExecutor getHiveCommitSyncExecutor() {
    return hiveCommitSyncExecutor;
  }

  public TagsAutoCreatingExecutor getTagsAutoCreatingExecutor() {
    return tagsAutoCreatingExecutor;
  }
}

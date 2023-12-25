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

import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.utils.Configurations;

public class AsyncTableExecutors {

  private static final AsyncTableExecutors instance = new AsyncTableExecutors();
  private SnapshotsExpiringExecutor snapshotsExpiringExecutor;
  private TableRuntimeRefreshExecutor tableRefreshingExecutor;
  private OrphanFilesCleaningExecutor orphanFilesCleaningExecutor;
  private DanglingDeleteFilesCleaningExecutor danglingDeleteFilesCleaningExecutor;
  private BlockerExpiringExecutor blockerExpiringExecutor;
  private OptimizingCommitExecutor optimizingCommitExecutor;
  private OptimizingExpiringExecutor optimizingExpiringExecutor;
  private HiveCommitSyncExecutor hiveCommitSyncExecutor;
  private TagsAutoCreatingExecutor tagsAutoCreatingExecutor;
  private DataExpiringExecutor dataExpiringExecutor;

  public static AsyncTableExecutors getInstance() {
    return instance;
  }

  public void setup(TableManager tableManager, Configurations conf) {
    if (conf.getBoolean(ArcticManagementConf.EXPIRE_SNAPSHOTS_ENABLED)) {
      this.snapshotsExpiringExecutor =
          new SnapshotsExpiringExecutor(
              tableManager, conf.getInteger(ArcticManagementConf.EXPIRE_SNAPSHOTS_THREAD_COUNT));
    }
    if (conf.getBoolean(ArcticManagementConf.CLEAN_ORPHAN_FILES_ENABLED)) {
      this.orphanFilesCleaningExecutor =
          new OrphanFilesCleaningExecutor(
              tableManager, conf.getInteger(ArcticManagementConf.CLEAN_ORPHAN_FILES_THREAD_COUNT));
    }
    if (conf.getBoolean(ArcticManagementConf.CLEAN_DANGLING_DELETE_FILES_ENABLED)) {
      this.danglingDeleteFilesCleaningExecutor =
          new DanglingDeleteFilesCleaningExecutor(
              tableManager,
              conf.getInteger(ArcticManagementConf.CLEAN_DANGLING_DELETE_FILES_THREAD_COUNT));
    }
    this.optimizingCommitExecutor =
        new OptimizingCommitExecutor(
            tableManager, conf.getInteger(ArcticManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT));
    this.optimizingExpiringExecutor = new OptimizingExpiringExecutor(tableManager);
    this.blockerExpiringExecutor = new BlockerExpiringExecutor(tableManager);
    if (conf.getBoolean(ArcticManagementConf.SYNC_HIVE_TABLES_ENABLED)) {
      this.hiveCommitSyncExecutor =
          new HiveCommitSyncExecutor(
              tableManager, conf.getInteger(ArcticManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT));
    }
    this.tableRefreshingExecutor =
        new TableRuntimeRefreshExecutor(
            tableManager,
            conf.getInteger(ArcticManagementConf.REFRESH_TABLES_THREAD_COUNT),
            conf.getLong(ArcticManagementConf.REFRESH_TABLES_INTERVAL));
    if (conf.getBoolean(ArcticManagementConf.AUTO_CREATE_TAGS_ENABLED)) {
      this.tagsAutoCreatingExecutor =
          new TagsAutoCreatingExecutor(
              tableManager,
              conf.getInteger(ArcticManagementConf.AUTO_CREATE_TAGS_THREAD_COUNT),
              conf.getLong(ArcticManagementConf.AUTO_CREATE_TAGS_INTERVAL));
    }
    if (conf.getBoolean(ArcticManagementConf.DATA_EXPIRATION_ENABLED)) {
      this.dataExpiringExecutor =
          new DataExpiringExecutor(
              tableManager,
              conf.getInteger(ArcticManagementConf.DATA_EXPIRATION_THREAD_COUNT),
              conf.get(ArcticManagementConf.DATA_EXPIRATION_INTERVAL));
    }
  }

  public SnapshotsExpiringExecutor getSnapshotsExpiringExecutor() {
    return snapshotsExpiringExecutor;
  }

  public TableRuntimeRefreshExecutor getTableRefreshingExecutor() {
    return tableRefreshingExecutor;
  }

  public OrphanFilesCleaningExecutor getOrphanFilesCleaningExecutor() {
    return orphanFilesCleaningExecutor;
  }

  public DanglingDeleteFilesCleaningExecutor getDanglingDeleteFilesCleaningExecutor() {
    return danglingDeleteFilesCleaningExecutor;
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

  public DataExpiringExecutor getDataExpiringExecutor() {
    return dataExpiringExecutor;
  }
}

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

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.table.TableManager;

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
    if (conf.getBoolean(AmoroManagementConf.EXPIRE_SNAPSHOTS_ENABLED)) {
      this.snapshotsExpiringExecutor =
          new SnapshotsExpiringExecutor(
              tableManager, conf.getInteger(AmoroManagementConf.EXPIRE_SNAPSHOTS_THREAD_COUNT));
    }
    if (conf.getBoolean(AmoroManagementConf.CLEAN_ORPHAN_FILES_ENABLED)) {
      this.orphanFilesCleaningExecutor =
          new OrphanFilesCleaningExecutor(
              tableManager,
              conf.getInteger(AmoroManagementConf.CLEAN_ORPHAN_FILES_THREAD_COUNT),
              conf.get(AmoroManagementConf.CLEAN_ORPHAN_FILES_INTERVAL));
    }
    if (conf.getBoolean(AmoroManagementConf.CLEAN_DANGLING_DELETE_FILES_ENABLED)) {
      this.danglingDeleteFilesCleaningExecutor =
          new DanglingDeleteFilesCleaningExecutor(
              tableManager,
              conf.getInteger(AmoroManagementConf.CLEAN_DANGLING_DELETE_FILES_THREAD_COUNT));
    }
    this.optimizingCommitExecutor =
        new OptimizingCommitExecutor(
            tableManager, conf.getInteger(AmoroManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT));
    this.optimizingExpiringExecutor =
        new OptimizingExpiringExecutor(
            tableManager,
            conf.getInteger(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_KEEP_DAYS),
            conf.getInteger(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_EXPIRE_INTERVAL_HOURS));
    this.blockerExpiringExecutor = new BlockerExpiringExecutor(tableManager);
    if (conf.getBoolean(AmoroManagementConf.SYNC_HIVE_TABLES_ENABLED)) {
      this.hiveCommitSyncExecutor =
          new HiveCommitSyncExecutor(
              tableManager, conf.getInteger(AmoroManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT));
    }
    this.tableRefreshingExecutor =
        new TableRuntimeRefreshExecutor(
            tableManager,
            conf.getInteger(AmoroManagementConf.REFRESH_TABLES_THREAD_COUNT),
            conf.getLong(AmoroManagementConf.REFRESH_TABLES_INTERVAL));
    if (conf.getBoolean(AmoroManagementConf.AUTO_CREATE_TAGS_ENABLED)) {
      this.tagsAutoCreatingExecutor =
          new TagsAutoCreatingExecutor(
              tableManager,
              conf.getInteger(AmoroManagementConf.AUTO_CREATE_TAGS_THREAD_COUNT),
              conf.getLong(AmoroManagementConf.AUTO_CREATE_TAGS_INTERVAL));
    }
    if (conf.getBoolean(AmoroManagementConf.DATA_EXPIRATION_ENABLED)) {
      this.dataExpiringExecutor =
          new DataExpiringExecutor(
              tableManager,
              conf.getInteger(AmoroManagementConf.DATA_EXPIRATION_THREAD_COUNT),
              conf.get(AmoroManagementConf.DATA_EXPIRATION_INTERVAL));
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

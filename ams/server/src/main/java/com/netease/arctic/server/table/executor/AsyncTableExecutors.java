package com.netease.arctic.server.table.executor;

import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.table.TableRuntimeManager;
import com.netease.arctic.server.utils.Configurations;

public class AsyncTableExecutors {

  private static final AsyncTableExecutors instance = new AsyncTableExecutors();

  public static AsyncTableExecutors getInstance() {
    return instance;
  }

  private SnapshotsExpiringExecutor snapshotsExpiringExecutor;
  private TableRuntimeRefreshExecutor tableRefreshingExecutor;
  private OrphanFilesCleaningExecutor orphanFilesCleaningExecutor;
  private OptimizingCommitExecutor optimizingCommitExecutor;
  private OptimizingExpiringExecutor optimizingExpiringExecutor;
  private HiveCommitSyncExecutor hiveCommitSyncExecutor;

  public void initialize(TableRuntimeManager tableRuntimes, Configurations conf) {
    this.snapshotsExpiringExecutor = new SnapshotsExpiringExecutor(tableRuntimes,
            conf.getInteger(ArcticManagementConf.EXPIRE_THREAD_POOL_SIZE));
    this.orphanFilesCleaningExecutor = new OrphanFilesCleaningExecutor(tableRuntimes,
            conf.getInteger(ArcticManagementConf.ORPHAN_CLEAN_THREAD_POOL_SIZE));
    this.optimizingCommitExecutor = new OptimizingCommitExecutor(tableRuntimes,
            conf.getLong(ArcticManagementConf.OPTIMIZING_CHECK_STATUS_INTERVAL),
            conf.getInteger(ArcticManagementConf.OPTIMIZING_CHECK_THREAD_POOL_SIZE));
    this.optimizingExpiringExecutor = new OptimizingExpiringExecutor(tableRuntimes);
    this.hiveCommitSyncExecutor = new HiveCommitSyncExecutor(tableRuntimes,
            conf.getInteger(ArcticManagementConf.SUPPORT_HIVE_SYNC_THREAD_POOL_SIZE));
    this.tableRefreshingExecutor = new TableRuntimeRefreshExecutor(tableRuntimes,
            conf.getInteger(ArcticManagementConf.SNAPSHOTS_REFRESHING_THREAD_POOL_SIZE));
  }
}
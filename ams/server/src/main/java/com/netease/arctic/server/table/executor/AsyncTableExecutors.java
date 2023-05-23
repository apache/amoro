package com.netease.arctic.server.table.executor;

import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.utils.Configurations;

public class AsyncTableExecutors {

  private static final AsyncTableExecutors instance = new AsyncTableExecutors();
  private SnapshotsExpiringExecutor snapshotsExpiringExecutor;
  private TableRuntimeRefreshExecutor tableRefreshingExecutor;
  private OrphanFilesCleaningExecutor orphanFilesCleaningExecutor;
  private BlockerExpiringExecutor blockerExpiringExecutor;
  private OptimizingCommitExecutor optimizingCommitExecutor;
  private OptimizingExpiringExecutor optimizingExpiringExecutor;
  private HiveCommitSyncExecutor hiveCommitSyncExecutor;

  public static AsyncTableExecutors getInstance() {
    return instance;
  }

  public void setup(TableManager tableManager, Configurations conf) {
    this.snapshotsExpiringExecutor = new SnapshotsExpiringExecutor(tableManager,
        conf.getInteger(ArcticManagementConf.EXPIRE_THREAD_POOL_SIZE));
    this.orphanFilesCleaningExecutor = new OrphanFilesCleaningExecutor(tableManager,
        conf.getInteger(ArcticManagementConf.ORPHAN_CLEAN_THREAD_POOL_SIZE));
    this.optimizingCommitExecutor = new OptimizingCommitExecutor(tableManager,
        conf.getInteger(ArcticManagementConf.OPTIMIZING_COMMIT_THREAD_POOL_SIZE));
    this.optimizingExpiringExecutor = new OptimizingExpiringExecutor(tableManager);
    this.blockerExpiringExecutor = new BlockerExpiringExecutor(tableManager);
    this.hiveCommitSyncExecutor = new HiveCommitSyncExecutor(tableManager,
        conf.getInteger(ArcticManagementConf.SUPPORT_HIVE_SYNC_THREAD_POOL_SIZE));
    this.tableRefreshingExecutor = new TableRuntimeRefreshExecutor(tableManager,
        conf.getInteger(ArcticManagementConf.SNAPSHOTS_REFRESHING_THREAD_POOL_SIZE),
        conf.getLong(ArcticManagementConf.SNAPSHOTS_REFRESHING_INTERVAL));
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
}
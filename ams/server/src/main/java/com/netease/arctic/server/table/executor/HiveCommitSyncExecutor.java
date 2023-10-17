package com.netease.arctic.server.table.executor;

import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.HiveMetaSynchronizer;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveCommitSyncExecutor extends BaseTableExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(HiveCommitSyncExecutor.class);

  // 10 minutes
  private static final long INTERVAL = 10 * 60 * 1000L;

  public HiveCommitSyncExecutor(TableManager tableRuntimes, int poolSize) {
    super(tableRuntimes, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return true;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    long startTime = System.currentTimeMillis();
    ServerTableIdentifier tableIdentifier = tableRuntime.getTableIdentifier();
    try {
      ArcticTable arcticTable = (ArcticTable) loadTable(tableRuntime).originalTable();
      if (!TableTypeUtil.isHive(arcticTable)) {
        LOG.debug("{} is not a support hive table", tableIdentifier);
        return;
      }
      LOG.info("{} start hive sync", tableIdentifier);
      syncIcebergToHive(arcticTable);
    } catch (Exception e) {
      LOG.error("{} hive sync failed", tableIdentifier, e);
    } finally {
      LOG.info("{} hive sync finished, cost {}ms", tableIdentifier,
          System.currentTimeMillis() - startTime);
    }
  }

  public static void syncIcebergToHive(ArcticTable arcticTable) {
    HiveMetaSynchronizer.syncArcticDataToHive((SupportHive) arcticTable);
  }
}

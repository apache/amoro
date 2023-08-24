package com.netease.arctic.server.table.executor;

import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataExpiringExecutor extends BaseTableExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DataExpiringExecutor.class);

  private final long interval;

  protected DataExpiringExecutor(TableManager tableManager, int poolSize, long interval) {
    super(tableManager, poolSize);
    this.interval = interval;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return interval * 1000;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().getExpiringDataConfig().isEnabled();
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      ArcticTable arcticTable = loadTable(tableRuntime);
      if (!CompatiblePropertyUtil.propertyAsBoolean(
          arcticTable.properties(),
          TableProperties.ENABLE_DATA_EXPIRE,
          TableProperties.ENABLE_DATA_EXPIRE_DEFAULT)) {
        return;
      }

      purgeArcticData(arcticTable, tableRuntime);
    } catch (Throwable t) {
      LOG.error("unexpected expire error of table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }

  private void purgeArcticData(ArcticTable table, TableRuntime tableRuntime) {
    //do something
  }
}

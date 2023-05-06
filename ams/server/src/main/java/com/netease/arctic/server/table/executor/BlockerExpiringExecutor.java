package com.netease.arctic.server.table.executor;

import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.TableBlockerMapper;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeManager;

public class BlockerExpiringExecutor extends BaseTableExecutor {

  private final Persistency persistency = new Persistency();

  public BlockerExpiringExecutor(TableRuntimeManager tableRuntimes) {
    super(tableRuntimes, 1);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return 0;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return false;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      persistency.doExpiring(tableRuntime);
    } catch (Throwable t) {
      logger.error("table {} expire blocker failed.", tableRuntime.getTableIdentifier(), t);
    }
  }

  private static class Persistency extends PersistentBase {

    public void doExpiring(TableRuntime tableRuntime) {
      doAs(TableBlockerMapper.class,
          mapper -> mapper.deleteExpiredBlockers(tableRuntime.getTableIdentifier(), System.currentTimeMillis()));
    }
  }
}

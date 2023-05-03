package com.netease.arctic.ams.server.table.executor;

import com.netease.arctic.ams.server.optimizing.OptimizingStatus;
import com.netease.arctic.ams.server.table.TableRuntime;
import com.netease.arctic.ams.server.table.TableRuntimeManager;
import com.netease.arctic.table.ArcticTable;

import java.util.Optional;

public class OptimizingCommitExecutor extends BaseTableExecutor {

  private long interval;

  public OptimizingCommitExecutor(TableRuntimeManager tableRuntimes, long interval, int poolSize) {
    super(tableRuntimes, poolSize);
    this.interval = interval;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return interval;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getOptimizingStatus() == OptimizingStatus.COMMITTING;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    Optional.ofNullable(tableRuntime.getOptimizingProcess())
        .orElseThrow(() -> new IllegalStateException("OptimizingProcess is null while committing:" + tableRuntime))
        .commit();
  }

  @Override
  public void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  public void handleTableAdded(ArcticTable table, TableRuntime tableRuntime) {
  }

  protected long getStartDelay() {
    return 0;
  }
}

package com.netease.arctic.server.table.executor;

import static com.netease.arctic.server.optimizing.maintainer.TableMaintainer.ofTable;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.server.optimizing.maintainer.TableMaintainer;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DanglingDeleteFilesCleaningExecutor extends BaseTableExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(OrphanFilesCleaningExecutor.class);

  private static final long INTERVAL = 24 * 60 * 60 * 1000L;

  protected DanglingDeleteFilesCleaningExecutor(TableManager tableManager, int poolSize) {
    super(tableManager, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().isDeleteDanglingDeleteFilesEnabled();
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      LOG.info("{} start cleaning dangling delete files", tableRuntime.getTableIdentifier());
      AmoroTable<?> amoroTable = loadTable(tableRuntime);
      TableMaintainer tableMaintainer = ofTable(amoroTable);
      tableMaintainer.cleanDanglingDeleteFiles(tableRuntime);
    } catch (Throwable t) {
      LOG.error("{} failed to clean dangling delete file", tableRuntime.getTableIdentifier(), t);
    }
  }
}

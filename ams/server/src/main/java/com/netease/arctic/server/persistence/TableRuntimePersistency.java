package com.netease.arctic.server.persistence;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.config.TableConfiguration;
import com.netease.arctic.ams.api.process.PendingInput;

public class TableRuntimePersistency {
  private long tableId;
  private String catalogName;
  private String dbName;
  private String tableName;
  private TableFormat format;
  private String optimizerGroup;
  private TableConfiguration tableConfig;
  private PendingInput pendingInput;

  private TableRuntimePersistency() {}

  public long getTableId() {
    return tableId;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public String getDbName() {
    return dbName;
  }

  public String getTableName() {
    return tableName;
  }

  public TableFormat getFormat() {
    return format;
  }

  public String getOptimizerGroup() {
    return optimizerGroup;
  }

  public TableConfiguration getTableConfig() {
    return tableConfig;
  }

  public PendingInput getPendingInput() {
    return pendingInput;
  }
}

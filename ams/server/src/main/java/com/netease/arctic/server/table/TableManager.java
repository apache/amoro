package com.netease.arctic.server.table;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableIdentifier;

public interface TableManager extends TableRuntimeHandler {

  AmoroTable<?> loadTable(TableIdentifier tableIdentifier);

  TableRuntime getRuntime(ServerTableIdentifier tableIdentifier);

  default boolean contains(ServerTableIdentifier tableIdentifier) {
    return getRuntime(tableIdentifier) != null;
  }
}

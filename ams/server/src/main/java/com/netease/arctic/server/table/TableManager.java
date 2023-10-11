package com.netease.arctic.server.table;

import com.netease.arctic.AmoroTable;

public interface TableManager extends TableRuntimeHandler {

  AmoroTable<?> loadTable(ServerTableIdentifier tableIdentifier);

  TableRuntime getRuntime(ServerTableIdentifier tableIdentifier);

  default boolean contains(ServerTableIdentifier tableIdentifier) {
    return getRuntime(tableIdentifier) != null;
  }
}

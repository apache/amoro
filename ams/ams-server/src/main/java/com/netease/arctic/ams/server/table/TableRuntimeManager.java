package com.netease.arctic.ams.server.table;

public interface TableRuntimeManager extends TableRuntimeInitializer {

  void initialize();

  TableRuntime get(ServerTableIdentifier tableIdentifier);

  default boolean contains(ServerTableIdentifier tableIdentifier) {
    return get(tableIdentifier) != null;
  }

  void addHandler(TableRuntimeHandler handler);
}

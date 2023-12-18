package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.Action;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface TableScheduler {

  Pair<TableRuntime, Action> scheduleTable();

  void refreshTable(TableRuntime tableRuntime);

  void releaseTable(TableRuntime tableRuntime);

  boolean containsTable(ServerTableIdentifier identifier);

  List<TableRuntime> listTables();
}

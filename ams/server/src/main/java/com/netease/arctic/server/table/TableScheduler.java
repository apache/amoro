package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.ams.api.TableRuntime;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface TableScheduler {

  Pair<DefaultTableRuntime, Action> scheduleTable();

  void refreshTable(DefaultTableRuntime tableRuntime);

  void releaseTable(DefaultTableRuntime tableRuntime);

  boolean containsTable(ServerTableIdentifier identifier);

  List<TableRuntime> listTables();
}

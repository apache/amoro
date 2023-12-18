package com.netease.arctic.server.table;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.server.process.optimizing.OptimizingStage;

public interface TableWatcher {

  void start();

  void tableAdded(TableRuntime tableRuntime, AmoroTable<?> table);

  void tableRemoved(TableRuntime tableRuntime);

  void tableChanged(TableRuntime tableRuntime, TableConfiguration oldConfig);
}

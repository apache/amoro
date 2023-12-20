package com.netease.arctic.ams.api;

import com.netease.arctic.ams.api.config.TableConfiguration;
import com.netease.arctic.ams.api.process.OptimizingState;
import com.netease.arctic.ams.api.process.TableState;

import java.util.List;

public interface TableRuntime {

  List<OptimizingState> getOptimizingStates();

  List<TableState> getArbitraryStates();

  ServerTableIdentifier getTableIdentifier();

  TableConfiguration getTableConfiguration();

  int getMaxExecuteRetryCount();
}

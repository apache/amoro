package com.netease.arctic.ams.api.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.ServerTableIdentifier;

public class OptimizingState extends TableState {
  public OptimizingState(Action action, ServerTableIdentifier tableIdentifier) {
    super(action, tableIdentifier);
  }

  public OptimizingState(long id, Action action, ServerTableIdentifier tableIdentifier) {
    super(id, action, tableIdentifier);
  }
}

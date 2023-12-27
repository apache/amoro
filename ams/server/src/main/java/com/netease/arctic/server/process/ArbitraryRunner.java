package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.TableRuntime;
import com.netease.arctic.ams.api.process.AmoroProcess;
import com.netease.arctic.ams.api.process.ProcessFactory;
import com.netease.arctic.ams.api.process.TableState;

public class ArbitraryRunner extends SingletonActionRunner<TableState> {

  public ArbitraryRunner(
      TableRuntime tableRuntime,
      ProcessFactory<TableState> processFactory,
      Action action,
      boolean recover) {
    super(tableRuntime, processFactory, action, recover);
  }

  /** recoverProcesses nothing for right now. */
  protected void recoverProcesses() {}

  @Override
  protected void handleCompleted(AmoroProcess<TableState> process) {}

  @Override
  protected void handleSubmitted(AmoroProcess<TableState> process) {}
}

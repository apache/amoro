package com.netease.arctic.ams.api.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.TableRuntime;

/**
 * A factory to create a process.
 * Normally, There will be default ProcessFactories for each action and used by default scheduler.
 * Meanwhile, user could extend external ProcessFactory to run jobs on external resources like Yarn.
 */
public interface ProcessFactory<T extends ProcessState> {

  /**
   * Create a process for the action.
   * @param tableRuntime table runtime
   * @param action action type
   * @return target process which has not been submitted yet.
   */
  AmoroProcess<T> create(TableRuntime tableRuntime, Action action);

  /**
   * Recover a process for the action from a state.
   * @param tableRuntime table runtime
   * @param action action type
   * @param state state of the process
   * @return target process which has not been submitted yet.
   */
  AmoroProcess<T> recover(TableRuntime tableRuntime, Action action, T state);
}

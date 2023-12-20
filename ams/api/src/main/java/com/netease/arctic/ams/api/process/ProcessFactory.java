package com.netease.arctic.ams.api.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.TableRuntime;

public interface ProcessFactory<T extends ProcessState> {

    AmoroProcess<T> create(TableRuntime tableRuntime, Action action);

    AmoroProcess<T> recover(TableRuntime tableRuntime, Action action, T state);
}

package com.netease.arctic.server.table;

import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;

public abstract class RuntimeHandlerChain {

  private RuntimeHandlerChain next;

  protected void appendNext(RuntimeHandlerChain handler) {
    Preconditions.checkNotNull(handler);
    Preconditions.checkArgument(!Objects.equals(handler, this),
        "Cannot add the same runtime handler:{} twice", handler.getClass().getSimpleName());
    if (next == null) {
      next = handler;
    } else {
      next.appendNext(handler);
    }
  }

  public final void startHandler(List<TableRuntimeMeta> tableRuntimeMetaList) {
    initHandler(tableRuntimeMetaList);
    if (next != null) {
      next.startHandler(tableRuntimeMetaList);
    }
  }

  public final void fireStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
    handleStatusChanged(tableRuntime, originalStatus);
    if (next != null) {
      next.fireStatusChanged(tableRuntime, originalStatus);
    }
  }

  public final void fireConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    handleConfigChanged(tableRuntime, originalConfig);
    if (next != null) {
      next.fireConfigChanged(tableRuntime, originalConfig);
    }
  }

  public final void fireTableAdded(ArcticTable table, TableRuntime tableRuntime) {
    handleTableAdded(table, tableRuntime);
    if (next != null) {
      next.fireTableAdded(table, tableRuntime);
    }
  }

  public final void fireTableRemoved(TableRuntime tableRuntime) {
    if (next != null) {
      next.fireTableRemoved(tableRuntime);
    }
    handleTableRemoved(tableRuntime);
  }

  public final void dispose() {
    if (next != null) {
      next.doDispose();
    }
    doDispose();
  }

  protected abstract void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus);

  protected abstract void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig);

  protected abstract void handleTableAdded(ArcticTable table, TableRuntime tableRuntime);

  protected abstract void handleTableRemoved(TableRuntime tableRuntime);

  protected abstract void initHandler(List<TableRuntimeMeta> tableRuntimeMetaList);

  protected abstract void doDispose();
}

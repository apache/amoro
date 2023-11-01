package com.netease.arctic.server.table;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class RuntimeHandlerChain {

  private static final Logger LOG = LoggerFactory.getLogger(RuntimeHandlerChain.class);

  private RuntimeHandlerChain next;

  private boolean initialized;

  protected void appendNext(RuntimeHandlerChain handler) {
    Preconditions.checkNotNull(handler);
    Preconditions.checkArgument(
        !Objects.equals(handler, this),
        "Cannot add the same runtime handler:{} twice",
        handler.getClass().getSimpleName());
    if (next == null) {
      next = handler;
    } else {
      next.appendNext(handler);
    }
  }

  public final void initialize(List<TableRuntimeMeta> tableRuntimeMetaList) {
    List<TableRuntimeMeta> supportedtableRuntimeMetaList =
        tableRuntimeMetaList.stream()
            .filter(
                tableRuntimeMeta -> formatSupported(tableRuntimeMeta.getTableRuntime().getFormat()))
            .collect(Collectors.toList());
    initHandler(supportedtableRuntimeMetaList);
    initialized = true;
    if (next != null) {
      next.initialize(tableRuntimeMetaList);
    }
  }

  public final void fireStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
    if (!initialized) {
      return;
    }
    if (formatSupported(tableRuntime.getFormat())) {
      doSilently(() -> handleStatusChanged(tableRuntime, originalStatus));
    }
    if (next != null) {
      next.fireStatusChanged(tableRuntime, originalStatus);
    }
  }

  public final void fireConfigChanged(
      TableRuntime tableRuntime, TableConfiguration originalConfig) {
    if (!initialized) {
      return;
    }

    if (formatSupported(tableRuntime.getFormat())) {
      doSilently(() -> handleConfigChanged(tableRuntime, originalConfig));
    }
    if (next != null) {
      next.fireConfigChanged(tableRuntime, originalConfig);
    }
  }

  public final void fireTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
    if (!initialized) {
      return;
    }

    if (formatSupported(tableRuntime.getFormat())) {
      doSilently(() -> handleTableAdded(table, tableRuntime));
    }
    if (next != null) {
      next.fireTableAdded(table, tableRuntime);
    }
  }

  public final void fireTableRemoved(TableRuntime tableRuntime) {
    if (!initialized) {
      return;
    }

    if (next != null) {
      next.fireTableRemoved(tableRuntime);
    }

    if (formatSupported(tableRuntime.getFormat())) {
      doSilently(() -> handleTableRemoved(tableRuntime));
    }
  }

  public final void dispose() {
    if (next != null) {
      next.dispose();
    }
    doSilently(this::doDispose);
  }

  private void doSilently(Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable t) {
      LOG.error("failed to handle, ignore and continue", t);
    }
  }

  // Currently, paimon is unsupported
  protected boolean formatSupported(TableFormat format) {
    return format.in(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE);
  }

  protected abstract void handleStatusChanged(
      TableRuntime tableRuntime, OptimizingStatus originalStatus);

  protected abstract void handleConfigChanged(
      TableRuntime tableRuntime, TableConfiguration originalConfig);

  protected abstract void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime);

  protected abstract void handleTableRemoved(TableRuntime tableRuntime);

  protected abstract void initHandler(List<TableRuntimeMeta> tableRuntimeMetaList);

  protected abstract void doDispose();
}

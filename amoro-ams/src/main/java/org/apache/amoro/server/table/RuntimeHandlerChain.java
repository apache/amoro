/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.table;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
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

  public final void initialize(List<TableRuntime> tableRuntimes) {
    List<TableRuntime> supportedtableRuntimeList =
        tableRuntimes.stream()
            .filter(runtime -> formatSupported(runtime.getFormat()))
            .collect(Collectors.toList());
    initHandler(supportedtableRuntimeList);
    initialized = true;
    if (next != null) {
      next.initialize(tableRuntimes);
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

  protected abstract void initHandler(List<TableRuntime> tableRuntimeList);

  protected abstract void doDispose();
}

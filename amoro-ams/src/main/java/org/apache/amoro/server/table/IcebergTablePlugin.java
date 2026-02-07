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
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.TableRuntimeFactory;
import org.apache.amoro.table.TableRuntimeStore;

import javax.annotation.Nullable;

import java.util.List;

public class IcebergTablePlugin implements TableRuntimePlugin, TableRuntimeHandler {

  private final RuntimeHandlerChain headHandler;

  private IcebergTablePlugin(RuntimeHandlerChain headHandler) {
    Preconditions.checkNotNull(headHandler);
    this.headHandler = headHandler;
  }

  @Override
  public boolean accept(ServerTableIdentifier tableIdentifier) {
    return tableIdentifier.getFormat() == TableFormat.ICEBERG
        || tableIdentifier.getFormat() == TableFormat.MIXED_HIVE
        || tableIdentifier.getFormat() == TableFormat.MIXED_ICEBERG;
  }

  @Override
  public TableRuntime createTableRuntime(
      TableRuntimeFactory.Creator creator, TableRuntimeStore store) {
    if (!(store instanceof DefaultTableRuntimeStore)) {
      throw new IllegalStateException("Only support DefaultTableRuntimeStore");
    }
    DefaultTableRuntimeStore icebergRuntimeStore = (DefaultTableRuntimeStore) store;
    icebergRuntimeStore.setRuntimeHandler(this);
    return creator.create(store);
  }

  @Override
  public void initialize(List<TableRuntime> tableRuntimes) {
    if (headHandler != null) {
      headHandler.initialize(tableRuntimes);
    }
  }

  @Override
  public void onTableCreated(@Nullable AmoroTable<?> amoroTable, TableRuntime tableRuntime) {
    if (headHandler != null) {
      headHandler.fireTableAdded(amoroTable, tableRuntime);
    }
  }

  @Override
  public void onTableDropped(TableRuntime tableRuntime) {
    if (headHandler != null) {
      headHandler.fireTableRemoved(tableRuntime);
    }
  }

  @Override
  public void dispose() {
    if (headHandler != null) {
      headHandler.dispose();
    }
  }

  public void handleTableChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
    if (headHandler != null) {
      headHandler.fireStatusChanged(tableRuntime, originalStatus);
    }
  }

  public void handleTableChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    if (headHandler != null) {
      headHandler.fireConfigChanged(tableRuntime, originalConfig);
    }
  }

  public static IcebergTablePluginBuilder builder() {
    return new IcebergTablePluginBuilder();
  }

  public static class IcebergTablePluginBuilder {
    private RuntimeHandlerChain headHandler;

    private IcebergTablePluginBuilder() {}

    public IcebergTablePluginBuilder addHandler(RuntimeHandlerChain handler) {
      if (handler == null) {
        return this;
      }
      if (headHandler == null) {
        headHandler = handler;
      } else {
        headHandler.appendNext(handler);
      }
      return this;
    }

    public IcebergTablePlugin build() {
      return new IcebergTablePlugin(headHandler);
    }
  }
}

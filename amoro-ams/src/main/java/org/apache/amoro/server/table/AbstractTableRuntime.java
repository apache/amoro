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

import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.amoro.table.TableRuntimeStore;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class AbstractTableRuntime extends PersistentBase implements TableRuntime {

  private final TableRuntimeStore store;
  private final Map<Action, TableProcessContainer> processContainerMap = Maps.newConcurrentMap();

  protected AbstractTableRuntime(TableRuntimeStore store) {
    this.store = store;
  }

  public TableRuntimeStore store() {
    return store;
  }

  @Override
  public ServerTableIdentifier getTableIdentifier() {
    return store().getTableIdentifier();
  }

  @Override
  public TableConfiguration getTableConfiguration() {
    return TableConfigurations.parseTableConfig(store().getTableConfig());
  }

  @Override
  public List<TableProcessStore> getProcessStates() {
    return processContainerMap.values().stream()
        .flatMap(container -> container.getProcessStates().stream())
        .collect(Collectors.toList());
  }

  @Override
  public List<TableProcessStore> getProcessStates(Action action) {
    return processContainerMap.get(action).getProcessStates();
  }

  @Override
  public void registerProcess(TableProcessStore processStore) {
    processContainerMap
        .computeIfAbsent(processStore.getAction(), k -> new TableProcessContainer())
        .processLock
        .lock();
    try {
      processContainerMap
          .get(processStore.getAction())
          .processMap
          .put(processStore.getProcessId(), processStore);
    } finally {
      processContainerMap.get(processStore.getAction()).processLock.unlock();
    }
  }

  @Override
  public void removeProcess(TableProcessStore processStore) {
    processContainerMap.computeIfPresent(
        processStore.getAction(),
        (action, container) -> {
          container.processMap.remove(processStore.getProcessId());
          return container;
        });
  }

  @Override
  public String getGroupName() {
    return store().getGroupName();
  }

  public int getStatusCode() {
    return store().getStatusCode();
  }

  @Override
  public void dispose() {
    store().dispose();
  }

  private static class TableProcessContainer {
    private final Lock processLock = new ReentrantLock();
    private final Map<Long, TableProcessStore> processMap = Maps.newConcurrentMap();

    public List<TableProcessStore> getProcessStates() {
      return Lists.newArrayList(processMap.values());
    }
  }
}

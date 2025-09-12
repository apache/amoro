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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.exception.AmoroRuntimeException;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.NestedSqlSession;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.TableRuntimeState;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeStore;
import org.apache.amoro.table.TableSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Default table runtime store implementation. */
public class DefaultTableRuntimeStore extends PersistentBase implements TableRuntimeStore {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultTableRuntimeStore.class);
  private final Lock tableLock = new ReentrantLock();
  private final ServerTableIdentifier tableIdentifier;
  private final TableRuntimeMeta meta;
  private final Map<String, TableRuntimeState> states = new ConcurrentHashMap<>();

  private TableRuntimeHandler runtimeHandler;
  private TableRuntime tableRuntime;

  public DefaultTableRuntimeStore(
      ServerTableIdentifier tableIdentifier,
      TableRuntimeMeta meta,
      List<StateKey<?>> requiredStates,
      List<TableRuntimeState> restoredStates) {
    Preconditions.checkNotNull(tableIdentifier, "ServerTableIdentifier must not be null.");
    Preconditions.checkNotNull(meta, "TableRuntimeMeta must not be null.");
    Preconditions.checkNotNull(requiredStates, "requiredStates must not be null.");
    Preconditions.checkNotNull(restoredStates, "restoredStates must not be null.");
    this.tableIdentifier = tableIdentifier;
    this.meta = meta;
    restoreStates(requiredStates, restoredStates);
  }

  public void setTableRuntime(TableRuntime tableRuntime) {
    this.tableRuntime = tableRuntime;
  }

  public void setRuntimeHandler(TableRuntimeHandler runtimeHandler) {
    this.runtimeHandler = runtimeHandler;
  }

  @Override
  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  @Override
  public String getGroupName() {
    return this.meta.getGroupName();
  }

  @Override
  public Map<String, String> getTableConfig() {
    return this.meta.getTableConfig();
  }

  @Override
  public int getStatusCode() {
    return this.meta.getStatusCode();
  }

  @Override
  public <T> T getState(StateKey<T> key) {
    Preconditions.checkNotNull(key, "TableRuntime state key cannot be null");
    Preconditions.checkNotNull(
        states.containsKey(key.getKey()), "TableRuntime state %s not initialized", key);
    TableRuntimeState tableState = states.get(key.getKey());
    return key.deserialize(tableState.getStateValue());
  }

  @Override
  public void synchronizedInvoke(Runnable runnable) {
    tableLock.lock();
    try {
      runnable.run();
    } finally {
      tableLock.unlock();
    }
  }

  @Override
  public void dispose() {
    doAsTransaction(
        () -> doAs(TableRuntimeMapper.class, m -> m.deleteRuntime(tableIdentifier.getId())),
        () -> doAs(TableRuntimeMapper.class, m -> m.removeAllTableStates(tableIdentifier.getId())),
        () -> doAs(TableMetaMapper.class, m -> m.deleteTableIdById(tableIdentifier.getId())));
  }

  @Override
  public TableRuntimeOperation begin() {
    return new TableRuntimeOperationImpl();
  }

  protected void restoreStates(
      List<StateKey<?>> requiredStates, List<TableRuntimeState> restoredStates) {
    Map<String, TableRuntimeState> stateMap =
        restoredStates.stream()
            .collect(Collectors.toMap(TableRuntimeState::getStateKey, Function.identity()));

    requiredStates.forEach(
        key -> {
          if (stateMap.containsKey(key.getKey())) {
            states.put(key.getKey(), stateMap.get(key.getKey()));
            stateMap.remove(key.getKey());
          } else {
            doAs(
                TableRuntimeMapper.class,
                mapper ->
                    mapper.saveState(
                        tableIdentifier.getId(), key.getKey(), key.serializeDefault()));
            TableRuntimeState state =
                getAs(
                    TableRuntimeMapper.class,
                    m -> m.getState(tableIdentifier.getId(), key.getKey()));
            Preconditions.checkNotNull(state, "State %s initialize failed", key.getKey());
            states.put(key.getKey(), state);
          }
        });

    if (!stateMap.isEmpty()) {
      LOG.warn("Found {} useless runtime states for {}", stateMap.size(), tableIdentifier);
      stateMap.forEach(
          (k, s) -> {
            LOG.warn("Remove useless runtime state {} for {}", k, tableIdentifier);
            doAs(TableRuntimeMapper.class, m -> m.removeState(s.getStateId()));
          });
    }
  }

  private class TableRuntimeOperationImpl implements TableRuntimeOperation {

    private final TableRuntimeMeta oldMeta;
    private final List<Runnable> operations = Lists.newArrayList();
    private final Set<String> stateKeys = Sets.newHashSet();
    private final Map<String, String> oldStates = new ConcurrentHashMap<>();
    private boolean metaOperation = false;

    private final List<Consumer<TableRuntimeHandler>> handlerCallback = Lists.newArrayList();

    public TableRuntimeOperationImpl() {
      this.oldMeta = meta.copy();
      states.forEach((k, v) -> oldStates.put(k, v.getStateValue()));
    }

    @Override
    public <T> TableRuntimeOperation updateState(StateKey<T> key, Function<T, T> updater) {
      stateKeys.add(key.getKey());
      operations.add(
          () -> {
            Preconditions.checkNotNull(key, "TableRuntime state key cannot be null");
            Preconditions.checkNotNull(
                oldStates.containsKey(key.getKey()), "TableRuntime state %s not initialized", key);
            T oldValue = key.deserialize(oldStates.get(key.getKey()));
            T newValue = updater.apply(oldValue);
            String storedValue = key.serialize(newValue);
            oldStates.put(key.getKey(), storedValue);
          });
      return this;
    }

    @Override
    public TableRuntimeOperation updateGroup(Function<String, String> updater) {
      operations.add(
          () -> {
            String newGroupName = updater.apply(oldMeta.getGroupName());
            oldMeta.setGroupName(newGroupName);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public TableRuntimeOperation updateStatusCode(Function<Integer, Integer> updater) {
      operations.add(
          () -> {
            Integer newStatusCode = updater.apply(oldMeta.getStatusCode());
            oldMeta.setStatusCode(newStatusCode);
          });
      OptimizingStatus status = OptimizingStatus.ofCode(oldMeta.getStatusCode());
      handlerCallback.add(handler -> handler.handleTableChanged(tableRuntime, status));
      metaOperation = true;
      return this;
    }

    @Override
    public TableRuntimeOperation updateTableConfig(Consumer<Map<String, String>> updater) {
      operations.add(
          () -> {
            Map<String, String> tableConfig = oldMeta.getTableConfig();
            updater.accept(tableConfig);
            oldMeta.setTableConfig(tableConfig);
          });
      TableConfiguration oldConfiguration =
          TableConfigurations.parseTableConfig(oldMeta.getTableConfig());
      handlerCallback.add(handler -> handler.handleTableChanged(tableRuntime, oldConfiguration));
      metaOperation = true;
      return this;
    }

    @Override
    public TableRuntimeOperation updateTableSummary(Consumer<TableSummary> updater) {
      operations.add(
          () -> {
            TableSummary summary = oldMeta.getTableSummary();
            updater.accept(summary);
            oldMeta.setTableSummary(summary);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public void commit() {
      try (NestedSqlSession session = beginSession()) {
        try {
          tableLock.lock();
          operations.forEach(Runnable::run);
          persist();
          session.commit();
        } catch (Throwable t) {
          LOG.error("failed to commit operations", t);
          session.rollback();
          throw AmoroRuntimeException.wrap(t, PersistenceException::new);
        } finally {
          tableLock.unlock();
        }
        visitable();
      }
    }

    private void persist() {
      doAs(
          TableRuntimeMapper.class,
          mapper -> {
            if (metaOperation) {
              mapper.updateRuntime(oldMeta);
            }
            stateKeys.forEach(
                key -> {
                  String storedValue = oldStates.get(key);
                  mapper.setStateValue(tableIdentifier.getId(), key, storedValue);
                });
          });
    }

    private void visitable() {
      if (metaOperation) {
        meta.setGroupName(oldMeta.getGroupName());
        meta.setStatusCode(oldMeta.getStatusCode());
        meta.setStatusCodeUpdateTime(oldMeta.getStatusCodeUpdateTime());
        meta.setTableSummary(oldMeta.getTableSummary());
        meta.setTableConfig(oldMeta.getTableConfig());
      }
      stateKeys.forEach(
          key -> {
            TableRuntimeState state = states.get(key);
            state.setStateValue(oldStates.get(key));
          });

      if (runtimeHandler != null && tableRuntime != null) {
        handlerCallback.forEach(c -> c.accept(runtimeHandler));
      }
    }
  }
}

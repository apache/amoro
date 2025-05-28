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
import org.apache.amoro.SupportsProcessPlugins;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.AmoroProcess;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.persistence.StatedPersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.shaded.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DefaultTableRuntime extends StatedPersistentBase
    implements TableRuntime, SupportsProcessPlugins {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTableRuntime.class);

  private final ServerTableIdentifier tableIdentifier;
  private final DefaultOptimizingState optimizingState;
  private final Map<Action, TableProcessContainer> processContainerMap = Maps.newConcurrentMap();

  public DefaultTableRuntime(
      ServerTableIdentifier tableIdentifier,
      TableRuntimeHandler tableHandler,
      Map<String, String> properties) {
    Preconditions.checkNotNull(tableIdentifier, "ServerTableIdentifier must not be null.");
    Preconditions.checkNotNull(tableHandler, "TableRuntimeHandler must not be null.");
    this.tableIdentifier = tableIdentifier;
    this.optimizingState = new DefaultOptimizingState(this, tableHandler, properties);
  }

  public DefaultTableRuntime(TableRuntimeMeta tableRuntimeMeta, TableRuntimeHandler tableHandler) {
    Preconditions.checkNotNull(tableRuntimeMeta, "TableRuntimeMeta must not be null.");
    Preconditions.checkNotNull(tableHandler, "TableRuntimeHandler must not be null.");

    this.tableIdentifier =
        ServerTableIdentifier.of(
            tableRuntimeMeta.getTableId(),
            tableRuntimeMeta.getCatalogName(),
            tableRuntimeMeta.getDbName(),
            tableRuntimeMeta.getTableName(),
            tableRuntimeMeta.getFormat());
    this.optimizingState = new DefaultOptimizingState(this, tableRuntimeMeta, tableHandler);
  }

  public DefaultOptimizingState getOptimizingState() {
    return optimizingState;
  }

  public void recover(OptimizingProcess optimizingProcess) {
    optimizingState.recover(optimizingProcess);
  }

  public void registerMetric(MetricRegistry metricRegistry) {
    optimizingState.registerMetric(metricRegistry);
  }

  public void dispose() {
    optimizingState.dispose();
  }

  @Override
  public AmoroProcess<? extends TableProcessState> trigger(Action action) {
    return Optional.ofNullable(processContainerMap.get(action))
        .map(container -> container.trigger(action))
        // Define a related exception
        .orElseThrow(() -> new IllegalArgumentException("No ProcessFactory for action " + action));
  }

  @Override
  public void install(Action action, ProcessFactory<? extends TableProcessState> processFactory) {
    if (processContainerMap.putIfAbsent(action, new TableProcessContainer(processFactory))
        != null) {
      throw new IllegalStateException("ProcessFactory for action " + action + " already exists");
    }
  }

  @Override
  public boolean enabled(Action action) {
    return processContainerMap.get(action) != null;
  }

  @Override
  public List<TableProcessState> getProcessStates() {
    return processContainerMap.values().stream()
        .flatMap(container -> container.getProcessStates().stream())
        .collect(Collectors.toList());
  }

  @Override
  public List<TableProcessState> getProcessStates(Action action) {
    return processContainerMap.get(action).getProcessStates();
  }

  @Override
  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  @Override
  public TableConfiguration getTableConfiguration() {
    return optimizingState.getTableConfiguration();
  }

  private class TableProcessContainer {
    private final Lock processLock = new ReentrantLock();
    private final ProcessFactory<? extends TableProcessState> processFactory;
    private final Map<Long, AmoroProcess<? extends TableProcessState>> processMap =
        Maps.newConcurrentMap();

    TableProcessContainer(ProcessFactory<? extends TableProcessState> processFactory) {
      this.processFactory = processFactory;
    }

    public AmoroProcess<? extends TableProcessState> trigger(Action action) {
      processLock.lock();
      try {
        AmoroProcess<? extends TableProcessState> process =
            processFactory.create(DefaultTableRuntime.this, action);
        process.getCompleteFuture().whenCompleted(() -> processMap.remove(process.getId()));
        processMap.put(process.getId(), process);
        return process;
      } finally {
        processLock.unlock();
      }
    }

    public List<TableProcessState> getProcessStates() {
      return processMap.values().stream().map(AmoroProcess::getState).collect(Collectors.toList());
    }
  }
}

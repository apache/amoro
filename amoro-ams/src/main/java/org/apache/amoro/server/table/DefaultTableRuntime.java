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
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.AmoroProcess;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.persistence.StatedPersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DefaultTableRuntime extends StatedPersistentBase implements SupportsProcessPlugins {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTableRuntime.class);

  private final Lock processLock = new ReentrantLock();
  private final ServerTableIdentifier tableIdentifier;
  private final DefaultOptimizingState optimizingState;
  private final Map<Action, ProcessFactory<? extends TableProcessState>> processFactoryMap =
      new HashMap<>();
  private final Map<Long, AmoroProcess<? extends TableProcessState>> processMap = new HashMap<>();

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
    processLock.lock();
    try {
      AmoroProcess<? extends TableProcessState> process =
          Optional.ofNullable(processFactoryMap.get(action))
              .map(factory -> factory.create(this, action))
              // Define a related exception
              .orElseThrow(
                  () -> new IllegalArgumentException("No ProcessFactory for action " + action));
      processMap.put(process.getId(), process);
      process.getCompleteFuture().whenCompleted(() -> processMap.remove(process.getId()));
      return process;
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public void install(Action action, ProcessFactory<? extends TableProcessState> processFactory) {
    processLock.lock();
    try {
      if (processFactoryMap.containsKey(action)) {
        throw new IllegalStateException("ProcessFactory for action " + action + " already exists");
      }
      processFactoryMap.put(action, processFactory);
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public boolean enabled(Action action) {
    return processFactoryMap.get(action) != null;
  }

  @Override
  public List<TableProcessState> getProcessStates() {
    processLock.lock();
    try {
      return processMap.values().stream().map(AmoroProcess::getState).collect(Collectors.toList());
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  @Override
  public TableConfiguration getTableConfiguration() {
    return optimizingState.getTableConfiguration();
  }
}

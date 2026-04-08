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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.Action;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * ActionCoordinator for the optimizing action. Loads ProcessFactory implementations via SPI and
 * delegates table process creation/recovery to the appropriate factory based on table format.
 */
public class OptimizingActionCoordinator implements ActionCoordinator {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingActionCoordinator.class);

  public static final Action OPTIMIZING_ACTION = Action.register("OPTIMIZING");

  private final Map<TableFormat, ProcessFactory> processFactories = new HashMap<>();
  private int maxPlanningParallelism = 1;

  public OptimizingActionCoordinator() {}

  public OptimizingActionCoordinator(int maxPlanningParallelism) {
    this.maxPlanningParallelism = maxPlanningParallelism;
  }

  @Override
  public String name() {
    return "optimizing-coordinator";
  }

  @Override
  public Action action() {
    return OPTIMIZING_ACTION;
  }

  @Override
  public boolean formatSupported(TableFormat format) {
    return processFactories.containsKey(format);
  }

  @Override
  public int parallelism() {
    return maxPlanningParallelism;
  }

  @Override
  public long getNextExecutingTime(TableRuntime tableRuntime) {
    DefaultTableRuntime dtr = (DefaultTableRuntime) tableRuntime;
    long minInterval = dtr.getOptimizingConfig().getMinPlanInterval();
    long lastPlanTime = dtr.getLastPlanTime();
    return lastPlanTime + minInterval;
  }

  @Override
  public boolean enabled(TableRuntime tableRuntime) {
    DefaultTableRuntime dtr = (DefaultTableRuntime) tableRuntime;
    return dtr.getOptimizingConfig().isEnabled()
        && !dtr.getOptimizingStatus().isProcessing()
        && formatSupported(dtr.getFormat());
  }

  @Override
  public long getExecutorDelay() {
    // Default polling interval for optimizer threads
    return 1000L;
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime) {
    ProcessFactory factory = processFactories.get(tableRuntime.getFormat());
    if (factory == null) {
      LOG.warn(
          "No ProcessFactory registered for table format {}, skipping trigger for table {}",
          tableRuntime.getFormat(),
          tableRuntime.getTableIdentifier());
      return Optional.empty();
    }
    return factory.trigger(tableRuntime, OPTIMIZING_ACTION);
  }

  @Override
  public TableProcess recoverTableProcess(
      TableRuntime tableRuntime, TableProcessStore processStore) {
    ProcessFactory factory = processFactories.get(tableRuntime.getFormat());
    if (factory == null) {
      throw new IllegalStateException(
          "No ProcessFactory registered for table format " + tableRuntime.getFormat());
    }
    try {
      return factory.recover(tableRuntime, processStore);
    } catch (RecoverProcessFailedException e) {
      throw new RuntimeException(
          "Failed to recover optimizing process for table " + tableRuntime.getTableIdentifier(), e);
    }
  }

  @Override
  public void open(Map<String, String> properties) {
    ServiceLoader<ProcessFactory> loader = ServiceLoader.load(ProcessFactory.class);
    for (ProcessFactory factory : loader) {
      try {
        factory.open(properties);
        registerFactory(factory);
      } catch (Exception e) {
        LOG.error("Failed to open ProcessFactory: {}", factory.name(), e);
      }
    }
    LOG.info(
        "OptimizingActionCoordinator opened with {} format factories: {}",
        processFactories.size(),
        processFactories.keySet());
  }

  @Override
  public void close() {
    for (ProcessFactory factory : processFactories.values()) {
      try {
        factory.close();
      } catch (Exception e) {
        LOG.warn("Failed to close ProcessFactory: {}", factory.name(), e);
      }
    }
    processFactories.clear();
  }

  /**
   * Register a ProcessFactory manually (useful for testing or programmatic configuration). The
   * factory is indexed by all formats it supports via supportedActions().
   */
  public void registerFactory(ProcessFactory factory) {
    Map<TableFormat, java.util.Set<Action>> supported = factory.supportedActions();
    for (Map.Entry<TableFormat, java.util.Set<Action>> entry : supported.entrySet()) {
      if (entry.getValue().contains(OPTIMIZING_ACTION)) {
        processFactories.put(entry.getKey(), factory);
        LOG.info("Registered ProcessFactory '{}' for format {}", factory.name(), entry.getKey());
      }
    }
  }

  public void setMaxPlanningParallelism(int maxPlanningParallelism) {
    this.maxPlanningParallelism = maxPlanningParallelism;
  }
}

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
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;

import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link ActionCoordinator} that bridges {@link ProcessFactory}
 * declarations to the AMS scheduling framework.
 */
public class DefaultActionCoordinator implements ActionCoordinator {

  private final Action action;
  private final TableFormat format;
  private final ProcessFactory factory;
  private final ProcessTriggerStrategy strategy;

  public DefaultActionCoordinator(TableFormat format, Action action, ProcessFactory factory) {
    this.action = action;
    this.format = format;
    this.factory = factory;
    this.strategy = factory.triggerStrategy(format, action);
    Preconditions.checkArgument(
        strategy != null,
        "ProcessTriggerStrategy cannot be null for format %s, action %s, factory %s",
        format,
        action,
        factory.name());
  }

  @Override
  public String name() {
    // No need to be globally unique, this coordinator is not discovered via plugin manager.
    return String.format("%s-%s-coordinator", format.name().toLowerCase(), action.getName());
  }

  @Override
  public void open(Map<String, String> properties) {
    // No-op: lifecycle is managed by owning TableRuntimeFactory.
  }

  @Override
  public void close() {
    // No-op: nothing to close.
  }

  @Override
  public boolean formatSupported(TableFormat format) {
    return this.format.equals(format);
  }

  @Override
  public int parallelism() {
    return strategy.getTriggerParallelism();
  }

  @Override
  public Action action() {
    return action;
  }

  @Override
  public long getNextExecutingTime(TableRuntime tableRuntime) {
    // Fixed-rate scheduling based on configured trigger interval.
    return strategy.getTriggerInterval().toMillis();
  }

  @Override
  public boolean enabled(TableRuntime tableRuntime) {
    return formatSupported(tableRuntime.getFormat());
  }

  @Override
  public long getExecutorDelay() {
    return strategy.getTriggerInterval().toMillis();
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime) {
    return factory.trigger(tableRuntime, action);
  }

  @Override
  public TableProcess recoverTableProcess(
      TableRuntime tableRuntime, TableProcessStore processStore) {
    try {
      return factory.recover(tableRuntime, processStore);
    } catch (RecoverProcessFailedException e) {
      throw new IllegalStateException(
          String.format(
              "Failed to recover table process for format %s, action %s, table %s",
              format, action, tableRuntime.getTableIdentifier()),
          e);
    }
  }
}

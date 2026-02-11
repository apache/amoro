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
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;

import java.util.Optional;

public class DefaultActionCoordinator implements ActionCoordinator {

  private final Action action;
  private final ProcessFactory factory;
  private final TableFormat format;
  private final ProcessTriggerStrategy strategy;

  public DefaultActionCoordinator(TableFormat format, Action action, ProcessFactory factory) {
    this.action = action;
    this.factory = factory;
    this.format = format;
    this.strategy = factory.triggerStrategy(format, action);
    Preconditions.checkArgument(
        strategy != null, "ProcessTriggerStrategy cannot be null for %s: %s", format, action);
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
    return factory.recover(tableRuntime, processStore);
  }
}

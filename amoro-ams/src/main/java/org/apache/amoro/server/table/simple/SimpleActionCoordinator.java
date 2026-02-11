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

package org.apache.amoro.server.table.simple;

import org.apache.amoro.Action;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

public class SimpleActionCoordinator implements ActionCoordinator {

  private static final String PARALLELISM = "parallelism";
  private static final int PARALLELISM_DEFAULT = 1;
  private static final String CHECK_INTERVAL = "check-interval";
  private static final long CHECK_INTERVAL_DEFAULT = Duration.ofMinutes(5).toMillis();

  private final Action action;
  private final ProcessFactory factory;
  private final Set<TableFormat> supportedFormats;

  private final int parallelism;
  private final long checkInterval;

  public SimpleActionCoordinator(
      Action action,
      ProcessFactory factory,
      Set<TableFormat> supportedFormats,
      Map<String, String> configuration) {
    this.action = action;
    this.factory = factory;
    this.supportedFormats = supportedFormats;
    this.parallelism =
        Integer.parseInt(
            configuration.getOrDefault(PARALLELISM, Integer.toString(PARALLELISM_DEFAULT)));
    this.checkInterval =
        Long.parseLong(
            configuration.getOrDefault(CHECK_INTERVAL, Long.toString(CHECK_INTERVAL_DEFAULT)));
  }

  @Override
  public boolean formatSupported(TableFormat format) {
    return supportedFormats.contains(format);
  }

  @Override
  public int parallelism() {
    return parallelism;
  }

  @Override
  public Action action() {
    return action;
  }

  @Override
  public long getNextExecutingTime(TableRuntime tableRuntime) {
    return checkInterval;
  }

  @Override
  public boolean enabled(TableRuntime tableRuntime) {
    return formatSupported(tableRuntime.getFormat());
  }

  @Override
  public long getExecutorDelay() {
    return checkInterval;
  }

  @Override
  public boolean isReady(TableRuntime tableRuntime) {
    return factory.readyForAction(tableRuntime, action);
  }

  @Override
  public TableProcess createTableProcess(TableRuntime tableRuntime) {
    return factory.create(tableRuntime, action);
  }

  @Override
  public TableProcess recoverTableProcess(
      TableRuntime tableRuntime, TableProcessStore processStore) {
    return factory.recover(tableRuntime, processStore);
  }
}

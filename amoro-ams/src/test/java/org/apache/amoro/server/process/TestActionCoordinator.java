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

package org.apache.amoro.server.process;

import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.process.executor.EngineType;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestActionCoordinator implements ActionCoordinator {
  public static final int PROCESS_MAX_POOL_SIZE = 1000;
  private static final TableFormat[] DEFAULT_FORMATS = new TableFormat[] {TableFormat.PAIMON};

  public static final Action DEFAULT_ACTION = new Action(DEFAULT_FORMATS, 0, "default_action");

  private final Map<ServerTableIdentifier, TableProcess> tableProcessMap =
      new ConcurrentHashMap<>();

  @Override
  public boolean formatSupported(TableFormat format) {
    return true;
  }

  @Override
  public int parallelism() {
    return PROCESS_MAX_POOL_SIZE;
  }

  @Override
  public Action action() {
    return DEFAULT_ACTION;
  }

  @Override
  public String executionEngine() {
    return EngineType.DEFAULT.name();
  }

  @Override
  public long getNextExecutingTime(TableRuntime tableRuntime) {
    return 24 * 60 * 60 * 1000L;
  }

  @Override
  public boolean enabled(TableRuntime tableRuntime) {
    return true;
  }

  @Override
  public long getExecutorDelay() {
    return 0L;
  }

  @Override
  public TableProcess createTableProcess(TableRuntime tableRuntime) {
    TableProcessState tableProcessState =
        new TableProcessState(
            SNOWFLAKE_ID_GENERATOR.generateId(),
            action(),
            tableRuntime.getTableIdentifier(),
            executionEngine());
    TableProcessStore tableProcessStore =
        new DefaultTableProcessStore(
            tableProcessState.getId(),
            tableRuntime,
            TableProcessMeta.fromTableProcessState(tableProcessState),
            action());
    TestTableProcess testTableProcess = new TestTableProcess(tableRuntime, tableProcessStore);
    return testTableProcess;
  }

  @Override
  public TableProcess recoverTableProcess(TableRuntime tableRuntime, TableProcessMeta meta) {
    TableProcessStore tableProcessStore =
        new DefaultTableProcessStore(meta.getProcessId(), tableRuntime, meta, action());
    TestTableProcess testTableProcess = new TestTableProcess(tableRuntime, tableProcessStore);
    return testTableProcess;
  }

  @Override
  public TableProcess cancelTableProcess(TableRuntime tableRuntime, TableProcess process) {
    return process;
  }

  @Override
  public TableProcess retryTableProcess(TableProcess process) {
    return process;
  }

  @Override
  public void open(Map<String, String> properties) {}

  @Override
  public void close() {}

  @Override
  public String name() {
    return "default_action_coordinator";
  }

}

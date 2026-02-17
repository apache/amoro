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
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.utils.SnowflakeIdGenerator;

import java.util.HashMap;
import java.util.Optional;

/** Mock implementation of {@link ActionCoordinator} used in tests. */
public class MockActionCoordinator implements ActionCoordinator {
  public static final int PROCESS_MAX_POOL_SIZE = 1000;
  private static final TableFormat[] DEFAULT_FORMATS = new TableFormat[] {TableFormat.PAIMON};
  SnowflakeIdGenerator SNOWFLAKE_ID_GENERATOR = new SnowflakeIdGenerator();

  public static final Action DEFAULT_ACTION = Action.register("default_action");

  /**
   * Whether the format is supported.
   *
   * @param format table format
   * @return always true
   */
  @Override
  public boolean formatSupported(TableFormat format) {
    return true;
  }

  /**
   * Parallelism of mock coordinator.
   *
   * @return pool size
   */
  @Override
  public int parallelism() {
    return PROCESS_MAX_POOL_SIZE;
  }

  /** Get default action. */
  @Override
  public Action action() {
    return DEFAULT_ACTION;
  }

  /** Next executing time. */
  @Override
  public long getNextExecutingTime(TableRuntime tableRuntime) {
    return 24 * 60 * 60 * 1000L;
  }

  /** Always enabled. */
  @Override
  public boolean enabled(TableRuntime tableRuntime) {
    return true;
  }

  /** No delay. */
  @Override
  public long getExecutorDelay() {
    return 0L;
  }

  /**
   * Create a mock table process for runtime.
   *
   * @param tableRuntime table runtime
   * @return mock process
   */
  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime) {
    TableProcessMeta tableProcessMeta =
        TableProcessMeta.of(
            SNOWFLAKE_ID_GENERATOR.generateId(),
            tableRuntime.getTableIdentifier().getId(),
            action().getName(),
            "default",
            new HashMap<>());
    TableProcessStore tableProcessStore =
        new DefaultTableProcessStore(
            tableProcessMeta.getProcessId(), tableRuntime, tableProcessMeta, action(), 3);
    MockTableProcess mockTableProcess = new MockTableProcess(tableRuntime, tableProcessStore);
    return Optional.of(mockTableProcess);
  }

  /**
   * Recover mock process from store.
   *
   * @param tableRuntime table runtime
   * @param processStore store
   * @return mock process
   */
  @Override
  public TableProcess recoverTableProcess(
      TableRuntime tableRuntime, TableProcessStore processStore) {
    return new MockTableProcess(tableRuntime, processStore);
  }
}

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

package org.apache.amoro.process;

import org.apache.amoro.Action;
import org.apache.amoro.ActivePlugin;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;

import java.util.Optional;

/**
 * Coordinator for a specific {@link org.apache.amoro.Action} to manage table processes. Provides
 * scheduling parameters and lifecycle hooks to create/recover/cancel/retry table processes.
 */
public interface ActionCoordinator extends ActivePlugin {

  /**
   * Check whether the given table format is supported by this coordinator.
   *
   * @param format table format
   * @return true if supported, false otherwise
   */
  boolean formatSupported(TableFormat format);

  /**
   * Get the maximum parallelism for scheduling table processes.
   *
   * @return max parallelism
   */
  int parallelism();

  /**
   * Get the {@link Action} managed by this coordinator.
   *
   * @return action type
   */
  Action action();

  /**
   * Calculate the next executing time for the given table runtime.
   *
   * @param tableRuntime table runtime
   * @return next executing timestamp in milliseconds
   */
  long getNextExecutingTime(TableRuntime tableRuntime);

  /**
   * Determine whether scheduling is enabled for the given table runtime.
   *
   * @param tableRuntime table runtime
   * @return true if enabled, false otherwise
   */
  boolean enabled(TableRuntime tableRuntime);

  /**
   * Get the delay (in milliseconds) before executor polls or runs tasks.
   *
   * @return executor delay in milliseconds
   */
  long getExecutorDelay();

  /**
   * Create a new {@link TableProcess} instance for the given table runtime.
   *
   * @param tableRuntime table runtime
   * @return a new table process
   */
  Optional<TableProcess> trigger(TableRuntime tableRuntime);

  /**
   * Recover a {@link TableProcess} from persisted store.
   *
   * @param tableRuntime table runtime
   * @param processStore persisted process store
   * @return recovered table process
   */
  TableProcess recoverTableProcess(TableRuntime tableRuntime, TableProcessStore processStore);
}

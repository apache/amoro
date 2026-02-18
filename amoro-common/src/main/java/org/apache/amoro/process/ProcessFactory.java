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
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.StateKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A factory to create a process. Normally, There will be default ProcessFactories for each action
 * and used by default scheduler. Meanwhile, user could extend external ProcessFactory to run jobs
 * on external resources like Yarn.
 */
public interface ProcessFactory extends ActivePlugin {

  default List<StateKey<?>> requiredStates() {
    return Lists.newArrayList();
  }

  /** Get supported actions for each table format. */
  Map<TableFormat, Set<Action>> supportedActions();

  /** How to trigger a process for the action. */
  ProcessTriggerStrategy triggerStrategy(TableFormat format, Action action);

  /**
   * Try trigger a process for the action.
   *
   * @param tableRuntime table runtime
   * @param action action type
   * @return target process which has not been submitted yet.
   */
  Optional<TableProcess> trigger(TableRuntime tableRuntime, Action action);

  /**
   * Recover a process for the action from a state.
   *
   * @param tableRuntime table runtime
   * @param state state of the process
   * @return target process which has not been submitted yet.
   */
  TableProcess recover(TableRuntime tableRuntime, TableProcessStore store);
}

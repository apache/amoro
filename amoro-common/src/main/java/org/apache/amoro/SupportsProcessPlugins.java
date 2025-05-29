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

package org.apache.amoro;

import org.apache.amoro.process.AmoroProcess;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.TableProcessState;

public interface SupportsProcessPlugins {

  /**
   * Trigger a table process on an action.
   *
   * @param action
   * @return created table process
   */
  AmoroProcess<? extends TableProcessState> trigger(Action action);

  /**
   * Install a process factory for an action.
   *
   * @param action action type
   * @param processFactory process factory
   */
  void install(Action action, ProcessFactory<? extends TableProcessState> processFactory);

  /**
   * Check if an action is enabled.
   *
   * @param action
   * @return true if the action is enabled
   */
  boolean enabled(Action action);
}

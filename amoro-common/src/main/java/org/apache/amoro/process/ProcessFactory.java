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
import org.apache.amoro.TableRuntime;

/**
 * A factory to create a process. Normally, There will be default ProcessFactories for each action
 * and used by default scheduler. Meanwhile, user could extend external ProcessFactory to run jobs
 * on external resources like Yarn.
 */
public interface ProcessFactory<T extends ProcessState> {

  /**
   * Create a process for the action.
   *
   * @param tableRuntime table runtime
   * @param action action type
   * @return target process which has not been submitted yet.
   */
  AmoroProcess<T> create(TableRuntime tableRuntime, Action action);

  /**
   * Recover a process for the action from a state.
   *
   * @param tableRuntime table runtime
   * @param state state of the process
   * @return target process which has not been submitted yet.
   */
  AmoroProcess<T> recover(TableRuntime tableRuntime, T state);
}

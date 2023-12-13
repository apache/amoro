/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.api.events;

import com.netease.arctic.ams.api.TableIdentifier;

/** Event content for {@link EventType#TableOptimizingStateChanged} */
public class TableOptimizingStateChangedEventContent {
  /** Table is no need optimizing. */
  public static final String STATE_IDLE = "idle";

  /** Table is need optimizing, but waiting for resource */
  public static final String STATE_PENDING = "pending";

  /** Table is doing optimizing process planing. */
  public static final String STATE_PLANING = "planing";

  /** Table is executing optimizing process */
  public static final String STATE_EXECUTING = "executing";

  /** All optimizing process task is done, and process is committing. */
  public static final String STATE_COMMITTING = "committing";

  private final TableIdentifier identifier;
  private final String oldState;
  private final String currentState;


  public TableOptimizingStateChangedEventContent(
      TableIdentifier identifier,
      String oldState,
      String currentState) {
    this.identifier = identifier;
    this.oldState = oldState;
    this.currentState = currentState;
  }

  /** @return Table identifier */
  public TableIdentifier getIdentifier() {
    return identifier;
  }

  /** @return The old optimizing state, null if {@link Event#isOnLoad()} is true */
  public String getOldState() {
    return oldState;
  }

  /** @return The new optimizing state. */
  public String getCurrentState() {
    return currentState;
  }

}

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

/**
 * Events used to drive state transitions of table processes. Each event carries a priority for
 * conflict resolution.
 */
public enum ProcessEvent {

  /** Scheduler requested to submit the process. */
  SUBMIT_REQUESTED(10, "SUBMIT_REQUESTED"),
  /** Process completed successfully. */
  COMPLETE_SUCCESS(10, "COMPLETE_SUCCESS"),
  /** Process completed with failure; a reason may be attached. */
  COMPLETE_FAILED(10, "COMPLETE_FAILED"),
  /** Retry the process after a failure. */
  RETRY_REQUESTED(10, "RETRY_REQUESTED"),
  /** Scheduler/user requested a graceful cancellation. */
  CANCEL_REQUESTED(100, "CANCEL_REQUESTED"),
  /** Scheduler/user requested a force kill. */
  KILL_REQUESTED(100, "KILL_REQUESTED");

  private final int priority;
  private final String name;

  /**
   * Construct with a priority and display name.
   *
   * @param priority event priority
   * @param name event name
   */
  ProcessEvent(int priority, String name) {
    this.priority = priority;
    this.name = name;
  }

  /**
   * Get event display name.
   *
   * @return event name
   */
  public String getName() {
    return name;
  }

  /**
   * Get event priority.
   *
   * @return priority
   */
  public int getPriority() {
    return priority;
  }
}

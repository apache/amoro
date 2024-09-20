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

import java.util.Map;

/**
 * ProcessState contains information in any {@link AmoroProcess} which must be persistent and {@link
 * ProcessFactory} will use to recover {@link AmoroProcess}.
 */
public interface ProcessState {

  /** @return unique identifier of the process. */
  long getId();

  /**
   * @return the name of the state. If multiple stages are involved, it should be the name of the
   *     current stage.
   */
  String getName();

  /** @return start time of the process. */
  long getStartTime();

  /** @return the action of the process. */
  Action getAction();

  /** @return the status of the process. */
  ProcessStatus getStatus();

  /**
   * Get the string encoded summary of the process, this could be a simple description or a POJO
   * encoded by JSON
   *
   * @return the summary of the process
   */
  Map<String, String> getSummary();

  /** @return the reason of process failure, null if the process has not failed yet. */
  String getFailedReason();

  /**
   * Total millisecond running time of all tasks in the process.
   *
   * @return actual quota runtime of the process.
   */
  long getQuotaRuntime();

  /**
   * Quota value is calculated by the total millisecond running time of all tasks in the process
   * divided by the total millisecond from the start time to the current time. It is used to
   * evaluate the actual runtime concurrence of the process.
   *
   * @return the quota value of the process.
   */
  default double getQuotaValue() {
    return (double) getQuotaRuntime() / (System.currentTimeMillis() - getStartTime());
  }
}

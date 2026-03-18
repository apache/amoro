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

import org.apache.amoro.ActivePlugin;

import java.util.Collections;
import java.util.Map;

public interface ExecuteEngine extends ActivePlugin {

  /**
   * Get engine type of this execute engine.
   *
   * @return engine type
   */
  EngineType engineType();

  /**
   * Get the status from engine by external process identifier.
   *
   * @param processIdentifier external process identifier
   * @return current status in engine
   */
  ProcessStatus getStatus(String processIdentifier);

  /**
   * Get the status detail from engine by external process identifier.
   *
   * <p>Default implementation keeps backward compatibility for engines that only expose {@link
   * ProcessStatus}.
   *
   * @param processIdentifier external process identifier
   * @return current status detail in engine
   */
  default ProcessStatusInfo getStatusInfo(String processIdentifier) {
    return ProcessStatusInfo.of(getStatus(processIdentifier));
  }

  /**
   * Submit a table process to engine.
   *
   * <p>This method must return when process status is not Pending.
   *
   * @param tableProcess table process to submit
   * @return external process identifier in engine
   */
  String submitTableProcess(TableProcess tableProcess);

  /**
   * Cancel a table process in engine.
   *
   * <p>This method must return a readable process status or throw exception.
   *
   * @param tableProcess table process to cancel
   * @param processIdentifier external process identifier
   * @return status after cancel attempt
   */
  ProcessStatus tryCancelTableProcess(TableProcess tableProcess, String processIdentifier);

  /**
   * Build an updated summary for retry. Engines can override this to archive engine-specific
   * identifiers (e.g. qids) before the current identifier is cleared on retry.
   *
   * @param currentIdentifier current external process identifier about to be cleared
   * @param currentSummary current summary map
   * @return updated summary map for the retry attempt
   */
  default Map<String, String> buildRetrySummary(
      String currentIdentifier, Map<String, String> currentSummary) {
    return currentSummary != null ? currentSummary : Collections.emptyMap();
  }
}

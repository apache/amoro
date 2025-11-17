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

package org.apache.amoro.server.process.executor;

import org.apache.amoro.ActivePlugin;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;

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
}

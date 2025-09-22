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

package org.apache.amoro.server.refresh;

import org.apache.amoro.TableRuntime;
import org.apache.amoro.server.table.TableService;

public interface RefreshEvent {
  /** The unique identifier of the {@link RefreshEvent}. */
  String getIdentifier();

  /**
   * Refresh tableRuntime and evaluate pending input for tableRuntime
   *
   * @param tableRuntime The runtime instance of the table that needs to be refreshed and evaluated
   * @param tableService The tableService used for loading managed tables
   * @param maxPendingPartitions The maximum number of pending partitions to process, used to limit
   *     the processing amount per execution
   */
  void execute(TableRuntime tableRuntime, TableService tableService, int maxPendingPartitions);

  /**
   * Calculate the next execution time for refreshing the tableRuntime.
   *
   * @param tableRuntime The runtime instance of the table that needs to be refreshed and evaluated
   * @param defaultInterval Default interval for the next execution time set in
   *     TableRuntimeRefreshExecutor, in milliseconds
   * @return The next execution time of the task, in milliseconds since the start time.
   */
  long getNextExecutingTime(TableRuntime tableRuntime, long defaultInterval);
}

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

/** Event content for {@link EventType#TableOptimizingTaskAcked} */
public class TableOptimizingTaskEventContent {

  private final TableIdentifier identifier;

  private final long processId;

  private final long taskId;


  public TableOptimizingTaskEventContent(
      TableIdentifier identifier, long processId, long taskId) {
    this.identifier = identifier;
    this.processId = processId;
    this.taskId = taskId;
  }

  public TableIdentifier getIdentifier() {
    return identifier;
  }

  public long getProcessId() {
    return processId;
  }

  public long getTaskId() {
    return taskId;
  }
}

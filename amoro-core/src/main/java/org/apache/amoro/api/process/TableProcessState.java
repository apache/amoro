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

package org.apache.amoro.api.process;

import org.apache.amoro.api.Action;
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.StateField;
import org.apache.amoro.process.ProcessStatus;

import java.util.Map;

/** A common state of a table process. */
public class TableProcessState implements ProcessState {

  @StateField private volatile long id;
  private final Action action;
  private final ServerTableIdentifier tableIdentifier;
  @StateField private long startTime;
  @StateField private long endTime = -1L;
  @StateField private ProcessStatus status = ProcessStatus.ACTIVE;
  @StateField private volatile String failedReason;
  private volatile Map<String, String> summary;

  public TableProcessState(Action action, ServerTableIdentifier tableIdentifier) {
    this.action = action;
    this.tableIdentifier = tableIdentifier;
  }

  public TableProcessState(long id, Action action, ServerTableIdentifier tableIdentifier) {
    this.id = id;
    this.action = action;
    this.tableIdentifier = tableIdentifier;
  }

  @Override
  public long getId() {
    return id;
  }

  public String getName() {
    return action.getDescription();
  }

  public Action getAction() {
    return action;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public ProcessStatus getStatus() {
    return status;
  }

  @Override
  public Map<String, String> getSummary() {
    return summary;
  }

  @Override
  public long getQuotaRuntime() {
    return getDuration();
  }

  @Override
  public double getQuotaValue() {
    return 1;
  }

  public long getDuration() {
    return endTime > 0 ? endTime - startTime : System.currentTimeMillis() - startTime;
  }

  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  protected void setSummary(Map<String, String> summary) {
    this.summary = summary;
  }

  protected void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  protected void setStatus(ProcessStatus status) {
    if (status == ProcessStatus.SUCCESS
        || status == ProcessStatus.FAILED
        || status == ProcessStatus.CLOSED) {
      endTime = System.currentTimeMillis();
    } else if (this.status != ProcessStatus.ACTIVE && status == ProcessStatus.ACTIVE) {
      endTime = -1L;
      failedReason = null;
      summary = null;
    }
    this.status = status;
  }

  public String getFailedReason() {
    return failedReason;
  }

  protected void setFailedReason(String failedReason) {
    this.status = ProcessStatus.FAILED;
    this.failedReason = failedReason;
    this.endTime = System.currentTimeMillis();
  }

  protected void setId(long processId) {
    this.id = processId;
  }
}

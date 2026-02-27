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

package org.apache.amoro.server.persistence;

import java.util.Map;

/**
 * process_id BIGINT NOT NULL PRIMARY KEY, table_id BIGINT NOT NULL, target_snapshot_id BIGINT NOT
 * NULL, target_change_snapshot_id BIGINT NOT NULL, rewrite_input BLOB(64m), from_sequence
 * CLOB(64m), to_sequence CLOB(64m)
 */
public class OptimizingProcessState {
  private long processId;
  private long tableId;
  private long targetSnapshotId;
  private long targetChangeSnapshotId;
  private Map<String, Long> fromSequence;
  private Map<String, Long> toSequence;

  public long getProcessId() {
    return processId;
  }

  public void setProcessId(long processId) {
    this.processId = processId;
  }

  public long getTableId() {
    return tableId;
  }

  public void setTableId(long tableId) {
    this.tableId = tableId;
  }

  public long getTargetSnapshotId() {
    return targetSnapshotId;
  }

  public void setTargetSnapshotId(long targetSnapshotId) {
    this.targetSnapshotId = targetSnapshotId;
  }

  public long getTargetChangeSnapshotId() {
    return targetChangeSnapshotId;
  }

  public void setTargetChangeSnapshotId(long targetChangeSnapshotId) {
    this.targetChangeSnapshotId = targetChangeSnapshotId;
  }

  public Map<String, Long> getFromSequence() {
    return fromSequence;
  }

  public void setFromSequence(Map<String, Long> fromSequence) {
    this.fromSequence = fromSequence;
  }

  public Map<String, Long> getToSequence() {
    return toSequence;
  }

  public void setToSequence(Map<String, Long> toSequence) {
    this.toSequence = toSequence;
  }
}

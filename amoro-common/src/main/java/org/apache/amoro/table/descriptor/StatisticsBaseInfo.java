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

package org.apache.amoro.table.descriptor;

public class StatisticsBaseInfo {

  private Long snapshotId;

  private Long schemaId;

  private Long mergedRecordCount;

  private Long mergedRecordSize;

  public StatisticsBaseInfo() {}

  public StatisticsBaseInfo(
      Long snapshotId, Long schemaId, Long mergedRecordCount, Long mergedRecordSize) {
    this.snapshotId = snapshotId;
    this.schemaId = schemaId;
    this.mergedRecordCount = mergedRecordCount;
    this.mergedRecordSize = mergedRecordSize;
  }

  public long getSnapshotId() {
    return snapshotId;
  }

  public void setSnapshotId(Long snapshotId) {
    this.snapshotId = snapshotId;
  }

  public long getSchemaId() {
    return schemaId;
  }

  public void setSchemaId(Long schemaId) {
    this.schemaId = schemaId;
  }

  public Long getMergedRecordCount() {
    return mergedRecordCount;
  }

  public void setMergedRecordCount(Long mergedRecordCount) {
    this.mergedRecordCount = mergedRecordCount;
  }

  public Long getMergedRecordSize() {
    return mergedRecordSize;
  }

  public void setMergedRecordSize(Long mergedRecordSize) {
    this.mergedRecordSize = mergedRecordSize;
  }
}

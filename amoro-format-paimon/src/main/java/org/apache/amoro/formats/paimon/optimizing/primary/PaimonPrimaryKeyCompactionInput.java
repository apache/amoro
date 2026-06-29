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

package org.apache.amoro.formats.paimon.optimizing.primary;

import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.optimizing.BaseOptimizingInput;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

import java.util.List;

public class PaimonPrimaryKeyCompactionInput extends BaseOptimizingInput {

  private static final long serialVersionUID = 1L;

  private PaimonTable table;
  private List<PaimonBucketCompactionUnit> units;
  private OptimizingType optimizingType;
  private boolean fullCompaction;
  private long targetSnapshotId;
  private String commitUser;
  private long commitIdentifier;

  public PaimonPrimaryKeyCompactionInput() {}

  public PaimonPrimaryKeyCompactionInput(
      PaimonTable table,
      List<PaimonBucketCompactionUnit> units,
      OptimizingType optimizingType,
      boolean fullCompaction,
      long targetSnapshotId,
      String commitUser,
      long commitIdentifier) {
    this.table = table;
    this.units = units;
    this.optimizingType = optimizingType;
    this.fullCompaction = fullCompaction;
    this.targetSnapshotId = targetSnapshotId;
    this.commitUser = commitUser;
    this.commitIdentifier = commitIdentifier;
  }

  public PaimonTable getTable() {
    return table;
  }

  public List<PaimonBucketCompactionUnit> getUnits() {
    return units;
  }

  public OptimizingType getOptimizingType() {
    return optimizingType;
  }

  public boolean isFullCompaction() {
    return fullCompaction;
  }

  public long getTargetSnapshotId() {
    return targetSnapshotId;
  }

  public String getCommitUser() {
    return commitUser;
  }

  public long getCommitIdentifier() {
    return commitIdentifier;
  }

  @Override
  public String describe() {
    String tableId = table == null ? "<unknown>" : String.valueOf(table.id());
    int unitCount = units == null ? 0 : units.size();
    String bucketDescription =
        unitCount == 1 ? String.format(", bucket:%d", units.get(0).getBucket()) : "";
    return String.format(
        "Amoro paimon primary-key compaction task, table:%s, type:%s, bucketUnits:%d%s",
        tableId, optimizingType, unitCount, bucketDescription);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("table", table == null ? null : table.id())
        .add("units", units)
        .add("optimizingType", optimizingType)
        .add("fullCompaction", fullCompaction)
        .add("targetSnapshotId", targetSnapshotId)
        .add("commitUser", commitUser)
        .add("commitIdentifier", commitIdentifier)
        .addValue(super.toString())
        .toString();
  }
}

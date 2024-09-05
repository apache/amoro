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

package org.apache.amoro.flink.read.hybrid.enumerator;

import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;

/**
 * The enumerator offset indicate the snapshot id of the change table, or the timestamp of snapshot.
 */
public class MixedFormatEnumeratorOffset {
  private static final MixedFormatEnumeratorOffset EMPTY = of(Long.MIN_VALUE, Long.MIN_VALUE);

  /** use Long.MIN_VALUE to indicate the earliest offset */
  public static final long EARLIEST_SNAPSHOT_ID = Long.MIN_VALUE;

  private Long changeSnapshotId;
  private Long snapshotTimestampMs;

  private MixedFormatEnumeratorOffset(Long changeSnapshotId, Long snapshotTimestampMs) {
    this.changeSnapshotId = changeSnapshotId;
    this.snapshotTimestampMs = snapshotTimestampMs;
  }

  public static MixedFormatEnumeratorOffset of(Long changeSnapshotId, Long snapshotTimestampMs) {
    return new MixedFormatEnumeratorOffset(changeSnapshotId, snapshotTimestampMs);
  }

  public static MixedFormatEnumeratorOffset empty() {
    return EMPTY;
  }

  public Long changeSnapshotId() {
    return changeSnapshotId;
  }

  public void changeSnapshotId(long changeSnapshotId) {
    this.changeSnapshotId = changeSnapshotId;
  }

  public Long snapshotTimestampMs() {
    return snapshotTimestampMs;
  }

  public void snapshotTimestampMs(Long snapshotTimestamp) {
    this.snapshotTimestampMs = snapshotTimestamp;
  }

  public boolean isEmpty() {
    return (changeSnapshotId == null && snapshotTimestampMs == null) || equals(EMPTY);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(changeSnapshotId, snapshotTimestampMs);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("changeSnapshotId", changeSnapshotId)
        .add("snapshotTimestamp", snapshotTimestampMs)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MixedFormatEnumeratorOffset other = (MixedFormatEnumeratorOffset) o;
    return Objects.equal(changeSnapshotId, other.changeSnapshotId())
        && Objects.equal(snapshotTimestampMs, other.snapshotTimestampMs());
  }
}

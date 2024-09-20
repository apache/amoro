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

package org.apache.amoro.formats.mixed;

import org.apache.amoro.TableSnapshot;
import org.apache.amoro.shade.guava32.com.google.common.primitives.Longs;
import org.apache.iceberg.Snapshot;

import java.util.Optional;

public class MixedSnapshot implements TableSnapshot {

  private final Snapshot changeSnapshot;

  private final Snapshot baseSnapshot;

  public MixedSnapshot(Snapshot changeSnapshot, Snapshot baseSnapshot) {
    this.changeSnapshot = changeSnapshot;
    this.baseSnapshot = baseSnapshot;
  }

  @Override
  public long watermark() {
    return -1;
  }

  @Override
  public long commitTime() {
    Long changCommit =
        Optional.ofNullable(changeSnapshot).map(Snapshot::timestampMillis).orElse(-1L);
    Long baseCommit = Optional.ofNullable(baseSnapshot).map(Snapshot::timestampMillis).orElse(-1L);
    return Longs.max(changCommit, baseCommit);
  }

  public Snapshot getChangeSnapshot() {
    return changeSnapshot;
  }

  public Snapshot getBaseSnapshot() {
    return baseSnapshot;
  }

  public long getChangeSnapshotId() {
    return Optional.ofNullable(changeSnapshot).map(Snapshot::snapshotId).orElse(-1L);
  }

  public long getBaseSnapshotId() {
    return Optional.ofNullable(baseSnapshot).map(Snapshot::snapshotId).orElse(-1L);
  }

  @Override
  public String id() {
    return changeSnapshot + "_" + baseSnapshot;
  }
}

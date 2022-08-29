/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.netease.arctic.flink.read.hybrid.enumerator;

import com.netease.arctic.flink.read.FlinkSplitPlanner;
import com.netease.arctic.flink.read.hybrid.split.ArcticSplit;
import com.netease.arctic.flink.table.ArcticTableLoader;
import com.netease.arctic.table.KeyedTable;
import org.apache.flink.annotation.Internal;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.TableScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.netease.arctic.flink.read.FlinkSplitPlanner.planChangeTable;
import static com.netease.arctic.flink.util.ArcticUtils.loadArcticTable;

/**
 * Continuous planning {@link KeyedTable} by {@link ArcticEnumeratorOffset} and generate a
 * {@link ContinuousEnumerationResult}.
 */
@Internal
public class ContinuousSplitPlannerImpl implements ContinuousSplitPlanner {
  private static final Logger LOG = LoggerFactory.getLogger(ContinuousSplitPlannerImpl.class);

  private transient KeyedTable table;
  private final ArcticTableLoader loader;
  private static final AtomicInteger splitCount = new AtomicInteger();

  public ContinuousSplitPlannerImpl(ArcticTableLoader loader) {
    this.loader = loader;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public ContinuousEnumerationResult planSplits(ArcticEnumeratorOffset lastOffset) {
    if (table == null) {
      table = loadArcticTable(loader).asKeyedTable();
    }
    table.refresh();
    if (lastOffset != null) {
      return discoverIncrementalSplits(lastOffset);
    } else {
      return discoverInitialSplits();
    }
  }

  private ContinuousEnumerationResult discoverIncrementalSplits(ArcticEnumeratorOffset lastPosition) {
    long fromChangeSnapshotId = lastPosition.changeSnapshotId();
    Snapshot changeSnapshot = table.changeTable().currentSnapshot();
    if (changeSnapshot != null && changeSnapshot.snapshotId() != fromChangeSnapshotId) {
      long snapshotId = changeSnapshot.snapshotId();
      TableScan tableScan = table.changeTable().newScan().appendsBetween(fromChangeSnapshotId, snapshotId);

      List<ArcticSplit> arcticChangeSplit = planChangeTable(tableScan, splitCount);

      return new ContinuousEnumerationResult(
          arcticChangeSplit,
          lastPosition,
          ArcticEnumeratorOffset.of(snapshotId, null));
    }
    return ContinuousEnumerationResult.EMPTY;
  }

  private ContinuousEnumerationResult discoverInitialSplits() {
    Snapshot changeSnapshot = table.changeTable().currentSnapshot();
    if (changeSnapshot != null) {
      long changeStartSnapshotId = table.changeTable().currentSnapshot().snapshotId();
      List<ArcticSplit> arcticSplits = FlinkSplitPlanner.planFullTable(table, splitCount);

      return new ContinuousEnumerationResult(
          arcticSplits,
          null,
          ArcticEnumeratorOffset.of(changeStartSnapshotId, null));
    } else {
      LOG.info("There have no change snapshot, table {}.", table);
    }
    return ContinuousEnumerationResult.EMPTY;
  }
}

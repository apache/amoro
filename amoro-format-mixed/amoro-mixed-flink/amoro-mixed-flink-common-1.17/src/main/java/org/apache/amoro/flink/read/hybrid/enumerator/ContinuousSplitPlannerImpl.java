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

import static org.apache.amoro.flink.read.FlinkSplitPlanner.planChangeTable;
import static org.apache.amoro.flink.read.hybrid.enumerator.MixedFormatEnumeratorOffset.EARLIEST_SNAPSHOT_ID;
import static org.apache.amoro.flink.util.MixedFormatUtils.loadMixedTable;

import org.apache.amoro.flink.read.FlinkSplitPlanner;
import org.apache.amoro.flink.read.hybrid.split.ChangelogSplit;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.hybrid.split.SnapshotSplit;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.scan.ChangeTableIncrementalScan;
import org.apache.amoro.table.KeyedTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.annotation.Internal;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.expressions.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Continuous planning {@link KeyedTable} by {@link MixedFormatEnumeratorOffset} and generate a
 * {@link ContinuousEnumerationResult}.
 *
 * <p>{@link ContinuousEnumerationResult#splits()} includes the {@link SnapshotSplit}s and {@link
 * ChangelogSplit}s.
 */
@Internal
public class ContinuousSplitPlannerImpl implements ContinuousSplitPlanner {
  private static final Logger LOG = LoggerFactory.getLogger(ContinuousSplitPlannerImpl.class);

  protected transient KeyedTable table;
  protected final MixedFormatTableLoader loader;
  protected static final AtomicInteger SPLIT_COUNT = new AtomicInteger();

  public ContinuousSplitPlannerImpl(MixedFormatTableLoader loader) {
    this.loader = loader;
  }

  @Override
  public void close() throws IOException {
    if (loader != null) {
      loader.close();
    }
  }

  @Override
  public ContinuousEnumerationResult planSplits(
      MixedFormatEnumeratorOffset lastOffset, List<Expression> filters) {
    if (table == null) {
      table = loadMixedTable(loader).asKeyedTable();
    }
    table.refresh();
    if (lastOffset != null) {
      return discoverIncrementalSplits(lastOffset, filters);
    } else {
      return discoverInitialSplits(filters);
    }
  }

  protected ContinuousEnumerationResult discoverIncrementalSplits(
      MixedFormatEnumeratorOffset lastPosition, List<Expression> filters) {
    long fromChangeSnapshotId = lastPosition.changeSnapshotId();
    Snapshot changeSnapshot = table.changeTable().currentSnapshot();
    if (changeSnapshot != null && changeSnapshot.snapshotId() != fromChangeSnapshotId) {
      long snapshotId = changeSnapshot.snapshotId();
      ChangeTableIncrementalScan changeTableScan =
          table.changeTable().newScan().useSnapshot(snapshotId);
      if (filters != null) {
        for (Expression filter : filters) {
          changeTableScan = changeTableScan.filter(filter);
        }
      }

      if (fromChangeSnapshotId != Long.MIN_VALUE) {
        Snapshot snapshot = table.changeTable().snapshot(fromChangeSnapshotId);
        changeTableScan = changeTableScan.fromSequence(snapshot.sequenceNumber());
      }

      List<MixedFormatSplit> changeSplit = planChangeTable(changeTableScan, SPLIT_COUNT);
      return new ContinuousEnumerationResult(
          changeSplit, lastPosition, MixedFormatEnumeratorOffset.of(snapshotId, null));
    }
    return ContinuousEnumerationResult.EMPTY;
  }

  protected ContinuousEnumerationResult discoverInitialSplits(List<Expression> filters) {
    Snapshot changeSnapshot = table.changeTable().currentSnapshot();
    // todo ShuffleSplitAssigner doesn't support MergeOnReadSplit right now,
    //  because it doesn't implement the dataTreeNode() method
    //  fix AMORO-1950 in the future.
    List<MixedFormatSplit> mixedFormatSplits =
        FlinkSplitPlanner.planFullTable(table, filters, SPLIT_COUNT);

    long changeStartSnapshotId =
        changeSnapshot != null ? changeSnapshot.snapshotId() : EARLIEST_SNAPSHOT_ID;
    if (changeSnapshot == null && CollectionUtils.isEmpty(mixedFormatSplits)) {
      LOG.info("There have no change snapshot, and no base splits in table: {}.", table);
      return ContinuousEnumerationResult.EMPTY;
    }

    return new ContinuousEnumerationResult(
        mixedFormatSplits, null, MixedFormatEnumeratorOffset.of(changeStartSnapshotId, null));
  }
}

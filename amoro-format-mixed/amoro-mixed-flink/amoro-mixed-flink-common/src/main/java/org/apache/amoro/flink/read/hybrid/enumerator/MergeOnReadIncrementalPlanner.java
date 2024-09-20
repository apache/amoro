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

import static org.apache.amoro.flink.read.hybrid.enumerator.MixedFormatEnumeratorOffset.EARLIEST_SNAPSHOT_ID;

import org.apache.amoro.flink.read.FlinkSplitPlanner;
import org.apache.amoro.flink.read.hybrid.split.ChangelogSplit;
import org.apache.amoro.flink.read.hybrid.split.MergeOnReadSplit;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.expressions.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A planner for merge-on-read scanning by {@link this#discoverInitialSplits} and incremental
 * scanning by {@link this#discoverIncrementalSplits(MixedFormatEnumeratorOffset, List)}.
 *
 * <p>{@link ContinuousEnumerationResult#splits()} includes the {@link MergeOnReadSplit}s and {@link
 * ChangelogSplit}s.
 */
public class MergeOnReadIncrementalPlanner extends ContinuousSplitPlannerImpl {
  private static final Logger LOG = LoggerFactory.getLogger(MergeOnReadIncrementalPlanner.class);

  public MergeOnReadIncrementalPlanner(MixedFormatTableLoader loader) {
    super(loader);
  }

  @Override
  protected ContinuousEnumerationResult discoverInitialSplits(List<Expression> filters) {
    Snapshot changeSnapshot = table.changeTable().currentSnapshot();

    List<MixedFormatSplit> mixedFormatSplits =
        FlinkSplitPlanner.mergeOnReadPlan(table, filters, SPLIT_COUNT);

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

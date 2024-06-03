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
import static org.apache.amoro.flink.util.MixedFormatUtils.loadMixedTable;

import org.apache.amoro.flink.read.FlinkSplitPlanner;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.table.KeyedTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.expressions.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Used for MergeOnRead, only for the bounded reading and return append stream. */
public class MergeOnReadPlannerImpl implements ContinuousSplitPlanner {
  private static final Logger LOG = LoggerFactory.getLogger(MergeOnReadPlannerImpl.class);

  protected transient KeyedTable table;
  protected final MixedFormatTableLoader loader;
  protected static final AtomicInteger SPLIT_COUNT = new AtomicInteger();

  public MergeOnReadPlannerImpl(MixedFormatTableLoader loader) {
    this.loader = loader;
  }

  @Override
  public ContinuousEnumerationResult planSplits(
      MixedFormatEnumeratorOffset ignored, List<Expression> filters) {
    // todo support mor the table from the specific offset in the future
    if (table == null) {
      table = loadMixedTable(loader).asKeyedTable();
    }
    table.refresh();
    return discoverInitialSplits(filters);
  }

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

  @Override
  public void close() throws IOException {
    if (loader != null) {
      loader.close();
    }
  }
}

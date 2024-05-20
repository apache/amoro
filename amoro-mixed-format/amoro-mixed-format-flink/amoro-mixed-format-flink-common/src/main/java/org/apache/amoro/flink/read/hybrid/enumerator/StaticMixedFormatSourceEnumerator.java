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

import static org.apache.amoro.flink.util.MixedFormatUtils.loadMixedTable;

import org.apache.amoro.flink.read.hybrid.assigner.SplitAssigner;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.source.MixedFormatScanContext;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.table.KeyedTable;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.iceberg.flink.source.ScanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.Collection;

/**
 * This is a static mixed-format source enumerator, used for bounded source scan. Working enabled
 * only just {@link ScanContext#STREAMING} is equal to false.
 */
public class StaticMixedFormatSourceEnumerator extends AbstractMixedFormatEnumerator {
  private static final Logger LOG =
      LoggerFactory.getLogger(StaticMixedFormatSourceEnumerator.class);
  private final SplitAssigner assigner;
  private final MixedFormatTableLoader loader;
  private transient KeyedTable keyedTable;
  private final MixedFormatScanContext scanContext;
  private final boolean shouldEnumerate;
  private final ContinuousSplitPlanner splitPlanner;

  public StaticMixedFormatSourceEnumerator(
      SplitEnumeratorContext<MixedFormatSplit> enumeratorContext,
      SplitAssigner assigner,
      MixedFormatTableLoader loader,
      MixedFormatScanContext scanContext,
      @Nullable MixedFormatSourceEnumState enumState) {
    super(enumeratorContext, assigner);
    this.loader = loader;
    this.assigner = assigner;
    this.scanContext = scanContext;
    // split enumeration is not needed during a restore scenario
    this.shouldEnumerate = enumState == null;
    this.splitPlanner = new MergeOnReadPlannerImpl(loader);
  }

  @Override
  public void start() {
    super.start();
    if (keyedTable == null) {
      keyedTable = loadMixedTable(loader).asKeyedTable();
    }
    if (shouldEnumerate) {
      keyedTable.baseTable().refresh();
      keyedTable.changeTable().refresh();
      Collection<MixedFormatSplit> splits =
          splitPlanner.planSplits(null, scanContext.filters()).splits();
      assigner.onDiscoveredSplits(splits);
      LOG.info(
          "Discovered {} splits from table {} during job initialization",
          splits.size(),
          keyedTable.name());
    }
  }

  @Override
  protected boolean shouldWaitForMoreSplits() {
    return false;
  }

  @Override
  public MixedFormatSourceEnumState snapshotState(long checkpointId) throws Exception {
    return new MixedFormatSourceEnumState(assigner.state(), null, null, null);
  }
}

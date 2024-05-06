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

import static org.apache.amoro.flink.util.AmoroUtils.loadArcticTable;

import org.apache.amoro.flink.read.hybrid.assigner.SplitAssigner;
import org.apache.amoro.flink.read.hybrid.split.AmoroSplit;
import org.apache.amoro.flink.read.source.AmoroScanContext;
import org.apache.amoro.flink.table.AmoroTableLoader;
import org.apache.amoro.table.KeyedTable;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.iceberg.flink.source.ScanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.Collection;

/**
 * This is a static arctic source enumerator, used for bounded source scan. Working enabled only
 * just {@link ScanContext#STREAMING} is equal to false.
 */
public class StaticAmoroSourceEnumerator extends AbstractAmoroEnumerator {
  private static final Logger LOG = LoggerFactory.getLogger(StaticAmoroSourceEnumerator.class);
  private final SplitAssigner assigner;
  private final AmoroTableLoader loader;
  private transient KeyedTable keyedTable;
  private final AmoroScanContext scanContext;
  private final boolean shouldEnumerate;
  private final ContinuousSplitPlanner splitPlanner;

  public StaticAmoroSourceEnumerator(
      SplitEnumeratorContext<AmoroSplit> enumeratorContext,
      SplitAssigner assigner,
      AmoroTableLoader loader,
      AmoroScanContext scanContext,
      @Nullable AmoroSourceEnumState enumState) {
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
      keyedTable = loadArcticTable(loader).asKeyedTable();
    }
    if (shouldEnumerate) {
      keyedTable.baseTable().refresh();
      keyedTable.changeTable().refresh();
      Collection<AmoroSplit> splits = splitPlanner.planSplits(null, scanContext.filters()).splits();
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
  public AmoroSourceEnumState snapshotState(long checkpointId) throws Exception {
    return new AmoroSourceEnumState(assigner.state(), null, null, null);
  }
}

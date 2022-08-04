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

package com.netease.arctic.flink.read.hybrid.enumerator;

import com.netease.arctic.flink.read.hybrid.assigner.ShuffleSplitAssigner;
import com.netease.arctic.flink.read.hybrid.assigner.SplitAssigner;
import com.netease.arctic.flink.read.hybrid.reader.FirstSplits;
import com.netease.arctic.flink.read.hybrid.reader.HybridSplitReader;
import com.netease.arctic.flink.read.hybrid.split.ArcticSplit;
import com.netease.arctic.flink.read.hybrid.split.SplitRequestEvent;
import com.netease.arctic.flink.read.source.ArcticScanContext;
import com.netease.arctic.flink.table.ArcticTableLoader;
import com.netease.arctic.table.KeyedTable;
import org.apache.flink.api.connector.source.SourceEvent;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.util.FlinkRuntimeException;
import org.apache.iceberg.Snapshot;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.netease.arctic.flink.table.descriptors.ArcticValidator.FILE_SCAN_STARTUP_MODE;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.FILE_SCAN_STARTUP_MODE_LATEST;
import static com.netease.arctic.flink.util.ArcticUtils.loadArcticTable;

/**
 * Enumerator for arctic source, assign {@link ArcticSplit} to arctic source reader {@link HybridSplitReader}
 */
public class ArcticSourceEnumerator extends AbstractArcticEnumerator {
  private static final Logger LOG = LoggerFactory.getLogger(ArcticSourceEnumerator.class);
  private transient KeyedTable keyedTable;
  /**
   * To record the snapshotId at the first planSplits.
   * <p>
   * If its value is null, it means that we don't need to generate watermark. Won't check.
   * If its value is an empty Collection, it means that all splits have been finished.
   * So enumerator will push an event to all readers to start generate watermark. After that, reset it to null.
   * If its value is a Collection with elements, it means there are some splits have not been scanned.
   */
  private transient FirstSplits firstSplits = null;
  private final ArcticTableLoader loader;
  private final SplitEnumeratorContext<ArcticSplit> context;
  private final ContinuousSplitPlanner continuousSplitPlanner;
  private final SplitAssigner splitAssigner;
  private final ArcticScanContext scanContext;
  private final long snapshotDiscoveryIntervalMs;
  private final boolean generateWatermark;
  /**
   * snapshotId for the last enumerated snapshot. next incremental enumeration
   * should be based off this as the starting position.
   */
  private final AtomicReference<ArcticEnumeratorOffset> enumeratorPosition;

  public ArcticSourceEnumerator(
      SplitEnumeratorContext<ArcticSplit> enumContext,
      SplitAssigner splitAssigner,
      ArcticTableLoader loader,
      ArcticScanContext scanContext,
      @Nullable ArcticSourceEnumState enumState) {
    super(enumContext, splitAssigner);
    this.loader = loader;
    this.context = enumContext;
    this.splitAssigner = splitAssigner;
    this.scanContext = scanContext;
    this.continuousSplitPlanner = new ContinuousSplitPlannerImpl(loader);
    this.snapshotDiscoveryIntervalMs = scanContext.monitorInterval().toMillis();
    this.enumeratorPosition = new AtomicReference<>();
    if (enumState != null) {
      this.enumeratorPosition.set(enumState.lastEnumeratedOffset());
    }
    this.generateWatermark = true;
  }

  private final AtomicBoolean lock = new AtomicBoolean(false);

  @Override
  public void start() {
    if (keyedTable == null) {
      keyedTable = loadArcticTable(loader).asKeyedTable();
    }
    if (enumeratorPosition.get() == null &&
        FILE_SCAN_STARTUP_MODE_LATEST.equalsIgnoreCase(scanContext.scanStartupMode())) {
      keyedTable.refresh();
      Snapshot snapshot = keyedTable.changeTable().currentSnapshot();
      enumeratorPosition.set(ArcticEnumeratorOffset.of(snapshot.snapshotId(), null));
      LOG.info("{} is {}, the current snapshot id of the change table {}  is {}.",
          FILE_SCAN_STARTUP_MODE.key(), FILE_SCAN_STARTUP_MODE_LATEST, keyedTable.id(), snapshot.snapshotId());
    }
    if (snapshotDiscoveryIntervalMs > 0) {
      LOG.info(
          "Starting the ArcticSourceEnumerator with arctic table {} snapshot discovery interval of {} ms.",
          keyedTable,
          snapshotDiscoveryIntervalMs);
      context.callAsync(
          this::planSplits,
          this::handleResultOfSplits,
          0,
          snapshotDiscoveryIntervalMs
      );

      context.callAsync(
          this::assignSplits,
          (unused, t) -> {
            if (t != null) {
              throw new FlinkRuntimeException(
                  "Failed to assign arctic split due to ", t);
            }
          },
          1000,
          500
      );
    }
  }

  private ContinuousEnumerationResult planSplits() {
    ContinuousEnumerationResult result = doPlanSplits();
    if (generateWatermark && firstSplits == null) {
      firstSplits = new FirstSplits(result.splits());
    }
    return result;
  }

  private ContinuousEnumerationResult doPlanSplits() {
    if (lock.get()) {
      LOG.info("prefix plan splits thread haven't finished.");
      return ContinuousEnumerationResult.EMPTY;
    }
    lock.set(true);
    LOG.info("begin to plan splits current offset {}.", enumeratorPosition.get());
    return continuousSplitPlanner.planSplits(enumeratorPosition.get());
  }

  private void handleResultOfSplits(ContinuousEnumerationResult enumerationResult, Throwable t) {
    if (t != null) {
      lock.set(false);
      throw new FlinkRuntimeException(
          "Failed to scan arctic table due to ", t);
    }
    if (!enumerationResult.isEmpty()) {
      splitAssigner.onDiscoveredSplits(enumerationResult.splits());
      enumeratorPosition.set(enumerationResult.toOffset());
    }
    LOG.info("handled result of splits, discover splits size {}, latest offset {}.",
        enumerationResult.splits().size(), enumeratorPosition.get());
    lock.set(false);
  }

  @Override
  public void handleSourceEvent(int subtaskId, SourceEvent sourceEvent) {
    super.handleSourceEvent(subtaskId, sourceEvent);
    if (sourceEvent instanceof SplitRequestEvent) {
      SplitRequestEvent splitRequestEvent = (SplitRequestEvent) sourceEvent;
      Collection<String> finishedSplitIds = splitRequestEvent.finishedSplitIds();
      if (generateWatermark) {
        checkAndNotifyReaderTriggerWatermark(finishedSplitIds);
      }
    } else {
      throw new IllegalArgumentException(String.format("Received unknown event from subtask %d: %s",
          subtaskId, sourceEvent.getClass().getCanonicalName()));
    }
  }

  /**
   * TODO annotation
   *
   * @param finishedSplitIds
   */
  public void checkAndNotifyReaderTriggerWatermark(Collection<String> finishedSplitIds) {
    if (firstSplits == null || firstSplits.getFinished()) {
      return;
    }
    if (!firstSplits.removeAndReturnIfAllFinished(finishedSplitIds)) {
      return;
    }

    LOG.info("all splits finished, send events to readers");
    IntStream.range(0, context.currentParallelism())
        .forEach(i -> context.sendEventToSourceReader(i, StartWatermarkEvent.INSTANCE));
    firstSplits.clear();
  }

  @Override
  public ArcticSourceEnumState snapshotState(long checkpointId) throws Exception {
    long[] shuffleSplitRelation = null;
    if (splitAssigner instanceof ShuffleSplitAssigner) {
      shuffleSplitRelation = ((ShuffleSplitAssigner) splitAssigner).serializePartitionIndex();
    }
    return new ArcticSourceEnumState(splitAssigner.state(), enumeratorPosition.get(), shuffleSplitRelation);
  }

  @Override
  public void close() throws IOException {
    continuousSplitPlanner.close();
    splitAssigner.close();
    super.close();
  }


  @Override
  protected boolean shouldWaitForMoreSplits() {
    return true;
  }
}

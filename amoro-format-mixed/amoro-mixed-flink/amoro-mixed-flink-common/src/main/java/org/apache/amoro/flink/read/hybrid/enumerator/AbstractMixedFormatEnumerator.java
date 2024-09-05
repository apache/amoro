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

import org.apache.amoro.flink.read.hybrid.assigner.Split;
import org.apache.amoro.flink.read.hybrid.assigner.SplitAssigner;
import org.apache.amoro.flink.read.hybrid.reader.ReaderStartedEvent;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.hybrid.split.SplitRequestEvent;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.connector.source.SourceEvent;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/** The abstract mixed-format source enumerator. */
public abstract class AbstractMixedFormatEnumerator
    implements SplitEnumerator<MixedFormatSplit, MixedFormatSourceEnumState> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractMixedFormatEnumerator.class);
  private final SplitEnumeratorContext<MixedFormatSplit> enumeratorContext;
  private final SplitAssigner assigner;
  private final Map<Integer, String> readersAwaitingSplit;
  private final AtomicReference<CompletableFuture<Void>> availableFuture;

  AbstractMixedFormatEnumerator(
      SplitEnumeratorContext<MixedFormatSplit> enumeratorContext, SplitAssigner assigner) {
    this.enumeratorContext = enumeratorContext;
    this.assigner = assigner;
    this.readersAwaitingSplit = new ConcurrentHashMap<>();
    this.availableFuture = new AtomicReference<>();
  }

  @Override
  public void start() {}

  @Override
  public void close() throws IOException {
    assigner.close();
  }

  @Override
  public void handleSplitRequest(int subtaskId, @Nullable String requesterHostname) {
    throw new UnsupportedOperationException(
        String.format(
            "Received invalid default split request event "
                + "from subtask %d as mixed-format source uses custom split request event",
            subtaskId));
  }

  @Override
  public void handleSourceEvent(int subtaskId, SourceEvent sourceEvent) {
    if (sourceEvent instanceof SplitRequestEvent) {
      SplitRequestEvent splitRequestEvent = (SplitRequestEvent) sourceEvent;
      LOG.info("Received request split event from subtask {}", subtaskId);
      assigner.onCompletedSplits(splitRequestEvent.finishedSplitIds());
      readersAwaitingSplit.put(subtaskId, String.valueOf(splitRequestEvent.requesterHostname()));
      assignSplits();
    } else if (sourceEvent instanceof ReaderStartedEvent) {
      LOG.info("Received ReaderStartEvent from subtask {}", subtaskId);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "Received unknown event from subtask %d: %s",
              subtaskId, sourceEvent.getClass().getCanonicalName()));
    }
  }

  @Override
  public void addReader(int subtaskId) {
    LOG.info("Added reader: {}", subtaskId);
  }

  @Override
  public void addSplitsBack(List<MixedFormatSplit> splits, int subtaskId) {
    LOG.info("addSplitsBack from subtaskId {}, splits {}.", subtaskId, splits);
    assigner.onUnassignedSplits(splits);
  }

  /** return true if enumerator should wait for splits like in the continuous enumerator case. */
  protected abstract boolean shouldWaitForMoreSplits();

  protected void assignSplits() {
    LOG.info(
        "Assign mixed-format splits to {} readers, subtasks:{}.",
        readersAwaitingSplit.size(),
        readersAwaitingSplit.keySet().toArray());
    final Iterator<Map.Entry<Integer, String>> awaitingReader =
        readersAwaitingSplit.entrySet().iterator();
    while (awaitingReader.hasNext()) {
      final Map.Entry<Integer, String> nextAwaiting = awaitingReader.next();

      // if the reader that requested another split has failed in the meantime, remove
      // it from the list of waiting readers
      if (!enumeratorContext.registeredReaders().containsKey(nextAwaiting.getKey())) {
        LOG.info(
            "Due to this reader doesn't registered in the enumerator context any more, so remove this subtask reader"
                + " [{}] from the awaiting reader map.",
            nextAwaiting.getKey());
        awaitingReader.remove();
        continue;
      }

      final int awaitingSubtask = nextAwaiting.getKey();
      final Split nextSplit = assigner.getNext(awaitingSubtask);
      if (nextSplit.isAvailable()) {
        MixedFormatSplit mixedFormatSplit = nextSplit.split();
        LOG.info(
            "assign a mixed-format split to subtaskId {}, taskIndex {}, mixed-format split {}.",
            awaitingSubtask,
            mixedFormatSplit.taskIndex(),
            mixedFormatSplit);
        enumeratorContext.assignSplit(mixedFormatSplit, awaitingSubtask);
        awaitingReader.remove();
      } else if (nextSplit.isUnavailable()) {
        if (!shouldWaitForMoreSplits()) {
          LOG.info("No more splits available for subtask {}", awaitingSubtask);
          enumeratorContext.signalNoMoreSplits(awaitingSubtask);
          awaitingReader.remove();
        } else {
          fetchAvailableFutureIfNeeded();
          break;
        }
      }
    }
  }

  private synchronized void fetchAvailableFutureIfNeeded() {
    if (availableFuture.get() != null) {
      return;
    }

    CompletableFuture<Void> future =
        assigner
            .isAvailable()
            .thenAccept(
                ignore ->
                    // Must run assignSplits in coordinator thread
                    // because the future may be completed from other threads.
                    // E.g., in event time alignment assigner,
                    // watermark advancement from another source may
                    // cause the available future to be completed
                    enumeratorContext.runInCoordinatorThread(
                        () -> {
                          LOG.debug("Executing callback of assignSplits");
                          availableFuture.set(null);
                          assignSplits();
                        }));
    availableFuture.set(future);
    LOG.debug("Registered callback for future available splits");
  }

  @VisibleForTesting
  public Map<Integer, String> getReadersAwaitingSplit() {
    return readersAwaitingSplit;
  }
}

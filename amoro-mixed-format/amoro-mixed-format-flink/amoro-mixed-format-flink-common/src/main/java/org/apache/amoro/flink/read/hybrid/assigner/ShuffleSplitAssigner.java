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

package org.apache.amoro.flink.read.hybrid.assigner;

import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.flink.read.hybrid.enumerator.MixedFormatSourceEnumState;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplitState;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.util.FlinkRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * According to Mark, Index TreeNodes and subtaskId assigning a split to special subtask to read.
 */
public class ShuffleSplitAssigner implements SplitAssigner {
  private static final Logger LOG = LoggerFactory.getLogger(ShuffleSplitAssigner.class);

  private static final long POLL_TIMEOUT = 200;
  private final SplitEnumeratorContext<MixedFormatSplit> enumeratorContext;

  private int totalParallelism;
  private int totalSplitNum;
  private Long currentMaskOfTreeNode;
  private final Object lock = new Object();

  /**
   * Key is the partition data and file index of the mixed-format file, Value is flink application
   * subtaskId.
   */
  private final Map<Long, Integer> partitionIndexSubtaskMap;
  /** Key is subtaskId, Value is the queue of unAssigned mixed-format splits. */
  private final Map<Integer, PriorityBlockingQueue<MixedFormatSplit>> subtaskSplitMap;

  private CompletableFuture<Void> availableFuture;

  @VisibleForTesting
  public ShuffleSplitAssigner(SplitEnumeratorContext<MixedFormatSplit> enumeratorContext) {
    this.enumeratorContext = enumeratorContext;
    this.totalParallelism = enumeratorContext.currentParallelism();
    this.partitionIndexSubtaskMap = new ConcurrentHashMap<>();
    this.subtaskSplitMap = new ConcurrentHashMap<>();
  }

  public ShuffleSplitAssigner(
      SplitEnumeratorContext<MixedFormatSplit> enumeratorContext,
      String tableName,
      @Nullable MixedFormatSourceEnumState enumState) {
    this.enumeratorContext = enumeratorContext;
    this.partitionIndexSubtaskMap = new ConcurrentHashMap<>();
    this.subtaskSplitMap = new ConcurrentHashMap<>();
    if (enumState == null) {
      this.totalParallelism = enumeratorContext.currentParallelism();
      LOG.info(
          "Mixed-format source enumerator current parallelism is {} for table {}",
          totalParallelism,
          tableName);
    } else {
      LOG.info(
          "Mixed-format source restored {} splits from state for table {}",
          enumState.pendingSplits().size(),
          tableName);
      deserializePartitionIndex(
          Objects.requireNonNull(
              enumState.shuffleSplitRelation(),
              "The partition index and subtask state couldn't be null."));
      enumState
          .pendingSplits()
          .forEach(state -> onDiscoveredSplits(Collections.singleton(state.toSourceSplit())));
    }
  }

  @Override
  public Split getNext() {
    throw new UnsupportedOperationException(
        "ShuffleSplitAssigner couldn't support this operation.");
  }

  @Override
  public Split getNext(int subtaskId) {
    return getNextSplit(subtaskId)
        .map(Split::of)
        .orElseGet(isEmpty() ? Split::unavailable : Split::subtaskUnavailable);
  }

  private Optional<MixedFormatSplit> getNextSplit(int subTaskId) {
    int currentParallelism = enumeratorContext.currentParallelism();
    if (totalParallelism != currentParallelism) {
      throw new FlinkRuntimeException(
          String.format(
              "Source parallelism has been changed, before parallelism is %s, now is %s",
              totalParallelism, currentParallelism));
    }
    if (subtaskSplitMap.containsKey(subTaskId)) {
      PriorityBlockingQueue<MixedFormatSplit> queue = subtaskSplitMap.get(subTaskId);

      MixedFormatSplit mixedFormatSplit = null;
      try {
        mixedFormatSplit = queue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        LOG.warn("interruptedException", e);
      }
      if (mixedFormatSplit == null) {
        LOG.debug(
            "Subtask {}, couldn't retrieve mixed-format source split in the queue.", subTaskId);
        return Optional.empty();
      } else {
        LOG.info(
            "get next mixed-format split taskIndex {}, totalSplitNum {}, mixed-format split {}.",
            mixedFormatSplit.taskIndex(),
            totalSplitNum,
            mixedFormatSplit);
        return Optional.of(mixedFormatSplit);
      }
    } else {
      LOG.debug(
          "Subtask {}, it's an idle subtask due to the empty queue with this subtask.", subTaskId);
      return Optional.empty();
    }
  }

  @Override
  public void onDiscoveredSplits(Collection<MixedFormatSplit> splits) {
    splits.forEach(this::putSplitIntoQueue);
    // only complete pending future if new splits are discovered
    completeAvailableFuturesIfNeeded();
  }

  @Override
  public void onUnassignedSplits(Collection<MixedFormatSplit> splits) {
    onDiscoveredSplits(splits);
  }

  void putSplitIntoQueue(final MixedFormatSplit split) {
    List<DataTreeNode> exactlyTreeNodes = getExactlyTreeNodes(split);

    PrimaryKeyedFile file = findAnyFileInSplit(split);

    for (DataTreeNode node : exactlyTreeNodes) {
      long partitionIndexKey = Math.abs(file.partition().toString().hashCode() + node.index());
      int subtaskId =
          partitionIndexSubtaskMap.computeIfAbsent(
              partitionIndexKey, key -> (partitionIndexSubtaskMap.size() + 1) % totalParallelism);
      LOG.info(
          "partition = {}, (mask, index) = ({}, {}), subtaskId = {}",
          file.partition().toString(),
          node.mask(),
          node.index(),
          subtaskId);

      PriorityBlockingQueue<MixedFormatSplit> queue =
          subtaskSplitMap.getOrDefault(subtaskId, new PriorityBlockingQueue<>());
      MixedFormatSplit copiedSplit = split.copy();
      copiedSplit.modifyTreeNode(node);
      LOG.info("put split into queue: {}", copiedSplit);
      queue.add(copiedSplit);
      totalSplitNum = totalSplitNum + 1;
      subtaskSplitMap.put(subtaskId, queue);
    }
  }

  @Override
  public Collection<MixedFormatSplitState> state() {
    List<MixedFormatSplitState> mixedFormatSplitStates = new ArrayList<>();
    subtaskSplitMap.forEach(
        (key, value) ->
            mixedFormatSplitStates.addAll(
                value.stream().map(MixedFormatSplitState::new).collect(Collectors.toList())));

    return mixedFormatSplitStates;
  }

  @Override
  public synchronized CompletableFuture<Void> isAvailable() {
    if (availableFuture == null) {
      availableFuture = new CompletableFuture<>();
    }
    return availableFuture;
  }

  public boolean isEmpty() {
    if (subtaskSplitMap.isEmpty()) {
      return true;
    }
    for (Map.Entry<Integer, PriorityBlockingQueue<MixedFormatSplit>> entry :
        subtaskSplitMap.entrySet()) {
      if (!entry.getValue().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void close() throws IOException {
    subtaskSplitMap.clear();
    partitionIndexSubtaskMap.clear();
  }

  public long[] serializePartitionIndex() {
    int prefixParams = 3;
    long[] shuffleSplitRelation = new long[partitionIndexSubtaskMap.size() * 2 + prefixParams];
    shuffleSplitRelation[0] = totalParallelism;
    shuffleSplitRelation[1] = totalSplitNum;
    shuffleSplitRelation[2] = currentMaskOfTreeNode == null ? -1 : currentMaskOfTreeNode;

    int i = prefixParams;
    for (Map.Entry<Long, Integer> entry : partitionIndexSubtaskMap.entrySet()) {
      shuffleSplitRelation[i++] = entry.getKey();
      shuffleSplitRelation[i++] = entry.getValue();
    }
    return shuffleSplitRelation;
  }

  void deserializePartitionIndex(long[] shuffleSplitRelation) {
    int prefixParams = 3;
    this.totalParallelism = (int) shuffleSplitRelation[0];
    this.totalSplitNum = (int) shuffleSplitRelation[1];
    this.currentMaskOfTreeNode = shuffleSplitRelation[2] == -1 ? null : shuffleSplitRelation[2];

    for (int i = prefixParams; i < shuffleSplitRelation.length; i++) {
      partitionIndexSubtaskMap.put(shuffleSplitRelation[i], (int) shuffleSplitRelation[++i]);
    }
  }

  /**
   * Different data files may locate in different layers when multi snapshots are committed, so
   * mixed-format source reading should consider emitting the records and keeping ordering.
   * According to the dataTreeNode of the mixed-format split and the currentMaskOfTreeNode, return
   * the exact tree node list which may move up or go down layers in the mixed-format tree.
   *
   * <pre>
   * |mask=0          o
   * |             /     \
   * |mask=1     o        o
   * |         /   \    /   \
   * |mask=3  o     o  o     o
   * </pre>
   *
   * @param mixedFormatSplit Mixed-format split.
   * @return The exact tree node list.
   */
  public List<DataTreeNode> getExactlyTreeNodes(MixedFormatSplit mixedFormatSplit) {
    DataTreeNode dataTreeNode = mixedFormatSplit.dataTreeNode();
    long mask = dataTreeNode.mask();

    synchronized (lock) {
      if (currentMaskOfTreeNode == null) {
        currentMaskOfTreeNode = mask;
      }
    }

    return scanTreeNode(dataTreeNode);
  }

  private List<DataTreeNode> scanTreeNode(DataTreeNode dataTreeNode) {
    long mask = dataTreeNode.mask();
    if (mask == currentMaskOfTreeNode) {
      return Collections.singletonList(dataTreeNode);
    } else if (mask > currentMaskOfTreeNode) {
      // move up one layer
      return scanTreeNode(dataTreeNode.parent());
    } else {
      // go down one layer
      List<DataTreeNode> allNodes = new ArrayList<>();
      allNodes.addAll(scanTreeNode(dataTreeNode.left()));
      allNodes.addAll(scanTreeNode(dataTreeNode.right()));
      return allNodes;
    }
  }

  /**
   * In one mixed-format split, the partitions, mask and index of the files are the same.
   *
   * @param mixedFormatSplit mixed-format source split
   * @return anyone primary keyed file in the mixed-format split.
   */
  private PrimaryKeyedFile findAnyFileInSplit(MixedFormatSplit mixedFormatSplit) {
    AtomicReference<PrimaryKeyedFile> file = new AtomicReference<>();
    if (mixedFormatSplit.isChangelogSplit()) {
      List<MixedFileScanTask> mixedFileScanTasks =
          new ArrayList<>(mixedFormatSplit.asChangelogSplit().insertTasks());
      mixedFileScanTasks.addAll(mixedFormatSplit.asChangelogSplit().deleteTasks());
      mixedFileScanTasks.stream().findFirst().ifPresent(task -> file.set(task.file()));
      if (file.get() != null) {
        return file.get();
      }
    }

    List<MixedFileScanTask> mixedFileScanTasks =
        new ArrayList<>(mixedFormatSplit.asSnapshotSplit().insertTasks());
    mixedFileScanTasks.stream().findFirst().ifPresent(task -> file.set(task.file()));
    if (file.get() != null) {
      return file.get();
    }
    throw new FlinkRuntimeException("Couldn't find a primaryKeyedFile.");
  }

  private synchronized void completeAvailableFuturesIfNeeded() {
    if (availableFuture != null && !isEmpty()) {
      availableFuture.complete(null);
    }
    availableFuture = null;
  }
}

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

package com.netease.arctic.flink.read.hybrid.assigner;

import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.flink.read.hybrid.split.ArcticSplit;
import com.netease.arctic.flink.read.hybrid.split.ArcticSplitState;
import com.netease.arctic.scan.ArcticFileScanTask;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.util.FlinkRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * According to Mark,Index TreeNodes and subtaskId assigning a split to special subtask to read.
 */
public class ShuffleSplitAssigner implements SplitAssigner {
  private static final Logger LOG = LoggerFactory.getLogger(ShuffleSplitAssigner.class);

  private final SplitEnumeratorContext<ArcticSplit> enumeratorContext;

  private int totalParallelism;
  private int totalSplitNum;
  private Long currentMaskOfTreeNode;
  private final Object lock = new Object();

  /**
   * Key is the partition data and file index of the arctic file, Value is flink application subtaskId.
   */
  private final Map<Long, Integer> partitionIndexSubtaskMap;
  /**
   * Key is subtaskId, Value is the queue of unAssigned arctic splits.
   */
  private final Map<Integer, Queue<ArcticSplit>> subtaskSplitMap;


  public ShuffleSplitAssigner(
      SplitEnumeratorContext<ArcticSplit> enumeratorContext) {
    this.enumeratorContext = enumeratorContext;
    this.totalParallelism = enumeratorContext.currentParallelism();
    this.partitionIndexSubtaskMap = new ConcurrentHashMap<>();
    this.subtaskSplitMap = new ConcurrentHashMap<>();
  }

  public ShuffleSplitAssigner(
      SplitEnumeratorContext<ArcticSplit> enumeratorContext, Collection<ArcticSplitState> splitStates,
      long[] shuffleSplitRelation) {
    this.enumeratorContext = enumeratorContext;
    this.partitionIndexSubtaskMap = new ConcurrentHashMap<>();
    this.subtaskSplitMap = new ConcurrentHashMap<>();
    deserializePartitionIndex(shuffleSplitRelation);
    splitStates.forEach(state -> onDiscoveredSplits(Collections.singleton(state.toSourceSplit())));
  }

  @Override
  public Optional<ArcticSplit> getNext() {
    throw new UnsupportedOperationException("ShuffleSplitAssigner couldn't support this operation.");
  }

  @Override
  public Optional<ArcticSplit> getNext(int subTaskId) {
    int currentParallelism = enumeratorContext.currentParallelism();
    if (totalParallelism != currentParallelism) {
      throw new FlinkRuntimeException(
          String.format(
              "Source parallelism has been changed, before parallelism is %s, now is %s",
              totalParallelism, currentParallelism));
    }
    if (subtaskSplitMap.containsKey(subTaskId)) {
      Queue<ArcticSplit> queue = subtaskSplitMap.get(subTaskId);
      ArcticSplit arcticSplit = queue.poll();
      if (arcticSplit == null) {
        LOG.debug("Subtask {}, couldn't retrieve arctic source split in the queue.", subTaskId);
        return Optional.empty();
      } else {
        LOG.info("get next arctic split taskIndex {}, totalSplitNum {}, arcticSplit {}.",
            arcticSplit.taskIndex(), totalSplitNum, arcticSplit);
        return Optional.of(arcticSplit);
      }
    } else {
      LOG.info("Subtask {}, it's an idle subtask due to the empty queue with this subtask.", subTaskId);
      return Optional.empty();
    }
  }

  @Override
  public void onDiscoveredSplits(Collection<ArcticSplit> splits) {
    splits.forEach(this::putArcticIntoQueue);
    totalSplitNum += splits.size();
  }

  @Override
  public void onUnassignedSplits(Collection<ArcticSplit> splits) {
    onDiscoveredSplits(splits);
  }

  void putArcticIntoQueue(ArcticSplit split) {
    int subtaskId = getSubtaskIdByArcticSplit(split);
    Queue<ArcticSplit> queue = subtaskSplitMap.getOrDefault(subtaskId, new PriorityQueue<>());
    queue.add(split);
    subtaskSplitMap.put(subtaskId, queue);
  }

  private int getSubtaskIdByArcticSplit(ArcticSplit arcticSplit) {
    PrimaryKeyedFile file = findAnyFileInArcticSplit(arcticSplit);
    long partitionIndexKey = partitionAndIndexHashCode(
        file.partition().toString(), arcticSplit);

    int subtaskId = partitionIndexSubtaskMap.computeIfAbsent(
        partitionIndexKey, key -> (partitionIndexSubtaskMap.size() + 1) % totalParallelism);
    LOG.info("partition = {}, index = {}, subtaskId = {}", file.partition().toString(), file.node().index(), subtaskId);
    return subtaskId;
  }

  @Override
  public Collection<ArcticSplitState> state() {
    List<ArcticSplitState> arcticSplitStates = new ArrayList<>();
    subtaskSplitMap.forEach((key, value) ->
        arcticSplitStates.addAll(
            value.stream()
                .map(ArcticSplitState::new)
                .collect(Collectors.toList())));

    return arcticSplitStates;
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

  private long partitionAndIndexHashCode(String partition, ArcticSplit arcticSplit) {
    return Math.abs(partition.hashCode() + getExactlyIndexOfTreeNode(arcticSplit));
  }

  @VisibleForTesting
  public long getExactlyIndexOfTreeNode(ArcticSplit arcticSplit) {
    DataTreeNode dataTreeNode = arcticSplit.dataTreeNode();
    long index = dataTreeNode.index();
    long mask = dataTreeNode.mask();

    synchronized (lock) {
      if (currentMaskOfTreeNode == null) {
        currentMaskOfTreeNode = mask;
      }
    }

    boolean modifyDataTreeNode = mask != currentMaskOfTreeNode;
    boolean greaterThanCurrent = mask > currentMaskOfTreeNode;
    ++mask;
    while (mask != currentMaskOfTreeNode + 1) {
      if (greaterThanCurrent) {
        mask = mask >> 1;
        index = index >> 1;
      } else {
        mask = mask << 1;
        index = index << 1;
      }
    }

    // Have to modify the dataTreeNode of the arcticSplit due to the mask of this split is diff from the current
    // mask assigned to source readers.
    if (modifyDataTreeNode) {
      DataTreeNode expectedNode = DataTreeNode.of(currentMaskOfTreeNode, index);
      LOG.info("original dataTreeNode is {}, new dataTreeNode is {}.", dataTreeNode, expectedNode);
      arcticSplit.modifyTreeNode(expectedNode);
    }
    return index;
  }

  /**
   * In one arctic split, the partitions, mask and index of the files are the same.
   *
   * @param arcticSplit arctic source split
   * @return anyone primary keyed file in the arcticSplit.
   */
  private PrimaryKeyedFile findAnyFileInArcticSplit(ArcticSplit arcticSplit) {
    AtomicReference<PrimaryKeyedFile> file = new AtomicReference<>();
    if (arcticSplit.isChangelogSplit()) {
      List<ArcticFileScanTask> arcticSplits = new ArrayList<>(arcticSplit.asChangelogSplit().insertTasks());
      arcticSplits.addAll(arcticSplit.asChangelogSplit().deleteTasks());
      arcticSplits.stream().findFirst().ifPresent(task -> file.set(task.file()));
      if (file.get() != null) {
        return file.get();
      }
    }

    List<ArcticFileScanTask> arcticSplits = new ArrayList<>(arcticSplit.asSnapshotSplit().insertTasks());
    arcticSplits.stream().findFirst().ifPresent(task -> file.set(task.file()));
    if (file.get() != null) {
      return file.get();
    }
    throw new FlinkRuntimeException("Couldn't find a primaryKeyedFile.");
  }
}

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

package org.apache.amoro.flink.read;

import org.apache.amoro.flink.read.hybrid.split.ChangelogSplit;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.scan.MixedFileScanTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a group of the partitions and nodes of the mixed-format table, it can plan different
 * nodes and different partitions into different {@link MixedFormatSplit}.
 */
public class PartitionAndNodeGroup {
  AtomicInteger splitCount = new AtomicInteger();
  Collection<MixedFileScanTask> insertTasks;
  Collection<MixedFileScanTask> deleteTasks;

  public PartitionAndNodeGroup insertFileScanTask(Set<MixedFileScanTask> insertTasks) {
    this.insertTasks = insertTasks;
    return this;
  }

  public PartitionAndNodeGroup deleteFileScanTask(Set<MixedFileScanTask> deleteTasks) {
    this.deleteTasks = deleteTasks;
    return this;
  }

  public PartitionAndNodeGroup splitCount(AtomicInteger splitCount) {
    this.splitCount = splitCount;
    return this;
  }

  List<MixedFormatSplit> planSplits() {
    Map<String, Map<Long, Node>> nodes = new HashMap<>();
    plan(true, nodes);
    plan(false, nodes);

    List<MixedFormatSplit> splits = new ArrayList<>();

    nodes
        .values()
        .forEach(
            indexNodes ->
                indexNodes
                    .values()
                    .forEach(
                        node ->
                            splits.add(
                                new ChangelogSplit(
                                    node.inserts, node.deletes, splitCount.incrementAndGet()))));
    return splits;
  }

  /**
   * Split the collection of {@link MixedFileScanTask} into different groups.
   *
   * @param insert if plan insert files or not
   * @param nodes the key of nodes is partition info which the file located, the value of nodes is
   *     hashmap of mixed-format tree node id and {@link Node}
   */
  private void plan(boolean insert, Map<String, Map<Long, Node>> nodes) {
    Collection<MixedFileScanTask> tasks = insert ? insertTasks : deleteTasks;
    if (tasks == null) {
      return;
    }

    tasks.forEach(
        task -> {
          String partitionKey = task.file().partition().toString();
          Long nodeId = task.file().node().getId();
          Map<Long, Node> indexNodes = nodes.getOrDefault(partitionKey, new HashMap<>());
          Node node = indexNodes.getOrDefault(nodeId, new Node());
          if (insert) {
            node.addInsert(task);
          } else {
            node.addDelete(task);
          }
          indexNodes.put(nodeId, node);
          nodes.put(partitionKey, indexNodes);
        });
  }

  private static class Node {
    List<MixedFileScanTask> inserts = new ArrayList<>(1);
    List<MixedFileScanTask> deletes = new ArrayList<>(1);

    void addInsert(MixedFileScanTask task) {
      inserts.add(task);
    }

    void addDelete(MixedFileScanTask task) {
      deletes.add(task);
    }
  }
}

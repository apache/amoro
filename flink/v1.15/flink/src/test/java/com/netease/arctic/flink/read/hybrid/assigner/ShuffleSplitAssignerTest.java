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
import com.netease.arctic.flink.read.FlinkSplitPlanner;
import com.netease.arctic.flink.read.hybrid.reader.RowDataReaderFunctionTest;
import com.netease.arctic.flink.read.hybrid.split.ArcticSplit;
import org.apache.flink.api.connector.source.ReaderInfo;
import org.apache.flink.api.connector.source.SourceEvent;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.api.connector.source.SplitsAssignment;
import org.apache.flink.metrics.groups.SplitEnumeratorMetricGroup;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class ShuffleSplitAssignerTest extends RowDataReaderFunctionTest {
  private static final Logger LOG = LoggerFactory.getLogger(ShuffleSplitAssignerTest.class);

  @Test
  public void testSingleParallelism() {
    ShuffleSplitAssigner shuffleSplitAssigner = instanceSplitAssigner(1);

    List<ArcticSplit> splitList = FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger());
    shuffleSplitAssigner.onDiscoveredSplits(splitList);
    List<ArcticSplit> actual = new ArrayList<>();

    while (true) {
      Optional<ArcticSplit> splitOpt = shuffleSplitAssigner.getNext(0);
      if (splitOpt.isPresent()) {
        actual.add(splitOpt.get());
      } else {
        break;
      }
    }

    Assert.assertEquals(splitList.size(), actual.size());
  }

  @Test
  public void testMultiParallelism() {
    ShuffleSplitAssigner shuffleSplitAssigner = instanceSplitAssigner(3);

    List<ArcticSplit> splitList = FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger());
    shuffleSplitAssigner.onDiscoveredSplits(splitList);
    List<ArcticSplit> actual = new ArrayList<>();

    int subtaskId = 2;
    while (subtaskId >= 0) {
      Optional<ArcticSplit> splitOpt = shuffleSplitAssigner.getNext(subtaskId);
      if (splitOpt.isPresent()) {
        actual.add(splitOpt.get());
      } else {
        LOG.info("subtask id {}, splits {}.\n {}", subtaskId, actual.size(), actual);
        --subtaskId;
      }
    }

    Assert.assertEquals(splitList.size(), actual.size());
  }

  @Test
  public void testTreeNodeMaskUpdate() {
    ShuffleSplitAssigner shuffleSplitAssigner = instanceSplitAssigner(3);
    long[][] treeNodes = new long[][] {{3, 0}, {3, 1}, {3, 2}, {3, 3}, {7, 0}, {7, 1}, {7, 2}, {7, 3}, {7, 4},
                                       {1, 0}, {1, 1}, {0, 0}, {7, 7}, {15, 15}};
    List<Long> actual = new ArrayList<>();

    for (long[] node : treeNodes) {
      ArcticSplit arcticSplit = new ArcticSplit() {
        DataTreeNode dataTreeNode = DataTreeNode.of(node[0], node[1]);

        @Override
        public Integer taskIndex() {
          return null;
        }

        @Override
        public void updateOffset(Object[] recordOffsets) {
        }

        @Override
        public DataTreeNode dataTreeNode() {
          return this.dataTreeNode;
        }

        @Override
        public void modifyTreeNode(DataTreeNode expected) {
          this.dataTreeNode = expected;
        }

        @Override
        public String splitId() {
          return null;
        }

        @Override
        public String toString() {
          return dataTreeNode.toString();
        }
      };
      LOG.info("before split {}.", arcticSplit);
      long index = shuffleSplitAssigner.getExactlyIndexOfTreeNode(arcticSplit);
      LOG.info("after split {}.", arcticSplit);
      actual.add(index);
    }
    long[] result = actual.stream().mapToLong(l -> l).toArray();
    long[] expect = new long[] {0, 1, 2, 3, 0, 0, 1, 1, 2, 0, 2, 0, 3, 3};

    Assert.assertArrayEquals(expect, result);
  }

  protected ShuffleSplitAssigner instanceSplitAssigner(int parallelism) {
    SplitEnumeratorContext<ArcticSplit> splitEnumeratorContext = new InternalSplitEnumeratorContext(parallelism);
    return new ShuffleSplitAssigner(splitEnumeratorContext);
  }

  protected static class InternalSplitEnumeratorContext implements SplitEnumeratorContext<ArcticSplit> {
    private final int parallelism;

    public InternalSplitEnumeratorContext(int parallelism) {
      this.parallelism = parallelism;
    }

    @Override
    public SplitEnumeratorMetricGroup metricGroup() {
      return null;
    }

    @Override
    public void sendEventToSourceReader(int subtaskId, SourceEvent event) {

    }

    @Override
    public int currentParallelism() {
      return parallelism;
    }

    @Override
    public Map<Integer, ReaderInfo> registeredReaders() {
      return null;
    }

    @Override
    public void assignSplits(SplitsAssignment<ArcticSplit> newSplitAssignments) {

    }

    @Override
    public void signalNoMoreSplits(int subtask) {

    }

    @Override
    public <T> void callAsync(Callable<T> callable, BiConsumer<T, Throwable> handler) {

    }

    @Override
    public <T> void callAsync(Callable<T> callable, BiConsumer<T, Throwable> handler, long initialDelay, long period) {

    }

    @Override
    public void runInCoordinatorThread(Runnable runnable) {

    }
  }
}
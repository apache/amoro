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

import org.apache.amoro.flink.read.FlinkSplitPlanner;
import org.apache.amoro.flink.read.hybrid.assigner.ShuffleSplitAssigner;
import org.apache.amoro.flink.read.hybrid.assigner.Split;
import org.apache.amoro.flink.read.hybrid.assigner.TestShuffleSplitAssigner;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.hybrid.split.TemporalJoinSplits;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMixedFormatSourceEnumStateSerializer extends TestShuffleSplitAssigner {
  private static final Logger LOG =
      LoggerFactory.getLogger(TestMixedFormatSourceEnumStateSerializer.class);

  @Test
  public void testMixedFormatEnumState() throws IOException {
    ShuffleSplitAssigner shuffleSplitAssigner = instanceSplitAssigner(3);

    List<MixedFormatSplit> splitList =
        FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger());
    shuffleSplitAssigner.onDiscoveredSplits(splitList);
    TemporalJoinSplits splits = new TemporalJoinSplits(splitList, null);

    MixedFormatSourceEnumState expect =
        new MixedFormatSourceEnumState(
            shuffleSplitAssigner.state(),
            null,
            shuffleSplitAssigner.serializePartitionIndex(),
            splits);

    MixedFormatSourceEnumStateSerializer mixedFormatSourceEnumStateSerializer =
        new MixedFormatSourceEnumStateSerializer();
    byte[] ser = mixedFormatSourceEnumStateSerializer.serialize(expect);

    Assert.assertNotNull(ser);

    MixedFormatSourceEnumState actual = mixedFormatSourceEnumStateSerializer.deserialize(1, ser);

    Assert.assertEquals(expect.pendingSplits().size(), actual.pendingSplits().size());
    Assert.assertEquals(
        Objects.requireNonNull(expect.shuffleSplitRelation()).length,
        Objects.requireNonNull(actual.shuffleSplitRelation()).length);

    SplitEnumeratorContext<MixedFormatSplit> splitEnumeratorContext =
        new InternalSplitEnumeratorContext(3);
    try (ShuffleSplitAssigner actualAssigner =
        new ShuffleSplitAssigner(splitEnumeratorContext, getMixedTable().name(), actual)) {
      List<MixedFormatSplit> actualSplits = new ArrayList<>();

      int subtaskId = 2;
      while (subtaskId >= 0) {
        Split splitOpt = actualAssigner.getNext(subtaskId);
        if (splitOpt.isAvailable()) {
          actualSplits.add(splitOpt.split());
        } else {
          LOG.info("subtask id {}, splits {}.\n {}", subtaskId, actualSplits.size(), actualSplits);
          --subtaskId;
        }
      }

      Assert.assertEquals(splitList.size(), actualSplits.size());

      TemporalJoinSplits temporalJoinSplits = actual.temporalJoinSplits();
      Assert.assertEquals(expect.temporalJoinSplits(), temporalJoinSplits);
    }
  }
}

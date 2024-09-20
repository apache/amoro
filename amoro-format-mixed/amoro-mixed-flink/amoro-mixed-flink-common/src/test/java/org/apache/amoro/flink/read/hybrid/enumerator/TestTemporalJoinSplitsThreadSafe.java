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

import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.hybrid.split.TemporalJoinSplits;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TestTemporalJoinSplitsThreadSafe {

  @Test
  public void testTemporalJoinSplits() {
    List<String> allSplit = new LinkedList<>();
    for (int i = 0; i < 100; i++) {
      allSplit.add(UUID.randomUUID().toString());
    }

    Collection<MixedFormatSplit> mixedFormatSplits =
        allSplit.stream().map(TestMixedFormatSplit::of).collect(Collectors.toList());

    for (int i = 0; i < 2; i++) {
      round(allSplit, mixedFormatSplits);
    }
  }

  public void round(List<String> allSplit, Collection<MixedFormatSplit> mixedFormatSplits) {
    TemporalJoinSplits temporalJoinSplits = new TemporalJoinSplits(mixedFormatSplits, null);
    int n = allSplit.size();

    List<String> s1 = new ArrayList<>(allSplit.subList(0, (int) (2.0 / 3 * n))),
        s2 = new ArrayList<>(allSplit.subList((int) (1.0 / 3 * n), n));
    Collections.shuffle(s1);
    Collections.shuffle(s2);

    List<MixedFormatSplit> as = new ArrayList<>(mixedFormatSplits);
    Collections.shuffle(as);
    int an = as.size();
    List<MixedFormatSplit> as1 = new ArrayList<>(as.subList(0, (int) (2.0 / 3 * an)));
    List<MixedFormatSplit> as2 = new ArrayList<>(as.subList((int) (1.0 / 3 * an), an));
    CompletableFuture<Void> f1 =
        CompletableFuture.runAsync(() -> temporalJoinSplits.removeAndReturnIfAllFinished(s1));
    CompletableFuture<Void> f2 =
        CompletableFuture.runAsync(() -> temporalJoinSplits.addSplitsBack(as1));
    CompletableFuture<Void> f3 =
        CompletableFuture.runAsync(() -> temporalJoinSplits.removeAndReturnIfAllFinished(s2));
    CompletableFuture<Void> f4 =
        CompletableFuture.runAsync(() -> temporalJoinSplits.addSplitsBack(as2));
    CompletableFuture.allOf(f1, f2, f3, f4).join();
    Assert.assertTrue(temporalJoinSplits.removeAndReturnIfAllFinished(allSplit));
  }

  static class TestMixedFormatSplit extends MixedFormatSplit {
    private final String splitId;

    public TestMixedFormatSplit(String splitId) {
      this.splitId = splitId;
    }

    public static TestMixedFormatSplit of(String splitId) {
      return new TestMixedFormatSplit(splitId);
    }

    @Override
    public Integer taskIndex() {
      return null;
    }

    @Override
    public void updateOffset(Object[] recordOffsets) {}

    @Override
    public MixedFormatSplit copy() {
      return new TestMixedFormatSplit(splitId);
    }

    @Override
    public String splitId() {
      return splitId;
    }
  }
}

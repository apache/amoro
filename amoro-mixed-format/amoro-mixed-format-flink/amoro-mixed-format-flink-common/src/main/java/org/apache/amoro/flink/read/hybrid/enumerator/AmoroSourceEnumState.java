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

import org.apache.amoro.flink.read.hybrid.split.AmoroSplitState;
import org.apache.amoro.flink.read.hybrid.split.TemporalJoinSplits;

import javax.annotation.Nullable;

import java.util.Collection;

/**
 * State that contains pending arctic splits and last enumerator offset in arctic source enumerator
 * {@link AmoroSourceEnumerator}.
 */
public class AmoroSourceEnumState {
  @Nullable private final AmoroEnumeratorOffset lastEnumeratedOffset;
  private final Collection<AmoroSplitState> pendingSplits;
  @Nullable private final long[] shuffleSplitRelation;
  @Nullable private final TemporalJoinSplits temporalJoinSplits;

  public AmoroSourceEnumState(
      Collection<AmoroSplitState> pendingSplits,
      @Nullable AmoroEnumeratorOffset lastEnumeratedOffset,
      @Nullable long[] shuffleSplitRelation,
      @Nullable TemporalJoinSplits temporalJoinSplits) {
    this.pendingSplits = pendingSplits;
    this.lastEnumeratedOffset = lastEnumeratedOffset;
    this.shuffleSplitRelation = shuffleSplitRelation;
    this.temporalJoinSplits = temporalJoinSplits;
  }

  @Nullable
  public AmoroEnumeratorOffset lastEnumeratedOffset() {
    return lastEnumeratedOffset;
  }

  public Collection<AmoroSplitState> pendingSplits() {
    return pendingSplits;
  }

  @Nullable
  public long[] shuffleSplitRelation() {
    return shuffleSplitRelation;
  }

  @Nullable
  public TemporalJoinSplits temporalJoinSplits() {
    return temporalJoinSplits;
  }
}

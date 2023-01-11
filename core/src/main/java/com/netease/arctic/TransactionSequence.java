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

package com.netease.arctic;

import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Define the sequence number of Transaction for {@link KeyedTable}
 */
public class TransactionSequence implements Comparable<TransactionSequence> {

  public static long VOID_SEQUENCE_NUMBER = -1L;

  private final long changeSequence;
  private final long baseSequence;

  public TransactionSequence(long changeSequence, long baseSequence) {
    Preconditions.checkArgument(changeSequence >= 0, "changeSequence must >= 0");
    Preconditions.checkArgument(baseSequence >= 0 || baseSequence == VOID_SEQUENCE_NUMBER, "baseSequence must >= -1");
    this.changeSequence = changeSequence;
    this.baseSequence = baseSequence;
  }

  public static TransactionSequence of(long changeSequence, long baseSequence) {
    return new TransactionSequence(changeSequence, baseSequence);
  }

  public static TransactionSequence of(long changeSequence) {
    return new TransactionSequence(changeSequence, VOID_SEQUENCE_NUMBER);
  }

  public long getChangeSequence() {
    return changeSequence;
  }

  public long getBaseSequence() {
    return baseSequence;
  }

  @Override
  public int compareTo(@NotNull TransactionSequence o) {
    if (this.changeSequence == o.changeSequence) {
      return Long.compare(this.baseSequence, o.baseSequence);
    } else {
      return Long.compare(this.changeSequence, o.changeSequence);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TransactionSequence that = (TransactionSequence) o;
    return changeSequence == that.changeSequence && baseSequence == that.baseSequence;
  }

  @Override
  public int hashCode() {
    return Objects.hash(changeSequence, baseSequence);
  }

  @Override
  public String toString() {
    return String.format("TransactionSequence(%s,%s)", changeSequence, baseSequence);
  }
}

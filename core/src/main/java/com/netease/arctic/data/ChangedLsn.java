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

package com.netease.arctic.data;

import com.netease.arctic.TransactionSequence;

import java.io.Serializable;
import java.util.Objects;


/**
 * Global row sequence number.
 * <p>
 * Consist of two parts:
 * <ul>
 *   <li>id of transaction row written into the table
 *   <li>row sequence within the transaction
 * </ul>
 */

public class ChangedLsn implements Comparable<ChangedLsn>, Serializable {

  private final TransactionSequence transactionSequence;
  private final long fileOffset;

  public static ChangedLsn of(TransactionSequence transactionSequence, long fileOffset) {
    return new ChangedLsn(transactionSequence, fileOffset);
  }

  // TODO remove
  public static ChangedLsn of(long txId, long fileOffset) {
    return new ChangedLsn(TransactionSequence.of(txId), fileOffset);
  }

  public static ChangedLsn of(byte[] bytes) {
    // TODO fix
    return of(TransactionSequence.of(((long) bytes[15] << 56) |
                    ((long) bytes[14] & 0xff) << 48 |
                    ((long) bytes[13] & 0xff) << 40 |
                    ((long) bytes[12] & 0xff) << 32 |
                    ((long) bytes[11] & 0xff) << 24 |
                    ((long) bytes[10] & 0xff) << 16 |
                    ((long) bytes[9] & 0xff) << 8 |
                    ((long) bytes[8] & 0xff)),
            ((long) bytes[7] << 56) |
                    ((long) bytes[6] & 0xff) << 48 |
                    ((long) bytes[5] & 0xff) << 40 |
                    ((long) bytes[4] & 0xff) << 32 |
                    ((long) bytes[3] & 0xff) << 24 |
                    ((long) bytes[2] & 0xff) << 16 |
                    ((long) bytes[1] & 0xff) << 8 |
                    ((long) bytes[0] & 0xff));
  }

  private ChangedLsn(TransactionSequence transactionSequence, long fileOffset) {
    this.transactionSequence = transactionSequence;
    this.fileOffset = fileOffset;
  }

  public TransactionSequence transactionSequence() {
    return transactionSequence;
  }

  public long fileOffset() {
    return fileOffset;
  }


  @Override
  public int compareTo(ChangedLsn another) {
    int compare = this.transactionSequence.compareTo(another.transactionSequence);
    if (compare != 0) {
      return compare;
    } else {
      if (fileOffset > another.fileOffset()) {
        return 1;
      } else if (fileOffset < another.fileOffset) {
        return -1;
      } else {
        return 0;
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChangedLsn recordLsn = (ChangedLsn) o;
    return transactionSequence == recordLsn.transactionSequence &&
            fileOffset == recordLsn.fileOffset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionSequence, fileOffset);
  }

  @Override
  public String toString() {
    return new StringBuilder("RecordLsn(").append(transactionSequence)
            .append(", ").append(fileOffset).append(")").toString();
  }

  public byte[] toBytes() {
    // TODO fix
    return new byte[] {
        (byte) transactionSequence.getChangeSequence(),
        (byte) (transactionSequence.getChangeSequence() >> 8),
        (byte) (transactionSequence.getChangeSequence() >> 16),
        (byte) (transactionSequence.getChangeSequence() >> 24),
        (byte) (transactionSequence.getChangeSequence() >> 32),
        (byte) (transactionSequence.getChangeSequence() >> 40),
        (byte) (transactionSequence.getChangeSequence() >> 48),
        (byte) (transactionSequence.getChangeSequence() >> 56),
        (byte) fileOffset,
        (byte) (fileOffset >> 8),
        (byte) (fileOffset >> 16),
        (byte) (fileOffset >> 24),
        (byte) (fileOffset >> 32),
        (byte) (fileOffset >> 40),
        (byte) (fileOffset >> 48),
        (byte) (fileOffset >> 56)
    };
  }
}

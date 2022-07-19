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

import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @TODO rename to GlobalRowSequence
 * Global row sequence number.
 * <p>
 * Consist of two parts:
 * <ul>
 *   <li>id of transaction row written into the table
 *   <li>row sequence within the transaction
 * </ul>
 */

public class ChangedLsn implements Comparable<ChangedLsn>, Serializable {

  public static final int RECORD_LSN_LENGTH = Long.BYTES * 2;
  public static final long minimumLsn = 0L;

  private final long transactionId;
  //TODO rename to rowSequence
  private final long fileOffset;

  public static ChangedLsn of(long transactionId, long fileOffset) {
    return new ChangedLsn(transactionId, fileOffset);
  }

  public static ChangedLsn from(ByteBuffer byteBuffer) {
    Preconditions.checkArgument(byteBuffer.remaining() == RECORD_LSN_LENGTH);
    ChangedLsn recordLsn = ChangedLsn.of(byteBuffer.getLong(), byteBuffer.getLong());
    byteBuffer.rewind();
    return recordLsn;
  }

  /**
   * TODO deleted
   * @return minimum lsn, like base file data lsn
   */
  public static ChangedLsn minimum() {
    return new ChangedLsn(minimumLsn, minimumLsn);
  }

  private ChangedLsn(long transactionId, long fileOffset) {
    this.transactionId = transactionId;
    this.fileOffset = fileOffset;
  }

  public long transactionId() {
    return transactionId;
  }

  public long fileOffset() {
    return fileOffset;
  }

  public ByteBuffer byteBuffer() {
    return ByteBuffer.allocate(RECORD_LSN_LENGTH).putLong(transactionId).putLong(fileOffset);
  }

  @Override
  public int compareTo(ChangedLsn another) {
    if (transactionId > another.transactionId()) {
      return 1;
    } else if (transactionId < another.transactionId) {
      return -1;
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
    return transactionId == recordLsn.transactionId &&
        fileOffset == recordLsn.fileOffset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionId, fileOffset);
  }

  @Override
  public String toString() {
    return new StringBuilder("RecordLsn(").append(transactionId)
        .append(", ").append(fileOffset).append(")").toString();
  }
}

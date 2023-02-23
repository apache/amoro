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

package com.netease.arctic.ams.server.model;

/**
 * Files grouped by snapshot, but only with the file cnt.
 */
public class SnapshotFileGroup implements Comparable<SnapshotFileGroup> {
  private final long sequence;
  private final long transactionId;
  private int fileCnt = 0;

  public SnapshotFileGroup(long sequence, long transactionId) {
    this.sequence = sequence;
    this.transactionId = transactionId;
  }

  public SnapshotFileGroup(long sequence, long transactionId, int fileCnt) {
    this.sequence = sequence;
    this.transactionId = transactionId;
    this.fileCnt = fileCnt;
  }

  public void addFile() {
    fileCnt++;
  }

  public long getTransactionId() {
    return transactionId;
  }

  public int getFileCnt() {
    return fileCnt;
  }

  public long getSequence() {
    return sequence;
  }

  @Override
  public int compareTo(SnapshotFileGroup o) {
    return Long.compare(this.sequence, o.sequence);
  }
}

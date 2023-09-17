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

package com.netease.arctic.flink.read.hybrid.split;

import com.netease.arctic.scan.KeyedTableScanTask;
import com.netease.arctic.utils.FileScanTaskUtil;
import org.apache.flink.util.Preconditions;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;

public class MergeOnReadSplit extends ArcticSplit {
  private static final long serialVersionUID = 1L;
  private final int taskIndex;
  private final KeyedTableScanTask keyedTableScanTask;
  private long recordOffset;

  public MergeOnReadSplit(int taskIndex, KeyedTableScanTask keyedTableScanTask) {
    this.taskIndex = taskIndex;
    this.keyedTableScanTask = keyedTableScanTask;
  }

  public KeyedTableScanTask keyedTableScanTask() {
    return keyedTableScanTask;
  }

  @Override
  public Integer taskIndex() {
    return taskIndex;
  }

  @Override
  public void updateOffset(Object[] offsets) {
    Preconditions.checkArgument(offsets.length == 2);
    // offsets[0] is file offset, but we don't need it
    recordOffset = (long) offsets[1];
  }

  @Override
  public ArcticSplit copy() {
    return new MergeOnReadSplit(taskIndex, keyedTableScanTask);
  }

  @Override
  public String splitId() {
    return MoreObjects.toStringHelper(this)
        .add("insertTasks", FileScanTaskUtil.toString(keyedTableScanTask.insertTasks()))
        .add("baseTasks", FileScanTaskUtil.toString(keyedTableScanTask.baseTasks()))
        .add("arcticEquityDeletes", FileScanTaskUtil.toString(keyedTableScanTask.arcticEquityDeletes()))
        .toString();
  }

  public long recordOffset() {
    return recordOffset;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MergeOnReadSplit)) {
      return false;
    }
    MergeOnReadSplit other = (MergeOnReadSplit) obj;
    return splitId().equals(other.splitId()) &&
        recordOffset == other.recordOffset &&
        taskIndex == other.taskIndex;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("\ninsertTasks", FileScanTaskUtil.toString(keyedTableScanTask.insertTasks()))
        .add("\nbaseTasks", FileScanTaskUtil.toString(keyedTableScanTask.baseTasks()))
        .add("\narcticEquityDeletes", FileScanTaskUtil.toString(keyedTableScanTask.arcticEquityDeletes()))
        .add("\ncost", keyedTableScanTask.cost() / 1024 + " KB")
        .add("\nrecordCount", keyedTableScanTask.recordCount())
        .toString();
  }
}

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

package org.apache.amoro.flink.read.hybrid.split;

import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.utils.FileScanTaskUtil;

import java.util.Collection;
import java.util.Optional;

/** A changelog split generated during planning change table. */
public class ChangelogSplit extends MixedFormatSplit {
  private static final long serialVersionUID = 1L;
  private final int taskIndex;
  private final Collection<MixedFileScanTask> insertScanTasks;
  private final Collection<MixedFileScanTask> deleteScanTasks;
  private int insertFileOffset;
  private long insertRecordOffset;
  private int deleteFileOffset;
  private long deleteRecordOffset;
  private DataTreeNode dataTreeNode;

  public ChangelogSplit(
      Collection<MixedFileScanTask> insertScanTasks,
      Collection<MixedFileScanTask> deleteScanTasks,
      int taskIndex) {
    Preconditions.checkArgument(insertScanTasks.size() > 0 || deleteScanTasks.size() > 0);
    this.taskIndex = taskIndex;
    this.insertScanTasks = insertScanTasks;
    this.deleteScanTasks = deleteScanTasks;
    Optional<MixedFileScanTask> task = insertScanTasks.stream().findFirst();
    PrimaryKeyedFile file =
        task.isPresent() ? task.get().file() : deleteScanTasks.stream().findFirst().get().file();
    this.dataTreeNode = DataTreeNode.of(file.node().mask(), file.node().index());
  }

  @Override
  public Integer taskIndex() {
    return taskIndex;
  }

  @Override
  public DataTreeNode dataTreeNode() {
    return dataTreeNode;
  }

  @Override
  public void modifyTreeNode(DataTreeNode expectedNode) {
    Preconditions.checkNotNull(expectedNode);
    this.dataTreeNode = expectedNode;
  }

  @Override
  public void updateOffset(Object[] offsets) {
    Preconditions.checkArgument(offsets.length == 4);
    insertFileOffset = (int) offsets[0];
    insertRecordOffset = (long) offsets[1];
    deleteFileOffset = (int) offsets[2];
    deleteRecordOffset = (long) offsets[3];
  }

  @Override
  public MixedFormatSplit copy() {
    return new ChangelogSplit(insertScanTasks, deleteScanTasks, taskIndex);
  }

  @Override
  public String splitId() {
    return MoreObjects.toStringHelper(this)
        .add("insertTasks", FileScanTaskUtil.toString(insertScanTasks))
        .add("mixedFormatEquityDeletes", FileScanTaskUtil.toString(deleteScanTasks))
        .toString();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("insertTasks", FileScanTaskUtil.toString(insertScanTasks))
        .add("mixedFormatEquityDeletes", FileScanTaskUtil.toString(deleteScanTasks))
        .add("dataTreeNode", dataTreeNode.toString())
        .toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ChangelogSplit)) {
      return false;
    }
    ChangelogSplit other = (ChangelogSplit) obj;
    return splitId().equals(other.splitId())
        && insertFileOffset == other.insertFileOffset
        && insertRecordOffset == other.insertRecordOffset
        && deleteFileOffset == other.deleteFileOffset
        && deleteRecordOffset == other.deleteRecordOffset
        && taskIndex == other.taskIndex;
  }

  public int insertFileOffset() {
    return insertFileOffset;
  }

  public long insertRecordOffset() {
    return insertRecordOffset;
  }

  public int deleteFileOffset() {
    return deleteFileOffset;
  }

  public long deleteRecordOffset() {
    return deleteRecordOffset;
  }

  public Collection<MixedFileScanTask> insertTasks() {
    return insertScanTasks;
  }

  public Collection<MixedFileScanTask> deleteTasks() {
    return deleteScanTasks;
  }
}

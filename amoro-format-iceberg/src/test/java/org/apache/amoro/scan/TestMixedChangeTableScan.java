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

package org.apache.amoro.scan;

import org.apache.amoro.io.TableDataTestBase;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Predicate;

public class TestMixedChangeTableScan extends TableDataTestBase {

  @Test
  public void testIncrementalScan() throws IOException {
    ChangeTableIncrementalScan changeTableIncrementalScan =
        getMixedTable().asKeyedTable().changeTable().newScan();
    try (CloseableIterable<FileScanTask> tasks = changeTableIncrementalScan.planFiles()) {
      assertFilesSequence(tasks, 3, 1, 2);
    }
  }

  @Test
  public void testIncrementalScanFromPartitionSequence() throws IOException {
    StructLikeMap<Long> fromSequence = StructLikeMap.create(getMixedTable().spec().partitionType());
    StructLike partitionData =
        MixedDataFiles.data(getMixedTable().spec(), "op_time_day=2022-01-01");
    fromSequence.put(partitionData, 1L);
    ChangeTableIncrementalScan changeTableIncrementalScan =
        getMixedTable().asKeyedTable().changeTable().newScan().fromSequence(fromSequence);
    try (CloseableIterable<FileScanTask> tasks = changeTableIncrementalScan.planFiles()) {
      assertFilesSequence(tasks, 1, 2, 2);
    }
  }

  @Test
  public void testIncrementalScanFromSequence() throws IOException {
    ChangeTableIncrementalScan changeTableIncrementalScan =
        getMixedTable().asKeyedTable().changeTable().newScan().fromSequence(1L);
    try (CloseableIterable<FileScanTask> tasks = changeTableIncrementalScan.planFiles()) {
      assertFilesSequence(tasks, 1, 2, 2);
    }
  }

  @Test
  public void testIncrementalScanTo() throws IOException {
    ChangeTableIncrementalScan changeTableIncrementalScan =
        getMixedTable().asKeyedTable().changeTable().newScan().toSequence(1);
    try (CloseableIterable<FileScanTask> tasks = changeTableIncrementalScan.planFiles()) {
      assertFilesSequence(tasks, 2, 1, 1);
    }
  }

  @Test
  public void testIncrementalScanFromTo() throws IOException {
    StructLikeMap<Long> fromSequence = StructLikeMap.create(getMixedTable().spec().partitionType());
    StructLike partitionData =
        MixedDataFiles.data(getMixedTable().spec(), "op_time_day=2022-01-01");
    fromSequence.put(partitionData, 1L);
    ChangeTableIncrementalScan changeTableIncrementalScan =
        getMixedTable()
            .asKeyedTable()
            .changeTable()
            .newScan()
            .fromSequence(fromSequence)
            .toSequence(1);
    try (CloseableIterable<FileScanTask> tasks = changeTableIncrementalScan.planFiles()) {
      assertFilesSequence(tasks, 0, 0, 0);
    }
  }

  private void assertFiles(
      CloseableIterable<FileScanTask> tasks, int fileCnt, Predicate<FileScanTask> validator) {
    int taskCount = 0;
    for (FileScanTask task : tasks) {
      taskCount++;
      Assert.assertTrue(task instanceof BasicMixedFileScanTask);
      Assert.assertTrue(validator.test(task));
    }
    Assert.assertEquals(fileCnt, taskCount);
  }

  private void assertFilesSequence(
      CloseableIterable<FileScanTask> tasks, int fileCnt, long minSequence, long maxSequence) {
    assertFiles(
        tasks,
        fileCnt,
        task ->
            (task.file().dataSequenceNumber() >= minSequence)
                && (task.file().dataSequenceNumber() <= maxSequence));
  }
}

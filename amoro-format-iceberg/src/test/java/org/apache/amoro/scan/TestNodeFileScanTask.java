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

import org.apache.amoro.IcebergFileEntry;
import org.apache.amoro.data.DefaultKeyedFile;
import org.apache.amoro.utils.ManifestEntryFields;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/** Unit tests for lazy caching behavior of {@link NodeFileScanTask#dataTasks()}. */
public class TestNodeFileScanTask {

  // File name pattern: {nodeId}-{type}-{transactionId}-{partitionId}-{taskId}-{operationId}-{count}
  // BASE_FILE: nodeId-B-transactionId-...
  // INSERT_FILE: nodeId-I-transactionId-...
  // EQ_DELETE_FILE: nodeId-ED-transactionId-...
  private static MixedFileScanTask createBaseTask(String path) {
    DataFile dataFile =
        DataFiles.builder(PartitionSpec.unpartitioned())
            .withPath(path)
            .withFileSizeInBytes(100L)
            .withRecordCount(10L)
            .build();
    DefaultKeyedFile keyedFile = DefaultKeyedFile.parseBase(dataFile);
    return new BasicMixedFileScanTask(keyedFile, null, PartitionSpec.unpartitioned(), null);
  }

  private static MixedFileScanTask createInsertTask(String path) {
    IcebergFileEntry entry =
        new IcebergFileEntry(
            1L,
            2L,
            ManifestEntryFields.Status.ADDED,
            DataFiles.builder(PartitionSpec.unpartitioned())
                .withPath(path)
                .withFileSizeInBytes(100L)
                .withRecordCount(10L)
                .build());
    DefaultKeyedFile keyedFile = DefaultKeyedFile.parseChange((DataFile) entry.getFile());
    return new BasicMixedFileScanTask(keyedFile, null, PartitionSpec.unpartitioned(), null);
  }

  @Test
  public void testDataTasksReturnsSameInstanceOnConsecutiveCalls() {
    MixedFileScanTask baseTask =
        createBaseTask("/tmp/1-B-2-00000-0-9009257362994691056-00001.parquet");
    MixedFileScanTask insertTask =
        createInsertTask("/tmp/1-I-2-00000-0-9009257362994691056-00002.parquet");

    NodeFileScanTask nodeTask = new NodeFileScanTask(Arrays.asList(baseTask, insertTask));

    List<MixedFileScanTask> first = nodeTask.dataTasks();
    List<MixedFileScanTask> second = nodeTask.dataTasks();

    Assert.assertSame("dataTasks() should return the same cached instance", first, second);
  }

  @Test
  public void testDataTasksCacheIsInvalidatedAfterAddFile() {
    MixedFileScanTask baseTask =
        createBaseTask("/tmp/1-B-2-00000-0-9009257362994691056-00001.parquet");

    NodeFileScanTask nodeTask = new NodeFileScanTask();
    nodeTask.addFile(baseTask);

    List<MixedFileScanTask> before = nodeTask.dataTasks();
    Assert.assertEquals("Before addFile: should have 1 data task", 1, before.size());

    MixedFileScanTask insertTask =
        createInsertTask("/tmp/1-I-3-00000-0-9009257362994691056-00002.parquet");
    nodeTask.addFile(insertTask);

    List<MixedFileScanTask> after = nodeTask.dataTasks();
    Assert.assertEquals("After addFile: should have 2 data tasks", 2, after.size());
    Assert.assertNotSame(
        "dataTasks() should return a new instance after addFile invalidates cache", before, after);
  }

  @Test
  public void testDataTasksIsUnmodifiable() {
    MixedFileScanTask baseTask =
        createBaseTask("/tmp/1-B-2-00000-0-9009257362994691056-00001.parquet");
    NodeFileScanTask nodeTask = new NodeFileScanTask(Arrays.asList(baseTask));

    List<MixedFileScanTask> tasks = nodeTask.dataTasks();
    try {
      tasks.add(baseTask);
      Assert.fail("dataTasks() should return an unmodifiable list");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test
  public void testDataTasksContainsBothBaseAndInsertTasks() {
    MixedFileScanTask baseTask =
        createBaseTask("/tmp/1-B-2-00000-0-9009257362994691056-00001.parquet");
    MixedFileScanTask insertTask =
        createInsertTask("/tmp/1-I-3-00000-0-9009257362994691056-00002.parquet");

    NodeFileScanTask nodeTask = new NodeFileScanTask();
    nodeTask.addFile(baseTask);
    nodeTask.addFile(insertTask);

    List<MixedFileScanTask> tasks = nodeTask.dataTasks();
    Assert.assertEquals(
        "dataTasks() should contain both baseTasks and insertTasks", 2, tasks.size());
    Assert.assertTrue("dataTasks() should include the base task", tasks.contains(baseTask));
    Assert.assertTrue("dataTasks() should include the insert task", tasks.contains(insertTask));
  }

  @Test
  public void testDataTasksDoesNotContainEqDeleteFiles() {
    MixedFileScanTask baseTask =
        createBaseTask("/tmp/1-B-2-00000-0-9009257362994691056-00001.parquet");
    // EQ_DELETE_FILE: path with "ED" type identifier
    MixedFileScanTask deleteTask =
        createInsertTask("/tmp/1-ED-3-00000-0-9009257362994691056-00003.parquet");

    NodeFileScanTask nodeTask = new NodeFileScanTask();
    nodeTask.addFile(baseTask);
    nodeTask.addFile(deleteTask);

    List<MixedFileScanTask> tasks = nodeTask.dataTasks();
    Assert.assertEquals("dataTasks() should not include EQ_DELETE_FILE tasks", 1, tasks.size());
    Assert.assertTrue("dataTasks() should include only the base task", tasks.contains(baseTask));
  }
}

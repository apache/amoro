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

package org.apache.amoro.flink.write;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.flink.FlinkTestBase;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.flink.runtime.checkpoint.OperatorSubtaskState;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.WriteResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestMixedFormatFileCommitter extends FlinkTestBase {
  public MixedFormatTableLoader tableLoader;

  public TestMixedFormatFileCommitter() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  public OneInputStreamOperatorTestHarness<WriteResult, Void> createMixedFormatFileCommitter(
      MixedFormatTableLoader tableLoader,
      MixedTable table,
      OperatorSubtaskState operatorSubtaskState)
      throws Exception {
    OneInputStreamOperator<WriteResult, Void> committer =
        FlinkSink.createFileCommitter(
            table, tableLoader, false, SnapshotRef.MAIN_BRANCH, table.spec());
    OneInputStreamOperatorTestHarness<WriteResult, Void> harness =
        new OneInputStreamOperatorTestHarness<>(committer, 1, 1, 0);

    harness.setup();
    if (operatorSubtaskState == null) {
      harness.initializeEmptyState();
    } else {
      harness.initializeState(operatorSubtaskState);
    }
    harness.open();

    return harness;
  }

  public void checkChangeFiles(int fileCnt, int recordCnt, KeyedTable table) {
    table.changeTable().refresh();
    TableScan tableScan = table.changeTable().newScan();
    CloseableIterable<FileScanTask> fileScanTasks = tableScan.planFiles();
    int actualFileCnt = 0;
    int actualRecordCnt = 0;
    for (FileScanTask fileScanTask : fileScanTasks) {
      actualFileCnt++;
      actualRecordCnt += fileScanTask.file().recordCount();
    }
    Assert.assertEquals(fileCnt, actualFileCnt);
    Assert.assertEquals(recordCnt, actualRecordCnt);
  }

  @Test
  public void testCommit() throws Exception {
    tableLoader = MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder);
    KeyedTable table = MixedFormatUtils.loadMixedTable(tableLoader).asKeyedTable();

    List<WriteResult> completedFiles = prepareChangeFiles();
    OperatorSubtaskState snapshot;
    long checkpoint = 1;
    try (OneInputStreamOperatorTestHarness<WriteResult, Void> testHarness =
        createMixedFormatFileCommitter(tableLoader, table, null)) {

      for (WriteResult completedFile : completedFiles) {
        testHarness.processElement(new StreamRecord<>(completedFile));
      }
      snapshot = testHarness.snapshot(checkpoint, System.currentTimeMillis());
    }

    try (OneInputStreamOperatorTestHarness<WriteResult, Void> testHarness =
        createMixedFormatFileCommitter(tableLoader, table, snapshot)) {
      testHarness.notifyOfCompletedCheckpoint(checkpoint);
    }

    checkChangeFiles(7, 9, table);
  }

  private List<WriteResult> prepareChangeFiles() throws Exception {
    List<WriteResult> changeFiles;
    long checkpointId = 1L;
    try (OneInputStreamOperatorTestHarness<RowData, WriteResult> testHarness =
        TestMixedFormatFileWriter.createMixedFormatStreamWriter(tableLoader)) {
      // The first checkpoint
      testHarness.processElement(createRowData(1, "hello", "2020-10-11T10:10:11.0"), 1);
      testHarness.processElement(createRowData(2, "hello", "2020-10-12T10:10:11.0"), 1);
      testHarness.processElement(createRowData(3, "hello", "2020-10-13T10:10:11.0"), 1);

      testHarness.prepareSnapshotPreBarrier(checkpointId);
      Assert.assertEquals(1, testHarness.extractOutputValues().size());
      Assert.assertEquals(3, testHarness.extractOutputValues().get(0).dataFiles().length);

      checkpointId = checkpointId + 1;

      // The second checkpoint
      testHarness.processElement(createRowData(1, "hello", "2020-10-12T10:10:11.0"), 1);
      testHarness.processElement(
          createRowData(2, "hello", "2020-10-12T10:10:11.0", RowKind.UPDATE_BEFORE), 1);
      testHarness.processElement(
          createRowData(2, "hello0", "2020-10-12T10:10:11.0", RowKind.UPDATE_AFTER), 1);
      testHarness.processElement(
          createRowData(3, "hello", "2020-10-12T10:10:11.0", RowKind.DELETE), 1);
      testHarness.processElement(createRowData(5, "hello", "2020-10-12T10:10:11.0"), 1);
      testHarness.processElement(createRowData(6, "hello", "2020-10-12T10:10:11.0"), 1);

      testHarness.prepareSnapshotPreBarrier(checkpointId);
      // testHarness.extractOutputValues() compute the sum
      Assert.assertEquals(2, testHarness.extractOutputValues().size());
      Assert.assertEquals(4, testHarness.extractOutputValues().get(1).dataFiles().length);
      changeFiles = testHarness.extractOutputValues();
    }
    return changeFiles;
  }
}

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

package org.apache.amoro.flink.read;

import org.apache.amoro.flink.read.hybrid.reader.TestRowDataReaderFunction;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.scan.ChangeTableIncrementalScan;
import org.apache.iceberg.Snapshot;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFlinkSplitPlanner extends TestRowDataReaderFunction {

  @Test
  public void testPlanSplitFromKeyedTable() {
    testKeyedTable.baseTable().refresh();
    testKeyedTable.changeTable().refresh();
    List<MixedFormatSplit> splitList =
        FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger());
    Assert.assertEquals(7, splitList.size());
  }

  @Test
  public void testIncrementalChangelog() throws IOException {
    testKeyedTable.baseTable().refresh();
    testKeyedTable.changeTable().refresh();
    List<MixedFormatSplit> splitList =
        FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger());

    Assert.assertEquals(7, splitList.size());

    long startSnapshotId = testKeyedTable.changeTable().currentSnapshot().snapshotId();
    writeUpdate();
    testKeyedTable.changeTable().refresh();

    Snapshot snapshot = testKeyedTable.changeTable().snapshot(startSnapshotId);
    long fromSequence = snapshot.sequenceNumber();

    long nowSnapshotId = testKeyedTable.changeTable().currentSnapshot().snapshotId();
    ChangeTableIncrementalScan changeTableScan =
        testKeyedTable
            .changeTable()
            .newScan()
            .useSnapshot(nowSnapshotId)
            .fromSequence(fromSequence);

    List<MixedFormatSplit> changeSplits =
        FlinkSplitPlanner.planChangeTable(changeTableScan, new AtomicInteger());

    Assert.assertEquals(1, changeSplits.size());
  }
}

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

package org.apache.amoro.formats.hudi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.OperationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Tests for {@link HudiTableDescriptor#getSnapshots} with real {@link HudiSnapshot} instances, only
 * the external Hudi table access ({@link HudiTable#getSnapshotList(ExecutorService)}) is mocked
 * with a mocked {@link ExecutorService}, so no real Hudi I/O happens.
 */
public class TestHudiTableDescriptorSnapshots {

  private static final ExecutorService IO_EXECUTOR = mock(ExecutorService.class);

  private HudiTable hudiTable;
  private HudiTableDescriptor descriptor;

  private void mockSnapshotList(List<HudiSnapshot> snapshots) {
    hudiTable = mock(HudiTable.class);
    when(hudiTable.getSnapshotList(IO_EXECUTOR)).thenReturn(snapshots);
    descriptor = new HudiTableDescriptor();
    descriptor.withIoExecutor(IO_EXECUTOR);
  }

  private List<AmoroSnapshotsOfTable> getSnapshots(OperationType operationType) {
    return descriptor.getSnapshots(hudiTable, null, operationType);
  }

  private static HudiSnapshot snapshot(
      String snapshotId,
      long commitTimestamp,
      String operationType,
      String operation,
      long totalFileSize) {
    Map<String, String> summary = new HashMap<>();
    summary.put("instant", snapshotId);
    summary.put("total-size", String.valueOf(totalFileSize));
    return new HudiSnapshot(
        snapshotId,
        commitTimestamp,
        operationType,
        operation,
        5,
        3,
        2,
        totalFileSize,
        100L,
        summary);
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 1024L, 1048576L, 2147483647L, 2147483648L, 3221225472L, 5368709120L})
  public void testOriginalFileSizeIsNotTruncated(long fileSize) {
    mockSnapshotList(
        Collections.singletonList(
            snapshot("20240101000000", 1704067200000L, "NON_OPTIMIZING", "commit", fileSize)));

    List<AmoroSnapshotsOfTable> snapshots = getSnapshots(OperationType.ALL);

    assertEquals(1, snapshots.size());
    assertEquals(
        fileSize,
        snapshots.get(0).getOriginalFileSize(),
        "original file size " + fileSize + " must not be truncated");
  }

  @Test
  public void testSnapshotFieldsAreMapped() {
    Map<String, String> summary = new HashMap<>();
    summary.put("total-records", "42");
    HudiSnapshot hudiSnapshot =
        new HudiSnapshot(
            "20240101010101",
            1704067261000L,
            "OPTIMIZING",
            "compaction",
            7,
            4,
            3,
            3L * 1024 * 1024 * 1024,
            100L,
            summary);
    mockSnapshotList(Collections.singletonList(hudiSnapshot));

    List<AmoroSnapshotsOfTable> snapshots = getSnapshots(OperationType.ALL);

    assertEquals(1, snapshots.size());
    AmoroSnapshotsOfTable snapshot = snapshots.get(0);
    assertEquals("20240101010101", snapshot.getSnapshotId());
    assertEquals(1704067261000L, snapshot.getCommitTime());
    assertEquals(7, snapshot.getFileCount());
    assertEquals(3221225472L, snapshot.getOriginalFileSize());
    assertEquals(summary, snapshot.getSummary());
    assertEquals("compaction", snapshot.getOperation());
    Map<String, String> filesSummary = snapshot.getFilesSummaryForChart();
    assertEquals("0", filesSummary.get("delta-files"));
    assertEquals("4", filesSummary.get("data-files"));
    assertEquals("3", filesSummary.get("changelogs"));
  }

  @Test
  public void testSnapshotFilteringByOperationType() {
    mockSnapshotList(
        Arrays.asList(
            snapshot("1", 1L, "NON_OPTIMIZING", "commit", 1L),
            snapshot("2", 2L, "OPTIMIZING", "compaction", 2L),
            snapshot("3", 3L, "OPTIMIZING", "clustering", 3L)));

    List<AmoroSnapshotsOfTable> allSnapshots = getSnapshots(OperationType.ALL);
    assertEquals(3, allSnapshots.size());

    List<AmoroSnapshotsOfTable> optimizingSnapshots = getSnapshots(OperationType.OPTIMIZING);
    assertEquals(2, optimizingSnapshots.size());
    assertEquals("compaction", optimizingSnapshots.get(0).getOperation());
    assertEquals("clustering", optimizingSnapshots.get(1).getOperation());

    List<AmoroSnapshotsOfTable> nonOptimizingSnapshots = getSnapshots(OperationType.NON_OPTIMIZING);
    assertEquals(1, nonOptimizingSnapshots.size());
    assertEquals("commit", nonOptimizingSnapshots.get(0).getOperation());
    assertEquals(1L, nonOptimizingSnapshots.get(0).getOriginalFileSize());
  }

  @Test
  public void testEmptySnapshotList() {
    mockSnapshotList(Collections.emptyList());

    assertTrue(getSnapshots(OperationType.ALL).isEmpty());
    assertTrue(getSnapshots(OperationType.OPTIMIZING).isEmpty());
    assertTrue(getSnapshots(OperationType.NON_OPTIMIZING).isEmpty());
  }
}

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

package org.apache.amoro.flink.read.hybrid.enumerator;

import static org.apache.flink.util.Preconditions.checkState;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.flink.FlinkTestBase;
import org.apache.amoro.flink.read.FlinkSplitPlanner;
import org.apache.amoro.flink.read.hybrid.assigner.ShuffleSplitAssigner;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplitState;
import org.apache.amoro.flink.read.hybrid.split.SplitRequestEvent;
import org.apache.amoro.flink.read.source.MixedFormatScanContext;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.table.KeyedTable;
import org.apache.flink.api.connector.source.ReaderInfo;
import org.apache.flink.api.connector.source.SourceEvent;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.api.connector.source.SplitsAssignment;
import org.apache.flink.metrics.groups.SplitEnumeratorMetricGroup;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.io.TaskWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class TestMixedFormatSourceEnumerator extends FlinkTestBase {

  public TestMixedFormatSourceEnumerator() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  private final int splitCount = 4;
  private final int parallelism = 5;
  private KeyedTable testKeyedTable;

  public static final String SCAN_STARTUP_MODE_EARLIEST = "earliest";

  protected static final LocalDateTime LDT =
      LocalDateTime.of(LocalDate.of(2022, 1, 1), LocalTime.of(0, 0, 0, 0));

  @Before
  public void init() throws IOException {
    testKeyedTable = getMixedTable().asKeyedTable();
    // write change insert
    {
      TaskWriter<RowData> taskWriter = createKeyedTaskWriter(testKeyedTable, FLINK_ROW_TYPE, false);
      List<RowData> insert =
          new ArrayList<RowData>() {
            {
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      1,
                      StringData.fromString("john"),
                      LDT.toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT)));
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      2,
                      StringData.fromString("lily"),
                      LDT.plusDays(1).toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT.plusDays(1))));
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      3,
                      StringData.fromString("jake"),
                      LDT.plusDays(1).toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT.plusDays(1))));
              add(
                  GenericRowData.ofKind(
                      RowKind.INSERT,
                      4,
                      StringData.fromString("sam"),
                      LDT.plusDays(1).toEpochSecond(ZoneOffset.UTC),
                      TimestampData.fromLocalDateTime(LDT.plusDays(1))));
            }
          };
      for (RowData record : insert) {
        taskWriter.write(record);
      }
      commit(testKeyedTable, taskWriter.complete(), false);
    }
  }

  @Test
  public void testReadersNumGreaterThanSplits() throws Exception {
    TestingSplitEnumeratorContext splitEnumeratorContext =
        instanceSplitEnumeratorContext(parallelism);
    ShuffleSplitAssigner shuffleSplitAssigner = instanceSplitAssigner(splitEnumeratorContext);
    MixedFormatScanContext scanContext =
        MixedFormatScanContext.contextBuilder()
            .streaming(true)
            .scanStartupMode(SCAN_STARTUP_MODE_EARLIEST)
            .build();

    List<MixedFormatSplit> splitList =
        FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger());
    shuffleSplitAssigner.onDiscoveredSplits(splitList);
    assertSnapshot(shuffleSplitAssigner, splitCount);

    MixedFormatSourceEnumerator enumerator =
        new MixedFormatSourceEnumerator(
            splitEnumeratorContext,
            shuffleSplitAssigner,
            MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder),
            scanContext,
            null,
            false);

    Collection<MixedFormatSplitState> pendingSplitsEmpty =
        enumerator.snapshotState(1).pendingSplits();
    Assert.assertEquals(splitCount, pendingSplitsEmpty.size());

    // register readers, and let them request a split
    // 4 split, 5 subtask, one or more subtask will fetch empty split
    // subtask 0
    splitEnumeratorContext.registerReader(0, "host0");
    enumerator.addReader(0);
    enumerator.handleSourceEvent(0, new SplitRequestEvent());
    // subtask 1
    splitEnumeratorContext.registerReader(1, "host1");
    enumerator.addReader(1);
    enumerator.handleSourceEvent(1, new SplitRequestEvent());
    // subtask 2
    splitEnumeratorContext.registerReader(2, "host2");
    enumerator.addReader(2);
    enumerator.handleSourceEvent(2, new SplitRequestEvent());
    // subtask 3
    splitEnumeratorContext.registerReader(3, "host3");
    enumerator.addReader(3);
    enumerator.handleSourceEvent(3, new SplitRequestEvent());
    // subtask 4
    splitEnumeratorContext.registerReader(4, "host4");
    enumerator.addReader(4);
    enumerator.handleSourceEvent(4, new SplitRequestEvent());

    Assert.assertEquals(parallelism - splitCount, enumerator.getReadersAwaitingSplit().size());
    Assert.assertTrue(enumerator.snapshotState(2).pendingSplits().isEmpty());
  }

  private void assertSnapshot(ShuffleSplitAssigner assigner, int splitCount) {
    Collection<MixedFormatSplitState> stateBeforeGet = assigner.state();
    Assert.assertEquals(splitCount, stateBeforeGet.size());
  }

  private ShuffleSplitAssigner instanceSplitAssigner(
      TestingSplitEnumeratorContext splitEnumeratorContext) {
    return new ShuffleSplitAssigner(splitEnumeratorContext);
  }

  private TestingSplitEnumeratorContext instanceSplitEnumeratorContext(int parallelism) {
    return new TestingSplitEnumeratorContext(parallelism);
  }

  protected static class TestingSplitEnumeratorContext
      implements SplitEnumeratorContext<MixedFormatSplit> {
    private final int parallelism;

    private final HashMap<Integer, SplitAssignmentState<MixedFormatSplit>> splitAssignments =
        new HashMap<>();

    private final HashMap<Integer, List<SourceEvent>> events = new HashMap<>();

    private final HashMap<Integer, ReaderInfo> registeredReaders = new HashMap<>();

    public Map<Integer, SplitAssignmentState<MixedFormatSplit>> getSplitAssignments() {
      return splitAssignments;
    }

    public Map<Integer, List<SourceEvent>> getSentEvents() {
      return events;
    }

    public void registerReader(int subtask, String hostname) {
      checkState(!registeredReaders.containsKey(subtask), "Reader already registered");
      registeredReaders.put(subtask, new ReaderInfo(subtask, hostname));
    }

    public TestingSplitEnumeratorContext(int parallelism) {
      this.parallelism = parallelism;
    }

    @Override
    public SplitEnumeratorMetricGroup metricGroup() {
      return null;
    }

    @Override
    public void sendEventToSourceReader(int subtaskId, SourceEvent event) {
      final List<SourceEvent> eventsForSubTask =
          events.computeIfAbsent(subtaskId, (key) -> new ArrayList<>());
      eventsForSubTask.add(event);
    }

    @Override
    public int currentParallelism() {
      return parallelism;
    }

    @Override
    public Map<Integer, ReaderInfo> registeredReaders() {
      return registeredReaders;
    }

    @Override
    public void assignSplits(SplitsAssignment<MixedFormatSplit> newSplitAssignments) {
      for (final Map.Entry<Integer, List<MixedFormatSplit>> entry :
          newSplitAssignments.assignment().entrySet()) {
        final SplitAssignmentState<MixedFormatSplit> assignment =
            splitAssignments.computeIfAbsent(entry.getKey(), (key) -> new SplitAssignmentState<>());

        assignment.getAssignedSplits().addAll(entry.getValue());
      }
    }

    @Override
    public void assignSplit(MixedFormatSplit split, int subtask) {
      SplitEnumeratorContext.super.assignSplit(split, subtask);
    }

    @Override
    public void signalNoMoreSplits(int subtask) {
      final SplitAssignmentState assignment =
          splitAssignments.computeIfAbsent(subtask, (key) -> new SplitAssignmentState<>());
      assignment.noMoreSplits = true;
    }

    @Override
    public <T> void callAsync(
        Callable<T> callable, BiConsumer<T, Throwable> handler, long initialDelay, long period) {}

    @Override
    public <T> void callAsync(Callable<T> callable, BiConsumer<T, Throwable> handler) {}

    @Override
    public void runInCoordinatorThread(Runnable runnable) {}
  }

  public static final class SplitAssignmentState<T> {

    final List<T> splits = new ArrayList<>();
    boolean noMoreSplits;

    public List<T> getAssignedSplits() {
      return splits;
    }

    public boolean hasReceivedNoMoreSplitsSignal() {
      return noMoreSplits;
    }
  }
}

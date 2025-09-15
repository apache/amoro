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

package org.apache.amoro.flink.read.hybrid.reader;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.flink.read.FlinkSplitPlanner;
import org.apache.amoro.flink.read.hybrid.enumerator.TestContinuousSplitPlannerImpl;
import org.apache.amoro.flink.read.hybrid.split.ChangelogSplit;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.source.DataIterator;
import org.apache.amoro.scan.ChangeTableIncrementalScan;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.table.KeyedTable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava31.com.google.common.collect.Maps;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TestRowDataReaderFunction extends TestContinuousSplitPlannerImpl {
  private static final Logger LOG = LoggerFactory.getLogger(TestRowDataReaderFunction.class);
  private static final AtomicInteger splitCount = new AtomicInteger();

  public TestRowDataReaderFunction() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  @Test
  public void testReadChangelog() throws IOException {

    List<MixedFormatSplit> mixedFormatSplits =
        FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger(0));

    RowDataReaderFunction rowDataReaderFunction =
        new RowDataReaderFunction(
            new Configuration(),
            testKeyedTable.schema(),
            testKeyedTable.schema(),
            testKeyedTable.primaryKeySpec(),
            null,
            true,
            testKeyedTable.io());

    List<RowData> actual = new ArrayList<>();
    mixedFormatSplits.forEach(
        split -> {
          LOG.info("Mixed format split: {}.", split);
          DataIterator<RowData> dataIterator = rowDataReaderFunction.createDataIterator(split);
          while (dataIterator.hasNext()) {
            RowData rowData = dataIterator.next();
            LOG.info("{}", rowData);
            actual.add(rowData);
          }
        });

    assertArrayEquals(excepts(), actual);

    long snapshotId = testKeyedTable.changeTable().currentSnapshot().snapshotId();
    writeUpdate();

    testKeyedTable.changeTable().refresh();
    long nowSnapshotId = testKeyedTable.changeTable().currentSnapshot().snapshotId();
    ChangeTableIncrementalScan changeTableScan =
        testKeyedTable.changeTable().newScan().useSnapshot(nowSnapshotId);

    Snapshot snapshot = testKeyedTable.changeTable().snapshot(snapshotId);
    long fromSequence = snapshot.sequenceNumber();

    Set<MixedFileScanTask> appendLogTasks = new HashSet<>();
    Set<MixedFileScanTask> deleteLogTasks = new HashSet<>();
    try (CloseableIterable<FileScanTask> tasks = changeTableScan.planFiles()) {
      for (FileScanTask fileScanTask : tasks) {
        if (fileScanTask.file().dataSequenceNumber() <= fromSequence) {
          continue;
        }
        MixedFileScanTask mixedFileScanTask = (MixedFileScanTask) fileScanTask;
        if (mixedFileScanTask.fileType().equals(DataFileType.INSERT_FILE)) {
          appendLogTasks.add(mixedFileScanTask);
        } else if (mixedFileScanTask.fileType().equals(DataFileType.EQ_DELETE_FILE)) {
          deleteLogTasks.add(mixedFileScanTask);
        } else {
          throw new IllegalArgumentException(
              String.format(
                  "DataFileType %s is not supported during change log reading period.",
                  mixedFileScanTask.fileType()));
        }
      }
    }
    ChangelogSplit changelogSplit =
        new ChangelogSplit(appendLogTasks, deleteLogTasks, splitCount.incrementAndGet());
    actual.clear();
    DataIterator<RowData> dataIterator = rowDataReaderFunction.createDataIterator(changelogSplit);
    while (dataIterator.hasNext()) {
      RowData rowData = dataIterator.next();
      actual.add(rowData);
    }
    assertArrayEquals(excepts2(), actual);
  }

  @Test
  public void testReadNodesUpMoved() throws IOException {
    writeUpdateWithSpecifiedMaskOne();
    List<MixedFormatSplit> mixedFormatSplits =
        FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger(0));

    RowDataReaderFunction rowDataReaderFunction =
        new RowDataReaderFunction(
            new Configuration(),
            testKeyedTable.schema(),
            testKeyedTable.schema(),
            testKeyedTable.primaryKeySpec(),
            null,
            true,
            testKeyedTable.io());

    List<RowData> actual = new ArrayList<>();
    mixedFormatSplits.forEach(
        split -> {
          LOG.info("Mixed format split: {}.", split);
          DataIterator<RowData> dataIterator = rowDataReaderFunction.createDataIterator(split);
          while (dataIterator.hasNext()) {
            RowData rowData = dataIterator.next();
            LOG.info("{}", rowData);
            actual.add(rowData);
          }
        });

    List<RowData> excepts = expectedCollection();
    excepts.addAll(generateRecords());
    RowData[] array =
        excepts.stream()
            .sorted(Comparator.comparing(RowData::toString))
            .collect(Collectors.toList())
            .toArray(new RowData[excepts.size()]);
    assertArrayEquals(array, actual);
  }

  protected void assertArrayEquals(RowData[] excepts, List<RowData> actual) {
    Assert.assertArrayEquals(excepts, sortRowDataCollection(actual));
  }

  protected RowData[] sortRowDataCollection(Collection<RowData> records) {
    return records.stream()
        .sorted(Comparator.comparing(RowData::toString))
        .collect(Collectors.toList())
        .toArray(new RowData[records.size()]);
  }

  protected void writeUpdate() throws IOException {
    // write change update
    writeUpdate(updateRecords());
  }

  protected void writeUpdate(List<RowData> input) throws IOException {
    writeUpdate(input, testKeyedTable);
  }

  protected void writeUpdateWithSpecifiedMaskOne() throws IOException {
    List<RowData> excepts = generateRecords();

    writeUpdateWithSpecifiedMask(excepts, testKeyedTable, 1);
  }

  protected void writeUpdateWithSpecifiedMask(List<RowData> input, KeyedTable table, long mask)
      throws IOException {
    // write change update
    TaskWriter<RowData> taskWriter = createKeyedTaskWriter(table, ROW_TYPE, false, mask);

    for (RowData record : input) {
      taskWriter.write(record);
    }
    commit(table, taskWriter.complete(), false);
  }

  protected void writeUpdate(List<RowData> input, KeyedTable table) throws IOException {
    // write change update
    TaskWriter<RowData> taskWriter = createKeyedTaskWriter(table, ROW_TYPE, false);

    for (RowData record : input) {
      taskWriter.write(record);
    }
    commit(table, taskWriter.complete(), false);
  }

  protected List<RowData> generateRecords() {
    List<RowData> excepts = new ArrayList<>();
    excepts.add(
        GenericRowData.ofKind(
            RowKind.INSERT,
            7,
            StringData.fromString("syan"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.UPDATE_BEFORE,
            2,
            StringData.fromString("lily"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.UPDATE_AFTER,
            2,
            StringData.fromString("daniel"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.UPDATE_BEFORE,
            7,
            StringData.fromString("syan"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.UPDATE_AFTER,
            7,
            StringData.fromString("syan2"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    return excepts;
  }

  protected List<RowData> updateRecords() {
    List<RowData> excepts = new ArrayList<>();
    excepts.add(
        GenericRowData.ofKind(
            RowKind.UPDATE_BEFORE,
            5,
            StringData.fromString("lind"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.UPDATE_AFTER,
            5,
            StringData.fromString("lina"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    return excepts;
  }

  protected RowData[] excepts2() {
    List<RowData> excepts = updateRecords();

    return updateRecords().stream()
        .sorted(Comparator.comparing(RowData::toString))
        .collect(Collectors.toList())
        .toArray(new RowData[excepts.size()]);
  }

  protected RowData[] excepts() {
    List<RowData> excepts = expectedCollection();

    return excepts.stream()
        .sorted(Comparator.comparing(RowData::toString))
        .collect(Collectors.toList())
        .toArray(new RowData[excepts.size()]);
  }

  protected RowData[] expectedAfterMOR() {
    List<RowData> expected = expectedCollection();
    return mor(expected).stream()
        .sorted(Comparator.comparing(RowData::toString))
        .toArray(RowData[]::new);
  }

  protected Collection<RowData> mor(final Collection<RowData> changelog) {
    Map<Integer, RowData> map = Maps.newHashMap();

    changelog.forEach(
        rowData -> {
          int key = rowData.getInt(0);
          RowKind kind = rowData.getRowKind();

          if ((kind == RowKind.INSERT || kind == RowKind.UPDATE_AFTER) && !map.containsKey(key)) {
            rowData.setRowKind(RowKind.INSERT);
            map.put(key, rowData);
          } else if ((kind == RowKind.DELETE || kind == RowKind.UPDATE_BEFORE)) {
            map.remove(key);
          }
        });

    return map.values();
  }

  protected List<RowData> expectedCollection() {
    List<RowData> excepts = new ArrayList<>();
    excepts.add(
        GenericRowData.ofKind(
            RowKind.INSERT,
            1,
            StringData.fromString("john"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.INSERT,
            2,
            StringData.fromString("lily"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.INSERT,
            3,
            StringData.fromString("jake"),
            LDT.plusDays(1).toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT.plusDays(1))));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.INSERT,
            4,
            StringData.fromString("sam"),
            LDT.plusDays(1).toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT.plusDays(1))));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.INSERT,
            5,
            StringData.fromString("mary"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.INSERT,
            6,
            StringData.fromString("mack"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.DELETE,
            5,
            StringData.fromString("mary"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    excepts.add(
        GenericRowData.ofKind(
            RowKind.INSERT,
            5,
            StringData.fromString("lind"),
            LDT.toEpochSecond(ZoneOffset.UTC),
            TimestampData.fromLocalDateTime(LDT)));
    return excepts;
  }
}

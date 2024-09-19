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

import static org.apache.amoro.MockAmoroManagementServer.TEST_CATALOG_NAME;
import static org.apache.amoro.MockAmoroManagementServer.TEST_DB_NAME;
import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.SCAN_STARTUP_MODE_EARLIEST;
import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.SCAN_STARTUP_MODE_LATEST;
import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.flink.read.hybrid.reader.ReaderFunction;
import org.apache.amoro.flink.read.hybrid.reader.RowDataReaderFunction;
import org.apache.amoro.flink.read.hybrid.reader.TestRowDataReaderFunction;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.source.DataIterator;
import org.apache.amoro.flink.read.source.MixedFormatScanContext;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.flink.write.FlinkSink;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.TableFileUtil;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.runtime.highavailability.nonha.embedded.HaLeadershipControl;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.runtime.minicluster.RpcServiceSharing;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.ChainingStrategy;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.api.operators.collect.ClientAndIterator;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.table.runtime.typeutils.InternalTypeInfo;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.CloseableIterator;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMixedFormatSource extends TestRowDataReaderFunction implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(TestMixedFormatSource.class);
  private static final long serialVersionUID = 7418812854449034756L;
  private static final int PARALLELISM = 1;

  @Rule
  public final MiniClusterWithClientResource miniClusterResource =
      new MiniClusterWithClientResource(
          new MiniClusterResourceConfiguration.Builder()
              .setNumberTaskManagers(1)
              .setNumberSlotsPerTaskManager(PARALLELISM)
              .setRpcServiceSharing(RpcServiceSharing.DEDICATED)
              .withHaLeadershipControl()
              .build());

  protected KeyedTable testFailoverTable;
  protected static final String SINK_TABLE_NAME = "test_sink_exactly_once";
  protected static final TableIdentifier FAIL_TABLE_ID =
      TableIdentifier.of(
          TableTestHelper.TEST_CATALOG_NAME, TableTestHelper.TEST_DB_NAME, SINK_TABLE_NAME);

  @Before
  public void testSetup() throws IOException {
    MixedFormatCatalog testCatalog = getMixedFormatCatalog();

    String db = FAIL_TABLE_ID.getDatabase();
    if (!testCatalog.listDatabases().contains(db)) {
      testCatalog.createDatabase(db);
    }

    if (!testCatalog.tableExists(FAIL_TABLE_ID)) {
      testFailoverTable =
          testCatalog
              .newTableBuilder(FAIL_TABLE_ID, TABLE_SCHEMA)
              .withPartitionSpec(BasicTableTestHelper.SPEC)
              .withPrimaryKeySpec(BasicTableTestHelper.PRIMARY_KEY_SPEC)
              .create()
              .asKeyedTable();
    }
  }

  @After
  public void dropTable() {
    miniClusterResource.cancelAllJobs();
    getMixedFormatCatalog().dropTable(FAIL_TABLE_ID, true);
    getMixedFormatCatalog().dropTable(TableTestHelper.TEST_TABLE_ID, true);
    getMixedFormatCatalog().dropDatabase(TableTestHelper.TEST_DB_NAME);
  }

  @Test
  public void testMixedFormatSourceStatic() throws Exception {
    MixedFormatSource<RowData> mixedFormatSource = initMixedFormatSource(false);

    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(3000);
    // set the source parallelism to 4
    final CloseableIterator<RowData> resultIterator =
        env.fromSource(
                mixedFormatSource, WatermarkStrategy.noWatermarks(), "MixedFormatParallelSource")
            .setParallelism(PARALLELISM)
            .executeAndCollect();

    List<RowData> actualResult = new ArrayList<>();

    resultIterator.forEachRemaining(
        row -> {
          GenericRowData rowData = convert(row);
          actualResult.add(rowData);
        });
    RowData[] expected = expectedAfterMOR();
    assertArrayEquals(expected, actualResult);
  }

  @Test
  public void testMixedFormatSourceStaticJobManagerFailover() throws Exception {
    testMixedFormatSource(FailoverType.JM);
  }

  @Test
  public void testMixedFormatSourceStaticTaskManagerFailover() throws Exception {
    testMixedFormatSource(FailoverType.TM);
  }

  public void testMixedFormatSource(FailoverType failoverType) throws Exception {
    List<RowData> expected = new ArrayList<>(expectedCollection());
    List<RowData> updated = updateRecords();
    writeUpdate(updated);
    List<RowData> records = generateRecords(2, 1);
    writeUpdate(records);
    expected.addAll(updated);
    expected.addAll(records);

    MixedFormatSource<RowData> mixedFormatSource = initMixedFormatSource(false);
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(1000);
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(1, 0));

    DataStream<RowData> input =
        env.fromSource(
                mixedFormatSource, WatermarkStrategy.noWatermarks(), "MixedFormatParallelSource")
            .setParallelism(PARALLELISM);

    List<RowData> expectedAfterMoR = new ArrayList<>(mor(expected));
    DataStream<RowData> streamFailingInTheMiddleOfReading =
        RecordCounterToFail.wrapWithFailureAfter(input, expectedAfterMoR.size() / 2);

    FlinkSink.forRowData(streamFailingInTheMiddleOfReading)
        .context(Optional::of)
        .table(testFailoverTable)
        .tableLoader(MixedFormatTableLoader.of(FAIL_TABLE_ID, catalogBuilder))
        .flinkSchema(FLINK_SCHEMA)
        .build();

    JobClient jobClient = env.executeAsync("Bounded Mixed-Format Source Failover Test");
    JobID jobId = jobClient.getJobID();

    RecordCounterToFail.waitToFail();
    triggerFailover(
        failoverType,
        jobId,
        RecordCounterToFail::continueProcessing,
        miniClusterResource.getMiniCluster());

    assertRecords(testFailoverTable, expectedAfterMoR, Duration.ofMillis(10), 12000);
  }

  @Test
  public void testDimTaskManagerFailover() throws Exception {
    int restartAttempts = 10;
    List<RowData> updated = updateRecords();
    writeUpdate(updated);
    List<RowData> records = generateRecords(2, 1);
    writeUpdate(records);

    MixedFormatSource<RowData> mixedFormatSource = initMixedFormatDimSource(true);
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(3000);
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(restartAttempts, 0));

    DataStream<RowData> input =
        env.fromSource(
                mixedFormatSource, WatermarkStrategy.noWatermarks(), "MixedFormatParallelSource")
            .setParallelism(PARALLELISM);

    WatermarkAwareFailWrapper.wrapWithFailureAfter(input);

    JobClient jobClient = env.executeAsync("Dim Mixed-Format Source Failover Test");
    JobID jobId = jobClient.getJobID();

    WatermarkAwareFailWrapper.waitToFail();
    triggerFailover(
        FailoverType.TM,
        jobId,
        WatermarkAwareFailWrapper::continueProcessing,
        miniClusterResource.getMiniCluster());

    while (WatermarkAwareFailWrapper.watermarkCounter.get() != restartAttempts) {
      Thread.sleep(2000);
      LOG.info("wait for watermark after failover");
    }
    Assert.assertEquals(Long.MAX_VALUE, WatermarkAwareFailWrapper.getWatermarkAfterFailover());
  }

  @Test
  public void testMixedFormatContinuousSource() throws Exception {
    MixedFormatSource<RowData> mixedFormatSource = initMixedFormatSource(true);
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(1000);
    ClientAndIterator<RowData> clientAndIterator =
        executeAndCollectWithClient(env, mixedFormatSource);

    JobClient jobClient = clientAndIterator.client;

    List<RowData> actualResult =
        collectRecordsFromUnboundedStream(clientAndIterator, excepts().length);

    assertArrayEquals(excepts(), actualResult);

    LOG.info(
        "begin write update_before update_after data and commit new snapshot to change table.");
    writeUpdate();

    actualResult = collectRecordsFromUnboundedStream(clientAndIterator, excepts2().length);

    assertArrayEquals(excepts2(), actualResult);
    jobClient.cancel();
  }

  @Test
  public void testMixedFormatContinuousSourceWithEmptyChangeInInit() throws Exception {
    TableIdentifier tableId =
        TableIdentifier.of(TEST_CATALOG_NAME, TEST_DB_NAME, "test_empty_change");
    KeyedTable table =
        getMixedFormatCatalog()
            .newTableBuilder(tableId, TABLE_SCHEMA)
            .withPartitionSpec(BasicTableTestHelper.SPEC)
            .withPrimaryKeySpec(BasicTableTestHelper.PRIMARY_KEY_SPEC)
            .create()
            .asKeyedTable();

    TaskWriter<RowData> taskWriter = createTaskWriter(true);
    List<RowData> baseData =
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
                    LDT.toEpochSecond(ZoneOffset.UTC),
                    TimestampData.fromLocalDateTime(LDT)));
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
    for (RowData record : baseData) {
      taskWriter.write(record);
    }
    commit(table, taskWriter.complete(), true);

    MixedFormatSource<RowData> mixedFormatSource =
        initMixedFormatSource(true, SCAN_STARTUP_MODE_EARLIEST, tableId);
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(1000);
    ClientAndIterator<RowData> clientAndIterator =
        executeAndCollectWithClient(env, mixedFormatSource);

    JobClient jobClient = clientAndIterator.client;

    List<RowData> actualResult =
        collectRecordsFromUnboundedStream(clientAndIterator, baseData.size());

    Assert.assertEquals(new HashSet<>(baseData), new HashSet<>(actualResult));

    LOG.info(
        "begin write update_before update_after data and commit new snapshot to change table.");
    writeUpdate(updateRecords(), table);
    writeUpdate(updateRecords(), table);

    actualResult = collectRecordsFromUnboundedStream(clientAndIterator, excepts2().length * 2);
    jobClient.cancel();

    Assert.assertEquals(new HashSet<>(updateRecords()), new HashSet<>(actualResult));
    getMixedFormatCatalog().dropTable(tableId, true);
  }

  @Test
  public void testMixedFormatSourceEnumeratorWithChangeExpired() throws Exception {
    final String maxContinuousEmptyCommits = "flink.max-continuous-empty-commits";
    TableIdentifier tableId = TableIdentifier.of(TEST_CATALOG_NAME, TEST_DB_NAME, "test_keyed_tb");
    KeyedTable table =
        getMixedFormatCatalog()
            .newTableBuilder(tableId, TABLE_SCHEMA)
            .withProperty(maxContinuousEmptyCommits, "1")
            .withPrimaryKeySpec(BasicTableTestHelper.PRIMARY_KEY_SPEC)
            .create()
            .asKeyedTable();

    TaskWriter<RowData> taskWriter = createTaskWriter(table, false);
    List<RowData> changeData =
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
                    LDT.toEpochSecond(ZoneOffset.UTC),
                    TimestampData.fromLocalDateTime(LDT)));
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
    for (RowData record : changeData) {
      taskWriter.write(record);
    }

    List<DataFile> changeDataFiles = new ArrayList<>();
    WriteResult result = taskWriter.complete();
    changeDataFiles.addAll(Arrays.asList(result.dataFiles()));
    commit(table, result, false);

    for (DataFile dataFile : changeDataFiles) {
      Assert.assertTrue(table.io().exists(dataFile.path().toString()));
    }

    final Duration monitorInterval = Duration.ofSeconds(1);
    MixedFormatSource<RowData> mixedFormatSource =
        initMixedFormatSourceWithMonitorInterval(
            true, SCAN_STARTUP_MODE_EARLIEST, tableId, monitorInterval);
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(1000);
    ClientAndIterator<RowData> clientAndIterator =
        executeAndCollectWithClient(env, mixedFormatSource);

    JobClient jobClient = clientAndIterator.client;

    List<RowData> actualResult =
        collectRecordsFromUnboundedStream(clientAndIterator, changeData.size());
    Assert.assertEquals(new HashSet<>(changeData), new HashSet<>(actualResult));

    // expire changeTable snapshots
    DeleteFiles deleteFiles = table.changeTable().newDelete();
    for (DataFile dataFile : changeDataFiles) {
      Assert.assertTrue(table.io().exists(dataFile.path().toString()));
      deleteFiles.deleteFile(dataFile);
    }
    deleteFiles.commit();

    LOG.info("commit empty snapshot");
    AppendFiles changeAppend = table.changeTable().newAppend();
    changeAppend.commit();

    final long timeWait = (monitorInterval.toMillis() * 2);
    LOG.info("try sleep {}, wait snapshot expired and scan the empty snapshot.", timeWait);
    Thread.sleep(timeWait);

    expireSnapshots(table.changeTable(), System.currentTimeMillis(), new HashSet<>());

    writeUpdate(updateRecords(), table);
    writeUpdate(updateRecords(), table);

    actualResult = collectRecordsFromUnboundedStream(clientAndIterator, excepts2().length * 2);
    jobClient.cancel();

    Assert.assertEquals(new HashSet<>(updateRecords()), new HashSet<>(actualResult));
    getMixedFormatCatalog().dropTable(tableId, true);
  }

  @Test
  public void testMixedFormatSourceEnumeratorWithBaseExpired() throws Exception {
    final String maxContinuousEmptyCommits = "flink.max-continuous-empty-commits";
    TableIdentifier tableId = TableIdentifier.of(TEST_CATALOG_NAME, TEST_DB_NAME, "test_keyed_tb");
    KeyedTable table =
        getMixedFormatCatalog()
            .newTableBuilder(tableId, TABLE_SCHEMA)
            .withProperty(maxContinuousEmptyCommits, "1")
            .withPrimaryKeySpec(BasicTableTestHelper.PRIMARY_KEY_SPEC)
            .create()
            .asKeyedTable();

    TaskWriter<RowData> taskWriter = createTaskWriter(table, true);
    List<RowData> baseData =
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
                    LDT.toEpochSecond(ZoneOffset.UTC),
                    TimestampData.fromLocalDateTime(LDT)));
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
    for (RowData record : baseData) {
      taskWriter.write(record);
    }

    List<DataFile> baseDataFiles = new ArrayList<>();
    WriteResult result = taskWriter.complete();
    baseDataFiles.addAll(Arrays.asList(result.dataFiles()));
    commit(table, result, true);

    for (DataFile dataFile : baseDataFiles) {
      Assert.assertTrue(table.io().exists(dataFile.path().toString()));
    }

    final Duration monitorInterval = Duration.ofSeconds(1);
    MixedFormatSource<RowData> mixedFormatSource =
        initMixedFormatSourceWithMonitorInterval(
            true, SCAN_STARTUP_MODE_EARLIEST, tableId, monitorInterval);
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(1000);
    ClientAndIterator<RowData> clientAndIterator =
        executeAndCollectWithClient(env, mixedFormatSource);

    JobClient jobClient = clientAndIterator.client;

    List<RowData> actualResult =
        collectRecordsFromUnboundedStream(clientAndIterator, baseData.size());
    Assert.assertEquals(new HashSet<>(baseData), new HashSet<>(actualResult));

    // expire baseTable snapshots
    DeleteFiles deleteFiles = table.baseTable().newDelete();
    for (DataFile dataFile : baseDataFiles) {
      Assert.assertTrue(table.io().exists(dataFile.path().toString()));
      deleteFiles.deleteFile(dataFile);
    }
    deleteFiles.commit();

    LOG.info("commit empty snapshot");
    AppendFiles changeAppend = table.changeTable().newAppend();
    changeAppend.commit();

    final long timeWait = (monitorInterval.toMillis() * 2);
    LOG.info("try sleep {}, wait snapshot expired and scan the empty snapshot.", timeWait);
    Thread.sleep(timeWait);

    expireSnapshots(table.baseTable(), System.currentTimeMillis(), new HashSet<>());

    writeUpdate(updateRecords(), table);
    writeUpdate(updateRecords(), table);

    actualResult = collectRecordsFromUnboundedStream(clientAndIterator, excepts2().length * 2);
    jobClient.cancel();

    Assert.assertEquals(new HashSet<>(updateRecords()), new HashSet<>(actualResult));
    getMixedFormatCatalog().dropTable(tableId, true);
  }

  @Test
  public void testLatestStartupMode() throws Exception {
    MixedFormatSource<RowData> mixedFormatSource = initMixedFormatSourceWithLatest();
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(1000);

    ClientAndIterator<RowData> clientAndIterator =
        executeAndCollectWithClient(env, mixedFormatSource);

    JobClient jobClient = clientAndIterator.client;

    while (true) {
      if (JobStatus.RUNNING == jobClient.getJobStatus().get()) {
        Thread.sleep(500);
        LOG.info(
            "begin write update_before update_after data and commit new snapshot to change table.");
        writeUpdate();
        break;
      }
      Thread.sleep(100);
    }

    List<RowData> actualResult =
        collectRecordsFromUnboundedStream(clientAndIterator, excepts2().length);

    assertArrayEquals(excepts2(), actualResult);
    jobClient.cancel();
  }

  @Test
  public void testMixedFormatContinuousSourceJobManagerFailover() throws Exception {
    testMixedFormatContinuousSource(FailoverType.JM);
  }

  @Test
  public void testMixedFormatContinuousSourceTaskManagerFailover() throws Exception {
    testMixedFormatContinuousSource(FailoverType.TM);
  }

  public void testMixedFormatContinuousSource(final FailoverType failoverType) throws Exception {
    List<RowData> expected = new ArrayList<>(Arrays.asList(excepts()));
    writeUpdate();
    expected.addAll(Arrays.asList(excepts2()));

    MixedFormatSource<RowData> mixedFormatSource = initMixedFormatSource(true);
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // enable checkpoint
    env.enableCheckpointing(1000);
    //    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(1, 0));

    DataStream<RowData> input =
        env.fromSource(
                mixedFormatSource, WatermarkStrategy.noWatermarks(), "MixedFormatParallelSource")
            .setParallelism(PARALLELISM);

    FlinkSink.forRowData(input)
        .context(Optional::of)
        .table(testFailoverTable)
        .tableLoader(MixedFormatTableLoader.of(FAIL_TABLE_ID, catalogBuilder))
        .flinkSchema(FLINK_SCHEMA)
        .build();

    JobClient jobClient = env.executeAsync("Unbounded Mixed-Format Source Failover Test");
    JobID jobId = jobClient.getJobID();

    for (int i = 1; i < 5; i++) {
      Thread.sleep(10);
      List<RowData> records = generateRecords(2, i);
      expected.addAll(records);
      writeUpdate(records);
      if (i == 2) {
        triggerFailover(failoverType, jobId, () -> {}, miniClusterResource.getMiniCluster());
      }
    }

    // wait longer for continuous source to reduce flakiness
    // because CI servers tend to be overloaded.
    assertRecords(testFailoverTable, expected, Duration.ofMillis(10), 12000);
    jobClient.cancel();
  }

  private void assertRecords(
      KeyedTable testFailoverTable,
      List<RowData> expected,
      Duration checkInterval,
      int maxCheckCount)
      throws InterruptedException {
    for (int i = 0; i < maxCheckCount; ++i) {
      if (equalsRecords(expected, tableRecords(testFailoverTable), testFailoverTable.schema())) {
        break;
      } else {
        Thread.sleep(checkInterval.toMillis());
      }
    }
    // success or failure, assert on the latest table state
    equalsRecords(expected, tableRecords(testFailoverTable), testFailoverTable.schema());
  }

  private boolean equalsRecords(List<RowData> expected, List<RowData> tableRecords, Schema schema) {
    try {
      RowData[] expectedArray = sortRowDataCollection(expected);
      RowData[] actualArray = sortRowDataCollection(tableRecords);
      Assert.assertArrayEquals(expectedArray, actualArray);
      return true;
    } catch (Throwable e) {
      return false;
    }
  }

  public static List<RowData> tableRecords(final KeyedTable keyedTable) {
    keyedTable.refresh();
    List<MixedFormatSplit> mixedFormatSplits =
        FlinkSplitPlanner.planFullTable(keyedTable, new AtomicInteger(0));

    RowDataReaderFunction rowDataReaderFunction =
        new RowDataReaderFunction(
            new Configuration(),
            keyedTable.schema(),
            keyedTable.schema(),
            keyedTable.primaryKeySpec(),
            null,
            true,
            keyedTable.io());

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
    return actual;
  }

  private List<RowData> generateRecords(int numRecords, int index) {
    int pk = 100;
    List<RowData> records = new ArrayList<>(numRecords);
    for (int i = index; i < numRecords + index; i++) {
      records.add(
          GenericRowData.ofKind(
              RowKind.INSERT,
              pk + index,
              StringData.fromString("jo" + index + i),
              LDT.toEpochSecond(ZoneOffset.UTC),
              TimestampData.fromLocalDateTime(LDT)));
      records.add(
          GenericRowData.ofKind(
              RowKind.DELETE,
              pk + index,
              StringData.fromString("jo" + index + i),
              LDT.toEpochSecond(ZoneOffset.UTC),
              TimestampData.fromLocalDateTime(LDT)));
    }
    return records;
  }

  // ------------------------------------------------------------------------
  //  test utilities
  // ------------------------------------------------------------------------

  private enum FailoverType {
    NONE,
    TM,
    JM
  }

  private static void triggerFailover(
      FailoverType type, JobID jobId, Runnable afterFailAction, MiniCluster miniCluster)
      throws Exception {
    switch (type) {
      case NONE:
        afterFailAction.run();
        break;
      case TM:
        restartTaskManager(afterFailAction, miniCluster);
        break;
      case JM:
        triggerJobManagerFailover(jobId, afterFailAction, miniCluster);
        break;
    }
  }

  private static void triggerJobManagerFailover(
      JobID jobId, Runnable afterFailAction, MiniCluster miniCluster) throws Exception {
    final HaLeadershipControl haLeadershipControl = miniCluster.getHaLeadershipControl().get();
    haLeadershipControl.revokeJobMasterLeadership(jobId).get();
    afterFailAction.run();
    haLeadershipControl.grantJobMasterLeadership(jobId).get();
  }

  private static void restartTaskManager(Runnable afterFailAction, MiniCluster miniCluster)
      throws Exception {
    miniCluster.terminateTaskManager(0).get();
    afterFailAction.run();
    miniCluster.startTaskManager();
  }

  private List<RowData> collectRecordsFromUnboundedStream(
      final ClientAndIterator<RowData> client, final int numElements) {

    checkNotNull(client, "client");
    checkArgument(numElements > 0, "numElement must be > 0");

    final ArrayList<RowData> result = new ArrayList<>(numElements);
    final Iterator<RowData> iterator = client.iterator;

    CollectTask collectTask = new CollectTask(result, iterator, numElements);
    new Thread(collectTask).start();

    long start = System.currentTimeMillis();
    final long timeout = 60 * 1000;
    long intervalOneSecond = 1;
    while (collectTask.running) {
      // TODO a more proper timeout strategy?
      long timeFlies = System.currentTimeMillis() - start;
      if (timeFlies / 1000 >= intervalOneSecond) {
        LOG.info("Time flies: {} ms.", timeFlies);
        intervalOneSecond++;
      }
      if (System.currentTimeMillis() - start > timeout) {
        LOG.error(
            "This task [{}] try to collect records from unbounded stream but timeout {}. As of now, collect result:{}.",
            client.client.getJobID().toString(),
            timeout,
            result.toArray());
        break;
      }
    }

    Assert.assertEquals(
        String.format(
            "The stream ended before reaching the requested %d records. Only %d records were received, received list:%s.",
            numElements, result.size(), Arrays.toString(result.toArray())),
        numElements,
        result.size());

    return result;
  }

  private static class CollectTask implements Runnable {
    final ArrayList<RowData> result;
    final Iterator<RowData> iterator;
    final int limit;

    boolean running = true;

    public CollectTask(ArrayList<RowData> result, Iterator<RowData> iterator, int limit) {
      this.result = result;
      this.iterator = iterator;
      this.limit = limit;
    }

    @Override
    public void run() {
      while (iterator.hasNext()) {
        result.add(convert(iterator.next()));
        if (result.size() == limit) {
          running = false;
          return;
        }
      }
    }
  }

  private ClientAndIterator<RowData> executeAndCollectWithClient(
      StreamExecutionEnvironment env, MixedFormatSource<RowData> mixedFormatSource)
      throws Exception {
    final DataStreamSource<RowData> source =
        env.fromSource(
                mixedFormatSource, WatermarkStrategy.noWatermarks(), "MixedFormatParallelSource")
            .setParallelism(PARALLELISM);
    return DataStreamUtils.collectWithClient(source, "job_" + name.getMethodName());
  }

  private static GenericRowData convert(RowData row) {
    GenericRowData rowData = new GenericRowData(row.getRowKind(), row.getArity());
    rowData.setField(0, row.getInt(0));
    rowData.setField(1, row.getString(1));
    rowData.setField(2, row.getLong(2));
    rowData.setField(3, row.getTimestamp(3, 6));
    return rowData;
  }

  private static void expireSnapshots(
      UnkeyedTable tableStore, long olderThan, Set<String> exclude) {
    LOG.debug("start expire snapshots, the exclude is {}", exclude);
    final AtomicInteger toDeleteFiles = new AtomicInteger(0);
    final AtomicInteger deleteFiles = new AtomicInteger(0);
    Set<String> parentDirectory = new HashSet<>();
    tableStore
        .expireSnapshots()
        .retainLast(1)
        .expireOlderThan(olderThan)
        .deleteWith(
            file -> {
              try {
                if (!exclude.contains(file)
                    && !exclude.contains(new Path(file).getParent().toString())) {
                  tableStore.io().deleteFile(file);
                }
                parentDirectory.add(new Path(file).getParent().toString());
                deleteFiles.incrementAndGet();
              } catch (Throwable t) {
                LOG.warn("failed to delete file {}", file, t);
              } finally {
                toDeleteFiles.incrementAndGet();
              }
            })
        .cleanExpiredFiles(true)
        .commit();
    parentDirectory.forEach(
        parent -> TableFileUtil.deleteEmptyDirectory(tableStore.io(), parent, exclude));
    LOG.info("to delete {} files, success delete {} files", toDeleteFiles.get(), deleteFiles.get());
  }

  private MixedFormatSource<RowData> initMixedFormatSource(boolean isStreaming) {
    return initMixedFormatSource(isStreaming, SCAN_STARTUP_MODE_EARLIEST);
  }

  private MixedFormatSource<RowData> initMixedFormatSourceWithLatest() {
    return initMixedFormatSource(true, SCAN_STARTUP_MODE_LATEST);
  }

  private MixedFormatSource<RowData> initMixedFormatSource(
      boolean isStreaming, String scanStartupMode) {
    MixedFormatTableLoader tableLoader = initLoader();
    MixedFormatScanContext mixedFormatScanContext =
        initMixedFormatScanContext(isStreaming, scanStartupMode);
    ReaderFunction<RowData> rowDataReaderFunction = initRowDataReadFunction();
    TypeInformation<RowData> typeInformation =
        InternalTypeInfo.of(FlinkSchemaUtil.convert(testKeyedTable.schema()));

    return new MixedFormatSource<>(
        tableLoader,
        mixedFormatScanContext,
        rowDataReaderFunction,
        typeInformation,
        testKeyedTable.name(),
        false);
  }

  private MixedFormatSource<RowData> initMixedFormatSourceWithMonitorInterval(
      boolean isStreaming,
      String scanStartupMode,
      TableIdentifier tableIdentifier,
      Duration monitorInterval) {
    MixedFormatTableLoader tableLoader = MixedFormatTableLoader.of(tableIdentifier, catalogBuilder);
    MixedFormatScanContext mixedFormatScanContext =
        initMixedFormatScanContext(isStreaming, scanStartupMode, monitorInterval);
    MixedTable table = MixedFormatUtils.loadMixedTable(tableLoader);
    ReaderFunction<RowData> rowDataReaderFunction = initRowDataReadFunction(table.asKeyedTable());
    TypeInformation<RowData> typeInformation =
        InternalTypeInfo.of(FlinkSchemaUtil.convert(table.schema()));

    return new MixedFormatSource<>(
        tableLoader,
        mixedFormatScanContext,
        rowDataReaderFunction,
        typeInformation,
        table.name(),
        false);
  }

  private MixedFormatSource<RowData> initMixedFormatSource(
      boolean isStreaming, String scanStartupMode, TableIdentifier tableIdentifier) {
    return initMixedFormatSourceWithMonitorInterval(
        isStreaming, scanStartupMode, tableIdentifier, Duration.ofMillis(500));
  }

  private MixedFormatSource<RowData> initMixedFormatDimSource(boolean isStreaming) {
    MixedFormatTableLoader tableLoader = initLoader();
    MixedFormatScanContext mixedFormatScanContext =
        initMixedFormatScanContext(isStreaming, SCAN_STARTUP_MODE_EARLIEST);
    ReaderFunction<RowData> rowDataReaderFunction = initRowDataReadFunction();
    Schema schema = testKeyedTable.schema();
    Schema schemaWithWm =
        TypeUtil.join(
            schema,
            new Schema(Types.NestedField.of(-1, true, "opt", Types.TimestampType.withoutZone())));
    TypeInformation<RowData> typeInformation =
        InternalTypeInfo.of(FlinkSchemaUtil.convert(schemaWithWm));

    return new MixedFormatSource<>(
        tableLoader,
        mixedFormatScanContext,
        rowDataReaderFunction,
        typeInformation,
        testKeyedTable.name(),
        true);
  }

  private RowDataReaderFunction initRowDataReadFunction() {
    return initRowDataReadFunction(testKeyedTable);
  }

  private RowDataReaderFunction initRowDataReadFunction(KeyedTable keyedTable) {
    return new RowDataReaderFunction(
        new Configuration(),
        keyedTable.schema(),
        keyedTable.schema(),
        keyedTable.primaryKeySpec(),
        null,
        true,
        keyedTable.io());
  }

  private MixedFormatScanContext initMixedFormatScanContext(
      boolean isStreaming, String scanStartupMode, Duration monitorInterval) {
    return MixedFormatScanContext.contextBuilder()
        .streaming(isStreaming)
        .scanStartupMode(scanStartupMode)
        .monitorInterval(monitorInterval)
        .build();
  }

  private MixedFormatScanContext initMixedFormatScanContext(
      boolean isStreaming, String scanStartupMode) {
    return MixedFormatScanContext.contextBuilder()
        .streaming(isStreaming)
        .scanStartupMode(scanStartupMode)
        .monitorInterval(Duration.ofMillis(500))
        .build();
  }

  private MixedFormatTableLoader initLoader() {
    return MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder);
  }

  // ------------------------------------------------------------------------
  //  mini cluster failover utilities
  // ------------------------------------------------------------------------

  private static class RecordCounterToFail {

    private static AtomicInteger records;
    private static CompletableFuture<Void> fail;
    private static CompletableFuture<Void> continueProcessing;

    private static <T> DataStream<T> wrapWithFailureAfter(DataStream<T> stream, int failAfter) {

      records = new AtomicInteger();
      fail = new CompletableFuture<>();
      continueProcessing = new CompletableFuture<>();
      return stream.map(
          record -> {
            final boolean halfOfInputIsRead = records.incrementAndGet() > failAfter;
            final boolean notFailedYet = !fail.isDone();
            if (notFailedYet && halfOfInputIsRead) {
              fail.complete(null);
              continueProcessing.get();
            }
            return record;
          });
    }

    private static void waitToFail() throws ExecutionException, InterruptedException {
      fail.get();
    }

    private static void continueProcessing() {
      continueProcessing.complete(null);
    }
  }

  private static class WatermarkAwareFailWrapper {

    private static WatermarkFailoverTestOperator op;
    private static long watermarkAfterFailover = -1;
    private static final AtomicInteger watermarkCounter = new AtomicInteger(0);

    public static long getWatermarkAfterFailover() {
      return watermarkAfterFailover;
    }

    private static DataStream<RowData> wrapWithFailureAfter(DataStream<RowData> stream) {
      op = new WatermarkFailoverTestOperator();
      return stream.transform("watermark failover", TypeInformation.of(RowData.class), op);
    }

    private static void waitToFail() throws InterruptedException {
      op.waitToFail();
    }

    private static void continueProcessing() {
      op.continueProcessing();
    }

    static class WatermarkFailoverTestOperator extends AbstractStreamOperator<RowData>
        implements OneInputStreamOperator<RowData, RowData> {

      private static final long serialVersionUID = 1L;
      private static boolean fail = false;
      private static boolean failoverHappened = false;

      public WatermarkFailoverTestOperator() {
        super();
        chainingStrategy = ChainingStrategy.ALWAYS;
      }

      private void waitToFail() throws InterruptedException {
        while (!fail) {
          LOG.info("Waiting to fail");
          Thread.sleep(1000);
        }
      }

      private void continueProcessing() {
        failoverHappened = true;
        LOG.info("failover happened");
      }

      @Override
      public void open() throws Exception {
        super.open();
      }

      @Override
      public void processElement(StreamRecord<RowData> element) throws Exception {
        output.collect(element);
      }

      @Override
      public void processWatermark(Watermark mark) throws Exception {
        LOG.info("processWatermark: {}", mark);
        if (!failoverHappened && mark.getTimestamp() > 0) {
          fail = true;
        }
        if (failoverHappened) {
          LOG.info("failover happened, watermark: {}", mark);
          Assert.assertEquals(Long.MAX_VALUE, mark.getTimestamp());
          if (watermarkAfterFailover == -1) {
            watermarkAfterFailover = mark.getTimestamp();
          } else {
            watermarkAfterFailover = Math.min(watermarkAfterFailover, mark.getTimestamp());
          }
          watermarkCounter.incrementAndGet();
        }
        super.processWatermark(mark);
      }
    }
  }
}

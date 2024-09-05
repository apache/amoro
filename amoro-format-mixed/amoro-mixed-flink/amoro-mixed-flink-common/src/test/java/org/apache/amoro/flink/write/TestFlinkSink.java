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
import org.apache.amoro.flink.util.DataUtil;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestFlinkSink extends FlinkTestBase {

  public TestFlinkSink(boolean isKeyed) {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(isKeyed, false));
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection parameters() {
    return Arrays.asList(new Object[][] {{true}, {false}});
  }

  @Test
  public void testKeyedSink() throws Exception {
    Assume.assumeTrue(isKeyedTable());
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    KeyedTable testKeyedTable = getMixedTable().asKeyedTable();

    env.enableCheckpointing(2000, CheckpointingMode.EXACTLY_ONCE);
    env.getCheckpointConfig()
        .enableExternalizedCheckpoints(
            CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

    List<Object[]> data = new LinkedList<>();
    data.add(
        new Object[] {
          1000004,
          "a",
          LocalDateTime.parse("2022-06-17T10:10:11.0").toEpochSecond(ZoneOffset.UTC),
          LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          1000015,
          "b",
          LocalDateTime.parse("2022-06-17T10:08:11.0").toEpochSecond(ZoneOffset.UTC),
          LocalDateTime.parse("2022-06-17T10:08:11.0")
        });
    data.add(
        new Object[] {
          1000011,
          "c",
          LocalDateTime.parse("2022-06-18T10:10:11.0").toEpochSecond(ZoneOffset.UTC),
          LocalDateTime.parse("2022-06-18T10:10:11.0")
        });
    data.add(
        new Object[] {
          1000014,
          "d",
          LocalDateTime.parse("2022-06-17T10:11:11.0").toEpochSecond(ZoneOffset.UTC),
          LocalDateTime.parse("2022-06-17T10:11:11.0")
        });
    data.add(
        new Object[] {
          1000021,
          "d",
          LocalDateTime.parse("2022-06-17T16:10:11.0").toEpochSecond(ZoneOffset.UTC),
          LocalDateTime.parse("2022-06-17T16:10:11.0")
        });
    data.add(
        new Object[] {
          1000015,
          "e",
          LocalDateTime.parse("2022-06-17T10:10:11.0").toEpochSecond(ZoneOffset.UTC),
          LocalDateTime.parse("2022-06-17T10:10:11.0")
        });

    DataStream<RowData> input =
        env.fromElements(data.stream().map(DataUtil::toRowData).toArray(RowData[]::new));

    FlinkSink.forRowData(input)
        .context(Optional::of)
        .table(testKeyedTable)
        .tableLoader(MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder))
        .flinkSchema(FLINK_SCHEMA)
        .build();

    env.execute();

    testKeyedTable.changeTable().refresh();
    List<Record> actual = MixedDataTestHelpers.readKeyedTable(testKeyedTable, null);

    Set<Record> expected = toRecords(DataUtil.toRowSet(data));
    Assert.assertEquals(expected, new HashSet<>(actual));
  }

  @Test
  public void testUnkeyedSink() throws Exception {
    Assume.assumeFalse(isKeyedTable());
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    UnkeyedTable testTable = getMixedTable().asUnkeyedTable();

    env.enableCheckpointing(2000, CheckpointingMode.EXACTLY_ONCE);
    env.getCheckpointConfig()
        .enableExternalizedCheckpoints(
            CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

    List<Object[]> data = new LinkedList<>();
    data.add(
        new Object[] {1000004, "a", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {1000015, "b", 1655513411000L, LocalDateTime.parse("2022-06-17T10:08:11.0")});
    data.add(
        new Object[] {1000011, "c", 1655599811000L, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(
        new Object[] {1000014, "d", 1655513411000L, LocalDateTime.parse("2022-06-17T10:11:11.0")});
    data.add(
        new Object[] {1000021, "d", 1655513411000L, LocalDateTime.parse("2022-06-17T16:10:11.0")});
    data.add(
        new Object[] {1000015, "e", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});

    DataStream<RowData> input =
        env.fromElements(data.stream().map(DataUtil::toRowData).toArray(RowData[]::new));

    FlinkSink.forRowData(input)
        .context(Optional::of)
        .table(testTable)
        .tableLoader(MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder))
        .flinkSchema(FLINK_SCHEMA)
        .build();

    env.execute();
    testTable.refresh();
    Set<Record> actual = DataUtil.read(testTable);

    Set<Record> expected = toRecords(DataUtil.toRowSet(data));
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testUnkeyedOverwrite() throws Exception {
    Assume.assumeFalse(isKeyedTable());
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    UnkeyedTable testTable = getMixedTable().asUnkeyedTable();

    env.enableCheckpointing(2000, CheckpointingMode.EXACTLY_ONCE);
    env.getCheckpointConfig()
        .enableExternalizedCheckpoints(
            CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

    List<Object[]> data = new LinkedList<>();
    data.add(
        new Object[] {1000004, "a", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {1000015, "b", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {1000011, "c", 1655599811000L, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(
        new Object[] {1000014, "d", 1655599811000L, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(
        new Object[] {1000021, "d", 1655599811000L, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(
        new Object[] {1000015, "e", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});

    DataStream<RowData> input =
        env.fromElements(data.stream().map(DataUtil::toRowData).toArray(RowData[]::new));

    FlinkSink.forRowData(input)
        .context(Optional::of)
        .table(testTable)
        .tableLoader(MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder))
        .flinkSchema(FLINK_SCHEMA)
        .build();
    env.execute();

    data.clear();
    data.add(new Object[] {12, "d", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {11, "a", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {15, "c", 1655599811000L, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {21, "k", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {91, "l", 1655599811000L, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {74, "m", 1655513411000L, LocalDateTime.parse("2022-06-17T10:10:11.0")});

    DataStream<RowData> overwrite =
        env.fromElements(data.stream().map(DataUtil::toRowData).toArray(RowData[]::new));

    FlinkSink.forRowData(overwrite)
        .context(Optional::of)
        .table(testTable)
        .tableLoader(MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder))
        .overwrite(true)
        .flinkSchema(FLINK_SCHEMA)
        .build();

    env.execute();
    testTable.refresh();
    Set<Record> actual = DataUtil.read(testTable);

    Set<Record> expected = toRecords(DataUtil.toRowSet(data));
    Assert.assertEquals(expected, actual);
  }
}

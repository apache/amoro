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

package org.apache.amoro.flink.table;

import static org.apache.amoro.MockAmoroManagementServer.TEST_CATALOG_NAME;
import static org.apache.amoro.flink.kafka.testutils.KafkaContainerTest.KAFKA_CONTAINER;
import static org.apache.amoro.table.TableProperties.ENABLE_LOG_STORE;
import static org.apache.amoro.table.TableProperties.LOG_STORE_ADDRESS;
import static org.apache.amoro.table.TableProperties.LOG_STORE_MESSAGE_TOPIC;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.flink.FlinkTestBase;
import org.apache.amoro.flink.kafka.testutils.KafkaContainerTest;
import org.apache.amoro.flink.util.DataUtil;
import org.apache.amoro.flink.util.TestUtil;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.table.api.ApiExpression;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestUnkeyed extends FlinkTestBase {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private static final String TABLE = "test_unkeyed";
  private static final String DB = TableTestHelper.TEST_TABLE_ID.getDatabase();

  private String catalog;
  private MixedFormatCatalog mixedFormatCatalog;
  private String db;
  private String topic;

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();
  public boolean isHive;

  public TestUnkeyed(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper, boolean isHive) {
    super(catalogTestHelper, tableTestHelper);
    this.isHive = isHive;
  }

  @Parameterized.Parameters(name = "{0}, {1}, {2}")
  public static Collection parameters() {
    return Arrays.asList(
        new Object[][] {
          {
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, true),
            true
          },
          {
            new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true),
            false
          },
          {
            new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true),
            false
          }
        });
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    KAFKA_CONTAINER.start();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    KAFKA_CONTAINER.close();
  }

  @Before
  public void before() throws Exception {
    if (isHive) {
      catalog = HiveTableTestHelper.TEST_CATALOG_NAME;
      db = HiveTableTestHelper.TEST_DB_NAME;
    } else {
      catalog = TEST_CATALOG_NAME;
      db = DB;
    }
    super.before();
    mixedFormatCatalog = getMixedFormatCatalog();
    topic = String.join(".", catalog, db, TABLE);
    super.config();
  }

  @After
  public void after() {
    sql("DROP TABLE IF EXISTS mixed_catalog." + db + "." + TABLE);
  }

  @Test
  public void testUnPartitionDDL() throws IOException {
    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, age SMALLINT, sex TINYINT, score BIGINT, height FLOAT, speed DOUBLE, ts TIMESTAMP)");

    MixedTable table =
        mixedFormatCatalog.loadTable(TableIdentifier.of(catalog, db, TestUnkeyed.TABLE));

    Schema required =
        new Schema(
            Types.NestedField.optional(1, "id", Types.IntegerType.get()),
            Types.NestedField.optional(2, "name", Types.StringType.get()),
            Types.NestedField.optional(3, "age", Types.IntegerType.get()),
            Types.NestedField.optional(4, "sex", Types.IntegerType.get()),
            Types.NestedField.optional(5, "score", Types.LongType.get()),
            Types.NestedField.optional(6, "height", Types.FloatType.get()),
            Types.NestedField.optional(7, "speed", Types.DoubleType.get()),
            Types.NestedField.optional(8, "ts", Types.TimestampType.withoutZone()));
    Assert.assertEquals(required.asStruct(), table.schema().asStruct());
  }

  @Test
  public void testPartitionDDL() throws IOException {
    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, age SMALLINT, sex TINYINT, score BIGINT, height FLOAT, speed DOUBLE, ts TIMESTAMP)"
            + " PARTITIONED BY (ts)");

    Schema required =
        new Schema(
            Types.NestedField.optional(1, "id", Types.IntegerType.get()),
            Types.NestedField.optional(2, "name", Types.StringType.get()),
            Types.NestedField.optional(3, "age", Types.IntegerType.get()),
            Types.NestedField.optional(4, "sex", Types.IntegerType.get()),
            Types.NestedField.optional(5, "score", Types.LongType.get()),
            Types.NestedField.optional(6, "height", Types.FloatType.get()),
            Types.NestedField.optional(7, "speed", Types.DoubleType.get()),
            Types.NestedField.optional(8, "ts", Types.TimestampType.withoutZone()));
    MixedTable table = mixedFormatCatalog.loadTable(TableIdentifier.of(catalog, db, TABLE));
    Assert.assertEquals(required.asStruct(), table.schema().asStruct());

    PartitionSpec requiredSpec = PartitionSpec.builderFor(required).identity("ts").build();
    Assert.assertEquals(requiredSpec, table.spec());
  }

  @Test
  public void testUnkeyedWatermarkSet() throws Exception {
    List<Object[]> data = new LinkedList<>();

    data.add(new Object[] {1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {1000015, "b", LocalDateTime.parse("2022-06-17T10:08:11.0")});
    data.add(new Object[] {1000011, "c", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {1000014, "d", LocalDateTime.parse("2022-06-17T10:11:11.0")});
    data.add(new Object[] {1000021, "d", LocalDateTime.parse("2022-06-17T16:10:11.0")});
    data.add(new Object[] {1000007, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});

    List<ApiExpression> rows = DataUtil.toRows(data);

    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING()),
                    DataTypes.FIELD("ts", DataTypes.TIMESTAMP())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, ts TIMESTAMP)");

    sql(
        "create table user_tb ("
            + "    rtime as cast(ts as timestamp(3)),"
            + "    WATERMARK FOR rtime as rtime"
            + "  ) LIKE mixed_catalog."
            + db
            + "."
            + TABLE);

    sql("insert into mixed_catalog." + db + "." + TABLE + " select * from input");

    TableResult result =
        exec(
            "select id, name, ts from user_tb"
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='file'"
                + ", 'scan.startup.mode'='earliest'"
                + ")*/");

    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (Object[] datum : data) {
        actual.add(iterator.next());
      }
    }
    Assert.assertEquals(DataUtil.toRowSet(data), actual);

    result.getJobClient().ifPresent(TestUtil::cancelJob);
  }

  @Test
  public void testSinkBatchRead() throws IOException {
    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {1000015, "b", LocalDateTime.parse("2022-06-17T10:08:11.0")});
    data.add(new Object[] {1000011, "c", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {1000014, "d", LocalDateTime.parse("2022-06-17T10:11:11.0")});
    data.add(new Object[] {1000021, "d", LocalDateTime.parse("2022-06-17T16:10:11.0")});
    data.add(new Object[] {1000007, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});

    List<ApiExpression> rows = DataUtil.toRows(data);

    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING()),
                    DataTypes.FIELD("op_time", DataTypes.TIMESTAMP())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP)");

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + "/*+ OPTIONS('mixed-format.emit.mode'='file')*/ select * from input");

    MixedTable table = mixedFormatCatalog.loadTable(TableIdentifier.of(catalog, db, TABLE));
    Iterable<Snapshot> snapshots = table.asUnkeyedTable().snapshots();
    Snapshot s = snapshots.iterator().next();

    Assert.assertEquals(
        DataUtil.toRowSet(data),
        new HashSet<>(
            sql(
                "select * from mixed_catalog."
                    + db
                    + "."
                    + TABLE
                    + "/*+ OPTIONS("
                    + "'mixed-format.read.mode'='file'"
                    + ", 'streaming'='false'"
                    + ", 'snapshot-id'='"
                    + s.snapshotId()
                    + "'"
                    + ")*/")));
  }

  @Test
  public void testSinkStreamRead() throws Exception {
    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a"});
    data.add(new Object[] {1000015, "b"});
    data.add(new Object[] {1000011, "c"});
    data.add(new Object[] {1000014, "d"});
    data.add(new Object[] {1000021, "d"});
    data.add(new Object[] {1000007, "e"});

    List<ApiExpression> rows = DataUtil.toRows(data);

    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));
    sql("CREATE TABLE IF NOT EXISTS mixed_catalog." + db + "." + TABLE + "(id INT, name STRING)");

    sql("insert into mixed_catalog." + db + "." + TABLE + " select * from input");

    // verify in earliest scan-startup-mode file read
    TableResult resultWithEarliestPosition =
        exec(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + "/*+ OPTIONS("
                + "'streaming'='true'"
                + ", 'mixed-format.read.mode'='file'"
                + ", 'scan.startup.mode'='earliest'"
                + ", 'monitor-interval'='1s'"
                + ")*/");

    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = resultWithEarliestPosition.collect()) {
      for (int i = 0; i < data.size(); i++) {
        actual.add(iterator.next());
      }
    }
    resultWithEarliestPosition.getJobClient().ifPresent(TestUtil::cancelJob);
    Assert.assertEquals(DataUtil.toRowSet(data), actual);

    // verify in latest scan-startup-mode file read
    TableResult resultWithLatestPosition =
        exec(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + "/*+ OPTIONS("
                + "'streaming'='true'"
                + ", 'mixed-format.read.mode'='file'"
                + ", 'scan.startup.mode'='latest'"
                + ", 'monitor-interval'='1s'"
                + ")*/");

    List<Object[]> appendData = new LinkedList<>();
    appendData.add(new Object[] {2000004, "a"});
    appendData.add(new Object[] {2000015, "b"});
    appendData.add(new Object[] {2000011, "c"});
    appendData.add(new Object[] {2000014, "d"});
    appendData.add(new Object[] {2000021, "d"});
    appendData.add(new Object[] {2000007, "e"});

    List<ApiExpression> appendRows = DataUtil.toRows(appendData);

    Table appendInput =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING())),
                appendRows);
    getTableEnv().createTemporaryView("appendInput", appendInput);

    actual.clear();
    try (CloseableIterator<Row> iterator = resultWithLatestPosition.collect()) {
      sql("insert into mixed_catalog." + db + "." + TABLE + " select * from appendInput");
      for (int i = 0; i < appendData.size(); i++) {
        Assert.assertTrue("Should have more records", iterator.hasNext());
        actual.add(iterator.next());
      }
    }
    resultWithLatestPosition.getJobClient().ifPresent(TestUtil::cancelJob);
    Assert.assertEquals(DataUtil.toRowSet(appendData), actual);
  }

  @Test
  public void testLogSinkSource() throws Exception {
    String topic = this.topic + "testLogSinkSource";
    KafkaContainerTest.createTopics(KAFKA_PARTITION_NUMS, 1, topic);

    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a"});
    data.add(new Object[] {1000015, "b"});
    data.add(new Object[] {1000011, "c"});
    data.add(new Object[] {1000014, "d"});
    data.add(new Object[] {1000021, "d"});
    data.add(new Object[] {1000007, "e"});

    List<ApiExpression> rows = DataUtil.toRows(data);
    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(ENABLE_LOG_STORE, "true");
    tableProperties.put(
        LOG_STORE_ADDRESS, KafkaContainerTest.KAFKA_CONTAINER.getBootstrapServers());
    tableProperties.put(LOG_STORE_MESSAGE_TOPIC, topic);
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " /*+ OPTIONS("
            + "'mixed-format.emit.mode'='log'"
            + ", 'log.version'='v1'"
            + ") */"
            + " select * from input");

    TableResult result =
        exec(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='log'"
                + ", 'scan.startup.mode'='earliest'"
                + ")*/");

    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (Object[] datum : data) {
        actual.add(iterator.next());
      }
    }
    Assert.assertEquals(DataUtil.toRowSet(data), actual);

    result.getJobClient().ifPresent(TestUtil::cancelJob);
    KafkaContainerTest.deleteTopics(topic);
  }

  @Test
  public void testUnpartitionLogSinkSourceWithSelectedFields() throws Exception {
    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {1000015, "b", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {1000011, "c", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {1000014, "d", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {1000015, "d", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {1000007, "e", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {1000007, "e", LocalDateTime.parse("2022-06-18T10:10:11.0")});

    List<ApiExpression> rows = DataUtil.toRows(data);
    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING()),
                    DataTypes.FIELD("op_time", DataTypes.TIMESTAMP())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(ENABLE_LOG_STORE, "true");
    tableProperties.put(
        LOG_STORE_ADDRESS, KafkaContainerTest.KAFKA_CONTAINER.getBootstrapServers());
    tableProperties.put(LOG_STORE_MESSAGE_TOPIC, topic);
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " /*+ OPTIONS("
            + "'mixed-format.emit.mode'='log'"
            + ", 'log.version'='v1'"
            + ") */"
            + " select * from input");

    TableResult result =
        exec(
            "select id, op_time from mixed_catalog."
                + db
                + "."
                + TABLE
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='log'"
                + ", 'scan.startup.mode'='earliest'"
                + ")*/");

    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (Object[] datum : data) {
        actual.add(iterator.next());
      }
    }

    List<Object[]> expected = new LinkedList<>();
    expected.add(new Object[] {1000004, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {1000015, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {1000011, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {1000014, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    expected.add(new Object[] {1000015, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    expected.add(new Object[] {1000007, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    expected.add(new Object[] {1000007, LocalDateTime.parse("2022-06-18T10:10:11.0")});

    Assert.assertEquals(DataUtil.toRowSet(expected), actual);

    result.getJobClient().ifPresent(TestUtil::cancelJob);
  }

  @Test
  public void testUnPartitionDoubleSink() throws Exception {
    String topic = this.topic + "testUnPartitionDoubleSink";
    KafkaContainerTest.createTopics(KAFKA_PARTITION_NUMS, 1, topic);

    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a"});
    data.add(new Object[] {1000015, "b"});
    data.add(new Object[] {1000011, "c"});
    data.add(new Object[] {1000014, "d"});
    data.add(new Object[] {1000021, "d"});
    data.add(new Object[] {1000007, "e"});

    List<ApiExpression> rows = DataUtil.toRows(data);
    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING())),
                rows);
    getTableEnv().createTemporaryView("input", input);
    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(ENABLE_LOG_STORE, "true");
    tableProperties.put(
        LOG_STORE_ADDRESS, KafkaContainerTest.KAFKA_CONTAINER.getBootstrapServers());
    tableProperties.put(LOG_STORE_MESSAGE_TOPIC, topic);
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " /*+ OPTIONS("
            + "'mixed-format.emit.mode'='file, log'"
            + ", 'log.version'='v1'"
            + ") */"
            + "select id, name from input");

    Assert.assertEquals(
        DataUtil.toRowSet(data),
        sqlSet(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + " /*+ OPTIONS('mixed-format.read.mode'='file', 'streaming'='false') */"));

    TableResult result =
        exec(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + " /*+ OPTIONS('mixed-format.read.mode'='log', 'scan.startup.mode'='earliest') */");
    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (Object[] datum : data) {
        actual.add(iterator.next());
      }
    }
    Assert.assertEquals(DataUtil.toRowSet(data), actual);
    result.getJobClient().ifPresent(TestUtil::cancelJob);
    KafkaContainerTest.deleteTopics(topic);
  }

  @Test
  public void testPartitionSinkBatchRead() throws IOException {
    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a", "2022-05-17"});
    data.add(new Object[] {1000015, "b", "2022-05-17"});
    data.add(new Object[] {1000011, "c", "2022-05-17"});
    data.add(new Object[] {1000014, "d", "2022-05-18"});
    data.add(new Object[] {1000021, "d", "2022-05-18"});
    data.add(new Object[] {1000007, "e", "2022-05-18"});

    List<Object[]> expected = new LinkedList<>();
    expected.add(new Object[] {1000014, "d", "2022-05-18"});
    expected.add(new Object[] {1000021, "d", "2022-05-18"});
    expected.add(new Object[] {1000007, "e", "2022-05-18"});

    List<ApiExpression> rows = DataUtil.toRows(data);

    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING()),
                    DataTypes.FIELD("dt", DataTypes.STRING())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, dt STRING)"
            + " PARTITIONED BY (dt)");

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " PARTITION (dt='2022-05-18') select id, name from input"
            + " where dt='2022-05-18' ");

    TableIdentifier identifier = TableIdentifier.of(catalog, db, TABLE);
    MixedTable table = mixedFormatCatalog.loadTable(identifier);
    Iterable<Snapshot> snapshots = table.asUnkeyedTable().snapshots();
    Snapshot s = snapshots.iterator().next();

    Assert.assertEquals(
        DataUtil.toRowSet(expected),
        sqlSet(
            "select * from mixed_catalog."
                + db
                + "."
                + TestUnkeyed.TABLE
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='file'"
                + ", 'snapshot-id'='"
                + s.snapshotId()
                + "'"
                + ", 'streaming'='false'"
                + ")*/"));
    Assert.assertEquals(
        DataUtil.toRowSet(expected),
        sqlSet(
            "select * from mixed_catalog."
                + db
                + "."
                + TestUnkeyed.TABLE
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='file'"
                + ", 'as-of-timestamp'='"
                + s.timestampMillis()
                + "'"
                + ", 'streaming'='false'"
                + ")*/"));
  }

  @Test
  public void testPartitionSinkStreamRead() throws Exception {
    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a", "2022-05-17"});
    data.add(new Object[] {1000015, "b", "2022-05-17"});
    data.add(new Object[] {1000011, "c", "2022-05-17"});
    data.add(new Object[] {1000014, "d", "2022-05-18"});
    data.add(new Object[] {1000021, "d", "2022-05-18"});
    data.add(new Object[] {1000007, "e", "2022-05-18"});

    List<ApiExpression> rows = DataUtil.toRows(data);

    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING()),
                    DataTypes.FIELD("dt", DataTypes.STRING())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, dt STRING)"
            + " PARTITIONED BY (dt)");

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " PARTITION (dt='2022-05-18') select id, name from input"
            + " where dt='2022-05-18' ");
    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " PARTITION (dt='2022-05-18') select id, name from input"
            + " where dt='2022-05-18' ");

    TableIdentifier identifier = TableIdentifier.of(catalog, db, TABLE);
    MixedTable table = mixedFormatCatalog.loadTable(identifier);
    Iterable<Snapshot> snapshots = table.asUnkeyedTable().snapshots();
    Snapshot s = snapshots.iterator().next();

    TableResult result =
        exec(
            "select * from mixed_catalog."
                + db
                + "."
                + TestUnkeyed.TABLE
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='file'"
                + ", 'start-snapshot-id'='"
                + s.snapshotId()
                + "'"
                + ")*/");

    List<Row> expected =
        new ArrayList<Row>() {
          {
            add(Row.of(1000014, "d", "2022-05-18"));
            add(Row.of(1000021, "d", "2022-05-18"));
            add(Row.of(1000007, "e", "2022-05-18"));
          }
        };

    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (int i = 0; i < expected.size(); i++) {
        actual.add(iterator.next());
      }
    }
    result.getJobClient().ifPresent(TestUtil::cancelJob);
    Assert.assertEquals(new HashSet<>(expected), actual);
  }

  @Test
  public void testPartitionLogSinkSource() throws Exception {
    String topic = this.topic + "testUnKeyedPartitionLogSinkSource";
    KafkaContainerTest.createTopics(KAFKA_PARTITION_NUMS, 1, topic);

    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a", "2022-05-17"});
    data.add(new Object[] {1000015, "b", "2022-05-17"});
    data.add(new Object[] {1000011, "c", "2022-05-17"});
    data.add(new Object[] {1000014, "d", "2022-05-18"});
    data.add(new Object[] {1000021, "d", "2022-05-18"});
    data.add(new Object[] {1000007, "e", "2022-05-18"});

    List<ApiExpression> rows = DataUtil.toRows(data);

    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING()),
                    DataTypes.FIELD("dt", DataTypes.STRING())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(ENABLE_LOG_STORE, "true");
    tableProperties.put(LOG_STORE_ADDRESS, KAFKA_CONTAINER.getBootstrapServers());
    tableProperties.put(LOG_STORE_MESSAGE_TOPIC, topic);
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, dt STRING) PARTITIONED BY (dt) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " /*+ OPTIONS("
            + "'mixed-format.emit.mode'='log'"
            + ", 'log.version'='v1'"
            + ") */"
            + " select * from input");

    TableResult result =
        exec(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='log'"
                + ", 'scan.startup.mode'='earliest'"
                + ")*/");

    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (Object[] datum : data) {
        actual.add(iterator.next());
      }
    }
    Assert.assertEquals(DataUtil.toRowSet(data), actual);

    result.getJobClient().ifPresent(TestUtil::cancelJob);
    KafkaContainerTest.deleteTopics(topic);
  }

  @Test
  public void testPartitionLogSinkSourceWithSelectedFields() throws Exception {
    String topic = this.topic + "testPartitionLogSinkSourceWithSelectedFields";
    KafkaContainerTest.createTopics(KAFKA_PARTITION_NUMS, 1, topic);

    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {1000015, "b", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {1000011, "c", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[] {1000014, "d", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {1000015, "d", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {1000007, "e", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[] {1000007, "e", LocalDateTime.parse("2022-06-18T10:10:11.0")});

    List<ApiExpression> rows = DataUtil.toRows(data);
    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING()),
                    DataTypes.FIELD("op_time", DataTypes.TIMESTAMP())),
                rows);
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(ENABLE_LOG_STORE, "true");
    tableProperties.put(LOG_STORE_ADDRESS, KAFKA_CONTAINER.getBootstrapServers());
    tableProperties.put(LOG_STORE_MESSAGE_TOPIC, topic);
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP) PARTITIONED BY (op_time) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " /*+ OPTIONS("
            + "'mixed-format.emit.mode'='log'"
            + ", 'log.version'='v1'"
            + ") */"
            + " select * from input");

    TableResult result =
        exec(
            "select id, op_time from mixed_catalog."
                + db
                + "."
                + TABLE
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='log'"
                + ", 'scan.startup.mode'='earliest'"
                + ")*/");

    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (Object[] datum : data) {
        actual.add(iterator.next());
      }
    }

    List<Object[]> expected = new LinkedList<>();
    expected.add(new Object[] {1000004, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {1000015, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {1000011, LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {1000014, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    expected.add(new Object[] {1000015, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    expected.add(new Object[] {1000007, LocalDateTime.parse("2022-06-18T10:10:11.0")});
    expected.add(new Object[] {1000007, LocalDateTime.parse("2022-06-18T10:10:11.0")});

    Assert.assertEquals(DataUtil.toRowSet(expected), actual);

    result.getJobClient().ifPresent(TestUtil::cancelJob);
    KafkaContainerTest.deleteTopics(topic);
  }

  @Test
  public void testPartitionDoubleSink() throws Exception {
    String topic = this.topic + "testUnkeyedPartitionDoubleSink";
    KafkaContainerTest.createTopics(KAFKA_PARTITION_NUMS, 1, topic);

    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a", "2022-05-17"});
    data.add(new Object[] {1000015, "b", "2022-05-17"});
    data.add(new Object[] {1000011, "c", "2022-05-17"});
    data.add(new Object[] {1000014, "d", "2022-05-18"});
    data.add(new Object[] {1000021, "d", "2022-05-18"});
    data.add(new Object[] {1000007, "e", "2022-05-18"});

    List<ApiExpression> rows = DataUtil.toRows(data);

    Table input =
        getTableEnv()
            .fromValues(
                DataTypes.ROW(
                    DataTypes.FIELD("id", DataTypes.INT()),
                    DataTypes.FIELD("name", DataTypes.STRING()),
                    DataTypes.FIELD("dt", DataTypes.STRING())),
                rows);
    getTableEnv().createTemporaryView("input", input);
    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(ENABLE_LOG_STORE, "true");
    tableProperties.put(LOG_STORE_ADDRESS, KAFKA_CONTAINER.getBootstrapServers());
    tableProperties.put(LOG_STORE_MESSAGE_TOPIC, topic);
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, dt STRING) PARTITIONED BY (dt) WITH %s",
        toWithClause(tableProperties));
    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " /*+ OPTIONS("
            + "'mixed-format.emit.mode'='file, log'"
            + ", 'log.version'='v1'"
            + ") */"
            + "select * from input");

    Assert.assertEquals(
        DataUtil.toRowSet(data),
        sqlSet(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + " /*+ OPTIONS('mixed-format.read.mode'='file', 'streaming'='false') */"));
    TableResult result =
        exec(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + " /*+ OPTIONS('mixed-format.read.mode'='log', 'scan.startup.mode'='earliest') */");
    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (Object[] datum : data) {
        actual.add(iterator.next());
      }
    }
    Assert.assertEquals(DataUtil.toRowSet(data), actual);
    result.getJobClient().ifPresent(TestUtil::cancelJob);
    KafkaContainerTest.deleteTopics(topic);
  }
}

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

import static org.apache.amoro.table.TableProperties.ENABLE_LOG_STORE;
import static org.apache.amoro.table.TableProperties.LOG_STORE_ADDRESS;
import static org.apache.amoro.table.TableProperties.LOG_STORE_MESSAGE_TOPIC;
import static org.apache.amoro.table.TableProperties.LOG_STORE_STORAGE_TYPE_KAFKA;
import static org.apache.amoro.table.TableProperties.LOG_STORE_TYPE;
import static org.apache.flink.table.api.Expressions.$;

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
import org.apache.amoro.table.TableProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.ApiExpression;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.runtime.typeutils.InternalTypeInfo;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.CloseableIterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestKeyed extends FlinkTestBase {

  public static final Logger LOG = LoggerFactory.getLogger(TestKeyed.class);

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  @Rule public TestName testName = new TestName();
  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  private static final String DB = TableTestHelper.TEST_TABLE_ID.getDatabase();
  private static final String TABLE = "test_keyed";

  private String catalog;
  private String db;
  private String topic;
  private final Map<String, String> tableProperties = new HashMap<>();
  public boolean isHive;

  public TestKeyed(
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
    FlinkTestBase.prepare();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    FlinkTestBase.shutdown();
  }

  @Before
  public void before() throws Exception {
    if (isHive) {
      db = HiveTableTestHelper.TEST_DB_NAME;
    } else {
      db = DB;
    }
    super.before();
    prepareLog();
    super.config();
  }

  @After
  public void after() {
    sql("DROP TABLE IF EXISTS mixed_catalog." + db + "." + TABLE);
  }

  private void prepareLog() {
    topic = TestUtil.getUtMethodName(testName) + isHive;
    tableProperties.clear();
    tableProperties.put(ENABLE_LOG_STORE, "true");
    tableProperties.put(LOG_STORE_MESSAGE_TOPIC, topic);
    KafkaContainerTest.createTopics(KAFKA_PARTITION_NUMS, 1, topic);
    tableProperties.put(LOG_STORE_TYPE, LOG_STORE_STORAGE_TYPE_KAFKA);
    tableProperties.put(
        LOG_STORE_ADDRESS, KafkaContainerTest.KAFKA_CONTAINER.getBootstrapServers());
  }

  @Test
  public void testSinkSourceFile() throws IOException {

    List<Object[]> data = new LinkedList<>();
    data.add(
        new Object[] {
          RowKind.INSERT,
          1000004,
          "a",
          LocalDateTime.parse("2022-06-17T10:10:11.0"),
          LocalDateTime.parse("2022-06-17T10:10:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });
    data.add(
        new Object[] {
          RowKind.DELETE,
          1000015,
          "b",
          LocalDateTime.parse("2022-06-17T10:08:11.0"),
          LocalDateTime.parse("2022-06-17T10:08:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });
    data.add(
        new Object[] {
          RowKind.DELETE,
          1000011,
          "c",
          LocalDateTime.parse("2022-06-18T10:10:11.0"),
          LocalDateTime.parse("2022-06-18T10:10:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE,
          1000021,
          "d",
          LocalDateTime.parse("2022-06-17T10:11:11.0"),
          LocalDateTime.parse("2022-06-17T10:11:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER,
          1000021,
          "e",
          LocalDateTime.parse("2022-06-17T10:11:11.0"),
          LocalDateTime.parse("2022-06-17T10:11:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });
    data.add(
        new Object[] {
          RowKind.INSERT,
          1000015,
          "e",
          LocalDateTime.parse("2022-06-17T10:10:11.0"),
          LocalDateTime.parse("2022-06-17T10:10:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });

    DataStream<RowData> source =
        getEnv()
            .fromCollection(
                DataUtil.toRowData(data),
                InternalTypeInfo.ofFields(
                    DataTypes.INT().getLogicalType(),
                    DataTypes.VARCHAR(100).getLogicalType(),
                    DataTypes.TIMESTAMP().getLogicalType(),
                    DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE().getLogicalType()));

    Table input =
        getTableEnv().fromDataStream(source, $("id"), $("name"), $("op_time"), $("op_time_tz"));
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));
    sql(
        "CREATE TABLE mixed_catalog."
            + db
            + "."
            + TABLE
            + " ("
            + " id INT,"
            + " name STRING,"
            + " op_time_tz TIMESTAMP WITH LOCAL TIME ZONE,"
            + " op_time TIMESTAMP,"
            + " PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(op_time) "
            + " WITH ("
            + " 'connector' = 'arctic'"
            + ")");

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + "/*+ OPTIONS("
            + "'mixed-format.emit.mode'='file'"
            + ")*/ select id, name, op_time_tz, op_time from input");

    List<Row> actual =
        sql(
            "select id, op_time, op_time_tz from mixed_catalog."
                + db
                + "."
                + TABLE
                + "/*+ OPTIONS("
                + "'mixed-format.read.mode'='file'"
                + ", 'streaming'='false'"
                + ", 'source.parallelism'='2'"
                + ")*/");

    List<Object[]> expected = new LinkedList<>();
    expected.add(
        new Object[] {
          RowKind.INSERT,
          1000004,
          LocalDateTime.parse("2022-06-17T10:10:11.0"),
          LocalDateTime.parse("2022-06-17T10:10:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });
    expected.add(
        new Object[] {
          RowKind.INSERT,
          1000021,
          LocalDateTime.parse("2022-06-17T10:11:11.0"),
          LocalDateTime.parse("2022-06-17T10:11:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });
    expected.add(
        new Object[] {
          RowKind.INSERT,
          1000015,
          LocalDateTime.parse("2022-06-17T10:10:11.0"),
          LocalDateTime.parse("2022-06-17T10:10:11.0").atZone(ZoneId.systemDefault()).toInstant()
        });

    Assert.assertTrue(CollectionUtils.isEqualCollection(DataUtil.toRowList(expected), actual));
  }

  @Test
  public void testUnpartitionLogSinkSource() throws Exception {
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

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, PRIMARY KEY (id) NOT ENFORCED) WITH %s",
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
                + ", 'source.parallelism'='2'"
                + ")*/");

    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      for (Object[] datum : data) {
        Row row = iterator.next();
        actual.add(row);
      }
    }
    Assert.assertEquals(DataUtil.toRowSet(data), actual);
    result.getJobClient().ifPresent(TestUtil::cancelJob);
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

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED) WITH %s",
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
        Row row = iterator.next();
        actual.add(row);
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

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, PRIMARY KEY (id) NOT ENFORCED) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + " /*+ OPTIONS("
            + "'mixed-format.emit.mode'='file, log'"
            + ") */"
            + "select id, name from input");

    Assert.assertEquals(
        DataUtil.toRowSet(data),
        new HashSet<>(
            sql(
                "select * from mixed_catalog."
                    + db
                    + "."
                    + TABLE
                    + " /*+ OPTIONS('streaming'='false') */")));

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
  }

  @Test
  public void testPartitionSinkFile() throws IOException {

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

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(op_time) WITH ('connector' = 'arctic')");

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + "/*+ OPTIONS("
            + "'mixed-format.emit.mode'='file'"
            + ")*/"
            + " select * from input");

    Assert.assertEquals(
        DataUtil.toRowSet(data),
        new HashSet<>(
            sql(
                "select * from mixed_catalog."
                    + db
                    + "."
                    + TABLE
                    + " /*+ OPTIONS("
                    + "'streaming'='false'"
                    + ") */")));
  }

  @Test
  public void testSinkSourceFileWithoutSelectPK() throws Exception {

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

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED "
            + ") WITH ('connector' = 'arctic')");

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + "/*+ OPTIONS("
            + "'mixed-format.emit.mode'='file'"
            + ")*/"
            + " select * from input");

    TableResult result =
        exec(
            "select name, op_time from mixed_catalog."
                + db
                + "."
                + TABLE
                + " /*+ OPTIONS("
                + "'streaming'='false'"
                + ") */");
    LinkedList<Row> actual = new LinkedList<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      while (iterator.hasNext()) {
        Row row = iterator.next();
        actual.add(row);
      }
    }

    List<Object[]> expected = new LinkedList<>();
    expected.add(new Object[] {"a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {"b", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {"c", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(new Object[] {"d", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    expected.add(new Object[] {"d", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    expected.add(new Object[] {"e", LocalDateTime.parse("2022-06-18T10:10:11.0")});

    Assert.assertEquals(DataUtil.toRowSet(expected), new HashSet<>(actual));
  }

  @Test
  public void testFileUpsert() {

    List<Object[]> data = new LinkedList<>();
    data.add(
        new Object[] {RowKind.INSERT, 1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {RowKind.DELETE, 1000015, "b", LocalDateTime.parse("2022-06-17T10:08:11.0")});
    data.add(
        new Object[] {RowKind.DELETE, 1000011, "c", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE, 1000021, "d", LocalDateTime.parse("2022-06-17T10:11:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000021, "e", LocalDateTime.parse("2022-06-17T10:11:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000021, "e", LocalDateTime.parse("2022-06-17T10:11:11.0")
        });
    data.add(
        new Object[] {RowKind.INSERT, 1000015, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {RowKind.INSERT, 1000021, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE, 1000021, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000021, "d", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE, 1000015, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000021, "f", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    DataStream<RowData> source =
        getEnv()
            .fromCollection(
                DataUtil.toRowData(data),
                InternalTypeInfo.ofFields(
                    DataTypes.INT().getLogicalType(),
                    DataTypes.VARCHAR(100).getLogicalType(),
                    DataTypes.TIMESTAMP().getLogicalType()));

    Table input = getTableEnv().fromDataStream(source, $("id"), $("name"), $("op_time"));
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(TableProperties.UPSERT_ENABLED, "true");
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(op_time) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + "/*+ OPTIONS("
            + "'mixed-format.emit.mode'='file'"
            + ")*/"
            + " select * from input");

    List<Object[]> expected = new LinkedList<>();
    expected.add(
        new Object[] {RowKind.INSERT, 1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    // key = 1000021 locate in two partitions.
    expected.add(
        new Object[] {RowKind.INSERT, 1000021, "e", LocalDateTime.parse("2022-06-17T10:11:11.0")});
    expected.add(
        new Object[] {RowKind.INSERT, 1000021, "f", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    Assert.assertEquals(
        DataUtil.toRowSet(expected),
        new HashSet<>(
            sql(
                "select * from mixed_catalog."
                    + db
                    + "."
                    + TABLE
                    + " /*+ OPTIONS("
                    + "'streaming'='false'"
                    + ") */")));
  }

  @Test
  public void testFileCDC() {

    List<Object[]> data = new LinkedList<>();
    data.add(
        new Object[] {RowKind.INSERT, 1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {RowKind.DELETE, 1000015, "b", LocalDateTime.parse("2022-06-17T10:08:11.0")});
    data.add(
        new Object[] {RowKind.DELETE, 1000011, "c", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE, 1000021, "d", LocalDateTime.parse("2022-06-17T10:11:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000021, "e", LocalDateTime.parse("2022-06-17T10:11:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000021, "e", LocalDateTime.parse("2022-06-17T10:11:11.0")
        });
    data.add(
        new Object[] {RowKind.INSERT, 1000015, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {RowKind.INSERT, 1000021, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE, 1000021, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000021, "d", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE, 1000015, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000021, "f", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {RowKind.INSERT, 1000031, "g", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {RowKind.INSERT, 1000032, "h", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE, 1000031, "g", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_BEFORE, 1000032, "h", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000031, "f", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    data.add(
        new Object[] {
          RowKind.UPDATE_AFTER, 1000032, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")
        });
    DataStream<RowData> source =
        getEnv()
            .fromCollection(
                DataUtil.toRowData(data),
                InternalTypeInfo.ofFields(
                    DataTypes.INT().getLogicalType(),
                    DataTypes.VARCHAR(100).getLogicalType(),
                    DataTypes.TIMESTAMP().getLogicalType()));

    Table input = getTableEnv().fromDataStream(source, $("id"), $("name"), $("op_time"));
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(op_time) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + "/*+ OPTIONS("
            + "'mixed-format.emit.mode'='file'"
            + ")*/"
            + " select * from input");

    List<Object[]> expected = new LinkedList<>();
    // upsert is disEnabled, key=1000021 locate in two diff partitions.
    expected.add(
        new Object[] {RowKind.INSERT, 1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(
        new Object[] {RowKind.INSERT, 1000021, "e", LocalDateTime.parse("2022-06-17T10:11:11.0")});
    expected.add(
        new Object[] {RowKind.INSERT, 1000021, "d", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(
        new Object[] {RowKind.INSERT, 1000021, "f", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(
        new Object[] {RowKind.INSERT, 1000031, "f", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(
        new Object[] {RowKind.INSERT, 1000032, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    Assert.assertEquals(
        DataUtil.toRowSet(expected),
        new HashSet<>(
            sql(
                "select * from mixed_catalog."
                    + db
                    + "."
                    + TABLE
                    + " /*+ OPTIONS("
                    + "'streaming'='false'"
                    + ") */")));
  }

  @Test
  public void testFileUpsertWithSamePrimaryKey() throws Exception {

    List<Object[]> data = new LinkedList<>();
    data.add(
        new Object[] {RowKind.INSERT, 1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {RowKind.INSERT, 1000004, "b", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {RowKind.INSERT, 1000011, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(
        new Object[] {RowKind.INSERT, 1000011, "f", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    DataStream<RowData> source =
        getEnv()
            .fromCollection(
                DataUtil.toRowData(data),
                InternalTypeInfo.ofFields(
                    DataTypes.INT().getLogicalType(),
                    DataTypes.VARCHAR(100).getLogicalType(),
                    DataTypes.TIMESTAMP().getLogicalType()));

    getEnv().setParallelism(4);
    Table input = getTableEnv().fromDataStream(source, $("id"), $("name"), $("op_time"));
    getTableEnv().createTemporaryView("input", input);

    sql("CREATE CATALOG mixed_catalog WITH %s", toWithClause(props));

    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(TableProperties.UPSERT_ENABLED, "true");
    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(op_time) WITH %s",
        toWithClause(tableProperties));

    sql(
        "insert into mixed_catalog."
            + db
            + "."
            + TABLE
            + "/*+ OPTIONS("
            + "'mixed-format.emit.mode'='file'"
            + ")*/"
            + " select * from input");

    TableResult result =
        exec(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + " /*+ OPTIONS("
                + "'streaming'='false'"
                + ") */");
    LinkedList<Row> actual = new LinkedList<>();
    try (CloseableIterator<Row> iterator = result.collect()) {
      while (iterator.hasNext()) {
        Row row = iterator.next();
        actual.add(row);
      }
    }

    LinkedList<Object[]> expected = new LinkedList<>();

    expected.add(
        new Object[] {RowKind.INSERT, 1000004, "b", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    expected.add(
        new Object[] {RowKind.INSERT, 1000011, "f", LocalDateTime.parse("2022-06-17T10:10:11.0")});

    Map<Object, List<Row>> actualMap = DataUtil.groupByPrimaryKey(actual, 0);
    Map<Object, List<Row>> expectedMap =
        DataUtil.groupByPrimaryKey(DataUtil.toRowList(expected), 0);

    for (Object key : actualMap.keySet()) {
      Assert.assertTrue(
          CollectionUtils.isEqualCollection(actualMap.get(key), expectedMap.get(key)));
    }
  }

  @Test
  public void testPartitionLogSinkSource() throws Exception {
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

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(op_time) WITH %s",
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
        Row row = iterator.next();
        actual.add(row);
      }
    }
    Assert.assertEquals(DataUtil.toRowSet(data), actual);
    result.getJobClient().ifPresent(TestUtil::cancelJob);
  }

  @Test
  public void testPartitionLogSinkSourceWithSelectedFields() throws Exception {
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

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(op_time) WITH %s",
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
        Row row = iterator.next();
        actual.add(row);
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
  public void testPartitionDoubleSink() throws Exception {
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

    sql(
        "CREATE TABLE IF NOT EXISTS mixed_catalog."
            + db
            + "."
            + TABLE
            + "("
            + " id INT, name STRING, op_time TIMESTAMP, PRIMARY KEY (id) NOT ENFORCED "
            + ") PARTITIONED BY(op_time) WITH %s",
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
        new HashSet<>(
            sql(
                "select * from mixed_catalog."
                    + db
                    + "."
                    + TABLE
                    + " /*+ OPTIONS("
                    + "'streaming'='false'"
                    + ") */")));
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
        Row row = iterator.next();
        actual.add(row);
      }
    }
    Assert.assertEquals(DataUtil.toRowSet(data), actual);

    result.getJobClient().ifPresent(TestUtil::cancelJob);
  }
}

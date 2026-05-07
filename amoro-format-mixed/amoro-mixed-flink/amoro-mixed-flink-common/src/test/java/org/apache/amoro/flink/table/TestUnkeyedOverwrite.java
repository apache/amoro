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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.flink.FlinkTestBase;
import org.apache.amoro.flink.util.DataUtil;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.flink.table.api.ApiExpression;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class TestUnkeyedOverwrite extends FlinkTestBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestUnkeyedOverwrite.class);

  private static final String TABLE = "test_unkeyed";
  private static final String DB = TableTestHelper.TEST_TABLE_ID.getDatabase();

  private String db;
  public boolean isHive;
  static final TestHMS TEST_HMS = new TestHMS();

  static java.util.stream.Stream<Arguments> parameters() {
    return java.util.stream.Stream.of(
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, true),
            true),
        Arguments.of(
            new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true),
            false));
  }

  @BeforeAll
  public static void startTestHms() throws Exception {
    TEST_HMS.before();
  }

  @AfterAll
  public static void stopTestHms() {
    TEST_HMS.after();
  }

  @AfterEach
  public void dropTestTable() {
    if (db != null) {
      sql("DROP TABLE IF EXISTS mixed_catalog." + db + "." + TABLE);
    }
  }

  private void setUpForParam(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper, boolean isHive)
      throws Exception {
    this.isHive = isHive;
    if (isHive) {
      db = HiveTableTestHelper.TEST_DB_NAME;
    } else {
      db = DB;
    }
    initFlinkTestBase(catalogTestHelper, tableTestHelper);
    config();
  }

  @ParameterizedTest(name = "{0}, {1}, {2}")
  @MethodSource("parameters")
  public void testInsertOverwrite(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper, boolean isHive)
      throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper, isHive);

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
            + " id INT, name STRING)");

    sql("insert overwrite mixed_catalog." + db + "." + TABLE + " select * from input");

    Assertions.assertEquals(
        DataUtil.toRowSet(data),
        sqlSet(
            "select * from mixed_catalog."
                + db
                + "."
                + TABLE
                + " /*+ OPTIONS("
                + "'streaming'='false'"
                + ") */"));
  }

  @ParameterizedTest(name = "{0}, {1}, {2}")
  @MethodSource("parameters")
  public void testPartitionInsertOverwrite(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper, boolean isHive)
      throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper, isHive);

    List<Object[]> data = new LinkedList<>();
    data.add(new Object[] {1000004, "a", "2022-05-17"});
    data.add(new Object[] {1000015, "b", "2022-05-17"});
    data.add(new Object[] {1000011, "c", "2022-05-17"});
    data.add(new Object[] {1000014, "d", "2022-05-18"});
    data.add(new Object[] {1000021, "d", "2022-05-18"});
    data.add(new Object[] {1000007, "e", "2022-05-18"});

    List<Object[]> expected = new LinkedList<>();
    expected.add(new Object[] {11, "d", "2022-05-19"});
    expected.add(new Object[] {21, "d", "2022-05-19"});
    expected.add(new Object[] {35, "e", "2022-05-19"});

    data.addAll(expected);
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
            + " id INT, name STRING, dt STRING) PARTITIONED BY (dt)");

    sql("insert into mixed_catalog." + db + "." + TABLE + " select * from input");
    sql(
        "insert overwrite mixed_catalog."
            + db
            + "."
            + TABLE
            + " PARTITION (dt='2022-05-18') select id, name from input where dt = '2022-05-19'");

    Assertions.assertEquals(
        DataUtil.toRowSet(expected),
        sqlSet(
            "select id, name, '2022-05-19' from mixed_catalog."
                + db
                + "."
                + TABLE
                + " /*+ OPTIONS("
                + "'streaming'='false'"
                + ") */"
                + " where dt='2022-05-18'"));
  }
}

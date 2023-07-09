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

package com.netease.arctic.flink.read.hybrid.reader;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.flink.FlinkTestBase;
import com.netease.arctic.table.TableIdentifier;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FlinkSqlMORTest extends FlinkTestBase {
  public static final Logger LOG = LoggerFactory.getLogger(FlinkSqlMORTest.class);
  private StreamTableEnvironment tableEnv;
  private static final String CATALOG = TableTestHelper.TEST_CATALOG_NAME;
  private static final String DB = TableTestHelper.TEST_DB_NAME;
  private static final String TABLE = "test_keyed";
  private static final String TABLE_FULL_NAME = String.format("%s.%s.%s", CATALOG, DB, TABLE);
  private static final TableIdentifier TABLE_ID = TableIdentifier.of(CATALOG, DB, TABLE);

  public FlinkSqlMORTest() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, false));
  }

  @Before
  public void before() throws Exception {
    super.before();
    super.config();
    tableEnv = getTableEnv(EnvironmentSettings.newInstance().inBatchMode().build());

    sql(tableEnv, String.format("CREATE catalog %s WITH %s", CATALOG, toWithClause(props)));
    sql(tableEnv, String.format("create database if not exists %s.%s;", CATALOG, DB));

    sql(
        tableEnv,
        String.format("create table if not exists %s (" +
            "    id bigint," +
            "    name string," +
            "    primary key(id) not ENFORCED" +
            ") with (" +
            "    'write.upsert.enabled' = 'true'" +
            ");", TABLE_FULL_NAME));

    sql(
        tableEnv,
        String.format("insert into %s values (1, 'tom'), (2, 'joey'), (1, 'tom123');", TABLE_FULL_NAME)
    );
  }

  @After
  public void after() {
    getCatalog().dropTable(TABLE_ID, true);
  }

  @Test
  public void testSqlMOR() throws Exception {
    List<Row> rows = sql(tableEnv, String.format("select id, name from %s;", TABLE_FULL_NAME));
    rows.forEach(row -> LOG.info(row.toString()));
    Assert.assertEquals(2, rows.size());
  }
}
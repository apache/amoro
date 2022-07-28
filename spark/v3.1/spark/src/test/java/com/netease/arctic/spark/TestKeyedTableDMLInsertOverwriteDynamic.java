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

package com.netease.arctic.spark;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class TestKeyedTableDMLInsertOverwriteDynamic {

  private final String database = "db";
  private final String table = "testA";
  private KeyedTable keyedTable;
  private final TableIdentifier identifier = TableIdentifier.of(SparkTestContext.catalogName, database, table);

  private String contextOverwriteMode;

  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  @Before
  public void before() {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create database if not exists {0}", database);
    sparkTestContext.sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " data string , \n " +
        " ts timestamp , \n" +
        " primary key (id) \n" +
        ") using arctic \n" +
        " partitioned by ( days(ts) ) \n", database, table);

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "(1, ''aaa'',  timestamp('' 2022-1-1 09:00:00 '')), \n " +
        "(2, ''bbb'',  timestamp('' 2022-1-2 09:00:00 '')), \n " +
        "(3, ''ccc'',  timestamp('' 2022-1-3 09:00:00 '')) \n ", database, table);
    keyedTable = SparkTestContext.loadTable(identifier).asKeyedTable();

    SparkTestContext.writeChange(identifier, ChangeAction.INSERT, Lists.newArrayList(
        SparkTestContext.newRecord(keyedTable, 4, "ddd", SparkTestContext.quickDateWithZone(1)),
        SparkTestContext.newRecord(keyedTable, 5, "eee", SparkTestContext.quickDateWithZone(2)),
        SparkTestContext.newRecord(keyedTable, 6, "666", SparkTestContext.quickDateWithZone(3)),
        SparkTestContext.newRecord(keyedTable, 1024, "1024", SparkTestContext.quickDateWithZone(4))
    ));

    sparkTestContext.sql("select * from {0}.{1} order by id", database, table);

    contextOverwriteMode = SparkTestContext.spark.conf().get("spark.sql.sources.partitionOverwriteMode");
    System.out.println("spark.sql.sources.partitionOverwriteMode = " + contextOverwriteMode);
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", "DYNAMIC");
  }

  @After
  public void after() {
    sparkTestContext.sql("drop table {0}.{1}", database, table);
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", contextOverwriteMode);
    sparkTestContext.sql("DROP DATABASE IF EXISTS {0}", database);
  }

  @Test
  public void testInsertOverwrite() {
    // insert overwrite by values
    // before 4 partition, [1-3] partition has base && change file, [4] partition has change file
    // expect [1-2] partition replaced, [3-4] partition keep:
    // P[1]=> [7  ]
    // P[2]=> [8,9]
    // P[3]=> [3,6]
    // P[4]=> [1024]
    //
    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "(7, ''aaa'',  timestamp('' 2022-1-1 09:00:00 '')), \n " +
        "(8, ''bbb'',  timestamp('' 2022-1-2 09:00:00 '')), \n " +
        "(9, ''ccc'',  timestamp('' 2022-1-2 09:00:00 '')) \n ", database, table);

    sparkTestContext.rows = sparkTestContext.sql("select id, data, ts from {0}.{1} order by id", database, table);
    Assert.assertEquals(6, sparkTestContext.rows.size());

    SparkTestContext.assertContainIdSet(sparkTestContext.rows, 0, 7, 8, 9, 3, 6, 1024);
  }

  @Test
  public void testInsertOverwriteNoBasePartition() {
    // insert overwrite by values
    // before 4 partition, [1-3] partition has base && change file, [4] partition has change file
    // expect
    // P[1]=> [1,4]
    // P[2]=> [2,5]
    // P[3]=> [3,6]
    // P[4]=> [7，8，9]
    //
    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "(7, ''aaa'',  timestamp('' 2022-1-4 09:00:00 '')), \n " +
        "(8, ''bbb'',  timestamp('' 2022-1-4 09:00:00 '')), \n " +
        "(9, ''ccc'',  timestamp('' 2022-1-4 09:00:00 '')) \n ", database, table);

    sparkTestContext.rows = sparkTestContext.sql("select id, data, ts from {0}.{1} order by id", database, table);
    Assert.assertEquals(9, sparkTestContext.rows.size());

    SparkTestContext.assertContainIdSet(sparkTestContext.rows, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
  }
}

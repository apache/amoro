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

public class TestKeyedTableDMLInsertOverwriteStatic {

  private final String database = "db";
  private final String table = "testA";
  private KeyedTable keyedTable;
  private final TableIdentifier identifier = TableIdentifier.of(SparkTestContext.catalogName, database, table);

  private String contextOverwriteMode;

  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  @Before
  public void before() {
    contextOverwriteMode = SparkTestContext.spark.conf().get("spark.sql.sources.partitionOverwriteMode");
    System.out.println("spark.sql.sources.partitionOverwriteMode = " + contextOverwriteMode);
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", "STATIC");

    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create database if not exists {0}", database);
    sparkTestContext.sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " data string , \n " +
        " dt string , \n" +
        " primary key (id) \n" +
        ") using arctic \n" +
        " partitioned by ( dt ) \n", database, table);

    keyedTable = SparkTestContext.loadTable(identifier).asKeyedTable();

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "(1, ''aaa'',  ''2021-1-1''), \n " +
        "(2, ''bbb'',  ''2021-1-2''), \n " +
        "(3, ''ccc'',  ''2021-1-3'') \n ", database, table);

    SparkTestContext.writeChange(identifier, ChangeAction.INSERT, Lists.newArrayList(
        SparkTestContext.newRecord(keyedTable, 4, "ddd", "2021-1-1"),
        SparkTestContext.newRecord(keyedTable, 5, "eee", "2021-1-2"),
        SparkTestContext.newRecord(keyedTable, 6, "666", "2021-1-3"),
        SparkTestContext.newRecord(keyedTable, 1024, "1024", "2021-1-4")
    ));
  }

  @After
  public void after() {
    sparkTestContext.sql("drop table {0}.{1}", database, table);
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", contextOverwriteMode);
    sparkTestContext.sql("DROP DATABASE IF EXISTS {0}", database);
  }

  @Test
  public void testInsertOverwriteAllPartitionByValue() {
    // insert overwrite by values, no partition expr
    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "(7, ''aaa'',  ''2021-1-1''), \n " +
        "(8, ''bbb'',  ''2021-1-2''), \n " +
        "(9, ''ccc'',  ''2021-1-2'') \n ", database, table);

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}", database, table);
    Assert.assertEquals(3, sparkTestContext.rows.size());

    SparkTestContext.assertContainIdSet(sparkTestContext.rows, 0, 7, 8, 9);
  }

  @Test
  public void testInsertOverwriteSomePartitionByValue() {
    // insert overwrite by values, with partition expr
    // expect 1 partition replaced, 2 partition keep
    // expect
    // P[1]=> [7,8,9]
    // P[2]=> [2,5]
    // P[3]=> [3,6]
    // P[4]=> [1024]
    sparkTestContext.sql("insert overwrite {0}.{1} \n" +
        "partition( dt = ''2021-1-1'')  values \n" +
        "(7, ''aaa''), \n " +
        "(8, ''bbb''), \n " +
        "(9, ''ccc'') \n ", database, table);

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1} order by id", database, table);
    Assert.assertEquals(8, sparkTestContext.rows.size());
    SparkTestContext.assertContainIdSet(sparkTestContext.rows, 0, 7, 8, 9, 2, 5, 3, 6, 1024);
  }
}

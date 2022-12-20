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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDropPartitions extends SparkTestBase {
  private final String database = "db_arctic";

  @Before
  public void prepare() {
    sql("use " + catalogNameArctic);
    sql("create database if not exists " + database);
  }

  @After
  public void cleanUpTable() {
    sql("drop database " + database);
  }

  @Test
  public void testAlterKeyedTableDropPartitions() {
    String dropPartitionTable = "drop_partition_table";
    sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string , \n " +
        " ts int, primary key(id) " +
        ") using arctic \n" +
        " partitioned by (ts, name) \n" +
        " tblproperties ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, dropPartitionTable);
    sql("insert overwrite {0}.{1} "+
        " values (1, ''aaa'', 1 ) , " +
        "(4, ''bbb'', 2), " +
        "(5, ''ccc'', 3) ", database, dropPartitionTable);

    sql("insert into {0}.{1} "+
        " values (2, ''aaa'', 1 ) , " +
        "(3, ''bbb'', 2), " +
        "(6, ''ccc'', 3) ", database, dropPartitionTable);
    rows = sql("select * from {0}.{1}", database,dropPartitionTable);
    Assert.assertEquals(6, rows.size());

    sql("alter table {0}.{1} drop if exists partition (ts=1, name=''aaa'')", database, dropPartitionTable);
    sql("select * from {0}.{1}", database,dropPartitionTable);
    Assert.assertEquals(4, rows.size());
    sql("drop table {0}.{1}", database, dropPartitionTable);
  }

  @Test
  public void testAlterUnKeyedTableDropPartitions() {
    String dropPartitionTable = "drop_partition_table";
    sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string , \n " +
        " ts int" +
        ") using arctic \n" +
        " partitioned by (ts, name) \n" +
        " tblproperties ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, dropPartitionTable);
    sql("insert overwrite {0}.{1} "+
        " values (1, ''aaa'', 1 ) , " +
        "(4, ''bbb'', 2), " +
        "(5, ''ccc'', 3) ", database, dropPartitionTable);

    sql("insert into {0}.{1} "+
        " values (2, ''aaa'', 1 ) , " +
        "(3, ''bbb'', 2), " +
        "(6, ''ccc'', 3) ", database, dropPartitionTable);
    rows = sql("select * from {0}.{1}", database,dropPartitionTable);
    Assert.assertEquals(6, rows.size());

    sql("alter table {0}.{1} drop if exists partition (ts=1, name=''aaa'')", database, dropPartitionTable);
    sql("select * from {0}.{1}", database,dropPartitionTable);
    Assert.assertEquals(4, rows.size());
    sql("drop table {0}.{1}", database, dropPartitionTable);
  }
}

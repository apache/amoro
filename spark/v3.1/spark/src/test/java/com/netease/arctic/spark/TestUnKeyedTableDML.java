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
import org.junit.ClassRule;
import org.junit.Test;

public class TestUnKeyedTableDML {

  protected String database = "arc_def";
  protected String table = "testA";

  protected String createTableTemplate = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string, \n" +
      " ts timestamp ) \n" +
      " using arctic " +
      " partitioned by ( days(ts)  ) ";

  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  @Before
  public void prepareTable() {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create database " + database);

    sparkTestContext.sql(createTableTemplate, database, table);
  }

  @After
  public void cleanUpTable() {
    sparkTestContext.sql("drop table " + database + "." + table);
    sparkTestContext.sql("drop database " + database);
  }

  @Test
  public void testUnKeyedTableDML() {
    sparkTestContext.sql("insert into " + database + "." + table +
        " values (1, 'aaa', 'abcd', timestamp('2021-1-1 01:00:00') ) , " +
        "(2, 'bbb', 'bbcd', timestamp('2022-1-2 12:21:00') ), " +
        "(3, 'ccc', 'cbcd', timestamp('2022-1-2 21:00:00') ) ");

    sparkTestContext.rows = sparkTestContext.sql("select * from " + database + "." + table);
    Assert.assertEquals(3, sparkTestContext.rows.size());

    String table2 = "arc_test2";
    sparkTestContext.sql("create table {0}.{1} ( \n" +
        " id int ,\n" +
        " name string ,\n" +
        " dt date ) \n" +
        " using arctic \n" +
        " partitioned by ( years(dt) )", database, table2);
    sparkTestContext.sql(
        "insert into {0}.{1} select id, name, date(ts) as dt from {0}.{2}",
        database, table2, table);

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}", database, table2);
    Assert.assertEquals(3, sparkTestContext.rows.size());
    sparkTestContext.sql("drop table {0}.{1}", database, table2);
  }

  @Test
  public void testUpdate() {
    sparkTestContext.sql("insert into " + database + "." + table +
        " values (1, 'aaa', 'abcd', timestamp('2021-1-1 01:00:00') ) , " +
        "(2, 'bbb', 'bbcd', timestamp('2022-1-2 12:21:00') ), " +
        "(3, 'ccc', 'cbcd', timestamp('2022-1-2 21:00:00') ) ");

    sparkTestContext.sql("update {0}.{1} set name = \"ddd\" where id = 3", database, table);
    sparkTestContext.rows = sparkTestContext.sql("select id, name from {0}.{1} where id = 3", database, table);

    Assert.assertEquals(1, sparkTestContext.rows.size());
    Assert.assertEquals("ddd", sparkTestContext.rows.get(0)[1]);
  }

  @Test
  public void testDelete() {
    sparkTestContext.sql("insert into " + database + "." + table +
        " values (1, 'aaa', 'abcd', timestamp('2021-1-1 01:00:00') ) , " +
        "(2, 'bbb', 'bbcd', timestamp('2022-1-2 12:21:00') ), " +
        "(3, 'ccc', 'cbcd', timestamp('2022-1-2 21:00:00') ) ");

    sparkTestContext.sql("delete from {0}.{1} where id = 3", database, table);
    sparkTestContext.rows = sparkTestContext.sql("select id, name from {0}.{1} order by id", database, table);

    Assert.assertEquals(2, sparkTestContext.rows.size());
    Assert.assertEquals(1, sparkTestContext.rows.get(0)[0]);
    Assert.assertEquals(2, sparkTestContext.rows.get(1)[0]);
  }

  @Test
  public void testMergeInto() {
    sparkTestContext.sql("insert into " + database + "." + table +
        " values (1, 'aaa', 'abcd', timestamp('2021-1-1 01:00:00') ) , " +
        "(2, 'bbb', 'bbcd', timestamp('2022-1-2 12:21:00') ), " +
        "(3, 'ccc', 'cbcd', timestamp('2022-1-2 21:00:00') ) ");

    String mergeIntoTable = "testB";
    sparkTestContext.sql(createTableTemplate, database, mergeIntoTable);

    sparkTestContext.sql("insert into " + database + "." + mergeIntoTable +
        " values (4, 'eee', 'eee', timestamp('2021-5-1 01:00:00') ) , " +
        "(2, 'bbb', 'b', timestamp('2022-1-2 12:21:00') ), " +
        "(3, 'ccc', 'c', timestamp('2022-1-2 21:00:00') ) ");

    sparkTestContext.sql("merge into {0}.{1} t using {0}.{2} s on t.id = s.id \n" +
        "when matched and s.name = \"ccc\" then delete \n" +
        "when matched then update set t.data = s.data \n" +
        "when not matched then insert *", database, table, mergeIntoTable);

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1} order by id", database, table);
    Assert.assertEquals(3, sparkTestContext.rows.size());
    Assert.assertEquals(1, sparkTestContext.rows.get(0)[0]);
    Assert.assertEquals(2, sparkTestContext.rows.get(1)[0]);
    Assert.assertEquals("b", sparkTestContext.rows.get(1)[2]);
    Assert.assertEquals(4, sparkTestContext.rows.get(2)[0]);

    sparkTestContext.sql("drop table {0}.{1}", database, mergeIntoTable);
  }

  @Test
  public void testDynamicInsertOverwrite() {
    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "(1, ''aaa'', ''a'', timestamp('' 2021-1-1 01:00:00 '')), \n " +
        "(2, ''bbb'', ''b'', timestamp('' 2021-1-2 01:00:00 '')), \n " +
        "(3, ''ccc'', ''c'', timestamp('' 2021-1-2 01:00:00 '')) \n ", database, table);
    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1} order by id", database, table);
    Assert.assertEquals(3, sparkTestContext.rows.size());

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "(4, ''aaa'', ''a'', timestamp('' 2021-1-3 01:00:00 '')), \n " +
        "(5, ''bbb'', ''b'', timestamp('' 2021-1-4 01:00:00 '')), \n " +
        "(6, ''ccc'', ''c'', timestamp('' 2021-1-4 01:00:00 '')) \n ", database, table);
    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}  order by id", database, table);
    Assert.assertEquals(6, sparkTestContext.rows.size());

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "(7, ''aaa'', ''a'', timestamp('' 2021-1-2 01:00:00 '')), \n " +
        "(8, ''bbb'', ''b'', timestamp('' 2021-1-4 01:00:00 '')) \n ", database, table);
    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}  order by id", database, table);
    Assert.assertEquals(4, sparkTestContext.rows.size());
  }

  @Test
  public void testStaticInsertOverwrite() {
    String mode = SparkTestContext.spark.conf().get("spark.sql.sources.partitionOverwriteMode");
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", "STATIC");
    String table = "test_overwrite";
    sparkTestContext.sql("create table {0}.{1} ( id int , data string, pt string) \n" +
        "using arctic partitioned by (pt) ", database, table);

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "( 1, ''aaa'', ''INFO'' ), \n " +
        "( 2, ''bbb'', ''DEBUG'')", database, table);

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1} order by id", database, table);
    Assert.assertEquals(2, sparkTestContext.rows.size());

    sparkTestContext.sql("insert overwrite {0}.{1} \n " +
        "partition ( pt = ''INFO'' ) values \n" +
        "( 3, ''ccc''), \n " +
        "( 4, ''ddd'')", database, table);
    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}  order by id", database, table);
    Assert.assertEquals(3, sparkTestContext.rows.size());

    sparkTestContext.sql("drop table {0}.{1}", database, table);
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", mode);
  }

  @Test
  public void testStaticInsertOverwriteUseSelect() {
    String mode = SparkTestContext.spark.conf().get("spark.sql.sources.partitionOverwriteMode");
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", "STATIC");
    String table = "test_overwrite_use_select";
    String source = "test_source";
    sparkTestContext.sql("create table {0}.{1} ( id int , data string, pt string) \n" +
        "using arctic partitioned by (pt) ", database, table);

    sparkTestContext.sql("create table {0}.{1} ( id int , data string, pt string) \n" +
        "using arctic partitioned by (pt) ", database, source);

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "( 1, ''aaa'', ''INFO'' ), \n " +
        "( 2, ''bbb'', ''DEBUG'')", database, table);

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "( 4, ''aaa'', ''INFO'' ), \n " +
        "( 5, ''bbb'', ''DEBUG''), \n " +
        "( 6, ''ccc'', ''DEBUG'')", database, source);

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1} order by id", database, table);
    Assert.assertEquals(2, sparkTestContext.rows.size());

    sparkTestContext.sql("insert overwrite {0}.{1} \n " +
        "partition ( pt ) \n" +
        "select id, data, pt from {0}.{2} \n" +
        "where pt = ''DEBUG'' ", database, table, source);
    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}  order by id", database, table);
    Assert.assertEquals(2, sparkTestContext.rows.size());

    sparkTestContext.sql("drop table {0}.{1}", database, table);
    sparkTestContext.sql("drop table {0}.{1}", database, source);
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", mode);
  }

  @Test
  public void testDynamicInsertOverwriteUseSelect() {
    String table = "test_dynamic_overwrite_use_select";
    String source = "test_source";

    String mode = SparkTestContext.spark.conf().get("spark.sql.sources.partitionOverwriteMode");
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", "DYNAMIC");
    sparkTestContext.sql("create table {0}.{1} ( id int , data string, pt string) \n" +
        "using arctic partitioned by (pt) ", database, table);

    sparkTestContext.sql("create table {0}.{1} ( id int , data string, pt string) \n" +
        "using arctic partitioned by (pt) ", database, source);

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "( 1, ''aaa'', ''INFO'' ), \n " +
        "( 2, ''bbb'', ''DEBUG'')", database, table);

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
        "( 4, ''aaa'', ''INFO'' ), \n " +
        "( 5, ''bbb'', ''DEBUG''), \n " +
        "( 6, ''ccc'', ''DEBUG'')", database, source);

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1} order by id", database, table);
    Assert.assertEquals(2, sparkTestContext.rows.size());

    sparkTestContext.sql("insert overwrite {0}.{1} \n " +
        "select id, data, pt from {0}.{2} \n" +
        "where pt = ''DEBUG'' ", database, table, source);
    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}  order by id", database, table);
    Assert.assertEquals(3, sparkTestContext.rows.size());

    sparkTestContext.sql("drop table {0}.{1}", database, table);
    sparkTestContext.sql("drop table {0}.{1}", database, source);
    sparkTestContext.sql("set spark.sql.sources.partitionOverwriteMode = {0}", mode);
  }
}

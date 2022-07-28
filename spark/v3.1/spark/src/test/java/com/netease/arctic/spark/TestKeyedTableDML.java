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

import com.google.common.collect.Lists;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class TestKeyedTableDML {
  private final String database = "db_test";
  private final String table = "testA";
  private KeyedTable keyedTable;

  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  @Before
  public void before() {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create database if not exists {0}", database);
    sparkTestContext.sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string , \n " +
        " ts timestamp , \n" +
        " primary key (id) \n" +
        ") using arctic \n" +
        " partitioned by ( days(ts) ) \n" +
        " options ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, table);
    keyedTable = SparkTestContext.loadTable(TableIdentifier.of(SparkTestContext.catalogName, database, table)).asKeyedTable();
  }

  @After
  public void cleanUp() {
    sparkTestContext.sql("drop table {0}.{1}", database, table);
    sparkTestContext.sql("DROP DATABASE IF EXISTS {0}", database);
  }

  @Test
  public void testMergeOnRead() {
    TableIdentifier identifier = TableIdentifier.of(SparkTestContext.catalogName, database, table);
    SparkTestContext.writeBase(identifier,  Lists.newArrayList(
        new Object[]{1, "aaa", SparkTestContext.ofDateWithZone(2022, 1, 1,0)},
        new Object[]{2, "bbb", SparkTestContext.ofDateWithZone(2022, 1, 2,0)},
        new Object[]{3, "ccc", SparkTestContext.ofDateWithZone(2022, 1, 2,0)}
    ));
    SparkTestContext.writeChange(identifier, ChangeAction.INSERT, Lists.newArrayList(
        SparkTestContext.newRecord(keyedTable, 4, "ddd", SparkTestContext.quickDateWithZone(4) ),
        SparkTestContext.newRecord(keyedTable, 5, "eee", SparkTestContext.quickDateWithZone(4) )
    ));
    SparkTestContext.writeChange(identifier, ChangeAction.DELETE, Lists.newArrayList(
        SparkTestContext.newRecord(keyedTable, 1, "aaa", SparkTestContext.quickDateWithZone(1))
    ));

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}", database, table);
    Assert.assertEquals(4, sparkTestContext.rows.size());
    Set<Object> idSet = sparkTestContext.rows.stream().map(r -> r[0]).collect(Collectors.toSet());

    Assert.assertEquals(4, idSet.size());
    Assert.assertTrue(idSet.contains(4));
    Assert.assertTrue(idSet.contains(5));
    Assert.assertFalse(idSet.contains(1));
  }


  @Test
  public void testSelectChangeFiles() {
    TableIdentifier identifier = TableIdentifier.of(SparkTestContext.catalogName, database, table);
    SparkTestContext.writeChange(identifier,  ChangeAction.INSERT, Lists.newArrayList(
        SparkTestContext.newRecord(keyedTable, 4, "ddd", SparkTestContext.quickDateWithZone(4)),
        SparkTestContext.newRecord(keyedTable, 5, "eee", SparkTestContext.quickDateWithZone(4))
    ));
    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}.change", database, table);
    Assert.assertEquals(2, sparkTestContext.rows.size());
  }

}

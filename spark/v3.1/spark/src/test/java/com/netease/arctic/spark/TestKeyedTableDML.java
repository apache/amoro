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
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class TestKeyedTableDML extends SparkTestBase {
  private final String database = "db_test";
  private final String table = "testA";
  private KeyedTable keyedTable;

  @Before
  public void before() {
    sql("use " + catalogName);
    sql("create database if not exists {0}", database);
    sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string , \n " +
        " ts timestamp , \n" +
        " primary key (id) \n" +
        ") using arctic \n" +
        " partitioned by ( days(ts) ) \n" +
        " options ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, table);
    keyedTable = (KeyedTable) loadTable(TableIdentifier.of(catalogName, database, table));
  }

  @After
  public void cleanUp() {
    sql("drop table {0}.{1}", database, table);
  }

  @Test
  public void testMergeOnRead() {
    TableIdentifier identifier = TableIdentifier.of(catalogName, database, table);
    writeBase(identifier,  Lists.newArrayList(
        new Object[]{1, "aaa", ofDateWithZone(2022, 1, 1,0)},
        new Object[]{2, "bbb", ofDateWithZone(2022, 1, 2,0)},
        new Object[]{3, "ccc", ofDateWithZone(2022, 1, 2,0)}
    ));
    writeChange(identifier, ChangeAction.INSERT, Lists.newArrayList(
        newRecord(keyedTable, 4, "ddd", quickDateWithZone(4) ),
        newRecord(keyedTable, 5, "eee", quickDateWithZone(4) )
    ));
    writeChange(identifier, ChangeAction.DELETE, Lists.newArrayList(
        newRecord(keyedTable, 1, "aaa", quickDateWithZone(1))
    ));

    rows = sql("select * from {0}.{1}", database, table);
    Assert.assertEquals(4, rows.size());
    Set<Object> idSet = rows.stream().map(r -> r[0]).collect(Collectors.toSet());

    Assert.assertEquals(4, idSet.size());
    Assert.assertTrue(idSet.contains(4));
    Assert.assertTrue(idSet.contains(5));
    Assert.assertFalse(idSet.contains(1));
  }


  @Test
  public void testSelectChangeFiles() {
    TableIdentifier identifier = TableIdentifier.of(catalogName, database, table);
    writeChange(identifier,  ChangeAction.INSERT, Lists.newArrayList(
        newRecord(keyedTable, 4, "ddd", quickDateWithZone(4)),
        newRecord(keyedTable, 5, "eee", quickDateWithZone(4))
    ));
    rows = sql("select * from {0}.{1}.change", database, table);
    Assert.assertEquals(2, rows.size());
  }

}

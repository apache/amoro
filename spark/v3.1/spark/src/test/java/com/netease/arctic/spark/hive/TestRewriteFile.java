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

package com.netease.arctic.spark.hive;

import com.google.common.collect.Lists;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.HivePartitionUtil;
import com.netease.arctic.spark.SparkTestBase;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.StructLike;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.netease.arctic.spark.SparkSQLProperties.CHECK_SOURCE_DUPLICATES_ENABLE;

import java.util.List;

public class TestRewriteFile extends SparkTestBase {

  private final String database = "db_hive";
  private final String keyedTable = "keyedTable";
  private final String keyedParTable = "keyedParTable";
  private final String unKeyedTable = "unKeyedTable";
  private final String unKeyedParTable = "unKeyedParTable";

  protected String createKeyedTableTemplate = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string, primary key(id)) \n" +
      " using arctic" +
      " tblproperties ( \n" +
      " ''write.upsert.enabled'' = ''true'' ";

  protected String createKeyedPartitionTableTemplate = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string, primary key(id)) \n" +
      " using arctic \n" +
      " PARTITIONED BY (data)" +
      " tblproperties ( \n" +
      " ''write.upsert.enabled'' = ''true'' ";

  protected String createUnKeyedTableTemplate = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string) \n" +
      " using arctic";

  protected String createUnKeyedPartitionTableTemplate = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string) \n" +
      " using arctic \n" +
      " PARTITIONED BY (data)" +
      " tblproperties ( \n" +
      " ''base.file-index.hash-bucket'' = ''1'' )";

  @Before
  public void prepare() {
    sql("use " + catalogNameHive);
    sql("create database if not exists " + database);
  }

  @Test
  public void testRewriteKeyedTable() {
    sql("set `{0}` = `false`", CHECK_SOURCE_DUPLICATES_ENABLE);
    sql("use " + catalogNameHive);
    sql(createKeyedTableTemplate, database, keyedTable);
    sql("insert overwrite " + database + "." + keyedTable +
        " values (1, 'aaa', 'aaaa' ) , " +
        "(4, 'bbb', 'bbcd'), " +
        "(5, 'ccc', 'cbcd') ");
    sql("insert into " + database + "." + keyedTable +
        " values (1, 'aaa', 'dddd' ) , " +
        "(2, 'bbb', 'bbbb'), " +
        "(3, 'ccc', 'cccc') ");

    rows = sql("select * from {0}.{1} ", database, keyedTable);
    Assert.assertEquals(5, rows.size());
    sql("CALL {0}.system.rewrite_data_files(''{1}.{2}'')", catalogNameHive, database, keyedTable);
    //check mix-iceberg table
    rows = sql("select * from {0}.{1} ", database, keyedTable);
    Assert.assertEquals(5, rows.size());
    //check hive table
    sql("use spark_catalog");
    rows = sql("select * from {0}.{1} ", database, keyedTable);
    Assert.assertEquals(5, rows.size());
  }

  @Test
  public void testRewriteUnKeyedTable() {
    sql("set `{0}` = `false`", CHECK_SOURCE_DUPLICATES_ENABLE);
    sql("use " + catalogNameHive);
    sql(createUnKeyedTableTemplate, database, unKeyedTable);
    sql("insert overwrite " + database + "." + unKeyedTable +
        " values (1, 'aaa', 'aaaa' ) , " +
        "(2, 'bbb', 'bbcd'), " +
        "(3, 'ccc', 'cbcd') ");
    sql("insert into " + database + "." + unKeyedTable +
        " values (4, 'aaa', 'dddd' ) , " +
        "(5, 'bbb', 'bbbb'), " +
        "(6, 'ccc', 'cccc') ");

    rows = sql("select * from {0}.{1} ", database, unKeyedTable);
    Assert.assertEquals(6, rows.size());
    sql("CALL {0}.system.rewrite_data_files(''{1}.{2}'')", catalogNameHive, database, unKeyedTable);
    //check mix-iceberg table
    rows = sql("select * from {0}.{1} ", database, unKeyedTable);
    Assert.assertEquals(6, rows.size());
    //check hive table
    sql("use spark_catalog");
    rows = sql("select * from {0}.{1} ", database, unKeyedTable);
    Assert.assertEquals(6, rows.size());
  }

  @Test
  public void testRewriteKeyedPartitionTable() {
    sql("set `{0}` = `false`", CHECK_SOURCE_DUPLICATES_ENABLE);
    sql("use " + catalogNameHive);
    sql(createKeyedPartitionTableTemplate, database, keyedParTable);
    sql("insert overwrite " + database + "." + keyedParTable +
        " values (1, 'aaa', 'aaaa' ) , " +
        "(4, 'bbb', 'bbcd'), " +
        "(5, 'ccc', 'cbcd') ");
    sql("insert into " + database + "." + keyedParTable +
        " values (1, 'aaaa', 'aaaa' ) , " +
        "(2, 'bbb', 'bbbb'), " +
        "(3, 'ccc', 'cccc') ");

    rows = sql("select * from {0}.{1} ", database, keyedParTable);
    Assert.assertEquals(5, rows.size());
    sql("CALL {0}.system.rewrite_data_files(''{1}.{2}'')", catalogNameHive, database, keyedParTable);
    //check mix-iceberg table
    rows = sql("select * from {0}.{1} ", database, keyedParTable);
    Assert.assertEquals(5, rows.size());

    Assert.assertEquals(5, rows.size());
    ArcticTable table = catalog(catalogNameHive).loadTable(TableIdentifier.of(catalogNameHive, database,
        keyedParTable));
    Assert.assertEquals(1, table.io().list(findHiveParLocation((SupportHive) table, keyedParTable, "aaaa")).size());
    sql("ALTER TABLE {0}.{1} SET TBLPROPERTIES (''write.upsert.enabled'' = ''false'')", database, keyedParTable);
    sql("insert into " + database + "." + keyedParTable +
        " values (9, 'aaaa', 'aaaa' )");
    sql("CALL {0}.system.rewrite_data_files(''{1}.{2}'')", catalogNameHive, database, keyedParTable);
    Assert.assertEquals(2, table.io().list(findHiveParLocation((SupportHive) table, keyedParTable, "aaaa")).size());

    //check hive table
    sql("use spark_catalog");
    rows = sql("select * from {0}.{1} ", database, keyedParTable);
    Assert.assertEquals(5, rows.size());
  }

  @Test
  public void testRewriteUnKeyedPartitionTable() {
    sql("set `{0}` = `false`", CHECK_SOURCE_DUPLICATES_ENABLE);
    sql("use " + catalogNameHive);
    sql(createUnKeyedPartitionTableTemplate, database, unKeyedParTable);
    sql("insert overwrite " + database + "." + unKeyedParTable +
        " values (1, 'aaa', 'aaaa' ) , " +
        "(2, 'bbb', 'bbcd'), " +
        "(3, 'ccc', 'cbcd') ");
    sql("insert into " + database + "." + unKeyedParTable +
        " values (5, 'aaa', 'aaaa' ) , " +
        "(6, 'bbb', 'bbbb'), " +
        "(7, 'ccc', 'cccc') ");

    sql("CALL {0}.system.rewrite_data_files(''{1}.{2}'')", catalogNameHive, database, unKeyedParTable);
    rows = sql("select * from {0}.{1} ", database, unKeyedParTable);
    Assert.assertEquals(6, rows.size());
    ArcticTable table = catalog(catalogNameHive).loadTable(TableIdentifier.of(catalogNameHive, database,
        unKeyedParTable));
    String parName = "aaaa";
    Assert.assertEquals(
        2,
        table.io().list(findHiveParLocation((SupportHive) table, unKeyedParTable, parName)).size());
    rows = sql("select * from {0}.{1} ", database, unKeyedParTable);
    Assert.assertEquals(6, rows.size());
    sql("insert into " + database + "." + unKeyedParTable +
        " values (9, 'aaa', 'aaaa' )");
    sql("CALL {0}.system.rewrite_data_files(''{1}.{2}'')", catalogNameHive, database, unKeyedParTable);
    rows = sql("select * from {0}.{1} ", database, unKeyedParTable);
    Assert.assertEquals(7, rows.size());
    Assert.assertEquals(
        3,
        table.io().list(findHiveParLocation((SupportHive) table, unKeyedParTable, parName)).size());
    sql("insert into " + database + "." + unKeyedParTable +
        " values (13, 'aaa', 'aaaa' )");
    sql("CALL {0}.system.rewrite_data_files(''{1}.{2}'')", catalogNameHive, database, unKeyedParTable);
    Assert.assertEquals(
        1,
        table.io().list(findHiveParLocation((SupportHive) table, unKeyedParTable, parName)).size());

    //check mix-iceberg table data
    rows = sql("select * from {0}.{1} ", database, unKeyedParTable);
    Assert.assertEquals(8, rows.size());
    //check hive table data
    sql("use spark_catalog");
    rows = sql("select * from {0}.{1} ", database, unKeyedParTable);
    Assert.assertEquals(8, rows.size());
  }

  private String findHiveParLocation(SupportHive table, String tableName, String parName) {
    List<String> partitionLocations = HivePartitionUtil.getHivePartitionLocations(
        table.getHMSClient(),
        TableIdentifier.of(catalogNameHive, database,
            tableName));
    return partitionLocations.stream().filter(e -> e.contains(parName)).findFirst().get();
  }
}

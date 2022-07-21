/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.netease.arctic.spark;

import com.netease.arctic.table.TableIdentifier;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.*;

public class TestCreateKeyedTableAsSelect extends SparkTestBase {

  private final String database = "db_def";
  private final String table = "testA";

  private final String sourceTable = "test_table";

  @Before
  public void prepare() {
    sql("use " + catalogName);
    sql("create database if not exists " + database);
    sql("create table {0}.{1} ( \n" +
        " id int , data string, pt string ) using arctic \n" +
        " partitioned by (pt) \n" , database, sourceTable);

    sql("insert overwrite {0}.{1} values \n" +
            "( 1, ''aaaa'', ''0001''), \n" +
            "( 2, ''aaaa'', ''0001''), \n" +
            "( 3, ''aaaa'', ''0001''), \n" +
            "( 4, ''aaaa'', ''0001''), \n" +
            "( 5, ''aaaa'', ''0002''), \n" +
            "( 6, ''aaaa'', ''0002''), \n" +
            "( 7, ''aaaa'', ''0002''), \n" +
            "( 8, ''aaaa'', ''0002'') \n" ,
        database, sourceTable);
  }

  @After
  public void removeTables() {
    sql("DROP TABLE IF EXISTS {0}.{1}", database, sourceTable);
  }

  @Test
  public void testCTAS() {
    TableIdentifier identifier = TableIdentifier.of(catalogName, database, table);
    sql("create table {0}.{1} using arctic primary key(id) AS SELECT * from {2}.{3}.{4}",
        database, table, catalogName, database, sourceTable);
    assertTableExist(identifier);
    sql("desc table {0}.{1}", database, table);
    assertDescResult(rows, Lists.newArrayList("id"));
    rows = sql("select * from {0}.{1}", database, table);
    Assert.assertEquals(8, rows.size());
  }

  private void assertDescResult(List<Object[]> rows, List<String> primaryKeys) {
    boolean primaryKeysBlock = false;
    List<String> descPrimaryKeys = Lists.newArrayList();
    for (Object[] row : rows) {
      if (StringUtils.equalsIgnoreCase("# Primary keys", row[0].toString())) {
        primaryKeysBlock = true;
      } else if (StringUtils.startsWith(row[0].toString(), "# ") && primaryKeysBlock) {
        primaryKeysBlock = false;
      } else if (primaryKeysBlock){
        descPrimaryKeys.add(row[0].toString());
      }
    }

    Assert.assertEquals(primaryKeys.size(), descPrimaryKeys.size());
    Assert.assertArrayEquals(primaryKeys.toArray(), descPrimaryKeys.toArray());
  }


}

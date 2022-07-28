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

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

public class TestMigrateNonHiveTable {
  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  private final String sourceDatabase = "test_db";
  private final String sourceTable = "test_table";
  private final String database = "db1";
  private final String table = "table1";

  @Before
  public void before(){
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create database if not exists " + database);

    sparkTestContext.sql("use spark_catalog");
    sparkTestContext.sql("create database if not exists {0}", sourceDatabase);
  }

  @Test
  public void testMigrateNoBucketParquetTable() {
    sparkTestContext.sql("create table {0}.{1} ( \n" +
        " id int , data string, pt string ) using parquet \n" +
        " partitioned by (pt) \n" , sourceDatabase, sourceTable);

    sparkTestContext.sql("insert overwrite {0}.{1} values \n" +
            "( 1, ''aaaa'', ''0001''), \n" +
            "( 2, ''aaaa'', ''0001''), \n" +
            "( 3, ''aaaa'', ''0001''), \n" +
            "( 4, ''aaaa'', ''0001''), \n" +
            "( 5, ''aaaa'', ''0002''), \n" +
            "( 6, ''aaaa'', ''0002''), \n" +
            "( 7, ''aaaa'', ''0002''), \n" +
            "( 8, ''aaaa'', ''0002'') \n" ,
        sourceDatabase, sourceTable);

    sparkTestContext.sql("migrate {0}.{1} to arctic {2}.{3}.{4} ",
        sourceDatabase, sourceTable,
        SparkTestContext.catalogName, database, table);

    sparkTestContext.rows = sparkTestContext.sql("select * from {0}.{1}.{2}", SparkTestContext.catalogName, database, table);
    Assert.assertEquals(8, sparkTestContext.rows.size());

    ArcticTable t = SparkTestContext.loadTable(SparkTestContext.catalogName, database, table);
    UnkeyedTable unkey = t.asUnkeyedTable();
    StructLikeMap<List<DataFile>> partitionFiles = SparkTestContext.partitionFiles(unkey);
    Assert.assertEquals(2, partitionFiles.size());

  }
}

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

package com.netease.arctic.spark.source;

import com.netease.arctic.spark.SparkTestContext;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.StructType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class TestKeyedTableDataFrameAPI {
  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();
  
  final String database = "ddd";
  final String table = "tbl";
  final String tablePath = SparkTestContext.catalogName + "." + database + "." + table;
  final TableIdentifier identifier = TableIdentifier.of(SparkTestContext.catalogName, database, table);
  final Schema schema = new Schema(
      Types.NestedField.of(1, false, "id", Types.IntegerType.get()),
      Types.NestedField.of(2, false, "data", Types.StringType.get()),
      Types.NestedField.of(3, false, "ts", Types.TimestampType.withZone())
  );

  Dataset<Row> df;


  @Before
  public void setUp() {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create database if not exists {0} ", database);
  }

  @After
  public void cleanUp() {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("drop table  if exists {0}.{1}", database, table);
    sparkTestContext.sql("drop database {0}", database);
  }


  @Test
  public void testV2ApiKeyedTable() throws Exception {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create table {0}.{1} (" +
        " id int, data string, ts timestamp, primary key (id) \n" +
        ") using arctic partitioned by (days(ts)) ", database, table);

    // test overwrite partitions
    StructType structType = SparkSchemaUtil.convert(schema);
    df = SparkTestContext.spark.createDataFrame(
        Lists.newArrayList(
            RowFactory.create(1, "aaa", SparkTestContext.quickTs(1)),
            RowFactory.create(2, "bbb", SparkTestContext.quickTs(2)),
            RowFactory.create(3, "ccc", SparkTestContext.quickTs(3))
        ), structType
    );
    df.writeTo(tablePath).overwritePartitions();

    df = SparkTestContext.spark.read().table(tablePath);
    Assert.assertEquals(3, df.count());

    df = SparkTestContext.spark.createDataFrame(
        Lists.newArrayList(
            RowFactory.create(4, "aaa", SparkTestContext.quickTs(3)),
            RowFactory.create(5, "bbb", SparkTestContext.quickTs(4)),
            RowFactory.create(6, "ccc", SparkTestContext.quickTs(5))
        ), structType
    );
    df.writeTo(tablePath).overwritePartitions();
    df = SparkTestContext.spark.read().table(tablePath);
    Assert.assertEquals(5, df.count());
  }

}

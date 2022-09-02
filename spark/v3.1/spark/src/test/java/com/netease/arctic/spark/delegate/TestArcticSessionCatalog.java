/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.delegate;

import com.netease.arctic.spark.ArcticSparkSessionCatalog;
import com.netease.arctic.spark.SparkTestContext;
import java.io.IOException;
import java.util.Map;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class TestArcticSessionCatalog extends SparkTestContext {

  @Rule
  public TestName testName = new TestName();
  protected long begin ;

  @BeforeClass
  public static void startAll() throws IOException, ClassNotFoundException {
    Map<String, String> configs = Maps.newHashMap();
    Map<String, String> arcticConfigs = setUpTestDirAndArctic();
    Map<String, String> hiveConfigs = setUpHMS();
    configs.putAll(arcticConfigs);
    configs.putAll(hiveConfigs);

    configs.put("spark.sql.catalog.spark_catalog", ArcticSparkSessionCatalog.class.getName());
    configs.put("spark.sql.catalog.spark_catalog.url", amsUrl + "/" + catalogNameHive);
    configs.put("spark.arctic.sql.delegate.enable", "true");

    setUpSparkSession(configs);
  }

  @AfterClass
  public static void stopAll() {
    cleanUpAms();
    cleanUpHive();
    cleanUpSparkSession();
  }


  @Before
  public void testBegin(){
    System.out.println("==================================");
    System.out.println("  Test Begin: " + testName.getMethodName());
    System.out.println("==================================");
    begin = System.currentTimeMillis();
  }

  @After
  public void after() {
    long cost = System.currentTimeMillis() - begin;
    System.out.println("==================================");
    System.out.println("  Test End: " + testName.getMethodName() + ", total cost: " + cost + " ms");
    System.out.println("==================================");
  }

  private String database = "default";
  private String table2 = "test2";
  private String table3 = "test3";
  private String table_D = "test4";
  private String table_D2 = "test5";
  
  @Test
  public void testHiveDelegate() throws TException {
    System.out.println("spark.arctic.sql.delegate.enable = " + spark.conf().get("spark.arctic.sql.delegate.enable"));
    sql("use spark_catalog");
    sql("create table {0}.{1} ( id int, data string) using arctic", database, table_D);
    sql("create table {0}.{1} ( id int, data string) STORED AS parquet", database, table_D2);
    sql("insert overwrite {0}.{1} values \n" +
        "(1, ''aaa''), \n " +
        "(2, ''bbb''), \n " +
        "(3, ''ccc'') \n ", database, table_D);
    sql("insert overwrite {0}.{1} values \n" +
        "(1, ''aaa''), \n " +
        "(2, ''bbb''), \n " +
        "(3, ''ccc'') \n ", database, table_D2);
    Table tableA = hms.getClient().getTable(database, table_D);
    Assert.assertNotNull(tableA);
    Table tableB = hms.getClient().getTable(database, table_D2);
    Assert.assertNotNull(tableB);
    sql("select * from {0}.{1}", database, table_D);
    sql("select * from {0}.{1}", database, table_D2);

    sql("drop table {0}.{1}", database, table_D);
    sql("drop table {0}.{1}", database, table_D2);

  }

  @Test
  public void testCatalogEnable() throws TException {
    sql("set spark.arctic.sql.delegate.enable=false");
    sql("use spark_catalog");
    System.out.println("spark.arctic.sql.delegate.enable = " + spark.conf().get("spark.arctic.sql.delegate.enable"));
    sql("create table {0}.{1} ( id int, data string) STORED AS parquet", database, table2);
    sql("insert overwrite {0}.{1} values \n" +
        "(1, ''aaa''), \n " +
        "(2, ''bbb''), \n " +
        "(3, ''ccc'') \n ", database, table2);
    Table tableB = hms.getClient().getTable(database, table2);
    Assert.assertNotNull(tableB);
    sql("select * from {0}.{1}", database, table2);

    sql("create table {0}.{1} ( id int, data string) STORED AS parquet", database, table3);
    sql("insert overwrite {0}.{1} values \n" +
        "(4, ''aaa''), \n " +
        "(5, ''bbb''), \n " +
        "(6, ''ccc'') \n ", database, table2);
    rows = sql("select * from {0}.{1}", database, table2);
    assertContainIdSet(rows, 0, 4, 5, 6);
    sql("select * from {0}.{1}", database, table3);

    sql("drop table {0}.{1}", database, table2);
    sql("drop table {0}.{1}", database, table3);
  }





}

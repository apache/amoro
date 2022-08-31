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
    configs.put("arctic.sql.delegate-hive-table", "false");

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
  private String table = "test";

  @Test
  public void testCatalogEnable() throws IOException, TException {
    sql("create table {0} ( id int, data string) using arctic", table);
    hms.getClient().getTable("default", table);
    // Assert.assertNotNull(hiveTableA);
  }

  private void enableHiveDelegate() {
    sql("set `arctic.sql.delegate-hive-table` = true");
  }
}

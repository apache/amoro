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

import com.netease.arctic.AmsClientPools;
import com.netease.arctic.CatalogMetaTestUtil;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.spark.ArcticSparkCatalog;
import com.netease.arctic.spark.ArcticSparkExtensions;
import com.netease.arctic.spark.SparkTestContext;
import org.apache.commons.io.FileUtils;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.internal.SQLConf;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * context from all spark test with hive dependency
 */
public class SparkHiveTestContext extends SparkTestContext {

  static final File hmsDir = new File(testBaseDir, "hive");
  static final HMSMockServer hms = new HMSMockServer(hmsDir);


  public static void setUpHMS(){
    System.out.println("======================== start hive metastore ========================= ");
    hms.start();
    additionSparkConfigs.put("hive.metastore.uris", "thrift://127.0.0.1:" + hms.getMetastorePort()) ;
    additionSparkConfigs.put("spark.sql.catalogImplementation", "hive");
    additionSparkConfigs.put("spark.sql.hive.metastore.version", "2.3.7");
    //hive.metastore.client.capability.check
    additionSparkConfigs.put("hive.metastore.client.capability.check", "false");
  }

  public static void setUpTestDirAndArctic() throws IOException {
    System.out.println("======================== start AMS  ========================= ");
    FileUtils.deleteQuietly(testBaseDir);
    testBaseDir.mkdirs();

    AmsClientPools.cleanAll();
    if (!ams.isStarted()) {
      ams.start();
    }
    amsUrl = "thrift://127.0.0.1:" + ams.port();

    CatalogMeta arctic_hive = HiveCatalogMetaTestUtil.createArcticCatalog(testArcticDir);
    catalogName = arctic_hive.getCatalogName();
    ams.handler().createCatalog(arctic_hive);
  }

  public static void setUpSparkSession() {
    System.out.println("======================== set up spark session  ========================= ");
    Map<String, String> sparkConfigs = Maps.newHashMap();

    sparkConfigs.put(SQLConf.PARTITION_OVERWRITE_MODE().key(), "DYNAMIC");
    sparkConfigs.put("spark.executor.heartbeatInterval", "300s");
    sparkConfigs.put("spark.network.timeout", "500s");
    sparkConfigs.put("spark.sql.warehouse.dir", testSparkDir.getAbsolutePath());
    sparkConfigs.put("spark.sql.extensions", ArcticSparkExtensions.class.getName());
    sparkConfigs.put("spark.testing.memory", "471859200");

    sparkConfigs.put("spark.sql.catalog." + catalogName, ArcticSparkCatalog.class.getName());
    sparkConfigs.put("spark.sql.catalog." + catalogName + ".type", "hive");
    sparkConfigs.put("spark.sql.catalog." + catalogName + ".url", amsUrl + "/" + catalogName);

    sparkConfigs.putAll(additionSparkConfigs);
    sparkConfigs.forEach(((k, v) -> System.out.println("--" + k + "=" + v)));


    SparkConf sparkconf = new SparkConf()
        .setAppName("test")
        .setMaster("local");

    sparkConfigs.forEach(sparkconf::set);

    spark = SparkSession
        .builder()
        .config(sparkconf)
        .getOrCreate();
    spark.sparkContext().setLogLevel("WARN");
  }

  public static void cleanUpHive() {
    System.out.println("======================== stop hive metastore ========================= ");
    hms.stop();
  }
}

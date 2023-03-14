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

package com.netease.arctic.spark.test;

import com.netease.arctic.CatalogMetaTestUtil;
import com.netease.arctic.TestAms;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.spark.ArcticSparkExtensions;
import com.netease.arctic.spark.SparkSQLProperties;
import com.netease.arctic.spark.hive.HiveCatalogMetaTestUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public abstract class SparkTestBase implements SupportSparkContext {

  public static final Logger LOG = LoggerFactory.getLogger("SparkUnitTests");

  @ClassRule public static final TemporaryFolder warehouse = new TemporaryFolder();

  /**
   * Mocked AMS instance for tests
   */
  @ClassRule public static final TestAms AMS = new TestAms();

  /**
   * embedded HMS instance for tests
   */
  @ClassRule public static final TestHMS HMS = new TestHMS();

  static String catalogNameInAMS(String catalogType) {
    if ("Hive".equalsIgnoreCase(catalogType)) {
      return "hive_catalog";
    } else if ("Arctic".equalsIgnoreCase(catalogType)) {
      return "arctic_catalog";
    } else {
      throw new IllegalArgumentException("unknown type of catalog type:" + catalogType);
    }
  }

  @BeforeClass
  public static void setupCatalogs() throws IOException {
    CatalogMeta arcticCatalogMeta = CatalogMetaTestUtil.createArcticCatalog(warehouse.getRoot());
    arcticCatalogMeta.setCatalogName(catalogNameInAMS("ARCTIC"));
    AMS.getAmsHandler().createCatalog(arcticCatalogMeta);

    HiveConf hiveConf = HMS.getHiveConf();
    CatalogMeta hiveCatalogMeta = HiveCatalogMetaTestUtil.createArcticCatalog(warehouse.getRoot(), hiveConf);
    hiveCatalogMeta.setCatalogName(catalogNameInAMS("HIVE"));
    AMS.getAmsHandler().createCatalog(hiveCatalogMeta);
  }

  @AfterClass
  public static void cleanCatalogs() {
    AMS.getAmsHandler().cleanUp();
  }

  private SparkSession spark;

  protected abstract Map<String, String> testSparkConf();

  private Map<String, String> sparkConf() {
    Map<String, String> configs = Maps.newHashMap();
    configs.putAll(testSparkConf());
    String hiveVersion = null;
    try {
      hiveVersion = SparkTestBase.class.getClassLoader()
          .loadClass("org.apache.hadoop.hive.metastore.HiveMetaStoreClient")
          .getPackage()
          .getImplementationVersion();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    configs.put("hive.metastore.uris", "thrift://127.0.0.1:" + HMS.getMetastorePort());
    configs.put("spark.sql.catalogImplementation", "hive");
    configs.put("spark.sql.hive.metastore.version", hiveVersion);
    configs.put("spark.sql.hive.metastore.jars", "maven");
    configs.put("hive.metastore.client.capability.check", "false");

    configs.put("spark.executor.heartbeatInterval", "500s");
    configs.put("spark.cores.max", "6");
    configs.put("spark.executor.cores", "2");
    configs.put("spark.default.parallelism", "12");
    configs.put("spark.network.timeout", "600s");
    configs.put("spark.sql.warehouse.dir", warehouse.getRoot().getAbsolutePath());
    configs.put("spark.sql.extensions", ArcticSparkExtensions.class.getName());
    configs.put("spark.testing.memory", "943718400");

    return configs;
  }

  @Override
  public SparkSession spark() {
    if (spark != null) {
      return spark;
    }
    SparkConf sparkconf = new SparkConf()
        .setAppName("arctic-spark-unit-tests")
        .setMaster("local[*]");

    Map<String, String> conf = sparkConf();
    conf.forEach(sparkconf::set);
    this.spark = SparkSession
        .builder()
        .config(sparkconf)
        .getOrCreate();
    spark.sparkContext().setLogLevel("WARN");
    return spark;
  }

  @After
  public void closeSparkSession() {
    if (this.spark != null) {
      this.spark.close();
    }
  }

  public Dataset<Row> sql(String sqlText) {
    LOG.info("Execute SQL: " + sqlText);
    Dataset<Row> ds = spark().sql(sqlText);
    if (ds.columns().length == 0) {
      LOG.info("+----------------+");
      LOG.info("|  Empty Result  |");
      LOG.info("+----------------+");
    } else {
      ds.show();
    }
    return ds;
  }

  public boolean useTimestampWithoutZoneInNewTable() {
    String value = spark().sessionState().conf()
        .getConfString(
            SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES,
            SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT);
    return Boolean.parseBoolean(value);
  }
}

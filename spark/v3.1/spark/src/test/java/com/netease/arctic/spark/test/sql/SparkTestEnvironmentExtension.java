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

package com.netease.arctic.spark.test.sql;

import com.netease.arctic.CatalogMetaTestUtil;
import com.netease.arctic.SingletonResourceUtil;
import com.netease.arctic.TestAms;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.spark.ArcticSparkCatalog;
import com.netease.arctic.spark.ArcticSparkExtensions;
import com.netease.arctic.spark.ArcticSparkSessionCatalog;
import com.netease.arctic.spark.hive.HiveCatalogMetaTestUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.thrift.TException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class SparkTestEnvironmentExtension
    implements BeforeAllCallback, AfterAllCallback,
    BeforeEachCallback, AfterEachCallback {
  private static final Logger LOG = LoggerFactory.getLogger(SparkTestEnvironmentExtension.class);

  public final TemporaryFolder warehouse = new TemporaryFolder();

  /**
   * Mocked AMS instance for tests
   */
  public final TestAms AMS = new TestAms();

  /**
   * embedded HMS instance for tests
   */
  public final TestHMS HMS = new TestHMS();

  private boolean catalogSet = false;

  public String amsCatalogName(String catalogType) {
    if ("Hive".equalsIgnoreCase(catalogType)) {
      return "hive_catalog";
    } else if ("Arctic".equalsIgnoreCase(catalogType)) {
      return "arctic_catalog";
    } else {
      throw new IllegalArgumentException("unknown type of catalog type:" + catalogType);
    }
  }

  private void setupCatalogs() throws IOException {
    if (SingletonResourceUtil.isUseSingletonResource()) {
      if (catalogSet) {
        return;
      }
    }
    CatalogMeta arcticCatalogMeta = CatalogMetaTestUtil.createArcticCatalog(warehouse.getRoot());
    arcticCatalogMeta.setCatalogName(amsCatalogName("ARCTIC"));
    AMS.getAmsHandler().createCatalog(arcticCatalogMeta);

    HiveConf hiveConf = HMS.getHiveConf();
    CatalogMeta hiveCatalogMeta = HiveCatalogMetaTestUtil.createArcticCatalog(warehouse.getRoot(), hiveConf);
    hiveCatalogMeta.setCatalogName(amsCatalogName("HIVE"));
    AMS.getAmsHandler().createCatalog(hiveCatalogMeta);
    catalogSet = true;
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    AMS.before();
    HMS.before();
    warehouse.create();
    setupCatalogs();
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    if (!SingletonResourceUtil.isUseSingletonResource()) {
      AMS.getAmsHandler().cleanUp();
      catalogSet = false;
    }
    AMS.after();
    HMS.after();
  }

  public String hiveMetastoreUri() {
    return "thrift://127.0.0.1:" + HMS.getMetastorePort();
  }

  public String hiveVersion() {
    String hiveVersion = null;
    try {
      hiveVersion = SparkTestBase.class.getClassLoader()
          .loadClass("org.apache.hadoop.hive.metastore.HiveMetaStoreClient")
          .getPackage()
          .getImplementationVersion();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return hiveVersion;
  }

  public String warehouseDir() {
    return this.warehouse.getRoot().getAbsolutePath();
  }

  public String amsServerUrl() {
    return this.AMS.getServerUrl();
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    boolean usingArcticSessionCatalog = isAnnotatedUsingArcticSessionCatalog(context);
    initSparkSession(usingArcticSessionCatalog);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {

  }

  private boolean isAnnotatedUsingArcticSessionCatalog(ExtensionContext context) {
    Optional<SessionCatalog> classAnnotation = AnnotationUtils.findAnnotation(
        context.getRequiredTestClass(), SessionCatalog.class);
    Optional<SessionCatalog> methodAnnotation = AnnotationUtils.findAnnotation(
        context.getRequiredTestMethod(), SessionCatalog.class
    );
    if (methodAnnotation.isPresent()) {
      return methodAnnotation.get().usingArcticSessionCatalog();
    } else if (classAnnotation.isPresent()) {
      return classAnnotation.get().usingArcticSessionCatalog();
    }
    return false;
  }

  private void initSparkSession(boolean arcticSessionCatalog) {
    Map<String, String> configs = Maps.newHashMap();
    configs.put("spark.sql.catalog." + amsCatalogName("arctic"), ArcticSparkCatalog.class.getName());
    configs.put("spark.sql.catalog." + amsCatalogName("arctic") + ".url"
        , amsServerUrl() + "/" + amsCatalogName("arctic"));
    configs.put("spark.sql.catalog." + amsCatalogName("hive"), ArcticSparkCatalog.class.getName());
    configs.put("spark.sql.catalog." + amsCatalogName("hive") + ".url"
        , amsServerUrl() + "/" + amsCatalogName("hive"));
    if (arcticSessionCatalog) {
      configs.put("spark.sql.catalog.spark_catalog", ArcticSparkSessionCatalog.class.getName());
      configs.put(
          "spark.sql.catalog.spark_catalog.url",
          amsServerUrl() + "/" + amsCatalogName("hive"));
    }

    configs.put("hive.metastore.uris", this.hiveMetastoreUri());
    configs.put("spark.sql.catalogImplementation", "hive");
    configs.put("spark.sql.hive.metastore.version", this.hiveVersion());
    configs.put("spark.sql.hive.metastore.jars", "maven");
    configs.put("hive.metastore.client.capability.check", "false");

    configs.put("spark.executor.heartbeatInterval", "500s");
    configs.put("spark.cores.max", "6");
    configs.put("spark.executor.cores", "2");
    configs.put("spark.default.parallelism", "12");
    configs.put("spark.network.timeout", "600s");
    configs.put("spark.sql.warehouse.dir", this.warehouseDir());
    configs.put("spark.sql.extensions", ArcticSparkExtensions.class.getName());
    configs.put("spark.testing.memory", "943718400");

    initializeSparkSession(configs);
  }

  SparkSession _spark;
  Map<String, String> _sparkConf;

  private void initializeSparkSession(Map<String, String> sparkConf) {
    boolean create = false;
    if (_spark == null) {
      create = true;
    }
    if (!isSameSparkConf(sparkConf)) {
      create = true;
    }
    if (create) {
      cleanLocalSparkContext();

      SparkConf sparkconf = new SparkConf()
          .setAppName("arctic-spark-unit-tests")
          .setMaster("local[*]");
      sparkConf.forEach(sparkconf::set);
      _spark = SparkSession
          .builder()
          .config(sparkconf)
          .getOrCreate();
      _spark.sparkContext().setLogLevel("WARN");
      _sparkConf = sparkConf;
    }
  }

  private boolean isSameSparkConf(Map<String, String> sparkConf) {
    if (_sparkConf == null) {
      return false;
    }
    if (_sparkConf.size() != sparkConf.size()) {
      return false;
    }
    for (String key : sparkConf.keySet()) {
      String value = sparkConf.get(key);
      String _value = _sparkConf.get(key);
      if (!StringUtils.equals(value, _value)) {
        return false;
      }
    }
    return true;
  }

  private void cleanLocalSparkContext() {
    if (_spark != null) {
      _spark.close();
      _spark = null;
    }
  }

  public void createHiveDatabaseIfNotExist(String database) {
    Database db = new Database();
    db.setName(database);
    try {
      HMS.getHiveClient().createDatabase(db);
    } catch (AlreadyExistsException e) {
      // pass
    } catch (TException e) {
      throw new RuntimeException(e);
    }
  }

  public SparkSession spark() {
    if (this._spark == null) {
      throw new IllegalStateException("spark context is not initialized");
    }
    return this._spark.cloneSession();
  }
}

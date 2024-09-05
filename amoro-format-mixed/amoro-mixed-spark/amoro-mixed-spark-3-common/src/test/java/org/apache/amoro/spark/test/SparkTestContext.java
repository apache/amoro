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

package org.apache.amoro.spark.test;

import org.apache.amoro.SingletonResourceUtil;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TestAms;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Joiner;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.thrift.TException;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class SparkTestContext {

  public static final String SESSION_CATALOG_IMPL =
      "org.apache.amoro.spark.MixedFormatSparkSessionCatalog";
  public static final String MIXED_CATALOG_IMPL = "org.apache.amoro.spark.MixedFormatSparkCatalog";
  public static final String SQL_EXTENSIONS_IMPL =
      "org.apache.amoro.spark.MixedFormatSparkExtensions"
          + ",org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions";

  public static final String UNIFIED_CATALOG_IMP = "org.apache.amoro.spark.SparkUnifiedCatalog";

  final TemporaryFolder warehouse = new TemporaryFolder();
  public static final String AMS_ALL_FORMAT_CATALOG_NAME = "all_formats";

  /** Define spark catalog names. */
  public static class SparkCatalogNames {
    public static final String MIXED_HIVE = "mixed_hive_catalog";
    public static final String MIXED_ICEBERG = "mixed_iceberg_catalog";

    public static final String UNIFIED_ICEBERG = "unified_iceberg";
    public static final String UNIFIED_MIXED_ICEBERG = "unified_mixed_iceberg";
    public static final String UNIFIED_MIXED_HIVE = "unified_mixed_hive";
    public static final String UNIFIED_PAIMON = "unified_paimon";
  }

  final TestAms ams = new TestAms();

  final TestHMS hms = new TestHMS();

  private boolean catalogSet = false;
  private SparkSession spark;
  private Map<String, String> sparkConf;

  public void initialize() throws Exception {
    ams.before();
    hms.before();
    warehouse.create();
    setupCatalogs();
  }

  public void close() {
    if (!SingletonResourceUtil.isUseSingletonResource()) {
      ams.getAmsHandler().cleanUp();
      catalogSet = false;
    }
    ams.after();
    hms.after();
  }

  public Table loadHiveTable(String database, String table) {
    try {
      return hms.getHiveClient().getTable(database, table);
    } catch (NoSuchObjectException e) {
      throw new NoSuchTableException(e, database + "." + table);
    } catch (TException e) {
      throw new RuntimeException(e);
    }
  }

  public void dropHiveTable(String database, String table) {
    try {
      hms.getHiveClient().dropTable(database, table, true, true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public HiveMetaStoreClient getHiveClient() {
    return hms.getHiveClient();
  }

  private void setupCatalogs() {
    if (SingletonResourceUtil.isUseSingletonResource()) {
      if (catalogSet) {
        return;
      }
    }
    HiveConf hiveConf = hms.getHiveConf();
    for (TableFormat format : TableFormat.values()) {
      if (format == TableFormat.HUDI) {
        continue;
      }
      // create catalog for all formats in AMS with hive metastore.
      CatalogMeta hiveCatalogMeta =
          HiveCatalogTestHelper.build(hiveConf, format)
              .buildCatalogMeta(warehouse.getRoot().getAbsolutePath());
      hiveCatalogMeta.setCatalogName(format.name().toLowerCase());
      ams.getAmsHandler().createCatalog(hiveCatalogMeta);
    }

    CatalogMeta allFormats =
        HiveCatalogTestHelper.build(hiveConf, TableFormat.values()[0])
            .buildCatalogMeta(warehouse.getRoot().getAbsolutePath());
    // spark unified catalog doesn't support hudi.
    String formats =
        Joiner.on(',')
            .join(
                Arrays.stream(TableFormat.values())
                    .filter(f -> TableFormat.HUDI != f)
                    .collect(Collectors.toList()));
    allFormats.putToCatalogProperties(CatalogMetaProperties.TABLE_FORMATS, formats);
    allFormats.setCatalogName(AMS_ALL_FORMAT_CATALOG_NAME);
    ams.getAmsHandler().createCatalog(allFormats);

    catalogSet = true;
  }

  public String amsCatalogUrl(TableFormat format) {
    if (format != null) {
      return this.ams.getServerUrl() + "/" + format.name().toLowerCase();
    }
    return this.ams.getServerUrl() + "/" + AMS_ALL_FORMAT_CATALOG_NAME;
  }

  public String amsThriftUrl() {
    return this.ams.getServerUrl();
  }

  private String hiveVersion() {
    try {
      return SparkTestContext.class
          .getClassLoader()
          .loadClass("org.apache.hadoop.hive.metastore.HiveMetaStoreClient")
          .getPackage()
          .getImplementationVersion();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private String hiveMetastoreUri() {
    return "thrift://127.0.0.1:" + hms.getMetastorePort();
  }

  private String warehouseDir() {
    return this.warehouse.getRoot().getAbsolutePath();
  }

  public SparkSession getSparkSession(Map<String, String> externalConfigs) {
    Map<String, String> configs = Maps.newHashMap();

    addMixedSparkCatalog(configs, SparkCatalogNames.MIXED_HIVE, TableFormat.MIXED_HIVE);
    addMixedSparkCatalog(configs, SparkCatalogNames.MIXED_ICEBERG, TableFormat.MIXED_ICEBERG);

    addUnifiedSparkCatalog(configs, SparkCatalogNames.UNIFIED_ICEBERG, TableFormat.ICEBERG);
    addUnifiedSparkCatalog(configs, SparkCatalogNames.UNIFIED_PAIMON, TableFormat.PAIMON);
    addUnifiedSparkCatalog(configs, SparkCatalogNames.UNIFIED_MIXED_HIVE, TableFormat.MIXED_HIVE);
    addUnifiedSparkCatalog(
        configs, SparkCatalogNames.UNIFIED_MIXED_ICEBERG, TableFormat.MIXED_ICEBERG);

    configs.put("hive.metastore.uris", this.hiveMetastoreUri());
    configs.put("spark.sql.catalogImplementation", "hive");
    configs.put("spark.sql.hive.metastore.version", this.hiveVersion());
    configs.put("spark.sql.hive.metastore.jars", "builtin");
    configs.put("hive.metastore.client.capability.check", "false");

    configs.put("spark.executor.heartbeatInterval", "500s");
    configs.put("spark.cores.max", "6");
    configs.put("spark.executor.cores", "2");
    configs.put("spark.default.parallelism", "12");
    configs.put("spark.network.timeout", "600s");
    configs.put("spark.sql.warehouse.dir", this.warehouseDir());
    configs.put("spark.sql.extensions", SQL_EXTENSIONS_IMPL);
    configs.put("spark.testing.memory", "943718400");

    if (externalConfigs != null) {
      configs.putAll(externalConfigs);
    }

    initializeSparkSession(configs);
    return this.spark.cloneSession();
  }

  private void addMixedSparkCatalog(
      Map<String, String> configs, String catalogName, TableFormat format) {
    configs.put("spark.sql.catalog." + catalogName, MIXED_CATALOG_IMPL);
    configs.put("spark.sql.catalog." + catalogName + ".uri", amsCatalogUrl(format));
  }

  private void addUnifiedSparkCatalog(
      Map<String, String> configs, String catalogName, TableFormat format) {
    configs.put("spark.sql.catalog." + catalogName, UNIFIED_CATALOG_IMP);
    configs.put("spark.sql.catalog." + catalogName + ".uri", amsCatalogUrl(format));
  }

  private boolean isSameSparkConf(Map<String, String> sparkConf) {
    if (this.sparkConf == null) {
      return false;
    }
    if (this.sparkConf.size() != sparkConf.size()) {
      return false;
    }
    for (String key : sparkConf.keySet()) {
      String newValue = sparkConf.get(key);
      String existedValue = this.sparkConf.get(key);
      if (!StringUtils.equals(newValue, existedValue)) {
        return false;
      }
    }
    return true;
  }

  private void cleanLocalSparkContext() {
    if (spark != null) {
      spark.close();
      spark = null;
    }
  }

  private void initializeSparkSession(Map<String, String> sparkConf) {
    boolean create = spark == null;
    if (!isSameSparkConf(sparkConf)) {
      create = true;
    }
    if (create) {
      cleanLocalSparkContext();

      SparkConf sparkconf =
          new SparkConf().setAppName("mixed-format-spark-unit-tests").setMaster("local[*]");
      sparkConf.forEach(sparkconf::set);
      spark = SparkSession.builder().config(sparkconf).getOrCreate();
      spark.sparkContext().setLogLevel("WARN");
      this.sparkConf = sparkConf;
    }
  }
}

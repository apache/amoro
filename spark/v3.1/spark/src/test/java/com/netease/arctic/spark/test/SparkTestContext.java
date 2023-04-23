package com.netease.arctic.spark.test;


import com.netease.arctic.CatalogMetaTestUtil;
import com.netease.arctic.SingletonResourceUtil;
import com.netease.arctic.TestAms;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.spark.ArcticSparkCatalog;
import com.netease.arctic.spark.ArcticSparkExtensions;
import com.netease.arctic.spark.ArcticSparkSessionCatalog;
import com.netease.arctic.spark.hive.HiveCatalogMetaTestUtil;
import com.netease.arctic.spark.test.sql.SparkTestBase;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.thrift.TException;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Map;

public class SparkTestContext {

  final TemporaryFolder warehouse = new TemporaryFolder();

  final TestAms AMS = new TestAms();

  final TestHMS HMS = new TestHMS();

  private boolean catalogSet = false;
  private SparkSession _spark;
  private Map<String, String> _sparkConf;

  public void initialize() throws Exception {
    AMS.before();
    HMS.before();
    warehouse.create();
    setupCatalogs();
  }

  public void close() {
    if (!SingletonResourceUtil.isUseSingletonResource()) {
      AMS.getAmsHandler().cleanUp();
      catalogSet = false;
    }
    AMS.after();
    HMS.after();
  }

  public Table loadHiveTable(String database, String table) {
    try {
      return HMS.getHiveClient().getTable(database, table);
    } catch (NoSuchObjectException e) {
      throw new NoSuchTableException(e, database + "." + table);
    } catch (TException e) {
      throw new RuntimeException(e);
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


  public String amsCatalogName(String catalogType) {
    if ("Hive".equalsIgnoreCase(catalogType)) {
      return "hive_catalog";
    } else if ("Arctic".equalsIgnoreCase(catalogType)) {
      return "arctic_catalog";
    } else {
      throw new IllegalArgumentException("unknown type of catalog type:" + catalogType);
    }
  }

  private String hiveVersion() {
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

  private String hiveMetastoreUri() {
    return "thrift://127.0.0.1:" + HMS.getMetastorePort();
  }

  private String warehouseDir() {
    return this.warehouse.getRoot().getAbsolutePath();
  }

  public SparkSession getSparkSession(boolean arcticSessionCatalog) {
    Map<String, String> configs = Maps.newHashMap();
    configs.put("spark.sql.catalog." + amsCatalogName("arctic"), ArcticSparkCatalog.class.getName());
    configs.put("spark.sql.catalog." + amsCatalogName("arctic") + ".url"
        , this.AMS.getServerUrl() + "/" + amsCatalogName("arctic"));
    configs.put("spark.sql.catalog." + amsCatalogName("hive"), ArcticSparkCatalog.class.getName());
    configs.put("spark.sql.catalog." + amsCatalogName("hive") + ".url"
        , this.AMS.getServerUrl() + "/" + amsCatalogName("hive"));
    if (arcticSessionCatalog) {
      configs.put("spark.sql.catalog.spark_catalog", ArcticSparkSessionCatalog.class.getName());
      configs.put(
          "spark.sql.catalog.spark_catalog.url",
          this.AMS.getServerUrl() + "/" + amsCatalogName("hive"));
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
    return this._spark.cloneSession();
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
}

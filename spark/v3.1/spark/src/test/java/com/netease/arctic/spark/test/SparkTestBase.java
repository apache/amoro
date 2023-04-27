package com.netease.arctic.spark.test;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.spark.test.extensions.EachParameterResolver;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SparkTestBase {
  protected static final Logger LOG = LoggerFactory.getLogger(SparkTestBase.class);
  public final static SparkTestContext context = new SparkTestContext();
  public static final String SESSION_CATALOG = "spark_catalog";
  public static final String INTERNAL_CATALOG = "arctic_catalog";
  public static final String HIVE_CATALOG = "hive_catalog";

  @RegisterExtension
  private final EachParameterResolver eachParameterResolver = new EachParameterResolver();

  @BeforeAll
  public static void setupContext() throws Exception {
    context.initialize();
  }

  @AfterAll
  public static void tearDownContext() {
    context.close();
  }

  protected SparkSession spark;
  private ArcticCatalog _catalog;
  protected String currentCatalog = SESSION_CATALOG;


  @AfterEach
  public void tearDownTestSession() {
    spark = null;
    _catalog = null;
  }

  public void setCurrentCatalog(String catalog) {
    this.currentCatalog = catalog;
    sql("USE " + this.currentCatalog);
    this._catalog = null;
  }

  protected ArcticCatalog catalog() {
    if (_catalog == null) {
      String catalogUrl = spark.sessionState().conf().getConfString(
          "spark.sql.catalog." + currentCatalog + ".url");
      _catalog = CatalogLoader.load(catalogUrl);
    }
    return _catalog;
  }

  protected boolean isHiveCatalog() {
    return SESSION_CATALOG.equalsIgnoreCase(currentCatalog) || HIVE_CATALOG.equals(currentCatalog);
  }


  public Dataset<Row> sql(String sqlText) {
    if (this.spark == null){
      this.spark = context.getSparkSession(true);
    }
    long begin = System.currentTimeMillis();
    LOG.info("Execute SQL: " + sqlText);
    Dataset<Row> ds = spark.sql(sqlText);
    if (ds.columns().length == 0) {
      LOG.info("+----------------+");
      LOG.info("|  Empty Result  |");
      LOG.info("+----------------+");
    } else {
      ds.show();
    }
    LOG.info("SQL Execution cost: " + (System.currentTimeMillis() - begin) + " ms");
    return ds;
  }
}

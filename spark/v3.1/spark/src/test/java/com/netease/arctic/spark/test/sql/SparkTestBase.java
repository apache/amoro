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

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.utils.CollectionHelper;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.thrift.TException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class SparkTestBase {

  public static final Logger LOG = LoggerFactory.getLogger("SparkUnitTests");

  @RegisterExtension
  public static final SparkTestEnvironmentExtension env = new SparkTestEnvironmentExtension();

  public static final String SESSION_CATALOG = "spark_catalog";
  public static final String INTERNAL_CATALOG = "arctic_catalog";
  public static final String HIVE_CATALOG = "hive_catalog";

  protected SparkSession spark;

  @BeforeEach
  public void cloneSparkSession() {
    this.spark = env.spark();
  }

  @AfterEach
  public void cleanUpSpark() {
    this.spark = null;
  }

  public Dataset<Row> sql(String sqlText) {
    LOG.info("Execute SQL: " + sqlText);
    Dataset<Row> ds = spark.sql(sqlText);
    if (ds.columns().length == 0) {
      LOG.info("+----------------+");
      LOG.info("|  Empty Result  |");
      LOG.info("+----------------+");
    } else {
      ds.show();
    }
    return ds;
  }

  public String catalogUrl(String sparkCatalog) {
    return spark.sessionState().conf().getConfString("spark.sql.catalog." + sparkCatalog + ".url");
  }

  public String arcticCatalogName(String sparkCatalog) {
    String url = catalogUrl(sparkCatalog);
    return url.substring(url.lastIndexOf('/') + 1);
  }

  public boolean isHiveCatalog(String sparkCatalog) {
    String catalogName = arcticCatalogName(sparkCatalog);
    try {
      CatalogMeta meta = env.AMS.getAmsHandler().getCatalog(catalogName);
      return "HIVE".equalsIgnoreCase(meta.getCatalogType());
    } catch (TException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, String> asMap(String... kv) {
    return CollectionHelper.asMap(kv);
  }

  protected abstract static class TestContext<T> {
    private List<Consumer<T>> beforeHocks = Lists.newArrayList();
    private List<Consumer<T>> afterHocks = Lists.newArrayList();

    public TestContext<T> before(Consumer<T> before) {
      this.beforeHocks.add(before);
      return this;
    }

    public TestContext<T> after(Consumer<T> after) {
      this.afterHocks.add(after);
      return this;
    }

    public void test(Consumer<T> test) {
      beforeHocks.forEach(b -> b.accept(testEnv()));
      try {
        test.accept(testEnv());
      } finally {
        afterHocks.forEach(a -> a.accept(testEnv()));
      }
    }

    abstract T testEnv();
  }
}

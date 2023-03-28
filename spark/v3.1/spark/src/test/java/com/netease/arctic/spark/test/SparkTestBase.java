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

import com.netease.arctic.ams.api.CatalogMeta;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.thrift.TException;
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

  public Dataset<Row> sql(String sqlText) {
    return env.sql(sqlText);
  }

  public String catalogUrl(String sparkCatalog) {
    return env.getSparkConf("spark.sql.catalog." + sparkCatalog + ".url");
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
    Preconditions.checkArgument(kv.length % 2 == 0, "number of key value pairs must even");
    Map<String, String> map = Maps.newHashMap();
    for (int i = 0; i < kv.length; i = i + 2) {
      map.put(kv[i], kv[i + 1]);
    }
    return map;
  }

  protected abstract static class TestContext<T> {
    private List<Runnable> beforeHocks = Lists.newArrayList();
    private List<Runnable> afterHocks = Lists.newArrayList();

    public TestContext<T> before(Runnable before) {
      this.beforeHocks.add(before);
      return this;
    }

    public TestContext<T> after(Runnable after) {
      this.afterHocks.add(after);
      return this;
    }

    public void test(Consumer<T> test) {
      for (Runnable r : beforeHocks) {
        r.run();
      }
      try {
        test.accept(testEnv());
      } finally {
        afterHocks.forEach(Runnable::run);
      }
    }

    abstract T testEnv();
  }
}

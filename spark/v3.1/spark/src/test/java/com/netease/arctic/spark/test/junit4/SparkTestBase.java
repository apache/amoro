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

package com.netease.arctic.spark.test.junit4;

import com.netease.arctic.ams.api.CatalogMeta;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkTestBase implements SupportExecuteSQL {

  public static final Logger LOG = LoggerFactory.getLogger("SparkUnitTests");
  public static final SparkTestContext context = new SparkTestContext();

  public static final String SESSION_CATALOG = "spark_catalog";
  public static final String INTERNAL_CATALOG = "arctic_catalog";
  public static final String HIVE_CATALOG = "hive_catalog";

  @BeforeClass
  public static void beforeAll() {
    context.beforeAll();
  }

  @AfterClass
  public static void afterAll() {
    context.afterAll();
  }

  String catalog;

  protected void setTestCatalog(String catalog) {
    this.catalog = catalog;
  }

  @Override
  public String catalog() {
    return catalog;
  }

  @Before
  public void beforeEach() {
    context.beforeEach(usingArcticSessionCatalog());
  }

  @AfterEach
  public void afterEach() {
    context.afterEach();
  }

  protected boolean usingArcticSessionCatalog() {
    return true;
  }

  @Override
  public Dataset<Row> sql(String sqlText) {
    return context.sql(sqlText);
  }

  public String catalogUrl() {
    return context.getSparkConf("spark.sql.catalog." + catalog() + ".url");
  }

  public String arcticCatalogName() {
    String url = catalogUrl();
    return url.substring(url.lastIndexOf('/'));
  }

  public String catalogType() {
    String catalogName = arcticCatalogName();
    try {
      CatalogMeta meta = context.AMS.getAmsHandler().getCatalog(catalogName);
    } catch (TException e) {
      throw new RuntimeException(e);
    }
  }
}

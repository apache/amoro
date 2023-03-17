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

import com.google.common.collect.Maps;
import com.netease.arctic.spark.ArcticSparkCatalog;
import com.netease.arctic.spark.ArcticSparkSessionCatalog;

import java.util.Map;

public abstract class SparkCatalogTestBase extends SparkTestBase {

  /**
   * @return session, arctic, hive
   */
  abstract protected String catalogType();

  protected String catalog() {
    String catalogType = catalogType();
    if ("SESSION".equalsIgnoreCase(catalogType)) {
      return "spark_catalog";
    } else {
      return env.amsCatalogName(catalogType);
    }
  }

  protected String arcticCatalog() {
    String catalogType = catalogType();
    if ("SESSION".equalsIgnoreCase(catalogType)) {
      return env.amsCatalogName("hive");
    } else {
      return env.amsCatalogName(catalogType);
    }
  }

  protected String catalogUrl() {
    return env.amsServerUrl() + "/" + arcticCatalog();
  }

  protected Map<String, String> testSparkConf() {
    Map<String, String> conf = Maps.newHashMap();
    if ("Session".equalsIgnoreCase(catalogType())) {
      conf.put("spark.sql.catalog." + catalog(), ArcticSparkSessionCatalog.class.getName());
    } else {
      conf.put("spark.sql.catalog." + catalog(), ArcticSparkCatalog.class.getName());
    }
    conf.put("spark.sql.catalog." + catalog() + ".url", catalogUrl());
    return conf;
  }
}

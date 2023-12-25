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

package com.netease.arctic.server.terminal;

import com.netease.arctic.server.catalog.CatalogType;
import com.netease.arctic.server.utils.Configurations;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class SparkContextUtil {

  public static final String ICEBERG_EXTENSION =
      "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions";
  public static final String MIXED_FORMAT_EXTENSION =
      "com.netease.arctic.spark.ArcticSparkExtensions";
  public static final String ICEBERG_CATALOG = "org.apache.iceberg.spark.SparkCatalog";
  public static final String MIXED_FORMAT_CATALOG = "com.netease.arctic.spark.ArcticSparkCatalog";
  public static final String PAIMON_CATALOG = "org.apache.paimon.spark.SparkCatalog";
  public static final String MIXED_FORMAT_SESSION_CATALOG =
      "com.netease.arctic.spark.ArcticSparkSessionCatalog";
  public static final String MIXED_FORMAT_PROPERTY_REFRESH_BEFORE_USAGE =
      "spark.sql.arctic.refresh-catalog-before-usage";

  public static Map<String, String> getSparkConf(Configurations sessionConfig) {
    Map<String, String> sparkConf = Maps.newLinkedHashMap();
    sparkConf.put("spark.sql.extensions", MIXED_FORMAT_EXTENSION + "," + ICEBERG_EXTENSION);

    List<String> catalogs = sessionConfig.get(TerminalSessionFactory.SessionConfigOptions.CATALOGS);
    String catalogUrlBase =
        sessionConfig.get(TerminalSessionFactory.SessionConfigOptions.CATALOG_URL_BASE);

    for (String catalog : catalogs) {
      String connector =
          sessionConfig.get(TerminalSessionFactory.SessionConfigOptions.catalogConnector(catalog));
      String catalogClassName;
      String sparkCatalogPrefix = "spark.sql.catalog." + catalog;
      if ("arctic".equalsIgnoreCase(connector)) {
        catalogClassName = MIXED_FORMAT_CATALOG;
        String type =
            sessionConfig.get(
                TerminalSessionFactory.SessionConfigOptions.catalogProperty(catalog, "type"));
        if (sessionConfig.getBoolean(
                TerminalSessionFactory.SessionConfigOptions.USING_SESSION_CATALOG_FOR_HIVE)
            && CatalogType.HIVE.name().equalsIgnoreCase(type)) {
          sparkCatalogPrefix = "spark.sql.catalog.spark_catalog";
          catalogClassName = MIXED_FORMAT_SESSION_CATALOG;
        }
        sparkConf.put(sparkCatalogPrefix + ".url", catalogUrlBase + "/" + catalog);
      } else {
        catalogClassName = "iceberg".equalsIgnoreCase(connector) ? ICEBERG_CATALOG : PAIMON_CATALOG;
        Map<String, String> properties =
            TerminalSessionFactory.SessionConfigOptions.getCatalogProperties(
                sessionConfig, catalog);
        for (String key : properties.keySet()) {
          String property = properties.get(key);
          sparkConf.put(sparkCatalogPrefix + "." + key, property);
        }
      }
      sparkConf.put(sparkCatalogPrefix, catalogClassName);
    }
    return sparkConf;
  }
}

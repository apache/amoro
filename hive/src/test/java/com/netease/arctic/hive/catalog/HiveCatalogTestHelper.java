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

package com.netease.arctic.hive.catalog;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.CatalogOperations;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelpers;
import com.netease.arctic.catalog.ExternalCatalogOperations;
import com.netease.arctic.catalog.MixedTables;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE_HIVE;

public class HiveCatalogTestHelper implements CatalogTestHelper {

  private final TableFormat[] tableFormats;
  private final Configuration hiveConf;


  public static HiveCatalogTestHelper mixedHiveCatalog(Configuration hiveConf) {
    return new HiveCatalogTestHelper(hiveConf, TableFormat.MIXED_HIVE);
  }

  public static HiveCatalogTestHelper icebergHiveCatalog(Configuration hiveConf) {
    return new HiveCatalogTestHelper(hiveConf, TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG);
  }

  @Deprecated
  public HiveCatalogTestHelper(TableFormat tableFormat, Configuration hiveConf) {
    this(hiveConf, tableFormat);
    Preconditions.checkArgument(TableFormat.MIXED_HIVE == tableFormat);
  }

  public HiveCatalogTestHelper(Configuration hiveConf, TableFormat... supportedFormats) {
    this.tableFormats = supportedFormats;
    this.hiveConf = hiveConf;
  }

  @Override
  public String catalogType() {
    return "hive";
  }


  @Override
  public CatalogMeta buildCatalogMeta(String baseDir) {
    Map<String, String> properties = Maps.newHashMap();
    return CatalogTestHelpers.buildHiveCatalogMeta(TEST_CATALOG_NAME,
        properties, hiveConf, tableFormats);
  }

  @Override
  public Catalog buildIcebergCatalog(CatalogMeta catalogMeta) {
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(ICEBERG_CATALOG_TYPE, ICEBERG_CATALOG_TYPE_HIVE);
    return org.apache.iceberg.CatalogUtil.buildIcebergCatalog(TEST_CATALOG_NAME,
        catalogProperties, hiveConf);
  }

  @Override
  public MixedTables buildMixedTables(CatalogMeta catalogMeta) {
    return new MixedHiveTables(catalogMeta);
  }

  @Override
  public CatalogOperations buildCatalogOperations(CatalogMeta catalogMeta) {
    return new ExternalCatalogOperations(catalogMeta);
  }

  @Override
  public boolean supportCatalogOperations() {
    return Arrays.stream(this.tableFormats)
        .noneMatch(t -> t == TableFormat.MIXED_HIVE);
  }

  @Override
  public String toString() {
    return catalogType();
  }
}

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

package com.netease.arctic.catalog;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import java.util.Map;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_AMS;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE_HADOOP;

public class BasicCatalogTestHelper implements CatalogTestHelper {

  private final String metastoreType;
  private final TableFormat tableFormat;
  private final Map<String, String> catalogProperties;

  public BasicCatalogTestHelper(TableFormat tableFormat) {
    this(tableFormat, Maps.newHashMap());
  }

  public BasicCatalogTestHelper(TableFormat tableFormat, Map<String, String> catalogProperties) {
    this(tableFormat == TableFormat.MIXED_ICEBERG ? CATALOG_TYPE_AMS : CATALOG_TYPE_HADOOP,
        catalogProperties, tableFormat);
  }

  public BasicCatalogTestHelper(
      String metastoreType, Map<String, String> catalogProperties, TableFormat... supportedFormats) {
    Preconditions.checkArgument(supportedFormats.length == 1, "Only support one table format");
    this.tableFormat = supportedFormats[0];
    this.catalogProperties = catalogProperties == null ? Maps.newHashMap() : Maps.newHashMap(catalogProperties);
    this.metastoreType = metastoreType;
  }

  @Override
  public TableFormat tableFormat() {
    return tableFormat;
  }

  @Override
  public CatalogMeta buildCatalogMeta(String baseDir) {
    Map<String, String> properties = Maps.newHashMap(catalogProperties);
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, baseDir);
    return CatalogTestHelpers.buildCatalogMeta(TEST_CATALOG_NAME, metastoreType(),
        properties, tableFormat());
  }

  @Override
  public String metastoreType() {
    return metastoreType;
  }

  @Override
  public Catalog buildIcebergCatalog(CatalogMeta catalogMeta) {
    if (!TableFormat.ICEBERG.equals(tableFormat)) {
      throw new UnsupportedOperationException("Cannot build iceberg catalog for table format:" + tableFormat);
    }
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(ICEBERG_CATALOG_TYPE, ICEBERG_CATALOG_TYPE_HADOOP);
    return org.apache.iceberg.CatalogUtil.buildIcebergCatalog(TEST_CATALOG_NAME,
        catalogProperties, new Configuration());
  }

  @Override
  public MixedTables buildMixedTables(CatalogMeta catalogMeta) {
    if (!TableFormat.MIXED_ICEBERG.equals(tableFormat)) {
      throw new UnsupportedOperationException("Cannot build mixed-tables for table format:" + tableFormat);
    }
    return new MixedTables(catalogMeta);
  }

  @Override
  public String toString() {
    return tableFormat.toString();
  }
}

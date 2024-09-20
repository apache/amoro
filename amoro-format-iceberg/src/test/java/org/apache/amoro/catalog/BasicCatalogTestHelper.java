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

package org.apache.amoro.catalog;

import org.apache.amoro.CommonUnifiedCatalog;
import org.apache.amoro.TableFormat;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.catalog.Catalog;

import java.util.Map;

public class BasicCatalogTestHelper implements CatalogTestHelper {

  private final String metastoreType;
  private final TableFormat tableFormat;
  private final Map<String, String> catalogProperties;

  public BasicCatalogTestHelper(TableFormat tableFormat) {
    this(tableFormat, Maps.newHashMap());
  }

  public BasicCatalogTestHelper(TableFormat tableFormat, Map<String, String> catalogProperties) {
    this(CatalogMetaProperties.CATALOG_TYPE_HADOOP, catalogProperties, tableFormat);
  }

  public BasicCatalogTestHelper(
      String metastoreType,
      Map<String, String> catalogProperties,
      TableFormat... supportedFormats) {
    Preconditions.checkArgument(supportedFormats.length == 1, "Only support one table format");
    this.tableFormat = supportedFormats[0];
    this.catalogProperties =
        catalogProperties == null ? Maps.newHashMap() : Maps.newHashMap(catalogProperties);
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
    return CatalogTestHelpers.buildCatalogMeta(
        TEST_CATALOG_NAME, metastoreType(), properties, tableFormat());
  }

  @Override
  public String metastoreType() {
    return metastoreType;
  }

  @Override
  public UnifiedCatalog buildUnifiedCatalog(CatalogMeta catalogMeta) {
    return new CommonUnifiedCatalog(() -> catalogMeta, Maps.newHashMap());
  }

  @Override
  public Catalog buildIcebergCatalog(CatalogMeta catalogMeta) {
    if (!TableFormat.ICEBERG.equals(tableFormat)) {
      throw new UnsupportedOperationException(
          "Cannot build iceberg catalog for table format:" + tableFormat);
    }
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(
        CatalogUtil.ICEBERG_CATALOG_TYPE, CatalogUtil.ICEBERG_CATALOG_TYPE_HADOOP);
    return CatalogUtil.buildIcebergCatalog(
        TEST_CATALOG_NAME, catalogProperties, new Configuration());
  }

  @Override
  public MixedTables buildMixedTables(CatalogMeta catalogMeta) {
    if (!TableFormat.MIXED_ICEBERG.equals(tableFormat)) {
      throw new UnsupportedOperationException(
          "Cannot build mixed-tables for table format:" + tableFormat);
    }
    return new MixedTables(
        catalogMeta.getCatalogProperties(),
        org.apache.amoro.utils.CatalogUtil.buildMetaStore(catalogMeta));
  }

  @Override
  public String toString() {
    return tableFormat.toString();
  }
}

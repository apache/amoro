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

import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE_HADOOP;

public class BasicCatalogTestHelper implements CatalogTestHelper {

  private final String catalogType;
  private final Map<String, String> catalogProperties;

  public static BasicCatalogTestHelper internalCatalog() {
    return new BasicCatalogTestHelper(CatalogMetaProperties.CATALOG_TYPE_AMS);
  }

  public static BasicCatalogTestHelper externalCatalog() {
    return new BasicCatalogTestHelper(CatalogMetaProperties.CATALOG_TYPE_HADOOP);
  }

  private static String formatToType(TableFormat format) {
    switch (format) {
      case MIXED_ICEBERG:
        return CatalogMetaProperties.CATALOG_TYPE_AMS;
      case ICEBERG:
        return CatalogMetaProperties.CATALOG_TYPE_HADOOP;
      default:
        throw new IllegalArgumentException("Unsupported format:" + format);
    }
  }

  /**
   * In order to be compatible with the old construction method.
   * @param format - mixed-iceberg or iceberg
   */
  @Deprecated
  public BasicCatalogTestHelper(TableFormat format) {
    this(formatToType(format), Maps.newHashMap());
  }

  public BasicCatalogTestHelper(String catalogType) {
    this(catalogType, Maps.newHashMap());
  }

  public BasicCatalogTestHelper(String catalogType, Map<String, String> catalogProperties) {
    this.catalogType = catalogType;
    this.catalogProperties = catalogProperties;
  }


  @Override
  public CatalogMeta buildCatalogMeta(String baseDir) {
    Map<String, String> properties = Maps.newHashMap(catalogProperties);
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, baseDir);
    return CatalogTestHelpers.buildCatalogMeta(TEST_CATALOG_NAME, catalogType,
        properties, getSupportedFormats());
  }

  @Override
  public String catalogType() {
    return catalogType;
  }

  protected TableFormat[] getSupportedFormats() {
    if (isInternalCatalog()) {
      return new TableFormat[]{TableFormat.MIXED_ICEBERG};
    }
    return new TableFormat[]{TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG};
  }

  @Override
  public Catalog buildIcebergCatalog(CatalogMeta catalogMeta) {
    if (isInternalCatalog()) {
      throw new UnsupportedOperationException("Cannot build iceberg catalog for catalog(type=" + catalogType + ")");
    }
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(ICEBERG_CATALOG_TYPE, ICEBERG_CATALOG_TYPE_HADOOP);
    return org.apache.iceberg.CatalogUtil.buildIcebergCatalog(TEST_CATALOG_NAME,
        catalogProperties, new Configuration());
  }

  @Override
  public MixedTables buildMixedTables(CatalogMeta catalogMeta) {
    if (!isInternalCatalog()) {
      throw new UnsupportedOperationException("Cannot build mixed-tables for catalog(type=" + catalogType + ")");
    }
    return new MixedTables(catalogMeta);
  }

  @Override
  public String toString() {
    return catalogType;
  }
}

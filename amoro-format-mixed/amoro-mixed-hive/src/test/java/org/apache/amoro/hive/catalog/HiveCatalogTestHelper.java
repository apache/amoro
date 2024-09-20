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

package org.apache.amoro.hive.catalog;

import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE_HIVE;

import org.apache.amoro.CommonUnifiedCatalog;
import org.apache.amoro.TableFormat;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.catalog.MixedTables;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.Catalog;

import java.util.Map;

public class HiveCatalogTestHelper implements CatalogTestHelper {

  private final TableFormat tableFormat;
  private final Configuration hiveConf;

  public static CatalogTestHelper build(Configuration hiveConf, TableFormat... formats) {
    return new HiveCatalogTestHelper(formats[0], hiveConf);
  }

  public HiveCatalogTestHelper(TableFormat tableFormat, Configuration hiveConf) {
    this.tableFormat = tableFormat;
    this.hiveConf = hiveConf;
  }

  @Override
  public String metastoreType() {
    return CatalogMetaProperties.CATALOG_TYPE_HIVE;
  }

  @Override
  public TableFormat tableFormat() {
    return tableFormat;
  }

  @Override
  public CatalogMeta buildCatalogMeta(String baseDir) {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(CatalogProperties.URI, hiveConf.get(HiveConf.ConfVars.METASTOREURIS.varname));
    properties.put(CatalogProperties.WAREHOUSE_LOCATION, baseDir);
    return CatalogTestHelpers.buildHiveCatalogMeta(
        TEST_CATALOG_NAME, properties, hiveConf, tableFormat);
  }

  @Override
  public UnifiedCatalog buildUnifiedCatalog(CatalogMeta catalogMeta) {
    catalogMeta
        .getCatalogProperties()
        .put(CatalogMetaProperties.TABLE_FORMATS, TableFormat.MIXED_HIVE.name());
    return new CommonUnifiedCatalog(() -> catalogMeta, Maps.newHashMap());
  }

  @Override
  public Catalog buildIcebergCatalog(CatalogMeta catalogMeta) {
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(ICEBERG_CATALOG_TYPE, ICEBERG_CATALOG_TYPE_HIVE);
    return org.apache.iceberg.CatalogUtil.buildIcebergCatalog(
        TEST_CATALOG_NAME, catalogProperties, hiveConf);
  }

  @Override
  public MixedTables buildMixedTables(CatalogMeta catalogMeta) {
    if (!TableFormat.MIXED_HIVE.equals(tableFormat)) {
      throw new UnsupportedOperationException(
          "Cannot build mixed-tables for table format:" + tableFormat);
    }
    return new MixedTables(
        catalogMeta.getCatalogProperties(), CatalogUtil.buildMetaStore(catalogMeta));
  }

  @Override
  public String toString() {
    return tableFormat.toString();
  }
}

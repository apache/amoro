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

package com.netease.arctic.formats.mixed;

import com.netease.arctic.FormatCatalog;
import com.netease.arctic.FormatCatalogFactory;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.TableMetaStore;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Map;

public class MixedIcebergCatalogFactory implements FormatCatalogFactory {
  @Override
  public FormatCatalog create(
      String catalogName,
      String metastoreType,
      Map<String, String> properties,
      TableMetaStore metaStore) {
    ArcticCatalog catalog = createMixedCatalog(catalogName, metastoreType, properties, metaStore);
    return new MixedCatalog(catalog, format());
  }

  @VisibleForTesting
  public static ArcticCatalog createMixedCatalog(
      String catalogName,
      String metastoreType,
      Map<String, String> properties,
      TableMetaStore metaStore) {
    String catalogImpl = CatalogLoader.catalogImpl(metastoreType, properties);
    ArcticCatalog catalog = CatalogLoader.buildCatalog(catalogImpl);

    Map<String, String> initializeProperties = Maps.newHashMap(properties);
    initializeProperties.put(org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE, metastoreType);
    if (CatalogMetaProperties.CATALOG_TYPE_GLUE.equals(metastoreType)) {
      initializeProperties.put(CatalogProperties.CATALOG_IMPL, CatalogLoader.GLUE_CATALOG_IMPL);
    }
    if (initializeProperties.containsKey(CatalogProperties.CATALOG_IMPL)) {
      initializeProperties.remove(org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE);
    }

    catalog.initialize(catalogName, initializeProperties, metaStore);
    return catalog;
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }
}

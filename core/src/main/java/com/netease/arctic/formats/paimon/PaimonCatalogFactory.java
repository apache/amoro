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

package com.netease.arctic.formats.paimon;

import com.netease.arctic.FormatCatalogFactory;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.catalog.CatalogFactory;
import org.apache.paimon.catalog.FileSystemCatalogFactory;
import org.apache.paimon.hive.HiveCatalogOptions;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.options.Options;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public class PaimonCatalogFactory implements FormatCatalogFactory {
  @Override
  public PaimonCatalog create(
      String name, String metastoreType, Map<String, String> properties, TableMetaStore metaStore) {
    Optional<URL> hiveSiteLocation = metaStore.getHiveSiteLocation();
    Map<String, String> catalogProperties = Maps.newHashMap();
    catalogProperties.putAll(properties);

    hiveSiteLocation.ifPresent(
        url ->
            catalogProperties.put(
                HiveCatalogOptions.HIVE_CONF_DIR.key(), new File(url.getPath()).getParent()));
    Catalog catalog = paimonCatalog(catalogProperties, metaStore.getConfiguration());
    return new PaimonCatalog(catalog, name);
  }

  public static Catalog paimonCatalog(Map<String, String> properties, Configuration configuration) {
    Options options = Options.fromMap(properties);
    CatalogContext catalogContext = CatalogContext.create(options, configuration);
    return CatalogFactory.createCatalog(catalogContext);
  }

  @Override
  public TableFormat format() {
    return TableFormat.PAIMON;
  }

  @Override
  public Map<String, String> convertCatalogProperties(
      String catalogName, String metastoreType, Map<String, String> unifiedCatalogProperties) {
    Options options = Options.fromMap(unifiedCatalogProperties);
    String type;
    if (CatalogMetaProperties.CATALOG_TYPE_HADOOP.equalsIgnoreCase(metastoreType)) {
      type = FileSystemCatalogFactory.IDENTIFIER;
    } else {
      type = metastoreType;
    }
    options.set(CatalogOptions.METASTORE, type);
    return options.toMap();
  }
}

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

package org.apache.amoro.utils;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.op.MixedHadoopTableOperations;
import org.apache.amoro.op.MixedTableOperations;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.CachingCatalog;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.Table;
import org.apache.iceberg.aws.glue.GlueCatalog;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.hadoop.HadoopTableOperations;
import org.apache.iceberg.rest.RESTCatalog;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class MixedFormatCatalogUtil {

  private static final Logger LOG = LoggerFactory.getLogger(MixedFormatCatalogUtil.class);

  /** Merge catalog properties in client side into catalog meta. */
  public static void mergeCatalogProperties(CatalogMeta meta, Map<String, String> properties) {
    if (meta.getCatalogProperties() == null) {
      meta.setCatalogProperties(Maps.newHashMap());
    }
    if (properties != null) {
      properties.forEach(meta::putToCatalogProperties);
    }
  }

  /**
   * add initialize properties for iceberg catalog
   *
   * @param catalogName - catalog name
   * @param metastoreType - metastore type
   * @param properties - catalog properties
   * @return catalog properties with initialize properties.
   */
  public static Map<String, String> withIcebergCatalogInitializeProperties(
      String catalogName, String metastoreType, Map<String, String> properties) {
    Map<String, String> icebergCatalogProperties = Maps.newHashMap(properties);
    icebergCatalogProperties.put(
        org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE, metastoreType);
    if (CatalogMetaProperties.CATALOG_TYPE_GLUE.equals(metastoreType)) {
      icebergCatalogProperties.put(CatalogProperties.CATALOG_IMPL, GlueCatalog.class.getName());
    }
    if (CatalogMetaProperties.CATALOG_TYPE_AMS.equalsIgnoreCase(metastoreType)) {
      icebergCatalogProperties.put(CatalogProperties.WAREHOUSE_LOCATION, catalogName);
      if (!icebergCatalogProperties.containsKey(CatalogProperties.CATALOG_IMPL)) {
        icebergCatalogProperties.put(CatalogProperties.CATALOG_IMPL, RESTCatalog.class.getName());
      }
    }

    if (CatalogMetaProperties.CATALOG_TYPE_CUSTOM.equalsIgnoreCase(metastoreType)) {
      Preconditions.checkArgument(
          icebergCatalogProperties.containsKey(CatalogProperties.CATALOG_IMPL),
          "Custom catalog properties must contains " + CatalogProperties.CATALOG_IMPL);
    }

    if (icebergCatalogProperties.containsKey(CatalogProperties.CATALOG_IMPL)) {
      icebergCatalogProperties.remove(org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE);
    }

    return icebergCatalogProperties;
  }

  /** Wrap table operation with authorization logic for {@link Table}. */
  public static Table useMixedTableOperations(
      Table table,
      String tableLocation,
      AuthenticatedFileIO authenticatedFileIO,
      Configuration configuration) {
    if (table instanceof org.apache.iceberg.BaseTable) {
      org.apache.iceberg.BaseTable baseTable = (org.apache.iceberg.BaseTable) table;
      if (baseTable.operations() instanceof MixedHadoopTableOperations) {
        return table;
      } else if (baseTable.operations() instanceof MixedTableOperations) {
        return table;
      } else if (baseTable.operations() instanceof HadoopTableOperations) {
        return new org.apache.iceberg.BaseTable(
            new MixedHadoopTableOperations(
                new Path(tableLocation), authenticatedFileIO, configuration),
            table.name());
      } else {
        return new org.apache.iceberg.BaseTable(
            new MixedTableOperations(((BaseTable) table).operations(), authenticatedFileIO),
            table.name());
      }
    }
    return table;
  }

  /**
   * merge properties of table level in catalog properties to table(properties key start with
   * table.)
   *
   * @param tableProperties properties in table
   * @param catalogProperties properties in catalog
   * @return merged table properties
   */
  public static Map<String, String> mergeCatalogPropertiesToTable(
      Map<String, String> tableProperties, Map<String, String> catalogProperties) {
    Map<String, String> mergedProperties =
        catalogProperties.entrySet().stream()
            .filter(e -> e.getKey().startsWith(CatalogMetaProperties.TABLE_PROPERTIES_PREFIX))
            .collect(
                Collectors.toMap(
                    e ->
                        e.getKey()
                            .substring(CatalogMetaProperties.TABLE_PROPERTIES_PREFIX.length()),
                    Map.Entry::getValue));

    if (!PropertyUtil.propertyAsBoolean(
        tableProperties,
        TableProperties.ENABLE_LOG_STORE,
        TableProperties.ENABLE_LOG_STORE_DEFAULT)) {
      mergedProperties =
          mergedProperties.entrySet().stream()
              .filter(
                  e -> !e.getKey().startsWith(CatalogMetaProperties.LOG_STORE_PROPERTIES_PREFIX))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    String optimizationEnabled =
        tableProperties.getOrDefault(
            TableProperties.ENABLE_SELF_OPTIMIZING,
            mergedProperties.getOrDefault(
                TableProperties.ENABLE_SELF_OPTIMIZING,
                String.valueOf(TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT)));
    if (!Boolean.parseBoolean(optimizationEnabled)) {
      mergedProperties =
          mergedProperties.entrySet().stream()
              .filter(e -> !e.getKey().startsWith(CatalogMetaProperties.OPTIMIZE_PROPERTIES_PREFIX))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      // maintain 'optimize.enable' flag as false in table properties
      mergedProperties.put(TableProperties.ENABLE_SELF_OPTIMIZING, optimizationEnabled);
    }
    mergedProperties.putAll(tableProperties);

    return mergedProperties;
  }

  /**
   * Build cache catalog.
   *
   * @param catalog The catalog of the wrap that needs to be cached
   * @param properties table properties
   * @return If Cache is enabled, CachingCatalog is returned, otherwise, the input catalog is
   *     returned
   */
  public static Catalog buildCacheCatalog(Catalog catalog, Map<String, String> properties) {
    boolean cacheEnabled =
        PropertyUtil.propertyAsBoolean(
            properties, CatalogProperties.CACHE_ENABLED, CatalogProperties.CACHE_ENABLED_DEFAULT);

    boolean cacheCaseSensitive =
        PropertyUtil.propertyAsBoolean(
            properties,
            CatalogProperties.CACHE_CASE_SENSITIVE,
            CatalogProperties.CACHE_CASE_SENSITIVE_DEFAULT);

    long cacheExpirationIntervalMs =
        PropertyUtil.propertyAsLong(
            properties,
            CatalogProperties.CACHE_EXPIRATION_INTERVAL_MS,
            CatalogProperties.CACHE_EXPIRATION_INTERVAL_MS_DEFAULT);

    // An expiration interval of 0ms effectively disables caching.
    // Do not wrap with CachingCatalog.
    if (cacheExpirationIntervalMs <= 0) {
      LOG.warn(
          "Configuration `{}` is {}, less than or equal to 0, then the cache will not take effect.",
          CatalogProperties.CACHE_EXPIRATION_INTERVAL_MS,
          cacheExpirationIntervalMs);
      cacheEnabled = false;
    }
    return cacheEnabled
        ? CachingCatalog.wrap(catalog, cacheCaseSensitive, cacheExpirationIntervalMs)
        : catalog;
  }
}

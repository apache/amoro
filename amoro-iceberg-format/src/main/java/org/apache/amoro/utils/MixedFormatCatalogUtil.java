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

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.op.MixedHadoopTableOperations;
import org.apache.amoro.op.MixedTableOperations;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.commons.lang3.StringUtils;
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

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MixedFormatCatalogUtil {

  private static final Logger LOG = LoggerFactory.getLogger(MixedFormatCatalogUtil.class);

  /** Return table format set catalog supported. */
  public static Set<TableFormat> tableFormats(CatalogMeta meta) {
    return tableFormats(meta.getCatalogType(), meta.getCatalogProperties());
  }

  /** Return table format set catalog supported. */
  public static Set<TableFormat> tableFormats(
      String metastoreType, Map<String, String> catalogProperties) {
    if (catalogProperties != null
        && catalogProperties.containsKey(CatalogMetaProperties.TABLE_FORMATS)) {
      String tableFormatsProperty = catalogProperties.get(CatalogMetaProperties.TABLE_FORMATS);
      return Arrays.stream(tableFormatsProperty.split(","))
          .map(
              tableFormatString ->
                  TableFormat.valueOf(tableFormatString.trim().toUpperCase(Locale.ROOT)))
          .collect(Collectors.toSet());
    } else {
      // Generate table format from catalog type for compatibility with older versions
      switch (metastoreType) {
        case CatalogMetaProperties.CATALOG_TYPE_AMS:
          return Sets.newHashSet(TableFormat.MIXED_ICEBERG);
        case CatalogMetaProperties.CATALOG_TYPE_CUSTOM:
        case CatalogMetaProperties.CATALOG_TYPE_HADOOP:
        case CatalogMetaProperties.CATALOG_TYPE_GLUE:
          return Sets.newHashSet(TableFormat.ICEBERG);
        case CatalogMetaProperties.CATALOG_TYPE_HIVE:
          return Sets.newHashSet(TableFormat.MIXED_HIVE);
        default:
          throw new IllegalArgumentException("Unsupported catalog type:" + metastoreType);
      }
    }
  }

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

  /** Build {@link TableMetaStore} from catalog meta. */
  public static TableMetaStore buildMetaStore(CatalogMeta catalogMeta) {
    // load storage configs
    TableMetaStore.Builder builder = TableMetaStore.builder();
    if (catalogMeta.getStorageConfigs() != null) {
      Map<String, String> storageConfigs = catalogMeta.getStorageConfigs();
      if (CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HADOOP.equalsIgnoreCase(
          MixedFormatCatalogUtil.getCompatibleStorageType(storageConfigs))) {
        String coreSite = storageConfigs.get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE);
        String hdfsSite = storageConfigs.get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE);
        String hiveSite = storageConfigs.get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE);
        builder
            .withBase64CoreSite(coreSite)
            .withBase64MetaStoreSite(hiveSite)
            .withBase64HdfsSite(hdfsSite);
      }
    }

    boolean loadAuthFromAMS =
        PropertyUtil.propertyAsBoolean(
            catalogMeta.getCatalogProperties(),
            CatalogMetaProperties.LOAD_AUTH_FROM_AMS,
            CatalogMetaProperties.LOAD_AUTH_FROM_AMS_DEFAULT);
    // load auth configs from ams
    if (loadAuthFromAMS) {
      if (catalogMeta.getAuthConfigs() != null) {
        Map<String, String> authConfigs = catalogMeta.getAuthConfigs();
        String authType = authConfigs.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE);
        LOG.info("TableMetaStore use auth config in catalog meta, authType is {}", authType);
        if (CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE.equalsIgnoreCase(authType)) {
          String hadoopUsername =
              authConfigs.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME);
          builder.withSimpleAuth(hadoopUsername);
        } else if (CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_KERBEROS.equalsIgnoreCase(
            authType)) {
          String krb5 = authConfigs.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5);
          String keytab = authConfigs.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB);
          String principal = authConfigs.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL);
          builder.withBase64KrbAuth(keytab, krb5, principal);
        } else if (CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_AK_SK.equalsIgnoreCase(authType)) {
          String accessKey = authConfigs.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_ACCESS_KEY);
          String secretKey = authConfigs.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_SECRET_KEY);
          builder.withAkSkAuth(accessKey, secretKey);
        }
      }
    }

    // cover auth configs from ams with auth configs in properties
    String authType =
        catalogMeta.getCatalogProperties().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE);
    if (StringUtils.isNotEmpty(authType)) {
      LOG.info("TableMetaStore use auth config in properties, authType is {}", authType);
      if (CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE.equalsIgnoreCase(authType)) {
        String hadoopUsername =
            catalogMeta
                .getCatalogProperties()
                .get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME);
        builder.withSimpleAuth(hadoopUsername);
      } else if (CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_KERBEROS.equalsIgnoreCase(
          authType)) {
        String krb5 =
            catalogMeta.getCatalogProperties().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5);
        String keytab =
            catalogMeta.getCatalogProperties().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB);
        String principal =
            catalogMeta
                .getCatalogProperties()
                .get(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL);
        builder.withBase64KrbAuth(keytab, krb5, principal);
      } else if (CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_AK_SK.equalsIgnoreCase(authType)) {
        String accessKey =
            catalogMeta
                .getCatalogProperties()
                .get(CatalogMetaProperties.AUTH_CONFIGS_KEY_ACCESS_KEY);
        String secretKey =
            catalogMeta
                .getCatalogProperties()
                .get(CatalogMetaProperties.AUTH_CONFIGS_KEY_SECRET_KEY);
        builder.withAkSkAuth(accessKey, secretKey);
      }
    }
    return builder.build();
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

  public static TableIdentifier tableId(TableMeta tableMeta) {
    return TableIdentifier.of(
        tableMeta.getTableIdentifier().getCatalog(),
        tableMeta.getTableIdentifier().getDatabase(),
        tableMeta.getTableIdentifier().getTableName());
  }

  public static org.apache.amoro.api.TableIdentifier amsTaleId(TableIdentifier tableIdentifier) {
    return new org.apache.amoro.api.TableIdentifier(
        tableIdentifier.getCatalog(),
        tableIdentifier.getDatabase(),
        tableIdentifier.getTableName());
  }

  /**
   * Get storage type compatible with history storage type `hdfs`, which is `Hadoop` now.
   *
   * @param conf - configurations containing `storage.type`
   * @return storage type, return `Hadoop` if `storage.type` is `hdfs`, return null if
   *     `storage.type` not exist.
   */
  public static String getCompatibleStorageType(Map<String, String> conf) {
    if (CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HDFS_LEGACY.equals(
        conf.get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE))) {
      return CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HADOOP;
    }
    return conf.get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE);
  }

  /**
   * Copy property from source properties to target properties, support changing the key name.
   *
   * @param fromProperties - from these properties
   * @param toProperties - to these properties
   * @param fromKey - from key
   * @param toKey - to key
   */
  public static <T> void copyProperty(
      Map<String, String> fromProperties,
      Map<String, T> toProperties,
      String fromKey,
      String toKey) {
    if (StringUtils.isNotEmpty(fromProperties.get(fromKey))) {
      toProperties.put(toKey, (T) fromProperties.get(fromKey));
    }
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

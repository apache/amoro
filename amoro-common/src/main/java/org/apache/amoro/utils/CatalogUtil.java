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
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CatalogUtil {

  private static final Logger LOG = LoggerFactory.getLogger(CatalogUtil.class);

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
    mergedProperties.putAll(tableProperties);
    return mergedProperties;
  }

  /** Build {@link TableMetaStore} from catalog meta. */
  public static TableMetaStore buildMetaStore(CatalogMeta catalogMeta) {
    // load storage configs
    TableMetaStore.Builder builder = TableMetaStore.builder();
    if (catalogMeta.getStorageConfigs() != null) {
      Map<String, String> storageConfigs = catalogMeta.getStorageConfigs();
      if (CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HADOOP.equalsIgnoreCase(
          CatalogUtil.getCompatibleStorageType(storageConfigs))) {
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
        propertyAsBoolean(
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

  public static TableIdentifier tableId(TableMeta tableMeta) {
    return TableIdentifier.of(
        tableMeta.getTableIdentifier().getCatalog(),
        tableMeta.getTableIdentifier().getDatabase(),
        tableMeta.getTableIdentifier().getTableName());
  }

  public static org.apache.amoro.api.TableIdentifier amsTableId(TableIdentifier tableIdentifier) {
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

  private static boolean propertyAsBoolean(
      Map<String, String> properties, String property, boolean defaultValue) {
    String value = properties.get(property);
    if (value != null) {
      return Boolean.parseBoolean(value);
    }
    return defaultValue;
  }
}

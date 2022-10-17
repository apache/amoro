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

import com.netease.arctic.AmsClient;
import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.client.AmsClientPools;
import com.netease.arctic.ams.api.client.ArcticThriftUrl;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.table.TableMetaStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.common.DynConstructors;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HIVE;

/**
 * Catalogs, create catalog from arctic metastore thrift url.
 */
public class CatalogLoader {

  public static final String HADOOP_CATALOG_IMPL = BaseArcticCatalog.class.getName();
  public static final String HIVE_CATALOG_IMPL = "com.netease.arctic.hive.catalog.ArcticHiveCatalog";

  /**
   * Entrypoint for loading Catalog.
   *
   * @param catalogUrl arctic catalog url, thrift://arctic-ams-host:port/catalog_name
   * @param properties client side catalog configs
   * @return arctic catalog object
   */
  public static ArcticCatalog load(String catalogUrl, Map<String, String> properties) {
    ArcticThriftUrl url = ArcticThriftUrl.parse(catalogUrl);
    if (url.catalogName() == null || url.catalogName().contains("/")) {
      throw new IllegalArgumentException("invalid catalog name " + url.catalogName());
    }

    return loadCatalog(catalogUrl, url.catalogName(), properties);
  }

  /**
   * Entrypoint for loading catalog.
   *
   * @param catalogUrl arctic catalog url, thrift://arctic-ams-host:port/catalog_name
   * @return arctic catalog object
   */
  public static ArcticCatalog load(String catalogUrl) {
    return load(catalogUrl, Maps.newHashMap());
  }

  /**
   * Entrypoint for loading catalog.
   *
   * @param client arctic metastore client
   * @param catalogName arctic catalog name
   * @return arctic catalog object
   */
  public static ArcticCatalog load(AmsClient client, String catalogName) {
    return load(client, catalogName, Maps.newHashMap());
  }

  /**
   * Entrypoint for loading catalog
   *
   * @param client arctic metastore client
   * @param catalogName arctic catalog name
   * @param props client side catalog configs
   * @return arctic catalog object
   */
  public static ArcticCatalog load(AmsClient client, String catalogName, Map<String, String> props) {
    try {
      CatalogMeta catalogMeta = client.getCatalog(catalogName);
      // engine can set catalog configs with catalog properties
      setCatalogConfigsWithProperties(catalogMeta, props);

      String type = catalogMeta.getCatalogType();
      String catalogImpl;
      switch (type) {
        case CATALOG_TYPE_HADOOP:
          catalogImpl = HADOOP_CATALOG_IMPL;
          break;
        case CATALOG_TYPE_HIVE:
          catalogImpl = HIVE_CATALOG_IMPL;
          break;
        default:
          throw new IllegalStateException(
              "unsupported catalog type:" + type
          );
      }
      ArcticCatalog catalog = buildCatalog(catalogImpl);
      catalog.initialize(client, catalogMeta, props);
      return catalog;
    } catch (NoSuchObjectException e1) {
      throw new IllegalArgumentException("catalog not found, please check catalog name", e1);
    } catch (TException e) {
      throw new IllegalStateException("failed when load catalog " + catalogName, e);
    }
  }

  /**
   * Show catalog list in arctic metastore.
   *
   * @param metastoreUrl url of arctic metastore
   * @return catalog name list
   */
  public static List<String> catalogs(String metastoreUrl) {
    try {
      return ((ArcticTableMetastore.Iface) AmsClientPools.getClientPool(metastoreUrl).iface()).getCatalogs()
          .stream()
          .map(CatalogMeta::getCatalogName)
          .collect(Collectors.toList());
    } catch (TException e) {
      throw new IllegalStateException("failed when load catalogs", e);
    }
  }

  private static ArcticCatalog loadCatalog(
      String metaStoreUrl,
      String catalogName,
      Map<String, String> properties) {
    AmsClient client = new PooledAmsClient(metaStoreUrl);
    return load(client, catalogName, properties);
  }

  private static ArcticCatalog buildCatalog(String impl) {
    DynConstructors.Ctor<ArcticCatalog> ctor;
    try {
      ctor = DynConstructors.builder(ArcticCatalog.class).impl(impl).buildChecked();
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(String.format(
          "Cannot initialize Catalog implementation %s: %s", impl, e.getMessage()), e);
    }
    try {
      return ctor.newInstance();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          String.format("Cannot initialize Catalog, %s does not implement Catalog.", impl), e);
    }
  }

  private static void setCatalogConfigsWithProperties(CatalogMeta catalogMeta, Map<String, String> props) {
    Map<String, String> authConfigs = catalogMeta.getAuthConfigs() == null ?
        new HashMap<>() : catalogMeta.getAuthConfigs();
    Map<String, String> storageConfigs = catalogMeta.getStorageConfigs() == null ?
        new HashMap<>() : catalogMeta.getStorageConfigs();
    String authMethod = props.get(TableMetaStore.AUTH_METHOD);
    String coreSite = props.get(TableMetaStore.CORE_SITE);
    String hdfsSite = props.get(TableMetaStore.HDFS_SITE);
    String hiveSite = props.get(TableMetaStore.HIVE_SITE);
    String disableAuth = props.get(CatalogMetaProperties.AUTH_CONFIGS_DISABLE);

    if (StringUtils.isNotEmpty(coreSite)) {
      storageConfigs.put(TableMetaStore.CORE_SITE, coreSite);
    }
    if (StringUtils.isNotEmpty(hdfsSite)) {
      storageConfigs.put(TableMetaStore.HDFS_SITE, hdfsSite);
    }
    if (StringUtils.isNotEmpty(hiveSite)) {
      storageConfigs.put(TableMetaStore.HIVE_SITE, hiveSite);
    }
    catalogMeta.setStorageConfigs(storageConfigs);

    if (StringUtils.isNotEmpty(authMethod)) {
      authConfigs.put(TableMetaStore.AUTH_METHOD, authMethod);
      if (TableMetaStore.AUTH_METHOD_SIMPLE.equalsIgnoreCase(authMethod)) {
        String simpleUserName = props.get(TableMetaStore.SIMPLE_USER_NAME);
        authConfigs.put(TableMetaStore.SIMPLE_USER_NAME, simpleUserName);
      } else if (TableMetaStore.AUTH_METHOD_KERBEROS.equalsIgnoreCase(authMethod)) {
        String krbLoginUser = props.get(TableMetaStore.KEYTAB_LOGIN_USER);
        String keytabPath = new Path(props.get(TableMetaStore.KEYTAB)).toString();
        String krb5ConfPath = new Path(props.get(TableMetaStore.KRB5_CONF)).toString();
        authConfigs.put(TableMetaStore.KEYTAB_LOGIN_USER, krbLoginUser);
        authConfigs.put(TableMetaStore.KEYTAB, keytabPath);
        authConfigs.put(TableMetaStore.KRB5_CONF, krb5ConfPath);
      } else {
        throw new RuntimeException(String.format("this %s=%s is not supported", TableMetaStore.AUTH_METHOD,
            authMethod));
      }
    }

    if (StringUtils.isNotEmpty(disableAuth)) {
      if ("true".equalsIgnoreCase(disableAuth) || "false".equalsIgnoreCase(disableAuth)) {
        authConfigs.put(CatalogMetaProperties.AUTH_CONFIGS_DISABLE, disableAuth);
      } else {
        throw new RuntimeException(String.format("this %s=%s is not supported, only support true or false",
            CatalogMetaProperties.AUTH_CONFIGS_DISABLE, disableAuth));
      }
    }

    catalogMeta.setAuthConfigs(authConfigs);
  }
}

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
import org.apache.iceberg.util.PropertyUtil;
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
      // engine can set auth configs with catalog properties
      setAuthConfigsWithProperties(catalogMeta, props);

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
    } catch (Exception e) {
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

  private static void setAuthConfigsWithProperties(CatalogMeta catalogMeta, Map<String, String> props) {
    boolean disableAmsAuth = PropertyUtil.propertyAsBoolean(props,
        CatalogMetaProperties.AUTH_AMS_CONFIGS_DISABLE, CatalogMetaProperties.AUTH_AMS_CONFIGS_DISABLE_DEFAULT);

    if (disableAmsAuth) {
      Map<String, String> authConfigs;
      String authMethod = props.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE);
      if (StringUtils.isEmpty(authMethod)) {
        // if disable ams auth configs and no provide auth configs, use process auth configs
        authConfigs = catalogMeta.getAuthConfigs();
        authConfigs.put(CatalogMetaProperties.AUTH_CONFIGS_DISABLE, "true");
      } else {
        // replace catalog meta auth configs
        authConfigs = new HashMap<>();
        authConfigs.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE, authMethod);
        if (CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE.equalsIgnoreCase(authMethod)) {
          String simpleUserName = props.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME);
          authConfigs.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME, simpleUserName);
        } else if (CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_KERBEROS.equalsIgnoreCase(authMethod)) {
          String krbLoginUser = props.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL);
          String keytab = new Path(props.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB)).toString();
          String krb5Conf = new Path(props.get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5)).toString();
          authConfigs.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL, krbLoginUser);
          authConfigs.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB, keytab);
          authConfigs.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5, krb5Conf);
        } else {
          throw new RuntimeException(String.format("this %s=%s is not supported", TableMetaStore.AUTH_METHOD,
              authMethod));
        }
      }

      catalogMeta.setAuthConfigs(authConfigs);
    }
  }
}

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

package org.apache.amoro.mixed;

import org.apache.amoro.AmsClient;
import org.apache.amoro.Constants;
import org.apache.amoro.PooledAmsClient;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.AmoroTableMetastore;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.NoSuchObjectException;
import org.apache.amoro.client.AmsClientPools;
import org.apache.amoro.client.AmsThriftUrl;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.iceberg.common.DynConstructors;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Catalogs, create mixed-format catalog from metastore thrift url. */
public class CatalogLoader {

  public static final String INTERNAL_CATALOG_IMPL = InternalMixedIcebergCatalog.class.getName();
  public static final String HIVE_CATALOG_IMPL = "org.apache.amoro.hive.catalog.MixedHiveCatalog";
  public static final String MIXED_ICEBERG_CATALOG_IMP = BasicMixedIcebergCatalog.class.getName();

  /**
   * Entrypoint for loading Catalog.
   *
   * @param catalogUrl mixed-format catalog url, thrift://ams-host:port/catalog_name
   * @param properties client side catalog configs
   * @return mixed-format catalog object
   */
  public static MixedFormatCatalog load(String catalogUrl, Map<String, String> properties) {
    AmsThriftUrl url = AmsThriftUrl.parse(catalogUrl, Constants.THRIFT_TABLE_SERVICE_NAME);
    if (url.catalogName() == null || url.catalogName().contains("/")) {
      throw new IllegalArgumentException("invalid catalog name " + url.catalogName());
    }

    return loadCatalog(catalogUrl, url.catalogName(), properties);
  }

  /**
   * Entrypoint for loading catalog.
   *
   * @param catalogUrl mixed-format catalog url, thrift://ams-host:port/catalog_name
   * @return mixed-format catalog object
   */
  public static MixedFormatCatalog load(String catalogUrl) {
    return load(catalogUrl, Maps.newHashMap());
  }

  /**
   * get mixed-format catalog implement class name
   *
   * @param metastoreType - metastore type
   * @param catalogProperties - catalog properties
   * @return class name for catalog
   */
  private static String catalogImpl(String metastoreType, Map<String, String> catalogProperties) {
    Set<TableFormat> tableFormats =
        MixedFormatCatalogUtil.tableFormats(metastoreType, catalogProperties);
    Preconditions.checkArgument(
        tableFormats.size() == 1, "Catalog support only one table format now.");
    TableFormat tableFormat = tableFormats.iterator().next();
    Preconditions.checkArgument(
        TableFormat.MIXED_HIVE == tableFormat || TableFormat.MIXED_ICEBERG == tableFormat,
        "MixedCatalogLoader only support mixed-format, format: %s",
        tableFormat.name());

    String catalogImpl;
    switch (metastoreType) {
      case CatalogMetaProperties.CATALOG_TYPE_HADOOP:
      case CatalogMetaProperties.CATALOG_TYPE_GLUE:
      case CatalogMetaProperties.CATALOG_TYPE_CUSTOM:
        Preconditions.checkArgument(
            TableFormat.MIXED_ICEBERG == tableFormat,
            "%s catalog support mixed-iceberg table only.",
            metastoreType);
        catalogImpl = MIXED_ICEBERG_CATALOG_IMP;
        break;
      case CatalogMetaProperties.CATALOG_TYPE_HIVE:
        if (TableFormat.MIXED_HIVE == tableFormat) {
          catalogImpl = HIVE_CATALOG_IMPL;
        } else {
          catalogImpl = MIXED_ICEBERG_CATALOG_IMP;
        }
        break;
      case CatalogMetaProperties.CATALOG_TYPE_AMS:
        if (TableFormat.MIXED_ICEBERG == tableFormat) {
          catalogImpl = INTERNAL_CATALOG_IMPL;
        } else {
          throw new IllegalArgumentException("Internal Catalog mixed-iceberg table only");
        }
        break;
      default:
        throw new IllegalStateException("unsupported metastore type:" + metastoreType);
    }
    return catalogImpl;
  }

  /**
   * Load catalog meta from metastore.
   *
   * @param catalogUrl - catalog url
   * @return catalog meta
   */
  public static CatalogMeta loadMeta(String catalogUrl) {
    AmsThriftUrl url = AmsThriftUrl.parse(catalogUrl, Constants.THRIFT_TABLE_SERVICE_NAME);
    if (url.catalogName() == null || url.catalogName().contains("/")) {
      throw new IllegalArgumentException("invalid catalog name " + url.catalogName());
    }
    AmsClient client = new PooledAmsClient(catalogUrl);
    try {
      return client.getCatalog(url.catalogName());
    } catch (TException e) {
      throw new IllegalStateException("failed when load catalog " + url.catalogName(), e);
    }
  }

  /**
   * Show catalog list in metastore.
   *
   * @param metastoreUrl url of ams
   * @return catalog name list
   */
  public static List<String> catalogs(String metastoreUrl) {
    try {
      return ((AmoroTableMetastore.Iface) AmsClientPools.getClientPool(metastoreUrl).iface())
          .getCatalogs().stream().map(CatalogMeta::getCatalogName).collect(Collectors.toList());
    } catch (TException e) {
      throw new IllegalStateException("failed when load catalogs", e);
    }
  }

  /**
   * Entrypoint for loading catalog
   *
   * @param metaStoreUrl mixed-format metastore url
   * @param catalogName mixed-format catalog name
   * @param properties client side catalog configs
   * @return mixed-format catalog object
   */
  private static MixedFormatCatalog loadCatalog(
      String metaStoreUrl, String catalogName, Map<String, String> properties) {
    AmsClient client = new PooledAmsClient(metaStoreUrl);
    try {
      CatalogMeta catalogMeta = client.getCatalog(catalogName);
      String type = catalogMeta.getCatalogType();
      catalogMeta.putToCatalogProperties(CatalogMetaProperties.AMS_URI, metaStoreUrl);
      MixedFormatCatalogUtil.mergeCatalogProperties(catalogMeta, properties);
      return createCatalog(
          catalogName,
          type,
          catalogMeta.getCatalogProperties(),
          MixedFormatCatalogUtil.buildMetaStore(catalogMeta));
    } catch (NoSuchObjectException e1) {
      throw new IllegalArgumentException("catalog not found, please check catalog name", e1);
    } catch (Exception e) {
      throw new IllegalStateException("failed when load catalog " + catalogName, e);
    }
  }

  /**
   * build and initialize a mixed-format catalog.
   *
   * @param catalogName catalog-name
   * @param metastoreType metastore type
   * @param properties catalog properties
   * @param metaStore auth context
   * @return initialized catalog.
   */
  public static MixedFormatCatalog createCatalog(
      String catalogName,
      String metastoreType,
      Map<String, String> properties,
      TableMetaStore metaStore) {
    String catalogImpl = catalogImpl(metastoreType, properties);
    properties =
        MixedFormatCatalogUtil.withIcebergCatalogInitializeProperties(
            catalogName, metastoreType, properties);
    MixedFormatCatalog catalog = buildCatalog(catalogImpl);
    catalog.initialize(catalogName, properties, metaStore);
    return catalog;
  }

  @VisibleForTesting
  public static MixedFormatCatalog createCatalog(
      String catalogName,
      String catalogImpl,
      String metastoreType,
      Map<String, String> properties,
      TableMetaStore metaStore) {
    properties =
        MixedFormatCatalogUtil.withIcebergCatalogInitializeProperties(
            catalogName, metastoreType, properties);
    MixedFormatCatalog catalog = buildCatalog(catalogImpl);
    catalog.initialize(catalogName, properties, metaStore);
    return catalog;
  }

  private static MixedFormatCatalog buildCatalog(String impl) {
    DynConstructors.Ctor<MixedFormatCatalog> ctor;
    try {
      ctor = DynConstructors.builder(MixedFormatCatalog.class).impl(impl).buildChecked();
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
          String.format("Cannot initialize Catalog implementation %s: %s", impl, e.getMessage()),
          e);
    }
    try {
      return ctor.newInstance();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          String.format("Cannot initialize Catalog, %s does not implement Catalog.", impl), e);
    }
  }
}

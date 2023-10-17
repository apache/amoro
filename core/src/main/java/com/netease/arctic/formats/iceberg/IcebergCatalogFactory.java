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

package com.netease.arctic.formats.iceberg;

import com.netease.arctic.FormatCatalog;
import com.netease.arctic.FormatCatalogFactory;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.CatalogLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Map;

public class IcebergCatalogFactory implements FormatCatalogFactory {

  @Override
  public FormatCatalog create(
      String name, String metastoreType, Map<String, String> properties, Configuration configuration) {
    Catalog icebergCatalog = icebergCatalog(name, metastoreType, properties, configuration);
    return new IcebergCatalog(icebergCatalog);
  }

  public static Catalog icebergCatalog(
      String name,
      String metastoreType,
      Map<String, String> properties,
      Configuration configuration) {
    Preconditions.checkArgument(StringUtils.isNotBlank(metastoreType), "metastore type is blank");
    Map<String, String> icebergProperties = Maps.newHashMap(properties);
    if (CatalogMetaProperties.CATALOG_TYPE_HADOOP.equalsIgnoreCase(metastoreType) ||
        CatalogMetaProperties.CATALOG_TYPE_HIVE.equalsIgnoreCase(metastoreType)) {
      icebergProperties.put(CatalogUtil.ICEBERG_CATALOG_TYPE, metastoreType);
      icebergProperties.remove(CatalogProperties.CATALOG_IMPL);
    } else if (CatalogMetaProperties.CATALOG_TYPE_AMS.equalsIgnoreCase(metastoreType)) {
      icebergProperties.remove(CatalogUtil.ICEBERG_CATALOG_TYPE);
      icebergProperties.put(CatalogProperties.CATALOG_IMPL, CatalogLoader.ICEBERG_REST_CATALOG);
    } else if (CatalogMetaProperties.CATALOG_TYPE_GLUE.equals(metastoreType)) {
      icebergProperties.remove(CatalogUtil.ICEBERG_CATALOG_TYPE);
      icebergProperties.put(CatalogProperties.CATALOG_IMPL, CatalogLoader.GLUE_CATALOG_IMPL);
    } else {
      String icebergCatalogImpl = icebergProperties.get(CatalogProperties.CATALOG_IMPL);
      Preconditions.checkArgument(
          StringUtils.isNotBlank(icebergCatalogImpl),
          "iceberg catalog impl is blank");
      icebergProperties.remove(CatalogUtil.ICEBERG_CATALOG_TYPE);
      icebergProperties.put(CatalogProperties.CATALOG_IMPL, icebergCatalogImpl);
    }

    return CatalogUtil.buildIcebergCatalog(name, icebergProperties, configuration);
  }

  @Override
  public TableFormat format() {
    return TableFormat.ICEBERG;
  }
}

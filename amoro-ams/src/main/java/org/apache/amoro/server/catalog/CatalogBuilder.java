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

package org.apache.amoro.server.catalog;

import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_AMS;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_CUSTOM;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_GLUE;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_HIVE;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.utils.CatalogUtil;

import java.util.Map;
import java.util.Set;

public class CatalogBuilder {

  /** matrix of catalog type and supported table formats */
  private static final Map<String, Set<TableFormat>> formatSupportedMatrix =
      ImmutableMap.of(
          CATALOG_TYPE_HADOOP,
              Sets.newHashSet(
                  TableFormat.ICEBERG,
                  TableFormat.MIXED_ICEBERG,
                  TableFormat.PAIMON,
                  TableFormat.HUDI),
          CATALOG_TYPE_GLUE, Sets.newHashSet(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG),
          CATALOG_TYPE_CUSTOM, Sets.newHashSet(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG),
          CATALOG_TYPE_HIVE,
              Sets.newHashSet(
                  TableFormat.ICEBERG,
                  TableFormat.MIXED_ICEBERG,
                  TableFormat.MIXED_HIVE,
                  TableFormat.PAIMON,
                  TableFormat.HUDI),
          CATALOG_TYPE_AMS, Sets.newHashSet(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG));

  private static String getAmsURI(Configurations serviceConfig) {
    String host = serviceConfig.getString(AmoroManagementConf.SERVER_EXPOSE_HOST);
    Integer port = serviceConfig.getInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT);
    return String.format("thrift://%s:%d", host, port);
  }

  public static ServerCatalog buildServerCatalog(
      CatalogMeta catalogMeta, Configurations serverConfiguration) {
    String type = catalogMeta.getCatalogType();
    Set<TableFormat> tableFormats = CatalogUtil.tableFormats(catalogMeta);

    Preconditions.checkState(
        formatSupportedMatrix.containsKey(type), "unsupported catalog type: %s", type);

    Set<TableFormat> supportedFormats = formatSupportedMatrix.get(type);
    Preconditions.checkState(
        supportedFormats.containsAll(tableFormats),
        "Table format %s is not supported for metastore type: %s",
        tableFormats,
        type);

    switch (type) {
      case CATALOG_TYPE_HADOOP:
      case CATALOG_TYPE_GLUE:
      case CATALOG_TYPE_CUSTOM:
        return new ExternalCatalog(catalogMeta);
      case CATALOG_TYPE_HIVE:
        String amsUri = getAmsURI(serverConfiguration);
        catalogMeta.getCatalogProperties().put(CatalogMetaProperties.AMS_URI, amsUri);
        return new ExternalCatalog(catalogMeta);
      case CATALOG_TYPE_AMS:
        return new InternalCatalogImpl(catalogMeta, serverConfiguration);
      default:
        throw new IllegalStateException("unsupported catalog type:" + type);
    }
  }
}

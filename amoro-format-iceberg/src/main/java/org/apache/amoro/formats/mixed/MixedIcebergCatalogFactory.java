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

package org.apache.amoro.formats.mixed;

import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;

import org.apache.amoro.FormatCatalog;
import org.apache.amoro.FormatCatalogFactory;
import org.apache.amoro.TableFormat;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableMetaStore;

import java.util.Map;

public class MixedIcebergCatalogFactory implements FormatCatalogFactory {
  @Override
  public FormatCatalog create(
      String catalogName,
      String metastoreType,
      Map<String, String> properties,
      TableMetaStore metaStore) {
    MixedFormatCatalog catalog =
        CatalogLoader.createCatalog(catalogName, metastoreType, properties, metaStore);
    return new MixedCatalog(catalog, format());
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public Map<String, String> convertCatalogProperties(
      String catalogName, String metastoreType, Map<String, String> unifiedCatalogProperties) {
    Map<String, String> properties = Maps.newHashMap(unifiedCatalogProperties);
    properties.put(ICEBERG_CATALOG_TYPE, metastoreType);
    properties.put(CatalogMetaProperties.TABLE_FORMATS, format().name());
    return properties;
  }
}

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

package org.apache.amoro.formats.hudi;

import org.apache.amoro.FormatCatalog;
import org.apache.amoro.FormatCatalogFactory;
import org.apache.amoro.TableFormat;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableMetaStore;

import java.util.Map;

/** Hudi format catalog factory */
public class HudiCatalogFactory implements FormatCatalogFactory {
  @Override
  public FormatCatalog create(
      String catalogName,
      String metastoreType,
      Map<String, String> properties,
      TableMetaStore metaStore) {
    if (CatalogMetaProperties.CATALOG_TYPE_HADOOP.equalsIgnoreCase(metastoreType)) {
      return new HudiHadoopCatalog(catalogName, properties, metaStore);
    } else if (CatalogMetaProperties.CATALOG_TYPE_HIVE.equalsIgnoreCase(metastoreType)) {
      return new HudiHiveCatalog(catalogName, properties, metaStore);
    }
    throw new IllegalArgumentException(
        "Un-supported metastore type:" + metastoreType + " for Hudi");
  }

  @Override
  public TableFormat format() {
    return TableFormat.HUDI;
  }

  @Override
  public Map<String, String> convertCatalogProperties(
      String catalogName, String metastoreType, Map<String, String> unifiedCatalogProperties) {
    return unifiedCatalogProperties;
  }
}

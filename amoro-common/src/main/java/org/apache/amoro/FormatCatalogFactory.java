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

package org.apache.amoro;

import org.apache.amoro.table.TableMetaStore;

import java.util.Map;

/** A factory to create a {@link FormatCatalog}. */
public interface FormatCatalogFactory {

  /**
   * Creates a {@link FormatCatalog} given a map of catalog properties.
   *
   * @param catalogName catalog name
   * @param metastoreType metastore type
   * @param properties catalog properties
   * @param metaStore authentication context
   * @return a new {@link FormatCatalog}
   */
  FormatCatalog create(
      String catalogName,
      String metastoreType,
      Map<String, String> properties,
      TableMetaStore metaStore);

  /** format of this catalog factory */
  TableFormat format();

  /**
   * Convert UnifiedCatalog Properties to corresponding format Properties and use them to initialize
   * the corresponding Catalog.
   *
   * @param catalogName register in AMS
   * @param metastoreType metastore type
   * @param unifiedCatalogProperties properties of unified catalog.
   * @return properties of the target format.
   */
  Map<String, String> convertCatalogProperties(
      String catalogName, String metastoreType, Map<String, String> unifiedCatalogProperties);
}

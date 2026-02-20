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

package org.apache.amoro.formats.lance;

import org.apache.amoro.FormatCatalog;
import org.apache.amoro.FormatCatalogFactory;
import org.apache.amoro.TableFormat;
import org.apache.amoro.table.TableMetaStore;

import java.util.HashMap;
import java.util.Map;

/** Lance format catalog factory. */
public class LanceCatalogFactory implements FormatCatalogFactory {

  /** TableFormat instance for Lance. */
  public static final TableFormat LANCE = TableFormat.register("LANCE");

  @Override
  public FormatCatalog create(
      String catalogName,
      String metastoreType,
      Map<String, String> properties,
      TableMetaStore metaStore) {
    return new LanceDirectoryV1Catalog(catalogName, properties);
  }

  @Override
  public TableFormat format() {
    return LANCE;
  }

  @Override
  public Map<String, String> convertCatalogProperties(
      String catalogName, String metastoreType, Map<String, String> unifiedCatalogProperties) {
    if (unifiedCatalogProperties == null) {
      return null;
    }

    return new HashMap<>(unifiedCatalogProperties);
  }
}

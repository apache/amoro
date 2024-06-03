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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hudi.client.common.HoodieJavaEngineContext;
import org.apache.hudi.config.HoodieWriteConfig;
import org.apache.hudi.table.HoodieJavaTable;

import java.util.Collections;
import java.util.Map;

/**
 * The base implement for hudi catalog.
 */
public abstract class HudiCatalogBase implements FormatCatalog {

  protected final TableMetaStore metaStore;
  protected final String catalog;
  protected final Map<String, String> properties;

  protected HudiCatalogBase(String catalog, Map<String, String> catalogProperties, TableMetaStore metaStore) {
    this.catalog = catalog;
    this.metaStore = metaStore;
    this.properties = catalogProperties == null ?
        Collections.emptyMap():
        Collections.unmodifiableMap(catalogProperties);
  }


  protected Map<String, String> getProperties() {
    return this.properties;
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    String tableLocation = loadTableLocation(database, table);
    HoodieJavaEngineContext context = new HoodieJavaEngineContext(
        metaStore.getConfiguration());
    HoodieWriteConfig config = HoodieWriteConfig.newBuilder()
        .withPath(tableLocation)
        .build();

    return metaStore.doAs(() -> {
      HoodieJavaTable hoodieTable = HoodieJavaTable.create(config, context);
      TableIdentifier identifier = TableIdentifier.of(catalog, database, table);
      return new HudiTable(identifier, hoodieTable);
    });
  }


  protected abstract String loadTableLocation(String database, String table);
}

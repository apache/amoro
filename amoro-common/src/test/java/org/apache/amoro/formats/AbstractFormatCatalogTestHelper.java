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

package org.apache.amoro.formats;

import org.apache.amoro.AmoroCatalog;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.hadoop.conf.Configuration;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFormatCatalogTestHelper<C> implements AmoroCatalogTestHelper<C> {

  protected final String catalogName;

  protected final Map<String, String> catalogProperties;

  protected AbstractFormatCatalogTestHelper(
      String catalogName, Map<String, String> catalogProperties) {
    this.catalogName = catalogName;
    this.catalogProperties = catalogProperties == null ? new HashMap<>() : catalogProperties;
  }

  @Override
  public void initWarehouse(String warehouseLocation) {
    catalogProperties.put(catalogWarehouseKey(), warehouseLocation);
  }

  protected String catalogWarehouseKey() {
    return "warehouse";
  }

  protected abstract TableFormat format();

  protected String getMetastoreType() {
    return CatalogMetaProperties.CATALOG_TYPE_HADOOP;
  }

  @Override
  public void initHiveConf(Configuration hiveConf) {}

  @Override
  public CatalogMeta getCatalogMeta() {
    return CatalogTestHelpers.buildCatalogMeta(
        catalogName, getMetastoreType(), catalogProperties, format());
  }

  @Override
  public abstract AmoroCatalog amoroCatalog();

  @Override
  public abstract C originalCatalog();

  @Override
  public String catalogName() {
    return catalogName;
  }
}

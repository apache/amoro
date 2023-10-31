/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.formats;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.arctic.AmoroCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.CatalogTestHelpers;
import com.netease.arctic.formats.iceberg.IcebergCatalogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Types;

import java.util.HashMap;
import java.util.Map;

public class IcebergHadoopCatalogTestHelper implements AmoroCatalogTestHelper<Catalog> {

  public static final Schema schema =
      new Schema(
          Lists.newArrayList(
              Types.NestedField.required(1, DEFAULT_SCHEMA_ID_NAME, Types.LongType.get()),
              Types.NestedField.required(2, DEFAULT_SCHEMA_NAME_NAME, Types.StringType.get()),
              Types.NestedField.optional(3, DEFAULT_SCHEMA_AGE_NAME, Types.IntegerType.get())),
          Sets.newHashSet(1));

  public static final PartitionSpec spec =
      PartitionSpec.builderFor(schema).identity(DEFAULT_SCHEMA_AGE_NAME).build();

  public static final Map<String, String> properties = new HashMap<>();

  static {
    properties.put(DEFAULT_TABLE_OPTION_KEY, DEFAULT_TABLE_OPTION_VALUE);
  }

  protected final String catalogName;

  protected final Map<String, String> catalogProperties;

  public IcebergHadoopCatalogTestHelper(String catalogName, Map<String, String> catalogProperties) {
    this.catalogName = catalogName;
    this.catalogProperties = catalogProperties == null ? new HashMap<>() : catalogProperties;
  }

  @Override
  public void initWarehouse(String warehouseLocation) {
    catalogProperties.put(CatalogMetaProperties.KEY_WAREHOUSE, warehouseLocation);
  }

  @Override
  public void initHiveConf(Configuration hiveConf) {
    // Do nothing
  }

  @Override
  public CatalogMeta getCatalogMeta() {
    return CatalogTestHelpers.buildCatalogMeta(
        catalogName, getMetastoreType(), catalogProperties, TableFormat.ICEBERG);
  }

  @Override
  public AmoroCatalog amoroCatalog() {
    IcebergCatalogFactory icebergCatalogFactory = new IcebergCatalogFactory();
    return icebergCatalogFactory.create(
        catalogName, getMetastoreType(), catalogProperties, new Configuration());
  }

  @Override
  public Catalog originalCatalog() {
    return IcebergCatalogFactory.icebergCatalog(
        catalogName, getMetastoreType(), catalogProperties, new Configuration());
  }

  @Override
  public String catalogName() {
    return catalogName;
  }

  @Override
  public void setTableProperties(String db, String tableName, String key, String value) {
    originalCatalog()
        .loadTable(TableIdentifier.of(db, tableName))
        .updateProperties()
        .set(key, value)
        .commit();
  }

  @Override
  public void removeTableProperties(String db, String tableName, String key) {
    originalCatalog()
        .loadTable(TableIdentifier.of(db, tableName))
        .updateProperties()
        .remove(key)
        .commit();
  }

  @Override
  public void clean() {
    Catalog catalog = originalCatalog();
    if (catalog instanceof SupportsNamespaces) {
      for (Namespace ns : ((SupportsNamespaces) catalog).listNamespaces()) {
        catalog.listTables(ns).forEach(tableIdentifier -> catalog.dropTable(tableIdentifier, true));
        try {
          ((SupportsNamespaces) catalog).dropNamespace(ns);
        } catch (Exception e) {
          // 'default' database can not be dropped in hive catalog.
        }
      }
    }
  }

  @Override
  public void createTable(String db, String tableName) throws Exception {
    Catalog catalog = originalCatalog();
    catalog.createTable(TableIdentifier.of(db, tableName), schema, spec, properties);
  }

  protected String getMetastoreType() {
    return CatalogMetaProperties.CATALOG_TYPE_HADOOP;
  }

  public static IcebergHadoopCatalogTestHelper defaultHelper() {
    return new IcebergHadoopCatalogTestHelper("test_iceberg_catalog", new HashMap<>());
  }
}

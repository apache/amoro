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
import org.apache.amoro.formats.iceberg.IcebergCatalogFactory;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
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

public class IcebergHadoopCatalogTestHelper extends AbstractFormatCatalogTestHelper<Catalog> {

  public static final Schema SCHEMA =
      new Schema(
          Lists.newArrayList(
              Types.NestedField.required(1, DEFAULT_SCHEMA_ID_NAME, Types.LongType.get()),
              Types.NestedField.required(2, DEFAULT_SCHEMA_NAME_NAME, Types.StringType.get()),
              Types.NestedField.optional(3, DEFAULT_SCHEMA_AGE_NAME, Types.IntegerType.get())),
          Sets.newHashSet(1));

  public static final PartitionSpec SPEC =
      PartitionSpec.builderFor(SCHEMA).identity(DEFAULT_SCHEMA_AGE_NAME).build();

  public static final Map<String, String> PROPERTIES = new HashMap<>();

  static {
    PROPERTIES.put(DEFAULT_TABLE_OPTION_KEY, DEFAULT_TABLE_OPTION_VALUE);
  }

  public IcebergHadoopCatalogTestHelper(String catalogName, Map<String, String> catalogProperties) {
    super(catalogName, catalogProperties);
  }

  @Override
  protected TableFormat format() {
    return TableFormat.ICEBERG;
  }

  @Override
  public void initHiveConf(Configuration hiveConf) {
    // Do nothing
  }

  @Override
  public AmoroCatalog amoroCatalog() {
    IcebergCatalogFactory icebergCatalogFactory = new IcebergCatalogFactory();
    TableMetaStore metaStore = CatalogUtil.buildMetaStore(getCatalogMeta());
    Map<String, String> properties =
        icebergCatalogFactory.convertCatalogProperties(
            catalogName, getMetastoreType(), catalogProperties);
    return icebergCatalogFactory.create(catalogName, getMetastoreType(), properties, metaStore);
  }

  @Override
  public Catalog originalCatalog() {
    Map<String, String> props =
        MixedFormatCatalogUtil.withIcebergCatalogInitializeProperties(
            catalogName, getMetastoreType(), catalogProperties);
    TableMetaStore metaStore = CatalogUtil.buildMetaStore(getCatalogMeta());
    return org.apache.iceberg.CatalogUtil.buildIcebergCatalog(
        catalogName, props, metaStore.getConfiguration());
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
    catalog.createTable(TableIdentifier.of(db, tableName), SCHEMA, SPEC, PROPERTIES);
  }

  @Override
  public void createDatabase(String database) {
    Catalog catalog = originalCatalog();
    if (catalog instanceof SupportsNamespaces) {
      Namespace ns = Namespace.of(database);
      ((SupportsNamespaces) catalog).createNamespace(ns);
    }
  }

  public static IcebergHadoopCatalogTestHelper defaultHelper() {
    return new IcebergHadoopCatalogTestHelper("test_iceberg_catalog", new HashMap<>());
  }
}

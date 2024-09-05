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
import org.apache.amoro.formats.mixed.MixedIcebergCatalogFactory;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;

import java.util.HashMap;
import java.util.Map;

public class MixedIcebergHadoopCatalogTestHelper
    extends AbstractFormatCatalogTestHelper<MixedFormatCatalog> {

  public static final Schema SCHEMA =
      new Schema(
          Lists.newArrayList(
              Types.NestedField.required(1, DEFAULT_SCHEMA_ID_NAME, Types.LongType.get()),
              Types.NestedField.required(2, DEFAULT_SCHEMA_NAME_NAME, Types.StringType.get()),
              Types.NestedField.optional(3, DEFAULT_SCHEMA_AGE_NAME, Types.IntegerType.get())),
          Sets.newHashSet(1));

  public static final PartitionSpec SPEC =
      PartitionSpec.builderFor(SCHEMA).identity(DEFAULT_SCHEMA_AGE_NAME).build();

  public MixedIcebergHadoopCatalogTestHelper(
      String catalogName, Map<String, String> catalogProperties) {
    super(catalogName, catalogProperties);
  }

  @Override
  protected TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public AmoroCatalog amoroCatalog() {
    MixedIcebergCatalogFactory mixedIcebergCatalogFactory = new MixedIcebergCatalogFactory();
    TableMetaStore metaStore = MixedFormatCatalogUtil.buildMetaStore(getCatalogMeta());
    Map<String, String> properties =
        mixedIcebergCatalogFactory.convertCatalogProperties(
            catalogName, getMetastoreType(), catalogProperties);
    return mixedIcebergCatalogFactory.create(
        catalogName, getMetastoreType(), properties, metaStore);
  }

  @Override
  public MixedFormatCatalog originalCatalog() {
    CatalogMeta meta = getCatalogMeta();
    TableMetaStore metaStore = MixedFormatCatalogUtil.buildMetaStore(meta);
    return CatalogLoader.createCatalog(
        catalogName(), meta.getCatalogType(), meta.getCatalogProperties(), metaStore);
  }

  @Override
  public void setTableProperties(String db, String tableName, String key, String value) {
    originalCatalog()
        .loadTable(TableIdentifier.of(catalogName(), db, tableName))
        .updateProperties()
        .set(key, value)
        .commit();
  }

  @Override
  public void removeTableProperties(String db, String tableName, String key) {
    originalCatalog()
        .loadTable(TableIdentifier.of(catalogName(), db, tableName))
        .updateProperties()
        .remove(key)
        .commit();
  }

  @Override
  public void clean() {
    MixedFormatCatalog catalog = originalCatalog();
    catalog
        .listDatabases()
        .forEach(
            db -> {
              catalog.listTables(db).forEach(id -> catalog.dropTable(id, true));
              try {
                catalog.dropDatabase(db);
              } catch (Exception e) {
                // pass
              }
            });
  }

  @Override
  public void createTable(String db, String tableName) throws Exception {
    MixedFormatCatalog catalog = originalCatalog();
    catalog
        .newTableBuilder(TableIdentifier.of(catalogName(), db, tableName), SCHEMA)
        .withPartitionSpec(SPEC)
        .create();
  }

  @Override
  public void createDatabase(String database) {
    MixedFormatCatalog catalog = originalCatalog();
    catalog.createDatabase(database);
  }

  public static MixedIcebergHadoopCatalogTestHelper defaultHelper() {
    return new MixedIcebergHadoopCatalogTestHelper("test_mixed_iceberg_catalog", new HashMap<>());
  }
}

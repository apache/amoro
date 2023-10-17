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

import com.netease.arctic.AmoroCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.CatalogTestHelpers;
import com.netease.arctic.formats.paimon.PaimonCatalogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.types.DataTypes;

import java.util.HashMap;
import java.util.Map;

public class PaimonHadoopCatalogTestHelper implements AmoroCatalogTestHelper<Catalog> {

  public static final Schema schema = Schema.newBuilder()
      .column("id", DataTypes.INT())
      .column("name", DataTypes.STRING())
      .column("age", DataTypes.INT())
      .primaryKey("id", "age")
      .partitionKeys("age")
      .option("amoro.test.key", "amoro.test.value")
      .build();

  protected final String catalogName;

  protected final Map<String, String> catalogProperties;

  public PaimonHadoopCatalogTestHelper(
      String catalogName,
      Map<String, String> catalogProperties) {
    this.catalogName = catalogName;
    this.catalogProperties = catalogProperties == null ? new HashMap<>() : catalogProperties;
  }

  public void initWarehouse(String warehouseLocation) {
    catalogProperties.put(CatalogOptions.WAREHOUSE.key(), warehouseLocation);
  }

  @Override
  public void initHiveConf(Configuration hiveConf) {
    //Do nothing
  }

  @Override
  public CatalogMeta getCatalogMeta() {
    return CatalogTestHelpers.buildCatalogMeta(
        catalogName,
        getMetastoreType(),
        catalogProperties,
        TableFormat.PAIMON);
  }

  @Override
  public AmoroCatalog amoroCatalog() {
    PaimonCatalogFactory paimonCatalogFactory = new PaimonCatalogFactory();
    return paimonCatalogFactory.create(
        catalogName,
        getMetastoreType(),
        catalogProperties,
        new Configuration()
    );
  }

  @Override
  public Catalog originalCatalog() {
    return PaimonCatalogFactory.paimonCatalog(getMetastoreType(), catalogProperties, null);
  }

  @Override
  public String catalogName() {
    return catalogName;
  }

  @Override
  public void setTableProperties(String db, String tableName, String key, String value) {
    try {
      originalCatalog().alterTable(
          Identifier.create(db, tableName),
          SchemaChange.setOption(key, value),
          true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void removeTableProperties(String db, String tableName, String key) {
    try {
      originalCatalog().alterTable(
          Identifier.create(db, tableName),
          SchemaChange.removeOption(key),
          true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void clean() {
    try (Catalog catalog = originalCatalog()) {
      for (String dbName : catalog.listDatabases()) {
        try {
          catalog.dropDatabase(dbName, true, true);
          continue;
        } catch (Exception e) {
          // If drop database failed, drop all tables in this database. Because 'default' database can not be
          // dropped in hive catalog.
        }
        for (String tableName : catalog.listTables(dbName)) {
          catalog.dropTable(Identifier.create(dbName, tableName), true);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void createTable(String db, String tableName)
      throws Exception {
    try (Catalog catalog = originalCatalog()) {
      catalog.createTable(Identifier.create(db, tableName), schema, false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String getMetastoreType() {
    return CatalogMetaProperties.CATALOG_TYPE_HADOOP;
  }

  public static PaimonHadoopCatalogTestHelper defaultHelper() {
    return new PaimonHadoopCatalogTestHelper(
        "test_paimon_catalog",
        new HashMap<>());
  }
}

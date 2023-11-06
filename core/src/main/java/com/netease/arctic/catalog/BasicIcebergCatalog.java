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

package com.netease.arctic.catalog;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.blocker.TableBlockerManager;
import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Catalog;

import java.util.List;
import java.util.Map;

/** A wrapper class around {@link Catalog} and implement {@link ArcticCatalog}. */
public class BasicIcebergCatalog implements ArcticCatalog {

  private IcebergCatalogWrapper catalogWrapper;

  @Override
  public String name() {
    return catalogWrapper.name();
  }

  @Override
  public void initialize(String name, Map<String, String> properties, TableMetaStore metaStore) {
    this.catalogWrapper = new IcebergCatalogWrapper();
    this.catalogWrapper.initialize(name, properties, metaStore);
  }

  @Override
  public List<String> listDatabases() {
    return catalogWrapper.listDatabases();
  }

  @Override
  public void createDatabase(String databaseName) {
    catalogWrapper.createDatabase(databaseName);
  }

  @Override
  public void dropDatabase(String databaseName) {
    catalogWrapper.dropDatabase(databaseName);
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    return catalogWrapper.listTables(database);
  }

  @Override
  public ArcticTable loadTable(TableIdentifier tableIdentifier) {
    return catalogWrapper.loadTable(tableIdentifier);
  }

  @Override
  public void renameTable(TableIdentifier from, String newTableName) {
    catalogWrapper.renameTable(from, newTableName);
  }

  @Override
  public boolean dropTable(TableIdentifier tableIdentifier, boolean purge) {
    return catalogWrapper.dropTable(tableIdentifier, purge);
  }

  @Override
  public TableBuilder newTableBuilder(TableIdentifier identifier, Schema schema) {
    return catalogWrapper.newTableBuilder(identifier, schema);
  }

  @Override
  public TableBlockerManager getTableBlockerManager(TableIdentifier tableIdentifier) {
    throw new UnsupportedOperationException(
        "Catalog " + this.getClass().getName() + " doesn't support table blocker " + "manager");
  }

  @Override
  public Map<String, String> properties() {
    return catalogWrapper.properties();
  }
}

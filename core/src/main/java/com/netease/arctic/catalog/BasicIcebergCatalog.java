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

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.blocker.BasicTableBlockerManager;
import com.netease.arctic.table.blocker.TableBlockerManager;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

/**
 * A wrapper class around {@link Catalog} and implement {@link ArcticCatalog}.
 */
public class BasicIcebergCatalog implements ArcticCatalog {

  private AmsClient client;
  private CatalogMeta meta;
  private volatile CommonExternalMetastore metastore;


  @Override
  public String name() {
    return meta.getCatalogName();
  }

  @Override
  public void initialize(
      AmsClient client, CatalogMeta meta, Map<String, String> properties) {
    this.meta = meta;
    this.client = client;
  }


  private CommonExternalMetastore lazyMetastore() {
    if (metastore == null) {
      synchronized (this) {
        if (metastore == null){
          this.metastore = new CommonExternalMetastore(this.meta);
        }
      }
    }
    return this.metastore;
  }


  @Override
  public List<String> listDatabases() {
    return lazyMetastore().listDatabases();
  }

  @Override
  public void createDatabase(String databaseName) {
    lazyMetastore().createDatabase(databaseName);
  }

  @Override
  public void dropDatabase(String databaseName) {
    lazyMetastore().dropDatabase(databaseName);
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    return null;
  }

  @Override
  public ArcticTable loadTable(TableIdentifier tableIdentifier) {
    TableFormat format = lazyMetastore().tableFormat(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
    return lazyMetastore().tables(format).loadTable(tableIdentifier);
  }

  @Override
  public boolean tableExists(TableIdentifier tableIdentifier) {
    return lazyMetastore().exist(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
  }

  @Override
  public void renameTable(TableIdentifier from, String newTableName) {

  }

  @Override
  public boolean dropTable(TableIdentifier tableIdentifier, boolean purge) {
    try {
      TableFormat format = lazyMetastore().tableFormat(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
      return lazyMetastore().tables(format).dropTable(tableIdentifier, purge);
    } catch (NoSuchTableException e) {
      return false;
    }
  }

  @Override
  public TableBuilder newTableBuilder(
      TableIdentifier identifier, Schema schema) {
    return this.newTableBuilder(identifier, schema, TableFormat.ICEBERG);
  }

  @Override
  public TableBuilder newTableBuilder(TableIdentifier identifier, Schema schema, TableFormat format) {
    return lazyMetastore().tables(format).newTableBuilder(schema, identifier);
  }

  @Override
  public synchronized void refresh() {
    try {
      this.meta = client.getCatalog(name());
      this.metastore = null;
    } catch (TException e) {
      throw new IllegalStateException(String.format("failed load catalog %s.", this.meta.getCatalogName()), e);
    }
  }

  @Override
  public TableBlockerManager getTableBlockerManager(TableIdentifier tableIdentifier) {
    return BasicTableBlockerManager.build(tableIdentifier, client);
  }

  @Override
  public Map<String, String> properties() {
    return meta.getCatalogProperties();
  }
}

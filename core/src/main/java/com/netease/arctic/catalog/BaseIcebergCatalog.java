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
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticHadoopFileIO;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseUnkeyedTable;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A wrapper class around {@link Catalog} and implement {@link ArcticCatalog}.
 */
public class BaseIcebergCatalog implements ArcticCatalog {

  private CatalogMeta meta;
  private transient TableMetaStore tableMetaStore;
  private transient Catalog icebergCatalog;

  @Override
  public String name() {
    return meta.getCatalogName();
  }

  @Override
  public void initialize(
      AmsClient client, CatalogMeta meta, Map<String, String> properties) {
    this.meta = meta;
    meta.putToCatalogProperties(org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE,
        meta.getCatalogType());
    tableMetaStore = CatalogUtil.buildMetaStore(meta);
    if (meta.getCatalogProperties().containsKey(CatalogProperties.CATALOG_IMPL)) {
      meta.getCatalogProperties().remove("type");
    }
    icebergCatalog = org.apache.iceberg.CatalogUtil.buildIcebergCatalog(name(),
        meta.getCatalogProperties(), tableMetaStore.getConfiguration());
  }

  @Override
  public List<String> listDatabases() {
    if (icebergCatalog instanceof SupportsNamespaces) {
      return tableMetaStore.doAs(() -> ((SupportsNamespaces)icebergCatalog).listNamespaces(Namespace.empty()).stream()
          .map(namespace -> namespace.level(0)).distinct().collect(Collectors.toList()));
    } else {
      throw new UnsupportedOperationException(String.format("Iceberg catalog: %s do now implement SupportsNamespaces",
       icebergCatalog.getClass().getName()));
    }
  }

  @Override
  public void createDatabase(String databaseName) {
    if (icebergCatalog instanceof SupportsNamespaces) {
      tableMetaStore.doAs(() -> {
        ((SupportsNamespaces)icebergCatalog).createNamespace(Namespace.of(databaseName));
        return null;
      });
    } else {
      throw new UnsupportedOperationException(String.format("Iceberg catalog: %s do now implement SupportsNamespaces",
          icebergCatalog.getClass().getName()));
    }
  }

  @Override
  public void dropDatabase(String databaseName) {
    if (icebergCatalog instanceof SupportsNamespaces) {
      tableMetaStore.doAs(() -> {
        ((SupportsNamespaces)icebergCatalog).dropNamespace(Namespace.of(databaseName));
        return null;
      });
    } else {
      throw new UnsupportedOperationException(String.format("Iceberg catalog: %s do now implement SupportsNamespaces",
          icebergCatalog.getClass().getName()));
    }
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    return tableMetaStore.doAs(() -> icebergCatalog.listTables(Namespace.of(database)).stream()
      .filter(tableIdentifier -> tableIdentifier.namespace().levels().length == 1)
      .map(tableIdentifier -> TableIdentifier.of(name(), database, tableIdentifier.name()))
      .collect(Collectors.toList()));
  }

  @Override
  public ArcticTable loadTable(TableIdentifier tableIdentifier) {
    Table icebergTable = tableMetaStore.doAs(() -> icebergCatalog
        .loadTable(toIcebergTableIdentifier(tableIdentifier)));
    ArcticFileIO arcticFileIO = new ArcticHadoopFileIO(tableMetaStore);
    return new BaseIcebergTable(tableIdentifier, CatalogUtil.useArcticTableOperations(icebergTable,
        icebergTable.location(), arcticFileIO, tableMetaStore.getConfiguration()), arcticFileIO);
  }

  @Override
  public void renameTable(TableIdentifier from, String newTableName) {
    tableMetaStore.doAs(() -> {
      icebergCatalog.renameTable(toIcebergTableIdentifier(from),
          org.apache.iceberg.catalog.TableIdentifier.of(Namespace.of(from.getDatabase()),
              newTableName));
      return null;
    });
  }

  @Override
  public boolean dropTable(TableIdentifier tableIdentifier, boolean purge) {
    return tableMetaStore.doAs(() -> icebergCatalog.dropTable(toIcebergTableIdentifier(tableIdentifier), purge));
  }

  @Override
  public TableBuilder newTableBuilder(
      TableIdentifier identifier, Schema schema) {
    throw new UnsupportedOperationException("unsupported new iceberg table for now.");
  }

  private org.apache.iceberg.catalog.TableIdentifier toIcebergTableIdentifier(TableIdentifier tableIdentifier) {
    return org.apache.iceberg.catalog.TableIdentifier.of(Namespace.of(tableIdentifier.getDatabase()),
        tableIdentifier.getTableName());
  }

  public class BaseIcebergTable extends BaseUnkeyedTable {

    public BaseIcebergTable(
        TableIdentifier tableIdentifier,
        Table icebergTable,
        ArcticFileIO arcticFileIO) {
      super(tableIdentifier, icebergTable, arcticFileIO);
    }
  }
}

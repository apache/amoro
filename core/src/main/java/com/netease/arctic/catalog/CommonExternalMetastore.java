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
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.iceberg.IcebergTables;
import com.netease.arctic.iceberg.mixed.MixedIcebergTables;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.ArcticTables;
import com.netease.arctic.table.MixedTables;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonExternalMetastore implements Metastore {

  private final Catalog icebergCatalog;
  private final TableMetaStore tableMetaStore;

  private final Pattern databaseFilterPattern;
  private final IcebergTables icebergTables;
  private final MixedTables mixedIcebergTables;


  public CommonExternalMetastore(CatalogMeta meta) {
    meta.putToCatalogProperties(
        org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE,
        meta.getCatalogType());
    if (meta.getCatalogProperties().containsKey(CatalogProperties.CATALOG_IMPL)) {
      meta.getCatalogProperties().remove("type");
    }
    this.tableMetaStore = CatalogUtil.buildMetaStore(meta);
    this.icebergCatalog = tableMetaStore.doAs(() -> org.apache.iceberg.CatalogUtil.buildIcebergCatalog(
        meta.getCatalogName(), meta.getCatalogProperties(), tableMetaStore.getConfiguration()));

    if (meta.getCatalogProperties().containsKey(CatalogMetaProperties.KEY_DATABASE_FILTER_REGULAR_EXPRESSION)) {
      String databaseFilter =
          meta.getCatalogProperties().get(CatalogMetaProperties.KEY_DATABASE_FILTER_REGULAR_EXPRESSION);
      databaseFilterPattern = Pattern.compile(databaseFilter);
    } else {
      databaseFilterPattern = null;
    }

    this.icebergTables = new IcebergTables(icebergCatalog, meta.getCatalogProperties(), tableMetaStore);
    this.mixedIcebergTables = new MixedIcebergTables(this.icebergTables);
  }

  @Override
  public List<String> listDatabases() {
    if (!(icebergCatalog instanceof SupportsNamespaces)) {
      throw new UnsupportedOperationException(String.format(
          "Iceberg catalog: %s doesn't implement SupportsNamespaces",
          icebergCatalog.getClass().getName()));
    }

    List<String> databases =
        tableMetaStore.doAs(() ->
            ((SupportsNamespaces) icebergCatalog).listNamespaces(Namespace.empty())
                .stream()
                .map(namespace -> namespace.level(0))
                .distinct()
                .collect(Collectors.toList())
        );
    if (databaseFilterPattern == null) {
      return databases;
    }
    return databases.stream()
        .filter(database -> databaseFilterPattern.matcher(database).matches())
        .collect(Collectors.toList());
  }

  @Override
  public boolean exist(String database) {
    Preconditions.checkNotNull(database);
    if (!(icebergCatalog instanceof SupportsNamespaces)) {
      return false;
    }

    return tableMetaStore.doAs(() ->
            ((SupportsNamespaces) icebergCatalog).listNamespaces(Namespace.empty()))
        .stream()
        .map(namespace -> namespace.level(0))
        .anyMatch(database::equals);
  }

  @Override
  public boolean exist(String database, String table) {
    Namespace ns = Namespace.of(database);
    return tableMetaStore.doAs(() -> icebergCatalog.tableExists(TableIdentifier.of(ns, table)));
  }

  @Override
  public void createDatabase(String database) {
    if (!(icebergCatalog instanceof SupportsNamespaces)) {
      throw new UnsupportedOperationException(String.format(
          "Iceberg catalog: %s doesn't implement SupportsNamespaces",
          icebergCatalog.getClass().getName()));
    }
  }

  @Override
  public void dropDatabase(String database) {
    Preconditions.checkNotNull(database);
    if (icebergCatalog instanceof SupportsNamespaces) {
      SupportsNamespaces supportsNamespaces = (SupportsNamespaces) icebergCatalog;
      Namespace ns = Namespace.of(database);
      tableMetaStore.doAs(() -> supportsNamespaces.dropNamespace(ns));
    }
  }

  @Override
  public TableFormat tableFormat(String database, String table) {
    return this.loadTable(database, table).format();
  }


  public ArcticTable loadTable(String database, String table) {
    ArcticTable icebergTable = icebergTables.loadTable(com.netease.arctic.table.TableIdentifier.of(
        this.icebergCatalog.name(), database, table
    ));
    if (mixedIcebergTables.isMixedTable(icebergTable)) {
      return mixedIcebergTables.loadMixedTable(icebergTable);
    }
    return icebergTable;
  }

  public ArcticTables tables(TableFormat format) {
    switch (format) {
      case ICEBERG:
        return icebergTables;
      case MIXED_ICEBERG:
        return mixedIcebergTables;
      default:
        throw new IllegalArgumentException("this catalog doesn't support table format: " + format);
    }
  }
}

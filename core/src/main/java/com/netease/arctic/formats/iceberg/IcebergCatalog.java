/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.formats.iceberg;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.FormatCatalog;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class IcebergCatalog implements FormatCatalog {

  private final Catalog icebergCatalog;

  public IcebergCatalog(Catalog icebergCatalog) {
    this.icebergCatalog = icebergCatalog;
  }

  @Override
  public List<String> listDatabases() {
    if (icebergCatalog instanceof SupportsNamespaces) {
      return ((SupportsNamespaces) icebergCatalog)
          .listNamespaces().stream().map(ns -> ns.level(0)).collect(Collectors.toList());
    }
    return Lists.newArrayList();
  }

  @Override
  public boolean exist(String database) {
    return listDatabases().contains(database);
  }

  @Override
  public boolean exist(String database, String table) {
    TableIdentifier identifier = TableIdentifier.of(database, table);
    return icebergCatalog.tableExists(identifier);
  }

  @Override
  public void createDatabase(String database) {
    if (icebergCatalog instanceof SupportsNamespaces) {
      Namespace ns = Namespace.of(database);
      ((SupportsNamespaces) icebergCatalog).createNamespace(ns);
    }
  }

  @Override
  public void dropDatabase(String database) {
    Namespace ns = Namespace.of(database);
    if (icebergCatalog instanceof SupportsNamespaces) {
      ((SupportsNamespaces) icebergCatalog).dropNamespace(ns);
    }
  }

  @Override
  public List<String> listTables(String database) {
    return icebergCatalog.listTables(Namespace.of(database)).stream()
        .map(TableIdentifier::name)
        .collect(Collectors.toList());
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    Table icebergTable = icebergCatalog.loadTable(TableIdentifier.of(database, table));
    return new IcebergTable(
        com.netease.arctic.table.TableIdentifier.of(icebergCatalog.name(), database, table),
        icebergTable);
  }
}

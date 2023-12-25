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

package com.netease.arctic.formats.mixed;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.FormatCatalog;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.exceptions.NoSuchTableException;

import java.util.List;
import java.util.stream.Collectors;

public class MixedCatalog implements FormatCatalog {
  final ArcticCatalog catalog;
  final TableFormat format;

  public MixedCatalog(ArcticCatalog catalog, TableFormat format) {
    this.catalog = catalog;
    this.format = format;
  }

  @Override
  public List<String> listDatabases() {
    return catalog.listDatabases();
  }

  @Override
  public boolean exist(String database) {
    return catalog.listDatabases().contains(database);
  }

  @Override
  public boolean exist(String database, String table) {
    return catalog.tableExists(TableIdentifier.of(catalog.name(), database, table));
  }

  @Override
  public void createDatabase(String database) {
    catalog.createDatabase(database);
  }

  @Override
  public void dropDatabase(String database) {
    catalog.dropDatabase(database);
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    try {
      ArcticTable mixedTable =
          catalog.loadTable(TableIdentifier.of(catalog.name(), database, table));
      return new MixedTable(mixedTable, format);
    } catch (NoSuchTableException e) {
      throw new com.netease.arctic.NoSuchTableException(e);
    }
  }

  @Override
  public List<String> listTables(String database) {
    return catalog.listTables(database).stream()
        .map(TableIdentifier::getTableName)
        .collect(Collectors.toList());
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    return catalog.dropTable(TableIdentifier.of(catalog.name(), database, table), purge);
  }
}

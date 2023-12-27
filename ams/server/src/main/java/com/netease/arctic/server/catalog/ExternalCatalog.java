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

package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.CommonUnifiedCatalog;
import com.netease.arctic.TableIDWithFormat;
import com.netease.arctic.UnifiedCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExternalCatalog extends ServerCatalog {

  UnifiedCatalog unifiedCatalog;
  TableMetaStore tableMetaStore;
  private Pattern tableFilterPattern;
  private Pattern databaseFilterPattern;

  protected ExternalCatalog(CatalogMeta metadata) {
    super(metadata);
    this.tableMetaStore = CatalogUtil.buildMetaStore(metadata);
    this.unifiedCatalog =
        this.tableMetaStore.doAs(
            () -> new CommonUnifiedCatalog(this::getMetadata, Maps.newHashMap()));
    updateTableFilter(metadata);
    updateDatabaseFilter(metadata);
  }

  public void syncTable(String database, String tableName, TableFormat format) {
    ServerTableIdentifier tableIdentifier =
        ServerTableIdentifier.of(getMetadata().getCatalogName(), database, tableName, format);
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(tableIdentifier));
  }

  public ServerTableIdentifier getServerTableIdentifier(String database, String tableName) {
    return getAs(
        TableMetaMapper.class,
        mapper ->
            mapper.selectTableIdentifier(getMetadata().getCatalogName(), database, tableName));
  }

  public void disposeTable(String database, String tableName) {
    doAs(
        TableMetaMapper.class,
        mapper -> mapper.deleteTableIdByName(getMetadata().getCatalogName(), database, tableName));
  }

  @Override
  public void updateMetadata(CatalogMeta metadata) {
    super.updateMetadata(metadata);
    this.tableMetaStore = CatalogUtil.buildMetaStore(metadata);
    this.unifiedCatalog.refresh();
    updateDatabaseFilter(metadata);
    updateTableFilter(metadata);
  }

  @Override
  public boolean exist(String database) {
    return doAs(() -> unifiedCatalog.exist(database));
  }

  @Override
  public boolean exist(String database, String tableName) {
    return doAs(() -> unifiedCatalog.exist(database, tableName));
  }

  @Override
  public List<String> listDatabases() {
    return doAs(
        () ->
            unifiedCatalog.listDatabases().stream()
                .filter(
                    database ->
                        databaseFilterPattern == null
                            || databaseFilterPattern.matcher(database).matches())
                .collect(Collectors.toList()));
  }

  @Override
  public List<TableIDWithFormat> listTables() {
    return doAs(
        () ->
            unifiedCatalog.listDatabases().stream()
                .map(this::listTables)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
  }

  @Override
  public List<TableIDWithFormat> listTables(String database) {
    return doAs(
        () ->
            new ArrayList<>(
                unifiedCatalog.listTables(database).stream()
                    .filter(
                        tableIDWithFormat ->
                            tableFilterPattern == null
                                || tableFilterPattern
                                    .matcher(
                                        (database
                                            + "."
                                            + tableIDWithFormat.getIdentifier().getTableName()))
                                    .matches())
                    .collect(Collectors.toList())));
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    return doAs(() -> unifiedCatalog.loadTable(database, tableName));
  }

  private void updateDatabaseFilter(CatalogMeta metadata) {
    String databaseFilter =
        metadata.getCatalogProperties().get(CatalogMetaProperties.KEY_DATABASE_FILTER);
    if (databaseFilter != null) {
      databaseFilterPattern = Pattern.compile(databaseFilter);
    } else {
      databaseFilterPattern = null;
    }
  }

  private void updateTableFilter(CatalogMeta metadata) {
    String tableFilter =
        metadata.getCatalogProperties().get(CatalogMetaProperties.KEY_TABLE_FILTER);
    if (tableFilter != null) {
      tableFilterPattern = Pattern.compile(tableFilter);
    } else {
      tableFilterPattern = null;
    }
  }

  private <T> T doAs(Callable<T> callable) {
    return tableMetaStore.doAs(callable);
  }
}

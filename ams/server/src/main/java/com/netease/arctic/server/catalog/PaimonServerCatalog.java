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

package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.CommonUnifiedCatalog;
import com.netease.arctic.TableIDWithFormat;
import com.netease.arctic.UnifiedCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PaimonServerCatalog extends ExternalCatalog {

  private volatile UnifiedCatalog paimonCatalog;

  private volatile TableMetaStore tableMetaStore;

  protected PaimonServerCatalog(CatalogMeta metadata) {
    super(metadata);
    this.tableMetaStore = CatalogUtil.buildMetaStore(metadata);
    this.paimonCatalog =
        doAs(() -> new CommonUnifiedCatalog(null, metadata, metadata.catalogProperties));
  }

  @Override
  public void updateMetadata(CatalogMeta metadata) {
    super.updateMetadata(metadata);
    this.tableMetaStore = CatalogUtil.buildMetaStore(metadata);
    this.paimonCatalog =
        doAs(() -> new CommonUnifiedCatalog(null, metadata, metadata.catalogProperties));
  }

  @Override
  public boolean exist(String database) {
    return doAs(() -> paimonCatalog.exist(database));
  }

  @Override
  public boolean exist(String database, String tableName) {
    return doAs(() -> paimonCatalog.exist(database, tableName));
  }

  @Override
  public List<String> listDatabases() {
    return doAs(() -> paimonCatalog.listDatabases());
  }

  @Override
  public List<TableIDWithFormat> listTables() {
    return doAs(
        () ->
            paimonCatalog.listDatabases().stream()
                .map(this::listTables)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
  }

  @Override
  public List<TableIDWithFormat> listTables(String database) {
    return doAs(() -> new ArrayList<>(paimonCatalog.listTables(database)));
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    return doAs(() -> paimonCatalog.loadTable(database, tableName));
  }

  private <T> T doAs(Callable<T> callable) {
    return tableMetaStore.doAs(callable);
  }
}

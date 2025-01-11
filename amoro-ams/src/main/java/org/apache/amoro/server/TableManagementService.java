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

package org.apache.amoro.server;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.AmoroTableMetastore;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.Blocker;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.NoSuchObjectException;
import org.apache.amoro.api.OperationConflictException;
import org.apache.amoro.api.TableCommitMeta;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.utils.InternalTableUtil;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableManagementService implements AmoroTableMetastore.Iface {

  private final CatalogManager catalogManager;
  private final TableService tableService;

  public TableManagementService(CatalogManager catalogManager, TableService tableService) {
    this.catalogManager = catalogManager;
    this.tableService = tableService;
  }

  @Override
  public void ping() {}

  @Override
  public List<CatalogMeta> getCatalogs() {
    return catalogManager.listCatalogMetas();
  }

  @Override
  public CatalogMeta getCatalog(String name) {
    return catalogManager.getCatalogMeta(name);
  }

  @Override
  public List<String> getDatabases(String catalogName) {
    ServerCatalog serverCatalog = catalogManager.getServerCatalog(catalogName);
    return serverCatalog.listDatabases();
  }

  @Override
  public void createDatabase(String catalogName, String database) {
    InternalCatalog serverCatalog = catalogManager.getInternalCatalog(catalogName);
    serverCatalog.createDatabase(database);
  }

  @Override
  public void dropDatabase(String catalogName, String database) {
    InternalCatalog serverCatalog = catalogManager.getInternalCatalog(catalogName);
    serverCatalog.dropDatabase(database);
  }

  @Override
  public void createTableMeta(TableMeta tableMeta) {
    if (tableMeta == null) {
      throw new IllegalArgumentException("table meta should not be null");
    }
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of(
            tableMeta.getTableIdentifier(), TableFormat.valueOf(tableMeta.getFormat()));
    InternalCatalog catalog = catalogManager.getInternalCatalog(identifier.getCatalog());
    CatalogMeta catalogMeta = catalog.getMetadata();
    TableMetadata tableMetadata = new TableMetadata(identifier, tableMeta, catalogMeta);
    tableService.createTable(catalog.name(), tableMetadata);
  }

  @Override
  public List<TableMeta> listTables(String catalogName, String database) {
    InternalCatalog serverCatalog = catalogManager.getInternalCatalog(catalogName);
    List<TableMetadata> tableMetadataList = serverCatalog.listTableMetadataInDatabase(database);
    return tableMetadataList.stream()
        .map(TableMetadata::buildTableMeta)
        .collect(Collectors.toList());
  }

  @Override
  public TableMeta getTable(TableIdentifier tableIdentifier) {
    InternalCatalog serverCatalog = catalogManager.getInternalCatalog(tableIdentifier.getCatalog());
    TableMetadata tableMetadata =
        serverCatalog.loadTableMetadata(
            tableIdentifier.getDatabase(), tableIdentifier.getTableName());
    if (tableMetadata.getFormat() == TableFormat.MIXED_ICEBERG
        && !InternalTableUtil.isLegacyMixedIceberg(tableMetadata)) {
      throw new IllegalArgumentException(
          "The table "
              + tableIdentifier.toString()
              + " is based"
              + " on rest-catalog, please upgrade your connector");
    }
    return tableMetadata.buildTableMeta();
  }

  @Override
  public void removeTable(TableIdentifier tableIdentifier, boolean deleteData) {
    tableService.dropTableMetadata(tableIdentifier, deleteData);
  }

  @Override
  public void tableCommit(TableCommitMeta commit) {}

  @Override
  public long allocateTransactionId(TableIdentifier tableIdentifier, String transactionSignature)
      throws TException {
    throw new UnsupportedOperationException("allocate TransactionId from AMS is not supported now");
  }

  @Override
  public Blocker block(
      TableIdentifier tableIdentifier,
      List<BlockableOperation> operations,
      Map<String, String> properties)
      throws OperationConflictException {
    return tableService.block(tableIdentifier, operations, properties);
  }

  @Override
  public void releaseBlocker(TableIdentifier tableIdentifier, String blockerId) {
    tableService.releaseBlocker(tableIdentifier, blockerId);
  }

  @Override
  public long renewBlocker(TableIdentifier tableIdentifier, String blockerId)
      throws NoSuchObjectException {
    return tableService.renewBlocker(tableIdentifier, blockerId);
  }

  @Override
  public List<Blocker> getBlockers(TableIdentifier tableIdentifier) {
    return tableService.getBlockers(tableIdentifier);
  }
}

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

package com.netease.arctic.server;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.ArcticException;
import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.Blocker;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.ams.api.OperationErrorException;
import com.netease.arctic.ams.api.TableCommitMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.server.exception.ArcticRuntimeException;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.table.TableService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class TableManagementService implements AmsClient, ArcticTableMetastore.Iface {

  public static final Logger LOG = LoggerFactory.getLogger(TableManagementService.class);

  private final TableService tableService;

  public TableManagementService(TableService tableService) {
    this.tableService = tableService;
  }

  @Override
  public void ping() {
  }

  private <T> T getAs(Supplier<T> supplier) throws ArcticException {
    try {
      return supplier.get();
    } catch (Throwable throwable) {
      throw ArcticRuntimeException.transformThrift(throwable);
    }
  }

  private void doAs(Runnable runnable) throws ArcticException {
    try {
      runnable.run();
    } catch (Throwable throwable) {
      throw ArcticRuntimeException.transformThrift(throwable);
    }
  }

  @Override
  public List<CatalogMeta> getCatalogs() throws ArcticException {
    return getAs(tableService::listCatalogMetas);
  }

  @Override
  public CatalogMeta getCatalog(String name) throws ArcticException {
    return getAs(() -> tableService.getCatalogMeta(name));
  }

  @Override
  public List<String> getDatabases(String catalogName) throws ArcticException {
    return getAs(() -> tableService.listDatabases(catalogName));
  }

  @Override
  public void createDatabase(String catalogName, String database) throws ArcticException {
    doAs(() -> tableService.createDatabase(catalogName, database));
  }

  @Override
  public void dropDatabase(String catalogName, String database) throws ArcticException {
    doAs(() -> tableService.dropDatabase(catalogName, database));
  }

  @Override
  public void createTableMeta(TableMeta tableMeta) throws ArcticException {
    if (tableMeta == null) {
      throw new IllegalArgumentException("table meta should not be null");
    }

    doAs(() -> tableService.createTable(tableMeta.tableIdentifier.getCatalog(), tableMeta));
  }

  @Override
  public List<TableMeta> listTables(String catalogName, String database) throws ArcticException {
    List<TableMetadata> tableMetadataList = getAs(() -> tableService.listTableMetas(catalogName, database));
    return tableMetadataList.stream()
            .map(TableMetadata::buildTableMeta)
            .collect(Collectors.toList());
  }

  @Override
  public TableMeta getTable(TableIdentifier tableIdentifier) throws ArcticException {
    return getAs(() -> tableService.loadTableMetadata(tableIdentifier)).buildTableMeta();
  }

  @Override
  public void removeTable(TableIdentifier tableIdentifier, boolean deleteData) throws ArcticException {
    doAs(() -> tableService.dropTableMetadata(tableIdentifier, deleteData));
  }

  @Override
  public void tableCommit(TableCommitMeta commit) throws ArcticException {
  }

  @Override
  public long allocateTransactionId(TableIdentifier tableIdentifier, String transactionSignature) {
    return 0;
  }

  @Override
  public Blocker block(
      TableIdentifier tableIdentifier, List<BlockableOperation> operations, Map<String, String> properties)
      throws OperationConflictException, TException {
    return null;
  }

  @Override
  public void releaseBlocker(TableIdentifier tableIdentifier, String blockerId) throws TException {

  }

  @Override
  public long renewBlocker(TableIdentifier tableIdentifier, String blockerId) throws NoSuchObjectException, TException {
    return 0;
  }

  @Override
  public List<Blocker> getBlockers(TableIdentifier tableIdentifier) throws TException {
    return null;
  }

  @Override
  public void refreshTable(TableIdentifier tableIdentifier) throws OperationErrorException, TException {

  }
}

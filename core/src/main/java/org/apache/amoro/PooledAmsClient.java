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

package org.apache.amoro;

import org.apache.amoro.api.ArcticTableMetastore;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.Blocker;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.TableCommitMeta;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.client.AmsClientPools;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

/** {@link AmsClient} implementation using client pool. */
public class PooledAmsClient implements AmsClient {
  private final String metastoreUrl;

  public PooledAmsClient(String metastoreUrl) {
    this.metastoreUrl = metastoreUrl;
  }

  private ArcticTableMetastore.Iface getIface() {
    return AmsClientPools.getClientPool(metastoreUrl).iface();
  }

  @Override
  public void ping() throws TException {}

  @Override
  public List<CatalogMeta> getCatalogs() throws TException {
    return getIface().getCatalogs();
  }

  @Override
  public CatalogMeta getCatalog(String name) throws TException {
    return getIface().getCatalog(name);
  }

  @Override
  public List<String> getDatabases(String catalogName) throws TException {
    return getIface().getDatabases(catalogName);
  }

  @Override
  public void createDatabase(String catalogName, String database) throws TException {
    getIface().createDatabase(catalogName, database);
  }

  @Override
  public void dropDatabase(String catalogName, String database) throws TException {
    getIface().dropDatabase(catalogName, database);
  }

  @Override
  public void createTableMeta(TableMeta tableMeta) throws TException {
    getIface().createTableMeta(tableMeta);
  }

  @Override
  public List<TableMeta> listTables(String catalogName, String database) throws TException {
    return getIface().listTables(catalogName, database);
  }

  @Override
  public TableMeta getTable(TableIdentifier tableIdentifier) throws TException {
    return getIface().getTable(tableIdentifier);
  }

  @Override
  public void removeTable(TableIdentifier tableIdentifier, boolean deleteData) throws TException {
    getIface().removeTable(tableIdentifier, deleteData);
  }

  @Override
  public void tableCommit(TableCommitMeta commit) throws TException {
    getIface().tableCommit(commit);
  }

  @Override
  public long allocateTransactionId(TableIdentifier tableIdentifier, String transactionSignature)
      throws TException {
    return getIface().allocateTransactionId(tableIdentifier, transactionSignature);
  }

  @Override
  public Blocker block(
      TableIdentifier tableIdentifier,
      List<BlockableOperation> operations,
      Map<String, String> properties)
      throws TException {
    return getIface().block(tableIdentifier, operations, properties);
  }

  @Override
  public void releaseBlocker(TableIdentifier tableIdentifier, String blockerId) throws TException {
    getIface().releaseBlocker(tableIdentifier, blockerId);
  }

  @Override
  public long renewBlocker(TableIdentifier tableIdentifier, String blockerId) throws TException {
    return getIface().renewBlocker(tableIdentifier, blockerId);
  }

  @Override
  public List<Blocker> getBlockers(TableIdentifier tableIdentifier) throws TException {
    return getIface().getBlockers(tableIdentifier);
  }
}

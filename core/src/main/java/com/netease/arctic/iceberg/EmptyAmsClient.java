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

package com.netease.arctic.iceberg;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.AlreadyExistsException;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.Blocker;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.InvalidObjectException;
import com.netease.arctic.ams.api.MetaException;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.NotSupportedException;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.ams.api.TableCommitMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.TableMeta;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

/**
 * This class is an empty implement for AmsClient, to make mixed-iceberg tables fit the older apis.
 */
public class EmptyAmsClient implements AmsClient {


  @Override
  public void ping() throws TException {

  }

  @Override
  public List<CatalogMeta> getCatalogs() throws TException {
    throw new UnsupportedOperationException("empty ams client can't load catalog meta");
  }

  @Override
  public CatalogMeta getCatalog(String name) throws NoSuchObjectException, TException {
    return new CatalogMeta();
  }

  @Override
  public List<String> getDatabases(String catalogName) throws NoSuchObjectException, TException {
    throw new UnsupportedOperationException("empty ams client can't load database meta");
  }

  @Override
  public void createDatabase(String catalogName, String database)
      throws NoSuchObjectException, AlreadyExistsException, TException {

  }

  @Override
  public void dropDatabase(String catalogName, String database)
      throws NoSuchObjectException, NotSupportedException, TException {

  }

  @Override
  public void createTableMeta(TableMeta tableMeta)
      throws AlreadyExistsException, InvalidObjectException, MetaException, TException {

  }

  @Override
  public List<TableMeta> listTables(String catalogName, String database) throws NoSuchObjectException, TException {
    throw new UnsupportedOperationException("empty ams client can't load table meta");
  }

  @Override
  public TableMeta getTable(TableIdentifier tableIdentifier) throws NoSuchObjectException, TException {
    throw new UnsupportedOperationException("empty ams client can't load table meta");
  }

  @Override
  public void removeTable(
      TableIdentifier tableIdentifier, boolean deleteData)
      throws NoSuchObjectException, MetaException, TException {

  }

  @Override
  public void tableCommit(TableCommitMeta commit) throws MetaException, TException {

  }

  @Override
  public long allocateTransactionId(
      TableIdentifier tableIdentifier, String transactionSignature) throws TException {
    return 0;
  }

  @Override
  public Blocker block(
      TableIdentifier tableIdentifier, List<BlockableOperation> operations, Map<String, String> properties)
      throws OperationConflictException, TException {
    throw new UnsupportedOperationException("empty ams client can't load block meta");
  }

  @Override
  public void releaseBlocker(TableIdentifier tableIdentifier, String blockerId) throws TException {

  }

  @Override
  public long renewBlocker(
      TableIdentifier tableIdentifier, String blockerId) throws NoSuchObjectException, TException {
    return 0;
  }

  @Override
  public List<Blocker> getBlockers(TableIdentifier tableIdentifier) throws TException {
    throw new UnsupportedOperationException("empty ams client can't load block meta");
  }
}

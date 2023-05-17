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

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.hive.CachedHiveClientPool;
import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.catalog.MixedHiveTables;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.persistence.mapper.CatalogMetaMapper;
import com.netease.arctic.server.persistence.mapper.TableBlockerMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableMetadata;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.thrift.TException;

import java.util.List;

public class MixedHiveCatalogImpl extends MixedCatalogImpl {

  private final MixedHiveTables mixedTables;
  private final CachedHiveClientPool hiveClientPool;

  protected MixedHiveCatalogImpl(CatalogMeta metadata) {
    super(metadata, new MixedHiveTables(metadata));
    this.mixedTables = (MixedHiveTables)tables();
    this.hiveClientPool = mixedTables.getHiveClientPool();
  }

  @Override
  public void createDatabase(String databaseName) {
    // do not support
  }

  @Override
  public void dropDatabase(String databaseName) {
    // do not support
  }

  @Override
  public ServerTableIdentifier createTable(TableMeta tableMeta) {
    validateTableIdentifier(tableMeta.getTableIdentifier());
    ServerTableIdentifier tableIdentifier = ServerTableIdentifier.of(tableMeta.getTableIdentifier());
    TableMetadata tableMetadata = new TableMetadata(tableIdentifier, tableMeta, getMetadata());
    doAsTransaction(
        () -> doAs(TableMetaMapper.class, mapper -> mapper.insertTable(tableIdentifier)),
        () -> doAs(TableMetaMapper.class, mapper -> mapper.insertTableMeta(tableMetadata)),
        () -> doAsExisted(
            CatalogMetaMapper.class,
            mapper -> mapper.incTableCount(1, name()),
            () -> new ObjectNotExistsException(name())));
    return getAs(
        TableMetaMapper.class,
        mapper -> mapper.selectTableIdentifier(tableMeta.getTableIdentifier().getCatalog(),
            tableMeta.getTableIdentifier().getDatabase(), tableMeta.getTableIdentifier().getTableName()));
  }

  @Override
  public ServerTableIdentifier dropTable(String databaseName, String tableName) {
    ServerTableIdentifier tableIdentifier = getAs(TableMetaMapper.class, mapper -> mapper
        .selectTableIdentifier(getMetadata().getCatalogName(), databaseName, tableName));
    if (tableIdentifier.getId() == null) {
      throw new ObjectNotExistsException(getTableDesc(databaseName, tableName));
    }
    doAsTransaction(
        () -> doAsExisted(
            TableMetaMapper.class,
            mapper -> mapper.deleteTableIdById(tableIdentifier.getId()),
            () -> new ObjectNotExistsException(getTableDesc(databaseName, tableName))),
        () -> doAs(TableMetaMapper.class, mapper -> mapper.deleteTableMetaById(tableIdentifier.getId())),
        () -> doAs(TableBlockerMapper.class, mapper -> mapper.deleteBlockers(tableIdentifier)),
        () -> dropTableInternal(databaseName, tableName),
        () -> doAsExisted(
            CatalogMetaMapper.class,
            mapper -> mapper.decTableCount(1, tableIdentifier.getCatalog()),
            () -> new ObjectNotExistsException(name())));
    return tableIdentifier;
  }

  @Override
  public boolean exist(String database) {
    try {
      return hiveClientPool.run(client -> {
        try {
          client.getDatabase(database);
          return true;
        } catch (NoSuchObjectException exception) {
          return false;
        }
      });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to get databases", e);
    }
  }

  @Override
  public List<String> listDatabases() {
    try {
      return hiveClientPool.run(HMSClient::getAllDatabases);
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to list databases", e);
    }
  }
}

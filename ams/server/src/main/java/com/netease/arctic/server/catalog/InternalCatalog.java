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

import com.netease.arctic.TableIDWithFormat;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.server.exception.AlreadyExistsException;
import com.netease.arctic.server.exception.IllegalMetadataException;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.persistence.mapper.CatalogMetaMapper;
import com.netease.arctic.server.persistence.mapper.TableBlockerMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.table.internal.InternalTableCreator;
import com.netease.arctic.server.table.internal.InternalTableHandler;
import org.apache.iceberg.rest.requests.CreateTableRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class InternalCatalog extends ServerCatalog {

  protected InternalCatalog(CatalogMeta metadata) {
    super(metadata);
  }

  @Override
  public List<String> listDatabases() {
    return getAs(
        TableMetaMapper.class, mapper -> mapper.selectDatabases(getMetadata().getCatalogName()));
  }

  public void createDatabase(String databaseName) {
    if (!exist(databaseName)) {
      doAsTransaction(
          // make sure catalog existed in database
          () ->
              doAsExisted(
                  CatalogMetaMapper.class,
                  mapper -> mapper.incDatabaseCount(1, name()),
                  () -> new ObjectNotExistsException("Catalog " + name())),
          () ->
              doAs(
                  TableMetaMapper.class,
                  mapper -> mapper.insertDatabase(getMetadata().getCatalogName(), databaseName)),
          () -> createDatabaseInternal(databaseName));
    } else {
      throw new AlreadyExistsException("Database " + databaseName);
    }
  }

  public void dropDatabase(String databaseName) {
    if (exist(databaseName)) {
      doAsTransaction(
          () ->
              doAsExisted(
                  TableMetaMapper.class,
                  mapper -> mapper.dropDb(getMetadata().getCatalogName(), databaseName),
                  () ->
                      new IllegalMetadataException(
                          "Database " + databaseName + " has more than one table")),
          () -> dropDatabaseInternal(databaseName),
          () ->
              doAsExisted(
                  CatalogMetaMapper.class,
                  mapper -> mapper.decDatabaseCount(1, name()),
                  () -> new ObjectNotExistsException(name())));
    } else {
      throw new ObjectNotExistsException("Database " + databaseName);
    }
  }

  @Override
  public List<TableIDWithFormat> listTables(String database) {
    return getAs(
            TableMetaMapper.class,
            mapper -> mapper.selectTableIdentifiersByDb(getMetadata().getCatalogName(), database))
        .stream()
        .map(
            sid ->
                TableIDWithFormat.of(
                    com.netease.arctic.table.TableIdentifier.of(sid.getIdentifier()),
                    sid.getFormat()))
        .collect(Collectors.toList());
  }

  @Override
  public List<TableIDWithFormat> listTables() {
    return getAs(
            TableMetaMapper.class,
            mapper -> mapper.selectTableIdentifiersByCatalog(getMetadata().getCatalogName()))
        .stream()
        .map(
            sid ->
                TableIDWithFormat.of(
                    com.netease.arctic.table.TableIdentifier.of(sid.getIdentifier()),
                    sid.getFormat()))
        .collect(Collectors.toList());
  }

  public abstract InternalTableCreator newTableCreator(
      String database, String tableName, TableFormat format, CreateTableRequest creatorArguments);

  public abstract <O> InternalTableHandler<O> newTableHandler(String database, String tableName);

  public TableMetadata createTable(TableMetadata tableMetadata) {
    validateTableIdentifier(tableMetadata.getTableIdentifier().getIdentifier());
    ServerTableIdentifier tableIdentifier = tableMetadata.getTableIdentifier();
    doAsTransaction(
        () -> doAs(TableMetaMapper.class, mapper -> mapper.insertTable(tableIdentifier)),
        () -> doAs(TableMetaMapper.class, mapper -> mapper.insertTableMeta(tableMetadata)),
        () ->
            doAsExisted(
                CatalogMetaMapper.class,
                mapper -> mapper.incTableCount(1, name()),
                () -> new ObjectNotExistsException(name())),
        () -> increaseDatabaseTableCount(tableIdentifier.getDatabase()));

    return getAs(
        TableMetaMapper.class,
        mapper ->
            mapper.selectTableMetaByName(
                tableIdentifier.getCatalog(),
                tableIdentifier.getDatabase(),
                tableIdentifier.getTableName()));
  }

  public ServerTableIdentifier dropTable(String databaseName, String tableName) {
    ServerTableIdentifier tableIdentifier =
        getAs(
            TableMetaMapper.class,
            mapper ->
                mapper.selectTableIdentifier(
                    getMetadata().getCatalogName(), databaseName, tableName));
    if (tableIdentifier.getId() == null) {
      throw new ObjectNotExistsException(getTableDesc(databaseName, tableName));
    }
    doAsTransaction(
        () ->
            doAsExisted(
                TableMetaMapper.class,
                mapper -> mapper.deleteTableIdById(tableIdentifier.getId()),
                () -> new ObjectNotExistsException(getTableDesc(databaseName, tableName))),
        () ->
            doAs(
                TableMetaMapper.class,
                mapper -> mapper.deleteTableMetaById(tableIdentifier.getId())),
        () -> doAs(TableBlockerMapper.class, mapper -> mapper.deleteBlockers(tableIdentifier)),
        () -> dropTableInternal(databaseName, tableName),
        () ->
            doAsExisted(
                CatalogMetaMapper.class,
                mapper -> mapper.decTableCount(1, tableIdentifier.getCatalog()),
                () -> new ObjectNotExistsException(name())),
        () -> decreaseDatabaseTableCount(tableIdentifier.getDatabase()));
    return tableIdentifier;
  }

  @Override
  public boolean exist(String database) {
    return getAs(
            TableMetaMapper.class,
            mapper -> mapper.selectDatabase(getMetadata().getCatalogName(), database))
        != null;
  }

  @Override
  public boolean exist(String database, String tableName) {
    ServerTableIdentifier tableIdentifier =
        getAs(
            TableMetaMapper.class,
            mapper ->
                mapper.selectTableIdentifier(getMetadata().getCatalogName(), database, tableName));
    return tableIdentifier != null
        && getAs(
                TableMetaMapper.class,
                mapper -> mapper.selectTableMetaById(tableIdentifier.getId()))
            != null;
  }

  public TableMetadata loadTableMetadata(String database, String table) {
    return Optional.ofNullable(
            getAs(
                TableMetaMapper.class,
                mapper -> mapper.selectTableMetaByName(name(), database, table)))
        .orElseThrow(
            () ->
                new ObjectNotExistsException(
                    com.netease.arctic.table.TableIdentifier.of(name(), database, table)
                        .toString()));
  }

  private String getDatabaseDesc(String database) {
    return name() + '.' + database;
  }

  protected String getTableDesc(String database, String tableName) {
    return name() + '.' + database + '.' + tableName;
  }

  public Integer getTableCount() {
    return getAs(CatalogMetaMapper.class, mapper -> mapper.selectTableCount(name()));
  }

  public Integer getTableCount(String databaseName) {
    return getAs(TableMetaMapper.class, mapper -> mapper.selectTableCount(name()));
  }

  protected void decreaseDatabaseTableCount(String databaseName) {
    doAsExisted(
        TableMetaMapper.class,
        mapper -> mapper.decTableCount(1, databaseName),
        () -> new ObjectNotExistsException(getDatabaseDesc(databaseName)));
  }

  protected void increaseDatabaseTableCount(String databaseName) {
    doAsExisted(
        TableMetaMapper.class,
        mapper -> mapper.incTableCount(1, databaseName),
        () -> new ObjectNotExistsException(getDatabaseDesc(databaseName)));
  }

  protected void createDatabaseInternal(String databaseName) {
    // do nothing, create internal table default done on client side
  }

  protected void dropTableInternal(String databaseName, String tableName) {
    // do nothing, create internal table default done on client side
  }

  protected void dropDatabaseInternal(String databaseName) {
    // do nothing, create internal table default done on client side
  }

  protected void validateTableIdentifier(TableIdentifier tableIdentifier) {
    if (!name().equals(tableIdentifier.getCatalog())) {
      throw new IllegalMetadataException("Catalog name is error in table identifier");
    }
  }
}

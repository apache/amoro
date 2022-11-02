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

package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.api.InvalidObjectException;
import com.netease.arctic.ams.api.MetaException;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.server.config.ServerTableProperties;
import com.netease.arctic.ams.server.mapper.DatabaseMetadataMapper;
import com.netease.arctic.ams.server.mapper.TableMetadataMapper;
import com.netease.arctic.ams.server.model.OptimizeQueueItem;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.optimize.TableOptimizeItem;
import com.netease.arctic.ams.server.service.IInternalTableService;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.IMetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.utils.PropertiesUtil;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticHadoopFileIO;
import com.netease.arctic.table.BaseUnkeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.Table;
import org.apache.iceberg.Tables;
import org.apache.iceberg.hadoop.HadoopTables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class JDBCMetaService extends IJDBCService implements IMetaService {
  public static final Logger LOG = LoggerFactory.getLogger(JDBCMetaService.class);
  public static final Map<Key, TableMetaStore> TABLE_META_STORE_CACHE = new ConcurrentHashMap<>();
  private final FileInfoCacheService fileInfoCacheService;
  private final ArcticTransactionService transactionService;
  private final DDLTracerService ddlTracerService;

  public JDBCMetaService() {
    super();
    this.fileInfoCacheService = ServiceContainer.getFileInfoCacheService();
    this.transactionService = ServiceContainer.getArcticTransactionService();
    this.ddlTracerService = ServiceContainer.getDdlTracerService();
  }

  @Override
  public void createTable(TableMetadata tableMetadata) throws MetaException {
    try (SqlSession sqlSession = getSqlSession(false)) {
      try {
        TableMetadataMapper tableMetadataMapper = getMapper(sqlSession, TableMetadataMapper.class);
        tableMetadataMapper.createTableMeta(tableMetadata);
        tableMetadata = tableMetadataMapper.loadTableMeta(tableMetadata.getTableIdentifier());
      } catch (Exception e) {
        sqlSession.rollback(true);
        throw e;
      }
      sqlSession.commit(true);
    }

    buildArcticTable(tableMetadata);
    TABLE_META_STORE_CACHE.put(new Key(tableMetadata.getTableIdentifier(), tableMetadata.getMetaStore()),
        tableMetadata.getMetaStore());
    try {
      ServiceContainer.getOptimizeService().listCachedTables(true);
    } catch (Exception e) {
      LOG.warn("createTable success but failed to refresh optimize table cache", e);
    }
  }

  @Override
  public TableMetadata loadTableMetadata(TableIdentifier tableIdentifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetadataMapper tableMetadataMapper = getMapper(sqlSession, TableMetadataMapper.class);
      TableMetadata tableMetadata = tableMetadataMapper.loadTableMeta(tableIdentifier);
      if (tableMetadata == null) {
        return null;
      }
      TableMetaStore existTableMetastore =
          TABLE_META_STORE_CACHE.putIfAbsent(new Key(tableMetadata.getTableIdentifier(), tableMetadata.getMetaStore()),
              tableMetadata.getMetaStore());
      if (existTableMetastore != null) {
        tableMetadata.setMetaStore(existTableMetastore);
      } else {
        LOG.info("{} build new TableMetaStore", tableMetadata.getTableIdentifier());
      }
      return tableMetadata;
    }
  }

  @Override
  public void dropTableMetadata(TableIdentifier tableIdentifier,
                                IInternalTableService internalTableService,
                                boolean deleteData) throws MetaException {
    try (SqlSession sqlSession = getSqlSession(false)) {
      TableMetadataMapper tableMetadataMapper = getMapper(sqlSession, TableMetadataMapper.class);
      TableMetadata tableMetadata = tableMetadataMapper.loadTableMeta(tableIdentifier);
      try {
        tableMetadataMapper.deleteTableMeta(tableIdentifier);

        if (internalTableService != null) {
          internalTableService.dropTable(tableMetadata.getMetaStore(),
              tableMetadata.getBaseLocation(),
              false);
          if (StringUtils.isNotBlank(tableMetadata.getPrimaryKey())) {
            internalTableService.dropTable(tableMetadata.getMetaStore(),
                tableMetadata.getChangeLocation(),
                false);
          }
        }

        fileInfoCacheService.deleteTableCache(tableIdentifier);
        transactionService.delete(tableIdentifier.buildTableIdentifier());
        ddlTracerService.dropTableData(tableIdentifier.buildTableIdentifier());
      } catch (Exception e) {
        LOG.error("The internal table service drop table failed.");
        sqlSession.rollback(true);
        throw e;
      }
      sqlSession.commit(true);
    }
  }

  @Override
  public void updateTableProperties(TableIdentifier tableIdentifier, Map<String, String> properties) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetadataMapper tableMetadataMapper = getMapper(sqlSession, TableMetadataMapper.class);
      PropertiesUtil.removeHiddenProperties(properties, ServerTableProperties.HIDDEN_INTERNAL);
      TableMetadata oldTableMetaData = loadTableMetadata(tableIdentifier);
      ServiceContainer.getDdlTracerService().commitProperties(tableIdentifier.buildTableIdentifier(),
          oldTableMetaData.getProperties(),
          properties);
      tableMetadataMapper.updateTableProperties(tableIdentifier, properties);
      String oldQueueName = oldTableMetaData.getProperties().getOrDefault(TableProperties.OPTIMIZE_GROUP,
          TableProperties.OPTIMIZE_GROUP_DEFAULT);
      String newQueueName =
          properties.getOrDefault(TableProperties.OPTIMIZE_GROUP, TableProperties.OPTIMIZE_GROUP_DEFAULT);
      if (StringUtils.isNotBlank(oldQueueName) && StringUtils.isNotBlank(newQueueName) && !oldQueueName.equals(
          newQueueName)) {
        OptimizeQueueItem newOptimizeQueue = ServiceContainer.getOptimizeQueueService().getOptimizeQueue(newQueueName);
        TableOptimizeItem arcticTableItem = ServiceContainer.getOptimizeService().getTableOptimizeItem(tableIdentifier);
        ServiceContainer.getOptimizeQueueService().release(tableIdentifier);
        try {
          arcticTableItem.clearOptimizeTasks();
        } catch (Throwable t) {
          LOG.error("failed to delete " + tableIdentifier + " compact task, ignore", t);
        }
        ServiceContainer.getOptimizeQueueService().bind(arcticTableItem.getTableIdentifier(),
            newOptimizeQueue.getOptimizeQueueMeta().getQueueId());
      }
    } catch (InvalidObjectException | NoSuchObjectException e) {
      LOG.error("get tables failed " + tableIdentifier, e);
    }
  }

  @Override
  public List<String> listDatabases(String catalogName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      DatabaseMetadataMapper dbMapper = getMapper(sqlSession, DatabaseMetadataMapper.class);
      return dbMapper.listDb(catalogName);
    }
  }

  @Override
  public void createDatabase(String catalogName, String dbName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      DatabaseMetadataMapper dbMapper = getMapper(sqlSession, DatabaseMetadataMapper.class);
      dbMapper.insertDb(catalogName, dbName);
    }
  }

  @Override
  public void dropDatabase(String catalogName, String dbName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      DatabaseMetadataMapper dbMapper = getMapper(sqlSession, DatabaseMetadataMapper.class);
      dbMapper.dropDb(catalogName, dbName);
    }
  }

  @Override
  public List<TableMetadata> listTables() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetadataMapper tableMetadataMapper = getMapper(sqlSession, TableMetadataMapper.class);
      return tableMetadataMapper.listTableMetas();
    }
  }

  @Override
  public List<TableMetadata> getTables(String catalogName, String database) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetadataMapper tableMetadataMapper = getMapper(sqlSession, TableMetadataMapper.class);
      return tableMetadataMapper.getTableMetas(catalogName, database);
    }
  }

  @Override
  public boolean isExist(TableIdentifier tableIdentifier) {
    return loadTableMetadata(tableIdentifier) != null;
  }

  @Override
  public UnkeyedTable buildArcticTable(TableMetadata tableMetadata) {
    Tables tables = new HadoopTables(tableMetadata.getMetaStore().getConfiguration());
    Table icebergTable = tableMetadata.getMetaStore().doAs(()
        -> tables.load(tableMetadata.getBaseLocation()));
    ArcticFileIO fileIO = new ArcticHadoopFileIO(tableMetadata.getMetaStore());
    return new BaseUnkeyedTable(tableMetadata.getTableIdentifier(), icebergTable, fileIO);
  }

  public static class Key {
    private final TableIdentifier tableIdentifier;
    private final TableMetaStore tableMetaStore;

    public Key(TableIdentifier tableIdentifier, TableMetaStore tableMetaStore) {
      this.tableIdentifier = tableIdentifier;
      this.tableMetaStore = tableMetaStore;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Key key = (Key) o;
      return Objects.equals(tableIdentifier, key.tableIdentifier) && Objects.equals(tableMetaStore,
          key.tableMetaStore);
    }

    @Override
    public int hashCode() {
      return Objects.hash(tableIdentifier, tableMetaStore);
    }
  }
}
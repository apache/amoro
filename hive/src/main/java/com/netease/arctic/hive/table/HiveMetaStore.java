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

package com.netease.arctic.hive.table;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.netease.arctic.hive.ArcticHiveClientPool;
import com.netease.arctic.hive.catalog.ArcticHiveCatalog;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.PrimaryKeysRequest;
import org.apache.hadoop.hive.metastore.api.SQLPrimaryKey;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.iceberg.ClientPool;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class HiveMetaStore implements Closeable {
  private static Logger LOG = LoggerFactory.getLogger(HiveMetaStore.class);
  public static final int HIVE_CLIENT_POOL_SIZE = 10;
  private static final LoadingCache<ArcticHiveCatalog, HiveMetaStore> META_STORE_CACHE = Caffeine.newBuilder()
      .build(HiveMetaStore::buildHiveMetaStore);
  public static final String PARQUET_INPUT_FORMAT = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat";
  public static final String PARQUET_OUTPUT_FORMAT = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat";
  public static final String PARQUET_HIVE_SERDE = "org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe";

  private final ArcticHiveClientPool clients;

  public static HiveMetaStore getHiveMetaStore(ArcticHiveCatalog arcticHiveCatalog) {
    Preconditions.checkNotNull(arcticHiveCatalog);
    return META_STORE_CACHE.get(arcticHiveCatalog);
  }

  private HiveMetaStore(TableMetaStore tableMetaStore, int clientPoolSize) {
    tableMetaStore.getHiveSiteLocation().ifPresent(HiveConf::setHiveSiteLocation);
    this.clients = new ArcticHiveClientPool(tableMetaStore, clientPoolSize);
  }

  public List<String> getAllHiveTables(String database) {
    try {
      return clients.run(client -> client.getAllTables(database));
    } catch (MetaException e) {
      reGetAllHiveTables(e, database);
    } catch (TException e) {
      throw new RuntimeException("Failed to get tables of database " + database, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to getAllTables", e);
    }
    throw new RuntimeException("Failed to get tables of database " + database);
  }

  private void reGetAllHiveTables(MetaException e, String database) {
    if (e.getMessage().contains("Got exception: org.apache.thrift.transport.TTransportException")) {
      try {
        clients.run(client -> {
          client.close();
          client.reconnect();
          return client.getAllTables(database);
        });
      } catch (TException ex) {
        throw new RuntimeException("Failed to get tables of database " + database, e);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Interrupted in call to getAllTables", e);
      }
    } else {
      throw new RuntimeException("Failed to get tables of database " + database, e);
    }
  }

  public static Table buildHiveTable(String database, String tableName, List<FieldSchema> columns,
                                     List<FieldSchema> partitionCols, String owner) {
    Table table = new Table();
    table.setDbName(database);
    table.setTableName(tableName);
    table.setPartitionKeys(partitionCols);
    table.setOwner(owner);

    table.setSd(new StorageDescriptor());
    table.getSd().setCols(columns);
    table.getSd().setInputFormat(PARQUET_INPUT_FORMAT);
    table.getSd().setOutputFormat(PARQUET_OUTPUT_FORMAT);
    table.getSd().setSerdeInfo(new SerDeInfo());
    table.getSd().getSerdeInfo().setSerializationLib(PARQUET_HIVE_SERDE);
    table.getSd().getSerdeInfo().putToParameters(serdeConstants.SERIALIZATION_FORMAT, "1");
    return table;
  }

  public void dropHiveTable(String database, String tableName) {
    try {
      clients.run(client -> {
        client.dropTable(database, tableName);
        return null;
      });
    } catch (NoSuchObjectException e) {
      throw new RuntimeException("No such table: " + database + "." + tableName);
    } catch (InterruptedException | TException e) {
      throw new RuntimeException("Drop hive table error: " + e);
    }
  }

  public HiveTable getHiveTable(TableIdentifier identifier) {
    String database = identifier.getDatabase();
    String name = identifier.getTableName();
    Table table;
    try {
      table = clients.run(client -> client.getTable(database, name));
      HiveTable hiveTable = HiveTable.of(table);
      return hiveTable;
    } catch (NoSuchObjectException e) {
      throw new NoSuchTableException(e, "Hive table does not exist: %s", name);
    } catch (TException e) {
      throw new RuntimeException("Failed to get table " + name, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to rename", e);
    }
  }

  public boolean checkExist(TableIdentifier identifier) {
    String database = identifier.getDatabase();
    String name = identifier.getTableName();
    try {
      clients.run(client -> client.getTable(database, name));
      return true;
    } catch (NoSuchObjectException e) {
      return false;
    } catch (TException e) {
      throw new RuntimeException("Failed to get table " + name, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to rename", e);
    }
  }

  public List<SQLPrimaryKey> getPrimaryKeys(TableIdentifier identifier) {
    List<SQLPrimaryKey> primaryKeys = null;
    PrimaryKeysRequest request = new PrimaryKeysRequest(identifier.getDatabase(), identifier.getTableName());
    try {
      primaryKeys = clients.run(client -> client.getPrimaryKeys(request));
    } catch (TException e) {
      throw new RuntimeException("Failed to get table primary keys: " + identifier.toString(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to rename", e);
    }
    return primaryKeys;
  }

  public <R> R run(ClientPool.Action<R, HiveMetaStoreClient, TException> action)
      throws TException, InterruptedException {
    return clients.run(action);
  }

  public void addHiveParameters(TableIdentifier tableIdentifier, Map<String, String> parameters) {
    HiveTable hiveTable = getHiveTable(tableIdentifier);
    Map<String, String> hiveParameters = hiveTable.getTable().getParameters();
    if (hiveParameters == null) {
      hiveParameters = Maps.newHashMap();
    }
    hiveParameters.putAll(parameters);
    hiveTable.getTable().setParameters(hiveParameters);
    try {
      run(client -> {
        client.alter_table(
            hiveTable.getDatabase(), hiveTable.getTableName(),
            hiveTable.getTable());
        return null;
      });
    } catch (TException e) {
      throw new RuntimeException(
          "Failed to update parameters of hive table: " +
              hiveTable.getDatabase() + "." + hiveTable.getTableName(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in update parameters of hive table", e);
    }
  }

  public List<Partition> getHiveAllPartitions(TableIdentifier tableIdentifier) {
    try {
      return clients.run(client ->
          client.listPartitions(tableIdentifier.getDatabase(), tableIdentifier.getTableName(), Short.MAX_VALUE));
    } catch (NoSuchObjectException e) {
      throw new NoSuchTableException(e, "Hive table does not exist: %s", tableIdentifier.getTableName());
    } catch (TException e) {
      throw new RuntimeException("Failed to get partitions " + tableIdentifier.getTableName(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to listPartitions", e);
    }
  }

  @Override
  public void close() {
    clients.close();
  }

  private static HiveMetaStore buildHiveMetaStore(ArcticHiveCatalog arcticHiveCatalog) throws MalformedURLException {
    LOG.info("build a new HiveMetaStore with catalog {}", arcticHiveCatalog.name());
    arcticHiveCatalog.getTableMetaStore().getHiveSiteLocation().ifPresent(HiveConf::setHiveSiteLocation);
    return new HiveMetaStore(arcticHiveCatalog.getTableMetaStore(), HIVE_CLIENT_POOL_SIZE);
  }
}

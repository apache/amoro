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

package org.apache.amoro.hive;

import org.apache.amoro.utils.DynMethods;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.PartitionDropOptions;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.EnvironmentContext;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.TableMeta;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class HMSClientImpl implements HMSClient {

  private static final Logger LOG = LoggerFactory.getLogger(HMSClientImpl.class);

  private final HiveMetaStoreClient client;

  public HMSClientImpl(HiveMetaStoreClient client) {
    this.client = client;
  }

  public HiveMetaStoreClient getClient() {
    return this.client;
  }

  @Override
  public void close() {
    getClient().close();
  }

  @Override
  public void reconnect() throws MetaException {
    getClient().reconnect();
  }

  @Override
  public List<String> getAllDatabases() throws TException {
    return getClient().getAllDatabases();
  }

  @Override
  public Partition getPartition(String dbName, String tblName, List<String> partVals)
      throws TException {
    return getClient().getPartition(dbName, tblName, partVals);
  }

  @Override
  public Partition getPartition(String dbName, String tblName, String name) throws TException {
    return getClient().getPartition(dbName, tblName, name);
  }

  @Override
  public Table getTable(String dbName, String tableName) throws TException {
    return getClient().getTable(dbName, tableName);
  }

  @Override
  public void alterTable(String defaultDatabaseName, String tblName, Table table)
      throws TException {
    getClient().alter_table(defaultDatabaseName, tblName, table);
  }

  @Override
  public List<Partition> listPartitions(String dbName, String tblName, short maxParts)
      throws TException {
    return getClient().listPartitions(dbName, tblName, maxParts);
  }

  @Override
  public List<Partition> listPartitions(
      String dbName, String tblName, List<String> partVals, short maxParts) throws TException {
    return getClient().listPartitions(dbName, tblName, partVals, maxParts);
  }

  @Override
  public List<String> listPartitionNames(String dbName, String tblName, short maxParts)
      throws TException {
    return getClient().listPartitionNames(dbName, tblName, maxParts);
  }

  @Override
  public void createDatabase(Database db) throws TException {
    getClient().createDatabase(db);
  }

  @Override
  public void dropDatabase(
      String name, boolean deleteData, boolean ignoreUnknownDb, boolean cascade) throws TException {
    getClient().dropDatabase(name, deleteData, ignoreUnknownDb, cascade);
  }

  @Override
  public void dropTable(
      String dbname, String tableName, boolean deleteData, boolean ignoreUnknownTab)
      throws TException {
    getClient().dropTable(dbname, tableName, deleteData, ignoreUnknownTab);
  }

  @Override
  public void createTable(Table tbl) throws TException {
    getClient().createTable(tbl);
  }

  @Override
  public Database getDatabase(String databaseName) throws TException {
    return getClient().getDatabase(databaseName);
  }

  @Override
  public Partition addPartition(Partition partition) throws TException {
    Partition added = getClient().add_partition(partition);
    LOG.info("Added Hive partition {}", describePartition(partition));
    return added;
  }

  @Override
  public boolean dropPartition(
      String dbName, String tblName, List<String> partVals, PartitionDropOptions options)
      throws TException {
    boolean dropped = getClient().dropPartition(dbName, tblName, partVals, options);
    if (dropped) {
      LOG.info("Dropped Hive partition {}.{} values={}", dbName, tblName, partVals);
    }
    return dropped;
  }

  @Override
  public int addPartitions(List<Partition> partitions) throws TException {
    int added = getClient().add_partitions(partitions);
    if (LOG.isInfoEnabled() && added > 0) {
      LOG.info(
          "Added {} Hive partitions; requested partitions: {}",
          added,
          describePartitions(partitions));
    }
    return added;
  }

  @Override
  public List<String> getAllTables(String dbName) throws TException {
    return getClient().getAllTables(dbName);
  }

  @Override
  public void alterPartitions(
      String dbName,
      String tblName,
      List<Partition> newParts,
      EnvironmentContext environmentContext) {
    DynMethods.UnboundMethod alterPartitions =
        DynMethods.builder("alter_partitions")
            .impl(
                HiveMetaStoreClient.class,
                String.class,
                String.class,
                List.class,
                EnvironmentContext.class)
            .impl(HiveMetaStoreClient.class, String.class, String.class, List.class)
            .build();
    alterPartitions.invoke(getClient(), dbName, tblName, newParts, environmentContext);
    if (LOG.isInfoEnabled() && !newParts.isEmpty()) {
      LOG.info(
          "Altered Hive partitions for {}.{}: {}", dbName, tblName, describePartitions(newParts));
    }
  }

  @Override
  public void alterPartition(
      String dbName, String tblName, Partition newPart, EnvironmentContext environmentContext) {
    DynMethods.UnboundMethod alterPartition =
        DynMethods.builder("alter_partition")
            .impl(
                HiveMetaStoreClient.class,
                String.class,
                String.class,
                Partition.class,
                EnvironmentContext.class)
            .impl(HiveMetaStoreClient.class, String.class, String.class, Partition.class)
            .build();
    alterPartition.invoke(getClient(), dbName, tblName, newPart, environmentContext);
    LOG.info("Altered Hive partition for {}.{}: {}", dbName, tblName, describePartition(newPart));
  }

  private static String describePartitions(List<Partition> partitions) {
    return partitions.stream()
        .map(HMSClientImpl::describePartition)
        .collect(Collectors.joining(", "));
  }

  private static String describePartition(Partition partition) {
    return String.format(
        "%s.%s values=%s location=%s",
        partition.getDbName(),
        partition.getTableName(),
        partition.getValues(),
        partition.isSetSd() ? partition.getSd().getLocation() : null);
  }

  @Override
  public List<Table> getTableObjectsByName(String dbName, List<String> tableNames)
      throws TException {
    return getClient().getTableObjectsByName(dbName, tableNames);
  }

  @Override
  public List<TableMeta> getTableMeta(
      String databasePattern, String tablePattern, List<String> tableTypes) throws TException {
    return getClient().getTableMeta(databasePattern, tablePattern, tableTypes);
  }
}

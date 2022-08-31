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

package com.netease.arctic.hive.utils;

import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.ClientPool;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class HiveTableUtil {

  private static final Logger LOG = LoggerFactory.getLogger(HiveTableUtil.class);

  public static boolean isHive(ArcticTable arcticTable) {
    return arcticTable instanceof SupportHive;
  }

  public static org.apache.hadoop.hive.metastore.api.Table loadHmsTable(
      HMSClient hiveClient, TableIdentifier tableIdentifier) {
    try {
      return hiveClient.run(client -> client.getTable(
          tableIdentifier.getDatabase(),
          tableIdentifier.getTableName()));
    } catch (NoSuchObjectException nte) {
      LOG.trace("Table not found {}", tableIdentifier.toString(), nte);
      return null;
    } catch (TException e) {
      throw new RuntimeException(String.format("Metastore operation failed for %s", tableIdentifier.toString()), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted during commit", e);
    }
  }

  public static void persistTable(HMSClient hiveClient, org.apache.hadoop.hive.metastore.api.Table tbl) {
    try {
      hiveClient.run(client -> {
        client.alter_table(tbl.getDbName(), tbl.getTableName(), tbl);
        return null;
      });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, String> generateTableProperties(int accessTimeInSeconds, List<DataFile> files) {
    Map<String, String> properties = Maps.newHashMap();
    long totalSize = files.stream().map(DataFile::fileSizeInBytes).reduce(0L, Long::sum);
    long numRows = files.stream().map(DataFile::recordCount).reduce(0L, Long::sum);
    properties.put("transient_lastDdlTime", accessTimeInSeconds + "");
    properties.put("totalSize", totalSize + "");
    properties.put("numRows", numRows + "");
    properties.put("numFiles", files.size() + "");
    properties.put(HiveTableProperties.ARCTIC_TABLE_FLAG, "true");
    return properties;
  }

  public static String hiveRootLocation(String tableLocation) {
    return tableLocation + "/hive";
  }

  public static String newKeyedHiveDataLocation(String hiveLocation, PartitionSpec partitionSpec,
                                                StructLike partitionData, Long transactionId) {
    if (partitionSpec.isUnpartitioned()) {
      return String.format("%s/%s", hiveLocation, "txid=" + transactionId);
    } else {
      return String.format("%s/%s/%s", hiveLocation, partitionSpec.partitionToPath(partitionData),
          "txid=" + transactionId);
    }
  }

  public static String newUnKeyedHiveDataLocation(String hiveLocation, PartitionSpec partitionSpec,
                                                  StructLike partitionData, String subDir) {
    if (partitionSpec.isUnpartitioned()) {
      return String.format("%s/%s", hiveLocation, subDir);
    } else {
      return String.format("%s/%s/%s", hiveLocation, partitionSpec.partitionToPath(partitionData), subDir);
    }
  }

  public static String getRandomSubDir() {
    return System.currentTimeMillis() + "_" + UUID.randomUUID();
  }

  public static StorageDescriptor storageDescriptor(
      Schema schema, PartitionSpec partitionSpec, String location,
      FileFormat format) {
    final StorageDescriptor storageDescriptor = new StorageDescriptor();
    storageDescriptor.setCols(HiveSchemaUtil.hiveTableFields(schema, partitionSpec));
    storageDescriptor.setLocation(location);
    SerDeInfo serDeInfo = new SerDeInfo();
    switch (format) {
      case PARQUET:
        storageDescriptor.setOutputFormat("org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat");
        storageDescriptor.setInputFormat("org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat");
        serDeInfo.setSerializationLib("org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe");
        break;
      default:
        throw new IllegalArgumentException("Unsupported hive table file format:" + format);
    }

    storageDescriptor.setSerdeInfo(serDeInfo);
    return storageDescriptor;
  }

  public List<Partition> getHiveAllPartitions(HMSClient hiveClient, TableIdentifier tableIdentifier) {
    try {
      return hiveClient.run(client ->
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

  public static List<String> getHivePartitionNames(HMSClient hiveClient, TableIdentifier tableIdentifier) {
    try {
      return hiveClient.run(client -> client.listPartitionNames(tableIdentifier.getDatabase(),
          tableIdentifier.getTableName(),
          Short.MAX_VALUE)).stream().collect(Collectors.toList());
    } catch (NoSuchObjectException e) {
      throw new NoSuchTableException(e, "Hive table does not exist: %s", tableIdentifier.getTableName());
    } catch (TException e) {
      throw new RuntimeException("Failed to get partitions " + tableIdentifier.getTableName(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to listPartitions", e);
    }
  }

  public static List<String> getHivePartitionLocations(HMSClient hiveClient,
                                                       TableIdentifier tableIdentifier) {
    try {
      return hiveClient.run(client -> client.listPartitions(tableIdentifier.getDatabase(),
          tableIdentifier.getTableName(),
          Short.MAX_VALUE))
          .stream()
          .map(partition -> partition.getSd().getLocation())
          .collect(Collectors.toList());
    } catch (NoSuchObjectException e) {
      throw new NoSuchTableException(e, "Hive table does not exist: %s", tableIdentifier.getTableName());
    } catch (TException e) {
      throw new RuntimeException("Failed to get partitions " + tableIdentifier.getTableName(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to listPartitions", e);
    }
  }

  public static void alterPartition(HMSClient hiveClient, TableIdentifier tableIdentifier,
                                    String partition, String newPath) throws IOException {
    try {
      LOG.info("alter table {} hive partition {} to new location {}",
          tableIdentifier, partition, newPath);
      Partition oldPartition = hiveClient.run(
          client -> client.getPartition(
              tableIdentifier.getDatabase(),
              tableIdentifier.getTableName(),
              partition));
      Partition newPartition = new Partition(oldPartition);
      newPartition.getSd().setLocation(newPath);
      hiveClient.run((ClientPool.Action<Void, HiveMetaStoreClient, TException>) client -> {
        client.alter_partition(tableIdentifier.getDatabase(),
            tableIdentifier.getTableName(),
            newPartition, null);
        return null;
      });
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public boolean checkExist(HMSClient hiveClient, TableIdentifier identifier) {
    String database = identifier.getDatabase();
    String name = identifier.getTableName();
    try {
      hiveClient.run(client -> client.getTable(database, name));
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

  public static List<String> getAllHiveTables(HMSClient hiveClient, String database) {
    try {
      return hiveClient.run(client -> client.getAllTables(database));
    } catch (MetaException e) {
      reGetAllHiveTables(hiveClient, e, database);
    } catch (TException e) {
      throw new RuntimeException("Failed to get tables of database " + database, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to getAllTables", e);
    }
    throw new RuntimeException("Failed to get tables of database " + database);
  }

  private static void reGetAllHiveTables(HMSClient hiveClient, MetaException e, String database) {
    if (e.getMessage().contains("Got exception: org.apache.thrift.transport.TTransportException")) {
      try {
        hiveClient.run(client -> {
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

  public static void alterTableLocation(HMSClient hiveClient, TableIdentifier tableIdentifier,
                                        String newPath) throws IOException {
    try {
      hiveClient.run(client -> {
        Table newTable = loadHmsTable(hiveClient, tableIdentifier);
        newTable.getSd().setLocation(newPath);
        client.alter_table(tableIdentifier.getDatabase(), tableIdentifier.getTableName(), newTable);
        return null;
      });
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}

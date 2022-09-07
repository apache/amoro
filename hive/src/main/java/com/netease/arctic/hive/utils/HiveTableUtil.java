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
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        storageDescriptor.setOutputFormat(HiveTableProperties.PARQUET_OUTPUT_FORMAT);
        storageDescriptor.setInputFormat(HiveTableProperties.PARQUET_INPUT_FORMAT);
        serDeInfo.setSerializationLib(HiveTableProperties.PARQUET_ROW_FORMAT_SERDE);
        break;
      default:
        throw new IllegalArgumentException("Unsupported hive table file format:" + format);
    }

    storageDescriptor.setSerdeInfo(serDeInfo);
    return storageDescriptor;
  }

  /**
   * Check whether the table is in Hive.
   *
   * @param hiveClient Hive client from ArcticHiveCatalog
   * @param tableIdentifier A table identifier
   * @return If table is existed in hive
   */
  public boolean checkExist(HMSClient hiveClient, TableIdentifier tableIdentifier) {
    String database = tableIdentifier.getDatabase();
    String name = tableIdentifier.getTableName();
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

  /**
   * Gets all the tables in a database.
   *
   * @param hiveClient Hive client from ArcticHiveCatalog
   * @param database Hive database
   * @return A List of table-names from hive database
   */
  public static List<String> getAllHiveTables(HMSClient hiveClient, String database) {
    try {
      return hiveClient.run(client -> client.getAllTables(database));
    } catch (TException e) {
      throw new RuntimeException("Failed to get tables of database " + database, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to getAllTables", e);
    }
  }

  /**
   * Change the location of the Hive table.
   *
   * @param hiveClient Hive client from ArcticHiveCatalog
   * @param tableIdentifier A table identifier
   */
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

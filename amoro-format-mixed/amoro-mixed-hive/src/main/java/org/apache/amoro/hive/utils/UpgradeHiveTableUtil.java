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

package org.apache.amoro.hive.utils;

import static org.apache.amoro.table.TableProperties.BASE_FILE_FORMAT;
import static org.apache.amoro.table.TableProperties.CHANGE_FILE_FORMAT;
import static org.apache.amoro.table.TableProperties.DEFAULT_FILE_FORMAT;
import static org.apache.amoro.table.TableProperties.FILE_FORMAT_ORC;
import static org.apache.amoro.table.TableProperties.FILE_FORMAT_PARQUET;

import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.catalog.MixedHiveCatalog;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.hive.table.UnkeyedHiveTable;
import org.apache.amoro.io.AuthenticatedHadoopFileIO;
import org.apache.amoro.op.UpdatePartitionProperties;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpgradeHiveTableUtil {

  private static final Logger LOG = LoggerFactory.getLogger(UpgradeHiveTableUtil.class);

  private static final long DEFAULT_TXID = 0L;

  /**
   * Upgrade a hive table to an mixed-hive table.
   *
   * @param mixedHiveCatalog A mixed-hive catalog
   * @param tableIdentifier A table identifier
   * @param pkList The name of the columns that needs to be set as the primary key
   * @param properties Properties to be added to the target table
   */
  public static void upgradeHiveTable(
      MixedHiveCatalog mixedHiveCatalog,
      TableIdentifier tableIdentifier,
      List<String> pkList,
      Map<String, String> properties)
      throws Exception {
    if (!formatCheck(mixedHiveCatalog.getHMSClient(), tableIdentifier, properties)) {
      throw new IllegalArgumentException("The storage format is not support");
    }
    boolean upgradeHive = false;
    try {
      Table hiveTable =
          HiveTableUtil.loadHmsTable(mixedHiveCatalog.getHMSClient(), tableIdentifier);

      Schema schema = HiveSchemaUtil.convertHiveSchemaToIcebergSchema(hiveTable, pkList);

      List<FieldSchema> partitionKeys = hiveTable.getPartitionKeys();

      PartitionSpec.Builder partitionBuilder = PartitionSpec.builderFor(schema);
      partitionKeys.stream().forEach(p -> partitionBuilder.identity(p.getName()));

      PrimaryKeySpec.Builder primaryKeyBuilder = PrimaryKeySpec.builderFor(schema);
      pkList.stream().forEach(p -> primaryKeyBuilder.addColumn(p));

      MixedTable mixedTable =
          mixedHiveCatalog
              .newTableBuilder(tableIdentifier, schema)
              .withProperties(properties)
              .withPartitionSpec(partitionBuilder.build())
              .withPrimaryKeySpec(primaryKeyBuilder.build())
              .withProperty(HiveTableProperties.ALLOW_HIVE_TABLE_EXISTED, "true")
              .create();
      upgradeHive = true;
      UpgradeHiveTableUtil.hiveDataMigration(
          (SupportHive) mixedTable, mixedHiveCatalog, tableIdentifier);
    } catch (Throwable t) {
      if (upgradeHive) {
        mixedHiveCatalog.dropTable(tableIdentifier, false);
      }
      throw t;
    }
  }

  private static void hiveDataMigration(
      SupportHive mixedHiveTable,
      MixedHiveCatalog mixedHiveCatalog,
      TableIdentifier tableIdentifier)
      throws Exception {
    Table hiveTable = HiveTableUtil.loadHmsTable(mixedHiveCatalog.getHMSClient(), tableIdentifier);
    String hiveDataLocation = HiveTableUtil.hiveRootLocation(hiveTable.getSd().getLocation());
    AuthenticatedHadoopFileIO io = mixedHiveTable.io();
    io.makeDirectories(hiveDataLocation);
    String newPath;
    if (hiveTable.getPartitionKeys().isEmpty()) {
      newPath = hiveDataLocation + "/" + System.currentTimeMillis() + "_" + UUID.randomUUID();
      io.makeDirectories(newPath);
      io.listDirectory(hiveTable.getSd().getLocation())
          .forEach(
              p -> {
                if (!p.isDirectory()) {
                  io.asFileSystemIO().rename(p.location(), newPath);
                }
              });

      try {
        HiveTableUtil.alterTableLocation(
            mixedHiveCatalog.getHMSClient(), mixedHiveTable.id(), newPath);
        LOG.info("Table {} alter hive table location {}", mixedHiveTable.name(), hiveDataLocation);
      } catch (IOException e) {
        LOG.warn("Table {} alter hive table location failed", mixedHiveTable.name(), e);
        throw new RuntimeException(e);
      }
    } else {
      List<String> partitions =
          HivePartitionUtil.getHivePartitionNames(mixedHiveCatalog.getHMSClient(), tableIdentifier);
      List<String> partitionLocations =
          HivePartitionUtil.getHivePartitionLocations(
              mixedHiveCatalog.getHMSClient(), tableIdentifier);
      for (int i = 0; i < partitionLocations.size(); i++) {
        String partition = partitions.get(i);
        String oldLocation = partitionLocations.get(i);
        String newLocation =
            hiveDataLocation
                + "/"
                + partition
                + "/"
                + HiveTableUtil.newHiveSubdirectory(DEFAULT_TXID);
        io.makeDirectories(newLocation);

        io.listDirectory(oldLocation)
            .forEach(
                p -> {
                  if (!p.isDirectory()) {
                    io.asFileSystemIO().rename(p.location(), newLocation);
                  }
                });
        HivePartitionUtil.alterPartition(
            mixedHiveCatalog.getHMSClient(), tableIdentifier, partition, newLocation);
      }
    }
    HiveMetaSynchronizer.syncHiveDataToMixedTable(mixedHiveTable, mixedHiveCatalog.getHMSClient());
    hiveTable = HiveTableUtil.loadHmsTable(mixedHiveCatalog.getHMSClient(), tableIdentifier);
    fillPartitionProperties(mixedHiveTable, mixedHiveCatalog, hiveTable);
  }

  /**
   * Check whether the storage format is supported.
   *
   * @param hiveClient Hive client from MixedHiveClient
   * @param tableIdentifier A table identifier
   * @return Support or not
   */
  private static boolean formatCheck(
      HMSClientPool hiveClient, TableIdentifier tableIdentifier, Map<String, String> properties)
      throws IOException {
    AtomicBoolean isSupport = new AtomicBoolean(false);
    try {
      hiveClient.run(
          client -> {
            Table hiveTable = HiveTableUtil.loadHmsTable(hiveClient, tableIdentifier);
            StorageDescriptor storageDescriptor = hiveTable.getSd();
            SerDeInfo serDeInfo = storageDescriptor.getSerdeInfo();
            switch (storageDescriptor.getInputFormat()) {
              case HiveTableProperties.PARQUET_INPUT_FORMAT:
                if (storageDescriptor
                        .getOutputFormat()
                        .equals(HiveTableProperties.PARQUET_OUTPUT_FORMAT)
                    && serDeInfo
                        .getSerializationLib()
                        .equals(HiveTableProperties.PARQUET_ROW_FORMAT_SERDE)) {
                  properties.put(DEFAULT_FILE_FORMAT, FILE_FORMAT_PARQUET);
                  properties.put(BASE_FILE_FORMAT, FILE_FORMAT_PARQUET);
                  properties.put(CHANGE_FILE_FORMAT, FILE_FORMAT_PARQUET);
                  isSupport.set(true);
                } else {
                  throw new IllegalStateException(
                      "Please check your hive table storage format for parquet is right");
                }
                break;
              case HiveTableProperties.ORC_INPUT_FORMAT:
                if (storageDescriptor
                        .getOutputFormat()
                        .equals(HiveTableProperties.ORC_OUTPUT_FORMAT)
                    && serDeInfo
                        .getSerializationLib()
                        .equals(HiveTableProperties.ORC_ROW_FORMAT_SERDE)) {
                  properties.put(DEFAULT_FILE_FORMAT, FILE_FORMAT_ORC);
                  properties.put(BASE_FILE_FORMAT, FILE_FORMAT_ORC);
                  properties.put(CHANGE_FILE_FORMAT, FILE_FORMAT_ORC);
                  isSupport.set(true);
                } else {
                  throw new IllegalStateException(
                      "Please check your hive table storage format for orc is right");
                }
                break;
              default:
                isSupport.set(false);
                break;
            }
            return null;
          });
    } catch (Exception e) {
      throw new IOException(e);
    }
    return isSupport.get();
  }

  @VisibleForTesting
  static void fillPartitionProperties(
      MixedTable table, MixedHiveCatalog mixedHiveCatalog, Table hiveTable) {
    UnkeyedHiveTable baseTable;
    if (table.isKeyedTable()) {
      baseTable = (UnkeyedHiveTable) table.asKeyedTable().baseTable();
    } else {
      baseTable = (UnkeyedHiveTable) table.asUnkeyedTable();
    }
    UpdatePartitionProperties updatePartitionProperties = baseTable.updatePartitionProperties(null);
    if (table.spec().isUnpartitioned()) {
      if (hasPartitionProperties(baseTable, false, null)) {
        return;
      }
      updatePartitionProperties.set(
          TablePropertyUtil.EMPTY_STRUCT,
          HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION,
          baseTable.hiveLocation());
      updatePartitionProperties.set(
          TablePropertyUtil.EMPTY_STRUCT,
          HiveTableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME,
          hiveTable.getParameters().get("transient_lastDdlTime"));
    } else {
      List<Partition> partitions =
          HivePartitionUtil.getHiveAllPartitions(mixedHiveCatalog.getHMSClient(), table.id());
      partitions.forEach(
          partition -> {
            StructLike partitionData =
                HivePartitionUtil.buildPartitionData(partition.getValues(), table.spec());
            if (hasPartitionProperties(baseTable, true, partitionData)) {
              return;
            }
            updatePartitionProperties.set(
                partitionData,
                HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION,
                partition.getSd().getLocation());
            updatePartitionProperties.set(
                partitionData,
                HiveTableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME,
                partition.getParameters().get("transient_lastDdlTime"));
          });
    }
    updatePartitionProperties.commit();
  }

  private static boolean hasPartitionProperties(
      UnkeyedHiveTable baseTable, boolean isPartitioned, StructLike partitionData) {
    Map<String, String> partitionProperties =
        isPartitioned
            ? baseTable.partitionProperty().get(partitionData)
            : baseTable.partitionProperty().get(TablePropertyUtil.EMPTY_STRUCT);
    return partitionProperties != null
        && partitionProperties.containsKey(
            HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION)
        && partitionProperties.containsKey(
            HiveTableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME);
  }
}

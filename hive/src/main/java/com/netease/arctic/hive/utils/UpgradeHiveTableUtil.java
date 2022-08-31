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

import com.netease.arctic.hive.catalog.ArcticHiveCatalog;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UpgradeHiveTableUtil {

  private static final Logger LOG = LoggerFactory.getLogger(UpgradeHiveTableUtil.class);

  private static final String DEFAULT_TXID = "txid=0";

  public static void upgradeHiveTable(ArcticHiveCatalog ac, TableIdentifier tableIdentifier,
                                      List<String> pkList, Map<String, String> properties) throws Exception {
    boolean upgradeHive = false;
    try {
      Table hiveTable = HiveTableUtil.loadHmsTable(ac.getHMSClient(), tableIdentifier);

      List<FieldSchema> hiveSchema = hiveTable.getSd().getCols();
      hiveSchema.addAll(hiveTable.getPartitionKeys());
      Schema schema = org.apache.iceberg.hive.HiveSchemaUtil.convert(hiveSchema);
      List<FieldSchema> partitionKeys = hiveTable.getPartitionKeys();

      PartitionSpec.Builder partitionBuilder = PartitionSpec.builderFor(schema);
      partitionKeys.stream().forEach(p -> partitionBuilder.identity(p.getName()));

      PrimaryKeySpec.Builder primaryKeyBuilder = PrimaryKeySpec.builderFor(schema);
      pkList.stream().forEach(p -> primaryKeyBuilder.addColumn(p));

      ArcticTable arcticTable = ac.newTableBuilder(tableIdentifier, schema)
          .withProperties(properties)
          .withPartitionSpec(partitionBuilder.build())
          .withPrimaryKeySpec(primaryKeyBuilder.build())
          .withProperty(TableProperties.ALLOW_HIVE_TABLE_EXISTED, "true")
          .create();
      upgradeHive = true;
      UpgradeHiveTableUtil.hiveDataMigration(arcticTable, ac, tableIdentifier);
    } catch (Throwable t) {
      if (upgradeHive) {
        ac.dropTableButNotDropHiveTable(tableIdentifier);
      }
      throw t;
    }
  }

  public static void hiveDataMigration(ArcticTable arcticTable, ArcticHiveCatalog ac, TableIdentifier tableIdentifier)
      throws Exception {
    Table hiveTable = HiveTableUtil.loadHmsTable(ac.getHMSClient(), tableIdentifier);
    String hiveDataLocation = hiveTable.getSd().getLocation() + "/hive";
    arcticTable.io().mkdirs(hiveDataLocation);
    String newPath;
    if (hiveTable.getPartitionKeys().isEmpty()) {
      newPath = hiveDataLocation + "/" + System.currentTimeMillis() + "_" + UUID.randomUUID();
      arcticTable.io().mkdirs(newPath);
      for (FileStatus fileStatus : arcticTable.io().list(hiveTable.getSd().getLocation())) {
        if (hiveTable.getPartitionKeys().isEmpty()) {
          if (!fileStatus.isDirectory()) {
            arcticTable.io().rename(fileStatus.getPath().toString(), newPath);
          }
        }
      }

      try {
        HiveTableUtil.alterTableLocation(ac.getHMSClient(), arcticTable.id(), newPath);
        LOG.info("table{" + arcticTable.name() + "} alter hive table location " + hiveDataLocation + " success");
      } catch (IOException e) {
        LOG.warn("table{" + arcticTable.name() + "} alter hive table location failed", e);
        throw new RuntimeException(e);
      }
    } else {
      List<String> partitions = HivePartitionUtil.getHivePartitionNames(ac.getHMSClient(), tableIdentifier);
      List<String> partitionLocations = HivePartitionUtil.getHivePartitionLocations(ac.getHMSClient(), tableIdentifier);
      for (int i = 0; i < partitionLocations.size(); i++) {
        String partition = partitions.get(i);
        String oldLocation = partitionLocations.get(i);
        String newLocation = hiveDataLocation + "/" + partition + "/" + DEFAULT_TXID;
        arcticTable.io().mkdirs(newLocation);
        for (FileStatus fileStatus : arcticTable.io().list(oldLocation)) {
          if (!fileStatus.isDirectory()) {
            arcticTable.io().rename(fileStatus.getPath().toString(), newLocation);
          }
        }
        HivePartitionUtil.alterPartition(ac.getHMSClient(), tableIdentifier, partition, newLocation);
      }
    }
    HiveMetaSynchronizer.syncHiveDataToArctic(arcticTable, ac.getHMSClient());
  }
}

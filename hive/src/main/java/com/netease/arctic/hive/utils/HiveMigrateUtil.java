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
import com.netease.arctic.table.TableIdentifier;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.hive.metastore.api.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class HiveMigrateUtil {

  private static final Logger LOG = LoggerFactory.getLogger(HiveMigrateUtil.class);

  private static final String DEFAULT_TXID = "txid=0";

  public static void hiveDataMigration(ArcticTable arcticTable, ArcticHiveCatalog ac, TableIdentifier tableIdentifier)
      throws Exception {
    Table hiveTable = HiveTableUtil.loadHmsTable(ac.getHMSClient(), tableIdentifier);
    String hiveDataLocation = hiveTable.getSd().getLocation() + "/hive";
    arcticTable.io().mkdirs(hiveDataLocation);
    String newPath = null;
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
      List<String> partitions = HiveTableUtil.getHivePartitionNames(ac.getHMSClient(), tableIdentifier);
      List<String> partitionLocations = HiveTableUtil.getHivePartitionLocations(ac.getHMSClient(), tableIdentifier);
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
        HiveTableUtil.alterPartition(ac.getHMSClient(), tableIdentifier, partition, newLocation);
      }
    }
    HiveMetaSynchronizer.syncHiveDataToArctic(arcticTable, ac.getHMSClient());
  }
}

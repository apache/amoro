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
import com.netease.arctic.hive.op.OverwriteHiveFiles;
import com.netease.arctic.io.ArcticHadoopFileIO;
import com.netease.arctic.op.OverwriteBaseFiles;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.MetricsConfig;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.data.TableMigrationUtil;
import org.apache.iceberg.mapping.NameMappingParser;
import org.apache.iceberg.relocated.com.google.common.collect.ListMultimap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Multimaps;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Synchronize the metadata of the hive table to arctic table
 */
public class HiveMetaSynchronizer {

  private static final Logger LOG = LoggerFactory.getLogger(HiveMetaSynchronizer.class);

  /**
   * Synchronize the schema change of the hive table to arctic table
   * @param table arctic table to accept the schema change
   * @param hiveClient hive client
   */
  public static void syncHiveSchemaToArctic(ArcticTable table, HMSClient hiveClient) {
    try {
      Table hiveTable = hiveClient.run(client -> client.getTable(table.id().getDatabase(), table.id().getTableName()));
      List<FieldSchema> fieldSchemas =  hiveTable.getSd().getCols();
      Schema hiveSchema = org.apache.iceberg.hive.HiveSchemaUtil.convert(fieldSchemas);
      UpdateSchema updateSchema = table.updateSchema();
      boolean update = false;
      for (int i = 0; i < hiveSchema.columns().size(); i++) {
        Types.NestedField hiveField = hiveSchema.columns().get(i);
        Types.NestedField icebergField = table.schema().findField(hiveField.name());
        if (icebergField == null) {
          updateSchema.addColumn(hiveField.name(), hiveField.type(), hiveField.doc());
          update = true;
          LOG.info("Sync new hive column {} to arctic", hiveField);
        } else if (!icebergField.type().equals(hiveField.type()) ||
            !Objects.equals(icebergField.doc(), (hiveField.doc()))) {
          updateSchema.updateColumn(icebergField.name(), icebergField.type().asPrimitiveType(), hiveField.doc());
          update = true;
          LOG.info("Sync updated hive column {} to arctic", icebergField);
        }
      }
      if (update) {
        updateSchema.commit();
      }
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to get hive table:" + table.id(), e);
    }
  }

  /**
   * Synchronize the data change of the hive table to arctic table
   * @param table arctic table to accept the data change
   * @param hiveClient hive client
   */
  public static void syncHiveDataToArctic(ArcticTable table, HMSClient hiveClient) {
    UnkeyedTable baseStore;
    if (table.isKeyedTable()) {
      baseStore = table.asKeyedTable().baseTable();
    } else {
      baseStore = table.asUnkeyedTable();
    }
    try {
      if (table.spec().isUnpartitioned()) {
        Table hiveTable =
            hiveClient.run(client -> client.getTable(table.id().getDatabase(), table.id().getTableName()));
        String hiveTransientTime =  hiveTable.getParameters().get("transient_lastDdlTime");
        String arcticTransientTime = baseStore.partitionProperty().get(TablePropertyUtil.EMPTY_STRUCT)
            .get(TableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME);
        if (arcticTransientTime == null || !arcticTransientTime.equals(hiveTransientTime)) {
          List<DataFile> hiveDataFiles = TableMigrationUtil.listPartition(Maps.newHashMap(),
              hiveTable.getSd().getLocation(),
              table.properties().getOrDefault(TableProperties.DEFAULT_FILE_FORMAT,
                  TableProperties.DEFAULT_FILE_FORMAT_DEFAULT),
              table.spec(), ((ArcticHadoopFileIO)table.io()).getTableMetaStore().getConfiguration(),
              MetricsConfig.fromProperties(table.properties()),
              NameMappingParser.fromJson(
                  table.properties().get(org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING)));
          List<DataFile> deleteFiles = Lists.newArrayList();
          baseStore.newScan().planFiles().forEach(fileScanTask -> deleteFiles.add(fileScanTask.file()));
          overwriteTable(table, deleteFiles, hiveDataFiles);
        }
      } else {
        List<Partition> hivePartitions = hiveClient.run(client -> client.listPartitions(table.id().getDatabase(),
            table.id().getTableName(), Short.MAX_VALUE));
        ListMultimap<StructLike, DataFile> filesGroupedByPartition
            = Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
        TableScan tableScan = baseStore.newScan();
        for (org.apache.iceberg.FileScanTask fileScanTask : tableScan.planFiles()) {
          filesGroupedByPartition.put(fileScanTask.file().partition(),fileScanTask.file());
        }
        Map<StructLike, Collection<DataFile>> filesMap = filesGroupedByPartition.asMap();
        List<DataFile> filesToDelete = Lists.newArrayList();
        List<DataFile> filesToAdd = Lists.newArrayList();
        List<StructLike> icebergPartitions = Lists.newArrayList(filesMap.keySet());
        for (Partition hivePartition : hivePartitions) {
          StructLike partitionData = HivePartitionUtil.buildPartitionData(hivePartition.getValues(), table.spec());
          icebergPartitions.remove(partitionData);
          String hiveTransientTime =  hivePartition.getParameters().get("transient_lastDdlTime");
          String arcticTransientTime = baseStore.partitionProperty().containsKey(partitionData) ?
              baseStore.partitionProperty().get(partitionData)
                  .get(TableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME) : null;
          if (arcticTransientTime == null || !arcticTransientTime.equals(hiveTransientTime)) {
            List<DataFile> hiveDataFiles = TableMigrationUtil.listPartition(
                buildPartitionValueMap(hivePartition.getValues(), table.spec()),
                hivePartition.getSd().getLocation(),
                table.properties().getOrDefault(TableProperties.DEFAULT_FILE_FORMAT,
                    TableProperties.DEFAULT_FILE_FORMAT_DEFAULT),
                table.spec(), ((ArcticHadoopFileIO)table.io()).getTableMetaStore().getConfiguration(),
                MetricsConfig.fromProperties(table.properties()), NameMappingParser.fromJson(
                    table.properties().get(org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING)));
            //TODO identify new partition is created by hive or arctic
            if (filesMap.get(partitionData) != null) {
              filesToDelete.addAll(filesMap.get(partitionData));
            }
            filesToAdd.addAll(hiveDataFiles);
          }
        }
        //TODO identify dropped partition is dropped by hive or arctic
        icebergPartitions.forEach(partition -> filesToDelete.addAll(filesMap.get(partition)));
        overwriteTable(table, filesToDelete, filesToAdd);
      }
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to get hive table:" + table.id(), e);
    }
  }

  private static Map<String, String> buildPartitionValueMap(List<String> partitionValues, PartitionSpec spec) {
    Map<String, String> partitionValueMap = Maps.newHashMap();
    for (int i = 0; i < partitionValues.size(); i++) {
      partitionValueMap.put(spec.fields().get(i).name(), partitionValues.get(i));
    }
    return partitionValueMap;
  }

  private static void overwriteTable(ArcticTable table, List<DataFile> filesToDelete, List<DataFile> filesToAdd) {
    if (filesToDelete.size() > 0 || filesToAdd.size() > 0) {
      LOG.info("Sync hive data change to arctic, delete files: {}, add files {}",
          filesToDelete.stream().map(DataFile::path).collect(Collectors.toList()),
          filesToAdd.stream().map(DataFile::path).collect(Collectors.toList()));
      if (table.isKeyedTable()) {
        long txId = table.asKeyedTable().beginTransaction(null);
        OverwriteBaseFiles overwriteBaseFiles = table.asKeyedTable().newOverwriteBaseFiles();
        overwriteBaseFiles.set(OverwriteHiveFiles.PROPERTIES_VALIDATE_LOCATION, "false");
        filesToDelete.forEach(overwriteBaseFiles::deleteFile);
        filesToAdd.forEach(overwriteBaseFiles::addFile);
        overwriteBaseFiles.withTransactionId(txId);
        overwriteBaseFiles.commit();
      } else {
        OverwriteFiles overwriteFiles = table.asUnkeyedTable().newOverwrite();
        overwriteFiles.set(OverwriteHiveFiles.PROPERTIES_VALIDATE_LOCATION, "false");
        filesToDelete.forEach(overwriteFiles::deleteFile);
        filesToAdd.forEach(overwriteFiles::addFile);
        overwriteFiles.commit();
      }
    }
  }
}

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
import com.netease.arctic.table.ArcticTable;
import org.apache.hadoop.hive.metastore.PartitionDropOptions;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.List;

public class HivePartitionUtil {

  public static List<String> partitionValuesAsList(StructLike partitionData, Types.StructType partitionSchema) {
    List<Types.NestedField> fields = partitionSchema.fields();
    List<String> values = Lists.newArrayList();
    for (int i = 0; i < fields.size(); i++) {
      Type type = fields.get(i).type();
      Object value = partitionData.get(i, type.typeId().javaClass());
      values.add(value.toString());
    }
    return values;
  }

  public static StructLike buildPartitionData(List<String> partitionValues, PartitionSpec spec) {
    StringBuilder pathBuilder = new StringBuilder();
    for (int i = 0; i < spec.partitionType().fields().size(); i++) {
      Types.NestedField field = spec.partitionType().fields().get(i);
      pathBuilder.append(field.name()).append("=").append(partitionValues.get(i));
      if (i > 0) {
        pathBuilder.append("/");
      }
    }
    return DataFiles.data(spec, pathBuilder.toString());
  }

  public static Partition newPartition(
      Table hiveTable,
      List<String> values,
      String location,
      List<DataFile> dataFiles,
      int createTimeInSeconds) {
    StorageDescriptor tableSd = hiveTable.getSd();
    PrincipalPrivilegeSet privilegeSet = hiveTable.getPrivileges();
    Partition p = new Partition();
    p.setValues(values);
    p.setDbName(hiveTable.getDbName());
    p.setTableName(hiveTable.getTableName());
    p.setCreateTime(createTimeInSeconds);
    p.setLastAccessTime(createTimeInSeconds);
    StorageDescriptor sd = tableSd.deepCopy();
    sd.setLocation(location);
    p.setSd(sd);

    HiveTableUtil.generateTableProperties(createTimeInSeconds, dataFiles)
        .forEach((key, value) -> p.putToParameters(key, value));

    if (privilegeSet != null) {
      p.setPrivileges(privilegeSet.deepCopy());
    }
    return p;
  }

  public static void rewriteHivePartitions(Partition partition, String location, List<DataFile> dataFiles,
      int accessTimestamp) {
    partition.getSd().setLocation(location);
    partition.setLastAccessTime(accessTimestamp);
    HiveTableUtil.generateTableProperties(accessTimestamp, dataFiles)
        .forEach(partition::putToParameters);
  }


  public static Partition getPartition(HMSClient hmsClient,
                                       ArcticTable arcticTable,
                                       List<String> partitionValues,
                                       String partitionLocation) {
    String db = arcticTable.id().getDatabase();
    String tableName = arcticTable.id().getTableName();
    Table hiveTable;
    try {
      hiveTable = hmsClient.run(c -> c.getTable(db, tableName));
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    try {
      return hmsClient.run(client -> {
        Partition partition;
        try {
          partition = client.getPartition(db, tableName, partitionValues);
          return partition;
        } catch (NoSuchObjectException noSuchObjectException) {
          partition = newPartition(hiveTable, partitionValues, partitionLocation, Collections.emptyList());
          client.add_partition(partition);
          return partition;
        }
      });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void addPartition(HMSClient hmsClient,
                                  ArcticTable arcticTable,
                                  List<String> partitionValues,
                                  String partitionLocation) {
    String db = arcticTable.id().getDatabase();
    String tableName = arcticTable.id().getTableName();
    Table hiveTable;
    try {
      hiveTable = hmsClient.run(c -> c.getTable(db, tableName));
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    try {
      hmsClient.run(client -> {
        Partition partition = newPartition(hiveTable, partitionValues, partitionLocation, Collections.emptyList());
        client.add_partition(partition);
        return partition;
      });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void dropPartition(HMSClient hmsClient,
                                   ArcticTable arcticTable,
                                   Partition hivePartition) {
    try {
      hmsClient.run(client -> {
        PartitionDropOptions options = PartitionDropOptions.instance()
            .deleteData(false)
            .ifExists(true)
            .purgeData(false)
            .returnResults(false);
        return client.dropPartition(arcticTable.id().getDatabase(),
            arcticTable.id().getTableName(), hivePartition.getValues(), options);
      });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void updatePartitionLocation(HMSClient hmsClient,
                                             ArcticTable arcticTable,
                                             Partition hivePartition,
                                             String newLocation) {
    dropPartition(hmsClient, arcticTable, hivePartition);
    addPartition(hmsClient, arcticTable, hivePartition.getValues(), newLocation);
  }
}

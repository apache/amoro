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

}

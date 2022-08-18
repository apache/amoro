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
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
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

  public static Partition newPartition(
      Table hiveTable,
      List<String> values,
      String location,
      List<DataFile> dataFiles) {
    StorageDescriptor tableSd = hiveTable.getSd();
    PrincipalPrivilegeSet privilegeSet = hiveTable.getPrivileges();
    int lastAccessTime = (int) (System.currentTimeMillis() / 1000);
    Partition p = new Partition();
    p.setValues(values);
    p.setDbName(hiveTable.getDbName());
    p.setTableName(hiveTable.getTableName());
    p.setCreateTime(lastAccessTime);
    p.setLastAccessTime(lastAccessTime);
    StorageDescriptor sd = tableSd.deepCopy();
    sd.setLocation(location);
    p.setSd(sd);

    int files = dataFiles.size();
    long totalSize = dataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
    p.putToParameters("transient_lastDdlTime", lastAccessTime + "");
    p.putToParameters("totalSize", totalSize + "");
    p.putToParameters("numFiles", files + "");
    if (privilegeSet != null) {
      p.setPrivileges(privilegeSet.deepCopy());
    }
    return p;
  }
}

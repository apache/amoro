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
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.table.ArcticTable;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class HiveTableUtil {

  private static final Logger LOG = LoggerFactory.getLogger(HiveTableUtil.class);

  public static boolean isHive(ArcticTable arcticTable) {
    return arcticTable instanceof SupportHive;
  }


  public static org.apache.hadoop.hive.metastore.api.Table loadHmsTable(
      HMSClient hiveClient, ArcticTable arcticTable) {
    try {
      return hiveClient.run(client -> client.getTable(
          arcticTable.id().getDatabase(),
          arcticTable.id().getTableName()));
    } catch (NoSuchObjectException nte) {
      LOG.trace("Table not found {}", arcticTable.id().toString(), nte);
      return null;
    } catch (TException e) {
      throw new RuntimeException(String.format("Metastore operation failed for %s", arcticTable.id().toString()), e);
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
    return properties;
  }

  public static String hiveRootLocation(String tableLocation) {
    return tableLocation + "/hive";
  }
}

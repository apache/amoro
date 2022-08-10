/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.hive.utils;

import com.netease.arctic.hive.CachedHiveClientPool;
import com.netease.arctic.table.ArcticTable;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveTableUtils {

  private static final Logger LOG = LoggerFactory.getLogger(HiveTableUtils.class);

  public static org.apache.hadoop.hive.metastore.api.Table loadHmsTable(
      CachedHiveClientPool hiveClientPool, ArcticTable arcticTable) {
    try {
      return hiveClientPool.run(client -> client.getTable(
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

  public static void persistTable(CachedHiveClientPool hiveClientPool, org.apache.hadoop.hive.metastore.api.Table tbl) {
    try {
      hiveClientPool.run(client -> {
        client.alter_table("", "", tbl);
        return null;
      });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

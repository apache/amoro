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

package com.netease.arctic.hive.ops;

import com.netease.arctic.hive.CachedHiveClientPool;
import com.netease.arctic.hive.utils.HiveSchemaUtil;
import com.netease.arctic.hive.utils.HiveTableUtils;
import com.netease.arctic.op.KeyedSchemaUpdate;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;

/**
 * Schema evolution API implementation for {@link KeyedTable}.
 */
public class HiveSchemaUpdate extends KeyedSchemaUpdate {
  private static final Logger LOG = LoggerFactory.getLogger(HiveSchemaUpdate.class);

  private ArcticTable arcticTable;
  private Table baseTable;
  private CachedHiveClientPool hiveClientPool;

  public HiveSchemaUpdate(KeyedTable keyedTable, CachedHiveClientPool hiveClientPool) {
    super(keyedTable);
    this.arcticTable = keyedTable;
    this.baseTable = keyedTable.baseTable();
    this.hiveClientPool = hiveClientPool;
  }

  @Override
  public void commit() {
    syncSchemaToHive();
    super.commit();
  }

  private void syncSchemaToHive() {
    org.apache.hadoop.hive.metastore.api.Table tbl = HiveTableUtils.loadHmsTable(hiveClientPool, arcticTable);
    if (tbl == null) {
      tbl = newHmsTable(arcticTable);
    }
    tbl.setSd(storageDescriptor(baseTable.schema()));
    HiveTableUtils.persistTable(hiveClientPool, tbl);
  }

  private StorageDescriptor storageDescriptor(Schema schema) {
    final StorageDescriptor storageDescriptor = new StorageDescriptor();
    storageDescriptor.setCols(HiveSchemaUtil.hivePartitionFields(schema, baseTable.spec()));
    SerDeInfo serDeInfo = new SerDeInfo();
    storageDescriptor.setSerdeInfo(serDeInfo);
    return storageDescriptor;
  }

  private org.apache.hadoop.hive.metastore.api.Table newHmsTable(ArcticTable arcticTable) {
    final long currentTimeMillis = System.currentTimeMillis();

    org.apache.hadoop.hive.metastore.api.Table newTable =
        new org.apache.hadoop.hive.metastore.api.Table(arcticTable.id().getTableName(),
        arcticTable.id().getDatabase(),
        System.getProperty("user.name"),
        (int) currentTimeMillis / 1000,
        (int) currentTimeMillis / 1000,
        Integer.MAX_VALUE,
        null,
        Collections.emptyList(),
        new HashMap<>(),
        null,
        null,
        TableType.EXTERNAL_TABLE.toString());

    newTable.getParameters().put("EXTERNAL", "TRUE"); // using the external table type also requires this
    return newTable;
  }
}

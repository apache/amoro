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

package com.netease.arctic.hive.op;

import com.netease.arctic.hive.HMSClientPool;
import com.netease.arctic.hive.utils.HiveSchemaUtil;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.UpdateSchema;

/**
 * Schema evolution API implementation for {@link KeyedTable}.
 */
public class HiveSchemaUpdate extends BaseSchemaUpdate {
  private final ArcticTable arcticTable;
  private final HMSClientPool hiveClient;
  private final UpdateSchema updateSchema;
  private Boolean needSyncToHive = true;

  public HiveSchemaUpdate(ArcticTable arcticTable, HMSClientPool hiveClient,
                          UpdateSchema updateSchema, Boolean needSyncToHive) {
    super(arcticTable, updateSchema);
    this.arcticTable = arcticTable;
    this.hiveClient = hiveClient;
    this.updateSchema = updateSchema;
    this.needSyncToHive = needSyncToHive;
  }

  @Override
  public void commit() {
    this.updateSchema.commit();
    if (HiveTableUtil.loadHmsTable(hiveClient, arcticTable.id()) == null) {
      throw new RuntimeException(String.format("there is no such hive table named %s", arcticTable.id().toString()));
    }
    if (needSyncToHive) {
      HiveSchemaUtil.syncSchemaToHive(arcticTable, hiveClient);
    }
  }
}

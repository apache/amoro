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

package com.netease.arctic.hive.table;

import com.netease.arctic.AmsClient;
import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.op.HiveOperationTransaction;
import com.netease.arctic.hive.op.HiveSchemaUpdate;
import com.netease.arctic.hive.op.OverwriteHiveFiles;
import com.netease.arctic.hive.op.ReplaceHivePartitions;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.BaseUnkeyedTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateSchema;

import static com.netease.arctic.table.TableProperties.BASE_HIVE_LOCATION_ROOT;

/**
 * Implementation of {@link com.netease.arctic.table.UnkeyedTable} with Hive table as base store.
 */
public class UnkeyedHiveTable extends BaseUnkeyedTable implements BaseTable, SupportHive {

  HMSClient hiveClient;
  String tableLocation;

  public UnkeyedHiveTable(
      TableIdentifier tableIdentifier,
      Table icebergTable,
      ArcticFileIO arcticFileIO,
      String tableLocation,
      AmsClient client,
      HMSClient hiveClient) {
    super(tableIdentifier, icebergTable, arcticFileIO, client);
    this.hiveClient = hiveClient;
    this.tableLocation = tableLocation;
  }

  @Override
  public ReplacePartitions newReplacePartitions() {
    ReplacePartitions replacePartitions = super.newReplacePartitions();
    return new ReplaceHivePartitions(replacePartitions, this, hiveClient, hiveClient);
  }

  @Override
  public String name() {
    return id().getTableName();
  }

  @Override
  public String hiveLocation() {
    return properties().containsKey(BASE_HIVE_LOCATION_ROOT) ?
        properties().get(BASE_HIVE_LOCATION_ROOT) :
        tableLocation + "/hive";
  }

  @Override
  public OverwriteHiveFiles newOverwrite() {
    OverwriteFiles overwriteFiles = super.newOverwrite();
    return new OverwriteHiveFiles(overwriteFiles, this, hiveClient, hiveClient);
  }

  @Override
  public Transaction newTransaction() {
    Transaction transaction = super.newTransaction();
    return new HiveOperationTransaction(this, transaction, hiveClient);
  }

  @Override
  public UpdateSchema updateSchema() {
    return new HiveSchemaUpdate(this, hiveClient, super.updateSchema());
  }
}

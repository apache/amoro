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
import com.netease.arctic.hive.op.ReplaceHivePartitions;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.BaseUnkeyedTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;

/**
 * Implementation of {@link com.netease.arctic.table.UnkeyedTable} with Hive table as base store.
 */
public class UnkeyedHiveTable extends BaseUnkeyedTable implements BaseTable {

  HMSClient hiveClient;

  public UnkeyedHiveTable(
      TableIdentifier tableIdentifier,
      Table icebergTable,
      ArcticFileIO arcticFileIO,
      AmsClient client,
      HMSClient hiveClient) {
    super(tableIdentifier, icebergTable, arcticFileIO, client);
    this.hiveClient = hiveClient;
  }

  @Override
  public ReplacePartitions newReplacePartitions() {
    ReplacePartitions replacePartitions = super.newReplacePartitions();
    return new ReplaceHivePartitions(replacePartitions, this, hiveClient, hiveClient);
  }

  @Override
  public Transaction newTransaction() {
    Transaction transaction = super.newTransaction();
    return new HiveOperationTransaction(this, transaction, hiveClient);
  }
}

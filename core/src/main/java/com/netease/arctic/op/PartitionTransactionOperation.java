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

package com.netease.arctic.op;

import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.util.StructLikeMap;

/**
 * Abstract transaction operation on {@link BaseTable} which will change
 * max transaction id map
 */
public abstract class PartitionTransactionOperation {

  KeyedTable keyedTable;

  public PartitionTransactionOperation(KeyedTable baseTable) {
    this.keyedTable = baseTable;
  }

  /**
   * Add some operation in transaction and change max transaction id map.
   *
   * @param transaction table transaction
   * @param partitionMaxTxId existing max transaction id map
   * @return changed max transaction id map
   */
  protected abstract StructLikeMap<Long> apply(Transaction transaction, StructLikeMap<Long> partitionMaxTxId);

  public void commit() {
    Transaction tx = keyedTable.baseTable().newTransaction();

    StructLikeMap<Long> partitionMaxTxId = apply(tx, keyedTable.partitionMaxTransactionId());

    String propertyValue = TablePropertyUtil.encodePartitionMaxTxId(keyedTable.spec(), partitionMaxTxId);
    UpdateProperties updateProperties = tx.updateProperties();
    updateProperties.set(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID, propertyValue);
    updateProperties.commit();

    tx.commitTransaction();
  }
}

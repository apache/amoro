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
import org.apache.iceberg.PendingUpdate;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.util.StructLikeMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract transaction operation on {@link BaseTable} which will change
 * max transaction id map
 */
public abstract class PartitionTransactionOperation implements PendingUpdate<StructLikeMap<Long>> {

  KeyedTable keyedTable;
  private Transaction tx;

  protected final Map<String, String> properties;

  public PartitionTransactionOperation(KeyedTable baseTable) {
    this.keyedTable = baseTable;
    this.properties = new HashMap<>();
  }

  public PartitionTransactionOperation set(String key, String value) {
    this.properties.put(key, value);
    return this;
  }

  /**
   * Add some operation in transaction and change max transaction id map.
   *
   * @param transaction table transaction
   * @param partitionMaxTxId existing max transaction id map
   * @return changed max transaction id map
   */
  protected abstract StructLikeMap<Long> apply(Transaction transaction, StructLikeMap<Long> partitionMaxTxId);

  @Override
  public StructLikeMap<Long> apply() {
    return apply(tx, TablePropertyUtil.getPartitionMaxTransactionId(keyedTable));
  }


  public void commit() {
    this.tx = keyedTable.baseTable().newTransaction();

    StructLikeMap<Long> partitionMaxSnapshotSequence = apply();
    UpdatePartitionProperties updatePartitionProperties = keyedTable.baseTable().updatePartitionProperties(tx);
    partitionMaxSnapshotSequence.forEach((partition, snapshotSequence) ->
        updatePartitionProperties.set(partition, TableProperties.PARTITION_MAX_TRANSACTION_ID,
            String.valueOf(snapshotSequence)));
    updatePartitionProperties.commit();

    tx.commitTransaction();
  }
}

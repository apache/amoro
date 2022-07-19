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

import com.netease.arctic.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;


public class RewritePartitionsTest extends TableTestBase {

  private long initTxId = 0;

  @Override
  public void before() {
    long txId = testKeyedTable.beginTransaction(System.currentTimeMillis() + "");
    List<DataFile> files = writeBaseNoCommit(testKeyedTable, txId, Lists.newArrayList(
        newGenericRecord(TABLE_SCHEMA, 1, "aaa", quickDate(1)),
        newGenericRecord(TABLE_SCHEMA, 2, "bbb", quickDate(2)),
        newGenericRecord(TABLE_SCHEMA, 3, "ccc", quickDate(3))
    ));
    this.initTxId = txId;

    RewritePartitions overwrite = ArcticOperations.newRewritePartitions(testKeyedTable);
    files.forEach(overwrite::addDataFile);
    overwrite.withTransactionId(txId);
    overwrite.commit();

    writeChange(PK_TABLE_ID, ChangeAction.INSERT, Lists.newArrayList(
        newGenericRecord(TABLE_SCHEMA, 4, "444", quickDate(1)),
        newGenericRecord(TABLE_SCHEMA, 5, "555", quickDate(2)),
        newGenericRecord(TABLE_SCHEMA, 6, "666", quickDate(3))
    ));

    // init. 3 partition with init txId
    StructLikeMap<Long> partitionMaxTxId = testKeyedTable.baseTable().partitionMaxTransactionId();
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, quickDate(1))
    ).longValue());
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, quickDate(2))
    ).longValue());
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, quickDate(3))
    ).longValue());

    testKeyedTable.baseTable().refresh();
    testKeyedTable.changeTable().refresh();

    List<Record> rows = readKeyedTable(testKeyedTable);
    // for init 6 record
    Assert.assertEquals(6, rows.size());
  }

  /**
   * overwrite partiton by data file.
   */
  @Test
  public void testDynamicOverwritePartition() {
    long txId = testKeyedTable.beginTransaction(System.currentTimeMillis() + "");
    List<Record> newRecords = Lists.newArrayList(
        newGenericRecord(TABLE_SCHEMA, 7, "777", quickDate(1)),
        newGenericRecord(TABLE_SCHEMA, 8, "888", quickDate(1)),
        newGenericRecord(TABLE_SCHEMA, 9, "999", quickDate(1))
    );
    List<DataFile> newFiles = writeBaseNoCommit(testKeyedTable, txId, newRecords);
    RewritePartitions overwrite = ArcticOperations.newRewritePartitions(testKeyedTable);
    newFiles.forEach(overwrite::addDataFile);
    overwrite.withTransactionId(txId);
    overwrite.commit();
    // overwrite 1 partition by data file

    StructLikeMap<Long> partitionMaxTxId = testKeyedTable.baseTable().partitionMaxTransactionId();
    // expect result: 1 partition with new txId, 2,3 partition use old txId
    Assert.assertEquals(txId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, quickDate(1))
    ).longValue());
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, quickDate(2))
    ).longValue());
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, quickDate(3))
    ).longValue());

    List<Record> rows = readKeyedTable(testKeyedTable);
    // partition1 -> base[7,8,9]
    // partition2 -> base[2], change[5]
    // partition3 -> base[3], change[6]
    Assert.assertEquals(7, rows.size());

    Set<Integer> resultIdSet = Sets.newHashSet();
    rows.forEach( r-> resultIdSet.add((Integer) r.get(0)));
    Assert.assertTrue(resultIdSet.contains(7));
    Assert.assertTrue(resultIdSet.contains(8));
    Assert.assertTrue(resultIdSet.contains(9));
    Assert.assertTrue(resultIdSet.contains(2));
    Assert.assertTrue(resultIdSet.contains(5));
    Assert.assertTrue(resultIdSet.contains(3));
    Assert.assertTrue(resultIdSet.contains(6));
  }
}

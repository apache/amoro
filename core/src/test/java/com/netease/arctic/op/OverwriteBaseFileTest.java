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
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;


public class OverwriteBaseFileTest extends TableTestBase {

  protected static final Schema TABLE_SCHEMA = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "name", Types.StringType.get()),
      Types.NestedField.required(3, "op_time", Types.StringType.get())
  );

  protected static final PartitionSpec SPEC = PartitionSpec.builderFor(TABLE_SCHEMA)
      .identity("op_time").build();

  private long initTxId;

  @Override
  public void setupTables() throws Exception {
    testCatalog = CatalogLoader.load(AMS.getUrl());
    tableDir = temp.newFolder();
    testKeyedTable = testCatalog
        .newTableBuilder(PK_TABLE_ID, TABLE_SCHEMA)
        .withProperty(TableProperties.LOCATION, tableDir.getPath() + "/pk_table")
        .withPartitionSpec(SPEC)
        .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
        .create().asKeyedTable();
    this.before();
  }

  @Override
  public void before() {
    long txId = testKeyedTable.beginTransaction(System.currentTimeMillis() + "");
    initTxId = TablePropertyUtil.allocateTransactionId(testKeyedTable);
    List<DataFile> files = writeBaseNoCommit(testKeyedTable, txId, Lists.newArrayList(
        newGenericRecord(TABLE_SCHEMA, 1, "aaa", "2020-1-1"),
        newGenericRecord(TABLE_SCHEMA, 2, "bbb", "2020-1-2"),
        newGenericRecord(TABLE_SCHEMA, 3, "ccc", "2020-1-3")
    ));

    RewritePartitions rewritePartitions = testKeyedTable.newRewritePartitions();
    files.forEach(rewritePartitions::addDataFile);
    rewritePartitions.withTransactionId(initTxId);
    rewritePartitions.commit();

    writeChange(PK_TABLE_ID, ChangeAction.INSERT, Lists.newArrayList(
        newGenericRecord(TABLE_SCHEMA, 4, "444", "2020-1-1"),
        newGenericRecord(TABLE_SCHEMA, 5, "555", "2020-1-2"),
        newGenericRecord(TABLE_SCHEMA, 6, "666", "2020-1-3"),
        newGenericRecord(TABLE_SCHEMA, 1024, "1024", "2020-1-4")
    ));

    // init. 3 partition with init txId
    StructLikeMap<Long> partitionMaxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testKeyedTable);
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-1")
    ).longValue());
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-2")
    ).longValue());
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-3")
    ).longValue());

    testKeyedTable.baseTable().refresh();
    testKeyedTable.changeTable().refresh();

    List<Record> rows = readKeyedTable(testKeyedTable);
    // for init 6 record
    Assert.assertEquals(7, rows.size());
  }

  /**
   * overwrite all partition, add new data files
   */
  @Test
  public void testOverwriteAllPartition() {
    long legacyTxId = testKeyedTable.beginTransaction(System.currentTimeMillis() + "");
    long txId = TablePropertyUtil.allocateTransactionId(testKeyedTable);
    List<Record> newRecords = Lists.newArrayList(
        newGenericRecord(TABLE_SCHEMA, 7, "777", "2020-1-1"),
        newGenericRecord(TABLE_SCHEMA, 8, "888", "2020-1-1"),
        newGenericRecord(TABLE_SCHEMA, 9, "999", "2020-1-1")
    );
    List<DataFile> newFiles = writeBaseNoCommit(testKeyedTable, legacyTxId, newRecords);
    OverwriteBaseFiles overwrite = testKeyedTable.newOverwriteBaseFiles();
    newFiles.forEach(overwrite::addFile);
    overwrite.overwriteByRowFilter(Expressions.alwaysTrue())
        .withLegacyTransactionId(legacyTxId)
        .withTransactionId(txId)
        .commit();
    // overwrite all partition and add new data file

    StructLikeMap<Long> partitionMaxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testKeyedTable);
    // expect result: all partition with new txId
    Assert.assertEquals(txId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-1")
    ).longValue());
    Assert.assertEquals(txId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-2")
    ).longValue());
    Assert.assertEquals(txId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-3")
    ).longValue());
    Assert.assertEquals(txId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-4")
    ).longValue());


    List<Record> rows = readKeyedTable(testKeyedTable);
    // partition1 -> base[7,8,9]
    Assert.assertEquals(3, rows.size());

    Set<Integer> resultIdSet = Sets.newHashSet();
    rows.forEach(r -> resultIdSet.add((Integer) r.get(0)));
    Assert.assertTrue(resultIdSet.contains(7));
    Assert.assertTrue(resultIdSet.contains(8));
    Assert.assertTrue(resultIdSet.contains(9));
  }

  @Test
  public void testOverwritePartitionByExpression() {
    long legacyTxId = testKeyedTable.beginTransaction(System.currentTimeMillis() + "");
    long txId = TablePropertyUtil.allocateTransactionId(testKeyedTable);
    List<Record> newRecords = Lists.newArrayList(
        newGenericRecord(TABLE_SCHEMA, 7, "777", "2020-1-1"),
        newGenericRecord(TABLE_SCHEMA, 8, "888", "2020-1-1"),
        newGenericRecord(TABLE_SCHEMA, 9, "999", "2020-1-1")
    );
    List<DataFile> newFiles = writeBaseNoCommit(testKeyedTable, legacyTxId, newRecords);
    OverwriteBaseFiles overwrite = testKeyedTable.newOverwriteBaseFiles();
    newFiles.forEach(overwrite::addFile);
    overwrite.withLegacyTransactionId(legacyTxId);
    overwrite.withTransactionId(txId);
    overwrite.overwriteByRowFilter(
        Expressions.or(
            Expressions.or(
                Expressions.equal("op_time", "2020-1-1"),
                Expressions.equal("op_time", "2020-1-2")
            ),
            Expressions.equal("op_time", "2020-1-4")
        )

    );
    overwrite.commit();
    // overwrite all partition and add new data file

    StructLikeMap<Long> partitionMaxTxId = TablePropertyUtil.getPartitionMaxTransactionId(testKeyedTable);
    // expect result: 1,2 partition with new txId, 3 partition use old txId
    Assert.assertEquals(txId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-1")
    ).longValue());
    Assert.assertEquals(txId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC,"2020-1-2")
    ).longValue());
    Assert.assertEquals(initTxId, partitionMaxTxId.get(
        partitionData(TABLE_SCHEMA, SPEC, "2020-1-3")
    ).longValue());

    List<Record> rows = readKeyedTable(testKeyedTable);
    // partition1 -> base[7,8,9]
    // partition3 -> base[3], change[6]
    Assert.assertEquals(5, rows.size());

    Set<Integer> resultIdSet = Sets.newHashSet();
    rows.forEach(r -> resultIdSet.add((Integer) r.get(0)));
    Assert.assertTrue(resultIdSet.contains(7));
    Assert.assertTrue(resultIdSet.contains(8));
    Assert.assertTrue(resultIdSet.contains(9));

    Assert.assertTrue(resultIdSet.contains(3));
    Assert.assertTrue(resultIdSet.contains(6));
  }
}

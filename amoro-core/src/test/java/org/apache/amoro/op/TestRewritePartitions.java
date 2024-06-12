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

package org.apache.amoro.op;

import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.io.TableDataTestBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class TestRewritePartitions extends TableDataTestBase {

  /** overwrite partition by data file. */
  @Test
  public void testDynamicOverwritePartition() {
    long txId = getMixedTable().asKeyedTable().beginTransaction(System.currentTimeMillis() + "");
    List<Record> newRecords =
        Lists.newArrayList(
            MixedDataTestHelpers.createRecord(7, "777", 0, "2022-01-01T12:00:00"),
            MixedDataTestHelpers.createRecord(8, "888", 0, "2022-01-01T12:00:00"),
            MixedDataTestHelpers.createRecord(9, "999", 0, "2022-01-01T12:00:00"));
    List<DataFile> newFiles =
        MixedDataTestHelpers.writeBaseStore(
            getMixedTable().asKeyedTable(), txId, newRecords, false);
    RewritePartitions rewritePartitions = getMixedTable().asKeyedTable().newRewritePartitions();
    newFiles.forEach(rewritePartitions::addDataFile);
    rewritePartitions.updateOptimizedSequenceDynamically(txId);
    rewritePartitions.commit();
    // rewrite 1 partition by data file

    StructLikeMap<Long> partitionOptimizedSequence =
        MixedTableUtil.readOptimizedSequence(getMixedTable().asKeyedTable());
    // expect result: 1 partition with new txId, 2,3 partition use old txId
    Assert.assertEquals(
        txId,
        partitionOptimizedSequence
            .get(MixedDataTestHelpers.recordPartition("2022-01-01T12:00:00"))
            .longValue());
    Assert.assertNull(
        partitionOptimizedSequence.get(
            MixedDataTestHelpers.recordPartition("2022-01-02T12:00:00")));
    Assert.assertNull(
        partitionOptimizedSequence.get(
            MixedDataTestHelpers.recordPartition("2022-01-03T12:00:00")));
    Assert.assertNull(
        partitionOptimizedSequence.get(
            MixedDataTestHelpers.recordPartition("2022-01-04T12:00:00")));

    List<Record> rows =
        MixedDataTestHelpers.readKeyedTable(
            getMixedTable().asKeyedTable(), Expressions.alwaysTrue());
    // partition1 -> base[7,8,9]
    // partition2 -> base[2]
    // partition3 -> base[3]
    Assert.assertEquals(5, rows.size());

    Set<Integer> resultIdSet = Sets.newHashSet();
    rows.forEach(r -> resultIdSet.add((Integer) r.get(0)));
    Assert.assertTrue(resultIdSet.contains(7));
    Assert.assertTrue(resultIdSet.contains(8));
    Assert.assertTrue(resultIdSet.contains(9));
    Assert.assertTrue(resultIdSet.contains(2));
    Assert.assertTrue(resultIdSet.contains(3));
  }
}
